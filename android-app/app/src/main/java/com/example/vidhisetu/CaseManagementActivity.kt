package com.example.vidhisetu

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityCaseManagementBinding
import org.json.JSONArray
import org.json.JSONObject

class CaseManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaseManagementBinding

    private val cases = mutableListOf<CaseItem>()

    private val preferencesName = "vidhisetu_case_management"
    private val casesKey = "saved_cases"

    data class CaseItem(
        val id: Long,
        var title: String,
        var person: String,
        var caseType: String,
        var description: String,
        var nextDate: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityCaseManagementBinding.inflate(layoutInflater)

        setContentView(binding.root)

        loadCases()
        setupClicks()
        refreshCaseList()
    }

    // =========================================================
    // CLICK ACTIONS
    // =========================================================

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addCaseButton.setOnClickListener {
            showAddCaseDialog()
        }
    }

    // =========================================================
    // ADD CASE
    // =========================================================

    private fun showAddCaseDialog() {

        val dialogView =
            LayoutInflater.from(this)
                .inflate(
                    R.layout.dialog_add_case,
                    null
                )

        val titleInput =
            dialogView.findViewById<EditText>(
                R.id.caseTitleInput
            )

        val personInput =
            dialogView.findViewById<EditText>(
                R.id.casePersonInput
            )

        val typeInput =
            dialogView.findViewById<EditText>(
                R.id.caseTypeInput
            )

        val descriptionInput =
            dialogView.findViewById<EditText>(
                R.id.caseDescriptionInput
            )

        val dateInput =
            dialogView.findViewById<EditText>(
                R.id.caseNextDateInput
            )

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Add New Case")
                .setView(dialogView)
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton(
                    "Save",
                    null
                )
                .create()

        dialog.setOnShowListener {

            val saveButton =
                dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
                )

            saveButton.setOnClickListener {

                val title =
                    titleInput.text.toString().trim()

                val person =
                    personInput.text.toString().trim()

                val caseType =
                    typeInput.text.toString().trim()

                val description =
                    descriptionInput.text.toString().trim()

                val nextDate =
                    dateInput.text.toString().trim()

                if (title.isBlank()) {

                    titleInput.error =
                        "Enter a case title"

                    titleInput.requestFocus()

                    return@setOnClickListener
                }

                if (caseType.isBlank()) {

                    typeInput.error =
                        "Enter the case type"

                    typeInput.requestFocus()

                    return@setOnClickListener
                }

                if (description.isBlank()) {

                    descriptionInput.error =
                        "Enter a short description"

                    descriptionInput.requestFocus()

                    return@setOnClickListener
                }

                val newCase =
                    CaseItem(
                        id = System.currentTimeMillis(),
                        title = title,
                        person = person,
                        caseType = caseType,
                        description = description,
                        nextDate = nextDate
                    )

                cases.add(newCase)

                saveCases()

                refreshCaseList()

                Toast.makeText(
                    this,
                    "Case saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // =========================================================
    // DISPLAY CASES
    // =========================================================

    private fun refreshCaseList() {

        binding.casesContainer.removeAllViews()

        if (cases.isEmpty()) {

            binding.emptyCasesText.visibility =
                View.VISIBLE

            binding.casesContainer.visibility =
                View.GONE

            return
        }

        binding.emptyCasesText.visibility =
            View.GONE

        binding.casesContainer.visibility =
            View.VISIBLE

        cases.forEach { caseItem ->

            addCaseCard(caseItem)
        }
    }

    private fun addCaseCard(
        caseItem: CaseItem
    ) {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            15,
            16,
            15
        )

        card.background =
            getDrawable(
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
            caseItem.title

        title.textSize = 16f

        title.setTextColor(
            getColor(
                R.color.vs_primary_text
            )
        )

        title.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val type =
            TextView(this)

        type.text =
            "Case type: ${caseItem.caseType}"

        type.textSize = 12f

        type.setTextColor(
            getColor(
                R.color.vs_secondary_text
            )
        )

        type.setPadding(
            0,
            5,
            0,
            0
        )

        val person =
            TextView(this)

        person.text =
            if (caseItem.person.isBlank()) {
                "Person / organisation: Not specified"
            } else {
                "Person / organisation: ${caseItem.person}"
            }

        person.textSize = 12f

        person.setTextColor(
            getColor(
                R.color.vs_secondary_text
            )
        )

        person.setPadding(
            0,
            3,
            0,
            0
        )

        val date =
            TextView(this)

        date.text =
            if (caseItem.nextDate.isBlank()) {
                "Next date: Not specified"
            } else {
                "Next date: ${caseItem.nextDate}"
            }

        date.textSize = 12f

        date.setTextColor(
            getColor(
                R.color.vs_secondary_text
            )
        )

        date.setPadding(
            0,
            3,
            0,
            0
        )

        val open =
            TextView(this)

        open.text =
            "Open case  →"

        open.textSize = 11f

        open.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        open.setTextColor(
            getColor(
                R.color.vs_navy
            )
        )

        open.setPadding(
            0,
            10,
            0,
            0
        )

        card.addView(title)
        card.addView(type)
        card.addView(person)
        card.addView(date)
        card.addView(open)

        card.setOnClickListener {

            showCaseDetails(caseItem)
        }

        binding.casesContainer.addView(card)
    }

    // =========================================================
    // CASE DETAILS
    // =========================================================

    private fun showCaseDetails(
        caseItem: CaseItem
    ) {

        val message =
            buildString {

                append("Case type:\n")
                append(caseItem.caseType)
                append("\n\n")

                append("Person / organisation:\n")

                if (caseItem.person.isBlank()) {
                    append("Not specified")
                } else {
                    append(caseItem.person)
                }

                append("\n\n")

                append("Description:\n")
                append(caseItem.description)

                append("\n\n")

                append("Next date:\n")

                if (caseItem.nextDate.isBlank()) {
                    append("Not specified")
                } else {
                    append(caseItem.nextDate)
                }
            }

        AlertDialog.Builder(this)
            .setTitle(caseItem.title)
            .setMessage(message)
            .setNegativeButton(
                "Close",
                null
            )
            .setNeutralButton(
                "Delete"
            ) { _, _ ->

                deleteCase(caseItem)
            }
            .setPositiveButton(
                "Edit"
            ) { _, _ ->

                showEditCaseDialog(caseItem)
            }
            .show()
    }

    // =========================================================
    // EDIT CASE
    // =========================================================

    private fun showEditCaseDialog(
        caseItem: CaseItem
    ) {

        val dialogView =
            LayoutInflater.from(this)
                .inflate(
                    R.layout.dialog_add_case,
                    null
                )

        val titleInput =
            dialogView.findViewById<EditText>(
                R.id.caseTitleInput
            )

        val personInput =
            dialogView.findViewById<EditText>(
                R.id.casePersonInput
            )

        val typeInput =
            dialogView.findViewById<EditText>(
                R.id.caseTypeInput
            )

        val descriptionInput =
            dialogView.findViewById<EditText>(
                R.id.caseDescriptionInput
            )

        val dateInput =
            dialogView.findViewById<EditText>(
                R.id.caseNextDateInput
            )

        titleInput.setText(
            caseItem.title
        )

        personInput.setText(
            caseItem.person
        )

        typeInput.setText(
            caseItem.caseType
        )

        descriptionInput.setText(
            caseItem.description
        )

        dateInput.setText(
            caseItem.nextDate
        )

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Edit Case")
                .setView(dialogView)
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton(
                    "Save Changes",
                    null
                )
                .create()

        dialog.setOnShowListener {

            val saveButton =
                dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
                )

            saveButton.setOnClickListener {

                val title =
                    titleInput.text.toString().trim()

                val person =
                    personInput.text.toString().trim()

                val type =
                    typeInput.text.toString().trim()

                val description =
                    descriptionInput.text.toString().trim()

                val date =
                    dateInput.text.toString().trim()

                if (title.isBlank()) {

                    titleInput.error =
                        "Enter a case title"

                    return@setOnClickListener
                }

                if (type.isBlank()) {

                    typeInput.error =
                        "Enter the case type"

                    return@setOnClickListener
                }

                if (description.isBlank()) {

                    descriptionInput.error =
                        "Enter a description"

                    return@setOnClickListener
                }

                caseItem.title =
                    title

                caseItem.person =
                    person

                caseItem.caseType =
                    type

                caseItem.description =
                    description

                caseItem.nextDate =
                    date

                saveCases()

                refreshCaseList()

                Toast.makeText(
                    this,
                    "Case updated",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // =========================================================
    // DELETE CASE
    // =========================================================

    private fun deleteCase(
        caseItem: CaseItem
    ) {

        AlertDialog.Builder(this)
            .setTitle("Delete case?")
            .setMessage(
                "This will permanently remove this case from this device."
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                cases.removeAll {
                    it.id == caseItem.id
                }

                saveCases()

                refreshCaseList()

                Toast.makeText(
                    this,
                    "Case deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    // =========================================================
    // SAVE CASES
    // =========================================================

    private fun saveCases() {

        val array =
            JSONArray()

        cases.forEach { caseItem ->

            val objectItem =
                JSONObject()

            objectItem.put(
                "id",
                caseItem.id
            )

            objectItem.put(
                "title",
                caseItem.title
            )

            objectItem.put(
                "person",
                caseItem.person
            )

            objectItem.put(
                "caseType",
                caseItem.caseType
            )

            objectItem.put(
                "description",
                caseItem.description
            )

            objectItem.put(
                "nextDate",
                caseItem.nextDate
            )

            array.put(
                objectItem
            )
        }

        getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                casesKey,
                array.toString()
            )
            .apply()
    }

    // =========================================================
    // LOAD CASES
    // =========================================================

    private fun loadCases() {

        cases.clear()

        val saved =
            getSharedPreferences(
                preferencesName,
                Context.MODE_PRIVATE
            )
                .getString(
                    casesKey,
                    null
                )

        if (saved.isNullOrBlank()) {
            return
        }

        try {

            val array =
                JSONArray(saved)

            for (index in 0 until array.length()) {

                val objectItem =
                    array.getJSONObject(index)

                cases.add(
                    CaseItem(
                        id = objectItem.optLong(
                            "id"
                        ),

                        title = objectItem.optString(
                            "title"
                        ),

                        person = objectItem.optString(
                            "person"
                        ),

                        caseType = objectItem.optString(
                            "caseType"
                        ),

                        description = objectItem.optString(
                            "description"
                        ),

                        nextDate = objectItem.optString(
                            "nextDate"
                        )
                    )
                )
            }

        } catch (_: Exception) {

            cases.clear()
        }
    }
}