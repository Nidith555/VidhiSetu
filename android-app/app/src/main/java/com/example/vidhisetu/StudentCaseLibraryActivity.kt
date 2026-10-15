package com.example.vidhisetu

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vidhisetu.databinding.ActivityStudentCaseLibraryBinding

class StudentCaseLibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentCaseLibraryBinding

    data class CaseInfo(
        val title: String,
        val court: String,
        val year: String,
        val area: String,
        val summary: String,
        val keywords: List<String>
    )

    private val cases = listOf(

        CaseInfo(
            title = "Kesavananda Bharati v. State of Kerala",
            court = "Supreme Court of India",
            year = "1973",
            area = "Constitutional Law",
            summary = "A major constitutional case concerning the extent of Parliament's power to amend the Constitution and the basic structure doctrine.",
            keywords = listOf(
                "constitution",
                "basic structure",
                "amendment",
                "constitutional law"
            )
        ),

        CaseInfo(
            title = "Maneka Gandhi v. Union of India",
            court = "Supreme Court of India",
            year = "1978",
            area = "Fundamental Rights",
            summary = "The case examined Article 21 and the relationship between personal liberty and the procedure established by law.",
            keywords = listOf(
                "article 21",
                "fundamental rights",
                "personal liberty",
                "constitution"
            )
        ),

        CaseInfo(
            title = "Vishaka v. State of Rajasthan",
            court = "Supreme Court of India",
            year = "1997",
            area = "Women & Workplace",
            summary = "The Supreme Court addressed sexual harassment at the workplace and laid down guidelines for prevention and redressal.",
            keywords = listOf(
                "women",
                "workplace",
                "sexual harassment",
                "employment"
            )
        ),

        CaseInfo(
            title = "Donoghue v. Stevenson",
            court = "House of Lords",
            year = "1932",
            area = "Law of Torts",
            summary = "A foundational common-law case associated with the modern duty of care and the neighbour principle.",
            keywords = listOf(
                "tort",
                "negligence",
                "duty of care"
            )
        ),

        CaseInfo(
            title = "Carlill v. Carbolic Smoke Ball Co.",
            court = "Court of Appeal",
            year = "1893",
            area = "Contract Law",
            summary = "A leading contract-law case concerning unilateral offers and acceptance through performance.",
            keywords = listOf(
                "contract",
                "offer",
                "acceptance"
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityStudentCaseLibraryBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
        showCases(cases)
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.searchButton.setOnClickListener {
            searchCases()
        }

        binding.searchInput.setOnEditorActionListener {
                _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCases()
                true
            } else {
                false
            }
        }
    }

    private fun searchCases() {

        val query =
            binding.searchInput.text
                .toString()
                .trim()
                .lowercase()

        if (query.isBlank()) {
            showCases(cases)
            return
        }

        val results =
            cases.filter { caseInfo ->

                caseInfo.title.lowercase()
                    .contains(query) ||

                        caseInfo.court.lowercase()
                            .contains(query) ||

                        caseInfo.area.lowercase()
                            .contains(query) ||

                        caseInfo.summary.lowercase()
                            .contains(query) ||

                        caseInfo.keywords.any {
                            it.lowercase().contains(query)
                        }
            }

        showCases(results)
    }

    private fun showCases(
        results: List<CaseInfo>
    ) {

        binding.resultsContainer.removeAllViews()

        if (results.isEmpty()) {

            binding.emptyResultText.visibility =
                android.view.View.VISIBLE

            binding.emptyResultText.text =
                "No cases matched your search."

            return
        }

        binding.emptyResultText.visibility =
            android.view.View.GONE

        results.forEach { caseInfo ->

            addCaseCard(caseInfo)
        }
    }

    private fun addCaseCard(
        caseInfo: CaseInfo
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
            caseInfo.title

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

        val metadata =
            TextView(this)

        metadata.text =
            "${caseInfo.court} • ${caseInfo.year}"

        metadata.textSize = 11f

        metadata.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_secondary_text
            )
        )

        metadata.setPadding(
            0,
            5,
            0,
            0
        )

        val area =
            TextView(this)

        area.text =
            caseInfo.area

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

        val action =
            TextView(this)

        action.text =
            "Read case summary  →"

        action.textSize = 11f

        action.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        action.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_navy
            )
        )

        action.setPadding(
            0,
            9,
            0,
            0
        )

        card.addView(title)
        card.addView(metadata)
        card.addView(area)
        card.addView(action)

        card.setOnClickListener {

            showCaseDetails(caseInfo)
        }

        binding.resultsContainer.addView(card)
    }

    private fun showCaseDetails(
        caseInfo: CaseInfo
    ) {

        val details =
            """
            Court:
            ${caseInfo.court}
            
            Year:
            ${caseInfo.year}
            
            Area:
            ${caseInfo.area}
            
            Summary:
            ${caseInfo.summary}
            """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(caseInfo.title)
            .setMessage(details)
            .setPositiveButton(
                "Close",
                null
            )
            .show()
    }
}