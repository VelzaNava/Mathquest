package com.mathquest.app.ui.screens

import com.mathquest.app.R

data class FightQuestion(val question: String, val answer: String, val hint: String)

enum class FightLevel {
    EARTH, AIR, WATER;

    fun backgroundRes(): Int = when (this) {
        EARTH -> R.drawable.bg_earth
        AIR   -> R.drawable.bg_air
        WATER -> R.drawable.bg_water
    }

    fun monsterRes(): Int = when (this) {
        EARTH -> R.drawable.monster_multiplication
        AIR   -> R.drawable.monster_division
        WATER -> R.drawable.monster_fraction
    }

    fun monsterName(): String = when (this) {
        EARTH -> "Multiplication"
        AIR   -> "Division"
        WATER -> "Fraction"
    }

    fun questions(): List<FightQuestion> = when (this) {
        EARTH -> MULTIPLICATION_QUESTIONS
        AIR   -> DIVISION_QUESTIONS
        WATER -> FRACTION_QUESTIONS
    }
}

private val MULTIPLICATION_QUESTIONS = listOf(
    FightQuestion("What is 6 × 7?",  "42", "Count by 6s: 6, 12, 18, 24, 30, 36, 42!"),
    FightQuestion("What is 9 × 8?",  "72", "Try 10 × 8 = 80, then subtract 8."),
    FightQuestion("What is 4 × 12?", "48", "4 × 10 = 40, plus 4 × 2 = 8."),
    FightQuestion("What is 7 × 7?",  "49", "Seven squared — count up seven 7s."),
    FightQuestion("What is 3 × 15?", "45", "3 × 10 = 30, plus 3 × 5 = 15."),
)

private val DIVISION_QUESTIONS = listOf(
    FightQuestion("What is 56 ÷ 8?", "7", "Ask: 8 × ? = 56."),
    FightQuestion("What is 48 ÷ 6?", "8", "Ask: 6 × ? = 48."),
    FightQuestion("What is 72 ÷ 9?", "8", "Ask: 9 × ? = 72."),
    FightQuestion("What is 36 ÷ 4?", "9", "Ask: 4 × ? = 36."),
    FightQuestion("What is 45 ÷ 5?", "9", "Ask: 5 × ? = 45."),
)

private val FRACTION_QUESTIONS = listOf(
    FightQuestion("What is 1/2 of 24?", "12", "Half means divide by 2."),
    FightQuestion("What is 1/4 of 36?", "9",  "A quarter — divide by 4."),
    FightQuestion("What is 3/4 of 20?", "15", "Find 1/4 first (5), then × 3."),
    FightQuestion("What is 2/5 of 35?", "14", "1/5 of 35 = 7, then × 2."),
    FightQuestion("What is 1/3 of 45?", "15", "Divide 45 by 3."),
)
