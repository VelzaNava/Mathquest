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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    var answerText    by remember(restartKey) { mutableStateOf("") }
    var won           by remember(restartKey) { mutableStateOf(false) }
    var lost          by remember(restartKey) { mutableStateOf(false) }
    var hintsLeft     by remember(restartKey) { mutableIntStateOf(3) }
    var showHint      by remember { mutableStateOf(false) }

    LaunchedEffect(restartKey) {
        monsterVisible = false
        triggerFadeOut = false
        delay(450)
        monsterVisible = true
    }
    LaunchedEffect(questionIndex) { showHint = false }

    // Final boss: after pun shows for 2 s, fade the whole screen to black then hand off
    LaunchedEffect(won) {
        if (won && isFinalBoss) {
            delay(2000)           // let the pun text sit for a moment
            triggerFadeOut = true
            delay(1400)           // fade duration
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

    fun submitAnswer() {
        if (won || lost) return
        val q = currentQuestion ?: return
        val typed = answerText.trim()
        if (typed.isEmpty()) return
        if (typed == q.answer) {
            if (questionIndex >= questions.lastIndex) won = true
            else questionIndex += 1
        } else {
            hearts = (hearts - 1).coerceAtLeast(0)
            if (hearts <= 0) lost = true
        }
        answerText = ""
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

            // Monster (centered)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = monsterRes),
                    contentDescription = "${currentLevel.monsterName()} Monster",
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.95f)
                        // PERF: lambda-offset reads bobY in the LAYOUT phase, not the
                        // composition phase — saves an entire screen recomposition per frame
                        .offset { IntOffset(0, bobY.dp.roundToPx()) }
                        .alpha(monsterAlpha),
                    contentScale = ContentScale.Fit
                )
            }

            // Answer box (hidden after monster defeated)
            if (!won && !lost) {
                Image(
                    painter = painterResource(id = R.drawable.fight_answer_box),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Dialogue box (always visible)
            Image(
                painter = painterResource(id = R.drawable.fight_dialogue_box),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Player card — FillHeight + BottomStart pins it to the very bottom-left corner
            // (eliminates the horizontal letterbox that ContentScale.Fit adds on wide screens)
            Image(
                painter = painterResource(id = R.drawable.fight_player_card),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.BottomStart,
                contentScale = ContentScale.FillHeight
            )

            // Submit + hint buttons (active during combat)
            if (!won && !lost) {
                Image(
                    painter = painterResource(id = R.drawable.fight_submit_btn),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
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

                // Question / pun — strictly inside dialogue box
                // Canvas bounds: x 24.2%–75.8% (51.6% wide), y 0%–18% tall
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(width = w * 0.50f, height = h * 0.175f)
                        .clip(androidx.compose.ui.graphics.RectangleShape) // hard clip — text cannot escape
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dialogueText,
                        fontFamily = Baloo2,
                        fontWeight = FontWeight.ExtraBold,
                        // Pun text is longer so use a smaller size when won
                        fontSize = if (won) 16.sp else 24.sp,
                        color = Color(0xFF1E1B4B),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
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
                        fontSize = 11.sp,
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
                    // Answer input field
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = w * 0.55f, y = h * 0.760f)
                            .size(width = w * 0.30f, height = h * 0.060f),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        BasicTextField(
                            value = answerText,
                            onValueChange = { new ->
                                answerText = new.filter { it.isDigit() }.take(6)
                            },
                            textStyle = TextStyle(
                                fontFamily = Baloo2,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color(0xFF1E1B4B),
                                textAlign = TextAlign.Start
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { submitAnswer() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Submit hotspot
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = h * 0.075f)
                            .size(width = w * 0.18f, height = h * 0.10f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { submitAnswer() }
                    )

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
                                fontSize = 14.sp,
                                color = Color(0xFF92400E)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = currentQuestion.hint,
                                fontFamily = Baloo2,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
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
                                    fontSize = 13.sp,
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
            }

            // Pause button — top-right (hidden once the game is won or lost)
            if (!won && !lost) {
                Image(
                    painter = painterResource(id = R.drawable.btn_pause),
                    contentDescription = "Pause",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp, end = 14.dp)
                        .height(64.dp)
                        .aspectRatio(PAUSE_ICON_ASPECT)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isPaused = true },
                    contentScale = ContentScale.Fit
                )
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
                            fontSize = 56.sp,
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
                                fontSize = 18.sp,
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
