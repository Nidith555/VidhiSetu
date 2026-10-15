"""VidhiSetu Flask API.

The civilian endpoint collects an intake through one follow-up question at a time.
Hindi (hi) and Telugu (te) are supported as response languages.
"""

import json
import logging
import os
import re

from flask import Flask, jsonify, request
from ollama import Client, ResponseError

from legal_news import get_legal_news

app = Flask(__name__)
logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))

OLLAMA_HOST = os.getenv("OLLAMA_HOST", "http://127.0.0.1:11434")
MODEL_NAME = os.getenv("OLLAMA_MODEL", "llama3.2:3b")
ollama_client = Client(host=OLLAMA_HOST)

LANGUAGES = {
    "hi": {
        "name": "Hindi",
        "pattern": re.compile(r"[\u0900-\u097F]"),
        "retry": "पूरे प्रश्न को स्वाभाविक हिंदी में लिखें। अंग्रेज़ी में प्रश्न न दें।",
        "error": "AI चुनी गई भाषा में प्रश्न तैयार नहीं कर पाया। कृपया दोबारा प्रयास करें।",
        "service_error": "AI सेवा अभी उपलब्ध नहीं है। कृपया बाद में प्रयास करें।",
    },
    "te": {
        "name": "Telugu",
        "pattern": re.compile(r"[\u0C00-\u0C7F]"),
        "retry": "ప్రశ్నను సహజమైన తెలుగులో పూర్తిగా రాయండి. ఆంగ్లంలో ప్రశ్న ఇవ్వవద్దు.",
        "error": "ఎంచుకున్న భాషలో AI ప్రశ్నను రూపొందించలేకపోయింది. దయచేసి మళ్లీ ప్రయత్నించండి.",
        "service_error": "AI సేవ ప్రస్తుతం అందుబాటులో లేదు. దయచేసి తర్వాత మళ్లీ ప్రయత్నించండి.",
    },
}


def error_message(language: str, field: str) -> str:
    """Return a user-facing error in the requested supported language."""
    return LANGUAGES.get(language, LANGUAGES["hi"])[field]


@app.get("/")
def home():
    return jsonify({
        "status": "online",
        "service": "VidhiSetu AI Backend",
        "ai_provider": "ollama",
        "model": MODEL_NAME,
        "supported_languages": ["hi", "te"],
    })


@app.get("/api/health")
def health():
    try:
        ollama_client.list()
        ollama_status = "online"
    except Exception:
        app.logger.exception("Ollama health check failed")
        ollama_status = "offline"

    return jsonify({
        "status": "healthy",
        "ai_provider": "ollama",
        "model": MODEL_NAME,
        "ollama": ollama_status,
        "supported_languages": ["hi", "te"],
    })


@app.post("/api/civilian/question")
def civilian_question():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({"error": "A JSON object is required"}), 400

    language = data.get("language", "hi")
    if not isinstance(language, str) or language not in LANGUAGES:
        return jsonify({
            "error": "language must be 'hi' (Hindi) or 'te' (Telugu)"
        }), 400

    language_config = LANGUAGES[language]
    language_name = language_config["name"]
    initial_problem = data.get("initialProblem", "")
    if not isinstance(initial_problem, str) or not initial_problem.strip():
        return jsonify({"error": "initialProblem is required"}), 400

    fields = [
        "initialProblem",
        "timeline",
        "peopleInvolved",
        "documents",
        "actionsTaken",
        "desiredOutcome",
    ]
    case_context = {}
    for field in fields:
        value = data.get(field, "")
        if not isinstance(value, str):
            return jsonify({"error": f"{field} must be a string"}), 400
        case_context[field] = value.strip()

    question_count = data.get("questionCount", 0)
    if (
        isinstance(question_count, bool)
        or not isinstance(question_count, int)
        or question_count < 0
    ):
        return jsonify({"error": "questionCount must be a non-negative integer"}), 400

    if question_count >= 5:
        return jsonify({"complete": True, "question": None})

    system_prompt = f"""
You are VidhiSetu's civilian legal-intake assistant for India.
Your task is only to collect facts; do not give legal advice yet.

STRICT RESPONSE LANGUAGE:
- The selected language is {language_name} (code: {language}).
- Write every human-readable response, especially the next question, only in {language_name}.
- Do not switch to English because the input or context uses another language.
- Keep JSON property names exactly as specified below, but write the value of "question" in {language_name}.
- Ask exactly ONE useful question at a time.
- Adapt it to the user's situation and avoid repeating facts already provided.
- Use simple language suitable for ordinary citizens; avoid unnecessary legal terminology.
- Do not reach legal conclusions, recommend legal action, or cite laws during intake.
- Do not invent facts.
- Usually collect around 4–5 answers. If sufficient facts are collected, mark the intake complete.
- Never request passwords, OTPs, Aadhaar numbers, bank PINs, or unnecessary sensitive data.
- Return ONLY valid JSON, with no Markdown or extra text.

If another question is needed:
{{"complete": false, "question": "one question in {language_name}"}}

If enough information has been collected:
{{"complete": true, "question": null}}
"""

    user_prompt = f"""
Information collected so far:
{json.dumps(case_context, ensure_ascii=False, indent=2)}

Follow-up questions already answered: {question_count}

Choose the single most useful next question in {language_name}.
Do not repeat known information. This is fact collection only, not legal advice.
Return the required JSON object.
"""

    try:
        result = None
        for attempt in range(2):
            prompt = user_prompt
            if attempt == 1:
                prompt += "\n" + language_config["retry"]

            response = ollama_client.chat(
                model=MODEL_NAME,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": prompt},
                ],
                format="json",
                options={"temperature": 0.2},
            )

            raw_output = response["message"]["content"].strip()
            parsed = json.loads(raw_output)

            complete = parsed.get("complete")
            if not isinstance(complete, bool):
                raise ValueError("AI returned invalid 'complete' value")

            if complete:
                return jsonify({"complete": True, "question": None})

            question = parsed.get("question")
            if not isinstance(question, str) or not question.strip():
                raise ValueError("AI returned an invalid question")

            # Require at least one character from the selected script. This
            # catches obvious language switches; it is not a full language
            # identification or translation-quality guarantee.
            if language_config["pattern"].search(question):
                result = question.strip()
                break

        if result is None:
            return jsonify({"error": error_message(language, "error")}), 502

        return jsonify({"complete": False, "question": result})

    except ResponseError:
        app.logger.exception("Ollama response error")
        return jsonify({"error": error_message(language, "service_error")}), 503
    except (json.JSONDecodeError, ValueError, KeyError, TypeError):
        app.logger.exception("Invalid AI response")
        return jsonify({"error": error_message(language, "error")}), 502
    except Exception:
        app.logger.exception("Unexpected AI service error")
        return jsonify({"error": error_message(language, "service_error")}), 503


@app.get("/api/legal-news")
def legal_news():
    news = get_legal_news()
    return jsonify({
        "success": True,
        "count": len(news),
        "news": news,
    })


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=int(os.getenv("PORT", "5000")),
        debug=False,
    )
