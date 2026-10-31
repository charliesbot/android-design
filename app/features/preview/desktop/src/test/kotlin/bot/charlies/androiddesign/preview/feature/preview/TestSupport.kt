package bot.charlies.androiddesign.preview.feature.preview

import androidx.compose.ui.test.ComposeUiTest
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessResult
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import kotlinx.serialization.json.Json

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
  theme: String? = null,
): Proposal {
  val themeJson = if (theme != null) """"theme":$theme,""" else ""
  val json =
    """{"format":1,"name":"Test",$themeJson"navigation":{"start":"${screens.first().first}","panes":$panes},""" +
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
): HeadlessResult = HeadlessRenderer.render(proposal, design, null, screen, view, afterLaunch)

fun LayoutTree.node(id: String): LayoutNode =
  nodes.firstOrNull { it.id == id } ?: error("no node '$id' in ${nodes.map { it.id }}")

fun LayoutTree.has(id: String) = nodes.any { it.id == id }
