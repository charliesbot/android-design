/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.cash.paparazzi

import android.content.res.Configuration
import com.android.ide.common.rendering.api.HardwareConfig
import com.android.ide.common.resources.configuration.CountryCodeQualifier
import com.android.ide.common.resources.configuration.DensityQualifier
import com.android.ide.common.resources.configuration.FolderConfiguration
import com.android.ide.common.resources.configuration.KeyboardStateQualifier
import com.android.ide.common.resources.configuration.LayoutDirectionQualifier
import com.android.ide.common.resources.configuration.LocaleQualifier
import com.android.ide.common.resources.configuration.NavigationMethodQualifier
import com.android.ide.common.resources.configuration.NetworkCodeQualifier
import com.android.ide.common.resources.configuration.NightModeQualifier
import com.android.ide.common.resources.configuration.ScreenDimensionQualifier
import com.android.ide.common.resources.configuration.ScreenOrientationQualifier
import com.android.ide.common.resources.configuration.ScreenRatioQualifier
import com.android.ide.common.resources.configuration.ScreenRoundQualifier
import com.android.ide.common.resources.configuration.ScreenSizeQualifier
import com.android.ide.common.resources.configuration.TextInputMethodQualifier
import com.android.ide.common.resources.configuration.TouchScreenQualifier
import com.android.ide.common.resources.configuration.UiModeQualifier
import com.android.ide.common.resources.configuration.VersionQualifier
import com.android.resources.Density
import com.android.resources.Keyboard
import com.android.resources.KeyboardState
import com.android.resources.LayoutDirection
import com.android.resources.Navigation
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.android.resources.ScreenRatio
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import com.android.resources.TouchScreen
import com.android.resources.UiMode
import com.google.android.collect.Maps
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties
import kotlin.math.max
import kotlin.math.min
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Provides [FolderConfiguration] and [HardwareConfig] for device targets, plus utility methods to
 * parse `build.prop` and `attrs.xml`.
 */
