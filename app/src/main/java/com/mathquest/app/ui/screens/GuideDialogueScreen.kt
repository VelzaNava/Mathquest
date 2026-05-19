package com.mathquest.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathquest.app.R
import com.mathquest.app.ui.theme.Baloo2
import com.mathquest.app.ui.theme.Nunito

// ── Palette ────────────────────────────────────────────────────────────────
private val GuidePurple  = Color(0xFF7C3AED)
private val PlayerAmber  = Color(0xFFB45309)
private val BoxCream     = Color(0xFFFFF8EE)
private val BoxBorder    = Color(0xFFD4A76A)
private val TextInk      = Color(0xFF3B1F07)
private val ChoiceGreen  = Color(0xFF16A34A)
private val ChoiceRed    = Color(0xFFDC2626)

// ── Data ───────────────────────────────────────────────────────────────────
private data class DLine(val speaker: String, val text: String, val isGuide: Boolean)

// Guide expression cycle: neutral → happy → teach → serious (repeating)
private val GUIDE_SPRITES = listOf(
    R.drawable.guide_neutral,
    R.drawable.guide_happy,
    R.drawable.guide_teach,
    R.drawable.guide_serious,
)

private val LINES = listOf(
    DLine("You",   "...Where am I?",                                                                         false),
    DLine("Guide", "Welcome, Explorer. You are now inside Wumbo, a world where numbers live and breathe.",   true),
    DLine("Guide", "But something has gone terribly wrong.",                                                  true),
    DLine("Guide", "The balance of Wumbo has been broken.",                                                   true),
    DLine("Guide", "Our guardians… have been corrupted,",                                                     true),
    DLine("Guide", "They now spread chaos instead of balance.",                                               true),
    DLine("You",   "Why am I here?",                                                                         false),
    DLine("Guide", "Because you were chosen.",                                                                true),
    DLine("You",   "Chosen??",                                                                               false),
    DLine("Guide", "I sensed your potential… your ability to understand and restore balance through math.",   true),
    DLine("Guide", "You can fix what's been broken.",                                                         true),
    DLine("Guide", "To save Wumbo…",                                                                         true),
    DLine("Guide", "You must defeat the three corrupted guardians.",                                          true),
    DLine("Guide", "And the only way to defeat them…",                                                        true),
    DLine("Guide", "...is by solving their challenges.",                                                      true),
    DLine("Guide", "Are you ready, Explorer?",                                                                true),
)

private val NO_RESPONSE = DLine("Guide", "You must be ready… Wumbo depends on you.", true)

// ── Screen ─────────────────────────────────────────────────────────────────
@Composable
fun GuideDialogueScreen(onComplete: () -> Unit) {
    var lineIdx        by remember { mutableStateOf(0) }
    var guideSeen      by remember { mutableStateOf(0) }   // counts guide lines shown so far
    var showChoice     by remember { mutableStateOf(false) }
    var showNoLine     by remember { mutableStateOf(false) }

    // Fade in from white (Phase 1 left the screen white)
    var visible by remember { mutableStateOf(false) }
    val screenAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900, easing = LinearEasing),
        label = "screenAlpha"
    )
    LaunchedEffect(Unit) { visible = true }

    val line         = if (showNoLine) NO_RESPONSE else LINES[lineIdx]
    val guideSprite  = GUIDE_SPRITES[guideSeen % 4]
    val showSprite   = line.isGuide || showChoice

    fun advance() {
        when {
            showChoice  -> return
            showNoLine  -> { showNoLine = false; showChoice = true }
            else -> {
                if (LINES[lineIdx].isGuide) guideSeen++          // leaving a guide line → next expression
                val next = lineIdx + 1
                if (next >= LINES.size) showChoice = true
                else lineIdx = next
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !showChoice
            ) { advance() }
    ) {
        val isLandscape = maxWidth > maxHeight

        // ── Background ───────────────────────────────────────────────────
        Image(
            painter = painterResource(R.drawable.bg_guide_intro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ── Guide sprite ─────────────────────────────────────────────────
        if (showSprite) {
            val spriteH = if (isLandscape) maxHeight * 0.72f else maxHeight * 0.40f
            Image(
                painter = painterResource(guideSprite),
                contentDescription = "Guide",
                modifier = Modifier
                    .height(spriteH.coerceAtMost(420.dp))
                    .align(if (isLandscape) Alignment.BottomStart else Alignment.TopCenter)
                    .then(
                        if (isLandscape) Modifier.padding(start = 12.dp)
                        else Modifier.padding(top = 8.dp)
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // ── Dialogue box area ─────────────────────────────────────────────
        val boxStartPad = if (isLandscape && showSprite) (maxWidth * 0.30f).coerceAtLeast(180.dp) else 16.dp

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = boxStartPad, end = 16.dp, bottom = 14.dp)
        ) {
            Column {
                // Speaker name tag
                val nameColor = if (line.isGuide) GuidePurple else PlayerAmber
                Box(
                    modifier = Modifier
                        .background(nameColor, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                        .padding(horizontal = 18.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = line.speaker,
                        fontFamily = Baloo2,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(3.dp))

                // Dialogue card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BoxCream, RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .border(3.dp, BoxBorder, RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    if (!showChoice) {
                        // Normal dialogue line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = line.text,
                                fontFamily = Nunito,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
                                color = TextInk,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "▶",
                                color = BoxBorder,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    } else {
                        // Yes / No choice
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Are you ready, Explorer?",
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextInk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.weight(1f))
                                DialogueChoiceButton(label = "Yes!", color = ChoiceGreen) { onComplete() }
                                DialogueChoiceButton(label = "Not yet…", color = ChoiceRed) {
                                    showChoice = false
                                    showNoLine = true
                                }
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable choice button ─────────────────────────────────────────────────
@Composable
private fun DialogueChoiceButton(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(12.dp))
            .border(2.dp, color.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 28.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = Baloo2,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}
