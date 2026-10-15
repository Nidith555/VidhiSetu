package com.example.vidhisetu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.vidhisetu.databinding.ActivityAskVidhisetuBinding
import kotlinx.coroutines.launch
import java.util.Locale

class AskVidhiSetuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskVidhisetuBinding

    // =========================================================
    // AI TEXT TO SPEECH
    // =========================================================

    private lateinit var aiSpeechController: AiSpeechController

    // =========================================================
    // SPEECH TO TEXT
    // =========================================================

    private var speechRecognizer: SpeechRecognizer? = null

    private var isListening = false

    // =========================================================
    // CASE CONTEXT
    // =========================================================

    private val caseContext = CaseContext()

    private val answerInterpreter =
        AnswerInterpreter()

    // =========================================================
    // PHASE 5 — FLASK + OLLAMA AI
    // =========================================================

    private val aiQuestionPlanner =
        FlaskAiQuestionPlanner()

    private var currentQuestionNumber = 0

    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                startVoiceInput()

            } else {

                Toast.makeText(
                    this,
                    "Microphone permission is required for voice input.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityAskVidhisetuBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupSpeech()

        setupVoiceRecognition()

        setupInitialState()

        setupClicks()
    }

    // =========================================================
    // AI TEXT TO SPEECH
    // =========================================================

    private fun setupSpeech() {

        aiSpeechController =
            AiSpeechController(
                context = this,

                onStart = {

                    runOnUiThread {

                        binding.aiStatusText.text =
                            "SPEAKING • AI"

                        binding.questionProgress.text =
                            "VidhiSetu AI is speaking..."
                    }
                },

                onComplete = {

                    runOnUiThread {

                        binding.aiStatusText.text =
                            "ONLINE • READY"

                        if (
                            !isListening &&
                            caseContext.questionCount < 5
                        ) {

                            binding.questionProgress.text =
                                "Your answer"
                        }
                    }
                }
            )
    }

    // =========================================================
    // SPEECH RECOGNITION
    // =========================================================

    private fun setupVoiceRecognition() {

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(this)
        ) {

            Toast.makeText(
                this,
                "Speech recognition is not available on this device.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        speechRecognizer =
            SpeechRecognizer
                .createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    startListeningState()
                }

                override fun onBeginningOfSpeech() {

                    binding.aiStatusText.text =
                        "LISTENING • YOU"

                    binding.questionProgress.text =
                        "Listening..."
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEndOfSpeech() {

                    binding.aiStatusText.text =
                        "PROCESSING • VOICE"

                    binding.questionProgress.text =
                        "Understanding your answer..."
                }

                override fun onError(
                    error: Int
                ) {

                    stopListeningState()

                    val message =
                        when (error) {

                            SpeechRecognizer.ERROR_AUDIO ->
                                "Microphone error."

                            SpeechRecognizer.ERROR_CLIENT ->
                                "Speech recognition was interrupted."

                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission is required."

                            SpeechRecognizer.ERROR_NETWORK ->
                                "Network error during speech recognition."

                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Speech recognition timed out."

                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "I couldn't understand that. Please try again."

                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                                "Speech recognition is busy."

                            SpeechRecognizer.ERROR_SERVER ->
                                "Speech recognition service error."

                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "No speech detected."

                            else ->
                                "Unable to recognize speech."
                        }

                    binding.aiStatusText.text =
                        "ONLINE • READY"

                    binding.questionProgress.text =
                        "Tap the microphone to try again"

                    Toast.makeText(
                        this@AskVidhiSetuActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val spokenText =
                        matches?.firstOrNull()

                    if (
                        !spokenText.isNullOrBlank()
                    ) {

                        binding.messageInput.setText(
                            spokenText
                        )

                        binding.messageInput.setSelection(
                            binding.messageInput.text.length
                        )

                        stopListeningState()

                        sendUserMessage()
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {

                    val partialText =
                        partialResults
                            ?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )
                            ?.firstOrNull()

                    if (
                        !partialText.isNullOrBlank()
                    ) {

                        binding.messageInput.setText(
                            partialText
                        )

                        binding.messageInput.setSelection(
                            binding.messageInput.text.length
                        )
                    }
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )
    }

    // =========================================================
    // INITIAL STATE
    // =========================================================

    private fun setupInitialState() {

        binding.aiStatusText.text =
            "ONLINE • READY"

        binding.questionProgress.text =
            "Tell me about your situation"

        binding.messageInput
            .setOnEditorActionListener { _, _, _ ->

                sendUserMessage()

                true
            }
    }

    // =========================================================
    // CLICKS
    // =========================================================

    private fun setupClicks() {

        binding.backButton.setOnClickListener {

            finish()
        }

        binding.sendButton.setOnClickListener {

            sendUserMessage()
        }

        binding.micButton.setOnClickListener {

            if (isListening) {

                stopVoiceInput()

            } else {

                requestMicrophonePermission()
            }
        }

        binding.languageButton.setOnClickListener {

            Toast.makeText(
                this,
                "Language selection will be connected later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private fun requestMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            startVoiceInput()

        } else {

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    // =========================================================
    // START VOICE
    // =========================================================

    private fun startVoiceInput() {

        if (speechRecognizer == null) {

            Toast.makeText(
                this,
                "Speech recognition is unavailable.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            true
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            3
        )

        try {

            speechRecognizer?.startListening(
                intent
            )

            startListeningState()

        } catch (exception: Exception) {

            Toast.makeText(
                this,
                "Unable to start voice recognition.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // LISTENING UI
    // =========================================================

    private fun startListeningState() {

        isListening = true

        binding.aiStatusText.text =
            "LISTENING • YOU"

        binding.questionProgress.text =
            "Speak your answer..."

        binding.micButton.text =
            "⏹"
    }

    private fun stopListeningState() {

        isListening = false

        binding.micButton.text =
            "🎙"

        binding.aiStatusText.text =
            "ONLINE • READY"
    }

    private fun stopVoiceInput() {

        speechRecognizer?.stopListening()

        stopListeningState()
    }

    // =========================================================
    // USER MESSAGE
    // =========================================================

    private fun sendUserMessage() {

        val text =
            binding.messageInput.text
                .toString()
                .trim()

        if (text.isEmpty()) {
            return
        }

        if (isListening) {

            speechRecognizer?.stopListening()

            stopListeningState()
        }

        /*
         * First user message = initial problem.
         * Later messages = answers to AI questions.
         */

        if (currentQuestionNumber == 0) {

            caseContext.initialProblem =
                text

        } else {

            answerInterpreter.addAnswer(
                context = caseContext,
                questionNumber = currentQuestionNumber,
                answer = text
            )
        }

        addUserMessage(text)

        binding.messageInput.text.clear()

        askNextQuestion()
    }

    // =========================================================
    // PHASE 5 — AI QUESTION ENGINE
    // =========================================================

    private fun askNextQuestion() {

        binding.aiStatusText.text =
            "PROCESSING • AI"

        binding.questionProgress.text =
            "VidhiSetu is thinking..."

        lifecycleScope.launch {

            try {

                val result =
                    aiQuestionPlanner.getNextQuestion(
                        caseContext
                    )

                if (result.complete) {

                    finishQuestionnaire()

                    return@launch
                }

                val nextQuestion =
                    result.question

                if (nextQuestion.isNullOrBlank()) {

                    showAiError(
                        "VidhiSetu could not generate the next question."
                    )

                    return@launch
                }

                /*
                 * The backend has generated a new
                 * adaptive question.
                 */

                currentQuestionNumber =
                    caseContext.questionCount + 1

                /*
                 * Safety limit.
                 */

                if (currentQuestionNumber > 5) {

                    finishQuestionnaire()

                    return@launch
                }

                binding.questionProgress.text =
                    "Question $currentQuestionNumber of 5"

                addAIMessage(
                    nextQuestion
                )

                /*
                 * Speak exactly the same text
                 * that is displayed.
                 */

                aiSpeechController.speak(
                    nextQuestion
                )

            } catch (exception: Exception) {

                showAiError(
                    "I couldn't connect to VidhiSetu AI. Please check that the backend is running."
                )
            }
        }
    }

    // =========================================================
    // AI ERROR
    // =========================================================

    private fun showAiError(
        message: String
    ) {

        binding.aiStatusText.text =
            "OFFLINE • AI"

        binding.questionProgress.text =
            "Please try again"

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    // =========================================================
    // FINISH INTAKE
    // =========================================================

    private fun finishQuestionnaire() {

        binding.questionProgress.text =
            "Information collected"

        binding.aiStatusText.text =
            "ONLINE • READY"

        val finalMessage =
            """
            Thank you. I now have the initial information needed to understand your situation.
            
            VidhiSetu will next analyse the information you provided and identify the relevant legal issues before giving you guidance.
            """.trimIndent()

        addAIMessage(
            finalMessage
        )

        aiSpeechController.speak(
            finalMessage
        )

        /*
         * Future Phase:
         *
         * CaseContext
         *      ↓
         * Legal Issue Classification
         *      ↓
         * RAG
         *      ↓
         * Verified Legal Sources
         *      ↓
         * Legal Reasoning
         *      ↓
         * Final Legal Guidance
         */
    }

    // =========================================================
    // USER MESSAGE UI
    // =========================================================

    private fun addUserMessage(
        message: String
    ) {

        val outerContainer =
            LinearLayoutHelper.createVertical(
                this
            )

        outerContainer.gravity =
            Gravity.END

        outerContainer.setPadding(
            45,
            8,
            0,
            8
        )

        val label =
            TextView(this)

        label.text =
            "👤  You"

        label.setTextColor(
            Color.WHITE
        )

        label.textSize =
            11f

        label.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        outerContainer.addView(
            label
        )

        val messageView =
            TextView(this)

        messageView.text =
            message

        messageView.setTextColor(
            Color.rgb(
                7,
                29,
                50
            )
        )

        messageView.textSize =
            14f

        messageView.setPadding(
            14,
            10,
            14,
            10
        )

        messageView.background =
            getDrawable(
                R.drawable.user_message_background
            )

        outerContainer.addView(
            messageView
        )

        binding.conversationContainer
            .addView(
                outerContainer
            )

        scrollToBottom()
    }

    // =========================================================
    // AI MESSAGE UI
    // =========================================================

    private fun addAIMessage(
        message: String
    ) {

        val outerContainer =
            LinearLayoutHelper.createVertical(
                this
            )

        outerContainer.gravity =
            Gravity.START

        outerContainer.setPadding(
            0,
            8,
            35,
            8
        )

        val label =
            TextView(this)

        label.text =
            "⚖️  VidhiSetu AI"

        label.setTextColor(
            Color.rgb(
                244,
                194,
                79
            )
        )

        label.textSize =
            11f

        label.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        outerContainer.addView(
            label
        )

        val messageView =
            TextView(this)

        messageView.text =
            message

        messageView.setTextColor(
            Color.WHITE
        )

        messageView.textSize =
            14f

        messageView.setLineSpacing(
            3f,
            1f
        )

        messageView.setPadding(
            14,
            10,
            14,
            10
        )

        messageView.background =
            getDrawable(
                R.drawable.ai_message_background
            )

        outerContainer.addView(
            messageView
        )

        val listenButton =
            TextView(this)

        listenButton.text =
            "🔊  Listen"

        listenButton.setTextColor(
            Color.rgb(
                244,
                194,
                79
            )
        )

        listenButton.textSize =
            11f

        listenButton.setPadding(
            5,
            8,
            5,
            5
        )

        listenButton.setOnClickListener {

            aiSpeechController.speak(
                message
            )
        }

        outerContainer.addView(
            listenButton
        )

        binding.conversationContainer
            .addView(
                outerContainer
            )

        scrollToBottom()
    }

    // =========================================================
    // AUTO SCROLL
    // =========================================================

    private fun scrollToBottom() {

        binding.conversationScroll.post {

            binding.conversationScroll.fullScroll(
                View.FOCUS_DOWN
            )
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroy() {

        speechRecognizer?.destroy()

        speechRecognizer = null

        if (
            ::aiSpeechController.isInitialized
        ) {

            aiSpeechController.shutdown()
        }

        super.onDestroy()
    }
}