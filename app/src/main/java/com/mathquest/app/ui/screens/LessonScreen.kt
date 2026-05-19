package com.mathquest.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// ── Shared palette ────────────────────────────────────────────────────────────
internal val ChalkCream   = Color(0xFFFFF8EE)
internal val ChalkBorder  = Color(0xFFD4A76A)
internal val ChalkInk     = Color(0xFF3B1F07)
internal val CorrectGreen = Color(0xFF16A34A)
internal val WrongRed     = Color(0xFFDC2626)
internal val NeutralBlue  = Color(0xFF2563EB)
internal val TeacherLabel = Color(0xFF92400E)

// ── Shared data model ─────────────────────────────────────────────────────────
internal data class QuizChoice(val label: String, val feedback: String, val correct: Boolean)

internal sealed class LessonStep {
    data class Narrate(val text: String) : LessonStep()
    data class Quiz(val question: String, val choices: List<QuizChoice>) : LessonStep()
    object Finale : LessonStep()
}

// Teacher sprite cycle (3 images loop; teacher_end only on Finale)
internal val TEACHER_CYCLE = listOf(
    R.drawable.teacher_default,
    R.drawable.teacher_smile,
    R.drawable.teacher_thumbsup,
)

// ── Shared lesson screen ──────────────────────────────────────────────────────
@Composable
internal fun LessonScreen(
    steps: List<LessonStep>,
    finaleText: String,
    onComplete: () -> Unit
) {
    var stepIdx        by remember { mutableStateOf(0) }
    var selectedChoice by remember { mutableStateOf<Int?>(null) }

    val step = steps[stepIdx]
    val teacherRes = if (step is LessonStep.Finale) R.drawable.teacher_end
                     else TEACHER_CYCLE[stepIdx % 3]

    fun nextStep() {
        selectedChoice = null
        if (stepIdx + 1 >= steps.size) onComplete()
        else stepIdx++
    }

    var visible by remember { mutableStateOf(false) }
    val screenAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = LinearEasing),
        label = "lessonFade"
    )
    LaunchedEffect(Unit) { visible = true }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
    ) {
        val sw = maxWidth

        // Chalkboard background
        Image(
            painter = painterResource(R.drawable.bg_chalkboard),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Lesson card — compact square-ish box, truly centred on screen
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.54f),        // caps width so card doesn't stretch wall-to-wall
        ) {
            LessonCard(
                step = step,
                finaleText = finaleText,
                selectedChoice = selectedChoice,
                onChoiceTap = { idx -> selectedChoice = idx },
                onAdvance = { nextStep() }
            )
        }

        // Teacher (left side, bottom-anchored) — rendered after card so she stands in front
        // Container at 52% keeps her proportional; the character itself only occupies the left ~26% of the canvas
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.52f)
                .align(Alignment.BottomStart),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(teacherRes),
                contentDescription = "Teacher",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ── Lesson card ───────────────────────────────────────────────────────────────
@Composable
internal fun LessonCard(
    step: LessonStep,
    finaleText: String,
    selectedChoice: Int?,
    onChoiceTap: (Int) -> Unit,
    onAdvance: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChalkCream, RoundedCornerShape(20.dp))
            .border(3.dp, ChalkBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Speaker label tab
        Box(
            modifier = Modifier
                .background(
                    TeacherLabel,
                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                "Teacher",
                fontFamily = Baloo2,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(10.dp))

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            when (step) {
                is LessonStep.Narrate -> LessonNarration(step.text)
                is LessonStep.Quiz    -> LessonQuiz(step, selectedChoice, onChoiceTap)
                is LessonStep.Finale  -> LessonNarration(finaleText)
            }
        }

        Spacer(Modifier.height(12.dp))

        val canAdvance = step !is LessonStep.Quiz || selectedChoice != null
        if (canAdvance) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(
                        if (step is LessonStep.Finale) CorrectGreen else Color(0xFF7C3AED),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAdvance() }
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (step is LessonStep.Finale) "Continue Adventure!" else "Continue  ▶",
                    fontFamily = Baloo2,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

// ── Narration text ────────────────────────────────────────────────────────────
@Composable
private fun LessonNarration(text: String) {
    Text(
        text = text,
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        color = ChalkInk
    )
}

// ── Quiz choices ──────────────────────────────────────────────────────────────
@Composable
private fun LessonQuiz(
    step: LessonStep.Quiz,
    selectedChoice: Int?,
    onChoiceTap: (Int) -> Unit
) {
    Text(
        text = step.question,
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        color = ChalkInk
    )

    Spacer(Modifier.height(14.dp))

    step.choices.forEachIndexed { idx, choice ->
        val isSelected = selectedChoice == idx
        val bgColor = when {
            selectedChoice == null           -> NeutralBlue
            isSelected && choice.correct     -> CorrectGreen
            isSelected && !choice.correct    -> WrongRed
            !isSelected && choice.correct    -> CorrectGreen  // reveal answer
            else                             -> Color(0xFF9CA3AF)
        }
        val enabled = selectedChoice == null

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(bgColor, RoundedCornerShape(10.dp))
                .then(
                    if (enabled) Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onChoiceTap(idx) } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = choice.label,
                fontFamily = Baloo2,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (selectedChoice != null) {
        Spacer(Modifier.height(10.dp))
        val chosen = step.choices[selectedChoice]
        Text(
            text = chosen.feedback,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (chosen.correct) CorrectGreen else WrongRed,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
