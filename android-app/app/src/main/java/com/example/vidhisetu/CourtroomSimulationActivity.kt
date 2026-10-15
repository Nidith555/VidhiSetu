package com.example.vidhisetu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityCourtroomSimulationBinding

class CourtroomSimulationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourtroomSimulationBinding

    private var currentStage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityCourtroomSimulationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
        showStage(0)
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.startButton.setOnClickListener {
            showStage(1)
        }

        binding.submitOpeningButton.setOnClickListener {
            submitOpening()
        }

        binding.submitJudgeResponseButton.setOnClickListener {
            submitJudgeResponse()
        }

        binding.submitObjectionButton.setOnClickListener {
            submitObjection()
        }

        binding.submitClosingButton.setOnClickListener {
            submitClosing()
        }

        binding.restartButton.setOnClickListener {
            restartSimulation()
        }
    }

    private fun showStage(stage: Int) {

        currentStage = stage

        binding.introductionLayout.visibility =
            android.view.View.GONE

        binding.openingLayout.visibility =
            android.view.View.GONE

        binding.judgeQuestionLayout.visibility =
            android.view.View.GONE

        binding.objectionLayout.visibility =
            android.view.View.GONE

        binding.closingLayout.visibility =
            android.view.View.GONE

        binding.resultLayout.visibility =
            android.view.View.GONE

        when (stage) {

            0 -> {
                binding.introductionLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Courtroom Simulation"

                binding.startButton.visibility =
                    android.view.View.VISIBLE
            }

            1 -> {
                binding.openingLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 1 of 5"
            }

            2 -> {
                binding.judgeQuestionLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 2 of 5"
            }

            3 -> {
                binding.objectionLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 3 of 5"
            }

            4 -> {
                binding.closingLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 4 of 5"
            }

            5 -> {
                binding.resultLayout.visibility =
                    android.view.View.VISIBLE

                binding.stageText.text =
                    "Stage 5 of 5"
            }
        }
    }

    private fun submitOpening() {

        val answer =
            binding.openingInput.text.toString().trim()

        if (answer.isBlank()) {
            binding.openingInput.error =
                "Enter your opening submission"
            binding.openingInput.requestFocus()
            return
        }

        binding.openingFeedback.text =
            evaluateText(
                answer,
                "Opening submission"
            )

        binding.openingFeedback.visibility =
            android.view.View.VISIBLE

        binding.nextAfterOpeningButton.visibility =
            android.view.View.VISIBLE

        binding.nextAfterOpeningButton.setOnClickListener {
            showStage(2)
        }
    }

    private fun submitJudgeResponse() {

        val answer =
            binding.judgeResponseInput.text.toString().trim()

        if (answer.isBlank()) {
            binding.judgeResponseInput.error =
                "Respond to the judge"
            binding.judgeResponseInput.requestFocus()
            return
        }

        binding.judgeFeedback.text =
            evaluateText(
                answer,
                "Response to the judge"
            )

        binding.judgeFeedback.visibility =
            android.view.View.VISIBLE

        binding.nextAfterJudgeButton.visibility =
            android.view.View.VISIBLE

        binding.nextAfterJudgeButton.setOnClickListener {
            showStage(3)
        }
    }

    private fun submitObjection() {

        val answer =
            binding.objectionInput.text.toString().trim()

        if (answer.isBlank()) {
            binding.objectionInput.error =
                "Enter your response"
            binding.objectionInput.requestFocus()
            return
        }

        binding.objectionFeedback.text =
            evaluateText(
                answer,
                "Objection / response"
            )

        binding.objectionFeedback.visibility =
            android.view.View.VISIBLE

        binding.nextAfterObjectionButton.visibility =
            android.view.View.VISIBLE

        binding.nextAfterObjectionButton.setOnClickListener {
            showStage(4)
        }
    }

    private fun submitClosing() {

        val answer =
            binding.closingInput.text.toString().trim()

        if (answer.isBlank()) {
            binding.closingInput.error =
                "Enter your closing submission"
            binding.closingInput.requestFocus()
            return
        }

        val score =
            calculateFinalScore()

        binding.finalScoreText.text =
            "$score / 100"

        binding.finalFeedbackText.text =
            buildFinalFeedback(score)

        showStage(5)
    }

    private fun evaluateText(
        text: String,
        sectionName: String
    ): String {

        val lower =
            text.lowercase()

        val words =
            text.split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .size

        var score = 0

        if (words >= 30) {
            score += 25
        } else {
            score += 12
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
            lower.contains("court") ||
            lower.contains("respectfully") ||
            lower.contains("honourable")
        ) {
            score += 25
        } else {
            score += 10
        }

        return """
            $sectionName review
            
            Practice score: $score / 100
            
            Word count: $words
            
            ${
            if (score >= 80) {
                "The response is well developed and uses a clear legal structure."
            } else if (score >= 60) {
                "The response has a reasonable foundation but needs stronger legal reasoning."
            } else {
                "Develop the response further by connecting facts, law and reasoning."
            }
        }
        """.trimIndent()
    }

    private fun calculateFinalScore(): Int {

        val opening =
            binding.openingInput.text.toString()

        val judgeResponse =
            binding.judgeResponseInput.text.toString()

        val objection =
            binding.objectionInput.text.toString()

        val closing =
            binding.closingInput.text.toString()

        val totalWords =
            listOf(
                opening,
                judgeResponse,
                objection,
                closing
            ).sumOf {
                it.split("\\s+".toRegex())
                    .filter { word -> word.isNotBlank() }
                    .size
            }

        var score = 40

        if (totalWords >= 150) {
            score += 20
        }

        val combined =
            "$opening $judgeResponse $objection $closing"
                .lowercase()

        if (
            combined.contains("section") ||
            combined.contains("article") ||
            combined.contains("bns") ||
            combined.contains("bnss") ||
            combined.contains("bsa")
        ) {
            score += 15
        }

        if (
            combined.contains("because") ||
            combined.contains("therefore") ||
            combined.contains("evidence") ||
            combined.contains("fact")
        ) {
            score += 15
        }

        if (
            combined.contains("however") ||
            combined.contains("opponent") ||
            combined.contains("objection") ||
            combined.contains("contrary")
        ) {
            score += 10
        }

        return score.coerceAtMost(100)
    }

    private fun buildFinalFeedback(
        score: Int
    ): String {

        return when {

            score >= 85 -> {
                """
                Strong courtroom practice session.
                
                You demonstrated structured submissions, legal reasoning and responsiveness during the simulated proceeding.
                
                Continue working on precise authorities, factual application and concise courtroom responses.
                """.trimIndent()
            }

            score >= 70 -> {
                """
                Good courtroom practice session.
                
                Your responses show a developing understanding of courtroom advocacy.
                
                Focus on stronger statutory support, clearer issue identification and more direct responses to judicial questions.
                """.trimIndent()
            }

            score >= 50 -> {
                """
                Developing courtroom performance.
                
                Work on organising each submission around the legal issue, applicable rule, facts and conclusion.
                
                Practice answering judicial questions directly and supporting your position with relevant law.
                """.trimIndent()
            }

            else -> {
                """
                More practice is recommended.
                
                Build your submissions step by step:
                
                1. Identify the legal issue.
                2. State the applicable law.
                3. Apply the law to the facts.
                4. Address the opposing position.
                5. Give a clear conclusion.
                """.trimIndent()
            }
        }
    }

    private fun restartSimulation() {

        binding.openingInput.text?.clear()
        binding.judgeResponseInput.text?.clear()
        binding.objectionInput.text?.clear()
        binding.closingInput.text?.clear()

        binding.openingFeedback.visibility =
            android.view.View.GONE

        binding.judgeFeedback.visibility =
            android.view.View.GONE

        binding.objectionFeedback.visibility =
            android.view.View.GONE

        binding.nextAfterOpeningButton.visibility =
            android.view.View.GONE

        binding.nextAfterJudgeButton.visibility =
            android.view.View.GONE

        binding.nextAfterObjectionButton.visibility =
            android.view.View.GONE

        binding.finalScoreText.text = "—"

        showStage(0)

        Toast.makeText(
            this,
            "New courtroom simulation started",
            Toast.LENGTH_SHORT
        ).show()
    }
}