package bot.charlies.androiddesign.preview.domain.image

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32
import java.util.zip.Deflater

/**
 * Fast RGBA_8888 to PNG encoder using [Deflater.BEST_SPEED] to avoid `ImageIO` ARGB/filter overhead
 * on 2x and 4K desktop captures.
 */
object FastPngEncoder {
  private val PNG_SIG = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

  fun encodeFast(width: Int, height: Int, rgba: ByteArray): ByteArray {
    require(width > 0 && height > 0) { "PNG dimensions must be positive: ${width}x${height}" }
    val rowStride = width * 4
    require(rgba.size >= height * rowStride) {
      "Expected at least ${height * rowStride} RGBA bytes, got ${rgba.size}"
    }
    val idatRaw = ByteArray(height * (1 + rowStride))
    var src = 0
    var dst = 0
    for (y in 0 until height) {
      idatRaw[dst++] = 0 // filter: None
      System.arraycopy(rgba, src, idatRaw, dst, rowStride)
      src += rowStride
      dst += rowStride
    }

    val deflater = Deflater(Deflater.BEST_SPEED)
    val idatBytes =
      try {
        deflater.setInput(idatRaw)
        deflater.finish()
        val compressedOut = ByteArrayOutputStream(idatRaw.size / 3 + 1024)
        val buf = ByteArray(32768)
        while (!deflater.finished()) {
          val n = deflater.deflate(buf)
          compressedOut.write(buf, 0, n)
        }
        compressedOut.toByteArray()
      } finally {
        deflater.end()
      }

    val out = ByteArrayOutputStream(idatBytes.size + 128)
    out.write(PNG_SIG)
    val ihdr = ByteArray(13)
    writeIntBE(ihdr, 0, width)
    writeIntBE(ihdr, 4, height)
    ihdr[8] = 8 // bit depth
    ihdr[9] = 6 // color type RGBA
    ihdr[10] = 0 // compression
    ihdr[11] = 0 // filter
    ihdr[12] = 0 // interlace
    writeChunk(out, "IHDR", ihdr)
    writeChunk(out, "IDAT", idatBytes)
    writeChunk(out, "IEND", ByteArray(0))
    return out.toByteArray()
  }

  private fun writeChunk(out: ByteArrayOutputStream, type: String, data: ByteArray) {
    val typeBytes = type.toByteArray(StandardCharsets.US_ASCII)
    val lenBuf = ByteArray(4)
    writeIntBE(lenBuf, 0, data.size)
    out.write(lenBuf)
    out.write(typeBytes)
    out.write(data)
    val crc = CRC32()
    crc.update(typeBytes)
    crc.update(data)
    val crcBuf = ByteArray(4)
    writeIntBE(crcBuf, 0, crc.value.toInt())
    out.write(crcBuf)
  }

  private fun writeIntBE(buf: ByteArray, offset: Int, v: Int) {
    buf[offset] = ((v ushr 24) and 0xFF).toByte()
    buf[offset + 1] = ((v ushr 16) and 0xFF).toByte()
    buf[offset + 2] = ((v ushr 8) and 0xFF).toByte()
    buf[offset + 3] = (v and 0xFF).toByte()
  }
}
