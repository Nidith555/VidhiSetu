# VidhiSetu

**Multilingual AI-powered legal assistance platform for India.**

VidhiSetu is an Android-focused legal assistance project intended to make legal information more accessible to citizens, law students, and legal professionals.

## Repository status

This repository now includes the Kotlin source files supplied for the Android application and an initial Flask backend snapshot. It is **not yet a complete, buildable Android Studio project**: the Android manifest, XML layouts, drawable/mipmap assets, values/resources, and app-level Gradle configuration still need to be added. AI-agent and full RAG implementations also remain to be incorporated when their source code is available.

## Current structure

```text
VidhiSetu/
├── android-app/
│   └── app/src/main/java/com/example/vidhisetu/
│       └── [Android Kotlin source files]
├── backend/
│   ├── app.py
│   ├── legal_news.py
│   ├── requirements.txt
│   └── README.md
├── .github/workflows/backend-check.yml
├── .gitignore
└── README.md
```

## Android source included

Kotlin source files supplied so far include the main activity, student and lawyer screens, case and document-management screens, legal information/news screens, mock-trial and practice screens, speech support, and the Flask AI question-planner client.

The source uses the package `com.example.vidhisetu` and several activities use View Binding. To build the Android application, the matching XML layout/resource files and module configuration must also be committed.

## Backend

The initial Flask backend provides:

- `GET /` — service status and supported language codes.
- `GET /api/health` — checks API and Ollama availability.
- `POST /api/civilian/question` — asks one legal-intake follow-up question at a time, with Hindi (`hi`) or Telugu (`te`) selected response-language instructions and a basic script check.
- `GET /api/legal-news` — aggregates updates from the PIB RSS feed and the Supreme Court of India website.

See [backend/README.md](backend/README.md) for setup, environment variables, API examples, and known limitations.

**Deployment note:** a hosted Flask service does not automatically have access to Ollama running on a developer's laptop. Configure an Ollama service reachable from the deployment or another supported AI provider before expecting hosted AI generation to work. The language instruction and script check are not a guarantee of translation accuracy and currently apply only to the civilian intake endpoint.

## Planned / not yet included

- Android XML layouts, manifest, resources, and app-level Gradle configuration.
- AI-agent implementation and orchestration.
- Full RAG implementation and legal corpus owned by the relevant teammate.
- Complete localization of all screens and voice features in Hindi and Telugu.
- Automated integration tests for the Android app and end-to-end AI flow.

The team's RAG implementation should be integrated with its owner's agreement and correct attribution. Do not present planned features as completed functionality.

## Safety and privacy

- Never commit API keys, access tokens, passwords, Firebase service-account files, keystores, `google-services.json`, or local machine configuration.
- Keep real user legal records and personal information out of fixtures and public commits.
- AI-generated legal information can be incomplete or incorrect. Verify statutes and citations against official sources and consult a qualified lawyer for case-specific advice.

## Contribution workflow

1. Work on a dedicated branch.
2. Keep commits focused and descriptive.
3. Add or update tests when changing backend behaviour.
4. Build and test locally before claiming a feature is complete.
5. Review all files for credentials and personal data before pushing.
