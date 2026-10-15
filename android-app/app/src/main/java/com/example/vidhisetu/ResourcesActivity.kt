package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityResourcesBinding

class ResourcesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResourcesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResourcesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        // Back
        binding.backButton.setOnClickListener {
            finish()
        }

        // Indian Laws
        binding.indianLawsCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RelevantLawsActivity::class.java
                )
            )
        }

        // Constitution
        binding.constitutionCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RelevantLawsActivity::class.java
                )
            )
        }

        // BNS / BNSS / BSA
        binding.bnsCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RelevantLawsActivity::class.java
                )
            )
        }

        // Legal FAQs
        binding.faqCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    KnowMyRightsActivity::class.java
                )
            )
        }

        // Legal Glossary
        binding.glossaryCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    KnowMyRightsActivity::class.java
                )
            )
        }

        // Forms & Templates
        binding.formsCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DocumentTemplatesActivity::class.java
                )
            )
        }

        // Courts & Institutions
        binding.courtsCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    GetLegalHelpActivity::class.java
                )
            )
        }

        // Legal News
        binding.legalNewsCard.setOnClickListener {
            openLegalNews()
        }

        // Legal Aid Directory
        binding.legalAidCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    GetLegalHelpActivity::class.java
                )
            )
        }
    }

    private fun openLegalNews() {
        startActivity(
            Intent(
                this,
                LegalNewsActivity::class.java
            )
        )
    }
}