package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityServicesBinding

class ServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // 1. Legal Guidance
        binding.legalGuidanceCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AskVidhiSetuActivity::class.java
                )
            )
        }

        // 2. Document Analysis
        binding.documentAnalysisCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ScanExplainActivity::class.java
                )
            )
        }

        // 3. Find Relevant Laws
        binding.relevantLawsCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RelevantLawsActivity::class.java
                )
            )
        }

        // 4. Rights & Schemes
        binding.rightsSchemesCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    KnowMyRightsActivity::class.java
                )
            )
        }

        // 5. Legal Aid
        binding.legalAidCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    GetLegalHelpActivity::class.java
                )
            )
        }

        // 6. Document Drafting
        binding.documentDraftingCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DocumentTemplatesActivity::class.java
                )
            )
        }

        // 7. Voice Assistant
        binding.voiceAssistantCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AskVidhiSetuActivity::class.java
                )
            )
        }

        // 8. Case Management
        binding.caseManagementCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CaseManagementActivity::class.java
                )
            )
        }
    }
}