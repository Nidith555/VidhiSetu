package com.example.vidhisetu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal object BackendApiSettings {
    const val BASE_URL = "https://vidhisetu-backend.onrender.com"
    const val MAX_UPLOAD_BYTES = 10 * 1024 * 1024
}

class BackendApiException(
    val statusCode: Int,
    message: String
) : Exception(message)

data class ExtractedDocumentStatement(
    val text: String,
    val pageReference: String
)

data class DocumentAgentResult(
    val documentType: String,
    val summary: String,
    val parties: List<String>,
    val dates: List<String>,
    val allegations: List<ExtractedDocumentStatement>,
    val legalSections: List<String>,
    val obligations: List<ExtractedDocumentStatement>,
    val importantClauses: List<ExtractedDocumentStatement>,
    val evidence: List<ExtractedDocumentStatement>,
    val missingInformation: List<String>,
    val extractedFacts: List<ExtractedDocumentStatement>,
    val verifiedFacts: List<String>,
    val warnings: List<String>,
    val legalAnswer: String,
    val citations: List<String>,
    val disclaimer: String,
    val verificationStatus: String,
    val verifiedCitations: List<String>,
    val escalationRequired: Boolean,
    val escalationReason: String,
    val recommendedAction: String,
    val languageNotice: String,
    val legalError: String
)

data class LegalDraftResult(
    val status: String,
    val title: String,
    val draft: String,
    val missingInformation: List<String>,
    val questions: List<String>,
    val verifiedCitations: List<String>,
    val warning: String,
    val message: String
)

private fun JSONArray?.readStrings(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val value = optString(index).trim()
            if (value.isNotEmpty()) add(value)
        }
    }
}

private fun JSONArray?.readStatements(): List<ExtractedDocumentStatement> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val text = item.optString("text", "").trim()
            if (text.isNotEmpty()) {
                add(
                    ExtractedDocumentStatement(
                        text = text,
                        pageReference = item.optString("page_reference", "").trim()
                    )
                )
            }
        }
    }
}

private fun readErrorMessage(json: JSONObject, statusCode: Int): String =
    json.optString("message", "").ifBlank {
        json.optString("error", "").ifBlank { "The backend returned HTTP " + statusCode + "." }
    }

class DocumentAnalysisClient {

    suspend fun analyze(
        fileName: String,
        mimeType: String,
        content: ByteArray,
        language: String,
        role: String = "civilian"
    ): DocumentAgentResult = withContext(Dispatchers.IO) {
        require(content.isNotEmpty()) { "The selected document is empty." }
        require(content.size <= BackendApiSettings.MAX_UPLOAD_BYTES) {
            "The document is larger than the 10 MB upload limit."
        }

        val boundary = "----VidhiSetu" + System.currentTimeMillis()
        val connection = URL(BackendApiSettings.BASE_URL + "/api/documents/analyze")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 120_000
            connection.doOutput = true
            connection.setChunkedStreamingMode(64 * 1024)
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)

