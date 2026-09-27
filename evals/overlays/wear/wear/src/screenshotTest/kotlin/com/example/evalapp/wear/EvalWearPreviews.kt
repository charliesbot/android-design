package com.example.evalapp.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import com.android.tools.screenshot.PreviewTest

// Fixed eval renders of EvalWearScreen(). Do not edit: the eval runner owns this file.

@PreviewTest
@Preview(name = "large", device = WearDevices.LARGE_ROUND, showSystemUi = true, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun EvalWearLarge() = EvalWearScreen()

@PreviewTest
@Preview(name = "small", device = WearDevices.SMALL_ROUND, showSystemUi = true, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun EvalWearSmall() = EvalWearScreen()

@PreviewTest
@Preview(name = "font", device = WearDevices.SMALL_ROUND, showSystemUi = true, showBackground = true, backgroundColor = 0xFF000000, fontScale = 1.24f)
@Composable
fun EvalWearFont() = EvalWearScreen()
