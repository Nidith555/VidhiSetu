"""VidhiSetu Flask API.

The civilian endpoint collects an intake through one follow-up question at a time.
English (en), Hindi (hi), and Telugu (te) are supported as response languages.
"""

import json
import logging
import os
import re

import requests
from flask import Flask, jsonify, request
from ollama import Client, ResponseError

from legal_news import get_legal_news

app = Flask(__name__)
logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))

OLLAMA_HOST = os.getenv("OLLAMA_HOST", "http://127.0.0.1:11434")
MODEL_NAME = os.getenv("OLLAMA_MODEL", "llama3.2:3b")
RAG_API_URL = os.getenv("VIDHISETU_RAG_URL", "").strip().rstrip("/")
try:
    RAG_TIMEOUT_SECONDS = float(os.getenv("VIDHISETU_RAG_TIMEOUT_SECONDS", "75"))
except (TypeError, ValueError):
    RAG_TIMEOUT_SECONDS = 75.0
ollama_client = Client(host=OLLAMA_HOST)

LANGUAGES = {
    "en": {
        "name": "English",
        "pattern": re.compile(r"[A-Za-z]"),
        "retry": "Write the entire question naturally in English. Do not use another language.",
        "error": "The AI could not create a question in English. Please try again.",
        "service_error": "The AI service is temporarily unavailable. Please try again later.",
    },
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
    return LANGUAGES.get(language, LANGUAGES["en"])[field]


def fallback_question(case_context: dict, language: str):
    """Deterministic guided intake if Ollama is unavailable (not AI advice)."""
    questions = {
        "en": [
            ("timeline", "When did this happen, and what happened first?"),
            ("peopleInvolved", "Who are the people or organisations directly involved?"),
            ("documents", "Do you have documents, messages, receipts, photographs, or other evidence?"),
            ("actionsTaken", "Have you already taken any steps to resolve this? What happened?"),
            ("desiredOutcome", "What outcome are you hoping to achieve?"),
        ],
        "hi": [
            ("timeline", "यह घटना कब हुई और सबसे पहले क्या हुआ?"),
            ("peopleInvolved", "इस मामले में कौन-से लोग या संस्थाएँ सीधे शामिल हैं?"),
            ("documents", "क्या आपके पास कोई दस्तावेज़, संदेश, रसीद, तस्वीर या अन्य सबूत हैं?"),
            ("actionsTaken", "क्या आपने समस्या सुलझाने के लिए पहले कोई कदम उठाया है? क्या हुआ?"),
            ("desiredOutcome", "आप इस मामले में क्या परिणाम चाहते हैं?"),
        ],
        "te": [
            ("timeline", "ఈ సంఘటన ఎప్పుడు జరిగింది? మొదట ఏమి జరిగింది?"),
            ("peopleInvolved", "ఈ విషయంలో నేరుగా సంబంధం ఉన్న వ్యక్తులు లేదా సంస్థలు ఎవరు?"),
            ("documents", "మీ వద్ద పత్రాలు, సందేశాలు, రసీదులు, ఫోటోలు లేదా ఇతర ఆధారాలు ఉన్నాయా?"),
            ("actionsTaken", "సమస్యను పరిష్కరించడానికి మీరు ఇప్పటికే ఏమైనా చర్యలు తీసుకున్నారా? ఏమైంది?"),
            ("desiredOutcome", "ఈ విషయంలో మీరు ఎలాంటి ఫలితాన్ని కోరుకుంటున్నారు?"),
        ],
    }
    for field, question in questions[language]:
        if not case_context.get(field, "").strip():
            return question
    return None


def fallback_response(case_context: dict, language: str):
    """Return a clearly marked deterministic question when AI is offline."""
    question = fallback_question(case_context, language)
    return jsonify({
        "complete": question is None,
        "question": question,
        "fallback": True,
    })


@app.get("/")
def home():
    return jsonify({
        "status": "online",
        "service": "VidhiSetu AI Backend",
        "ai_provider": "ollama",
        "model": MODEL_NAME,
        "supported_languages": ["en", "hi", "te"],
        "legal_rag_configured": bool(RAG_API_URL),
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
        "supported_languages": ["en", "hi", "te"],
        "legal_rag_configured": bool(RAG_API_URL),
    })


@app.post("/api/civilian/question")
def civilian_question():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({"error": "A JSON object is required"}), 400

    language = data.get("language", "en")
    if not isinstance(language, str) or language not in LANGUAGES:
        return jsonify({
            "error": "language must be 'en' (English), 'hi' (Hindi), or 'te' (Telugu)"
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
            app.logger.warning("AI response did not satisfy the selected language; using guided-intake fallback")
            return fallback_response(case_context, language)

        return jsonify({"complete": False, "question": result})

    except ResponseError:
        app.logger.exception("Ollama response error; using guided-intake fallback")
        return fallback_response(case_context, language)
    except (json.JSONDecodeError, ValueError, KeyError, TypeError):
        app.logger.exception("Invalid AI response; using guided-intake fallback")
        return fallback_response(case_context, language)
    except Exception:
        app.logger.exception("Unexpected AI service error; using guided-intake fallback")
        return fallback_response(case_context, language)




# ======================================================================
# Source-grounded legal RAG gateway
# ======================================================================

LEGAL_RAG_ROUTES = {
    "civilian": "/api/v1/civilian/ask",
    "student": "/api/v1/student/ask",
    "lawyer": "/api/v1/lawyer/research",
}

RAG_LANGUAGE_NOTES = {
    "en": "",
    "hi": "ध्यान दें: वर्तमान कानूनी RAG सेवा व्याख्या अंग्रेज़ी में देती है। कानूनी स्रोत और उद्धरण मूल रूप में सुरक्षित रखे गए हैं।",
    "te": "గమనిక: ప్రస్తుతం న్యాయ RAG సేవ వివరణను ఆంగ్లంలో అందిస్తోంది. చట్టపరమైన మూల పాఠ్యం మరియు సూచనలు యథాతథంగా ఉంచబడ్డాయి.",
}


def _build_legal_query(data: dict) -> str:
    """Build one bounded retrieval query from the citizen's collected facts."""
    fields = [
        ("Initial situation", "initialProblem"),
        ("Timeline", "timeline"),
        ("People involved", "peopleInvolved"),
        ("Documents and evidence", "documents"),
        ("Actions already taken", "actionsTaken"),
        ("Desired outcome", "desiredOutcome"),
        ("Additional details", "additionalDetails"),
    ]
    parts = []
    for label, key in fields:
        value = data.get(key, "")
        if value is None:
            continue
        if not isinstance(value, str):
            raise ValueError(f"{key} must be a string")
        value = value.strip()
        if value:
            parts.append(f"{label}: {value}")
    query = "\n".join(parts).strip()
    # The RAG service schema accepts query strings up to 1,000 characters.
    return query[:1000].rstrip()


def _normalise_rag_response(payload: dict, role: str, language: str) -> dict:
    """Convert role-specific RAG response models into one Android-friendly contract."""
    answer_key = {
        "civilian": "simplified_explanation",
        "student": "statutory_analysis",
        "lawyer": "research_synthesis",
    }[role]
    answer = payload.get(answer_key, "")
    if not isinstance(answer, str):
        answer = ""

    takeaways = payload.get("key_takeaways")
    if not isinstance(takeaways, list):
        takeaways = payload.get("principles")
    if not isinstance(takeaways, list):
        takeaways = []
    takeaways = [item.strip() for item in takeaways if isinstance(item, str) and item.strip()]

    citations = payload.get("citations", [])
    if not isinstance(citations, list):
        citations = []
    citations = [item.strip() for item in citations if isinstance(item, str) and item.strip()]

    raw_passages = payload.get("supporting_passages", [])
    if not isinstance(raw_passages, list):
        raw_passages = []
    passages = []
    for item in raw_passages[:10]:
        if not isinstance(item, dict):
            continue
        passages.append({
            "chunk_id": str(item.get("chunk_id", "")),
            "act": str(item.get("act", "")),
            "section_number": item.get("section_number"),
            "section_title": str(item.get("section_title") or ""),
            "chapter": str(item.get("chapter") or ""),
            "page_start": item.get("page_start"),
            "page_end": item.get("page_end"),
            "source": str(item.get("source") or ""),
            "text": str(item.get("text") or "")[:2500],
        })

    disclaimer = payload.get("disclaimer", "")
    if not isinstance(disclaimer, str):
        disclaimer = ""

    status_value = payload.get("status", "UNKNOWN")
    if status_value not in ("SUCCESS", "FALLBACK_DETERMINISTIC", "INSUFFICIENT_EVIDENCE"):
        status_value = "UNKNOWN"

    return {
        "status": status_value,
        "role": role,
        "requested_language": language,
        # The upstream RAG API currently returns English synthesis; official
        # passages and citation strings are passed through without translation.
        "answer_language": "en",
        "language_notice": RAG_LANGUAGE_NOTES[language],
        "answer": answer,
        "takeaways": takeaways,
        "disclaimer": disclaimer,
        "citations": citations,
        "passages": passages,
    }


@app.post("/api/legal/answer")
def legal_answer():
    """Proxy the collected case facts to the source-grounded VidhiSetu RAG API."""
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({"error": "A JSON object is required"}), 400

    language = data.get("language", "en")
    if not isinstance(language, str) or language not in LANGUAGES:
        return jsonify({"error": "language must be 'en', 'hi', or 'te'"}), 400

    role = data.get("role", "civilian")
    if not isinstance(role, str) or role not in LEGAL_RAG_ROUTES:
        return jsonify({"error": "role must be 'civilian', 'student', or 'lawyer'"}), 400

    try:
        query = _build_legal_query(data)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400

    if not query:
        return jsonify({"error": "At least one case detail is required"}), 400

    if not RAG_API_URL:
        return jsonify({
            "error": "The legal RAG service is not configured. Set VIDHISETU_RAG_URL on the backend.",
            "rag_status": "not_configured",
        }), 503

    top_k = data.get("top_k", 3)
    if isinstance(top_k, bool) or not isinstance(top_k, int) or not 1 <= top_k <= 10:
        return jsonify({"error": "top_k must be an integer between 1 and 10"}), 400

    act_filter = data.get("act_filter")
    if act_filter is not None and not isinstance(act_filter, str):
        return jsonify({"error": "act_filter must be a string"}), 400
    if isinstance(act_filter, str):
        act_filter = act_filter.strip()[:120] or None

    upstream_payload = {"query": query, "top_k": top_k}
    if act_filter:
        upstream_payload["act_filter"] = act_filter

    try:
        upstream = requests.post(
            RAG_API_URL + LEGAL_RAG_ROUTES[role],
            json=upstream_payload,
            timeout=(5, RAG_TIMEOUT_SECONDS),
        )
    except requests.Timeout:
        app.logger.warning("Legal RAG request timed out for role %s", role)
        return jsonify({
            "error": "The legal retrieval service took too long to respond. Please try again.",
            "rag_status": "timeout",
        }), 504
    except requests.RequestException:
        app.logger.exception("Could not reach the legal RAG service for role %s", role)
        return jsonify({
            "error": "The legal retrieval service is temporarily unreachable. Please try again.",
            "rag_status": "unreachable",
        }), 502

    if upstream.status_code == 503:
        return jsonify({
            "error": "The legal retrieval engine is not ready yet.",
            "rag_status": "not_ready",
        }), 503
    if not 200 <= upstream.status_code < 300:
        app.logger.warning("Legal RAG returned HTTP %s for role %s", upstream.status_code, role)
        return jsonify({
            "error": "The legal retrieval service could not process this query.",
            "rag_status": "upstream_error",
        }), 502

    try:
        rag_payload = upstream.json()
    except ValueError:
        return jsonify({
            "error": "The legal retrieval service returned an invalid response.",
            "rag_status": "invalid_response",
        }), 502

    if not isinstance(rag_payload, dict):
        return jsonify({
            "error": "The legal retrieval service returned an invalid response.",
            "rag_status": "invalid_response",
        }), 502

    normalised = _normalise_rag_response(rag_payload, role, language)
    if normalised["status"] == "UNKNOWN" or not normalised["answer"]:
        return jsonify({
            "error": "The legal retrieval service returned no usable answer.",
            "rag_status": "invalid_response",
        }), 502

    return jsonify(normalised)


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
