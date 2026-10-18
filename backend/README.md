# VidhiSetu Flask Backend

This directory is the backend source snapshot for VidhiSetu. The Render deployment source is maintained in the private repository [Nidith555/vidhisetu-backend](https://github.com/Nidith555/vidhisetu-backend).

## Endpoints

- `GET /` — service status and supported languages.
- `GET /api/health` — Flask API and Ollama availability.
- `POST /api/civilian/question` — one fact-gathering follow-up question at a time.
- `POST /api/legal/answer` — sends the completed case facts to the separate source-grounded RAG API and returns an answer, citations, and supporting legal passages.
- `GET /api/legal-news` — legal updates aggregated from PIB RSS and Supreme Court of India updates.

The civilian intake supports **English (`en`), Hindi (`hi`), and Telugu (`te`)**. The Android app sends the selected language with each question request.

## Run locally

Requires Python 3.10+ and the dependencies in `requirements.txt`.

```bash
python -m venv .venv
# Windows PowerShell:
.venv\Scripts\Activate.ps1
# macOS/Linux:
# source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

Configuration:
- `OLLAMA_HOST` — defaults to `http://127.0.0.1:11434`.
- `OLLAMA_MODEL` — defaults to `llama3.2:3b`.
- `VIDHISETU_RAG_URL` — **required for legal answers**; base URL of the separately hosted FastAPI RAG service, for example `https://your-rag-service.example.com` (no trailing slash and no `/api/v1/...` suffix).
- `VIDHISETU_RAG_TIMEOUT_SECONDS` — optional RAG response timeout; defaults to `75` seconds.
- `PORT` — defaults to `5000`.

## Connect the RAG service

The Android app posts completed case facts to `POST /api/legal/answer` on this Flask backend. This endpoint proxies the case summary to the teammate's FastAPI service:

- civilian: `/api/v1/civilian/ask`
- student: `/api/v1/student/ask`
- lawyer: `/api/v1/lawyer/research`

The Flask gateway normalises those role-specific response shapes into `answer`, `takeaways`, `citations`, and `passages` for Android. It returns a clear 503 response when `VIDHISETU_RAG_URL` is not configured; it never fabricates a legal answer as a fallback.

### Local integration test

Run the teammate's RAG service first in its project folder (model initialisation can take time):

```powershell
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Then, in a separate terminal for this Flask backend:

```powershell
$env:VIDHISETU_RAG_URL = "http://127.0.0.1:8000"
python app.py
```

Use `http://127.0.0.1:5000/api/legal/answer` to test the gateway. A locally running Android emulator should use the host's reachable backend address; a physical phone must be on a reachable network or use the deployed URL.

**Deployment requirement:** The FastAPI RAG service must be deployed separately and reachable from the Flask service. Configure `VIDHISETU_RAG_URL` in the Flask host's environment. A URL value in GitHub source code is not a deployment setting.

**Language limitation:** The current teammate RAG response schemas produce English synthesis. The gateway preserves Hindi/Telugu selection and returns a localized notice, but statutory excerpts and the RAG answer are not translated by this integration. Do not present an English answer as verified Hindi/Telugu legal translation.

## AI availability and fallback

When Ollama responds correctly, the API requests one adaptive question from the model. If Ollama is unavailable or returns an invalid response, the endpoint returns a deterministic, localized guided-intake question with `"fallback": true`. This keeps the fact-collection flow usable while clearly identifying that the question was not AI-generated. The fallback is **not legal advice** and does not use the team's RAG system.

A hosted Flask server cannot reach Ollama running only on a developer's laptop. To enable model-generated questions in deployment, configure `OLLAMA_HOST` to an Ollama service reachable from the deployed server and ensure `OLLAMA_MODEL` is installed there.

## Example request

```json
{
  "language": "te",
  "initialProblem": "నా జీతం ఇవ్వడం లేదు",
  "timeline": "",
  "peopleInvolved": "",
  "documents": "",
  "actionsTaken": "",
  "desiredOutcome": "",
  "questionCount": 0
}
```

The script check helps catch obvious language switching; it is not a complete language-identification or translation-quality guarantee.

## Limitations

- The legal corpus and model are served by the separate FastAPI RAG repository; this Flask backend requires `VIDHISETU_RAG_URL` to be set to that service.
- Legal news scraping may return fewer results if upstream sites are unavailable or change markup.
- Verify statutory text and citations against official sources. VidhiSetu is informational and is not a substitute for a qualified lawyer.
