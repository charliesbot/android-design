package bot.charlies.androiddesign.preview.host.sandbox

import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DeviceMemoryStats
import bot.charlies.androiddesign.preview.model.PaletteVariant

internal class VirtualClockComposeRenderer(
  internal val assetCache: DeviceAssetCache = DeviceAssetCache(),
) {
  private val mainHandler = Handler(Looper.getMainLooper())

  private data class LoadedDex(val loader: PolicyDexClassLoader, val board: ExplorationBoard)

  private val dexCache = LruCache<String, LoadedDex>(MAX_CACHED_DEX)
  @Volatile private var reloadCount: Int = 0

  fun cachedDexHashes(): List<String> = dexCache.snapshot().keys.toList()

  fun currentReloadCount(): Int = reloadCount

  fun collectMemoryStats(): DeviceMemoryStats {
    val rt = Runtime.getRuntime()
    val usedJava = rt.totalMemory() - rt.freeMemory()
    val dbgMem = Debug.MemoryInfo()
    Debug.getMemoryInfo(dbgMem)
    return DeviceMemoryStats(
      javaHeapUsedBytes = usedJava,
      javaHeapMaxBytes = rt.maxMemory(),
      nativeHeapAllocatedBytes = Debug.getNativeHeapAllocatedSize(),
      nativeHeapSizeBytes = Debug.getNativeHeapSize(),
      totalPssKb = dbgMem.totalPss,
      nativePssKb = dbgMem.nativePss,
      dalvikPssKb = dbgMem.dalvikPss,
    )
  }

  @Synchronized
  fun setLive(
    dexBytes: ByteArray?,
    request: DeviceLiveStateRequest,
  ) {
    val loaded = resolveBoard(request.dexHash, request.entryClass, dexBytes)
    val board = loaded.board
    val direction =
      request.direction?.let { name -> board.directions.firstOrNull { it.name == name } }
        ?: board.directions.first()
    val screenId = request.screen?.takeIf { it in direction.screens } ?: direction.startScreen
    val variant = request.variantOverride?.let { PaletteVariant.fromWire(it) }
    val designSystem =
      board.resolveDesignSystem(
        designMd = request.designTheme,
        seedOverride = request.seedOverride,
        variantOverride = variant,
      )
    val dark = request.darkOverride ?: board.surfaceSpec.views.first().dark
    val resolver = assetCache.resolverFor(request.assetMap)
    val nextSession =
      LiveSession(
        dexHash = request.dexHash,
        board = board,
        direction = direction,
        screenId = screenId,
        overlayId = request.overlayId,
        designSystem = designSystem,
        dark = dark,
        fontScale = request.fontScale,
        assetResolver = resolver,
      )
    mainHandler.post { LivePreviewState.session.value = nextSession }
  }

  private fun resolveBoard(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray?,
  ): LoadedDex {
    dexCache.get(dexHash)?.let {
      return it
    }
    val bytes =
      dexBytes?.takeIf { it.isNotEmpty() }
        ?: throw IllegalArgumentException("DEX '$dexHash' is not preloaded and no bytes supplied")
    return loadDex(dexHash, entryClass, bytes, cacheResult = true)
  }

  private fun loadDex(
    dexHash: String,
    entryClass: String,
    dexBytes: ByteArray,
    cacheResult: Boolean,
  ): LoadedDex {
    val loader =
      PolicyDexClassLoader(dexBytes, VirtualClockComposeRenderer::class.java.classLoader!!)
    val clazz = loader.loadClass(entryClass)
    val getter =
      clazz.declaredMethods.firstOrNull {
        it.name == "getPreview" &&
          java.lang.reflect.Modifier.isStatic(it.modifiers) &&
          it.parameterCount == 0
      }
        ?: clazz.declaredMethods.firstOrNull {
          it.name == "preview" &&
            java.lang.reflect.Modifier.isStatic(it.modifiers) &&
            it.parameterCount == 0
        }
        ?: clazz.declaredMethods.firstOrNull {
          java.lang.reflect.Modifier.isStatic(it.modifiers) &&
            it.parameterCount == 0 &&
            ExplorationBoard::class.java.isAssignableFrom(it.returnType)
        }
        ?: error("No getPreview() method in $entryClass")
    val board = getter.invoke(null) as ExplorationBoard
    reloadCount++
    val loaded = LoadedDex(loader, board)
    if (cacheResult) {
      dexCache.put(dexHash, loaded)
    }
    return loaded
  }

  companion object {
    private const val MAX_CACHED_DEX = 4
  }
}
