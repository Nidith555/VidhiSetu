package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.vidhisetu.databinding.ActivityDocumentTemplatesBinding

class DocumentTemplatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentTemplatesBinding

    private val documentTypes = arrayOf(
        "Complaint",
        "Legal Notice",
        "Representation"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDocumentTemplatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDocumentSelector()
        setupClicks()
    }

    private fun setupDocumentSelector() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            documentTypes
        )

        binding.documentTypeSpinner.adapter = adapter
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.generateButton.setOnClickListener {
            generateDocument()
        }

        binding.copyButton.setOnClickListener {
            copyDraft()
        }

        binding.shareButton.setOnClickListener {
            shareDraft()
        }
    }

    private fun generateDocument() {
        val complainant = binding.nameInput.text.toString().trim()
        val respondent = binding.addressInput.text.toString().trim()
        val incidentPlace = binding.subjectInput.text.toString().trim()
        val factsText = binding.detailsInput.text.toString().trim()
        val incidentDate = binding.incidentDateInput.text.toString().trim()
        val requestedRelief = binding.requestedReliefInput.text.toString().trim()
        val type = when (binding.documentTypeSpinner.selectedItemPosition) {
            1 -> "legal_notice"
            2 -> "representation"
            else -> "complaint"
        }

        if (complainant.isBlank()) {
            binding.nameInput.error = "Enter your name"
            binding.nameInput.requestFocus()
            return
        }
        if (respondent.isBlank()) {
            binding.addressInput.error = "Enter the respondent or authority"
            binding.addressInput.requestFocus()
            return
        }
        if (factsText.isBlank()) {
            binding.detailsInput.error = "Describe the facts"
            binding.detailsInput.requestFocus()
            return
        }
        if (requestedRelief.isBlank()) {
            binding.requestedReliefInput.error = "Describe the outcome you are requesting"
            binding.requestedReliefInput.requestFocus()
            return
        }
        if (type == "complaint" && incidentDate.isBlank()) {
            binding.incidentDateInput.error = "Enter the incident date for a complaint"
            binding.incidentDateInput.requestFocus()
            return
        }
        if (type == "complaint" && incidentPlace.isBlank()) {
            binding.subjectInput.error = "Enter the incident place for a complaint"
            binding.subjectInput.requestFocus()
            return
        }

        binding.generateButton.isEnabled = false
        binding.previewCard.visibility = android.view.View.VISIBLE
        binding.previewTitle.text = "Preparing a review-only draft"
        binding.previewText.text = "Checking the required facts and retrieved legal sources..."
        binding.copyButton.visibility = android.view.View.GONE
        binding.shareButton.visibility = android.view.View.GONE

        lifecycleScope.launch {
            try {
                val role = intent.getStringExtra("role")
                    ?.lowercase(java.util.Locale.ROOT)
                    ?.takeIf { it in setOf("civilian", "student", "lawyer") }
                    ?: "civilian"
                val result = LegalDraftClient().createDraft(
                    documentType = type,
                    complainant = complainant,
                    respondent = respondent,
                    incidentDate = incidentDate,
                    incidentPlace = incidentPlace,
                    facts = listOf(factsText),
                    requestedRelief = requestedRelief,
                    language = AppLanguage.getCode(this@DocumentTemplatesActivity),
                    role = role
                )

                binding.previewTitle.text = result.title.ifBlank {
                    when (result.status) {
                        "needs_information" -> "More information needed"
                        "rejected" -> "Draft withheld"
                        else -> "Draft response"
                    }
                }

                val content = buildString {
                    when (result.status) {
                        "ready" -> {
                            append(result.draft)
                            if (result.verifiedCitations.isNotEmpty()) {
                                append("\n\nVerified citations\n• ")
                                append(result.verifiedCitations.joinToString("\n• "))
                            }
                        }
                        "needs_information" -> {
                            append("Please provide the following information before a draft can be prepared:")
                            (result.missingInformation + result.questions).distinct().forEach {
                                append("\n• ")
                                append(it)
                            }
                        }
                        else -> {
                            append(result.message.ifBlank {
                                "The backend withheld the draft because its legal support could not be verified."
                            })
                        }
                    }
                    if (result.warning.isNotBlank()) {
                        append("\n\n")
                        append(result.warning)
                    }
                }
                binding.previewText.text = content
                val draftReady = result.status == "ready" && result.draft.isNotBlank()
                binding.copyButton.visibility = if (draftReady) android.view.View.VISIBLE else android.view.View.GONE
                binding.shareButton.visibility = if (draftReady) android.view.View.VISIBLE else android.view.View.GONE
                Toast.makeText(
                    this@DocumentTemplatesActivity,
                    if (draftReady) "Review-only draft is ready" else "Draft needs attention",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (exception: Exception) {
                binding.previewTitle.text = "Draft service unavailable"
                binding.previewText.text = exception.message
                    ?: "The legal drafting service could not be reached. Please try again."
                binding.copyButton.visibility = android.view.View.GONE
                binding.shareButton.visibility = android.view.View.GONE
                Toast.makeText(
                    this@DocumentTemplatesActivity,
                    binding.previewText.text,
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.generateButton.isEnabled = true
            }
        }
    }

    private fun copyDraft() {

        val draft =
            binding.previewText.text.toString()

        if (draft.isBlank()) {
            Toast.makeText(
                this,
                "Generate a draft first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val clipboard =
            getSystemService(CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager

        val clip =
            android.content.ClipData.newPlainText(
                "VidhiSetu Legal Draft",
                draft
            )

        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            this,
            "Draft copied",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun shareDraft() {

        val draft =
            binding.previewText.text.toString()

        if (draft.isBlank()) {
            Toast.makeText(
                this,
                "Generate a draft first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(Intent.ACTION_SEND).apply {

                type = "text/plain"

                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "VidhiSetu Legal Draft"
                )

                putExtra(
                    Intent.EXTRA_TEXT,
                    draft
                )
            }

        startActivity(
            Intent.createChooser(
                intent,
                "Share Draft"
            )
        )
    }
}