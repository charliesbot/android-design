package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.ComposeUiTest
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessResult
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val strictJson = Json { ignoreUnknownKeys = false }

/** A screen as JSON: the component objects, comma separated, plus an optional data model. */
fun screenJson(components: String, dataModel: String? = null): String = buildString {
  append("""{"catalogId":"${M3ExpressiveCatalog.ID}",""")
  if (dataModel != null) append(""""dataModel":$dataModel,""")
  append(""""components":[$components]}""")
}

/** A proposal from `screen id to screen JSON` pairs; the first screen is the start. */
fun proposalOf(
  vararg screens: Pair<String, String>,
  panes: String = "{}",
  placeholders: String = "{}",
  theme: String? = null,
): Proposal {
  val themeJson = if (theme != null) """"theme":$theme,""" else ""
  val json =
    """{"format":1,"name":"Test",$themeJson"navigation":{"start":"${screens.first().first}","panes":$panes,"placeholders":$placeholders},""" +
      """"screens":{${screens.joinToString(",") { """"${it.first}":${it.second}""" }}}}"""
  return strictJson.decodeFromString(Proposal.serializer(), json)
}

/** One screen named `home` with [components]. */
fun singleScreen(components: String, dataModel: String? = null): Proposal =
  proposalOf("home" to screenJson(components, dataModel))

fun designOf(theme: ThemeOverride? = null): DesignSystem = DesignSystemResolver.resolve(null, theme)

fun render(
  proposal: Proposal,
  screen: String = proposal.navigation.start,
  view: ViewSpec = Views.Light,
  design: DesignSystem = designOf(proposal.theme),
  afterLaunch: (ComposeUiTest.(ProposalHostState) -> Unit)? = null,
): HeadlessResult =
  HeadlessRenderer.image(RenderSource(proposal, design, null), screen, view, afterLaunch)

fun LayoutTree.node(id: String): LayoutNode =
  nodes.firstOrNull { it.id == id } ?: error("no node '$id' in ${nodes.map { it.id }}")

fun LayoutTree.has(id: String) = nodes.any { it.id == id }

/** A valid value for a required prop; a child it needs is added to [extra]. */
private fun requiredValue(
  prop: PropDef,
  id: String,
  extra: MutableList<JsonObject>,
): JsonElement =
  when (val kind = prop.kind) {
    PropKind.Text -> JsonPrimitive("Sample")
    PropKind.Icon -> JsonPrimitive("home")
    PropKind.Image -> JsonPrimitive("missing.png")
    is PropKind.Enum -> JsonPrimitive(kind.values.first())
    PropKind.Child,
    PropKind.ChildList -> {
      val allowed = prop.allowedChildren?.minOrNull()
      val childId = if (allowed == null) "t1" else "${id}_${prop.name}"
      if (allowed != null) {
        extra += requiredNode(checkNotNull(M3ExpressiveCatalog.find(allowed)), childId, extra)
      }
      if (prop.kind == PropKind.Child) JsonPrimitive(childId)
      else JsonArray(listOf(JsonPrimitive(childId)))
    }
    else -> error("${prop.name}: no required value for $kind")
  }

/**
 * A component node of [def] with only its required props, valid values included. A child it needs
 * is added to [extra]; a child with no limit on its type points at a Text `t1`, which the caller
 * adds.
 */
internal fun requiredNode(
  def: ComponentDef,
  id: String,
  extra: MutableList<JsonObject>,
): JsonObject = buildJsonObject {
  put("id", id)
  put("component", def.name)
  def.props.filter { it.required }.forEach { put(it.name, requiredValue(it, id, extra)) }
}
