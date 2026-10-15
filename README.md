# VidhiSetu

**Multilingual AI-assisted legal information for India.**

VidhiSetu is an Android application project intended to make legal information easier to access for citizens, law students, and legal professionals.

## Repository status

The repository includes the Android Kotlin source, Android manifest, XML layouts, drawable resources, app-level Gradle configuration, and an initial Flask backend snapshot. The Android build configuration is being validated by a GitHub Actions workflow; the APK must not be considered build-verified until that workflow passes.

## Project structure

```text
VidhiSetu/
├── android-app/
│   ├── app/
│   │   ├── src/main/AndroidManifest.xml
│   │   ├── src/main/java/com/example/vidhisetu/
│   │   └── src/main/res/{drawable,layout,values}/
│   ├── gradle/libs.versions.toml
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle.properties
├── backend/
│   ├── app.py
│   ├── legal_news.py
│   ├── requirements.txt
│   └── README.md
└── .github/workflows/
    ├── backend-check.yml
    └── android-build.yml
```

## Build the Android app

Open the **android-app** directory in Android Studio (not the repository root). Install Android SDK Platform 35 if prompted, then allow Gradle sync to finish. The CI workflow builds the debug APK using Gradle 8.11.1 and JDK 17 and publishes it as a downloadable workflow artifact if the build succeeds.

The standard Gradle wrapper launch scripts are present. If Android Studio reports a missing wrapper JAR, use Android Studio's Gradle distribution setting or report the error so the missing wrapper component can be restored.

## Backend

The initial Flask backend provides:

- `GET /` — service status and supported language codes.
- `GET /api/health` — API and Ollama availability check.
- `POST /api/civilian/question` — legal-intake follow-up questions with Hindi (`hi`) or Telugu (`te`) response-language instructions and a basic script check.
- `GET /api/legal-news` — a news feed based on PIB RSS and Supreme Court of India updates.

See [backend/README.md](backend/README.md) for setup and limitations. A hosted Flask service cannot reach Ollama running only on a developer's laptop; the deployed backend needs an AI service reachable from its hosting environment.

## Not yet complete or verified

- A successful Android build and device/emulator test are still required.
- The AI-agent orchestration and full RAG/legal corpus implementation must be integrated from the team's actual source.
- Complete Hindi/Telugu UI localization and language-aware speech behaviour are not finished.
- AI-generated legal information can be incomplete or wrong; statutory text and citations need verification against official sources.

## Security

Never commit API keys, passwords, access tokens, Firebase service-account files, keystores, `google-services.json`, `local.properties`, real user case records, or personal information.
