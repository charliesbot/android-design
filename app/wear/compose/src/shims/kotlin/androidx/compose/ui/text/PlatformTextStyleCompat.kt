package androidx.compose.ui.text

/** Android-only parameter: desktop text has no font padding, so there is nothing to configure. */
@Suppress("UNUSED_PARAMETER")
fun PlatformTextStyle(includeFontPadding: Boolean): PlatformTextStyle? = null
