package bot.charlies.androiddesign.preview.model

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.EOFException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClassBundleCodecTest {
  @Test
  fun `encode and decode round-trip classes deterministically`() {
    val classesA =
      linkedMapOf(
        "ZClass" to byteArrayOf(1, 2, 3),
        "AClass\$Inner" to byteArrayOf(4, 5, 6, 7),
        "Empty" to byteArrayOf(),
      )
    val classesB =
      linkedMapOf(
        "Empty" to byteArrayOf(),
        "AClass\$Inner" to byteArrayOf(4, 5, 6, 7),
        "ZClass" to byteArrayOf(1, 2, 3),
      )

    val encodedA = ClassBundleCodec.encode(classesA)
    val encodedB = ClassBundleCodec.encode(classesB)
    assertContentEquals(encodedA, encodedB)

    val decoded = ClassBundleCodec.decode(encodedA)
    assertEquals(setOf("AClass\$Inner", "Empty", "ZClass"), decoded.keys)
    assertContentEquals(byteArrayOf(1, 2, 3), decoded["ZClass"])
    assertContentEquals(byteArrayOf(4, 5, 6, 7), decoded["AClass\$Inner"])
    assertContentEquals(byteArrayOf(), decoded["Empty"])
  }

  @Test
  fun `decode rejects negative lengths`() {
    val baos = ByteArrayOutputStream()
    DataOutputStream(baos).use { it.writeInt(-1) }
    assertFailsWith<EOFException> { ClassBundleCodec.decode(baos.toByteArray()) }
  }
}
