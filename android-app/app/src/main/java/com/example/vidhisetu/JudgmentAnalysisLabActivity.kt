package com.example.vidhisetu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityJudgmentAnalysisLabBinding

class JudgmentAnalysisLabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJudgmentAnalysisLabBinding

    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJudgmentAnalysisLabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showJudgment()
        setupClicks()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.submitButton.setOnClickListener {
            evaluateAnalysis()
        }

        binding.clearButton.setOnClickListener {
            clearAnalysis()
        }

        binding.restartButton.setOnClickListener {
            restartLab()
        }
    }

    private fun showJudgment() {

        binding.judgmentTitle.text =
            "Judgment 01 • Constitutional Law"

        binding.judgmentBrief.text = """
            Case: Maneka Gandhi v. Union of India

            Court: Supreme Court of India

            Year: 1978

            Subject: Fundamental Rights and Personal Liberty

            Judgment Brief:

            The case concerned the procedure through which a passport
            could be impounded and the relationship between fundamental
            rights under the Constitution.

            The Court considered the relationship between Articles 14,
            19 and 21 and examined whether a procedure affecting personal
            liberty must satisfy constitutional requirements.

            Study the brief and analyse the legal reasoning used by the
            Court.
        """.trimIndent()

        binding.analysisTask.text = """
            Analyse the judgment and identify:

            • The central constitutional issue
            • The important Articles involved
            • The Court's reasoning
            • The principle established by the judgment
            • The final holding
            • The significance of the decision
        """.trimIndent()
    }

    private fun evaluateAnalysis() {

        val issue = binding.issueInput.text.toString().trim()
        val provisions = binding.provisionsInput.text.toString().trim()
        val reasoning = binding.reasoningInput.text.toString().trim()
        val holding = binding.holdingInput.text.toString().trim()
        val significance = binding.significanceInput.text.toString().trim()

        if (
            issue.isEmpty() ||
            provisions.isEmpty() ||
            reasoning.isEmpty() ||
            holding.isEmpty() ||
            significance.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Please complete all sections before submitting.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        score = 0

        // Central issue
        if (
            issue.contains("21", true) ||
            issue.contains("liberty", true) ||
            issue.contains("procedure", true) ||
            issue.contains("fundamental", true) ||
            issue.contains("passport", true)
        ) {
            score += 20
        }

        // Constitutional provisions
        if (
            provisions.contains("14", true) &&
            provisions.contains("19", true) &&
            provisions.contains("21", true)
        ) {
            score += 25
        } else if (
            provisions.contains("21", true) ||
            provisions.contains("14", true) ||
            provisions.contains("19", true)
        ) {
            score += 15
        }

        // Reasoning
        if (reasoning.length >= 120) {
            score += 20
        } else if (reasoning.length >= 60) {
            score += 15
        } else {
            score += 8
        }

        // Holding
        if (
            holding.contains("procedure", true) ||
            holding.contains("fair", true) ||
            holding.contains("just", true) ||
            holding.contains("reasonable", true) ||
            holding.contains("article 21", true)
        ) {
            score += 20
        } else {
            score += 8
        }

        // Significance
        if (significance.length >= 100) {
            score += 15
        } else if (significance.length >= 50) {
            score += 10
        } else {
            score += 5
        }

        showResult()
    }

    private fun showResult() {

        binding.resultCard.visibility = View.VISIBLE

        binding.scoreText.text = "$score / 100"

        binding.feedbackText.text = when {
            score >= 85 -> {
                "Strong judgment analysis. You identified the central constitutional issue, relevant provisions, judicial reasoning, holding and significance."
            }

            score >= 70 -> {
                "Good judgment analysis. Your understanding is developing well, but the Court's reasoning and the significance of the decision can be explained in greater depth."
            }

            score >= 50 -> {
                "Developing analysis. Focus on separating the legal issue, constitutional provisions, judicial reasoning and final holding."
            }

            else -> {
                "More analysis is needed. Start by identifying the precise legal issue, then identify the relevant constitutional provisions and explain how the Court reached its conclusion."
            }
        }
    }

    private fun clearAnalysis() {

        binding.issueInput.text?.clear()
        binding.provisionsInput.text?.clear()
        binding.reasoningInput.text?.clear()
        binding.holdingInput.text?.clear()
        binding.significanceInput.text?.clear()

        binding.resultCard.visibility = View.GONE

        score = 0
    }

    private fun restartLab() {

        clearAnalysis()

        binding.studentScrollView.smoothScrollTo(0, 0)

        Toast.makeText(
            this,
            "Judgment Analysis Lab restarted.",
            Toast.LENGTH_SHORT
        ).show()
    }
}