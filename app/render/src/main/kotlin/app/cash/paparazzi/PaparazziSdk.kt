/*
 * Copyright (C) 2019 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paparazzi

import android.animation.AnimationHandler
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Handler
import android.os.Handler_Delegate
import android.os.Looper_Accessor
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.BridgeInflater
import android.view.Choreographer
import android.view.Choreographer_Delegate
import android.view.Display
import android.view.IWindow
import android.view.InputChannel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewRootImpl
import android.view.WindowManager
import android.view.WindowManagerGlobal
import android.view.WindowRelayoutResult
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.annotation.LayoutRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.WindowRecomposerPolicy
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.cash.paparazzi.agent.InterceptorRegistrar
import app.cash.paparazzi.internal.PaparazziCallback
import app.cash.paparazzi.internal.PaparazziLifecycleOwner
import app.cash.paparazzi.internal.PaparazziLogger
import app.cash.paparazzi.internal.PaparazziOnBackPressedDispatcherOwner
import app.cash.paparazzi.internal.PaparazziSavedStateRegistryOwner
import app.cash.paparazzi.internal.Renderer
import app.cash.paparazzi.internal.SessionParamsBuilder
import app.cash.paparazzi.internal.interceptors.EditModeInterceptor
import app.cash.paparazzi.internal.interceptors.RasterizeSkipAdvice
import app.cash.paparazzi.internal.parsers.LayoutPullParser
import com.android.ide.common.rendering.api.Result
import com.android.ide.common.rendering.api.Result.Status.ERROR_UNKNOWN
import com.android.ide.common.rendering.api.SessionParams
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.android.internal.lang.System_Delegate
import com.android.layoutlib.bridge.Bridge
import com.android.layoutlib.bridge.BridgeRenderSession
import com.android.layoutlib.bridge.impl.BridgeWindowSession
import com.android.layoutlib.bridge.impl.RenderAction
import com.android.layoutlib.bridge.impl.RenderSessionImpl
import com.android.resources.ScreenRound
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.android.asCoroutineDispatcher
import net.bytebuddy.agent.ByteBuddyAgent

public data class DriverFrameStatus(val hasPendingWork: Boolean)

public interface MountedDriverSession : AutoCloseable {
  public fun advanceFrame(nowNanos: Long): DriverFrameStatus

  public fun captureFrame(nowNanos: Long): BufferedImage
}

@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
public class PaparazziSdk
@JvmOverloads
constructor(
  private val environment: Environment,
  private val deviceConfig: DeviceConfig = DeviceConfig.NEXUS_5,
  private val theme: String = "android:Theme.Material.NoActionBar.Fullscreen",
  private val renderingMode: RenderingMode = RenderingMode.NORMAL,
  private val appCompatEnabled: Boolean = false,
  private val renderExtensions: Set<RenderExtension> = setOf(),
  private val supportsRtl: Boolean = false,
  private val showSystemUi: Boolean = false,
  private val onNewFrame: (BufferedImage) -> Unit = {},
) {
  private val logger = PaparazziLogger()
  private lateinit var renderSession: RenderSessionImpl
  private lateinit var bridgeRenderSession: BridgeRenderSession

  public val layoutInflater: LayoutInflater
    get() = RenderAction.getCurrentContext().getSystemService("layout_inflater") as BridgeInflater

  public val resources: Resources
    get() = RenderAction.getCurrentContext().resources

  public val context: Context
    get() = RenderAction.getCurrentContext()

  public fun setup() {
    if (!isInitialized) {
      registerViewEditModeInterception()
      registerRasterizeSkipInterception()
      ByteBuddyAgent.install()
      InterceptorRegistrar.registerMethodInterceptors()
    }
  }

  public fun prepare() {
    val layoutlibCallback =
      PaparazziCallback(logger, environment.packageName, environment.resourcePackageNames)
    layoutlibCallback.initResources()

    if (!isInitialized) {
      renderer = Renderer(environment, layoutlibCallback, logger)
      sessionParamsBuilder = renderer.prepare()
    }
    forcePlatformSdkVersion(environment.compileSdkVersion)

    sessionParamsBuilder =
      sessionParamsBuilder
        .copy(
          layoutPullParser = LayoutPullParser.createFromString(contentRoot(renderingMode)),
          deviceConfig = deviceConfig,
          renderingMode = renderingMode,
          supportsRtl = supportsRtl,
          decor = showSystemUi,
          logger = logger,
        )
        .withTheme(theme)

    ensureTouchModeWindowSession()
    val sessionParams = sessionParamsBuilder.build()
    renderSession = createRenderSession(sessionParams)
    renderSession.init(sessionParams.timeout)
    Bitmap.setDefaultDensity(DisplayMetrics.DENSITY_DEVICE_STABLE)

    if (appCompatEnabled) {
      initializeAppCompatIfPresent()
    }

    bridgeRenderSession = createBridgeSession(renderSession, renderSession.inflate())
  }

  public fun teardown() {
    if (::renderSession.isInitialized) {
      renderSession.release()
    }
    if (::bridgeRenderSession.isInitialized) {
      bridgeRenderSession.dispose()
    }
    Looper_Accessor.cleanupThread()

    if (isInitialized) {
      renderer.dumpDelegates()
    }
    logger.assertNoErrors()
  }

  @Suppress("UNCHECKED_CAST")
  public fun <V : View> inflate(@LayoutRes layoutId: Int): V =
    layoutInflater.inflate(layoutId, null) as V

  public fun snapshot(composable: @Composable () -> Unit) {
    val hostView =
      ComposeView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
      }
    hostView.setContent(composable)
    snapshot(hostView)
  }

  @JvmOverloads
  public fun snapshot(view: View, offsetMillis: Long = 0L) {
    val nanos = TimeUnit.MILLISECONDS.toNanos(offsetMillis)
    openSession(view).use { session ->
      session.advanceFrame(nanos)
      onNewFrame(session.captureFrame(nanos))
    }
  }

  public fun resetSession(
    deviceConfig: DeviceConfig? = null,
    theme: String? = null,
    renderingMode: RenderingMode? = null,
  ) {
    logger.flushErrors()
    renderSession.release()
    bridgeRenderSession.dispose()
    Looper_Accessor.cleanupThread()

    sessionParamsBuilder =
      sessionParamsBuilder.copy(
        layoutPullParser =
          LayoutPullParser.createFromString(contentRoot(renderingMode ?: this.renderingMode)),
      )

    if (deviceConfig != null) {
      sessionParamsBuilder = sessionParamsBuilder.copy(deviceConfig = deviceConfig)
    }

    if (theme != null) {
      sessionParamsBuilder = sessionParamsBuilder.withTheme(theme)
    }

    if (renderingMode != null) {
      sessionParamsBuilder = sessionParamsBuilder.copy(renderingMode = renderingMode)
    }

    ensureTouchModeWindowSession()
    val sessionParams = sessionParamsBuilder.build()
    renderSession = createRenderSession(sessionParams)
    renderSession.init(sessionParams.timeout)
    Bitmap.setDefaultDensity(DisplayMetrics.DENSITY_DEVICE_STABLE)
    bridgeRenderSession = createBridgeSession(renderSession, renderSession.inflate())
  }

  /**
   * Attaches [view] into the current render session and returns a frame-stepping session that keeps
   * [view] attached until [MountedDriverSession.close].
   */
  public fun openSession(view: View): MountedDriverSession {
    val viewGroup = bridgeRenderSession.rootViews[0].viewObject as ViewGroup
    val modifiedView =
      renderExtensions.fold(view) { currentView, renderExtension ->
        renderExtension.renderView(currentView)
      }

    System_Delegate.setNanosTime(0L)
    System_Delegate.setBootTimeNanos(0L)

    val previousUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      logger.error(throwable, throwable.message)
      previousUncaughtExceptionHandler?.uncaughtException(thread, throwable)
    }

    lateinit var lifecycleOwner: PaparazziLifecycleOwner
    var recomposer: Recomposer? = null

    return try {
      withTime(0L) {}

      if (hasComposeRuntime) {
        viewGroup.id = android.R.id.content
        WindowRecomposerPolicy.setFactory { rootView ->
          rootView.createLifecycleAwareWindowRecomposer(REAL_TIME_MAIN_DISPATCHER, null).also {
            recomposer = it
          }
        }
      }

      if (hasLifecycleOwnerRuntime) {
        lifecycleOwner = PaparazziLifecycleOwner()
        modifiedView.setViewTreeLifecycleOwner(lifecycleOwner)
        if (hasSavedStateRegistryOwnerRuntime) {
          modifiedView.setViewTreeSavedStateRegistryOwner(
            PaparazziSavedStateRegistryOwner(lifecycleOwner),
          )
        }
        if (hasAndroidxActivityRuntime) {
          modifiedView.setViewTreeOnBackPressedDispatcherOwner(
            PaparazziOnBackPressedDispatcherOwner(lifecycleOwner),
          )
        }
        lifecycleOwner.registry.currentState = Lifecycle.State.RESUMED
      }

      viewGroup.addView(modifiedView)

      object : MountedDriverSession {
        private var closed = false

        override fun advanceFrame(nowNanos: Long): DriverFrameStatus {
          check(!closed) { "Driver session already closed" }
          withTime(nowNanos) {
            prepareWindows()
            // Keep Compose layout, animations, and DisplayList recording (View#draw) active while
            // deferring Skia CPU rasterization (HardwareRenderer#syncAndDrawFrame) to captureFrame.
            // RenderThread-only animators such as ripples do not advance during settle.
            val result = withRasterizationSkipped { renderSession.render(true) }
            if (result.status == ERROR_UNKNOWN) throw result.exception
            // Drain resumed continuations so frame-clock awaiters register before reading pending.
            executeHandlerCallbacks()
          }
          val pending = recomposer?.hasPendingWork ?: false
          return DriverFrameStatus(hasPendingWork = pending)
        }

        override fun captureFrame(nowNanos: Long): BufferedImage {
          check(!closed) { "Driver session already closed" }
          withTime(nowNanos) {
            prepareWindows()
            val result = renderSession.render(true)
            if (result.status == ERROR_UNKNOWN) throw result.exception
          }
          val raw = bridgeRenderSession.recyclableImage!!.use { it.copy }
          return frameImage(raw)
        }

        override fun close() {
          if (closed) return
          closed = true
          try {
            if (hasLifecycleOwnerRuntime) {
              lifecycleOwner.registry.currentState = Lifecycle.State.DESTROYED
            }
            viewGroup.removeAllViews()
            if (modifiedView !== view) {
              (view.parent as? ViewGroup)?.removeView(view)
            }
            AnimationHandler.sAnimatorHandler.set(null)
            if (hasComposeRuntime) {
              forceReleaseComposeReferenceLeaks()
            }
            recomposer = null

            val choreographer = Choreographer.getInstance()
            val mLastFrameTimeNanos =
              choreographer::class.java.getDeclaredField("mLastFrameTimeNanos").apply {
                isAccessible = true
              }
            mLastFrameTimeNanos.set(choreographer, 0L)
          } finally {
            Thread.setDefaultUncaughtExceptionHandler(previousUncaughtExceptionHandler)
          }
        }
      }
    } catch (t: Throwable) {
      if (hasLifecycleOwnerRuntime) {
        runCatching { lifecycleOwner.registry.currentState = Lifecycle.State.DESTROYED }
      }
      runCatching { viewGroup.removeAllViews() }
      AnimationHandler.sAnimatorHandler.set(null)
      if (hasComposeRuntime) {
        runCatching { forceReleaseComposeReferenceLeaks() }
      }
      Thread.setDefaultUncaughtExceptionHandler(previousUncaughtExceptionHandler)
      throw t
    }
  }

  /**
   * Ensures [WindowManagerGlobal.sWindowSession] returns
   * [WindowManagerGlobal.ADD_FLAG_IN_TOUCH_MODE] when adding secondary windows so [ViewRootImpl]
   * keeps touch mode on first traversal instead of flipping to keyboard mode and assigning focus
   * highlights.
   */
  private fun ensureTouchModeWindowSession() {
    val current = windowSessionField.get(null)
    if (current !is TouchModeBridgeWindowSession) {
      windowSessionField.set(null, TouchModeBridgeWindowSession())
    }
  }

  /**
   * Prepares every window in [WindowManagerGlobal] before each render pass: forces touch mode so
   * secondary windows do not paint initial keyboard focus highlights, and invalidates the main
   * window's renderer root so secondary windows are composited on static base screens.
   */
  private fun prepareWindows() {
    val windows = WindowManagerGlobal.getInstance().windowViews
    for (v in windows) {
      val root = v.viewRootImpl ?: continue
      ensureTouchModeLocallyMethod.invoke(root, true)
    }
    val main = windows.firstOrNull()?.viewRootImpl ?: return
    val attachInfo = attachInfoField.get(main) ?: return
    val rendererField =
      threadedRendererField
        ?: attachInfo.javaClass.getDeclaredField("mThreadedRenderer").also {
          it.isAccessible = true
          threadedRendererField = it
        }
    val threadedRenderer = rendererField.get(attachInfo) ?: return
    val invalidate =
      invalidateRootMethod
        ?: threadedRenderer.javaClass.getDeclaredMethod("invalidateRoot").also {
          it.isAccessible = true
          invalidateRootMethod = it
        }
    invalidate.invoke(threadedRenderer)
  }

  private fun withTime(timeNanos: Long, block: () -> Unit) {
    System_Delegate.setNanosTime(timeNanos)
    try {
      executeHandlerCallbacks()
      val currentTimeNanos = uptimeNanos()
      Choreographer_Delegate.doFrame(currentTimeNanos)
      block()
    } catch (e: Throwable) {
      Bridge.getLog().error("broken", "Failed executing Choreographer#doFrame", e, null, null)
      throw e
    }
  }

  private fun createRenderSession(sessionParams: SessionParams): RenderSessionImpl {
    val renderSession = RenderSessionImpl(sessionParams)
    renderSession.setElapsedFrameTimeNanos(0L)
    return renderSession
  }

  private fun createBridgeSession(
    renderSession: RenderSessionImpl,
    result: Result,
  ): BridgeRenderSession {
    try {
      val bridgeSessionClass = Class.forName("com.android.layoutlib.bridge.BridgeRenderSession")
      val constructor =
        bridgeSessionClass.getDeclaredConstructor(RenderSessionImpl::class.java, Result::class.java)
      constructor.isAccessible = true
      val bridgeSession = constructor.newInstance(renderSession, result) as BridgeRenderSession
      val viewGroup = bridgeSession.rootViews[0].viewObject as ViewGroup
      Display::class.java.getDeclaredField("mRefreshRateChangesRegistered").apply {
        isAccessible = true
        set(viewGroup.display, true)
      }
      return bridgeSession
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  private fun frameImage(image: BufferedImage): BufferedImage {
    val activeConfig = sessionParamsBuilder.deviceConfig
    if (renderingMode == RenderingMode.NORMAL && activeConfig.screenRound == ScreenRound.ROUND) {
      val newImage = BufferedImage(image.width, image.height, image.type)
      val g = newImage.createGraphics()
      try {
        g.clip = Ellipse2D.Float(0f, 0f, image.height.toFloat(), image.width.toFloat())
        g.drawImage(image, 0, 0, image.width, image.height, null)
      } finally {
        g.dispose()
      }
      return newImage
    }
    return image
  }

  private fun forcePlatformSdkVersion(compileSdkVersion: Int) {
    val buildVersionClass =
      try {
        Class.forName("android.os.Build\$VERSION", true, PaparazziSdk::class.java.classLoader)
      } catch (_: ClassNotFoundException) {
        return
      }
    buildVersionClass.getFieldReflectively("SDK_INT").setStaticValue(compileSdkVersion)
  }

  private fun initializeAppCompatIfPresent() {
    lateinit var appCompatDelegateClass: Class<*>
    try {
      val appCompatDrawableManagerClass =
        Class.forName("androidx.appcompat.widget.AppCompatDrawableManager")
      val preloadMethod = appCompatDrawableManagerClass.getMethod("preload")
      preloadMethod.invoke(null)

      appCompatDelegateClass = Class.forName("androidx.appcompat.app.AppCompatDelegate")
    } catch (_: ClassNotFoundException) {
      logger.verbose("AppCompat not found on classpath")
      return
    }

    if (layoutInflater.factory == null) {
      layoutInflater.factory2 =
        object : LayoutInflater.Factory2 {
          override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet,
          ): View? {
            val appCompatViewInflaterClass =
              Class.forName("androidx.appcompat.app.AppCompatViewInflater")

            val createViewMethod =
              appCompatViewInflaterClass
                .getDeclaredMethod(
                  "createView",
                  View::class.java,
                  String::class.java,
                  Context::class.java,
                  AttributeSet::class.java,
                  Boolean::class.javaPrimitiveType,
                  Boolean::class.javaPrimitiveType,
                  Boolean::class.javaPrimitiveType,
                  Boolean::class.javaPrimitiveType,
                )
                .apply { isAccessible = true }

            val newAppCompatViewInflaterInstance =
              appCompatViewInflaterClass.getConstructor().newInstance()

            return createViewMethod.invoke(
              newAppCompatViewInflaterInstance,
              parent,
              name,
              context,
              attrs,
              true,
              true,
              true,
              true,
            ) as View?
          }

          override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? =
            onCreateView(null, name, context, attrs)
        }
    } else if (!appCompatDelegateClass.isAssignableFrom(layoutInflater.factory2::class.java)) {
      throw IllegalStateException(
        "The LayoutInflater already has a Factory installed so we can not install AppCompat's",
      )
    }
  }

  private fun registerViewEditModeInterception() {
    InterceptorRegistrar.addMethodInterceptor(
      "android.view.View",
      "isInEditMode",
      EditModeInterceptor::class.java,
    )
  }

  private fun registerRasterizeSkipInterception() {
    InterceptorRegistrar.addMethodAdvice(
      "android.graphics.HardwareRenderer",
      "syncAndDrawFrame",
      RasterizeSkipAdvice::class.java,
    )
  }

  private inline fun <T> withRasterizationSkipped(block: () -> T): T {
    val previous = RasterizeSkipAdvice.SKIP_RASTERIZATION.get()
    RasterizeSkipAdvice.SKIP_RASTERIZATION.set(true)
    return try {
      block()
    } finally {
      RasterizeSkipAdvice.SKIP_RASTERIZATION.set(previous)
    }
  }

  private fun forceReleaseComposeReferenceLeaks() {
    executeHandlerCallbacks()
  }

  private fun executeHandlerCallbacks() {
    synchronized(this) {
      val uptimeNanos = uptimeNanos()
      Handler_Delegate.executeCallbacks(uptimeNanos)
    }
  }

  private fun uptimeNanos() = android.os.SystemClock_Delegate.uptimeNanos()

  internal companion object {
    internal lateinit var renderer: Renderer
    internal val isInitialized: Boolean
      get() = ::renderer.isInitialized

    internal lateinit var sessionParamsBuilder: SessionParamsBuilder

    private val MAIN_DISPATCHER by lazy {
      Handler.getMain().asCoroutineDispatcher("Layoutlib-Main")
    }

    private val REAL_TIME_MAIN_DISPATCHER by lazy { RealTimeDelayDispatcher(MAIN_DISPATCHER) }

    private val ensureTouchModeLocallyMethod: Method by lazy {
      ViewRootImpl::class
        .java
        .getDeclaredMethod("ensureTouchModeLocally", Boolean::class.javaPrimitiveType)
        .apply { isAccessible = true }
    }

    private val attachInfoField: Field by lazy {
      ViewRootImpl::class.java.getDeclaredField("mAttachInfo").apply { isAccessible = true }
    }

    private val windowSessionField: Field by lazy {
      WindowManagerGlobal::class.java.getDeclaredField("sWindowSession").apply {
        isAccessible = true
      }
    }

    private var threadedRendererField: Field? = null
    private var invalidateRootMethod: Method? = null

    private val hasComposeRuntime: Boolean =
      isPresentInClasspath(
        "androidx.compose.runtime.snapshots.SnapshotKt",
        "androidx.compose.ui.platform.AndroidUiDispatcher",
      )
    private val hasLifecycleOwnerRuntime =
      isPresentInClasspath("androidx.lifecycle.ViewTreeLifecycleOwner")
    private val hasSavedStateRegistryOwnerRuntime =
      isPresentInClasspath("androidx.savedstate.SavedStateRegistryController\$Companion")
    private val hasAndroidxActivityRuntime =
      isPresentInClasspath("androidx.activity.ViewTreeOnBackPressedDispatcherOwner")

    private fun contentRoot(renderingMode: RenderingMode) =
      """
        |<?xml version="1.0" encoding="utf-8"?>
        |<${if (hasComposeRuntime) "app.cash.paparazzi.internal.ComposeViewAdapter" else "FrameLayout"}
        |     xmlns:android="http://schemas.android.com/apk/res/android"
        |              android:layout_width="${if (renderingMode.horizAction == RenderingMode.SizeAction.SHRINK) "wrap_content" else "match_parent"}"
        |              android:layout_height="${if (renderingMode.vertAction == RenderingMode.SizeAction.SHRINK) "wrap_content" else "match_parent"}"/>
      """
        .trimMargin()

    private fun isPresentInClasspath(vararg classNames: String): Boolean {
      return try {
        for (className in classNames) {
          Class.forName(className)
        }
        true
      } catch (_: ClassNotFoundException) {
        false
      }
    }
  }
}

