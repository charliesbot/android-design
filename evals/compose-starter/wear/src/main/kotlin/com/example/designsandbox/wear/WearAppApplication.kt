package com.example.designsandbox.wear

import android.app.Application
import com.example.designsandbox.data.di.coreDataModule
import com.example.designsandbox.wear.di.wearAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WearAppApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidContext(this@WearAppApplication)
      modules(coreDataModule, wearAppModule)
    }
  }
}
