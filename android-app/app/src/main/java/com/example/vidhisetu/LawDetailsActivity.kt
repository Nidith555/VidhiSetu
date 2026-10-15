package com.example.vidhisetu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityLawDetailsBinding

class LawDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLawDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLawDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        loadLawInformation()
        setupClicks()
    }

    private fun loadLawInformation() {

        val shortName =
            intent.getStringExtra(
                "law_short_name"
            ) ?: "Law"

        val fullName =
            intent.getStringExtra(
                "law_full_name"
            ) ?: ""

        val description =
            intent.getStringExtra(
                "law_description"
            ) ?: ""

        binding.lawShortName.text =
            shortName

        binding.lawFullName.text =
            fullName

        binding.lawDescription.text =
            description
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.openOfficialSourceButton.setOnClickListener {

            val url =
                intent.getStringExtra(
                    "law_url"
                )

            if (url.isNullOrBlank()) {

                Toast.makeText(
                    this,
                    "Official source unavailable.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            try {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                )

            } catch (_: Exception) {

                Toast.makeText(
                    this,
                    "Unable to open the official source.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}