package com.mathquest.app.ui.screens

import androidx.compose.runtime.Composable

// ── Fractions lesson data ─────────────────────────────────────────────────────
private val FRACTIONS_STEPS: List<LessonStep> = listOf(
    LessonStep.Narrate(
        "Welcome to the final chapter — Fractions!\n\nDon't panic. Fractions are just parts of a whole.\nYou deal with fractions every day without even knowing it!"
    ),
    LessonStep.Narrate(
        "A fraction looks like this:  3/4\n\n• Top number  → Numerator\n  (how many parts we HAVE)\n\n• Bottom number → Denominator\n  (how many equal parts in ALL)\n\n3/4 means: 3 out of 4 equal parts."
    ),
    LessonStep.Narrate(
        "Imagine a pizza cut into 4 equal slices.\nYou eat 1 slice.\n\nYou ate  1/4  of the pizza.\n\nThe denominator (4) = total slices.\nThe numerator (1) = slices you ate.\n\nSee? You already understand fractions."
    ),
    LessonStep.Quiz(
        question = "A chocolate bar is cut into 8 equal pieces.\nYou eat 3 pieces.\n\nWhat fraction of the bar did you eat?",
        choices = listOf(
            QuizChoice("3/8", "Correct! 3 pieces out of 8 total = 3/8.", true),
            QuizChoice("3/5", "The bar has 8 pieces, not 5!", false),
            QuizChoice("5/8", "That's the part you DIDN'T eat.", false),
        )
    ),
    LessonStep.Narrate(
        "Types of fractions:\n\n• Proper fraction — numerator < denominator\n  Example: 3/4, 1/2\n  (Less than one whole)\n\n• Improper fraction — numerator ≥ denominator\n  Example: 5/4, 7/3\n  (One whole or more)\n\n• Mixed number — whole number + fraction\n  Example: 1  3/4\n  (A whole plus extra parts)"
    ),
    LessonStep.Narrate(
        "Comparing fractions:\n\nSame denominator → bigger numerator wins\n  1/4 < 2/4 < 3/4\n\nSame numerator → SMALLER denominator wins\n  1/2 > 1/3 > 1/4\n\n(Bigger denominator = smaller slices!)"
    ),
    LessonStep.Quiz(
        question = "Which fraction is the BIGGEST?",
        choices = listOf(
            QuizChoice("1/2", "Correct! Same numerator — 1/2 has the smallest denominator, so it's biggest.", true),
            QuizChoice("1/3", "1/3 is smaller than 1/2.", false),
            QuizChoice("1/4", "1/4 is the smallest of the three.", false),
        )
    ),
    LessonStep.Narrate(
        "Finding a fraction of a number:\n\nTo find  3/5  of 20:\n\nStep 1: Divide by the denominator → 20 ÷ 5 = 4\nStep 2: Multiply by the numerator  → 4 × 3 = 12\n\nSo  3/5  of 20 = 12!"
    ),
    LessonStep.Narrate(
        "Let's use G.A.I.N.S with fractions!\n\nProblem: A gardener planted vegetables in 3/5 of her 20-section plot.\nHow many sections have vegetables?"
    ),
    LessonStep.Narrate(
        "G: 20 sections total, 3/5 has vegetables.\nA: How many sections have vegetables?\nI: Fraction of a whole number.\nN: 3/5 × 20\nS: 20 ÷ 5 = 4,  then  4 × 3 = 12\n\n12 sections have vegetables!"
    ),
    LessonStep.Quiz(
        question = "A farmer has 30 apples.\nHe gives 2/5 of them to a neighbor.\n\nHow many apples did he give away?\n\n2/5 × 30 = ?",
        choices = listOf(
            QuizChoice("12", "Correct! 30 ÷ 5 = 6, then 6 × 2 = 12.", true),
            QuizChoice("15", "That would be 1/2 of 30.", false),
            QuizChoice("18", "18 would be 3/5 of 30.", false),
        )
    ),
    LessonStep.Finale,
)

private const val FRACTIONS_FINALE =
    "INCREDIBLE!\nYou've conquered fractions — the final frontier!\n\nYou now understand parts of a whole, numerators, denominators, and fraction of a number.\n\nYou're a true Math Quest champion.\nGo face the Fraction Monster with confidence!\n\nGood luck out there, hero."

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun FractionsLessonScreen(onComplete: () -> Unit) {
    LessonScreen(
        steps = FRACTIONS_STEPS,
        finaleText = FRACTIONS_FINALE,
        onComplete = onComplete
    )
}
