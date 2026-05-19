package com.mathquest.app.ui.screens

import androidx.compose.runtime.Composable

// ── Division lesson data ──────────────────────────────────────────────────────
private val DIVISION_STEPS: List<LessonStep> = listOf(
    LessonStep.Narrate(
        "Welcome back, champion!\n\nYou defeated multiplication — now brace yourself for its arch-rival: Division!"
    ),
    LessonStep.Narrate(
        "What is division?\n\nDivision means splitting a total into equal groups.\nIt's the OPPOSITE of multiplication.\n\nIf multiplication builds groups, division breaks them apart."
    ),
    LessonStep.Narrate(
        "Know your vocabulary!\n\n• Dividend — the total being divided\n• Divisor  — how many groups (or size of each group)\n• Quotient — the answer\n\nExample:  12 ÷ 3 = 4\nDividend ÷ Divisor = Quotient"
    ),
    LessonStep.Narrate(
        "Think of it like sharing!\n\nA farmer has 20 carrots. He wants to put them equally into 4 baskets.\n\n20 ÷ 4 = 5 carrots per basket\n\nEveryone gets the same amount — that's division!"
    ),
    LessonStep.Narrate(
        "Division and multiplication are best friends!\n\nIf 4 × 5 = 20, then 20 ÷ 4 = 5.\n\nThis is called a FACT FAMILY.\nUse multiplication facts to help you divide faster!"
    ),
    LessonStep.Quiz(
        question = "A farmer picked 18 tomatoes and sorted them equally into 3 baskets.\n\nHow many tomatoes are in each basket?\n\n18 ÷ 3 = ?",
        choices = listOf(
            QuizChoice("6", "Correct! 3 × 6 = 18, so 18 ÷ 3 = 6.", true),
            QuizChoice("5", "Not quite — check the fact family!", false),
            QuizChoice("9", "Too many! Try again.", false),
        )
    ),
    LessonStep.Narrate(
        "Let's use the G.A.I.N.S Method for division!\n\nProblem: A gardener has 24 seeds. He plants them in rows of 6.\nHow many rows are there?"
    ),
    LessonStep.Narrate(
        "G: 24 seeds total, 6 per row.\nA: How many rows?\nI: Division — splitting into equal groups!\nN: 24 ÷ 6\nS: 24 ÷ 6 = 4\n\nThere are 4 rows of seeds!"
    ),
    LessonStep.Quiz(
        question = "Your turn with G.A.I.N.S!\n\nThere are 35 peppers. Each basket holds 7 peppers.\nHow many baskets are needed?\n\n35 ÷ 7 = ?",
        choices = listOf(
            QuizChoice("5", "Correct! 7 × 5 = 35, so 35 ÷ 7 = 5!", true),
            QuizChoice("6", "Close, but 7 × 6 = 42. Try again!", false),
            QuizChoice("4", "Not enough baskets — some peppers are homeless!", false),
        )
    ),
    LessonStep.Narrate(
        "Division rules to remember:\n\n• Any number ÷ 1 = itself\n  (Example: 9 ÷ 1 = 9)\n\n• Any number ÷ itself = 1\n  (Example: 8 ÷ 8 = 1)\n\n• 0 ÷ any number = 0\n  (Example: 0 ÷ 5 = 0)\n\n• You CANNOT divide by 0!"
    ),
    LessonStep.Quiz(
        question = "Final challenge!\n\nA farmer harvested 56 ears of corn and packs them into bags of 8.\nHow many bags does he fill?\n\n56 ÷ 8 = ?",
        choices = listOf(
            QuizChoice("7", "Correct! 8 × 7 = 56. Champion!", true),
            QuizChoice("6", "6 bags only holds 48 — 8 ears are left behind!", false),
            QuizChoice("8", "8 bags would need 64 — too many!", false),
        )
    ),
    LessonStep.Finale,
)

private const val DIVISION_FINALE =
    "Outstanding!\nYou've mastered the art of division.\n\nRemember — division is just sharing equally.\nWhenever numbers need splitting, you're the one for the job.\n\nNow go show that Division Monster what you've learned!"

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun DivisionLessonScreen(onComplete: () -> Unit) {
    LessonScreen(
        steps = DIVISION_STEPS,
        finaleText = DIVISION_FINALE,
        onComplete = onComplete
    )
}
