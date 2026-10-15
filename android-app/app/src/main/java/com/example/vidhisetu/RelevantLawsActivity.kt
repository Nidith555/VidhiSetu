package com.example.vidhisetu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vidhisetu.databinding.ActivityRelevantLawsBinding

class RelevantLawsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRelevantLawsBinding

    data class LawInfo(
        val shortName: String,
        val fullName: String,
        val description: String,
        val keywords: List<String>,
        val officialUrl: String
    )

    private val laws = listOf(

        LawInfo(
            shortName = "BNS 2023",
            fullName = "Bharatiya Nyaya Sanhita, 2023",
            description = "India Code entry for the Bharatiya Nyaya Sanhita, 2023.",
            keywords = listOf(
                "bns",
                "criminal",
                "offence",
                "offense",
                "crime",
                "theft",
                "cheating",
                "assault",
                "murder",
                "criminal law"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/handle/123456789/20062?col=123456789%2F1362&view_type=search"
        ),

        LawInfo(
            shortName = "BNSS 2023",
            fullName = "Bharatiya Nagarik Suraksha Sanhita, 2023",
            description = "India Code entry for the Bharatiya Nagarik Suraksha Sanhita, 2023.",
            keywords = listOf(
                "bnss",
                "criminal procedure",
                "procedure",
                "fir",
                "investigation",
                "arrest",
                "bail",
                "court procedure"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/handle/123456789/20099?view_type=browse"
        ),

        LawInfo(
            shortName = "BSA 2023",
            fullName = "Bharatiya Sakshya Adhiniyam, 2023",
            description = "India Code entry for the Bharatiya Sakshya Adhiniyam, 2023.",
            keywords = listOf(
                "bsa",
                "evidence",
                "evidence law",
                "proof",
                "witness",
                "documentary evidence"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/handle/123456789/20063?col=123456789%2F1362&view_type=search"
        ),

        LawInfo(
            shortName = "Constitution of India",
            fullName = "Constitution of India",
            description = "Search the official India Code collection for the Constitution of India.",
            keywords = listOf(
                "constitution",
                "fundamental rights",
                "fundamental right",
                "article",
                "constitutional",
                "right"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/"
        ),

        LawInfo(
            shortName = "Consumer Protection Act",
            fullName = "Consumer Protection Act, 2019",
            description = "Search the official India Code source for the Consumer Protection Act, 2019.",
            keywords = listOf(
                "consumer",
                "consumer complaint",
                "product",
                "service",
                "refund",
                "defective product",
                "seller",
                "purchase"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/"
        ),

        LawInfo(
            shortName = "Information Technology Act",
            fullName = "Information Technology Act, 2000",
            description = "Search the official India Code source for the Information Technology Act, 2000.",
            keywords = listOf(
                "cyber",
                "cybercrime",
                "online fraud",
                "hacking",
                "computer",
                "internet",
                "technology",
                "data"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/"
        ),

        LawInfo(
            shortName = "RTI Act",
            fullName = "Right to Information Act, 2005",
            description = "Search the official India Code source for the Right to Information Act, 2005.",
            keywords = listOf(
                "rti",
                "information",
                "government information",
                "public information"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/"
        ),

        LawInfo(
            shortName = "Motor Vehicles Act",
            fullName = "Motor Vehicles Act, 1988",
            description = "Search the official India Code source for the Motor Vehicles Act, 1988.",
            keywords = listOf(
                "vehicle",
                "motor vehicle",
                "driving",
                "driving licence",
                "license",
                "traffic",
                "road accident",
                "accident"
            ),
            officialUrl =
                "https://www.indiacode.nic.in/indiacode/"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRelevantLawsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.searchButton.setOnClickListener {
            performSearch()
        }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        binding.bnsCard.setOnClickListener {
            openLawDetails(
                laws.first { it.shortName == "BNS 2023" }
            )
        }

        binding.bnssCard.setOnClickListener {
            openLawDetails(
                laws.first { it.shortName == "BNSS 2023" }
            )
        }

        binding.bsaCard.setOnClickListener {
            openLawDetails(
                laws.first { it.shortName == "BSA 2023" }
            )
        }

        binding.constitutionCard.setOnClickListener {
            openLawDetails(
                laws.first { it.shortName == "Constitution of India" }
            )
        }
    }

    private fun performSearch() {

        val query =
            binding.searchInput.text
                .toString()
                .trim()

        if (query.isBlank()) {

            binding.resultsTitle.visibility =
                android.view.View.GONE

            binding.resultsContainer.removeAllViews()

            binding.emptyResultText.visibility =
                android.view.View.VISIBLE

            binding.emptyResultText.text =
                "Enter a law name or legal topic to search."

            return
        }

        val normalizedQuery =
            query.lowercase()

        val matches =
            laws.filter { law ->

                law.shortName.lowercase()
                    .contains(normalizedQuery) ||

                        law.fullName.lowercase()
                            .contains(normalizedQuery) ||

                        law.keywords.any { keyword ->

                            normalizedQuery.contains(
                                keyword.lowercase()
                            ) ||
                                    keyword.lowercase()
                                        .contains(normalizedQuery)
                        }
            }

        showSearchResults(
            query,
            matches
        )
    }

    private fun showSearchResults(
        query: String,
        matches: List<LawInfo>
    ) {

        binding.resultsContainer.removeAllViews()

        binding.resultsTitle.visibility =
            android.view.View.VISIBLE

        binding.emptyResultText.visibility =
            android.view.View.GONE

        if (matches.isEmpty()) {

            binding.resultsTitle.text =
                "No matching law found"

            binding.emptyResultText.visibility =
                android.view.View.VISIBLE

            binding.emptyResultText.text =
                """
                We couldn't match "$query" with the laws currently available in this screen.

                You can search the official India Code website for a broader result.
                """.trimIndent()

            return
        }

        binding.resultsTitle.text =
            "Relevant laws"

        matches.forEach { law ->

            addLawResultCard(law)
        }
    }

    private fun addLawResultCard(
        law: LawInfo
    ) {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            16,
            14,
            16,
            14
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

        params.bottomMargin =
            10

        card.layoutParams =
            params

        val title =
            TextView(this)

        title.text =
            law.shortName

        title.textSize =
            15f

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

        val fullName =
            TextView(this)

        fullName.text =
            law.fullName

        fullName.textSize =
            12f

        fullName.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.vs_secondary_text
            )
        )

        fullName.setPadding(
            0,
            5,
            0,
            0
        )

        val action =
            TextView(this)

        action.text =
            "View law details  →"

        action.textSize =
            11f

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
        card.addView(fullName)
        card.addView(action)

        card.setOnClickListener {
            openLawDetails(law)
        }

        binding.resultsContainer.addView(card)
    }

    private fun openLawDetails(
        law: LawInfo
    ) {

        val intent =
            Intent(
                this,
                LawDetailsActivity::class.java
            )

        intent.putExtra(
            "law_short_name",
            law.shortName
        )

        intent.putExtra(
            "law_full_name",
            law.fullName
        )

        intent.putExtra(
            "law_description",
            law.description
        )

        intent.putExtra(
            "law_url",
            law.officialUrl
        )

        startActivity(intent)
    }
}