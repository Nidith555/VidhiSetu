package com.example.vidhisetu

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vidhisetu.databinding.ActivityCompletedCasesBinding
import org.json.JSONArray
import org.json.JSONObject

class CompletedCasesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletedCasesBinding

    private val preferencesName = "completed_cases_data"
    private val casesKey = "completed_cases"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompletedCasesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
        loadCases()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addCaseButton.setOnClickListener {
            showAddCaseDialog()
        }
    }

    private fun showAddCaseDialog() {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_completed_case,
            null
        )

        val titleInput =
            dialogView.findViewById<android.widget.EditText>(
                R.id.caseTitleInput
            )

        val courtInput =
            dialogView.findViewById<android.widget.EditText>(
                R.id.courtInput
            )

        val areaInput =
            dialogView.findViewById<android.widget.EditText>(
                R.id.areaInput
            )

        val resultInput =
            dialogView.findViewById<android.widget.EditText>(
                R.id.resultInput
            )

        val notesInput =
            dialogView.findViewById<android.widget.EditText>(
                R.id.notesInput
            )

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Completed Case")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val title =
                        titleInput.text.toString().trim()

                    val court =
                        courtInput.text.toString().trim()

                    val area =
                        areaInput.text.toString().trim()

                    val result =
                        resultInput.text.toString().trim()

                    val notes =
                        notesInput.text.toString().trim()

                    if (title.isBlank()) {
                        titleInput.error =
                            "Enter the case title"
                        titleInput.requestFocus()
                        return@setOnClickListener
                    }

                    saveCase(
                        title,
                        court,
                        area,
                        result,
                        notes
                    )

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    private fun saveCase(
        title: String,
        court: String,
        area: String,
        result: String,
        notes: String
    ) {

        val preferences = getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

        val existingJson =
            preferences.getString(casesKey, "[]")

        val array = JSONArray(existingJson)

        val caseObject = JSONObject().apply {
            put("title", title)
            put("court", court)
            put("area", area)
            put("result", result)
            put("notes", notes)
        }

        array.put(caseObject)

        preferences.edit()
            .putString(casesKey, array.toString())
            .apply()

        loadCases()
    }

    private fun loadCases() {

        binding.casesContainer.removeAllViews()

        val preferences = getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

        val json =
            preferences.getString(casesKey, "[]")

        try {

            val array = JSONArray(json)

            if (array.length() == 0) {

                binding.emptyText.visibility =
                    View.VISIBLE

                binding.caseCountText.text =
                    "No completed cases"

                return
            }

            binding.emptyText.visibility =
                View.GONE

            binding.caseCountText.text =
                "${array.length()} completed case(s)"

            for (i in array.length() - 1 downTo 0) {

                val caseObject =
                    array.getJSONObject(i)

                addCaseCard(
                    caseObject,
                    i
                )
            }

        } catch (e: Exception) {

            binding.emptyText.visibility =
                View.VISIBLE

            binding.caseCountText.text =
                "No completed cases"
        }
    }

    private fun addCaseCard(
        caseObject: JSONObject,
        index: Int
    ) {

        val card = LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            15,
            16,
            15
        )

        card.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.simple_right_card
            )

        card.isClickable = true
        card.isFocusable = true

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.bottomMargin = 10

        card.layoutParams = params

        val title =
            TextView(this)

        title.text =
            caseObject.optString(
                "title",
                "Untitled Case"
            )

        title.textSize = 15f

        title.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_primary_text
            )
        )

        title.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val court =
            TextView(this)

        court.text =
            caseObject.optString(
                "court",
                "Court not specified"
            )

        court.textSize = 11f

        court.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_secondary_text
            )
        )

        court.setPadding(
            0,
            5,
            0,
            0
        )

        val area =
            TextView(this)

        area.text =
            caseObject.optString(
                "area",
                "Legal area not specified"
            )

        area.textSize = 11f

        area.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_secondary_text
            )
        )

        area.setPadding(
            0,
            3,
            0,
            0
        )

        val result =
            TextView(this)

        result.text =
            "Result: " +
                    caseObject.optString(
                        "result",
                        "Not specified"
                    )

        result.textSize = 12f

        result.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_navy
            )
        )

        result.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        result.setPadding(
            0,
            8,
            0,
            0
        )

        val action =
            TextView(this)

        action.text =
            "View case details  →"

        action.textSize = 11f

        action.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_navy
            )
        )

        action.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        action.setPadding(
            0,
            9,
            0,
            0
        )

        card.addView(title)
        card.addView(court)
        card.addView(area)
        card.addView(result)
        card.addView(action)

        card.setOnClickListener {

            showCaseDetails(
                caseObject,
                index
            )
        }

        binding.casesContainer.addView(card)
    }

    private fun showCaseDetails(
        caseObject: JSONObject,
        index: Int
    ) {

        val details = """
            Court:
            ${caseObject.optString("court", "Not specified")}
            
            Legal Area:
            ${caseObject.optString("area", "Not specified")}
            
            Result:
            ${caseObject.optString("result", "Not specified")}
            
            Notes:
            ${caseObject.optString("notes", "No notes added")}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(
                caseObject.optString(
                    "title",
                    "Case Details"
                )
            )
            .setMessage(details)
            .setNegativeButton("Close", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteCase(index)
            }
            .show()
    }

    private fun deleteCase(index: Int) {

        val preferences = getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )

        val oldJson =
            preferences.getString(
                casesKey,
                "[]"
            )

        val oldArray =
            JSONArray(oldJson)

        val newArray =
            JSONArray()

        for (i in 0 until oldArray.length()) {

            if (i != index) {
                newArray.put(
                    oldArray.getJSONObject(i)
                )
            }
        }

        preferences.edit()
            .putString(
                casesKey,
                newArray.toString()
            )
            .apply()

        loadCases()
    }
}