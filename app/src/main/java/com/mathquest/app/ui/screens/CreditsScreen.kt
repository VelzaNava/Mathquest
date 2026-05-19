package com.mathquest.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathquest.app.R
import com.mathquest.app.ui.theme.Baloo2
import kotlinx.coroutines.delay

@Composable
fun CreditsScreen(
    playerName: String,         // kept for API compatibility — image is self-contained
    onMainMenu: () -> Unit
) {
    // Phase 1: image fades in from black  (starts after 200 ms)
    // Phase 2: "Return to Main Menu" button fades in (starts 1.5 s after image fully visible)
    var imageVisible  by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        imageVisible = true
        delay(1800)             // image fade duration + brief pause
        buttonVisible = true
    }

    val imageAlpha by animateFloatAsState(
        targetValue  = if (imageVisible) 1f else 0f,
        animationSpec = tween(1600, easing = LinearEasing),
        label = "creditsImageAlpha"
    )

    val buttonAlpha by animateFloatAsState(
        targetValue  = if (buttonVisible) 1f else 0f,
        animationSpec = tween(800, easing = LinearEasing),
        label = "creditsButtonAlpha"
    )

    // Black backing so the fade-in starts from black (seamless with LevelEarthScreen fade-out)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Full-screen ending image
        Image(
            painter      = painterResource(id = R.drawable.bg_credits),
            contentDescription = "Credits",
            modifier     = Modifier
                .fillMaxSize()
                .alpha(imageAlpha),
            contentScale = ContentScale.Fit
        )

        // Return button — fades in once the image has settled
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(buttonAlpha)
                .background(Color(0xCC7C3AED), RoundedCornerShape(999.dp))
                .clickable(
                    enabled = buttonVisible,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onMainMenu() }
                .padding(horizontal = 40.dp, vertical = 14.dp)
        ) {
            Text(
                text       = "Return to Main Menu",
                fontFamily = Baloo2,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 20.sp,
                color      = Color.White
            )
        }
    }
}
