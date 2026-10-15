# VidhiSetu

**Multilingual AI-assisted legal information for India.**

VidhiSetu is an Android application project intended to make legal information easier to access for citizens, law students, and legal professionals.

## Current project status

The Android project is configured and the debug APK successfully built in GitHub Actions. The Ask VidhiSetu intake now has an English/Hindi/Telugu language selector connected to the backend request, speech recognition, and text-to-speech. The backend accepts those three language codes and provides a clearly marked, deterministic intake fallback if Ollama is unavailable.

This does **not** mean every static label across every screen is translated, or that the team's full RAG/legal knowledge base and AI-agent orchestration are complete. Those still need implementation and integration.

## Repository structure

```text
VidhiSetu/
├── android-app/
│   ├── app/src/main/java/com/example/vidhisetu/
│   ├── app/src/main/res/{drawable,drawable-nodpi,layout,values}/
│   ├── app/build.gradle.kts
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/libs.versions.toml
├── backend/
│   ├── app.py
│   ├── legal_news.py
│   ├── requirements.txt
│   └── tests/test_app.py
└── .github/workflows/
```

## Build and download

The latest successful Android build workflow is available here:

[Open Android build workflow](https://github.com/Nidith555/VidhiSetu/actions)

The `vidhisetu-debug-apk` artifact contains `app-debug.apk`. GitHub Actions artifacts expire after the configured retention period; the local APK from the verified run is available in this conversation if needed.

To build locally, open the **android-app** directory in Android Studio, install Android SDK Platform 35 if prompted, and allow Gradle sync to finish.

## Backend and AI availability

The deployed API source is maintained in the private repository [Nidith555/vidhisetu-backend](https://github.com/Nidith555/vidhisetu-backend). A cloud-hosted Flask process cannot access Ollama running only on a developer's laptop. Configure an Ollama service reachable from the deployed backend to enable model-generated questions. Without it, the server uses a labelled deterministic guided questionnaire to collect facts; this is not an AI-generated response or legal advice.

## Security and legal safety

Never commit API keys, passwords, tokens, Firebase service-account files, keystores, `google-services.json`, or `local.properties`. Keep real personal legal records out of public commits. Verify statutes and citations against official sources and consult a qualified lawyer for case-specific advice.
