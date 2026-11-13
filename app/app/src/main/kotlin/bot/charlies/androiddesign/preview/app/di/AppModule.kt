package bot.charlies.androiddesign.preview.app.di

import bot.charlies.androiddesign.preview.app.preview.HeadlessProposalRenderer
import bot.charlies.androiddesign.preview.app.preview.PreviewWindow
import bot.charlies.androiddesign.preview.app.preview.ProposalRenderer
import bot.charlies.androiddesign.preview.data.FileProposalRepository
import bot.charlies.androiddesign.preview.domain.ProposalRepository
import org.koin.dsl.module

/**
 * Wires what every session shares: the proposal repository, the renderer and the window. Each
 * session builds its own viewer and controller (see `Sessions`).
 */
val appModule = module {
  single<ProposalRepository> { FileProposalRepository() }
  single { PreviewWindow() }
  single<ProposalRenderer> { HeadlessProposalRenderer() }
}
