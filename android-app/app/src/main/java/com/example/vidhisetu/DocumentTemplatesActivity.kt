package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityDocumentTemplatesBinding

class DocumentTemplatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentTemplatesBinding

    private val documentTypes = arrayOf(
        "Police Complaint",
        "Consumer Complaint",
        "RTI Application",
        "Legal Notice",
        "Rental Agreement Request",
        "Leave & Permission Letter",
        "Affidavit",
        "General Application"
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

        val documentType =
            binding.documentTypeSpinner.selectedItem.toString()

        val name =
            binding.nameInput.text.toString().trim()

        val address =
            binding.addressInput.text.toString().trim()

        val subject =
            binding.subjectInput.text.toString().trim()

        val details =
            binding.detailsInput.text.toString().trim()

        if (name.isBlank()) {
            binding.nameInput.error = "Enter your name"
            binding.nameInput.requestFocus()
            return
        }

        if (address.isBlank()) {
            binding.addressInput.error = "Enter your address"
            binding.addressInput.requestFocus()
            return
        }

        if (subject.isBlank()) {
            binding.subjectInput.error = "Enter the subject"
            binding.subjectInput.requestFocus()
            return
        }

        if (details.isBlank()) {
            binding.detailsInput.error = "Enter the details"
            binding.detailsInput.requestFocus()
            return
        }

        val draft = createDraft(
            documentType = documentType,
            name = name,
            address = address,
            subject = subject,
            details = details
        )

        binding.previewTitle.text =
            documentType

        binding.previewText.text =
            draft

        binding.previewCard.visibility =
            android.view.View.VISIBLE

        binding.copyButton.visibility =
            android.view.View.VISIBLE

        binding.shareButton.visibility =
            android.view.View.VISIBLE

        Toast.makeText(
            this,
            "Draft generated successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun createDraft(
        documentType: String,
        name: String,
        address: String,
        subject: String,
        details: String
    ): String {

        return when (documentType) {

            "Police Complaint" -> {

                """
                TO
                The Station House Officer
                [Police Station Name]
                
                Subject: $subject
                
                Respected Sir/Madam,
                
                I, $name, residing at:
                
                $address
                
                would like to submit this complaint regarding the following matter:
                
                $details
                
                I request the concerned authority to kindly examine the matter
                and take appropriate action according to law.
                
                I am willing to provide any additional information or documents
                required in connection with this complaint.
                
                Thank you.
                
                Yours faithfully,
                
                $name
                
                Address:
                $address
                
                Date: __________
                Place: __________
                
                Signature: __________
                """.trimIndent()
            }

            "Consumer Complaint" -> {

                """
                TO
                The Appropriate Consumer Authority
                
                Subject: $subject
                
                Respected Sir/Madam,
                
                I, $name, residing at:
                
                $address
                
                am submitting this complaint regarding the following consumer
                matter:
                
                $details
                
                I request that the matter be examined and appropriate relief
                be provided in accordance with the applicable law.
                
                I am prepared to provide relevant invoices, receipts,
                communications and other supporting documents.
                
                Thank you.
                
                Yours faithfully,
                
                $name
                
                Address:
                $address
                
                Date: __________
                Place: __________
                
                Signature: __________
                """.trimIndent()
            }

            "RTI Application" -> {

                """
                TO
                The Public Information Officer
                [Public Authority]
                
                Subject: $subject
                
                Respected Sir/Madam,
                
                I, $name, residing at:
                
                $address
                
                request the following information under the Right to
                Information framework:
                
                $details
                
                Kindly provide the requested information in the prescribed
                manner.
                
                Applicant Details:
                
                Name: $name
                Address: $address
                
                Date: __________
                Place: __________
                
                Signature: __________
                """.trimIndent()
            }

            "Legal Notice" -> {

                """
                LEGAL NOTICE
                
                To:
                [Recipient Name]
                [Recipient Address]
                
                From:
                $name
                $address
                
                Subject: $subject
                
                Sir/Madam,
                
                Under instructions from the concerned party, this notice is
                issued regarding the following matter:
                
                $details
                
                You are requested to take appropriate steps concerning the
                above matter within the applicable period.
                
                This document is a preliminary draft and should be reviewed
                by a qualified legal professional before formal use.
                
                Sincerely,
                
                $name
                
                Address:
                $address
                
                Date: __________
                """.trimIndent()
            }

            "Rental Agreement Request" -> {

                """
                RENTAL AGREEMENT REQUEST
                
                From:
                $name
                $address
                
                Subject: $subject
                
                Dear Sir/Madam,
                
                I am writing regarding the proposed rental arrangement.
                
                Details:
                
                $details
                
                I request that the terms and conditions of the proposed
                tenancy be discussed and recorded in an appropriate written
                agreement.
                
                Please provide confirmation of the agreed terms.
                
                Regards,
                
                $name
                
                Address:
                $address
                
                Date: __________
                
                Signature: __________
                """.trimIndent()
            }

            "Leave & Permission Letter" -> {

                """
                TO
                [Authority / Manager / Head]
                
                Subject: $subject
                
                Respected Sir/Madam,
                
                I, $name, request leave / permission for the following reason:
                
                $details
                
                My address/contact details are:
                
                $address
                
                I kindly request you to consider my request and grant the
                necessary permission.
                
                Thank you.
                
                Yours faithfully,
                
                $name
                
                Date: __________
                
                Signature: __________
                """.trimIndent()
            }

            "Affidavit" -> {

                """
                AFFIDAVIT
                
                I, $name, residing at:
                
                $address
                
                do hereby state and affirm the following:
                
                $details
                
                I declare that the statements made above are based on the
                information available to me and are made for the purpose
                stated below:
                
                Subject:
                $subject
                
                This draft should be reviewed and executed in the manner
                required for the particular purpose and jurisdiction.
                
                DEPONENT
                
                Name: $name
                
                Date: __________
                Place: __________
                
                Signature: __________
                """.trimIndent()
            }

            else -> {

                """
                GENERAL APPLICATION
                
                To:
                [Concerned Authority]
                
                From:
                $name
                $address
                
                Subject: $subject
                
                Respected Sir/Madam,
                
                I am writing this application regarding the following matter:
                
                $details
                
                I kindly request the concerned authority to consider my
                application and take appropriate action.
                
                Thank you.
                
                Yours faithfully,
                
                $name
                
                Address:
                $address
                
                Date: __________
                Place: __________
                
                Signature: __________
                """.trimIndent()
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