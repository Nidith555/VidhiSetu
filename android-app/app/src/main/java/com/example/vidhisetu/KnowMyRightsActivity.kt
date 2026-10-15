package com.example.vidhisetu

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityKnowMyRightsBinding

class KnowMyRightsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKnowMyRightsBinding

    data class RightInformation(
        val title: String,
        val description: String,
        val keywords: List<String>
    )

    private val rightsDatabase = listOf(

        RightInformation(
            title = "Family & Marriage",
            description = """
                This section provides general information about legal
                issues relating to marriage, family relationships,
                children, maintenance, separation and related matters.

                VidhiSetu can later identify the specific law applicable
                to your situation based on the facts you provide.
            """.trimIndent(),
            keywords = listOf(
                "family",
                "marriage",
                "husband",
                "wife",
                "divorce",
                "child",
                "maintenance",
                "separation"
            )
        ),

        RightInformation(
            title = "Property & Land",
            description = """
                This section covers general information about property
                and land-related matters.

                Examples include ownership, possession, boundaries,
                agreements, rent, inheritance and property disputes.

                The applicable law depends on the facts and the type
                of property involved.
            """.trimIndent(),
            keywords = listOf(
                "property",
                "land",
                "house",
                "plot",
                "boundary",
                "ownership",
                "possession",
                "rent",
                "landlord",
                "tenant",
                "inheritance"
            )
        ),

        RightInformation(
            title = "Employment & Workplace",
            description = """
                This section provides general information about
                employment and workplace-related legal concerns.

                Examples include wages, employment conditions,
                termination, workplace issues and employment records.

                The applicable protections can depend on the nature
                of employment and the circumstances of the matter.
            """.trimIndent(),
            keywords = listOf(
                "job",
                "employment",
                "employee",
                "employer",
                "salary",
                "wage",
                "work",
                "workplace",
                "fired",
                "termination",
                "office"
            )
        ),

        RightInformation(
            title = "Consumer Rights",
            description = """
                Consumer law provides protections relating to goods
                and services.

                The Consumer Protection Act, 2019 includes consumer
                rights relating to protection against hazardous goods
                and services, information, being heard, redressal and
                consumer awareness.

                VidhiSetu will later connect this section directly to
                verified statutory text and applicable procedures.
            """.trimIndent(),
            keywords = listOf(
                "consumer",
                "product",
                "service",
                "shop",
                "seller",
                "refund",
                "defective",
                "warranty",
                "online order",
                "purchase"
            )
        ),

        RightInformation(
            title = "Police & Complaints",
            description = """
                This section provides general information about
                reporting incidents and interacting with police
                authorities.

                The correct procedure depends on what happened,
                where it happened and the nature of the complaint.

                VidhiSetu will later connect this section to the
                applicable criminal-law and procedural provisions.
            """.trimIndent(),
            keywords = listOf(
                "police",
                "complaint",
                "fir",
                "crime",
                "stolen",
                "theft",
                "threat",
                "incident",
                "report"
            )
        ),

        RightInformation(
            title = "Women & Safety",
            description = """
                This section provides general information about
                legal protections and support relating to women's
                safety and related concerns.

                The applicable law depends on the circumstances
                of the situation.

                VidhiSetu will later identify the relevant legal
                provisions from its verified legal knowledge base.
            """.trimIndent(),
            keywords = listOf(
                "woman",
                "women",
                "female",
                "safety",
                "harassment",
                "domestic",
                "violence",
                "stalking"
            )
        ),

        RightInformation(
            title = "Children & Education",
            description = """
                This section provides general information about
                children's legal protections, education and safety.

                The applicable legal framework depends on the child's
                situation and the issue involved.

                VidhiSetu will later connect this section to the
                relevant verified laws and official sources.
            """.trimIndent(),
            keywords = listOf(
                "child",
                "children",
                "student",
                "school",
                "education",
                "minor",
                "guardian",
                "safety"
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityKnowMyRightsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.searchRightsButton.setOnClickListener {
            performSearch()
        }

        binding.searchRightsInput.setOnEditorActionListener {
                _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                performSearch()

                true

            } else {

                false
            }
        }

        binding.familyRightsButton.setOnClickListener {
            openRightInformation("Family & Marriage")
        }

        binding.propertyRightsButton.setOnClickListener {
            openRightInformation("Property & Land")
        }

        binding.employmentRightsButton.setOnClickListener {
            openRightInformation("Employment & Workplace")
        }

        binding.consumerRightsButton.setOnClickListener {
            openRightInformation("Consumer Rights")
        }

        binding.policeRightsButton.setOnClickListener {
            openRightInformation("Police & Complaints")
        }

        binding.womenRightsButton.setOnClickListener {
            openRightInformation("Women & Safety")
        }

        binding.childrenRightsButton.setOnClickListener {
            openRightInformation("Children & Education")
        }
    }

    private fun performSearch() {

        val query =
            binding.searchRightsInput.text
                .toString()
                .trim()

        if (query.isBlank()) {

            Toast.makeText(
                this,
                "Enter something to search.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val normalizedQuery =
            query.lowercase()

        val matches =
            rightsDatabase.filter { right ->

                right.keywords.any { keyword ->

                    normalizedQuery.contains(
                        keyword.lowercase()
                    )

                } ||

                        right.title.lowercase()
                            .contains(normalizedQuery)
            }

        when {

            matches.size == 1 -> {

                openRightInformation(
                    matches.first().title
                )
            }

            matches.size > 1 -> {

                showSearchChoices(
                    matches
                )
            }

            else -> {

                Toast.makeText(
                    this,
                    "No matching legal topic found.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showSearchChoices(
        matches: List<RightInformation>
    ) {

        val titles =
            matches.map {
                it.title
            }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Choose a topic")
            .setItems(titles) { _, which ->

                openRightInformation(
                    matches[which].title
                )
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun openRightInformation(
        title: String
    ) {

        val selected =
            rightsDatabase.firstOrNull {

                it.title.equals(
                    title,
                    ignoreCase = true
                )
            }

        if (selected == null) {
            return
        }

        val intent =
            Intent(
                this,
                RightInformationActivity::class.java
            )

        intent.putExtra(
            "RIGHT_TITLE",
            selected.title
        )

        intent.putExtra(
            "RIGHT_INFORMATION",
            selected.description
        )

        startActivity(intent)
    }
}