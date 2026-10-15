package com.example.vidhisetu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityRightInformationBinding

class RightInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRightInformationBinding
    private lateinit var aiSpeechController: AiSpeechController

    private var information = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRightInformationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupSpeech()
        loadInformation()
        setupClicks()
    }

    private fun setupSpeech() {

        aiSpeechController =
            AiSpeechController(
                context = this,
                onStart = {
                    runOnUiThread {
                        binding.listenButton.text =
                            "🔊  Speaking..."
                    }
                },
                onComplete = {
                    runOnUiThread {
                        binding.listenButton.text =
                            "🔊  Listen"
                    }
                }
            )
    }

    private fun loadInformation() {

        val title =
            intent.getStringExtra("RIGHT_TITLE")
                ?: "Legal Rights"

        information =
            intent.getStringExtra("RIGHT_INFORMATION")
                ?: "No information available."

        binding.rightTitle.text = title
        binding.rightInformation.text = information
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.listenButton.setOnClickListener {

            if (information.isBlank()) {

                Toast.makeText(
                    this,
                    "No information available.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                aiSpeechController.speak(
                    information
                )
            }
        }
    }

    override fun onDestroy() {

        if (::aiSpeechController.isInitialized) {
            aiSpeechController.shutdown()
        }

        super.onDestroy()
    }
}