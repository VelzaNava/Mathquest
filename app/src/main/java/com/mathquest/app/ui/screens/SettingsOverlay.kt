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
import androidx.compose.foundation.layout.Spacer
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

// bg_settings.png is 829×951 (aspect ≈ 0.872, already portrait).
// "SETTINGS" decorative title ends at ≈ 36.4 % of image height (y ≈ 346 px).
// Slider rows and button are placed below that.
private const val SETTINGS_PAPER_ASPECT = 829f / 951f   // ≈ 0.872
private const val SETTINGS_HOME_ASPECT  = 105f / 95f

@Composable
fun SettingsOverlay(
    musicVolume: Float,
    sfxVolume: Float,
    onMusicVolume: (Float) -> Unit,
    onSfxVolume: (Float) -> Unit,
    onClose: () -> Unit
) {
    // ──────────────────────────────────────────────────────────────────────
    // Direct overlay — NO Dialog (same reason as PauseOverlay: Dialog injects
    // a tight minWidth = screenWidth constraint that breaks child widths).
    // Tapping the dark background closes the settings.
    // ──────────────────────────────────────────────────────────────────────
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        // Smart panel sizing: cap panelH so panelW never exceeds 92 % of screen.
        // This guarantees panel aspect == image aspect → ContentScale.Fit fills
        // the panel exactly with no vertical letterboxing.
        val maxPanelHFromWidth = maxWidth * 0.92f / SETTINGS_PAPER_ASPECT
        val panelH = (maxHeight * 0.90f)
            .coerceAtMost(maxPanelHFromWidth)
            .coerceAtMost(620.dp)
        val panelW = panelH * SETTINGS_PAPER_ASPECT

        // Slider geometry — pixel-verified boundaries:
        //   Red margin line right edge: canvas x ≈ 217  → fraction 0.262 of image width
        //   Paper right edge (safe):    canvas x ≈ 733  → fraction 0.884 of image width
        //   Slider starts just AFTER the red line, ends just INSIDE the paper right edge.
        val sliderStartX = panelW * 0.272f   // ≈ 8 dp past the red line
        val sliderW      = panelW * 0.612f   // ends at ≈ 0.884 × W
        val sliderH      = 16.dp             // compact — "make it smaller"

        // Row top offsets:
        //   row height ≈ 13 dp (label) + 16 dp (slider) = 29 dp → top = centreY − 14.5 dp
        //   "SETTINGS" title ends at ≈ 36.4 % → Music row centre at 42 % gives ~5 % margin
        val musicRowY = panelH * 0.42f - 15.dp
        val sfxRowY   = panelH * 0.62f - 15.dp

        // Home button
        val btnH   = (panelH * 0.095f).coerceIn(46.dp, 72.dp)
        val btnW   = btnH * SETTINGS_HOME_ASPECT
        val btnTopY = panelH * 0.84f - btnH / 2f

        Box(
            modifier = Modifier
                .width(panelW)
                .height(panelH)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* swallow touches inside panel */ }
        ) {
            // ── Paper background ──────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.bg_settings),
                contentDescription = "Settings Panel",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit   // exact fill — aspect matches
            )

            // ── Music slider row ──────────────────────────────────────────
            SettingsSliderRow(
                icon     = "🎵",
                label    = "Music",
                value    = musicVolume,
                onChange = onMusicVolume,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = sliderStartX, y = musicRowY)
                    .width(sliderW),
                sliderH  = sliderH
            )

            // ── SFX slider row ────────────────────────────────────────────
            SettingsSliderRow(
                icon     = "🔊",
                label    = "SFX",
                value    = sfxVolume,
                onChange = onSfxVolume,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = sliderStartX, y = sfxRowY)
                    .width(sliderW),
                sliderH  = sliderH
            )

            // ── Home / Close button ───────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.btn_settings_home),
                contentDescription = "Close Settings",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = panelW / 2f - btnW / 2f, y = btnTopY)
                    .height(btnH)
                    .aspectRatio(SETTINGS_HOME_ASPECT)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

/** Emoji icon + label left, percentage right, slider below. */
@Composable
private fun SettingsSliderRow(
    icon: String,
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sliderH: Dp = 16.dp
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 13.asp())
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.asp(),
                    color = TextDark
                )
            }
            Text(
                text = "${(value * 100).toInt()}%",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.asp(),
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

/** Taller volume row — kept for any non-paper overlay contexts. */
@Composable
fun VolumeRow(
    iconEmoji: String,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = iconEmoji, fontSize = 20.asp())
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.asp(),
                    color = TextDark
                )
            }
            Text(
                text = "${(value * 100).toInt()}%",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.asp(),
                color = TextMid
            )
        }
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = 0f..1f,
            modifier      = Modifier.fillMaxWidth(),
            colors        = SliderDefaults.colors(
                thumbColor         = Color(0xFF7C3AED),
                activeTrackColor   = Color(0xFFA78BFA),
                inactiveTrackColor = Color(0xFFE9D5FF)
            )
        )
    }
}
