package com.example.vidhisetu

class AnswerInterpreter {

    fun addAnswer(
        context: CaseContext,
        questionNumber: Int,
        answer: String
    ) {

        when (questionNumber) {

            1 -> {
                context.timeline = answer
            }

            2 -> {
                context.peopleInvolved = answer
            }

            3 -> {
                context.documents = answer
            }

            4 -> {
                context.actionsTaken = answer
            }

            5 -> {
                context.desiredOutcome = answer
            }
        }

        context.questionCount++
    }
}