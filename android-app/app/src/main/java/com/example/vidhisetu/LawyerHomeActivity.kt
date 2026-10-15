package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityLawyerHomeBinding

class LawyerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLawyerHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLawyerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        // =====================================================
        // PROFILE
        // =====================================================

        binding.topProfileButton.setOnClickListener {
            openProfile()
        }


        // =====================================================
        // LEGAL WORKSPACE
        // =====================================================

        // My Matters
        binding.myMattersCard.setOnClickListener {
            openCaseManagement()
        }

        // Legal Research
        binding.legalResearchCard.setOnClickListener {
            openLegalResearch()
        }

        // Drafting
        binding.draftingCard.setOnClickListener {
            openDrafting()
        }

        // Documents
        binding.documentsCard.setOnClickListener {
            openDocumentManager()
        }


        // =====================================================
        // PROFESSIONAL SERVICES
        // =====================================================

        // Case Research
        binding.caseResearchCard.setOnClickListener {
            openCaseResearch()
        }

        // Citation Verification
        binding.citationCard.setOnClickListener {
            openCitationVerification()
        }

        // Document Analysis
        binding.documentAnalysisCard.setOnClickListener {
            openDocumentAnalysis()
        }

        // Draft Review
        binding.draftReviewCard.setOnClickListener {
            openDraftReview()
        }


        // =====================================================
        // ACTIVE MATTERS
        // =====================================================

        // Recent Cases
        binding.recentCasesCard.setOnClickListener {
            openCaseManagement()
        }

        // Pending Work
        binding.pendingWorkCard.setOnClickListener {
            openPendingWork()
        }

        // Important Documents
        binding.importantDocumentsCard.setOnClickListener {
            openDocumentManager()
        }


        // =====================================================
        // LEGAL RESOURCES
        // =====================================================

        binding.viewAllResources.setOnClickListener {
            openResources()
        }


        // =====================================================
        // BOTTOM NAVIGATION
        // =====================================================

        // Home
        binding.homeNav.setOnClickListener {
            binding.lawyerScrollView.smoothScrollTo(0, 0)
        }

        // Matters
        binding.mattersNav.setOnClickListener {
            openCaseManagement()
        }

        // AI Assistant
        binding.askAdvocateButton.setOnClickListener {
            openAskVidhiSetu()
        }

        // Resources
        binding.resourcesNav.setOnClickListener {
            openResources()
        }

        // Profile
        binding.profileNav.setOnClickListener {
            openProfile()
        }
    }


    // =========================================================
    // NAVIGATION METHODS
    // =========================================================

    private fun openCaseManagement() {
        startActivity(
            Intent(
                this,
                CaseManagementActivity::class.java
            )
        )
    }

    private fun openLegalResearch() {
        startActivity(
            Intent(
                this,
                RelevantLawsActivity::class.java
            )
        )
    }

    private fun openDrafting() {
        startActivity(
            Intent(
                this,
                DocumentTemplatesActivity::class.java
            )
        )
    }

    private fun openDocumentManager() {
        startActivity(
            Intent(
                this,
                LawyerDocumentsActivity::class.java
            )
        )
    }

    private fun openCaseResearch() {
        startActivity(
            Intent(
                this,
                LawyerCaseResearchActivity::class.java
            )
        )
    }

    private fun openCitationVerification() {
        startActivity(
            Intent(
                this,
                CitationVerificationActivity::class.java
            )
        )
    }

    private fun openDocumentAnalysis() {
        startActivity(
            Intent(
                this,
                ScanExplainActivity::class.java
            )
        )
    }

    private fun openDraftReview() {
        startActivity(
            Intent(
                this,
                DraftReviewActivity::class.java
            )
        )
    }

    private fun openPendingWork() {
        startActivity(
            Intent(
                this,
                PendingWorkActivity::class.java
            )
        )
    }

    private fun openAskVidhiSetu() {
        startActivity(
            Intent(
                this,
                AskVidhiSetuActivity::class.java
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

    private fun openProfile() {
        startActivity(
            Intent(
                this,
                ProfileActivity::class.java
            )
        )
    }
}