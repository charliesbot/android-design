package bot.charlies.androiddesign.preview.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceProtocolTest {
  @Test
  fun `an idle worker is never stuck`() {
    assertFalse(DevicePingResponse().isWorkerStuck())
  }

  @Test
  fun `an op inside its budget is not stuck`() {
    val ping =
      DevicePingResponse(inFlightOp = "RENDER_BATCH", inFlightAgeMs = 900, inFlightBudgetMs = 1_000)
    assertFalse(ping.isWorkerStuck())
  }

  @Test
  fun `an op older than its budget is stuck`() {
    val ping =
      DevicePingResponse(
        inFlightOp = "PRELOAD_DEX",
        inFlightAgeMs = 30_001,
        inFlightBudgetMs = 30_000,
      )
    assertTrue(ping.isWorkerStuck())
  }

  @Test
  fun `ping liveness fields survive a JSON round trip and default when absent`() {
    val ping =
      DevicePingResponse(inFlightOp = "SET_LIVE", inFlightAgeMs = 12, inFlightBudgetMs = 34)
    val json = PreviewJson.compact.encodeToString(DevicePingResponse.serializer(), ping)
    assertEquals(ping, PreviewJson.compact.decodeFromString(DevicePingResponse.serializer(), json))
    val legacy =
      PreviewJson.compact.decodeFromString(DevicePingResponse.serializer(), """{"ok":true}""")
    assertFalse(legacy.isWorkerStuck())
  }

  @Test
  fun `the adb forward target names the abstract socket the service listens on`() {
    assertEquals("localabstract:${DeviceTransportSpec.SOCKET_NAME}", DeviceTransportSpec.ADB_REMOTE)
  }
}
