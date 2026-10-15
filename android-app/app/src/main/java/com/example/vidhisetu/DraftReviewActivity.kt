package com.example.vidhisetu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityDraftReviewBinding

class DraftReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDraftReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDraftReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.reviewButton.setOnClickListener {
            reviewDraft()
        }

        binding.clearButton.setOnClickListener {
            clearDraft()
        }
    }

    private fun reviewDraft() {

        val documentTitle =
            binding.documentTitleInput.text.toString().trim()

        val draftText =
            binding.draftInput.text.toString().trim()

        if (documentTitle.isEmpty()) {
            binding.documentTitleInput.error =
                "Enter document title"
            return
        }

        if (draftText.isEmpty()) {
            binding.draftInput.error =
                "Enter the draft"
            return
        }

        binding.resultCard.visibility = View.VISIBLE

        val wordCount =
            draftText.split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .size

        binding.resultTitle.text =
            "Draft Review"

        binding.resultText.text = buildString {

            append("DOCUMENT\n")
            append(documentTitle)

            append("\n\nWORD COUNT\n")
            append(wordCount)

            append("\n\nREVIEW CHECKLIST\n")
            append("✓ Document title identified\n")
            append("✓ Draft content received\n")
            append("✓ Paragraph structure can be reviewed\n")
            append("✓ Defined terms should be checked for consistency\n")
            append("✓ Dates, names and references should be verified\n")
            append("✓ Citations should be checked against authoritative sources\n")
            append("✓ Ambiguous or incomplete clauses should be reviewed\n")
            append("✓ Formatting should be checked before finalization\n")

            append("\n\nIMPORTANT\n")
            append("This is a preliminary drafting checklist. ")
            append("It does not determine whether the document is legally correct.")
        }

        Toast.makeText(
            this,
            "Draft review completed",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun clearDraft() {

        binding.documentTitleInput.text?.clear()
        binding.draftInput.text?.clear()

        binding.resultCard.visibility =
            View.GONE
    }
}