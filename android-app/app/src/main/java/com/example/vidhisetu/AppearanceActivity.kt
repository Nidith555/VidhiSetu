package com.example.vidhisetu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.vidhisetu.databinding.ActivityAppearanceBinding

class AppearanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppearanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppearanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentTheme()
        setupClicks()
    }

    private fun loadCurrentTheme() {

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

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { _, enabled ->

            saveThemePreference(enabled)

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
    }

    private fun saveThemePreference(enabled: Boolean) {

        val preferences = getSharedPreferences(
            "vidhisetu_settings",
            MODE_PRIVATE
        )

        preferences.edit()
            .putBoolean("dark_mode", enabled)
            .apply()
    }
}