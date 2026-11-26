package app.cash.paparazzi.internal.interceptors;

import java.util.concurrent.atomic.AtomicInteger;
import net.bytebuddy.asm.Advice;

/**
 * Skips {@code android.graphics.HardwareRenderer#syncAndDrawFrame} during layoutlib settle frames
 * while keeping Compose layout, animation, and DisplayList recording ({@code View#draw}) intact.
 * Only {@code captureFrame} rasterizes pixels via Skia.
 */
public final class RasterizeSkipAdvice {
  public static final ThreadLocal<Boolean> SKIP_RASTERIZATION =
      ThreadLocal.withInitial(() -> Boolean.FALSE);

  public static final AtomicInteger SKIPPED_COUNT = new AtomicInteger();
  public static final AtomicInteger RASTERIZED_COUNT = new AtomicInteger();

  private RasterizeSkipAdvice() {}

  public static void resetCounters() {
    SKIPPED_COUNT.set(0);
    RASTERIZED_COUNT.set(0);
  }

  public static boolean recordAndShouldSkip() {
    if (Boolean.TRUE.equals(SKIP_RASTERIZATION.get())) {
      SKIPPED_COUNT.incrementAndGet();
      return true;
    }
    RASTERIZED_COUNT.incrementAndGet();
    return false;
  }

  @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
  public static boolean onEnter() {
    return RasterizeSkipAdvice.recordAndShouldSkip();
  }

  @Advice.OnMethodExit
  public static void onExit(
      @Advice.Enter boolean skipped, @Advice.Return(readOnly = false) int result) {
    if (skipped) {
      result = 0; // HardwareRenderer.SYNC_OK
    }
  }
}
