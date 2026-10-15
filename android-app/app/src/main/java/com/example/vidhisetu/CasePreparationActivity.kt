package com.example.vidhisetu

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityCasePreparationBinding
import org.json.JSONArray
import org.json.JSONObject

class CasePreparationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCasePreparationBinding

    private val preferencesName = "case_preparation_data"
    private val preparationsKey = "preparations"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCasePreparationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
        loadSavedPreparations()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.generateButton.setOnClickListener {
            generatePreparation()
        }

        binding.saveButton.setOnClickListener {
            savePreparation()
        }

        binding.viewSavedButton.setOnClickListener {
            showSavedPreparations()
        }

        binding.clearButton.setOnClickListener {
            clearFields()
        }
    }

    private fun generatePreparation() {

        val title = binding.caseTitleInput.text.toString().trim()
        val parties = binding.partiesInput.text.toString().trim()
        val facts = binding.factsInput.text.toString().trim()
        val issues = binding.issuesInput.text.toString().trim()
        val laws = binding.lawsInput.text.toString().trim()
        val arguments = binding.argumentsInput.text.toString().trim()

        if (title.isBlank()) {
            binding.caseTitleInput.error = "Enter the case title"
            binding.caseTitleInput.requestFocus()
            return
        }

        if (facts.isBlank()) {
            binding.factsInput.error = "Enter the important facts"
            binding.factsInput.requestFocus()
            return
        }

        val preparation = buildPreparation(
            title,
            parties,
            facts,
            issues,
            laws,
            arguments
        )

        binding.previewTitle.text = title
        binding.previewText.text = preparation

        binding.previewCard.visibility = android.view.View.VISIBLE

        Toast.makeText(
            this,
            "Case preparation generated",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun buildPreparation(
        title: String,
        parties: String,
        facts: String,
        issues: String,
        laws: String,
        arguments: String
    ): String {

        return """
            CASE PREPARATION SHEET
            
            Case:
            $title
            
            PARTIES
            ${if (parties.isBlank()) "Not provided" else parties}
            
            IMPORTANT FACTS
            $facts
            
            LEGAL ISSUES
            ${if (issues.isBlank()) "Not provided" else issues}
            
            APPLICABLE LAWS
            ${if (laws.isBlank()) "Not provided" else laws}
            
            KEY ARGUMENTS
            ${if (arguments.isBlank()) "Not provided" else arguments}
            
            STUDENT NOTES
            Review the facts, legal issues, statutory provisions,
            precedents and arguments before using this preparation
            for academic or practical work.
        """.trimIndent()
    }

    private fun savePreparation() {

        val title = binding.caseTitleInput.text.toString().trim()
        val parties = binding.partiesInput.text.toString().trim()
        val facts = binding.factsInput.text.toString().trim()
        val issues = binding.issuesInput.text.toString().trim()
        val laws = binding.lawsInput.text.toString().trim()
        val arguments = binding.argumentsInput.text.toString().trim()

        if (title.isBlank()) {
            binding.caseTitleInput.error = "Enter the case title"
            return
        }

        if (facts.isBlank()) {
            binding.factsInput.error = "Enter the important facts"
            return
        }

        val preferences = getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

        val existingJson = preferences.getString(
            preparationsKey,
            "[]"
        )

        val array = JSONArray(existingJson)

        val item = JSONObject().apply {
            put("title", title)
            put("parties", parties)
            put("facts", facts)
            put("issues", issues)
            put("laws", laws)
            put("arguments", arguments)
            put(
                "preparation",
                buildPreparation(
                    title,
                    parties,
                    facts,
                    issues,
                    laws,
                    arguments
                )
            )
        }

        array.put(item)

        preferences.edit()
            .putString(preparationsKey, array.toString())
            .apply()

        binding.previewTitle.text = title
        binding.previewText.text = item.getString("preparation")
        binding.previewCard.visibility = android.view.View.VISIBLE

        Toast.makeText(
            this,
            "Case preparation saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun loadSavedPreparations() {

        val preferences = getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

        val json = preferences.getString(
            preparationsKey,
            "[]"
        )

        try {
            val array = JSONArray(json)

            if (array.length() > 0) {
                binding.savedCountText.text =
                    "${array.length()} saved preparation(s)"
            } else {
                binding.savedCountText.text =
                    "No saved preparations"
            }

        } catch (e: Exception) {
            binding.savedCountText.text =
                "No saved preparations"
        }
    }

    private fun showSavedPreparations() {

        val preferences = getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

        val json = preferences.getString(
            preparationsKey,
            "[]"
        )

        val array = JSONArray(json)

        if (array.length() == 0) {
            Toast.makeText(
                this,
                "No saved case preparations",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val titles = Array(array.length()) { index ->
            array.getJSONObject(index).getString("title")
        }

        AlertDialog.Builder(this)
            .setTitle("Saved Case Preparations")
            .setItems(titles) { _, which ->
                showSavedPreparation(array.getJSONObject(which))
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSavedPreparation(item: JSONObject) {

        val title = item.getString("title")
        val preparation = item.getString("preparation")

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(preparation)
            .setPositiveButton("Close", null)
            .setNeutralButton("Delete") { _, _ ->
                deletePreparation(title)
            }
            .show()
    }

    private fun deletePreparation(title: String) {

        val preferences = getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

        val oldJson = preferences.getString(
            preparationsKey,
            "[]"
        )

        val oldArray = JSONArray(oldJson)
        val newArray = JSONArray()

        for (i in 0 until oldArray.length()) {

            val item = oldArray.getJSONObject(i)

            if (item.getString("title") != title) {
                newArray.put(item)
            }
        }

        preferences.edit()
            .putString(preparationsKey, newArray.toString())
            .apply()

        loadSavedPreparations()

        Toast.makeText(
            this,
            "Preparation deleted",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun clearFields() {

        binding.caseTitleInput.text?.clear()
        binding.partiesInput.text?.clear()
        binding.factsInput.text?.clear()
        binding.issuesInput.text?.clear()
        binding.lawsInput.text?.clear()
        binding.argumentsInput.text?.clear()

        binding.previewCard.visibility = android.view.View.GONE

        Toast.makeText(
            this,
            "Fields cleared",
            Toast.LENGTH_SHORT
        ).show()
    }
}