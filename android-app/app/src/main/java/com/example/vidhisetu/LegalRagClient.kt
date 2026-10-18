package com.example.vidhisetu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LegalRagPassage(
    val act: String,
    val sectionNumber: String,
    val sectionTitle: String,
    val chapter: String,
    val pageStart: Int?,
    val pageEnd: Int?,
    val source: String,
    val text: String
)

data class LegalRagResult(
    val status: String,
    val role: String,
    val answer: String,
    val answerLanguage: String,
    val languageNotice: String,
    val takeaways: List<String>,
    val disclaimer: String,
    val citations: List<String>,
    val passages: List<LegalRagPassage>
)

class LegalRagApiException(
    val statusCode: Int,
    val ragStatus: String?,
    message: String
) : Exception(message)

/**
 * Calls the Flask gateway. The gateway then proxies the request to the
 * teammate's FastAPI RAG service. The Android app never calls the RAG host
 * directly and never holds any LLM credentials.
 */
class LegalRagClient {

    companion object {
        private const val BASE_URL = "https://vidhisetu-backend.onrender.com"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 120_000
        private const val MAX_PASSAGES = 4
        private const val MAX_PASSAGE_CHARS = 1_200
    }

    suspend fun getLegalAnswer(context: CaseContext): LegalRagResult =
        withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/api/legal/answer")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")

                val requestJson = JSONObject().apply {
                    put("language", context.language)
                    put("role", context.role)
                    put("initialProblem", context.initialProblem)
                    put("timeline", context.timeline)
                    put("peopleInvolved", context.peopleInvolved)
                    put("documents", context.documents)
                    put("actionsTaken", context.actionsTaken)
                    put("desiredOutcome", context.desiredOutcome)
                    put("additionalDetails", context.additionalDetails)
                    put("top_k", 3)
                }

                connection.outputStream.use { output ->
                    output.write(requestJson.toString().toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode
                val responseStream =
                    if (statusCode in 200..299) connection.inputStream
                    else connection.errorStream

                val responseBody = responseStream?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                }.orEmpty()

                val json = try {
                    JSONObject(responseBody)
                } catch (_: Exception) {
                    JSONObject()
                }

                if (statusCode !in 200..299) {
                    throw LegalRagApiException(
                        statusCode = statusCode,
                        ragStatus = json.optString("rag_status").takeIf { it.isNotBlank() },
                        message = json.optString(
                            "error",
                            "The legal retrieval service returned HTTP $statusCode."
                        )
                    )
                }

                LegalRagResult(
                    status = json.optString("status", "UNKNOWN"),
                    role = json.optString("role", context.role),
                    answer = json.optString("answer", "").trim(),
                    answerLanguage = json.optString("answer_language", "en"),
                    languageNotice = json.optString("language_notice", "").trim(),
                    takeaways = readStringArray(json.optJSONArray("takeaways")),
                    disclaimer = json.optString("disclaimer", "").trim(),
                    citations = readStringArray(json.optJSONArray("citations")),
                    passages = readPassages(json.optJSONArray("passages"))
                ).also { result ->
                    if (result.answer.isBlank()) {
                        throw LegalRagApiException(
                            statusCode = 502,
                            ragStatus = "invalid_response",
                            message = "The legal retrieval service returned an empty answer."
                        )
                    }
                }
            } finally {
                connection.disconnect()
            }
        }

    private fun readStringArray(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val value = array.optString(index).trim()
                if (value.isNotEmpty()) add(value)
            }
        }
    }

    private fun readPassages(array: JSONArray?): List<LegalRagPassage> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until minOf(array.length(), MAX_PASSAGES)) {
                val item = array.optJSONObject(index) ?: continue
                val text = item.optString("text", "").trim()
                add(
                    LegalRagPassage(
                        act = item.optString("act", "Unspecified Act"),
                        sectionNumber = item.optString("section_number", ""),
                        sectionTitle = item.optString("section_title", ""),
                        chapter = item.optString("chapter", ""),
                        pageStart = item.nullableInt("page_start"),
                        pageEnd = item.nullableInt("page_end"),
                        source = item.optString("source", ""),
                        text = if (text.length > MAX_PASSAGE_CHARS) {
                            text.take(MAX_PASSAGE_CHARS) + "…"
                        } else {
                            text
                        }
                    )
                )
            }
        }
    }

    private fun JSONObject.nullableInt(key: String): Int? {
        if (isNull(key)) return null
        return try {
            getInt(key)
        } catch (_: Exception) {
            null
        }
    }
}
