package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.model.Views
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The test engine owns global state, so the renderer, not its callers, keeps renders apart. */
class ConcurrentRenderTest {
  private val proposal =
    proposalOf(
      "one" to
        screenJson(
          """{"id":"root","component":"Scaffold","content":"a"},{"id":"a","component":"Text","text":"One"}""",
        ),
      "two" to
        screenJson(
          """{"id":"root","component":"Scaffold","content":"b"},{"id":"b","component":"Text","text":"Two"}""",
        ),
    )
  private val source = RenderSource(proposal, designOf(proposal.theme), null)

  @Test
  fun `renders called at the same time run one after the other and each reads its own screen`() {
    val inside = AtomicInteger()
    val mostInside = AtomicInteger()
    val start = CountDownLatch(1)
    val failures = mutableListOf<Throwable>()
    val trees = mutableMapOf<String, Pair<Boolean, Boolean>>()

    val threads =
      listOf("one", "two").map { screen ->
        thread {
          try {
            start.await()
            val result =
              HeadlessRenderer.image(
                source,
                screen,
                Views.Light,
                afterLaunch = {
                  mostInside.accumulateAndGet(inside.incrementAndGet(), ::maxOf)
                  // Long enough that an unguarded second render would start inside this one.
                  Thread.sleep(OVERLAP_MS)
                  inside.decrementAndGet()
                },
              )
            synchronized(trees) {
              trees[screen] =
                (result.tree.screen == screen) to
                  (result.tree.nodes.any {
                    it.text?.value == screen.replaceFirstChar(Char::uppercase)
                  })
            }
          } catch (e: Throwable) {
            synchronized(failures) { failures += e }
          }
        }
      }
    start.countDown()
    threads.forEach { it.join() }

    assertNull(failures.firstOrNull(), "a render failed")
    assertEquals(1, mostInside.get(), "two renders were drawing at once")
    assertEquals(setOf("one", "two"), trees.keys)
    assertTrue(trees.values.all { (screenOk, textOk) -> screenOk && textOk }, "wrong tree: $trees")
  }

  private companion object {
    const val OVERLAP_MS = 300L
  }
}
