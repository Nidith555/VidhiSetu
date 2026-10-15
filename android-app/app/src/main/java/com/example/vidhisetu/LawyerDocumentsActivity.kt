package com.example.vidhisetu

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityLawyerDocumentsBinding
import org.json.JSONArray
import org.json.JSONObject

class LawyerDocumentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLawyerDocumentsBinding

    private val preferencesName = "lawyer_documents"
    private val documentsKey = "documents"

    private val documents = mutableListOf<LegalDocument>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLawyerDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadDocuments()
        setupClicks()
        displayDocuments()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addDocumentButton.setOnClickListener {
            showAddDocumentDialog()
        }

        binding.searchInput.setOnEditorActionListener { _, _, _ ->
            displayDocuments(
                binding.searchInput.text.toString().trim()
            )
            true
        }

        binding.searchButton.setOnClickListener {
            displayDocuments(
                binding.searchInput.text.toString().trim()
            )
        }

        binding.clearSearchButton.setOnClickListener {
            binding.searchInput.text?.clear()
            displayDocuments()
        }
    }

    // =========================================================
    // ADD DOCUMENT
    // =========================================================

    private fun showAddDocumentDialog() {

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(45, 10, 45, 5)

        val titleInput = EditText(this)
        titleInput.hint = "Document title"

        val categoryInput = EditText(this)
        categoryInput.hint = "Category"

        val descriptionInput = EditText(this)
        descriptionInput.hint = "Short description"
        descriptionInput.minLines = 3

        container.addView(titleInput)
        container.addView(categoryInput)
        container.addView(descriptionInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Document")
            .setView(container)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val title =
                        titleInput.text.toString().trim()

                    val category =
                        categoryInput.text.toString().trim()

                    val description =
                        descriptionInput.text.toString().trim()

                    if (title.isEmpty()) {
                        titleInput.error =
                            "Enter a document title"
                        return@setOnClickListener
                    }

                    val finalCategory =
                        if (category.isEmpty()) {
                            "General"
                        } else {
                            category
                        }

                    documents.add(
                        LegalDocument(
                            title = title,
                            category = finalCategory,
                            description = description,
                            important = false
                        )
                    )

                    saveDocuments()
                    displayDocuments()

                    Toast.makeText(
                        this,
                        "Document added",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    // =========================================================
    // DISPLAY DOCUMENTS
    // =========================================================

    private fun displayDocuments(
        searchQuery: String = ""
    ) {

        binding.documentsContainer.removeAllViews()

        val filteredDocuments =
            if (searchQuery.isEmpty()) {
                documents
            } else {
                documents.filter {
                    it.title.contains(
                        searchQuery,
                        ignoreCase = true
                    ) ||
                            it.category.contains(
                                searchQuery,
                                ignoreCase = true
                            ) ||
                            it.description.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                }
            }

        binding.documentCountText.text =
            "${filteredDocuments.size} document(s)"

        if (filteredDocuments.isEmpty()) {

            binding.emptyText.visibility = View.VISIBLE

            binding.emptyText.text =
                if (searchQuery.isEmpty()) {
                    "No documents added yet.\nTap + Add Document to create your first document."
                } else {
                    "No documents found for \"$searchQuery\"."
                }

            return
        }

        binding.emptyText.visibility = View.GONE

        filteredDocuments.forEach { document ->
            addDocumentCard(document)
        }
    }

    // =========================================================
    // DOCUMENT CARD
    // =========================================================

    private fun addDocumentCard(
        document: LegalDocument
    ) {

        val card = LinearLayout(this)

        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = android.view.Gravity.CENTER_VERTICAL
        card.setPadding(
            14,
            12,
            10,
            12
        )

        card.background =
            getDrawable(R.drawable.profile_option_card)

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            0,
            0,
            8
        )

        card.layoutParams = params

        // Icon
        val icon = TextView(this)

        icon.text =
            if (document.important) {
                "⭐"
            } else {
                "📄"
            }

        icon.textSize = 23f

        icon.gravity = android.view.Gravity.CENTER

        val iconParams =
            LinearLayout.LayoutParams(
                48,
                48
            )

        icon.background =
            getDrawable(R.drawable.service_icon_background)

        card.addView(
            icon,
            iconParams
        )

        // Text container
        val textContainer =
            LinearLayout(this)

        textContainer.orientation =
            LinearLayout.VERTICAL

        val textParams =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

        textParams.setMargins(
            12,
            0,
            8,
            0
        )

        // Title
        val title =
            TextView(this)

        title.text = document.title

        title.textSize = 14f

        title.setTextColor(
            getColor(R.color.vs_primary_text)
        )

        title.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        // Category
        val category =
            TextView(this)

        category.text =
            document.category

        category.textSize = 10f

        category.setTextColor(
            getColor(R.color.vs_secondary_text)
        )

        category.setPadding(
            0,
            3,
            0,
            0
        )

        // Description
        val description =
            TextView(this)

        description.text =
            if (document.description.isEmpty()) {
                "No description"
            } else {
                document.description
            }

        description.textSize = 9f

        description.setTextColor(
            getColor(R.color.vs_secondary_text)
        )

        description.setPadding(
            0,
            3,
            0,
            0
        )

        textContainer.addView(title)
        textContainer.addView(category)
        textContainer.addView(description)

        card.addView(
            textContainer,
            textParams
        )

        // More button
        val moreButton =
            TextView(this)

        moreButton.text = "⋮"
        moreButton.textSize = 24f
        moreButton.gravity =
            android.view.Gravity.CENTER

        val moreParams =
            LinearLayout.LayoutParams(
                32,
                50
            )

        card.addView(
            moreButton,
            moreParams
        )

        card.setOnClickListener {
            showDocumentDetails(document)
        }

        moreButton.setOnClickListener {
            showDocumentMenu(document)
        }

        binding.documentsContainer.addView(card)
    }

    // =========================================================
    // DOCUMENT DETAILS
    // =========================================================

    private fun showDocumentDetails(
        document: LegalDocument
    ) {

        val message = buildString {

            append("Title\n")
            append(document.title)

            append("\n\nCategory\n")
            append(document.category)

            append("\n\nDescription\n")

            append(
                if (document.description.isEmpty()) {
                    "No description provided."
                } else {
                    document.description
                }
            )

            append("\n\nStatus\n")

            append(
                if (document.important) {
                    "Important document"
                } else {
                    "Regular document"
                }
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Document Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    // =========================================================
    // DOCUMENT MENU
    // =========================================================

    private fun showDocumentMenu(
        document: LegalDocument
    ) {

        val options = arrayOf(
            if (document.important) {
                "Remove Important"
            } else {
                "Mark Important"
            },
            "Delete Document"
        )

        AlertDialog.Builder(this)
            .setTitle(document.title)
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
                        document.important =
                            !document.important

                        saveDocuments()
                        displayDocuments()

                        Toast.makeText(
                            this,
                            if (document.important) {
                                "Marked as important"
                            } else {
                                "Removed from important"
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    1 -> {
                        confirmDelete(document)
                    }
                }
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    // =========================================================
    // DELETE
    // =========================================================

    private fun confirmDelete(
        document: LegalDocument
    ) {

        AlertDialog.Builder(this)
            .setTitle("Delete Document?")
            .setMessage(
                "Are you sure you want to delete \"${document.title}\"?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                documents.remove(document)

                saveDocuments()
                displayDocuments()

                Toast.makeText(
                    this,
                    "Document deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    // =========================================================
    // SAVE
    // =========================================================

    private fun saveDocuments() {

        val array = JSONArray()

        documents.forEach { document ->

            val objectData = JSONObject()

            objectData.put(
                "title",
                document.title
            )

            objectData.put(
                "category",
                document.category
            )

            objectData.put(
                "description",
                document.description
            )

            objectData.put(
                "important",
                document.important
            )

            array.put(objectData)
        }

        getSharedPreferences(
            preferencesName,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                documentsKey,
                array.toString()
            )
            .apply()
    }

    // =========================================================
    // LOAD
    // =========================================================

    private fun loadDocuments() {

        documents.clear()

        val jsonString =
            getSharedPreferences(
                preferencesName,
                MODE_PRIVATE
            )
                .getString(
                    documentsKey,
                    null
                )
                ?: return

        try {

            val array =
                JSONArray(jsonString)

            for (i in 0 until array.length()) {

                val objectData =
                    array.getJSONObject(i)

                documents.add(
                    LegalDocument(
                        title =
                            objectData.optString(
                                "title"
                            ),

                        category =
                            objectData.optString(
                                "category",
                                "General"
                            ),

                        description =
                            objectData.optString(
                                "description"
                            ),

                        important =
                            objectData.optBoolean(
                                "important",
                                false
                            )
                    )
                )
            }

        } catch (_: Exception) {
            documents.clear()
        }
    }
}


// =============================================================
// DATA MODEL
// =============================================================

data class LegalDocument(
    val title: String,
    val category: String,
    val description: String,
    var important: Boolean
)