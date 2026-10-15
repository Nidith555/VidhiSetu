package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityStudentHomeBinding

class StudentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        // Profile
        binding.topProfileButton.setOnClickListener {
            openProfile()
        }

        // =========================
        // LEARNING TOOLS
        // =========================

        // AI Legal Tutor
        binding.tutorCard.setOnClickListener {
            openTutor()
        }
        binding.simulationLabsCard.setOnClickListener {
            openlabs()
        }

        // Case Library
        binding.caseLibraryCard.setOnClickListener {
            openCaseLibrary()
        }

        // Practice
        binding.practiceCard.setOnClickListener {
            openPractice()
        }

        // Case Preparation
        binding.casePreparationCard.setOnClickListener {
            openCasePreparation()
        }

        // =========================
        // MY LEARNING
        // =========================

        binding.currentPracticeCard.setOnClickListener {
            openPractice()
        }

        binding.argumentPerformanceCard.setOnClickListener {
            openArgumentPerformance()
        }

        binding.completedCasesCard.setOnClickListener {
            openCompletedCases()
        }

        // =========================
        // RESOURCES
        // =========================

        binding.viewAllResources.setOnClickListener {
            openResources()
        }

        // =========================
        // BOTTOM NAVIGATION
        // =========================

        binding.homeNav.setOnClickListener {
            binding.studentScrollView.smoothScrollTo(
                0,
                0
            )
        }

        binding.practiceNav.setOnClickListener {
            openPractice()
        }

        binding.askTutorButton.setOnClickListener {
            openTutor()
        }

        binding.resourcesNav.setOnClickListener {
            openResources()
        }

        binding.profileNav.setOnClickListener {
            openProfile()
        }
    }

    private fun openTutor() {
        startActivity(
            Intent(
                this,
                AskVidhiSetuActivity::class.java
            )
        )
    }

    private fun openCaseLibrary() {
        startActivity(
            Intent(
                this,
                StudentCaseLibraryActivity::class.java
            )
        )
    }
    private fun openlabs() {
        startActivity(
            Intent(
                this,
                SimulationLabsActivity::class.java
            )
        )
    }

    private fun openPractice() {
        startActivity(
            Intent(
                this,
                StudentPracticeActivity::class.java
            )
        )
    }

    private fun openCasePreparation() {
        startActivity(
            Intent(
                this,
                CasePreparationActivity::class.java
            )
        )
    }

    private fun openArgumentPerformance() {
        startActivity(
            Intent(
                this,
                ArgumentPerformanceActivity::class.java
            )
        )
    }

    private fun openCompletedCases() {
        startActivity(
            Intent(
                this,
                CompletedCasesActivity::class.java
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