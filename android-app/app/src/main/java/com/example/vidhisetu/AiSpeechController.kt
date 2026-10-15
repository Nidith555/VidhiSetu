package com.example.vidhisetu

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AiSpeechController(
    context: Context,
    private val onStart: () -> Unit = {},
    private val onComplete: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private val textToSpeech: TextToSpeech =
        TextToSpeech(context.applicationContext, this)

    private var isReady = false

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = textToSpeech.setLanguage(
                Locale.getDefault()
            )

            isReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

            if (isReady) {
                textToSpeech.setSpeechRate(0.95f)
                textToSpeech.setPitch(1.0f)
            }
        }
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