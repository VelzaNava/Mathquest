package com.mathquest.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathquest.app.R
import com.mathquest.app.ui.theme.Baloo2
import com.mathquest.app.ui.theme.Nunito
import kotlinx.coroutines.delay

private val STORY_IMAGES = listOf(
    R.drawable.story_1,
    R.drawable.story_2,
    R.drawable.story_3,
    R.drawable.story_4,
    R.drawable.story_5,
    R.drawable.story_6,
    R.drawable.story_7,
)

// Per-panel dialogue (mapped from FLOW.docx narrative beats)
private data class StoryLine(val speaker: String?, val text: String)

private val STORY_LINES = listOf(
    StoryLine(null,        "It was just an ordinary day…"),
    StoryLine("Player",    "*sighs* I really need to be studying."),
    StoryLine(null,        "But the numbers blurred together. Times tables, divisions, fractions… all a tangled mess."),
    StoryLine("???",       "Hero of Wumbo… we need you."),
    StoryLine("Guide",     "The Math Guardians have stolen the realm's wisdom. Only one who can solve their riddles may set Wumbo free."),
    StoryLine("Guide",     "Three guardians stand in your way: Multiplication, Division, and Fraction. Defeat them, and balance returns."),
    StoryLine("Guide",     "Will you accept the quest?"),
)

private const val CONTINUE_BTN_ASPECT = 539f / 123f
private const val BACK_BTN_ASPECT     = 539f / 123f

@Composable
fun IntroStoryScreen(
    playerName: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var currentPanel by remember { mutableStateOf(0) }

    // Entry white flash — screen starts fully white and fades to reveal story
    var entryWhite   by remember { mutableStateOf(true) }
    // Exit white flash — fades to white before navigating to fight
    var exitWhite    by remember { mutableStateOf(false) }

    val isLastPanel = currentPanel == STORY_IMAGES.lastIndex
    val line = STORY_LINES[currentPanel]

    // Entry: hold white briefly, then fade out
    LaunchedEffect(Unit) {
        delay(250)
        entryWhite = false
    }

    // Exit: white fades in, then navigate
    LaunchedEffect(exitWhite) {
        if (exitWhite) {
            delay(700)
            onComplete()
        }
    }

    val whiteAlpha by animateFloatAsState(
        targetValue   = if (entryWhite || exitWhite) 1f else 0f,
        animationSpec = tween(650, easing = LinearEasing),
        label         = "storyWhite"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val screenH = maxHeight

        // Story panel image
        Image(
            painter            = painterResource(id = STORY_IMAGES[currentPanel]),
            contentDescription = "Story panel ${currentPanel + 1}",
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Fit
        )

        // Dialogue overlay — uses fight_dialogue_box.png paper, flipped
        // vertically AND bottom-aligned so the box graphic lands at the
        // very bottom of the screen on any aspect ratio.
        AnimatedVisibility(
            visible  = !entryWhite && !exitWhite,
            enter    = fadeIn(tween(450)),
            exit     = fadeOut(tween(220)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter            = painterResource(id = R.drawable.fight_dialogue_box),
                    contentDescription = null,
                    modifier           = Modifier
                        .fillMaxSize()
                        .graphicsLayer { scaleY = -1f },
                    alignment          = Alignment.BottomCenter,
                    contentScale       = ContentScale.Fit
                )
                // Text overlay clamped tightly inside the visible paper —
                // narrow enough that long lines wrap (instead of running
                // past the paper edges), height-capped to never spill over
                // the top of the paper, and ellipsised if too long.
                DialogueContent(
                    speaker    = line.speaker,
                    text       = line.text,
                    playerName = playerName,
                    modifier   = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.40f)
                        .heightIn(max = screenH * 0.24f)
                        .padding(bottom = screenH * 0.05f, start = 8.dp, end = 8.dp)
                )
            }
        }

        // White flash overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(whiteAlpha)
                .background(Color.White)
        )

        // Navigation buttons — MASSIVE, flush to the absolute left and
        // right edges of the screen. The PNGs have a lot of transparent
        // padding around the actual button graphic, so we drop the
        // aspectRatio constraint entirely and give the modifier a big
        // square-ish bounding box (fillMaxWidth + fillMaxHeight). With
        // ContentScale.Fit, the artwork inside scales up to fill that
        // box — the visible button gets dramatically larger.
        if (!exitWhite) {
            // Back — most-LEFT
            Image(
                painter            = painterResource(id = R.drawable.btn_back_story),
                contentDescription = "Back",
                modifier           = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.30f)
                    .fillMaxHeight(0.55f)
                    .clickable(
                        enabled           = !entryWhite,
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) {
                        if (currentPanel == 0) onBack() else currentPanel--
                    },
                contentScale = ContentScale.Fit
            )

            // Continue — most-RIGHT
            Image(
                painter            = painterResource(id = R.drawable.btn_continue_story),
                contentDescription = "Continue Story",
                modifier           = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.30f)
                    .fillMaxHeight(0.55f)
                    .clickable(
                        enabled           = !entryWhite,
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) {
                        if (isLastPanel) exitWhite = true
                        else currentPanel++
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

// Text shown inside the paper dialogue box. Background is light cream from the
// PNG, so text uses dark indigo for legibility (the old dark pill used white).
@Composable
private fun DialogueContent(
    speaker: String?,
    text: String,
    playerName: String,
    modifier: Modifier = Modifier
) {
    val resolvedSpeaker = when (speaker) {
        "Player" -> playerName.ifBlank { "Player" }
        else     -> speaker
    }
    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        if (resolvedSpeaker != null) {
            Text(
                text       = resolvedSpeaker,
                fontFamily = Baloo2,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 11.sp,
                color      = Color(0xFF7C3AED),
                maxLines   = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
        }
        // No ellipsis: long lines wrap onto multiple stacked lines inside
        // the paper. Smaller font keeps everything inside the white box
        // even when the line is long (e.g. the "Math Guardians" speech).
        Text(
            text       = text,
            fontFamily = Nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 9.sp,
            color      = Color(0xFF1E1B4B),
            textAlign  = TextAlign.Center,
            maxLines   = 10,
            overflow   = TextOverflow.Visible
        )
    }
}

