package com.mathquest.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathquest.app.R
import com.mathquest.app.ui.theme.Nunito

// Natural aspect ratios from the actual cropped PNG assets
private const val BUTTON_ASPECT = 538f / 121f      // ≈ 4.45 — main wide buttons
private const val ICON_ASPECT = 154f / 172f        // ≈ 0.90 — settings/profile

@Composable
fun MainMenuScreen(
    hasSavedProgress: Boolean,
    playerName: String,
    musicVolume: Float,
    sfxVolume: Float,
    onMusicVolume: (Float) -> Unit,
    onSfxVolume: (Float) -> Unit,
    onStartAdventure: () -> Unit,
    onContinue: () -> Unit,
    onViewProgress: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Background — fills the entire landscape screen
        Image(
            painter = painterResource(id = R.drawable.bg_main_menu),
            contentDescription = "Main Menu Background",
            modifier = Modifier
                .fillMaxSize()
                // PERF: stable modifier chain; 0.dp is a no-op
                .blur(if (showSettings) 14.dp else 0.dp),
            contentScale = ContentScale.Crop
        )

        AnimatedVisibility(
            visible = !showSettings,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            MainMenuControls(
                hasSavedProgress = hasSavedProgress,
                onStartAdventure = onStartAdventure,
                onContinue = {
                    if (hasSavedProgress) onContinue()
                    else Toast.makeText(context, "No saved progress yet", Toast.LENGTH_SHORT).show()
                },
                onViewProgress = onViewProgress,
                onSettings = { showSettings = true },
                onProfile = { showProfile = true }
            )
        }

        if (showSettings) {
            SettingsOverlay(
                musicVolume = musicVolume,
                sfxVolume = sfxVolume,
                onMusicVolume = onMusicVolume,
                onSfxVolume = onSfxVolume,
                onClose = { showSettings = false }
            )
        }

        if (showProfile) {
            ProfileOverlay(
                playerName = playerName,
                onClose = { showProfile = false }
            )
        }
    }
}

@Composable
private fun MainMenuControls(
    hasSavedProgress: Boolean,
    onStartAdventure: () -> Unit,
    onContinue: () -> Unit,
    onViewProgress: () -> Unit,
    onSettings: () -> Unit,
    onProfile: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Icon size based on screen height — roughly 12% of available height
        val iconSize = (maxHeight * 0.13f).coerceIn(54.dp, 86.dp)
        // Main button width — narrower so the "Quest" title stays visible behind
        val buttonWidth = (maxWidth * 0.20f).coerceIn(150.dp, 240.dp)

        // TOP-LEFT: Settings + Profile stacked vertically
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp, start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ImageIconButton(
                resId = R.drawable.btn_settings,
                description = "Settings",
                onClick = onSettings,
                size = iconSize
            )
            ImageIconButton(
                resId = R.drawable.btn_profile,
                description = "Profile",
                onClick = onProfile,
                size = iconSize
            )
        }

        // CENTER-BOTTOM: 3 wide buttons stacked vertically
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Start Adventure
            Image(
                painter = painterResource(id = R.drawable.btn_start),
                contentDescription = "Start Adventure",
                modifier = Modifier
                    .width(buttonWidth)
                    .aspectRatio(BUTTON_ASPECT)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onStartAdventure() },
                contentScale = ContentScale.Fit
            )

            // Continue (blurred when no saved progress)
            Image(
                painter = painterResource(id = R.drawable.btn_continue_game),
                contentDescription = "Continue",
                modifier = Modifier
                    .width(buttonWidth)
                    .aspectRatio(BUTTON_ASPECT)
                    .alpha(if (hasSavedProgress) 1f else 0.45f)
                    // PERF: stable modifier chain; 0.dp blur is a no-op
                    .blur(if (hasSavedProgress) 0.dp else 3.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onContinue() },
                contentScale = ContentScale.Fit
            )

            // View Progress
            Image(
                painter = painterResource(id = R.drawable.btn_view_progress),
                contentDescription = "View Progress",
                modifier = Modifier
                    .width(buttonWidth)
                    .aspectRatio(BUTTON_ASPECT)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onViewProgress() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ImageButton(
    resId: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alpha: Float = 1f
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = description,
        modifier = modifier
            .alpha(alpha)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentScale = ContentScale.Fit
    )
}

@Composable
fun ImageIconButton(
    resId: Int,
    description: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = description,
        modifier = Modifier
            .size(size)
            .aspectRatio(ICON_ASPECT)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentScale = ContentScale.Fit
    )
}

// profile_card.png is 366×440 (aspect ≈ 0.832).
// White strip starts at card y ≈ 68 % (separator line at row 295/440).
private const val PROFILE_CARD_ASPECT = 366f / 440f
private const val PROFILE_NAME_FRAC = 0.03f   // above the character's head

@Composable
fun ProfileOverlay(playerName: String, onClose: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.60f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        // Size the card so it fits comfortably — portrait card, ~55 % of screen height
        val cardH = (maxHeight * 0.58f).coerceAtMost(380.dp)
        val cardW = cardH * PROFILE_CARD_ASPECT

        Box(
            modifier = Modifier
                .width(cardW)
                .height(cardH)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* swallow touches so card tap doesn't close */ }
        ) {
            // Card image (player already composited on it)
            Image(
                painter = painterResource(id = R.drawable.profile_card),
                contentDescription = "Player Card",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Player name floating on top of the character's head
            val nameY = cardH * PROFILE_NAME_FRAC
            Text(
                text = if (playerName.isBlank()) "Hero" else playerName,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = nameY)
            )
        }
    }
}
