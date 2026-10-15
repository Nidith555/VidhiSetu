# VidhiSetu Flask Backend

This directory contains the Flask API currently available from the existing VidhiSetu backend source.

## Features in this initial snapshot

- `GET /` — service status and supported language codes.
- `GET /api/health` — checks API and Ollama availability.
- `POST /api/civilian/question` — civilian legal-intake follow-up question generation. The endpoint asks one question at a time and accepts Hindi (`hi`) or Telugu (`te`) as the selected response language.
- `GET /api/legal-news` — legal updates aggregated from the Press Information Bureau RSS feed and the Supreme Court of India website.

## Requirements

- Python 3.10+
- Ollama reachable from the backend
- The model specified by `OLLAMA_MODEL` pulled into that Ollama instance (defaults to `llama3.2:3b`)

## Run locally

From this directory:

```bash
python -m venv .venv
# Windows PowerShell:
.venv\Scripts\Activate.ps1
# macOS/Linux:
# source .venv/bin/activate

pip install -r requirements.txt
```

Configure environment variables as needed:

- `OLLAMA_HOST` — defaults to `http://127.0.0.1:11434`
- `OLLAMA_MODEL` — defaults to `llama3.2:3b`
- `PORT` — defaults to `5000`

Start the service:

```bash
python app.py
```

For a production WSGI server, use:

```bash
gunicorn app:app
```

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

Set `language` to `hi` for Hindi or `te` for Telugu. The backend prompt and a basic selected-script check are intended to reduce accidental language switching; the script check is not a complete language-quality validator.

## Deployment note

A hosted Flask server does not automatically have access to Ollama running on a developer's laptop. Set `OLLAMA_HOST` to an Ollama service reachable from the deployment, or configure another supported AI provider before expecting the AI endpoint to work in the cloud. The root health/status endpoint alone does not prove that AI generation works.

## Current limitations

- This is an initial API snapshot, not the complete VidhiSetu application.
- Hindi/Telugu support is currently applied to civilian intake follow-up questions only; other AI features must pass and enforce the language too.
- Android app source, AI-agent implementation, and the complete RAG implementation have not yet been added to this repository.
- Legal news scraping can return fewer results when upstream sites change their markup or are unavailable. Review outputs before relying on them.
- This project provides informational assistance and must not be represented as a substitute for advice from a qualified lawyer.
