package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.vidhisetu.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load saved Dark Mode state
        loadDarkModeState()

        // Setup all clicks
        setupClicks()
    }

    // =========================================================
    // ALL CLICK LISTENERS
    // =========================================================

    private fun setupClicks() {

        // -----------------------------------------------------
        // BACK
        // -----------------------------------------------------

        binding.backButton.setOnClickListener {
            finish()
        }


        // -----------------------------------------------------
        // SWITCH PORTAL
        // -----------------------------------------------------

        binding.switchAccountCard.setOnClickListener {
            showPortalSelectionDialog()
        }


        // -----------------------------------------------------
        // DARK MODE
        // -----------------------------------------------------

        binding.darkModeSwitch.setOnCheckedChangeListener { _, enabled ->

            saveDarkModePreference(enabled)

            if (enabled) {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

            } else {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }


        // =====================================================
        // ACCOUNT
        // =====================================================

        binding.personalInfoCard.setOnClickListener {
            showMessage("Personal Information")
        }

        binding.languageCard.setOnClickListener {
            showMessage("Language Settings")
        }

        binding.voiceSettingsCard.setOnClickListener {
            showMessage("Voice & Accessibility")
        }


        // =====================================================
        // MY VIDHISETU
        // =====================================================

        binding.savedDocumentsCard.setOnClickListener {
            showMessage("Saved Documents")
        }

        binding.savedGuidanceCard.setOnClickListener {
            showMessage("Saved Guidance")
        }

        binding.historyCard.setOnClickListener {
            showMessage("History")
        }


        // =====================================================
        // PREFERENCES
        // =====================================================

        binding.notificationsCard.setOnClickListener {
            showMessage("Notification Settings")
        }

        binding.privacyCard.setOnClickListener {
            showMessage("Privacy & Security")
        }


        // =====================================================
        // HELP & INFORMATION
        // =====================================================

        binding.helpCard.setOnClickListener {
            showMessage("Help & Support")
        }

        binding.aboutCard.setOnClickListener {
            showMessage("About VidhiSetu")
        }


        // =====================================================
        // LOG OUT
        // =====================================================

        binding.logoutButton.setOnClickListener {
            showMessage("Log Out")
        }
    }


    // =========================================================
    // PORTAL SELECTION DIALOG
    // =========================================================

    private fun showPortalSelectionDialog() {

        val portals = arrayOf(
            "👤  Citizen Portal",
            "🎓  Student Portal",
            "⚖️  Lawyer Portal"
        )

        AlertDialog.Builder(this)
            .setTitle("Switch Portal")
            .setItems(portals) { _, which ->

                when (which) {

                    // Citizen
                    0 -> {
                        switchToCitizen()
                    }

                    // Student
                    1 -> {
                        switchToStudent()
                    }

                    // Lawyer
                    2 -> {
                        switchToLawyer()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    // =========================================================
    // SWITCH TO CITIZEN
    // =========================================================

    private fun switchToCitizen() {

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }


    // =========================================================
    // SWITCH TO STUDENT
    // =========================================================

    private fun switchToStudent() {

        val intent = Intent(
            this,
            StudentHomeActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }


    // =========================================================
    // SWITCH TO LAWYER
    // =========================================================

    private fun switchToLawyer() {

        val intent = Intent(
            this,
            LawyerHomeActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }


    // =========================================================
    // LOAD DARK MODE
    // =========================================================

    private fun loadDarkModeState() {

        val preferences = getSharedPreferences(
            "vidhisetu_settings",
            MODE_PRIVATE
        )

        val darkMode = preferences.getBoolean(
            "dark_mode",
            false
        )

        binding.darkModeSwitch.isChecked = darkMode
    }


    // =========================================================
    // SAVE DARK MODE
    // =========================================================

    private fun saveDarkModePreference(enabled: Boolean) {

        val preferences = getSharedPreferences(
            "vidhisetu_settings",
            MODE_PRIVATE
        )

        preferences.edit()
            .putBoolean("dark_mode", enabled)
            .apply()
    }


    // =========================================================
    // TOAST MESSAGE
    // =========================================================

    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}