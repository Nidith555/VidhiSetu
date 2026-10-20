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

    private val legalRagClient = LegalRagClient()
    private var isLegalAnswerLoading = false
    private var legalAnswerCompleted = false

    private var currentQuestionNumber = 0
    private var showedFallbackNotice = false

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

        caseContext.language = AppLanguage.getCode(this)
        caseContext.role = intent.getStringExtra("role")
            ?.lowercase(java.util.Locale.ROOT)
            ?.takeIf { it in setOf("civilian", "student", "lawyer") }
            ?: "civilian"

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

        aiSpeechController.setLanguage(caseContext.language)
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

        binding.aiStatusText.text = localized(
            "ONLINE • READY",
            "ऑनलाइन • तैयार",
            "ఆన్‌లైన్ • సిద్ధంగా ఉంది"
        )

        binding.questionProgress.text = localized(
            "Tell me about your situation",
            "अपनी स्थिति के बारे में बताइए",
            "మీ పరిస్థితి గురించి చెప్పండి"
        )
        binding.messageInput.hint = localized(
            "Type your answer...",
            "अपना उत्तर लिखें...",
            "మీ సమాధానాన్ని టైప్ చేయండి..."
        )
        binding.languageButton.contentDescription = localized(
            "Change language",
            "भाषा बदलें",
            "భాషను మార్చండి"
        )

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
            showLanguagePicker()
        }
    }

    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private fun localized(english: String, hindi: String, telugu: String): String =
        AppLanguage.text(caseContext.language, english, hindi, telugu)

    private fun showLanguagePicker() {
        AppLanguage.showPicker(this) { selected ->
            caseContext.language = selected
            aiSpeechController.setLanguage(selected)
            binding.messageInput.hint = localized(
                "Type your answer...",
                "अपना उत्तर लिखें...",
                "మీ సమాధానాన్ని టైప్ చేయండి..."
            )
            binding.languageButton.contentDescription = localized(
                "Change language",
                "भाषा बदलें",
                "భాషను మార్చండి"
            )
            binding.questionProgress.text = localized(
                "Tell me about your situation",
                "अपनी स्थिति के बारे में बताइए",
                "మీ పరిస్థితి గురించి చెప్పండి"
            )
            Toast.makeText(
                this,
                localized(
                    "Language updated. New questions and speech will use this language.",
                    "भाषा बदल दी गई है। आगे के प्रश्न और आवाज़ इसी भाषा में होंगे।",
                    "భాష మార్చబడింది. ఇకపై ప్రశ్నలు మరియు వాయిస్ ఈ భాషలో ఉంటాయి."
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
            AppLanguage.locale(caseContext.language).toLanguageTag()
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

        binding.aiStatusText.text = localized(
            "PROCESSING • AI",
            "AI प्रक्रिया में है",
            "AI పని చేస్తోంది"
        )

        binding.questionProgress.text = localized(
            "VidhiSetu is thinking...",
            "VidhiSetu सोच रहा है...",
            "VidhiSetu ఆలోచిస్తోంది..."
        )

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

                if (result.fallback && !showedFallbackNotice) {
                    showedFallbackNotice = true
                    Toast.makeText(
                        this@AskVidhiSetuActivity,
                        localized(
                            "AI is temporarily unavailable. These are basic guided questions, not AI-generated legal advice.",
                            "AI अस्थायी रूप से उपलब्ध नहीं है। ये सामान्य मार्गदर्शक प्रश्न हैं, AI द्वारा तैयार की गई कानूनी सलाह नहीं।",
                            "AI తాత్కాలికంగా అందుబాటులో లేదు. ఇవి సాధారణ మార్గదర్శక ప్రశ్నలు మాత్రమే; AI రూపొందించిన చట్టపరమైన సలహా కాదు."
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }

                val nextQuestion =
                    result.question

                if (nextQuestion.isNullOrBlank()) {

                    showAiError(
                        localized(
                            "VidhiSetu could not generate the next question.",
                            "VidhiSetu अगला प्रश्न तैयार नहीं कर पाया।",
                            "VidhiSetu తదుపరి ప్రశ్నను రూపొందించలేకపోయింది."
                        )
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

                binding.questionProgress.text = localized(
                    "Question $currentQuestionNumber of 5",
                    "प्रश्न $currentQuestionNumber / 5",
                    "ప్రశ్న $currentQuestionNumber / 5"
                )

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
                    localized(
                        "I couldn't connect to VidhiSetu AI. Check your internet connection and try again.",
                        "VidhiSetu AI से कनेक्ट नहीं हो सका। इंटरनेट कनेक्शन जाँचें और फिर प्रयास करें।",
                        "VidhiSetu AIకి కనెక్ట్ కాలేకపోయింది. ఇంటర్నెట్ కనెక్షన్‌ను తనిఖీ చేసి మళ్లీ ప్రయత్నించండి."
                    )
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

        binding.aiStatusText.text = localized(
            "OFFLINE • AI",
            "AI ऑफलाइन है",
            "AI ఆఫ్‌లైన్‌లో ఉంది"
        )

        binding.questionProgress.text = localized(
            "Please try again",
            "कृपया फिर प्रयास करें",
            "దయచేసి మళ్లీ ప్రయత్నించండి"
        )

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

        binding.questionProgress.text = localized(
            "Information collected",
            "जानकारी एकत्र की गई",
            "సమాచారం సేకరించబడింది"
        )

        binding.aiStatusText.text = localized(
            "SEARCHING LEGAL SOURCES",
            "कानूनी स्रोत खोजे जा रहे हैं",
            "చట్టపరమైన మూలాలను శోధిస్తోంది"
        )

        val finalMessage = localized(
            "Thank you. I have collected the initial facts. I will now search the available legal sources. I will show the retrieved provisions and citations so you can verify them.",
            "धन्यवाद। शुरुआती तथ्य एकत्र कर लिए गए हैं। अब उपलब्ध कानूनी स्रोत खोजे जाएँगे। आपको संबंधित प्रावधान और उद्धरण दिखाए जाएँगे ताकि आप उनकी जाँच कर सकें।",
            "ధన్యవాదాలు. ప్రాథమిక వివరాలు సేకరించబడ్డాయి. ఇప్పుడు అందుబాటులో ఉన్న చట్టపరమైన మూలాలను శోధిస్తాను. మీరు ధృవీకరించడానికి సంబంధిత నిబంధనలు మరియు సూచనలను చూపిస్తాను."
        )

        addAIMessage(finalMessage)
        aiSpeechController.speak(finalMessage)
        requestLegalGuidance()
    }

    private fun requestLegalGuidance() {
        if (isLegalAnswerLoading || legalAnswerCompleted) return

        isLegalAnswerLoading = true
        binding.aiStatusText.text = localized(
            "SEARCHING LEGAL SOURCES",
            "कानूनी स्रोत खोजे जा रहे हैं",
            "చట్టపరమైన మూలాలను శోధిస్తోంది"
        )
        binding.questionProgress.text = localized(
            "Retrieving relevant statutes and citations…",
            "संबंधित कानून और उद्धरण खोजे जा रहे हैं…",
            "సంబంధిత చట్టాలు మరియు సూచనలను సేకరిస్తోంది…"
        )

        lifecycleScope.launch {
            try {
                val result = legalRagClient.getLegalAnswer(caseContext)

                val heading = if (result.status == "INSUFFICIENT_EVIDENCE") {
                    localized(
                        "Insufficient matching legal evidence was found",
                        "पर्याप्त संबंधित कानूनी प्रमाण नहीं मिला",
                        "సరిపడే సంబంధిత చట్టపరమైన ఆధారాలు లభించలేదు"
                    )
                } else {
                    localized(
                        "Legal information from retrieved sources",
                        "प्राप्त कानूनी स्रोतों से जानकारी",
                        "సేకరించిన చట్టపరమైన మూలాల నుండి సమాచారం"
                    )
                }

                val answerText = buildString {
                    append(heading)
                    append("\n\n")
                    append(result.answer)
                    if (result.takeaways.isNotEmpty()) {
                        append("\n\n")
                        append(
                            localized(
                                "Key points",
                                "मुख्य बिंदु",
                                "ముఖ్యమైన అంశాలు"
                            )
                        )
                        result.takeaways.forEach { item ->
                            append("\n• ")
                            append(item)
                        }
                    }
                    if (result.verificationStatus.isNotBlank()) {
                        append("\n\n")
                        append(
                            localized(
                                "Citation support check: ",
                                "कानूनी उद्धरण जाँच: ",
                                "చట్టపరమైన సూచనల తనిఖీ: "
                            )
                        )
                        append(result.verificationStatus)
                    }
                    if (result.verificationReasons.isNotEmpty()) {
                        append("\n")
                        append(
                            localized(
                                "Review notes: ",
                                "समीक्षा नोट: ",
                                "సమీక్ష గమనికలు: "
                            )
                        )
                        append(result.verificationReasons.joinToString("; "))
                    }
                    if (result.escalationRequired) {
                        append("\n\n")
                        append(
                            localized(
                                "Qualified legal help is recommended",
                                "योग्य कानूनी सहायता लेने की सलाह दी जाती है",
                                "అర్హత కలిగిన న్యాయ సహాయం తీసుకోవడం మంచిది"
                            )
                        )
                        if (result.escalationReason.isNotBlank()) {
                            append("\n")
                            append(result.escalationReason)
                        }
                        if (result.recommendedAction.isNotBlank()) {
                            append("\n")
                            append(result.recommendedAction)
                        }
                    }
                    if (result.disclaimer.isNotBlank()) {
                        append("\n\n")
                        append(result.disclaimer)
                    }
                }

                val answerMatchesUiLanguage =
                    result.answerLanguage.equals(caseContext.language, ignoreCase = true)

                addAIMessage(answerText, enableListen = answerMatchesUiLanguage)
                if (answerMatchesUiLanguage) {
                    aiSpeechController.speak(answerText)
                }

                if (result.languageNotice.isNotBlank()) {
                    addAIMessage(result.languageNotice)
                    aiSpeechController.speak(result.languageNotice)
                }

                val sourceText = buildLegalSourceText(result)
                if (sourceText.isNotBlank()) {
                    // Statutory excerpts are shown exactly as returned by RAG and are not spoken
                    // automatically because the excerpts may be long.
                    addAIMessage(sourceText, enableListen = false)
                }

                binding.aiStatusText.text = localized(
                    "LEGAL SOURCES RETRIEVED",
                    "कानूनी स्रोत प्राप्त हुए",
                    "చట్టపరమైన మూలాలు లభించాయి"
                )
                binding.questionProgress.text = localized(
                    "Review the answer and verify the cited provisions",
                    "उत्तर पढ़ें और उद्धृत प्रावधानों की जाँच करें",
                    "సమాధానాన్ని పరిశీలించి పేర్కొన్న నిబంధనలను ధృవీకరించండి"
                )
                legalAnswerCompleted = true
            } catch (error: LegalRagApiException) {
                showLegalRagFailure(error.ragStatus)
            } catch (_: Exception) {
                showLegalRagFailure(null)
            } finally {
                isLegalAnswerLoading = false
            }
        }
    }

    private fun buildLegalSourceText(result: LegalRagResult): String {
        val sections = mutableListOf<String>()

        if (result.citations.isNotEmpty()) {
            sections += buildString {
                append(localized("Citations", "कानूनी उद्धरण", "చట్టపరమైన సూచనలు"))
                result.citations.forEach { citation ->
                    append("\n• ")
                    append(citation)
                }
            }
        }

        if (result.passages.isNotEmpty()) {
            sections += buildString {
                append(
                    localized(
                        "Retrieved statutory passages",
                        "प्राप्त वैधानिक अंश",
                        "సేకరించిన చట్టంలోని భాగాలు"
                    )
                )
                result.passages.forEachIndexed { index, passage ->
                    append("\n\n")
                    append(index + 1)
                    append(". ")
                    append(passage.act)
                    if (passage.sectionNumber.isNotBlank()) {
                        append(" — Section/Article ")
                        append(passage.sectionNumber)
                    }
                    if (passage.sectionTitle.isNotBlank()) {
                        append(": ")
                        append(passage.sectionTitle)
                    }
                    val pages = when {
                        passage.pageStart != null && passage.pageEnd != null &&
                            passage.pageStart != passage.pageEnd ->
                            " (pages ${passage.pageStart}–${passage.pageEnd})"
                        passage.pageStart != null -> " (page ${passage.pageStart})"
                        else -> ""
                    }
                    if (passage.source.isNotBlank()) {
                        append("\nSource: ")
                        append(passage.source)
                        append(pages)
                    }
                    if (passage.text.isNotBlank()) {
                        append("\n")
                        append(passage.text)
                    }
                }
            }
        }

        return sections.joinToString("\n\n")
    }

    private fun showLegalRagFailure(ragStatus: String?) {
        binding.aiStatusText.text = localized(
            "LEGAL RETRIEVAL UNAVAILABLE",
            "कानूनी खोज उपलब्ध नहीं है",
            "చట్టపరమైన శోధన అందుబాటులో లేదు"
        )
        binding.questionProgress.text = localized(
            "Tap retry after the retrieval service is connected",
            "खोज सेवा कनेक्ट होने के बाद फिर प्रयास करें",
            "రిట్రీవల్ సేవను కనెక్ట్ చేసిన తర్వాత మళ్లీ ప్రయత్నించండి"
        )

        val message = when (ragStatus) {
            "not_configured" -> localized(
                "The backend has not been connected to the RAG service yet. Set VIDHISETU_RAG_URL on the hosted Flask backend, then retry.",
                "बैकएंड अभी RAG सेवा से जुड़ा नहीं है। होस्ट किए गए Flask बैकएंड में VIDHISETU_RAG_URL सेट करें, फिर दोबारा प्रयास करें।",
                "బ్యాకెండ్ ఇంకా RAG సేవకు కనెక్ట్ కాలేదు. హోస్ట్ చేసిన Flask బ్యాకెండ్‌లో VIDHISETU_RAG_URL సెట్ చేసి మళ్లీ ప్రయత్నించండి."
            )
            "not_ready", "timeout", "unreachable", "upstream_error", "invalid_response" -> localized(
                "The legal retrieval service is not ready or could not respond. Please retry shortly.",
                "कानूनी खोज सेवा तैयार नहीं है या उत्तर नहीं दे सकी। कृपया थोड़ी देर बाद पुनः प्रयास करें।",
                "చట్టపరమైన శోధన సేవ సిద్ధంగా లేదు లేదా స్పందించలేకపోయింది. కొద్దిసేపటి తర్వాత మళ్లీ ప్రయత్నించండి."
            )
            else -> localized(
                "I could not reach the legal retrieval service. Check your internet connection and retry.",
                "कानूनी खोज सेवा से संपर्क नहीं हो सका। इंटरनेट कनेक्शन जाँचें और फिर प्रयास करें।",
                "చట్టపరమైన శోధన సేవను చేరుకోలేకపోయాను. ఇంటర్నెట్ కనెక్షన్‌ను తనిఖీ చేసి మళ్లీ ప్రయత్నించండి."
            )
        }

        addAIMessage(message)
        aiSpeechController.speak(message)

        val retry = TextView(this).apply {
            text = localized("↻ Retry legal search", "↻ कानूनी खोज फिर से करें", "↻ చట్టపరమైన శోధనను మళ్లీ ప్రయత్నించండి")
            setTextColor(Color.rgb(244, 194, 79))
            textSize = 14f
            setPadding(12, 10, 12, 10)
            isClickable = true
            isFocusable = true
            setOnClickListener { requestLegalGuidance() }
        }
        binding.conversationContainer.addView(retry)
        scrollToBottom()
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
        message: String,
        enableListen: Boolean = true
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

        if (enableListen) {
            val listenButton = TextView(this)
            listenButton.text = "🔊  Listen"
            listenButton.setTextColor(Color.rgb(244, 194, 79))
            listenButton.textSize = 11f
            listenButton.setPadding(5, 8, 5, 5)
            listenButton.setOnClickListener {
                aiSpeechController.speak(message)
            }
            outerContainer.addView(listenButton)
        }

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