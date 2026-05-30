package com.mathquest.app.ui.screens

import com.mathquest.app.R

data class FightQuestion(
    val question: String,
    val answer: String,
    val hint: String,
    // 4 multiple-choice options (Pokémon style). Must include [answer].
    val choices: List<String>
)

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
    FightQuestion("What is 6 × 7?",  "42", "Count by 6s: 6, 12, 18, 24, 30, 36, 42!", listOf("42", "48", "36", "49")),
    FightQuestion("What is 9 × 8?",  "72", "Try 10 × 8 = 80, then subtract 8.",       listOf("72", "81", "64", "63")),
    FightQuestion("What is 4 × 12?", "48", "4 × 10 = 40, plus 4 × 2 = 8.",            listOf("48", "44", "36", "52")),
    FightQuestion("What is 7 × 7?",  "49", "Seven squared — count up seven 7s.",      listOf("49", "42", "56", "14")),
    FightQuestion("What is 3 × 15?", "45", "3 × 10 = 30, plus 3 × 5 = 15.",           listOf("45", "35", "50", "48")),
)

private val DIVISION_QUESTIONS = listOf(
    FightQuestion("What is 56 ÷ 8?", "7", "Ask: 8 × ? = 56.", listOf("7", "8", "6", "9")),
    FightQuestion("What is 48 ÷ 6?", "8", "Ask: 6 × ? = 48.", listOf("8", "7", "9", "6")),
    FightQuestion("What is 72 ÷ 9?", "8", "Ask: 9 × ? = 72.", listOf("8", "9", "7", "6")),
    FightQuestion("What is 36 ÷ 4?", "9", "Ask: 4 × ? = 36.", listOf("9", "8", "7", "6")),
    FightQuestion("What is 45 ÷ 5?", "9", "Ask: 5 × ? = 45.", listOf("9", "8", "7", "10")),
)

private val FRACTION_QUESTIONS = listOf(
    FightQuestion("What is 1/2 of 24?", "12", "Half means divide by 2.",          listOf("12", "10", "14", "8")),
    FightQuestion("What is 1/4 of 36?", "9",  "A quarter — divide by 4.",          listOf("9", "6", "12", "8")),
    FightQuestion("What is 3/4 of 20?", "15", "Find 1/4 first (5), then × 3.",     listOf("15", "12", "10", "18")),
    FightQuestion("What is 2/5 of 35?", "14", "1/5 of 35 = 7, then × 2.",          listOf("14", "10", "12", "16")),
    FightQuestion("What is 1/3 of 45?", "15", "Divide 45 by 3.",                   listOf("15", "12", "9", "18")),
)
