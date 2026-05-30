package com.mathquest.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Returns a multiplier based on the screen's shortest side:
 *   phone  (<480 dp tall)  → 1.0×
 *   small tablet (480–600) → 1.20×
 *   large tablet (600 +)   → 1.40×
 *
 * We key off the SHORT side so landscape tablets (tall = short side)
 * are treated the same as portrait tablets.
 */
@Composable
fun adaptiveScale(): Float {
    val cfg   = LocalConfiguration.current
    val short = minOf(cfg.screenWidthDp, cfg.screenHeightDp)
    return when {
        short >= 600 -> 1.40f
        short >= 480 -> 1.20f
        else         -> 1.00f
    }
}

/** Scales an sp font size for the current screen. */
@Composable
fun Int.asp(): TextUnit = (this * adaptiveScale()).sp

/** Scales a dp spacing/size value for the current screen. */
@Composable
fun Int.adp(): Dp = (this * adaptiveScale()).dp
