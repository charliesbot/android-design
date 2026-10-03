package com.example.designsandbox.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.designsandbox.wear.navigation.WearNavigation
import com.example.designsandbox.wear.theme.WearAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { WearAppTheme { WearNavigation() } }
  }
}
