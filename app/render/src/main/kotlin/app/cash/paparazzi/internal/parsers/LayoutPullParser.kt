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

package app.cash.paparazzi.internal.parsers

import com.android.SdkConstants.ATTR_IGNORE
import com.android.SdkConstants.EXPANDABLE_LIST_VIEW
import com.android.SdkConstants.GRID_VIEW
import com.android.SdkConstants.LIST_VIEW
import com.android.SdkConstants.SPINNER
import com.android.SdkConstants.TOOLS_URI
import com.android.ide.common.rendering.api.ILayoutPullParser
import com.android.ide.common.rendering.api.ResourceNamespace
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOError
import java.io.InputStream
import java.nio.charset.StandardCharsets
import org.xmlpull.v1.XmlPullParserException

/**
 * A layout parser that holds an in-memory tree of a given resource for subsequent traversal during
 * the inflation process.
 */
internal class LayoutPullParser : InMemoryParser, AaptAttrParser, ILayoutPullParser {
  private constructor(inputStream: InputStream) : super() {
    try {
      val bytes = inputStream.use { it.readBytes() }

      setFeature(FEATURE_PROCESS_NAMESPACES, true)
      setInput(ByteArrayInputStream(bytes), null)

      val resourceParser = ResourceParser(ByteArrayInputStream(bytes))
      root = resourceParser.createTagSnapshot()

      declaredAaptAttrs = findDeclaredAaptAttrs(root)
    } catch (e: XmlPullParserException) {
      throw IOError(e)
    }
  }

  private constructor(aaptResource: TagSnapshot) : super() {
    root = aaptResource
    declaredAaptAttrs = emptyMap()
  }

  private val root: TagSnapshot
  private val declaredAaptAttrs: Map<String, TagSnapshot>

  private var layoutNamespace = ResourceNamespace.RES_AUTO

  override fun rootTag() = root

  override fun getViewCookie(): Any? {
    val name = super.getName() ?: return null

    if (LIST_VIEW == name || EXPANDABLE_LIST_VIEW == name || GRID_VIEW == name || SPINNER == name) {
      var map: MutableMap<String, String>? = null
      val count = attributeCount
      for (i in 0 until count) {
        val namespace = getAttributeNamespace(i)
        if (namespace == TOOLS_URI) {
          val attribute = getAttributeName(i)!!
          if (attribute == ATTR_IGNORE) {
            continue
          }
          if (map == null) {
            map = HashMap(4)
          }
          map[attribute] = getAttributeValue(i)!!
        }
      }

      return map
    }

    return null
  }

  override fun getLayoutNamespace(): ResourceNamespace = layoutNamespace

  override fun getAaptDeclaredAttrs(): Map<String, TagSnapshot> = declaredAaptAttrs

  fun setLayoutNamespace(layoutNamespace: ResourceNamespace) {
    this.layoutNamespace = layoutNamespace
  }

  private fun findDeclaredAaptAttrs(tag: TagSnapshot): Map<String, TagSnapshot> {
    if (!tag.hasDeclaredAaptAttrs) {
      return emptyMap()
    }

    return buildMap {
      tag.attributes.filterIsInstance<AaptAttrSnapshot>().forEach { attr ->
        val bundledTag = attr.bundledTag
        put(attr.id, bundledTag)
        for (child in bundledTag.children) {
          putAll(findDeclaredAaptAttrs(child))
        }
      }
      for (child in tag.children) {
        putAll(findDeclaredAaptAttrs(child))
      }
    }
  }

  companion object {
    @Throws(FileNotFoundException::class)
    fun createFromFile(layoutFile: File) = LayoutPullParser(FileInputStream(layoutFile))

    fun createFromString(contents: String): LayoutPullParser {
      return LayoutPullParser(
        ByteArrayInputStream(contents.toByteArray(StandardCharsets.UTF_8)),
      )
    }

    fun createFromAaptResource(aaptResource: TagSnapshot): LayoutPullParser {
      return LayoutPullParser(aaptResource)
    }
  }
}
