package com.example.vidhisetu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityMootCourtSimulationBinding

class MootCourtSimulationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMootCourtSimulationBinding

    private var selectedSide = ""
    private var currentStage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityMootCourtSimulationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
        showStage(0)
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.petitionerButton.setOnClickListener {
            selectedSide = "Petitioner"
            updateSideSelection()
        }

        binding.respondentButton.setOnClickListener {
            selectedSide = "Respondent"
            updateSideSelection()
        }

        binding.nextButton.setOnClickListener {
            moveToNextStage()
        }

        binding.submitArgumentButton.setOnClickListener {
            evaluateArgument()
        }

        binding.submitResponseButton.setOnClickListener {
            evaluateResponse()
        }

        binding.restartButton.setOnClickListener {
            restartSimulation()
        }
    }

    private fun updateSideSelection() {

        binding.selectedSideText.text =
            "Selected side: $selectedSide"

        binding.selectedSideText.visibility =
            android.view.View.VISIBLE

        binding.nextButton.isEnabled = true
    }

    private fun showStage(stage: Int) {

        currentStage = stage

        binding.sideSelectionLayout.visibility =
            android.view.View.GONE

        binding.caseBriefLayout.visibility =
            android.view.View.GONE

        binding.argumentLayout.visibility =
            android.view.View.GONE

        binding.responseLayout.visibility =
            android.view.View.GONE

        binding.resultLayout.visibility =
            android.view.View.GONE

        when (stage) {

            0 -> {
                binding.sideSelectionLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 1 of 5"

                binding.nextButton.visibility =
                    android.view.View.VISIBLE

                binding.nextButton.text =
                    "Continue"
            }

            1 -> {
                binding.caseBriefLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 2 of 5"

                binding.nextButton.visibility =
                    android.view.View.VISIBLE

                binding.nextButton.text =
                    "Start Argument"
            }

            2 -> {
                binding.argumentLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 3 of 5"

                binding.nextButton.visibility =
                    android.view.View.GONE
            }

            3 -> {
                binding.responseLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 4 of 5"

                binding.nextButton.visibility =
                    android.view.View.GONE
            }

            4 -> {
                binding.resultLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 5 of 5"

                binding.nextButton.visibility =
                    android.view.View.GONE
            }
        }
    }

    private fun moveToNextStage() {

        if (currentStage == 0) {

            if (selectedSide.isBlank()) {
                Toast.makeText(
                    this,
                    "Choose your side first",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            showStage(1)

        } else if (currentStage == 1) {

            showStage(2)
        }
    }

    private fun evaluateArgument() {

        val argument =
            binding.argumentInput.text.toString().trim()

        if (argument.isBlank()) {
            binding.argumentInput.error =
                "Present your argument first"
            binding.argumentInput.requestFocus()
            return
        }

        val wordCount =
            argument.split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .size

        val lower =
            argument.lowercase()

        var score = 0

        if (wordCount >= 30) {
            score += 20
        } else {
            score += 10
        }

        if (
            lower.contains("section") ||
            lower.contains("article") ||
            lower.contains("bns") ||
            lower.contains("bnss") ||
            lower.contains("bsa") ||
            lower.contains("act")
        ) {
            score += 25
        } else {
            score += 10
        }

        if (
            lower.contains("because") ||
            lower.contains("therefore") ||
            lower.contains("evidence") ||
            lower.contains("fact")
        ) {
            score += 25
        } else {
            score += 10
        }

        if (
            lower.contains("issue") ||
            lower.contains("whether")
        ) {
            score += 15
        } else {
            score += 5
        }

        if (
            lower.contains("court") ||
            lower.contains("honourable") ||
            lower.contains("respectfully")
        ) {
            score += 15
        } else {
            score += 5
        }

        binding.argumentScoreText.text =
            "$score / 100"

        binding.argumentFeedbackText.text =
            buildArgumentFeedback(
                score,
                wordCount,
                lower
            )

        showStage(3)
    }

    private fun buildArgumentFeedback(
        score: Int,
        wordCount: Int,
        argument: String
    ): String {

        val feedback =
            StringBuilder()

        feedback.append(
            "Your submission contains approximately $wordCount words.\n\n"
        )

        if (score >= 80) {
            feedback.append(
                "Strong practice submission. "
            )
        } else if (score >= 60) {
            feedback.append(
                "Good foundation, but the submission can be developed further. "
            )
        } else {
            feedback.append(
                "The submission needs more legal structure and supporting reasoning. "
            )
        }

        feedback.append("\n\n")

        if (
            argument.contains("section") ||
            argument.contains("article") ||
            argument.contains("act") ||
            argument.contains("bns") ||
            argument.contains("bnss") ||
            argument.contains("bsa")
        ) {
            feedback.append(
                "✓ Legal provisions referenced.\n"
            )
        } else {
            feedback.append(
                "• Consider identifying the relevant statutory provision or constitutional Article.\n"
            )
        }

        if (
            argument.contains("because") ||
            argument.contains("therefore") ||
            argument.contains("evidence")
        ) {
            feedback.append(
                "✓ Reasoning connects the position with supporting material.\n"
            )
        } else {
            feedback.append(
                "• Explain why the facts support your legal position.\n"
            )
        }

        if (
            argument.contains("issue") ||
            argument.contains("whether")
        ) {
            feedback.append(
                "✓ A legal issue is identified.\n"
            )
        } else {
            feedback.append(
                "• State the precise legal issue before presenting the conclusion.\n"
            )
        }

        return feedback.toString()
    }

    private fun evaluateResponse() {

        val response =
            binding.responseInput.text.toString().trim()

        if (response.isBlank()) {
            binding.responseInput.error =
                "Enter your response"
            binding.responseInput.requestFocus()
            return
        }

        val lower =
            response.lowercase()

        var responseScore = 0

        if (response.split("\\s+".toRegex()).size >= 25) {
            responseScore += 25
        } else {
            responseScore += 12
        }

        if (
            lower.contains("however") ||
            lower.contains("but") ||
            lower.contains("contrary") ||
            lower.contains("opponent")
        ) {
            responseScore += 25
        } else {
            responseScore += 10
        }

        if (
            lower.contains("section") ||
            lower.contains("article") ||
            lower.contains("act") ||
            lower.contains("bns") ||
            lower.contains("bnss") ||
            lower.contains("bsa")
        ) {
            responseScore += 25
        } else {
            responseScore += 10
        }

        if (
            lower.contains("because") ||
            lower.contains("therefore") ||
            lower.contains("evidence")
        ) {
            responseScore += 25
        } else {
            responseScore += 10
        }

        binding.responseScoreText.text =
            "$responseScore / 100"

        binding.finalFeedbackText.text =
            buildResponseFeedback(responseScore)

        showStage(4)
    }

    private fun buildResponseFeedback(
        score: Int
    ): String {

        return if (score >= 80) {
            """
            Strong rebuttal practice.
            
            You addressed the opposing position and included legal reasoning. Continue improving the precision of your authorities and factual application.
            """.trimIndent()

        } else if (score >= 60) {
            """
            Reasonable rebuttal.
            
            Try to directly answer the opponent's strongest point and connect your response to the applicable legal provision and facts.
            """.trimIndent()

        } else {
            """
            The rebuttal needs further development.
            
            Identify the opponent's specific contention, explain why it is incorrect or distinguishable, and support your response with relevant law and facts.
            """.trimIndent()
        }
    }

    private fun restartSimulation() {

        selectedSide = ""
        currentStage = 0

        binding.selectedSideText.visibility =
            android.view.View.GONE

        binding.argumentInput.text?.clear()
        binding.responseInput.text?.clear()

        binding.argumentScoreText.text = "—"
        binding.responseScoreText.text = "—"

        showStage(0)
    }
}