package com.example.designsandbox.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.designsandbox.ProposalScreen
import kotlinx.serialization.Serializable

@Serializable private data object Proposal : NavKey

@Composable
fun AppNavigation() {
  val backStack = rememberNavBackStack(Proposal)
  NavDisplay(
    backStack = backStack,
    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
    entryProvider = entryProvider { entry<Proposal> { ProposalScreen() } },
  )
}
