package com.example.vidhisetu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityCaseAnalysisLabBinding

class CaseAnalysisLabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaseAnalysisLabBinding

    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCaseAnalysisLabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
        showCase()
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

    private fun showCase() {

        binding.caseTitle.text = "Case 01 • Constitutional Law"

        binding.caseFacts.text = """
            A State Government introduces a law that places certain restrictions
            on the freedom of speech of citizens.

            A citizen challenges the law before the court, arguing that the
            restrictions interfere with the fundamental right to freedom of
            speech and expression.

            The State argues that the restrictions are necessary for public
            order and security.

            Analyse the dispute and identify the important constitutional
            issues involved.
        """.trimIndent()

        binding.caseQuestion.text = """
            Analyse the case from a legal perspective.

            Identify:
            • The main legal issue
            • The fundamental right involved
            • The relevant constitutional principle
            • The arguments of both sides
            • Your reasoned conclusion
        """.trimIndent()
    }

    private fun evaluateAnalysis() {

        val issues = binding.issuesInput.text.toString().trim()
        val laws = binding.lawsInput.text.toString().trim()
        val arguments = binding.argumentsInput.text.toString().trim()
        val conclusion = binding.conclusionInput.text.toString().trim()

        if (
            issues.isEmpty() ||
            laws.isEmpty() ||
            arguments.isEmpty() ||
            conclusion.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Please complete all analysis sections.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        score = 0

        // Legal issue identification
        if (
            issues.contains("speech", true) ||
            issues.contains("freedom", true) ||
            issues.contains("article", true) ||
            issues.contains("fundamental", true)
        ) {
            score += 25
        }

        // Applicable law
        if (
            laws.contains("19", true) ||
            laws.contains("article", true) ||
            laws.contains("constitution", true) ||
            laws.contains("reasonable restriction", true)
        ) {
            score += 25
        }

        // Arguments
        if (arguments.length >= 80) {
            score += 25
        } else if (arguments.length >= 40) {
            score += 15
        } else {
            score += 5
        }

        // Conclusion
        if (conclusion.length >= 80) {
            score += 25
        } else if (conclusion.length >= 40) {
            score += 15
        } else {
            score += 5
        }

        showResult()
    }

    private fun showResult() {

        binding.resultCard.visibility = android.view.View.VISIBLE

        binding.scoreText.text = "$score / 100"

        val feedback = when {
            score >= 80 -> {
                "Strong case analysis. You identified the major legal issue, connected it with the relevant constitutional framework, considered the competing arguments, and provided a reasoned conclusion."
            }

            score >= 60 -> {
                "Good analysis. Your basic legal reasoning is present, but the issues, applicable law, arguments, or conclusion could be developed further."
            }

            score >= 40 -> {
                "Developing analysis. Try to identify the precise legal issue first, then connect it to the relevant constitutional provision and explain both sides."
            }

            else -> {
                "Your analysis needs more development. Focus on identifying the legal issue, applicable law, arguments of both parties, and a reasoned conclusion."
            }
        }

        binding.feedbackText.text = feedback
    }

    private fun clearAnalysis() {

        binding.issuesInput.text?.clear()
        binding.lawsInput.text?.clear()
        binding.argumentsInput.text?.clear()
        binding.conclusionInput.text?.clear()

        binding.resultCard.visibility = android.view.View.GONE

        score = 0
    }

    private fun restartLab() {

        clearAnalysis()

        binding.studentScrollView.smoothScrollTo(
            0,
            0
        )

        Toast.makeText(
            this,
            "Case Analysis Lab restarted.",
            Toast.LENGTH_SHORT
        ).show()
    }
}