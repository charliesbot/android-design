package com.example.evalapp

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import com.android.tools.screenshot.PreviewTest

// Fixed eval renders of EvalScreen(). Do not edit: the eval runner owns this file.
private const val PHONE = "spec:width=412dp,height=915dp,dpi=420"

@PreviewTest
@Preview(name = "light", device = PHONE, showSystemUi = true)
@Composable
fun EvalLight() = EvalScreen()

@PreviewTest
@Preview(name = "dark", device = PHONE, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EvalDark() = EvalScreen()

@PreviewTest
@Preview(name = "font200", device = PHONE, showSystemUi = true, fontScale = 2f)
@Composable
fun EvalFont200() = EvalScreen()

@PreviewTest
@Preview(name = "wallpaper", device = PHONE, showSystemUi = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun EvalWallpaper() = EvalScreen()