public data class DeviceConfig(
  val screenHeight: Int = 1280,
  val screenWidth: Int = 768,
  val xdpi: Int = 320,
  val ydpi: Int = 320,
  val orientation: ScreenOrientation = ScreenOrientation.PORTRAIT,
  val uiMode: UiMode = UiMode.NORMAL,
  val nightMode: NightMode = NightMode.NOTNIGHT,
  val density: Density = Density.XHIGH,
  val fontScale: Float = 1f,
  val layoutDirection: LayoutDirection = LayoutDirection.LTR,
  val locale: String? = detectLocaleProperty(),
  val ratio: ScreenRatio = ScreenRatio.NOTLONG,
  val size: ScreenSize = ScreenSize.NORMAL,
  val keyboard: Keyboard = Keyboard.NOKEY,
  val touchScreen: TouchScreen = TouchScreen.FINGER,
  val keyboardState: KeyboardState = KeyboardState.SOFT,
  val softButtons: Boolean = true,
  val navigation: Navigation = Navigation.NONAV,
  val screenRound: ScreenRound? = null,
  val released: String = "November 13, 2012",
) {
  public val folderConfiguration: FolderConfiguration
    get() =
      FolderConfiguration.createDefault().apply {
        densityQualifier = DensityQualifier(density)
        navigationMethodQualifier = NavigationMethodQualifier(navigation)
        screenDimensionQualifier =
          when {
            screenWidth > screenHeight -> ScreenDimensionQualifier(screenWidth, screenHeight)
            else -> ScreenDimensionQualifier(screenHeight, screenWidth)
          }
        screenRatioQualifier = ScreenRatioQualifier(ratio)
        screenSizeQualifier = ScreenSizeQualifier(size)
        textInputMethodQualifier = TextInputMethodQualifier(keyboard)
        touchTypeQualifier = TouchScreenQualifier(touchScreen)
        keyboardStateQualifier = KeyboardStateQualifier(keyboardState)
        screenOrientationQualifier = ScreenOrientationQualifier(orientation)

        updateScreenWidthAndHeight()
        uiModeQualifier = UiModeQualifier(uiMode)
        nightModeQualifier = NightModeQualifier(nightMode)
        countryCodeQualifier = CountryCodeQualifier()
        layoutDirectionQualifier = LayoutDirectionQualifier(layoutDirection)
        networkCodeQualifier = NetworkCodeQualifier()
        localeQualifier =
          if (locale != null) LocaleQualifier.getQualifier(locale) else LocaleQualifier()
        versionQualifier = VersionQualifier()
        screenRoundQualifier = ScreenRoundQualifier(screenRound)
      }

  public val hardwareConfig: HardwareConfig
    get() =
      HardwareConfig(
        currentWidth,
        currentHeight,
        density,
        xdpi.toFloat(),
        ydpi.toFloat(),
        size,
        orientation,
        screenRound,
        softButtons,
      )

  public val uiModeMask: Int
    get() {
      val nightMask =
        if (nightMode == NightMode.NIGHT) {
          Configuration.UI_MODE_NIGHT_YES
        } else {
          Configuration.UI_MODE_NIGHT_NO
        }
      val typeMask =
        when (uiMode) {
          UiMode.NORMAL -> Configuration.UI_MODE_TYPE_NORMAL
          UiMode.DESK -> Configuration.UI_MODE_TYPE_DESK
          UiMode.CAR -> Configuration.UI_MODE_TYPE_CAR
          UiMode.TELEVISION -> Configuration.UI_MODE_TYPE_TELEVISION
          UiMode.APPLIANCE -> Configuration.UI_MODE_TYPE_APPLIANCE
          UiMode.WATCH -> Configuration.UI_MODE_TYPE_WATCH
          UiMode.VR_HEADSET -> Configuration.UI_MODE_TYPE_VR_HEADSET
        }
      return nightMask or typeMask
    }

  private val currentWidth: Int
    get() =
      when (orientation) {
        ScreenOrientation.PORTRAIT -> min(screenWidth, screenHeight)
        ScreenOrientation.LANDSCAPE -> max(screenWidth, screenHeight)
        else ->
          throw UnsupportedOperationException(
            "Only Portrait or Landscape orientations are supported",
          )
      }

  private val currentHeight: Int
    get() =
      when (orientation) {
        ScreenOrientation.PORTRAIT -> max(screenWidth, screenHeight)
        ScreenOrientation.LANDSCAPE -> min(screenWidth, screenHeight)
        else ->
          throw UnsupportedOperationException(
            "Only Portrait or Landscape orientations are supported",
          )
      }

  public companion object {
    @JvmField
    public val NEXUS_5: DeviceConfig =
      DeviceConfig(
        screenHeight = 1920,
        screenWidth = 1080,
        xdpi = 445,
        ydpi = 445,
        orientation = ScreenOrientation.PORTRAIT,
        density = Density.XXHIGH,
        ratio = ScreenRatio.NOTLONG,
        size = ScreenSize.NORMAL,
        keyboard = Keyboard.NOKEY,
        touchScreen = TouchScreen.FINGER,
        keyboardState = KeyboardState.SOFT,
        softButtons = true,
        navigation = Navigation.NONAV,
        released = "October 31, 2013",
      )

    private const val TAG_ATTR = "attr"
    private const val TAG_ENUM = "enum"
    private const val TAG_FLAG = "flag"
    private const val ATTR_NAME = "name"
    private const val ATTR_VALUE = "value"

    @Throws(IOException::class)
    internal fun loadProperties(path: File): Map<String, String> {
      val p = Properties()
      val map = Maps.newHashMap<String, String>()
      FileInputStream(path).use { p.load(it) }
      for (key in p.stringPropertyNames()) {
        map[key] = p.getProperty(key)
      }
      return map
    }

    @Throws(IOException::class, XmlPullParserException::class)
    internal fun getEnumMap(path: File): Map<String, Map<String, Int>> {
      val map = mutableMapOf<String, MutableMap<String, Int>>()

      val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
      FileInputStream(path).use { stream ->
        xmlPullParser.setInput(stream, null)
        var eventType = xmlPullParser.eventType
        var attr: String? = null
        while (eventType != XmlPullParser.END_DOCUMENT) {
          if (eventType == XmlPullParser.START_TAG) {
            if (TAG_ATTR == xmlPullParser.name) {
              attr = xmlPullParser.getAttributeValue(null, ATTR_NAME)
            } else if (TAG_ENUM == xmlPullParser.name || TAG_FLAG == xmlPullParser.name) {
              val name = xmlPullParser.getAttributeValue(null, ATTR_NAME)
              val value = xmlPullParser.getAttributeValue(null, ATTR_VALUE)
              // Integer.decode cannot handle "ffffffff", see JDK issue 6624867
              val i = (java.lang.Long.decode(value) as Long).toInt()
              require(attr != null)
              var attributeMap: MutableMap<String, Int>? = map[attr]
              if (attributeMap == null) {
                attributeMap = Maps.newHashMap()
                map[attr] = attributeMap
              }
              attributeMap[name] = i
            }
          } else if (eventType == XmlPullParser.END_TAG) {
            if (TAG_ATTR == xmlPullParser.name) {
              attr = null
            }
          }
          eventType = xmlPullParser.next()
        }
      }

      return map
    }

    internal fun detectLocaleProperty(): String? =
      System.getProperty("app.cash.paparazzi.defaultLocale")?.takeIf { it.isNotEmpty() }
  }
}
