package com.example.vidhisetu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FlaskAiQuestionPlanner : AiQuestionPlanner {

    companion object {
        private const val BASE_URL = "https://vidhisetu-backend.onrender.com"
    }

    override suspend fun getNextQuestion(
        caseContext: CaseContext
    ): AiQuestionResult = withContext(Dispatchers.IO) {

        val url = URL(
            "$BASE_URL/api/civilian/question"
        )

        val connection =
            url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 60000
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            val requestBody = JSONObject().apply {

                put("language", caseContext.language)

                put(
                    "initialProblem",
                    caseContext.initialProblem
                )

                put(
                    "timeline",
                    caseContext.timeline
                )

                put(
                    "peopleInvolved",
                    caseContext.peopleInvolved
                )

                put(
                    "documents",
                    caseContext.documents
                )

                put(
                    "actionsTaken",
                    caseContext.actionsTaken
                )

                put(
                    "desiredOutcome",
                    caseContext.desiredOutcome
                )

                put(
                    "questionCount",
                    caseContext.questionCount
                )
            }

            connection.outputStream.use { output ->
                output.write(
                    requestBody
                        .toString()
                        .toByteArray(Charsets.UTF_8)
                )
            }

            val responseCode =
                connection.responseCode

            val responseStream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseText =
                responseStream?.bufferedReader()
                    ?.use { it.readText() }
                    ?: ""

            if (responseCode !in 200..299) {
                throw Exception(
                    "Backend error $responseCode: $responseText"
                )
            }

            val json =
                JSONObject(responseText)

            val complete =
                json.optBoolean("complete", false)

            val question =
                if (json.isNull("question")) {
                    null
                } else {
                    json.optString("question")
                }

            AiQuestionResult(
                complete = complete,
                question = question,
                fallback = json.optBoolean("fallback", false)
            )

        } finally {
            connection.disconnect()
        }
    }
}