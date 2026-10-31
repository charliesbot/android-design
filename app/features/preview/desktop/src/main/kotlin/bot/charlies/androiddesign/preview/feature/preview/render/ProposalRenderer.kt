package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.record.TreeRecorder
import bot.charlies.androiddesign.preview.feature.preview.theme.LocalResolvedTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalTheme
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlinx.serialization.json.Json

/**
 * Draws one screen of a proposal with real Material 3 components: its `root` component, then the
 * overlay that is open on it (overlays stay closed until an `open` action).
 *
 * @param sizeClass The width class of the preview frame, not of the app window; it picks `showIn`
 *   and `sizeClass` overrides.
 * @param recorder Collects the layout tree while drawing.
 * @param idPrefix Prepended to the tree ids of this screen; empty for the screen being asked about.
 */
@Composable
fun ProposalRenderer(
  host: ProposalHostState,
  screenId: String,
  sizeClass: SizeClass,
  modifier: Modifier = Modifier,
  recorder: TreeRecorder? = null,
  idPrefix: String = "",
) {
  val screen = host.proposal.screens[screenId] ?: return
  val theme = LocalResolvedTheme.current
  val assets = remember(host.proposalDir) { AssetLoader(host.proposalDir) }
  val env =
    remember(screen, screenId, host, theme, sizeClass, recorder, idPrefix, assets) {
      RenderEnv(screenId, screen, host, theme, sizeClass, recorder, idPrefix, assets)
    }
  CompositionLocalProvider(LocalRenderEnv provides env) {
    Box(modifier.fillMaxSize()) {
      RenderNode(
        env = env,
        ref = ChildRef(ComponentNode.ROOT_ID, DataScope.root(screen.dataModel)),
        parentId = null,
        order = listOf(0, 0),
        host = ChildHost.Fill,
      )
      RenderOpenOverlay(env)
    }
  }
}

private val PreviewProposal: Proposal by lazy {
  Json.decodeFromString(
    Proposal.serializer(),
    """
    {"format":1,"navigation":{"start":"home"},"screens":{"home":{
      "catalogId":"https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1",
      "components":[
        {"id":"root","component":"Scaffold","topBar":"bar","content":"body"},
        {"id":"bar","component":"TopAppBar","title":"Preview"},
        {"id":"body","component":"Column","spacing":"lg","padding":"lg","children":["hello","go"]},
        {"id":"hello","component":"Text","text":"Hello, Material 3 Expressive","type":"headlineSmall"},
        {"id":"go","component":"Button","label":"Go"}
      ]}}}
    """
      .trimIndent(),
  )
}

@Preview
@Composable
private fun ProposalRendererPreview() {
  val host = remember { ProposalHostState(PreviewProposal) }
  ProposalTheme(DesignSystemResolver.resolve(null, null), dark = false) {
    ProposalRenderer(host, "home", SizeClass.Compact)
  }
}
