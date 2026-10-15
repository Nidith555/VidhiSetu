package com.example.vidhisetu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityLawyerCaseResearchBinding

class LawyerCaseResearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLawyerCaseResearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLawyerCaseResearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        // Back
        binding.backButton.setOnClickListener {
            finish()
        }

        // Start Research
        binding.startResearchButton.setOnClickListener {
            performResearch()
        }

        // Clear
        binding.clearButton.setOnClickListener {
            clearResearch()
        }

        // Save
        binding.saveButton.setOnClickListener {
            saveResearch()
        }
    }

    private fun performResearch() {

        val caseTitle =
            binding.caseTitleInput.text.toString().trim()

        val legalIssue =
            binding.legalIssueInput.text.toString().trim()

        val facts =
            binding.factsInput.text.toString().trim()

        val researchArea =
            binding.researchAreaInput.text.toString().trim()

        if (caseTitle.isEmpty()) {
            binding.caseTitleInput.error =
                "Enter the case title"
            return
        }

        if (legalIssue.isEmpty()) {
            binding.legalIssueInput.error =
                "Enter the legal issue"
            return
        }

        if (facts.isEmpty()) {
            binding.factsInput.error =
                "Enter the relevant facts"
            return
        }

        if (researchArea.isEmpty()) {
            binding.researchAreaInput.error =
                "Enter the research area"
            return
        }

        binding.resultCard.visibility =
            android.view.View.VISIBLE

        binding.resultTitle.text =
            "Research Framework"

        binding.resultText.text = buildString {

            append("CASE\n")
            append(caseTitle)

            append("\n\nLEGAL ISSUE\n")
            append(legalIssue)

            append("\n\nRESEARCH AREA\n")
            append(researchArea)

            append("\n\nRELEVANT FACTS\n")
            append(facts)

            append("\n\nRESEARCH CHECKLIST\n")
            append("• Identify applicable statutory provisions\n")
            append("• Check relevant constitutional provisions\n")
            append("• Search for related judicial decisions\n")
            append("• Identify supporting and opposing authorities\n")
            append("• Verify the current legal position\n")
            append("• Record citations and source references\n")
        }

        Toast.makeText(
            this,
            "Research framework generated",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun saveResearch() {

        val caseTitle =
            binding.caseTitleInput.text.toString().trim()

        if (caseTitle.isEmpty()) {
            Toast.makeText(
                this,
                "Enter a case title first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val preferences =
            getSharedPreferences(
                "lawyer_research",
                MODE_PRIVATE
            )

        preferences.edit()
            .putString(
                "last_case_title",
                caseTitle
            )
            .putString(
                "last_legal_issue",
                binding.legalIssueInput.text.toString()
            )
            .putString(
                "last_facts",
                binding.factsInput.text.toString()
            )
            .putString(
                "last_research_area",
                binding.researchAreaInput.text.toString()
            )
            .apply()

        Toast.makeText(
            this,
            "Research saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun clearResearch() {

        binding.caseTitleInput.text?.clear()
        binding.legalIssueInput.text?.clear()
        binding.factsInput.text?.clear()
        binding.researchAreaInput.text?.clear()

        binding.resultCard.visibility =
            android.view.View.GONE
    }
}