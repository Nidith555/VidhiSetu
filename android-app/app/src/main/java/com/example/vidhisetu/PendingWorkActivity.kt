package com.example.vidhisetu

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityPendingWorkBinding
import org.json.JSONArray
import org.json.JSONObject

class PendingWorkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingWorkBinding

    private val preferencesName = "lawyer_pending_work"
    private val listKey = "pending_items"

    private val pendingItems = mutableListOf<PendingItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPendingWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadItems()
        setupClicks()
        renderItems()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.addWorkButton.setOnClickListener {
            showAddWorkDialog()
        }

        binding.clearSearchButton.setOnClickListener {
            binding.searchInput.text?.clear()
            renderItems()
        }

        binding.searchButton.setOnClickListener {
            renderItems()
        }
    }

    private fun showAddWorkDialog() {

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(40, 10, 40, 0)

        val titleInput = EditText(this)
        titleInput.hint = "Work title"

        val clientInput = EditText(this)
        clientInput.hint = "Client / matter"

        val dueDateInput = EditText(this)
        dueDateInput.hint = "Due date"

        val priorityInput = EditText(this)
        priorityInput.hint = "Priority: High / Medium / Low"

        container.addView(titleInput)
        container.addView(clientInput)
        container.addView(dueDateInput)
        container.addView(priorityInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Pending Work")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val title =
                        titleInput.text.toString().trim()

                    val client =
                        clientInput.text.toString().trim()

                    val dueDate =
                        dueDateInput.text.toString().trim()

                    val priority =
                        priorityInput.text.toString().trim()

                    if (title.isEmpty()) {
                        titleInput.error = "Enter work title"
                        return@setOnClickListener
                    }

                    if (client.isEmpty()) {
                        clientInput.error = "Enter client / matter"
                        return@setOnClickListener
                    }

                    val item = PendingItem(
                        id = System.currentTimeMillis().toString(),
                        title = title,
                        client = client,
                        dueDate = if (dueDate.isEmpty()) {
                            "Not specified"
                        } else {
                            dueDate
                        },
                        priority = if (priority.isEmpty()) {
                            "Medium"
                        } else {
                            priority
                        },
                        completed = false
                    )

                    pendingItems.add(item)

                    saveItems()
                    renderItems()

                    Toast.makeText(
                        this,
                        "Pending work added",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    private fun renderItems() {

        binding.workContainer.removeAllViews()

        val query =
            binding.searchInput.text.toString()
                .trim()
                .lowercase()

        val filteredItems =
            pendingItems.filter { item ->

                query.isEmpty() ||
                        item.title.lowercase().contains(query) ||
                        item.client.lowercase().contains(query) ||
                        item.priority.lowercase().contains(query)
            }

        binding.workCountText.text =
            "${filteredItems.size} pending item(s)"

        if (filteredItems.isEmpty()) {

            binding.emptyText.visibility = View.VISIBLE

            if (pendingItems.isEmpty()) {
                binding.emptyText.text =
                    "No pending work yet.\nTap + to add your first task."
            } else {
                binding.emptyText.text =
                    "No pending work matches your search."
            }

            return
        }

        binding.emptyText.visibility = View.GONE

        filteredItems.forEach { item ->
            addWorkCard(item)
        }
    }

    private fun addWorkCard(item: PendingItem) {

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(18, 16, 18, 16)
        card.background =
            getDrawable(R.drawable.profile_option_card)

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(0, 0, 0, 12)

        card.layoutParams = params

        val title = TextView(this)
        title.text = item.title
        title.textSize = 16f
        title.setTextColor(
            getColor(R.color.vs_primary_text)
        )
        title.setTypeface(null, android.graphics.Typeface.BOLD)

        val client = TextView(this)
        client.text = "Matter: ${item.client}"
        client.textSize = 12f
        client.setTextColor(
            getColor(R.color.vs_secondary_text)
        )

        val dueDate = TextView(this)
        dueDate.text = "Due: ${item.dueDate}"
        dueDate.textSize = 12f
        dueDate.setTextColor(
            getColor(R.color.vs_secondary_text)
        )

        val priority = TextView(this)
        priority.text = "Priority: ${item.priority}"
        priority.textSize = 12f
        priority.setTextColor(
            getColor(R.color.vs_secondary_text)
        )

        card.addView(title)
        card.addView(client)
        card.addView(dueDate)
        card.addView(priority)

        card.setOnClickListener {
            showWorkDetails(item)
        }

        binding.workContainer.addView(card)
    }

    private fun showWorkDetails(item: PendingItem) {

        val message = buildString {
            append("Work: ${item.title}\n\n")
            append("Matter: ${item.client}\n")
            append("Due: ${item.dueDate}\n")
            append("Priority: ${item.priority}")
        }

        AlertDialog.Builder(this)
            .setTitle("Pending Work")
            .setMessage(message)
            .setNegativeButton("Close", null)
            .setNeutralButton(
                if (item.completed) "Mark Pending"
                else "Mark Complete"
            ) { _, _ ->

                item.completed = !item.completed

                if (item.completed) {
                    pendingItems.remove(item)
                    saveItems()
                    renderItems()

                    Toast.makeText(
                        this,
                        "Work marked complete",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setPositiveButton("Delete") { _, _ ->

                pendingItems.remove(item)
                saveItems()
                renderItems()

                Toast.makeText(
                    this,
                    "Pending work deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun saveItems() {

        val array = JSONArray()

        pendingItems.forEach { item ->

            val objectItem = JSONObject()

            objectItem.put("id", item.id)
            objectItem.put("title", item.title)
            objectItem.put("client", item.client)
            objectItem.put("dueDate", item.dueDate)
            objectItem.put("priority", item.priority)
            objectItem.put("completed", item.completed)

            array.put(objectItem)
        }

        getSharedPreferences(
            preferencesName,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                listKey,
                array.toString()
            )
            .apply()
    }

    private fun loadItems() {

        val json =
            getSharedPreferences(
                preferencesName,
                MODE_PRIVATE
            )
                .getString(listKey, null)
                ?: return

        try {

            val array = JSONArray(json)

            for (i in 0 until array.length()) {

                val objectItem =
                    array.getJSONObject(i)

                pendingItems.add(
                    PendingItem(
                        id = objectItem.optString("id"),
                        title = objectItem.optString("title"),
                        client = objectItem.optString("client"),
                        dueDate = objectItem.optString("dueDate"),
                        priority = objectItem.optString("priority"),
                        completed = objectItem.optBoolean(
                            "completed",
                            false
                        )
                    )
                )
            }

        } catch (_: Exception) {
            pendingItems.clear()
        }
    }

    data class PendingItem(
        val id: String,
        val title: String,
        val client: String,
        val dueDate: String,
        val priority: String,
        var completed: Boolean
    )
}