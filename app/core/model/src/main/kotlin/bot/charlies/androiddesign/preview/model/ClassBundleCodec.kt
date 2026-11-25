package bot.charlies.androiddesign.preview.model

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.nio.charset.StandardCharsets

/**
 * Deterministic binary codec for encoding and decoding a `Map<String, ByteArray>` of compiled JVM
 * class files over the worker wire protocol.
 */
object ClassBundleCodec {
  fun encode(classes: Map<String, ByteArray>): ByteArray {
    val sorted = classes.entries.sortedBy { it.key }
    val baos = ByteArrayOutputStream()
    DataOutputStream(baos).use { out ->
      out.writeInt(sorted.size)
      for ((name, bytes) in sorted) {
        val nameBytes = name.toByteArray(StandardCharsets.UTF_8)
        out.writeInt(nameBytes.size)
        out.write(nameBytes)
        out.writeInt(bytes.size)
        out.write(bytes)
      }
    }
    return baos.toByteArray()
  }

  fun decode(bundleBytes: ByteArray): Map<String, ByteArray> {
    val inp = DataInputStream(ByteArrayInputStream(bundleBytes))
    val count = inp.readInt()
    if (count < 0) throw EOFException("Invalid class count: $count")
    val result = LinkedHashMap<String, ByteArray>(count)
    repeat(count) {
      val nameLen = inp.readInt()
      if (nameLen < 0) throw EOFException("Invalid class name length: $nameLen")
      val nameBytes = ByteArray(nameLen)
      inp.readFully(nameBytes)
      val classLen = inp.readInt()
      if (classLen < 0) throw EOFException("Invalid class bytecode length: $classLen")
      val classBytes = ByteArray(classLen)
      inp.readFully(classBytes)
      result[String(nameBytes, StandardCharsets.UTF_8)] = classBytes
    }
    return result
  }
}
