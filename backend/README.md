# VidhiSetu Flask Backend

This directory is the backend source snapshot for VidhiSetu. The Render deployment source is maintained in the private repository [Nidith555/vidhisetu-backend](https://github.com/Nidith555/vidhisetu-backend).

## Endpoints

- `GET /` — service status and supported languages.
- `GET /api/health` — Flask API and Ollama availability.
- `POST /api/civilian/question` — one fact-gathering follow-up question at a time.
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
- `PORT` — defaults to `5000`.

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

- The RAG/legal corpus and AI-agent orchestration have not been merged into this snapshot; they remain with the relevant teammates.
- Legal news scraping may return fewer results if upstream sites are unavailable or change markup.
- Verify statutory text and citations against official sources. VidhiSetu is informational and is not a substitute for a qualified lawyer.