/**
 * Runs `kotlinx.coroutines.delay()` in wall-clock time while keeping the Compose frame clock
 * virtual, matching the host renderer.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
internal class RealTimeDelayDispatcher(private val delegate: CoroutineDispatcher) :
  CoroutineDispatcher(), Delay {
  override fun dispatch(context: CoroutineContext, block: Runnable) =
    delegate.dispatch(context, block)

  override fun isDispatchNeeded(context: CoroutineContext) = delegate.isDispatchNeeded(context)

  override fun scheduleResumeAfterDelay(
    timeMillis: Long,
    continuation: CancellableContinuation<Unit>,
  ) {
    val future =
      SHARED_TIMER.schedule(
        {
          with(continuation) {
            delegate.dispatch(EmptyCoroutineContext) {
              with(this@RealTimeDelayDispatcher) { resumeUndispatched(Unit) }
            }
          }
        },
        timeMillis,
        TimeUnit.MILLISECONDS,
      )
    continuation.invokeOnCancellation { future.cancel(false) }
  }

  private companion object {
    val SHARED_TIMER = Executors.newSingleThreadScheduledExecutor { r ->
      Thread(r, "layoutlib-real-delay").apply { isDaemon = true }
    }
  }
}

private class TouchModeBridgeWindowSession : BridgeWindowSession() {
  override fun addToDisplayAsUser(
    window: IWindow?,
    attrs: WindowManager.LayoutParams?,
    viewVisibility: Int,
    displayId: Int,
    userId: Int,
    requestedVisibleTypes: Int,
    outInputChannel: InputChannel?,
    outRelayoutResult: WindowRelayoutResult?,
  ): Int =
    super.addToDisplayAsUser(
      window,
      attrs,
      viewVisibility,
      displayId,
      userId,
      requestedVisibleTypes,
      outInputChannel,
      outRelayoutResult,
    ) or WindowManagerGlobal.ADD_FLAG_IN_TOUCH_MODE
}
