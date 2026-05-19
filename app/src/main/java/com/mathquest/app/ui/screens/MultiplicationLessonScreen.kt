package com.mathquest.app.ui.screens

import androidx.compose.runtime.Composable

// ── Multiplication lesson data ────────────────────────────────────────────────
private val MULTIPLICATION_STEPS: List<LessonStep> = listOf(
    LessonStep.Narrate(
        "Hey! You made it to the multiplication training ground.\n\nReady to become a multiplication master?"
    ),
    LessonStep.Narrate(
        "Whether you like it or not… I'm going to teach you multiplication!\n\nI know it can look scary when the numbers get bigger — but trust me, it's easier than it looks."
    ),
    LessonStep.Narrate(
        "Before we begin — what even is multiplication?\n\nMultiplication is repeated addition.\nIt means adding the same number again and again."
    ),
    LessonStep.Narrate(
        "Imagine a farmer planting crops.\nThe farmer planted 3 rows of carrots.\nEach row has 4 carrots.\n\nWould you count them one by one?"
    ),
    LessonStep.Narrate(
        "Instead, we multiply.\n3 groups of 4 means:\n\n    3 × 4 = 12\n\nSo there are 12 carrots in total!"
    ),
    LessonStep.Narrate(
        "Let's break it down:\n\n• 1st number → how many groups\n• 2nd number → how many in each group\n• Multiply them → the total!"
    ),
    LessonStep.Quiz(
        question = "Imagine there are 5 baskets.\nEach basket has 2 tomatoes.\n\nHow many tomatoes are there in total?",
        choices = listOf(
            QuizChoice("10", "Correct! 5 × 2 = 10.", true),
            QuizChoice("7",  "Nope — some tomatoes disappeared!", false),
            QuizChoice("12", "That's too many tomatoes.", false),
        )
    ),
    LessonStep.Narrate(
        "Since there are 5 groups with 2 tomatoes each:\n\n    5 × 2 = 10\n\nPretty easy, right?"
    ),
    LessonStep.Narrate(
        "But sometimes math problems are written in sentences instead of numbers.\nThat can get confusing.\n\nThat's why we use the G.A.I.N.S Method!"
    ),
    LessonStep.Narrate(
        "The G.A.I.N.S Method:\n\nG – Given\nA – Asked\nI – Indicated Operation\nN – Number Sentence\nS – Solution"
    ),
    LessonStep.Narrate(
        "Let's try G.A.I.N.S together!\n\nProblem: A gardener planted 4 rows of cabbage. Each row has 3 cabbages.\nHow many cabbages are there in total?"
    ),
    LessonStep.Narrate(
        "G: 4 rows, 3 cabbages each.\nA: Total cabbages.\nI: Multiplication — we see equal groups!\nN: 4 × 3\nS: 4 × 3 = 12\n\nThere are 12 cabbages in total!"
    ),
    LessonStep.Narrate(
        "Now let's try bigger numbers.\n\n6 × 3 can be thought of as 6 + 6 + 6 = 18.\n\nNow you try: 7 × 4 = ?"
    ),
    LessonStep.Quiz(
        question = "7 × 4 = ?",
        choices = listOf(
            QuizChoice("28", "Correct! Nice work!", true),
            QuizChoice("21", "Incorrect! Remember: 7 groups of 4.", false),
            QuizChoice("32", "Nope — close, but not quite!", false),
        )
    ),
    LessonStep.Narrate(
        "Now let's apply multiplication to a word problem.\n\nA farmer has 8 garden plots.\nEach plot has 6 pepper plants.\n\n    8 × 6 = 48\n\nThere are 48 pepper plants in total."
    ),
    LessonStep.Quiz(
        question = "Your turn!\n\nThere are 9 rows of onions.\nEach row has 4 onions.\nHow many onions are there altogether?",
        choices = listOf(
            QuizChoice("36", "Correct! 9 × 4 = 36!", true),
            QuizChoice("32", "Incorrect! Count those groups carefully.", false),
            QuizChoice("49", "Bro multiplied the wrong numbers.", false),
        )
    ),
    LessonStep.Narrate(
        "A multiplication warrior also needs tricks!\n\n• Anything × 1 = itself\n  (Example: 9 × 1 = 9)\n\n• Anything × 0 = 0\n  (Example: 15 × 0 = 0)\n\nThe zero destroys everything. Terrifying."
    ),
    LessonStep.Quiz(
        question = "Final challenge!\n\nA farmer harvested 12 baskets of potatoes.\nEach basket has 5 potatoes.\nHow many potatoes were harvested in total?",
        choices = listOf(
            QuizChoice("60", "Correct! 12 × 5 = 60. You did it!", true),
            QuizChoice("55", "Close, but no.", false),
            QuizChoice("72", "Nope — try again!", false),
        )
    ),
    LessonStep.Finale,
)

private const val MULTIPLICATION_FINALE =
    "You did it!\nYou survived multiplication training.\n\nWell, that wraps up our lesson.\nGood luck out there, child.\nUse your multiplication powers wisely."

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun MultiplicationLessonScreen(onComplete: () -> Unit) {
    LessonScreen(
        steps = MULTIPLICATION_STEPS,
        finaleText = MULTIPLICATION_FINALE,
        onComplete = onComplete
    )
}
