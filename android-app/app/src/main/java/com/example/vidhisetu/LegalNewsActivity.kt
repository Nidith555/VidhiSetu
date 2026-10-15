package com.example.vidhisetu

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityLegalNewsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LegalNewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLegalNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLegalNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
        loadLegalNews()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.refreshButton.setOnClickListener {
            loadLegalNews()
        }
    }

    private fun loadLegalNews() {

        showLoading()

        RetrofitClient.legalNewsApi
            .getLegalNews()
            .enqueue(object : Callback<LegalNewsResponse> {

                override fun onResponse(
                    call: Call<LegalNewsResponse>,
                    response: Response<LegalNewsResponse>
                ) {

                    if (response.isSuccessful &&
                        response.body() != null
                    ) {

                        val result = response.body()!!

                        if (result.success &&
                            result.news.isNotEmpty()
                        ) {

                            displayNews(result.news)

                        } else {

                            showEmpty()
                        }

                    } else {

                        showError(
                            "Unable to load legal news."
                        )
                    }
                }

                override fun onFailure(
                    call: Call<LegalNewsResponse>,
                    t: Throwable
                ) {

                    showError(
                        "Unable to connect to VidhiSetu server."
                    )
                }
            })
    }

    private fun displayNews(
        newsList: List<LegalNewsItem>
    ) {

        binding.loadingText.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.emptyText.visibility = View.GONE
        binding.newsContainer.visibility = View.VISIBLE

        binding.newsContainer.removeAllViews()

        for (news in newsList) {

            val card = createNewsCard(news)

            binding.newsContainer.addView(card)
        }
    }

    private fun createNewsCard(
        news: LegalNewsItem
    ): View {

        val card = LinearLayout(this)

        card.orientation = LinearLayout.VERTICAL

        card.setPadding(
            dp(18),
            dp(18),
            dp(18),
            dp(18)
        )

        card.background =
            getDrawable(R.drawable.simple_right_card)

        val cardParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        cardParams.setMargins(
            0,
            0,
            0,
            dp(14)
        )

        card.layoutParams = cardParams


        // CATEGORY

        val category = TextView(this)

        category.text =
            news.category.uppercase()

        category.textSize = 12f

        category.setTypeface(
            null,
            Typeface.BOLD
        )

        category.setTextColor(
            getColor(R.color.vs_gold)
        )

        card.addView(category)


        // TITLE

        val title = TextView(this)

        title.text = news.title

        title.textSize = 18f

        title.setTypeface(
            null,
            Typeface.BOLD
        )

        title.setTextColor(
            getColor(R.color.vs_primary_text)
        )

        val titleParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        titleParams.setMargins(
            0,
            dp(7),
            0,
            dp(8)
        )

        title.layoutParams = titleParams

        card.addView(title)


        // SUMMARY

        val summary = TextView(this)

        summary.text = news.summary

        summary.textSize = 14f

        summary.setTextColor(
            getColor(R.color.vs_secondary_text)
        )

        card.addView(summary)


        // SOURCE + DATE

        val source = TextView(this)

        val date =
            if (news.date.isNotBlank()) {
                " • ${news.date}"
            } else {
                ""
            }

        source.text =
            "${news.source}$date"

        source.textSize = 12f

        source.setTextColor(
            getColor(R.color.vs_secondary_text)
        )

        val sourceParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        sourceParams.setMargins(
            0,
            dp(12),
            0,
            0
        )

        source.layoutParams = sourceParams

        card.addView(source)


        // OPEN SOURCE

        val readMore = TextView(this)

        readMore.text = "Read official source →"

        readMore.textSize = 13f

        readMore.setTypeface(
            null,
            Typeface.BOLD
        )

        readMore.setTextColor(
            getColor(R.color.vs_navy)
        )

        val readMoreParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        readMoreParams.setMargins(
            0,
            dp(12),
            0,
            0
        )

        readMore.layoutParams = readMoreParams

        card.addView(readMore)


        // CLICK CARD

        card.setOnClickListener {

            openSource(news.url)
        }


        readMore.setOnClickListener {

            openSource(news.url)
        }


        return card
    }

    private fun openSource(url: String) {

        if (url.isBlank()) {

            Toast.makeText(
                this,
                "Official source unavailable",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        try {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )

            startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open source",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showLoading() {

        binding.loadingText.visibility = View.VISIBLE

        binding.errorText.visibility = View.GONE

        binding.emptyText.visibility = View.GONE

        binding.newsContainer.visibility = View.GONE
    }

    private fun showError(message: String) {

        binding.loadingText.visibility = View.GONE

        binding.errorText.visibility = View.VISIBLE

        binding.emptyText.visibility = View.GONE

        binding.newsContainer.visibility = View.GONE

        binding.errorText.text = message
    }

    private fun showEmpty() {

        binding.loadingText.visibility = View.GONE

        binding.errorText.visibility = View.GONE

        binding.emptyText.visibility = View.VISIBLE

        binding.newsContainer.visibility = View.GONE
    }

    private fun dp(value: Int): Int {

        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }
}