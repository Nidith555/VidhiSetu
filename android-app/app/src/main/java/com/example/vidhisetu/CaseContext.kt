package com.example.vidhisetu

data class CaseContext(

    var initialProblem: String = "",

    var timeline: String = "",

    var peopleInvolved: String = "",

    var documents: String = "",

    var actionsTaken: String = "",

    var desiredOutcome: String = "",

    var additionalDetails: String = "",

    var questionCount: Int = 0,

    var language: String = "en",

    // Role-specific RAG policy; callers may pass "civilian", "student", or "lawyer".
    var role: String = "civilian"
) {

    fun buildSummary(): String {

        return """
            Initial Problem:
            $initialProblem
            
            Timeline:
            $timeline
            
            People / Organisations Involved:
            $peopleInvolved
            
            Documents / Evidence:
            $documents
            
            Actions Already Taken:
            $actionsTaken
            
            Desired Outcome:
            $desiredOutcome
            
            Additional Details:
            $additionalDetails
        """.trimIndent()
    }
}