package com.example.vidhisetu

class QuestionPlanner {

    companion object {

        const val COMPLETE = "COMPLETE"

        private const val MAX_QUESTIONS = 5
    }

    fun getNextQuestion(
        context: CaseContext
    ): String {

        if (context.questionCount >= MAX_QUESTIONS) {
            return COMPLETE
        }

        /*
         * Question 1:
         * Understand when the event happened.
         */
        if (context.timeline.isBlank()) {

            return when {

                isRentalMatter(context) ->
                    "When did you move out of the property or when did the rental issue begin?"

                isEmploymentMatter(context) ->
                    "When did this employment-related problem happen?"

                isPoliceMatter(context) ->
                    "When did this incident happen?"

                isPropertyMatter(context) ->
                    "When did this property-related problem begin?"

                isFamilyMatter(context) ->
                    "When did this situation begin?"

                else ->
                    "When did this situation happen, and what happened first?"
            }
        }

        /*
         * Question 2:
         * Identify the people / organisations involved.
         */
        if (context.peopleInvolved.isBlank()) {

            return when {

                isRentalMatter(context) ->
                    "Who is involved in the rental matter, such as the landlord, tenant, or property manager?"

                isEmploymentMatter(context) ->
                    "Who is directly involved, such as your employer, manager, or another person at the workplace?"

                isPoliceMatter(context) ->
                    "Which person, police station, authority, or organisation is directly involved?"

                isPropertyMatter(context) ->
                    "Who is involved in the property matter?"

                isFamilyMatter(context) ->
                    "Who are the people directly involved in this matter?"

                else ->
                    "Who is directly involved in this situation?"
            }
        }

        /*
         * Question 3:
         * Find documentary evidence.
         */
        if (context.documents.isBlank()) {

            return when {

                isRentalMatter(context) ->
                    "Do you have a rental agreement, payment records, messages, receipts, or any other evidence?"

                isEmploymentMatter(context) ->
                    "Do you have an employment agreement, salary records, emails, messages, notices, or other documents?"

                isPoliceMatter(context) ->
                    "Do you have a complaint, FIR, notice, messages, photographs, recordings, or other documents?"

                isPropertyMatter(context) ->
                    "Do you have property documents, agreements, receipts, notices, or other evidence?"

                else ->
                    "Do you have any documents, messages, receipts, photographs, agreements, or other evidence related to this matter?"
            }
        }

        /*
         * Question 4:
         * Understand what the citizen has already done.
         */
        if (context.actionsTaken.isBlank()) {

            return "Have you already taken any action to resolve the problem? If yes, what happened?"
        }

        /*
         * Question 5:
         * Understand the citizen's objective.
         */
        if (context.desiredOutcome.isBlank()) {

            return when {

                isRentalMatter(context) ->
                    "What would you like to happen now, such as getting the deposit back, resolving the dispute, or taking another action?"

                isEmploymentMatter(context) ->
                    "What outcome are you looking for from this situation?"

                isPropertyMatter(context) ->
                    "What would you like to achieve regarding the property issue?"

                else ->
                    "What outcome are you hoping to achieve from this situation?"
            }
        }

        return COMPLETE
    }

    // =========================================================
    // BASIC ISSUE DETECTION
    // =========================================================

    private fun isRentalMatter(
        context: CaseContext
    ): Boolean {

        val text = context.buildSummary()
            .lowercase()

        return text.contains("landlord") ||
                text.contains("tenant") ||
                text.contains("rent") ||
                text.contains("rental") ||
                text.contains("deposit") ||
                text.contains("lease")
    }

    private fun isEmploymentMatter(
        context: CaseContext
    ): Boolean {

        val text = context.buildSummary()
            .lowercase()

        return text.contains("job") ||
                text.contains("employer") ||
                text.contains("employee") ||
                text.contains("salary") ||
                text.contains("workplace") ||
                text.contains("termination") ||
                text.contains("fired")
    }

    private fun isPoliceMatter(
        context: CaseContext
    ): Boolean {

        val text = context.buildSummary()
            .lowercase()

        return text.contains("police") ||
                text.contains("fir") ||
                text.contains("complaint") ||
                text.contains("crime") ||
                text.contains("stolen") ||
                text.contains("threat")
    }

    private fun isPropertyMatter(
        context: CaseContext
    ): Boolean {

        val text = context.buildSummary()
            .lowercase()

        return text.contains("property") ||
                text.contains("land") ||
                text.contains("house") ||
                text.contains("plot") ||
                text.contains("boundary")
    }

    private fun isFamilyMatter(
        context: CaseContext
    ): Boolean {

        val text = context.buildSummary()
            .lowercase()

        return text.contains("husband") ||
                text.contains("wife") ||
                text.contains("divorce") ||
                text.contains("marriage") ||
                text.contains("child") ||
                text.contains("family")
    }
}