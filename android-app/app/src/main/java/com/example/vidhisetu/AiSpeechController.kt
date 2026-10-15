package com.example.vidhisetu

import android.content.Context
import android.speech.tts.TextToSpeech

class AiSpeechController(
    context: Context,
    private val onStart: () -> Unit = {},
    private val onComplete: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private val textToSpeech: TextToSpeech =
        TextToSpeech(context.applicationContext, this)

    private var isReady = false
    private var languageCode: String = AppLanguage.ENGLISH

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            applyLanguage()
            if (isReady) {
                textToSpeech.setSpeechRate(0.95f)
                textToSpeech.setPitch(1.0f)
            }
        } else {
            isReady = false
        }
    }

    fun setLanguage(code: String) {
        languageCode = when (code) {
            AppLanguage.HINDI, AppLanguage.TELUGU -> code
            else -> AppLanguage.ENGLISH
        }
        if (isReady) applyLanguage()
    }

    private fun applyLanguage() {
        val result = textToSpeech.setLanguage(AppLanguage.locale(languageCode))
        isReady =
            result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String) {

        if (!isReady || text.isBlank()) {
            return
        }

        stop()

        onStart()

        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "VIDHISETU_AI_RESPONSE"
        )
    }

    fun stop() {
        textToSpeech.stop()
    }

    fun shutdown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}