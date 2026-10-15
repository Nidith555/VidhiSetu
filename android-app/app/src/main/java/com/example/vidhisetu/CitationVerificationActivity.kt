package com.example.vidhisetu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityCitationVerificationBinding

class CitationVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitationVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCitationVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.verifyButton.setOnClickListener {
            verifyCitation()
        }

        binding.clearButton.setOnClickListener {
            clearFields()
        }
    }

    private fun verifyCitation() {

        val caseName = binding.caseNameInput.text.toString().trim()
        val citation = binding.citationInput.text.toString().trim()
        val court = binding.courtInput.text.toString().trim()
        val year = binding.yearInput.text.toString().trim()

        if (caseName.isEmpty()) {
            binding.caseNameInput.error = "Enter case name"
            return
        }

        if (citation.isEmpty()) {
            binding.citationInput.error = "Enter citation"
            return
        }

        if (court.isEmpty()) {
            binding.courtInput.error = "Enter court"
            return
        }

        if (year.isEmpty()) {
            binding.yearInput.error = "Enter year"
            return
        }

        binding.resultCard.visibility = View.VISIBLE

        binding.resultStatus.text = "Citation Added for Verification"

        binding.resultText.text = buildString {
            append("CASE\n")
            append(caseName)

            append("\n\nCITATION\n")
            append(citation)

            append("\n\nCOURT\n")
            append(court)

            append("\n\nYEAR\n")
            append(year)

            append("\n\nVERIFICATION CHECKLIST\n")
            append("• Verify case name\n")
            append("• Verify court / jurisdiction\n")
            append("• Verify year and citation format\n")
            append("• Check whether the cited authority exists\n")
            append("• Confirm the citation against an authoritative source\n")
            append("• Check whether the authority is current and relevant\n")
        }

        Toast.makeText(
            this,
            "Citation verification checklist generated",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun clearFields() {

        binding.caseNameInput.text?.clear()
        binding.citationInput.text?.clear()
        binding.courtInput.text?.clear()
        binding.yearInput.text?.clear()

        binding.resultCard.visibility = View.GONE
    }
}