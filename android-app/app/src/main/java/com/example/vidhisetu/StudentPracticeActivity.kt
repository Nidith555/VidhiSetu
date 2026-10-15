package com.example.vidhisetu

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vidhisetu.databinding.ActivityStudentPracticeBinding

class StudentPracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentPracticeBinding

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int
    )

    private val questions = listOf(

        Question(
            question = "Which document is the supreme law of India?",
            options = listOf(
                "Indian Penal Code",
                "Constitution of India",
                "BNS",
                "BNSS"
            ),
            correctAnswer = 1
        ),

        Question(
            question = "What does BNS primarily deal with?",
            options = listOf(
                "Civil procedure",
                "Evidence",
                "Criminal offences",
                "Taxation"
            ),
            correctAnswer = 2
        ),

        Question(
            question = "What does BNSS primarily deal with?",
            options = listOf(
                "Criminal procedure",
                "Contracts",
                "Property registration",
                "Company law"
            ),
            correctAnswer = 0
        ),

        Question(
            question = "What does BSA primarily concern?",
            options = listOf(
                "Evidence",
                "Taxation",
                "Motor vehicles",
                "Employment"
            ),
            correctAnswer = 0
        ),

        Question(
            question = "Which institution is the highest judicial authority in India?",
            options = listOf(
                "High Court",
                "District Court",
                "Supreme Court",
                "Tribunal"
            ),
            correctAnswer = 2
        )
    )

    private var currentQuestion = 0
    private var score = 0
    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityStudentPracticeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupClicks()
        showQuestion()
    }

    private fun setupClicks() {

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.optionA.setOnClickListener {
            selectAnswer(0)
        }

        binding.optionB.setOnClickListener {
            selectAnswer(1)
        }

        binding.optionC.setOnClickListener {
            selectAnswer(2)
        }

        binding.optionD.setOnClickListener {
            selectAnswer(3)
        }

        binding.nextButton.setOnClickListener {

            if (!answered) {
                Toast.makeText(
                    this,
                    "Select an answer first",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (currentQuestion < questions.lastIndex) {

                currentQuestion++
                answered = false
                showQuestion()

            } else {

                showFinalResult()
            }
        }
    }

    private fun showQuestion() {

        val question =
            questions[currentQuestion]

        binding.questionNumber.text =
            "Question ${currentQuestion + 1} of ${questions.size}"

        binding.questionText.text =
            question.question

        binding.optionA.text =
            question.options[0]

        binding.optionB.text =
            question.options[1]

        binding.optionC.text =
            question.options[2]

        binding.optionD.text =
            question.options[3]

        binding.feedbackText.text = ""

        binding.nextButton.text =
            if (currentQuestion == questions.lastIndex) {
                "Finish"
            } else {
                "Next Question"
            }

        enableOptions()
    }

    private fun selectAnswer(
        selectedAnswer: Int
    ) {

        if (answered) {
            return
        }

        answered = true

        val question =
            questions[currentQuestion]

        if (selectedAnswer ==
            question.correctAnswer
        ) {

            score++

            binding.feedbackText.text =
                "✓ Correct answer"

        } else {

            binding.feedbackText.text =
                "✗ Incorrect. Correct answer: " +
                        question.options[question.correctAnswer]
        }

        disableOptions()
    }

    private fun enableOptions() {

        binding.optionA.isEnabled = true
        binding.optionB.isEnabled = true
        binding.optionC.isEnabled = true
        binding.optionD.isEnabled = true
    }

    private fun disableOptions() {

        binding.optionA.isEnabled = false
        binding.optionB.isEnabled = false
        binding.optionC.isEnabled = false
        binding.optionD.isEnabled = false
    }

    private fun showFinalResult() {

        val percentage =
            (score * 100) / questions.size

        binding.questionNumber.text =
            "Practice Complete"

        binding.questionText.text =
            "Your Score: $score / ${questions.size}"

        binding.optionA.text =
            "Percentage: $percentage%"

        binding.optionB.text =
            "Correct answers: $score"

        binding.optionC.text =
            "Questions attempted: ${questions.size}"

        binding.optionD.text =
            "Tap Back to return"

        binding.feedbackText.text =
            if (percentage >= 80) {
                "Excellent practice session."
            } else if (percentage >= 60) {
                "Good attempt. Keep practicing."
            } else {
                "Review the topics and try again."
            }

        binding.nextButton.text =
            "Done"

        binding.nextButton.setOnClickListener {
            finish()
        }

        disableOptions()
    }
}