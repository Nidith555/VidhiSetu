package com.example.vidhisetu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityArgumentPerformanceBinding

class ArgumentPerformanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArgumentPerformanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArgumentPerformanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.evaluateButton.setOnClickListener {
            evaluateArgument()
        }

        binding.clearButton.setOnClickListener {
            clearArgument()
        }
    }

    private fun evaluateArgument() {

        val argument = binding.argumentInput.text.toString().trim()

        if (argument.isBlank()) {
            binding.argumentInput.error = "Enter your argument first"
            binding.argumentInput.requestFocus()
            return
        }

        val wordCount = argument
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .size

        val lowerArgument = argument.lowercase()

        var structureScore = 0
        var legalIssueScore = 0
        var lawScore = 0
        var reasoningScore = 0
        var counterArgumentScore = 0

        // Structure
        if (
            lowerArgument.contains("because") ||
            lowerArgument.contains("therefore") ||
            lowerArgument.contains("however") ||
            lowerArgument.contains("firstly") ||
            lowerArgument.contains("secondly") ||
            lowerArgument.contains("finally")
        ) {
            structureScore = 20
        } else if (wordCount >= 50) {
            structureScore = 15
        } else {
            structureScore = 8
        }

        // Legal issue identification
        if (
            lowerArgument.contains("issue") ||
            lowerArgument.contains("question") ||
            lowerArgument.contains("whether")
        ) {
            legalIssueScore = 20
        } else {
            legalIssueScore = 10
        }

        // Legal provisions
        if (
            lowerArgument.contains("section") ||
            lowerArgument.contains("article") ||
            lowerArgument.contains("bns") ||
            lowerArgument.contains("bnss") ||
            lowerArgument.contains("bsa") ||
            lowerArgument.contains("act")
        ) {
            lawScore = 20
        } else {
            lawScore = 8
        }

        // Reasoning
        if (
            lowerArgument.contains("because") ||
            lowerArgument.contains("therefore") ||
            lowerArgument.contains("evidence") ||
            lowerArgument.contains("fact") ||
            lowerArgument.contains("proves") ||
            lowerArgument.contains("shows")
        ) {
            reasoningScore = 20
        } else {
            reasoningScore = 10
        }

        // Counterargument
        if (
            lowerArgument.contains("however") ||
            lowerArgument.contains("opponent") ||
            lowerArgument.contains("counter") ||
            lowerArgument.contains("contrary") ||
            lowerArgument.contains("defence") ||
            lowerArgument.contains("defense")
        ) {
            counterArgumentScore = 20
        } else {
            counterArgumentScore = 8
        }

        val totalScore =
            structureScore +
                    legalIssueScore +
                    lawScore +
                    reasoningScore +
                    counterArgumentScore

        val feedback = buildFeedback(
            totalScore,
            structureScore,
            legalIssueScore,
            lawScore,
            reasoningScore,
            counterArgumentScore,
            wordCount
        )

        binding.scoreText.text = "$totalScore / 100"

        binding.structureScore.text =
            "Structure: $structureScore / 20"

        binding.issueScore.text =
            "Legal Issue: $legalIssueScore / 20"

        binding.lawScore.text =
            "Legal Provisions: $lawScore / 20"

        binding.reasoningScore.text =
            "Reasoning: $reasoningScore / 20"

        binding.counterArgumentScore.text =
            "Counterarguments: $counterArgumentScore / 20"

        binding.feedbackText.text = feedback

        binding.resultCard.visibility =
            android.view.View.VISIBLE

        Toast.makeText(
            this,
            "Argument evaluated",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun buildFeedback(
        total: Int,
        structure: Int,
        issue: Int,
        law: Int,
        reasoning: Int,
        counter: Int,
        wordCount: Int
    ): String {

        val feedback = StringBuilder()

        feedback.append("Performance Review\n\n")

        if (total >= 80) {
            feedback.append(
                "Your argument has a strong overall structure. "
            )
        } else if (total >= 60) {
            feedback.append(
                "Your argument has a reasonable foundation but can be strengthened. "
            )
        } else {
            feedback.append(
                "Your argument needs further development before it is presentation-ready. "
            )
        }

        feedback.append("\n\n")

        if (structure < 15) {
            feedback.append(
                "• Structure: Clearly separate your issue, rule, application and conclusion.\n"
            )
        } else {
            feedback.append(
                "• Structure: The argument shows useful logical connectors.\n"
            )
        }

        if (issue < 15) {
            feedback.append(
                "• Legal Issue: State the precise legal question or issue.\n"
            )
        } else {
            feedback.append(
                "• Legal Issue: The argument identifies a legal question.\n"
            )
        }

        if (law < 15) {
            feedback.append(
                "• Legal Provisions: Support the argument with the relevant statutory provision or constitutional Article.\n"
            )
        } else {
            feedback.append(
                "• Legal Provisions: The argument refers to legal provisions.\n"
            )
        }

        if (reasoning < 15) {
            feedback.append(
                "• Reasoning: Connect the facts to the legal rule instead of only stating the conclusion.\n"
            )
        } else {
            feedback.append(
                "• Reasoning: The argument contains reasoning connecting facts and conclusions.\n"
            )
        }

        if (counter < 15) {
            feedback.append(
                "• Counterarguments: Address what the opposing side could argue and explain why your position remains stronger.\n"
            )
        } else {
            feedback.append(
                "• Counterarguments: The argument acknowledges an opposing position.\n"
            )
        }

        feedback.append("\n")
        feedback.append("Word count: $wordCount")

        return feedback.toString()
    }

    private fun clearArgument() {

        binding.argumentInput.text?.clear()

        binding.resultCard.visibility =
            android.view.View.GONE

        binding.scoreText.text = "—"

        Toast.makeText(
            this,
            "Argument cleared",
            Toast.LENGTH_SHORT
        ).show()
    }
}