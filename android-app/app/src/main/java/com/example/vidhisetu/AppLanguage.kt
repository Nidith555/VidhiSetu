package com.example.vidhisetu

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

/** Stores the user's preferred language for intake and speech. */
object AppLanguage {
    const val ENGLISH = "en"
    const val HINDI = "hi"
    const val TELUGU = "te"

    private const val PREFS_NAME = "vidhisetu_settings"
    private const val LANGUAGE_KEY = "language_code"

    fun getCode(activity: AppCompatActivity): String {
        val saved = activity.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, ENGLISH)
        return when (saved) {
            HINDI, TELUGU -> saved
            else -> ENGLISH
        }
    }

    fun setCode(activity: AppCompatActivity, code: String) {
        val safe = when (code) {
            HINDI, TELUGU -> code
            else -> ENGLISH
        }
        activity.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
            .edit().putString(LANGUAGE_KEY, safe).apply()
    }

    fun locale(code: String): Locale = when (code) {
        HINDI -> Locale("hi", "IN")
        TELUGU -> Locale("te", "IN")
        else -> Locale("en", "IN")
    }

    fun text(code: String, english: String, hindi: String, telugu: String): String =
        when (code) {
            HINDI -> hindi
            TELUGU -> telugu
            else -> english
        }

    fun showPicker(activity: AppCompatActivity, onSelected: (String) -> Unit) {
        val codes = arrayOf(ENGLISH, HINDI, TELUGU)
        val labels = arrayOf("English", "हिन्दी", "తెలుగు")
        val current = getCode(activity)
        AlertDialog.Builder(activity)
            .setTitle(text(current, "Choose language", "भाषा चुनें", "భాషను ఎంచుకోండి"))
            .setSingleChoiceItems(labels, codes.indexOf(current)) { dialog, which ->
                val selected = codes[which]
                setCode(activity, selected)
                dialog.dismiss()
                onSelected(selected)
            }
            .setNegativeButton(text(current, "Cancel", "रद्द करें", "రద్దు"), null)
            .show()
    }
}
