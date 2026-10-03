package com.example.designsandbox

import android.app.Application
import com.example.designsandbox.data.di.coreDataModule
import com.example.designsandbox.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidContext(this@AppApplication)
      modules(coreDataModule, appModule)
    }
  }
}
