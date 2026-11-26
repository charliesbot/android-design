package bot.charlies.androiddesign.preview.model

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import kotlinx.serialization.KSerializer

/**
 * Shared binary framing helpers for length-prefixed JSON and byte packets over stream transports.
 */
internal object WireFraming {
  const val DEFAULT_MAX_JSON_BYTES = 16 * 1024 * 1024
  const val DEFAULT_MAX_BLOB_BYTES = 64 * 1024 * 1024

  fun readOpOrNull(inp: DataInputStream): Int? =
    try {
      inp.readUnsignedByte()
    } catch (_: EOFException) {
      null
    }

  fun writeUtf8(out: DataOutputStream, text: String) {
    writeBytes(out, text.toByteArray(Charsets.UTF_8))
  }

  fun readUtf8(inp: DataInputStream, maxBytes: Int = DEFAULT_MAX_JSON_BYTES): String =
    readBytes(inp, maxBytes, "utf8").toString(Charsets.UTF_8)

  fun writeBytes(out: DataOutputStream, bytes: ByteArray) {
    out.writeInt(bytes.size)
    if (bytes.isNotEmpty()) {
      out.write(bytes)
    }
  }

  fun readBytes(
    inp: DataInputStream,
    maxBytes: Int = DEFAULT_MAX_BLOB_BYTES,
    label: String = "blob",
  ): ByteArray {
    val len = inp.readInt()
    if (len < 0 || len > maxBytes) {
      throw EOFException("Invalid $label byte length $len (max $maxBytes)")
    }
    val bytes = ByteArray(len)
    if (len > 0) inp.readFully(bytes)
    return bytes
  }

  fun writeOptionalBytes(out: DataOutputStream, bytes: ByteArray?) {
    if (bytes == null) {
      out.writeInt(-1)
    } else {
      out.writeInt(bytes.size)
      if (bytes.isNotEmpty()) out.write(bytes)
    }
  }

  fun readOptionalBytes(
    inp: DataInputStream,
    maxBytes: Int = DEFAULT_MAX_BLOB_BYTES,
    label: String = "optional blob",
  ): ByteArray? {
    val len = inp.readInt()
    if (len == -1) return null
    if (len < 0 || len > maxBytes) {
      throw EOFException("Invalid $label length $len (max $maxBytes)")
    }
    val bytes = ByteArray(len)
    if (len > 0) inp.readFully(bytes)
    return bytes
  }

  fun <T> writeJson(out: DataOutputStream, serializer: KSerializer<T>, value: T) {
    val json = PreviewJson.compact.encodeToString(serializer, value).toByteArray(Charsets.UTF_8)
    out.writeInt(json.size)
    out.write(json)
  }

  fun <T> readJson(
    inp: DataInputStream,
    maxBytes: Int = DEFAULT_MAX_JSON_BYTES,
    label: String,
    serializer: KSerializer<T>,
  ): T {
    val len = inp.readInt()
    if (len <= 0 || len > maxBytes) {
      throw EOFException("Invalid $label packet length $len (max $maxBytes)")
    }
    val buf = ByteArray(len)
    inp.readFully(buf)
    return PreviewJson.compact.decodeFromString(serializer, String(buf, Charsets.UTF_8))
  }
}
