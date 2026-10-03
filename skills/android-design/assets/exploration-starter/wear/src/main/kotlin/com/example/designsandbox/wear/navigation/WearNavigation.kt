package com.example.designsandbox.wear.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import com.example.designsandbox.wear.ProposalScreen
import kotlinx.serialization.Serializable

@Serializable private data object Proposal : NavKey

@Composable
fun WearNavigation() {
  val backStack = rememberNavBackStack(Proposal)
  NavDisplay(
    backStack = backStack,
    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
    sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy<NavKey>()),
    entryProvider = entryProvider { entry<Proposal> { ProposalScreen() } },
  )
}
