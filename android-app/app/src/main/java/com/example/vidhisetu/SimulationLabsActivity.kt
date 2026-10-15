package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivitySimulationLabsBinding

class SimulationLabsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimulationLabsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySimulationLabsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.mootCourtCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    MootCourtSimulationActivity::class.java
                )
            )
        }

        binding.courtroomCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CourtroomSimulationActivity::class.java
                )
            )
        }

        binding.caseAnalysisCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CaseAnalysisLabActivity::class.java
                )
            )
        }

        binding.argumentLabCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ArgumentPerformanceActivity::class.java
                )
            )
        }

        binding.legalResearchCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RelevantLawsActivity::class.java
                )
            )
        }

        binding.draftingLabCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DocumentTemplatesActivity::class.java
                )
            )
        }

        binding.judgmentAnalysisCard.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    JudgmentAnalysisLabActivity::class.java
                )
            )
        }
    }

    private fun showComingSoon(name: String) {
        android.widget.Toast.makeText(
            this,
            "$name will be added next.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}