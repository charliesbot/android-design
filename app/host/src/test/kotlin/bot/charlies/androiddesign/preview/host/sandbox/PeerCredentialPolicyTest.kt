package bot.charlies.androiddesign.preview.host.sandbox

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PeerCredentialPolicyTest {
  @Test
  fun `the adb shell and root are trusted peers`() {
    assertTrue(PeerCredentialPolicy.isTrusted(2000))
    assertTrue(PeerCredentialPolicy.isTrusted(0))
  }

  @Test
  fun `ordinary app uids, system, and unknown peers are rejected`() {
    assertFalse(PeerCredentialPolicy.isTrusted(10_123))
    assertFalse(PeerCredentialPolicy.isTrusted(1000))
    assertFalse(PeerCredentialPolicy.isTrusted(-1))
  }

  @Test
  fun `the shell uid matches the platform constant`() {
    assertTrue(PeerCredentialPolicy.SHELL_UID == android.os.Process.SHELL_UID)
  }
}
