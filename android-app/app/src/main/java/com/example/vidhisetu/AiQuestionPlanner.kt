package com.example.vidhisetu

interface AiQuestionPlanner {

    suspend fun getNextQuestion(
        caseContext: CaseContext
    ): AiQuestionResult
}

data class AiQuestionResult(
    val complete: Boolean,
    val question: String?
)