package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.Serializable

/** The last node clicked on the browser board for one exploration file. */
@Serializable
data class NodeSelection(
  val file: String,
  val line: Int,
  val callName: String,
  val callChain: List<String> = emptyList(),
  val direction: String,
  val screen: String,
  val overlay: String? = null,
  val view: String,
  val bounds: Bounds,
  val contentHash: String,
  val sourceLineText: String? = null,
  val stale: Boolean = false,
  val selectedAt: Long,
)

/**
 * Tracks the person's last clicked node per canonical exploration file, reports unread selections
 * once on `check`, and re-resolves selections across edits by call chain and source line text.
 */
class SelectionStore {
  private data class Entry(
    val selection: NodeSelection,
    val seq: Long,
    val deliveredSeq: Long,
  )

  private val byFile = ConcurrentHashMap<String, Entry>()
  private val clockSeq = AtomicLong(0L)

  fun record(sourcePath: Path, incoming: NodeSelection): NodeSelection {
    val key = canonicalKey(sourcePath)
    val lineText = incoming.sourceLineText ?: readLineText(sourcePath, incoming.line)
    val normalized =
      incoming.copy(
        file = incoming.file.ifBlank { sourcePath.fileName.toString() },
        sourceLineText = lineText?.trim(),
        stale = false,
      )
    val nextSeq = clockSeq.incrementAndGet()
    byFile[key] = Entry(selection = normalized, seq = nextSeq, deliveredSeq = 0L)
    return normalized
  }

  /** Returns the current selection and marks it delivered, or null if none exists. */
  fun current(sourcePath: Path): NodeSelection? {
    val key = canonicalKey(sourcePath)
    var found: NodeSelection? = null
    byFile.computeIfPresent(key) { _, entry ->
      found = entry.selection
      entry.copy(deliveredSeq = entry.seq)
    }
    return found
  }

  /** Returns the selection only if the person clicked since the agent's previous command. */
  fun consumePending(sourcePath: Path): NodeSelection? {
    val key = canonicalKey(sourcePath)
    var found: NodeSelection? = null
    byFile.computeIfPresent(key) { _, entry ->
      if (entry.seq > entry.deliveredSeq) {
        found = entry.selection
        entry.copy(deliveredSeq = entry.seq)
      } else {
        entry
      }
    }
    return found
  }

  /**
   * Re-resolves a stored selection after the file changed and the matching view re-rendered. A
   * unique match by `(callName, callChain, sourceLineText)` updates `line` and `bounds`; zero or
   * multiple matches mark `stale = true` while keeping `callName` and `callChain`.
   */
  fun reResolve(
    sourcePath: Path,
    newContentHash: String,
    direction: String,
    screen: String,
    overlay: String?,
    view: String,
    tree: LayoutTree,
    sourceLines: List<String>,
  ) {
    val key = canonicalKey(sourcePath)
    byFile.computeIfPresent(key) { _, entry ->
      val current = entry.selection
      if (
        current.contentHash == newContentHash ||
          current.direction != direction ||
          current.screen != screen ||
          current.overlay != overlay ||
          current.view != view
      ) {
        return@computeIfPresent entry
      }

      val byId = tree.nodes.associateBy { it.id }
      val candidates =
        tree.nodes.filter { node ->
          node.type == current.callName &&
            node.sourceLine != null &&
            ancestorChain(node, byId) == current.callChain
        }
      val refined =
        if (!current.sourceLineText.isNullOrBlank()) {
          val textMatches = candidates.filter { node ->
            val idx = (node.sourceLine ?: 0) - 1
            idx in sourceLines.indices && sourceLines[idx].trim() == current.sourceLineText
          }
          textMatches
        } else {
          candidates
        }

      val updated =
        if (refined.size == 1) {
          val match = refined.single()
          val line = match.sourceLine ?: current.line
          val lineText = sourceLines.getOrNull(line - 1)?.trim() ?: current.sourceLineText
          current.copy(
            line = line,
            bounds = match.bounds,
            contentHash = newContentHash,
            sourceLineText = lineText,
            stale = false,
          )
        } else {
          current.copy(contentHash = newContentHash, stale = true)
        }
      entry.copy(selection = updated)
    }
  }

  private fun canonicalKey(path: Path): String = path.toAbsolutePath().normalize().toString()

  private fun readLineText(sourcePath: Path, line: Int): String? {
    if (line <= 0 || !Files.isRegularFile(sourcePath)) return null
    return try {
      Files.readAllLines(sourcePath).getOrNull(line - 1)
    } catch (_: Exception) {
      null
    }
  }

  companion object {
    fun ancestorChain(node: LayoutNode, byId: Map<String, LayoutNode>): List<String> {
      val chain = mutableListOf<String>()
      val seen = mutableSetOf(node.id)
      var parent = node.parent?.let(byId::get)
      while (parent != null && seen.add(parent.id)) {
        chain += parent.type
        parent = parent.parent?.let(byId::get)
      }
      return chain.asReversed()
    }
  }
}
