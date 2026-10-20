# VidhiSetu Flask Backend

This folder is a source snapshot. The active Render deployment is maintained in the private repository [Nidith555/vidhisetu-backend](https://github.com/Nidith555/vidhisetu-backend); use that repository for the current implementation and deployment changes.

## Android-connected endpoints

- `GET /` — service status and supported languages.
- `GET /api/health` — backend and model-provider availability.
- `POST /api/civilian/question` — one localized intake question at a time.
- `POST /api/legal/answer` — source-grounded answer with citation verification, translation status, and lawyer-escalation guidance.
- `POST /api/documents/analyze` — PDF/PNG/JPEG extraction and document analysis; extracted statements remain explicitly unverified.
- `POST /api/legal/draft` — review-only draft generated only when required facts and legal support pass the backend checks.
- `POST /api/voice/ask` — optional audio upload flow; server STT/TTS requires configured providers.
- `GET /api/legal-news` — legal updates from configured sources.

The Android app uses the deployed service at `https://vidhisetu-backend.onrender.com`. Its current voice input and spoken responses use Android's native speech APIs; no backend STT provider is needed for that client flow.

## RAG connection and provider configuration

The legal API routes requests through the separately hosted RAG service. Configure `VIDHISETU_RAG_URL` on the host running the Flask backend. The host also needs a reachable language-model provider for model-assisted reasoning and translation. Never put these settings or credentials in the Android app or GitHub source.

Image OCR needs a server OCR provider or a Tesseract executable. Server-side voice uploads need `VIDHISETU_STT_PROVIDER`; server-generated audio is optional and uses `VIDHISETU_TTS_PROVIDER`. When required providers are unavailable, the API returns a clear error or safe fallback instead of fabricating legal support.

See the private backend repository's README for current local setup, Render settings, provider configuration, and endpoint schemas.

## Legal safety

Document extraction does not establish that a statement is true. Drafts are for review and must not be filed without checking facts, local requirements, and cited sources. Verify statutes and citations against official sources and consult a qualified lawyer for case-specific advice.
