# VidhiSetu

**Multilingual AI-assisted legal information for India.**

VidhiSetu is an Android application project intended to make legal information easier to access for citizens, law students, and legal professionals.

## Current project status

The Android project builds through GitHub Actions. The Ask VidhiSetu flow supports English, Hindi, and Telugu intake, uses the phone's speech recognition and text-to-speech, and sends completed case facts to the hosted Flask API. The app displays retrieved legal information, citation verification status, and lawyer-escalation guidance when the backend returns it.

The **Scan & Explain** screen uploads PDF, PNG, or JPEG documents to the document-analysis endpoint. It presents OCR-extracted statements as unverified and shows legal-source results separately. The **Document Drafting** screen calls the legal-draft endpoint; it only enables copying or sharing when the backend returns a ready draft with verified citations.

The Android app uses native device speech recognition and text-to-speech. The optional server-side audio endpoint requires a configured speech-to-text provider and is not needed for the current app flow. Every screen should be reviewed for language coverage; only the supported Ask flow is fully connected to the English/Hindi/Telugu language selection.

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

The latest Android build workflow is available here:

[Open Android build workflow](https://github.com/Nidith555/VidhiSetu/actions)

The `vidhisetu-debug-apk` artifact contains `app-debug.apk`. GitHub Actions artifacts expire after the configured retention period.

To build locally, open the **android-app** directory in Android Studio, install Android SDK Platform 35 if prompted, and allow Gradle sync to finish.

## Backend and AI availability

The deployed API source is maintained in the private repository [Nidith555/vidhisetu-backend](https://github.com/Nidith555/vidhisetu-backend). The Android app calls the Render service at `https://vidhisetu-backend.onrender.com`.

The hosted Flask service needs a reachable `VIDHISETU_RAG_URL` and configured model/provider settings for legal retrieval, verification, translation, and draft generation. Scanned documents need a server OCR provider or Tesseract. The optional server-side voice endpoint needs an STT provider. Configure these on Render; never place credentials in Android or source control. If an upstream service is unavailable, the app shows the returned service status rather than inventing legal information.

## Security and legal safety

Never commit API keys, passwords, tokens, Firebase service-account files, keystores, `google-services.json`, or `local.properties`. Keep real personal legal records out of public commits. Extracted document text is not independently verified. Review every draft and verify citations against official sources; consult a qualified lawyer for case-specific advice.
