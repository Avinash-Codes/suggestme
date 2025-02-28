package com.collegegrad.suggestme.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class AssessmentResult(
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val questions: List<QuestionResult> = emptyList()
)
@Serializable
data class QuestionResult(
    val question: String = "",
    val selectedAnswer: String = "",
    val correctAnswer: String = "",
    val isCorrect: Boolean = false,
    val options: List<String> = emptyList()
)