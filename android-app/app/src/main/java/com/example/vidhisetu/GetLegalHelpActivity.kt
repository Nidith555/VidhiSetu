package com.example.vidhisetu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityGetLegalHelpBinding

class GetLegalHelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGetLegalHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityGetLegalHelpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        // Back
        binding.backButton.setOnClickListener {
            finish()
        }

        // 1. TALK TO A LAWYER
        binding.talkToLawyerCard.setOnClickListener {
            showTalkToLawyerDialog()
        }

        // 2. LEGAL AID
        binding.legalAidCard.setOnClickListener {
            showLegalAidDialog()
        }

        // 3. FIND NEARBY HELP
        binding.findNearbyHelpCard.setOnClickListener {
            findNearbyLegalHelp()
        }

        // 4. URGENT LEGAL HELP
        binding.urgentHelpCard.setOnClickListener {
            showUrgentHelpDialog()
        }

        // 5. ASK VIDHISETU
        binding.askVidhiSetuButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AskVidhiSetuActivity::class.java
                )
            )
        }
    }

    // ---------------------------------------------------------
    // TALK TO A LAWYER
    // ---------------------------------------------------------

    private fun showTalkToLawyerDialog() {

        val input = EditText(this)

        input.hint =
            "Briefly describe your legal problem"

        input.minLines = 4

        input.setPadding(
            30,
            20,
            30,
            20
        )

        AlertDialog.Builder(this)
            .setTitle("Talk to a Lawyer")
            .setMessage(
                "Describe your issue so you can prepare the information needed when contacting legal assistance."
            )
            .setView(input)
            .setPositiveButton("Continue") { _, _ ->

                val problem =
                    input.text
                        .toString()
                        .trim()

                if (problem.isBlank()) {

                    Toast.makeText(
                        this,
                        "Please describe your issue.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    saveLegalHelpRequest(problem)

                    showLawyerConnectionOptions()
                }
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun showLawyerConnectionOptions() {

        AlertDialog.Builder(this)
            .setTitle("Legal Assistance")
            .setMessage(
                "You can contact the National Legal Aid Helpline or continue using VidhiSetu to understand your situation."
            )
            .setPositiveButton("Call 15100") { _, _ ->
                dialNumber("15100")
            }
            .setNeutralButton("Ask VidhiSetu") { _, _ ->
                startActivity(
                    Intent(
                        this,
                        AskVidhiSetuActivity::class.java
                    )
                )
            }
            .setNegativeButton(
                "Close",
                null
            )
            .show()
    }

    // ---------------------------------------------------------
    // LEGAL AID
    // ---------------------------------------------------------

    private fun showLegalAidDialog() {

        AlertDialog.Builder(this)
            .setTitle("Free Legal Aid")
            .setMessage(
                """
                Legal Services Authorities provide legal services and assistance through the NALSA system.

                You can contact the National Legal Aid Helpline:

                15100

                You can also approach the appropriate State, District or Taluk Legal Services Authority.
                """.trimIndent()
            )
            .setPositiveButton("Call 15100") { _, _ ->
                dialNumber("15100")
            }
            .setNeutralButton("Open NALSA") { _, _ ->
                openWebsite(
                    "https://nalsa.gov.in/"
                )
            }
            .setNegativeButton(
                "Close",
                null
            )
            .show()
    }

    // ---------------------------------------------------------
    // FIND NEARBY HELP
    // ---------------------------------------------------------

    private fun findNearbyLegalHelp() {

        val uri =
            Uri.parse(
                "geo:0,0?q=District Legal Services Authority"
            )

        val mapIntent =
            Intent(
                Intent.ACTION_VIEW,
                uri
            )

        mapIntent.setPackage(
            "com.google.android.apps.maps"
        )

        try {

            startActivity(mapIntent)

        } catch (_: Exception) {

            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.google.com/maps/search/?api=1&query=District+Legal+Services+Authority"
                    )
                )

            try {

                startActivity(browserIntent)

            } catch (_: Exception) {

                Toast.makeText(
                    this,
                    "Unable to open maps.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ---------------------------------------------------------
    // URGENT HELP
    // ---------------------------------------------------------

    private fun showUrgentHelpDialog() {

        AlertDialog.Builder(this)
            .setTitle("Urgent Help")
            .setMessage(
                """
                If you or someone else is in immediate danger, use India's emergency response service.

                Emergency number:

                112

                Use this only for genuine emergencies requiring immediate assistance.
                """.trimIndent()
            )
            .setPositiveButton("Call 112") { _, _ ->
                dialNumber("112")
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    // ---------------------------------------------------------
    // ASK VIDHISETU
    // ---------------------------------------------------------

    private fun openAskVidhiSetu() {

        startActivity(
            Intent(
                this,
                AskVidhiSetuActivity::class.java
            )
        )
    }

    // ---------------------------------------------------------
    // SAVE REQUEST
    // ---------------------------------------------------------

    private fun saveLegalHelpRequest(
        problem: String
    ) {

        val preferences =
            getSharedPreferences(
                "vidhisetu_legal_help",
                MODE_PRIVATE
            )

        preferences.edit()
            .putString(
                "latest_problem",
                problem
            )
            .putLong(
                "request_time",
                System.currentTimeMillis()
            )
            .apply()

        Toast.makeText(
            this,
            "Your legal-help request has been saved.",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ---------------------------------------------------------
    // PHONE
    // ---------------------------------------------------------

    private fun dialNumber(
        number: String
    ) {

        val intent =
            Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:$number")
            )

        try {

            startActivity(intent)

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Unable to open the phone application.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ---------------------------------------------------------
    // WEBSITE
    // ---------------------------------------------------------

    private fun openWebsite(
        url: String
    ) {

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

        try {

            startActivity(intent)

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Unable to open the website.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}