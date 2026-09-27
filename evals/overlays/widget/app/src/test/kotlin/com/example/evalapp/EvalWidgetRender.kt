package com.example.evalapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.compose
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Fixed eval renders of EvalWidget. Do not edit: the eval runner owns this file.
// Renders the widget's RemoteViews offscreen; the launcher's rounded clipping is not drawn.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = android.app.Application::class, sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class EvalWidgetRender {
    private val sizes = mapOf("2x2" to DpSize(180.dp, 180.dp), "4x2" to DpSize(330.dp, 180.dp))

    @Test fun light() = renderAll("light")

    @Test fun dark() {
        RuntimeEnvironment.setQualifiers("+night")
        renderAll("dark")
    }

    private fun renderAll(mode: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val out = File("../renders").apply { mkdirs() }
        sizes.forEach { (name, size) ->
            val views = runBlocking { EvalWidget().compose(context, size = size) }
            val density = context.resources.displayMetrics.density
            val w = (size.width.value * density).toInt()
            val h = (size.height.value * density).toInt()
            val parent = FrameLayout(context)
            parent.addView(views.apply(context, parent), FrameLayout.LayoutParams(w, h))
            parent.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
            parent.layout(0, 0, w, h)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).apply { drawColor(if (mode == "dark") 0xFF1B1B1F.toInt() else 0xFFEFEDF1.toInt()) }.also { parent.draw(it) }
            File(out, "$mode-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
