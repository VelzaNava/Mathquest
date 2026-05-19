package com.mathquest.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mathquest.app.R
import kotlinx.coroutines.delay

private val SLIDES = listOf(
    R.drawable.story_1,
    R.drawable.story_2,
    R.drawable.story_3,
    R.drawable.story_4,
    R.drawable.story_5,
    R.drawable.story_6,
    R.drawable.story_7,
)

private const val FADE_MS = 500
private const val HOLD_MS = 3000L

@Composable
fun CinematicIntroScreen(onComplete: () -> Unit) {
    var currentSlide by remember { mutableStateOf(0) }
    var imageVisible by remember { mutableStateOf(false) }
    var showWhite by remember { mutableStateOf(false) }

    val imageAlpha by animateFloatAsState(
        targetValue = if (imageVisible) 1f else 0f,
        animationSpec = tween(FADE_MS, easing = LinearEasing),
        label = "imageAlpha"
    )
    val whiteAlpha by animateFloatAsState(
        targetValue = if (showWhite) 1f else 0f,
        animationSpec = tween(700, easing = LinearEasing),
        label = "whiteAlpha"
    )

    LaunchedEffect(Unit) {
        for (i in SLIDES.indices) {
            currentSlide = i
            imageVisible = true
            delay(FADE_MS.toLong())   // fade-in completes
            delay(HOLD_MS)            // hold fully visible for 3 s
            imageVisible = false
            delay(FADE_MS.toLong())   // fade-out completes
        }
        showWhite = true
        delay(800L)                   // white settles before navigating
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = SLIDES[currentSlide]),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(imageAlpha),
            contentScale = ContentScale.Crop
        )

        // Final white-fade overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(whiteAlpha)
                .background(Color.White)
        )
    }
}