            connection.outputStream.use { output ->
                fun writeText(value: String) {
                    output.write(value.toByteArray(Charsets.UTF_8))
                }

                fun writeField(name: String, value: String) {
                    writeText("--" + boundary + "\r\n")
                    writeText("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n")
                    writeText(value + "\r\n")
                }

                writeField("language", language)
                writeField("role", role)
                val safeFileName = fileName.replace("\"", "").replace("\r", "").replace("\n", "")
                writeText("--" + boundary + "\r\n")
                writeText(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" +
                        safeFileName + "\"\r\n"
                )
                writeText("Content-Type: " + mimeType + "\r\n\r\n")
                output.write(content)
                writeText("\r\n--" + boundary + "--\r\n")
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = try {
                JSONObject(responseText)
            } catch (_: Exception) {
                JSONObject()
            }

            val analysis = json.optJSONObject("document_analysis")
            if (statusCode !in 200..299 && analysis == null) {
                throw BackendApiException(statusCode, readErrorMessage(json, statusCode))
            }
            if (analysis == null) {
                throw BackendApiException(statusCode, "The backend returned no document analysis.")
            }

            val legal = json.optJSONObject("legal_response")
            val verification = legal?.optJSONObject("verification")
            val escalation = legal?.optJSONObject("escalation")
            val translation = legal?.optJSONObject("translation")
            val legalError = when {
                legal == null -> json.optString("message", "")
                legal.optString("error", "").isNotBlank() -> readErrorMessage(legal, statusCode)
                statusCode !in 200..299 -> readErrorMessage(json, statusCode)
                else -> ""
            }

            DocumentAgentResult(
                documentType = analysis.optString("document_type", "other"),
                summary = analysis.optString("summary", "").trim(),
                parties = analysis.optJSONArray("parties").readStrings(),
                dates = analysis.optJSONArray("dates").readStrings(),
                allegations = analysis.optJSONArray("allegations").readStatements(),
                legalSections = analysis.optJSONArray("legal_sections").readStrings(),
                obligations = analysis.optJSONArray("obligations").readStatements(),
                importantClauses = analysis.optJSONArray("important_clauses").readStatements(),
                evidence = analysis.optJSONArray("evidence").readStatements(),
                missingInformation = analysis.optJSONArray("missing_information").readStrings(),
                extractedFacts = analysis.optJSONArray("extracted_facts").readStatements(),
                verifiedFacts = analysis.optJSONArray("verified_facts").readStrings(),
                warnings = analysis.optJSONArray("warnings").readStrings(),
                legalAnswer = legal?.optString("answer", "")?.trim().orEmpty(),
                citations = legal?.optJSONArray("citations").readStrings().orEmpty(),
                disclaimer = legal?.optString("disclaimer", "")?.trim().orEmpty(),
                verificationStatus = verification?.optString("status", "").orEmpty(),
                verifiedCitations = verification?.optJSONArray("verified_citations").readStrings().orEmpty(),
                escalationRequired = escalation?.optBoolean("escalation_required", false) ?: false,
                escalationReason = escalation?.optString("reason", "").orEmpty(),
                recommendedAction = escalation?.optString("recommended_action", "").orEmpty(),
                languageNotice = legal?.optString("language_notice", "").orEmpty()
                    .ifBlank { translation?.optString("warning", "").orEmpty() },
                legalError = legalError
            )
        } finally {
            connection.disconnect()
        }
    }
}

class LegalDraftClient {

    suspend fun createDraft(
        documentType: String,
        complainant: String,
        respondent: String,
        incidentDate: String,
        incidentPlace: String,
        facts: List<String>,
        requestedRelief: String,
        language: String,
        role: String = "civilian"
    ): LegalDraftResult = withContext(Dispatchers.IO) {
        val requestJson = JSONObject().apply {
            put("document_type", documentType)
            put("complainant", complainant)
            put("respondent", respondent)
            put("incident_date", incidentDate)
            put("incident_place", incidentPlace)
            put("facts", JSONArray(facts))
            put("requested_relief", requestedRelief)
            put("language", language)
            put("role", role)
        }

        val connection = URL(BackendApiSettings.BASE_URL + "/api/legal/draft")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 120_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.outputStream.use { output ->
                output.write(requestJson.toString().toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = try {
                JSONObject(responseText)
            } catch (_: Exception) {
                JSONObject()
            }
            if (statusCode !in 200..299) {
                throw BackendApiException(statusCode, readErrorMessage(json, statusCode))
            }

            LegalDraftResult(
                status = json.optString("status", "rejected"),
                title = json.optString("title", ""),
                draft = json.optString("draft", "").trim(),
                missingInformation = json.optJSONArray("missing_information").readStrings(),
                questions = json.optJSONArray("questions").readStrings(),
                verifiedCitations = json.optJSONArray("verified_citations").readStrings(),
                warning = json.optString("warning", "").trim(),
                message = json.optString("message", "").trim()
            )
        } finally {
            connection.disconnect()
        }
    }
}
