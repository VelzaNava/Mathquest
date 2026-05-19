package com.mathquest.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathquest.app.R
import com.mathquest.app.ui.theme.Nunito
import com.mathquest.app.ui.theme.TextDark
import com.mathquest.app.ui.theme.TextMid

// bg_paused_v2.png is a 726×873 portrait crop of the paper notepad
// Icons baked into the paper at these fractions of the cropped image
// (verified by pixel scan — previous values were picking up the PAUSED title):
//   Speaker (SFX): center y ≈ 0.508 × H  (crop y ≈ 443),  icon right x ≈ 0.376 × W
//   Music Note:    center y ≈ 0.628 × H  (crop y ≈ 548),  icon right x ≈ 0.349 × W
private const val PAUSE_PAPER_ASPECT = 726f / 873f   // ≈ 0.832 portrait
private const val PAUSE_BTN_ASPECT   = 105f / 95f

@Composable
fun PauseOverlay(
    visible: Boolean,
    musicVolume: Float,
    sfxVolume: Float,
    onMusicVolume: (Float) -> Unit,
    onSfxVolume: (Float) -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    if (!visible) return

    // ──────────────────────────────────────────────────────────────────────
    // Rendered as a direct Box overlay — NO Dialog.
    // Dialog injects minWidth = screenWidth as a tight constraint, which
    // prevents width(panelW) from working and makes sliders span the full
    // screen width.  As a plain overlay inside LevelEarthScreen's outer
    // Box(fillMaxSize), constraints are (0..screenW, 0..screenH), so
    // width(panelW) + height(panelH) constrain children correctly.
    // ──────────────────────────────────────────────────────────────────────
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* intercept all touches */ },
        contentAlignment = Alignment.Center
    ) {
        // Panel sized to paper aspect; coerce so it fits any screen
        val panelH = (maxHeight * 0.92f).coerceAtMost(700.dp)
        val panelW = (panelH * PAUSE_PAPER_ASPECT).coerceAtMost(maxWidth * 0.92f)

        // Slider geometry derived from baked icon positions in the paper crop.
        // Icon right edges: speaker ≈ 0.376 W, music ≈ 0.349 W → start after 0.40 W.
        val sliderStartX = panelW * 0.40f
        val sliderW      = panelW * 0.54f          // ends at ≈ 0.94 × W
        val sliderH      = 20.dp

        // Row offsets: label (≈ 14 dp) sits ABOVE the slider.
        // Whole row height ≈ 14 + 2 + 20 = 36 dp  → top = iconCentreY − 36/2 = iconCentreY − 18 dp
        val sfxRowY   = panelH * 0.508f - 18.dp
        val musicRowY = panelH * 0.628f - 18.dp

        // Button geometry — centred at 86 % to leave breathing room below music row
        val btnCentreY = panelH * 0.860f
        val btnH       = (panelH * 0.095f).coerceIn(42.dp, 66.dp)
        val btnW       = btnH * PAUSE_BTN_ASPECT
        val btnGap     = (panelW * 0.06f).coerceAtLeast(8.dp)
        val panelCX    = panelW / 2f

        Box(
            modifier = Modifier
                .width(panelW)
                .height(panelH)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* swallow */ }
        ) {
            // ── Paper background ──────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.bg_paused_v2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit   // exact fit; aspect matches
            )

            // ── SFX slider row ────────────────────────────────────────────
            PauseSliderRow(
                label    = "SFX",
                value    = sfxVolume,
                onChange = onSfxVolume,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = sliderStartX, y = sfxRowY)
                    .width(sliderW),
                sliderH  = sliderH
            )

            // ── Music slider row ──────────────────────────────────────────
            PauseSliderRow(
                label    = "Music",
                value    = musicVolume,
                onChange = onMusicVolume,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = sliderStartX, y = musicRowY)
                    .width(sliderW),
                sliderH  = sliderH
            )

            // ── Buttons (Home · Resume · Restart) ─────────────────────────
            // Three equally-spaced buttons centred in the paper
            val halfBtn  = btnW / 2f
            val step     = btnW + btnGap
            val btnTopY  = btnCentreY - btnH / 2f

            // Home
            Image(
                painter = painterResource(id = R.drawable.btn_paused_home),
                contentDescription = "Home",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = panelCX - step - halfBtn, y = btnTopY)
                    .height(btnH).aspectRatio(PAUSE_BTN_ASPECT)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onHome() },
                contentScale = ContentScale.Fit
            )

            // Resume
            Image(
                painter = painterResource(id = R.drawable.btn_resume),
                contentDescription = "Resume",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = panelCX - halfBtn, y = btnTopY)
                    .height(btnH).aspectRatio(PAUSE_BTN_ASPECT)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onResume() },
                contentScale = ContentScale.Fit
            )

            // Restart
            Image(
                painter = painterResource(id = R.drawable.btn_restart),
                contentDescription = "Restart",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = panelCX + step - halfBtn, y = btnTopY)
                    .height(btnH).aspectRatio(PAUSE_BTN_ASPECT)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRestart() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

/** Label + percentage header above a Slider, positioned via the external [modifier]. */
@Composable
private fun PauseSliderRow(
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sliderH: Dp = 20.dp
) {
    Column(modifier = modifier) {
        // Top row: label left, percentage right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = TextDark
            )
            Text(
                text = "${(value * 100).toInt()}%",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                color = TextMid
            )
        }
        Slider(
            value         = value,
            onValueChange = onChange,
            valueRange    = 0f..1f,
            modifier      = Modifier
                .fillMaxWidth()
                .height(sliderH),
            colors = SliderDefaults.colors(
                thumbColor         = Color(0xFF7C3AED),
                activeTrackColor   = Color(0xFFA78BFA),
                inactiveTrackColor = Color(0xFFE9D5FF)
            )
        )
    }
}
