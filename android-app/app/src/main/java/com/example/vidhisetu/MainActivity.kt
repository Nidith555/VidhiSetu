package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.vidhisetu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        applySavedTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
    }

    private fun applySavedTheme() {

        val preferences = getSharedPreferences(
            "vidhisetu_settings",
            MODE_PRIVATE
        )

        val darkMode = preferences.getBoolean(
            "dark_mode",
            false
        )

        if (darkMode) {

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

        } else {

            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupClicks() {

        // =========================
        // TOP PROFILE
        // =========================

        binding.profileIcon.setOnClickListener {
            openProfile()
        }


        // =========================
        // MAIN HOME CARDS
        // =========================

        binding.askCard.setOnClickListener {
            openAskVidhiSetu()
        }

        binding.scanCard.setOnClickListener {
            openScanExplain()
        }

        binding.rightsCard.setOnClickListener {
            openKnowMyRights()
        }

        binding.helpCard.setOnClickListener {
            openGetLegalHelp()
        }


        // =========================
        // LEGAL SERVICES
        // =========================

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


        // =========================
        // BOTTOM NAVIGATION
        // =========================

        binding.homeNav.setOnClickListener {
            // Already on Home
        }

        binding.servicesNav.setOnClickListener {
            openServices()
        }

        binding.askBottomButton.setOnClickListener {
            openAskVidhiSetu()
        }

        binding.resourcesNav.setOnClickListener {
            openResources()
        }

        binding.profileNav.setOnClickListener {
            openProfile()
        }


        // =========================
        // VIEW ALL SERVICES
        // =========================

        binding.viewAllServices.setOnClickListener {
            openServices()
        }


        // =========================
        // VIEW ALL NEWS
        // =========================

        binding.viewAllNews.setOnClickListener {
            openResources()
        }
    }


    // =========================
    // NAVIGATION METHODS
    // =========================

    private fun openAskVidhiSetu() {

        startActivity(
            Intent(
                this,
                AskVidhiSetuActivity::class.java
            )
        )
    }


    private fun openScanExplain() {

        startActivity(
            Intent(
                this,
                ScanExplainActivity::class.java
            )
        )
    }


    private fun openKnowMyRights() {

        startActivity(
            Intent(
                this,
                KnowMyRightsActivity::class.java
            )
        )
    }


    private fun openGetLegalHelp() {

        startActivity(
            Intent(
                this,
                GetLegalHelpActivity::class.java
            )
        )
    }


    private fun openServices() {

        startActivity(
            Intent(
                this,
                ServicesActivity::class.java
            )
        )
    }


    private fun openResources() {

        startActivity(
            Intent(
                this,
                ResourcesActivity::class.java
            )
        )
    }
    private fun openLegalNews() {
        startActivity(
            Intent(
                this,
                LegalNewsActivity::class.java
            )
        )
    }


    private fun openProfile() {

        startActivity(
            Intent(
                this,
                ProfileActivity::class.java
            )
        )
    }
}