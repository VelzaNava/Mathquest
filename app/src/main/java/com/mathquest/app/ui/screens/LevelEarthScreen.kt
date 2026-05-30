package com.mathquest.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathquest.app.R
import com.mathquest.app.ui.theme.Baloo2
import kotlinx.coroutines.delay

private const val PAUSE_ICON_ASPECT = 173f / 155f
private const val MAX_HEARTS = 3

private val LEVELS = listOf(FightLevel.EARTH, FightLevel.AIR, FightLevel.WATER)

private fun punFor(level: FightLevel): String = when (level) {
    FightLevel.EARTH -> "You multiplied my regret… by infinity!"
    FightLevel.AIR   -> "Looks like you divided me right down to zero!"
    FightLevel.WATER -> "You broke me into a thousand fractions!"
}

private fun monsterFor(level: FightLevel, lowHp: Boolean): Int = when (level) {
    FightLevel.EARTH ->
        if (lowHp) R.drawable.monster_multiplication
        else R.drawable.monster_multiplication_default
    FightLevel.AIR   -> R.drawable.monster_division
    FightLevel.WATER -> R.drawable.monster_fraction
}

@Composable
fun LevelEarthScreen(
    playerName: String,
    startLevel: Int = 0,
    musicVolume: Float,
    sfxVolume: Float,
    onMusicVolume: (Float) -> Unit,
    onSfxVolume: (Float) -> Unit,
    onLevelComplete: () -> Unit,
    onHome: () -> Unit
) {
    val currentLevel = LEVELS[startLevel.coerceIn(0, LEVELS.lastIndex)]
    // PERF: questions() builds a List — cache it per level so we don't rebuild every recomp
    val questions = remember(currentLevel) { currentLevel.questions() }
    val backgroundRes = remember(currentLevel) { currentLevel.backgroundRes() }
    // Final boss is the last level — skips Continue button and auto-fades to credits
    val isFinalBoss = remember(startLevel) { startLevel == LEVELS.lastIndex }

    var monsterVisible  by remember { mutableStateOf(false) }
    var isPaused        by remember { mutableStateOf(false) }
    var restartKey      by remember { mutableIntStateOf(0) }
    var triggerFadeOut  by remember { mutableStateOf(false) }

    var questionIndex by remember(restartKey) { mutableIntStateOf(0) }
    var hearts        by remember(restartKey) { mutableIntStateOf(MAX_HEARTS) }
    var won           by remember(restartKey) { mutableStateOf(false) }
    var lost          by remember(restartKey) { mutableStateOf(false) }
    var hintsLeft     by remember(restartKey) { mutableIntStateOf(3) }
    var showHint      by remember { mutableStateOf(false) }

    // Pokémon-style multiple-choice state
    var selectedChoice by remember(restartKey, questionIndex) { mutableStateOf<Int?>(null) }
    var locked         by remember(restartKey, questionIndex) { mutableStateOf(false) }
    var correctTick    by remember(restartKey) { mutableIntStateOf(0) }
    var wrongTick      by remember(restartKey) { mutableIntStateOf(0) }
    var flashOn        by remember { mutableStateOf(false) }
    var monsterDying   by remember(restartKey) { mutableStateOf(false) }

    LaunchedEffect(restartKey) {
        monsterVisible = false
        triggerFadeOut = false
        delay(450)
        monsterVisible = true
    }
    LaunchedEffect(questionIndex) { showHint = false }

    // Correct answer → sharp hit flash (2 quick bursts), then advance / win
    LaunchedEffect(correctTick) {
        if (correctTick == 0) return@LaunchedEffect
        repeat(2) {
            flashOn = true
            delay(35)
            flashOn = false
            delay(25)
        }
        if (questionIndex >= questions.lastIndex) won = true
        else questionIndex += 1   // selectedChoice + locked auto-reset via remember key
    }

    // Wrong answer → keep the red highlight briefly, then unlock (or end the run)
    LaunchedEffect(wrongTick) {
        if (wrongTick == 0) return@LaunchedEffect
        delay(450)
        if (hearts <= 0) lost = true
        else {
            selectedChoice = null
            locked = false
        }
    }

    // On win: start the monster death fade for ALL levels, then handle routing
    LaunchedEffect(won) {
        if (!won) return@LaunchedEffect
        monsterDying = true          // monster fades out (1 200 ms)
        if (isFinalBoss) {
            delay(2000)              // let pun + fade run, then black-out and hand off
            triggerFadeOut = true
            delay(1400)
            onLevelComplete()
        }
    }

    val fadeToBlackAlpha by animateFloatAsState(
        targetValue = if (triggerFadeOut) 1f else 0f,
        animationSpec = tween(1400, easing = LinearEasing),
        label = "fadeToBlack"
    )

    val monsterAlpha by animateFloatAsState(
        targetValue = if (monsterVisible) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "monsterAlpha"
    )

    // White "hit" flash overlay alpha — snaps on instantly, drops off fast
    val flashAlpha by animateFloatAsState(
        targetValue = if (flashOn) 1f else 0f,
        animationSpec = tween(20, easing = LinearEasing),
        label = "hitFlash"
    )

    // Monster death fade — 0 = fully visible, going to 0 over 1 200 ms when dying
    val monsterDeathAlpha by animateFloatAsState(
        targetValue = if (monsterDying) 0f else 1f,
        animationSpec = tween(1200, easing = LinearEasing),
        label = "monsterDeath"
    )

    // PERF: only animate the monster bob during active combat. When paused / won /
    // lost the infinite transition is removed from composition entirely, so no
    // per-frame ticks or layout invalidations happen behind the scenes.
    val bobActive = monsterVisible && !isPaused && !won && !lost
    val bobY: Float = if (bobActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "monsterBob")
        val v by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -14f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bobY"
        )
        v
    } else 0f

    val currentQuestion = questions.getOrNull(questionIndex)
    val isLowHealth = hearts <= 1 && !lost && !won
    // PERF: monsterFor is a simple when() but cache it anyway to keep the painter stable
    val monsterRes = remember(currentLevel, isLowHealth) { monsterFor(currentLevel, isLowHealth) }

    val dialogueText: String =
        if (won) punFor(currentLevel)
        else currentQuestion?.question.orEmpty()

    fun chooseAnswer(idx: Int) {
        if (won || lost || locked) return
        val q = currentQuestion ?: return
        selectedChoice = idx
        locked = true
        if (q.choices.getOrNull(idx) == q.answer) {
            correctTick += 1
        } else {
            hearts = (hearts - 1).coerceAtLeast(0)
            wrongTick += 1
        }
    }

    // 0 = idle, 1 = chosen-correct (green), 2 = chosen-wrong (red)
    fun choiceState(idx: Int): Int = when {
        selectedChoice != idx -> 0
        currentQuestion?.choices?.getOrNull(idx) == currentQuestion?.answer -> 1
        else -> 2
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // PERF: stable modifier chain — 0.dp blur is a no-op fast path,
                // so we never rebuild the chain when isPaused flips.
                .blur(if (isPaused) 18.dp else 0.dp)
        ) {
            // Background
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = "${currentLevel.monsterName()} Level",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Monster — bigger now, and nudged toward the right side of the screen
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val monsterShiftX = maxWidth * 0.10f
                val monsterModifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(1.25f)
                    .fillMaxHeight(1.25f)
                    // PERF: lambda-offset reads bobY in the LAYOUT phase, not the
                    // composition phase — saves an entire screen recomposition per frame
                    .offset { IntOffset(monsterShiftX.roundToPx(), bobY.dp.roundToPx()) }

                Image(
                    painter = painterResource(id = monsterRes),
                    contentDescription = "${currentLevel.monsterName()} Monster",
                    modifier = monsterModifier.alpha(monsterAlpha * monsterDeathAlpha),
                    contentScale = ContentScale.Fit
                )

                // White hit-flash — same sprite tinted white, fades in/out on a correct hit
                // Also inherits the death alpha so it doesn't linger after the monster dies
                if (flashAlpha > 0.01f) {
                    Image(
                        painter = painterResource(id = monsterRes),
                        contentDescription = null,
                        modifier = monsterModifier.alpha(flashAlpha * monsterDeathAlpha),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }

            // (Answer box image removed — replaced by the 2×2 multiple-choice buttons)

            // Player card — FillHeight + BottomStart pins it to the very bottom-left corner
            // (eliminates the horizontal letterbox that ContentScale.Fit adds on wide screens)
            Image(
                painter = painterResource(id = R.drawable.fight_player_card),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.BottomStart,
                contentScale = ContentScale.FillHeight
            )

            // Hint button graphic (kept — submit button removed)
            if (!won && !lost) {
                Image(
                    painter = painterResource(id = R.drawable.fight_hint_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (hintsLeft > 0) 1f else 0.5f),
                    contentScale = ContentScale.Fit
                )
            }

            // Text & interactive hotspots
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val w = maxWidth
                val h = maxHeight

                // PERF: cache all derived geometry — these only change when the
                // screen size changes (which is rare), not when game state changes.
                val cardScale = remember(h) { h / 1080.dp }
                val nameOffX  = remember(cardScale) { 44.dp * cardScale }
                val nameOffY  = remember(cardScale) { 895.dp * cardScale }
                val nameW     = remember(cardScale) { 367.dp * cardScale }
                val barH      = remember(cardScale) { 22.dp * cardScale }
                val barStartX = remember(cardScale) { 140.dp * cardScale }
                val barEndX   = remember(cardScale) { 340.dp * cardScale }
                val barW      = remember(barStartX, barEndX) { (barEndX - barStartX).coerceAtLeast(10.dp) }
                val heartBarY = remember(cardScale, barH) { 982.dp * cardScale - barH / 2f }
                val hintBarY  = remember(cardScale, barH) { 1036.dp * cardScale - barH / 2f }

                // Question dialogue box — moved to the LEFT side, clear of the monster.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = w * 0.04f, top = h * 0.08f)
                        .width(w * 0.40f)
                        .background(Color(0xF2FFF8EE), RoundedCornerShape(22.dp))
                        .border(4.dp, Color(0xFFD4A76A), RoundedCornerShape(22.dp))
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dialogueText,
                        fontFamily = Baloo2,
                        fontWeight = FontWeight.ExtraBold,
                        // Pun text is longer so use a smaller size when won
                        fontSize = if (won) 18.asp() else 22.asp(),
                        color = Color(0xFF1E1B4B),
                        textAlign = TextAlign.Center,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Player name — below portrait frame, centred inside card (canvas y ≈ 900)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = nameOffX, y = nameOffY)
                        .width(nameW),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerName.ifBlank { "Hero" },
                        fontFamily = Baloo2,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.asp(),
                        color = Color(0xFF1E1B4B),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                // ❤ Health bar — centred on heart icon (canvas y = 982)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = barStartX, y = heartBarY)
                        .width(barW)
                        .height(barH)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x661E1B4B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(hearts.toFloat() / MAX_HEARTS.toFloat())
                            .background(Color(0xFFE53E3E))
                    )
                }

                // 💡 Hints bar — centred on lightbulb icon (canvas y = 1036)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = barStartX, y = hintBarY)
                        .width(barW)
                        .height(barH)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x66453A00))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(hintsLeft.toFloat() / 3f)
                            .background(Color(0xFFFFD600))
                    )
                }

                if (!won && !lost) {
                    // 2×2 multiple-choice grid (Pokémon-style) — bottom-right, clear of the player card
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = w * 0.30f, end = w * 0.04f, bottom = h * 0.045f),
                        verticalArrangement = Arrangement.spacedBy(h * 0.018f)
                    ) {
                        val q = currentQuestion
                        if (q != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(w * 0.02f)
                            ) {
                                AnswerButton(q.choices.getOrElse(0) { "" }, choiceState(0), !locked,
                                    Modifier.weight(1f).height(h * 0.085f)) { chooseAnswer(0) }
                                AnswerButton(q.choices.getOrElse(1) { "" }, choiceState(1), !locked,
                                    Modifier.weight(1f).height(h * 0.085f)) { chooseAnswer(1) }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(w * 0.02f)
                            ) {
                                AnswerButton(q.choices.getOrElse(2) { "" }, choiceState(2), !locked,
                                    Modifier.weight(1f).height(h * 0.085f)) { chooseAnswer(2) }
                                AnswerButton(q.choices.getOrElse(3) { "" }, choiceState(3), !locked,
                                    Modifier.weight(1f).height(h * 0.085f)) { chooseAnswer(3) }
                            }
                        }
                    }

                    // Hint button tap hotspot — button sits at ~49–76% x, 65–75% y of the canvas
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = w * 0.49f, y = h * 0.65f)
                            .size(width = w * 0.27f, height = h * 0.10f)
                            .clickable(
                                enabled = hintsLeft > 0 || showHint,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!showHint) {
                                    showHint = true
                                    hintsLeft--
                                } else {
                                    showHint = false
                                }
                            }
                    )

                    // Hint popup card — floats above the hint button
                    if (showHint && currentQuestion != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = h * 0.38f)
                                .fillMaxWidth(0.44f)
                                .background(Color(0xF0FFFDF5), RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "Hint",
                                fontFamily = Baloo2,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.asp(),
                                color = Color(0xFF92400E)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = currentQuestion.hint,
                                fontFamily = Baloo2,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.asp(),
                                color = Color(0xFF1E1B4B),
                                textAlign = TextAlign.Start
                            )
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1B4B), RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showHint = false }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Got it",
                                    fontFamily = Baloo2,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.asp(),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Continue image button — only for non-final bosses (final boss auto-fades)
                if (won && !isFinalBoss) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_continue_game),
                        contentDescription = "Continue",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = h * 0.10f)
                            .width(w * 0.22f)
                            .aspectRatio(538f / 121f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onLevelComplete() },
                        contentScale = ContentScale.Fit
                    )
                }

                // Pause button — top-right (hidden once the game is won or lost)
                if (!won && !lost) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_pause),
                        contentDescription = "Pause",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 14.dp, end = 14.dp)
                            .height(h * 0.12f)
                            .aspectRatio(PAUSE_ICON_ASPECT)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isPaused = true },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Defeat overlay
            if (lost) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Defeated…",
                            fontFamily = Baloo2,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 56.asp(),
                            color = Color(0xFFFF6B6B)
                        )
                        Spacer(Modifier.height(18.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xCC1E1B4B), RoundedCornerShape(999.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { restartKey++ }
                                .padding(horizontal = 28.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Try Again",
                                fontFamily = Baloo2,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.asp(),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Final-boss fade-to-black overlay — sits above everything including blur layer
        if (isFinalBoss && (won || triggerFadeOut)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(fadeToBlackAlpha)
                    .background(Color.Black)
            )
        }

        // Pause overlay
        PauseOverlay(
            visible = isPaused,
            musicVolume = musicVolume,
            sfxVolume = sfxVolume,
            onMusicVolume = onMusicVolume,
            onSfxVolume = onSfxVolume,
            onResume = { isPaused = false },
            onRestart = {
                isPaused = false
                restartKey++
            },
            onHome = onHome
        )
    }
}

// ── One multiple-choice answer button (Pokémon-style) ──────────────────────────
// state: 0 = idle, 1 = chosen & correct (green), 2 = chosen & wrong (red)
@Composable
private fun AnswerButton(
    text: String,
    state: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when (state) {
        1 -> Color(0xFF16A34A)    // correct → green
        2 -> Color(0xFFDC2626)    // wrong → red
        else -> Color(0xF2FFF8EE) // idle → parchment
    }
    val fg = if (state == 0) Color(0xFF1E1B4B) else Color.White
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(16.dp))
            .border(3.dp, Color(0xFFD4A76A), RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Baloo2,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.asp(),
            color = fg,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
