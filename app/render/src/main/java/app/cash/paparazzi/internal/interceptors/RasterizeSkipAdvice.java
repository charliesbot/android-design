package app.cash.paparazzi.internal.interceptors;

import net.bytebuddy.asm.Advice;

/**
 * Skips {@code android.graphics.HardwareRenderer#syncAndDrawFrame} during layoutlib settle frames
 * while keeping Compose layout, animation, and DisplayList recording ({@code View#draw}) intact.
 * Only {@code captureFrame} rasterizes pixels via Skia.
 */
public final class RasterizeSkipAdvice {
  public static final ThreadLocal<Boolean> SKIP_RASTERIZATION =
      ThreadLocal.withInitial(() -> Boolean.FALSE);

  private RasterizeSkipAdvice() {}

  public static boolean shouldSkip() {
    return Boolean.TRUE.equals(SKIP_RASTERIZATION.get());
  }

  @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
  public static boolean onEnter() {
    return RasterizeSkipAdvice.shouldSkip();
  }

  @Advice.OnMethodExit
  public static void onExit(
      @Advice.Enter boolean skipped, @Advice.Return(readOnly = false) int result) {
    if (skipped) {
      result = 0; // HardwareRenderer.SYNC_OK
    }
  }
}
