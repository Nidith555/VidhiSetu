package com.example.vidhisetu

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.InputStream
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.vidhisetu.databinding.ActivityScanExplainBinding

class ScanExplainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanExplainBinding

    private lateinit var aiSpeechController: AiSpeechController

    // Selected file
    private var selectedFileUri: Uri? = null
    private var isAnalyzing = false

    // Camera image
    private var capturedBitmap: Bitmap? = null

    // Current explanation
    private var currentExplanation = ""

    // =========================================================
    // FILE PICKER
    // =========================================================

    private val filePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                selectedFileUri = uri
                capturedBitmap = null

                // Keep access to the selected file URI
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Some providers don't support persistable permission.
                    // Temporary access is still available for this activity.
                }

                handleSelectedFile(uri)
            }
        }

    // =========================================================
    // CAMERA
    // =========================================================

    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (bitmap != null) {

                capturedBitmap = bitmap
                selectedFileUri = null

                binding.documentPreview
                    .setImageBitmap(bitmap)

                binding.documentStatusText.text =
                    "Document captured from camera"

                currentExplanation = ""

                binding.explanationText.text =
                    "Document captured successfully. Tap Explain Document to continue."

            } else {

                Toast.makeText(
                    this,
                    "No document was captured.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // =========================================================
    // CAMERA PERMISSION
    // =========================================================

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                openCamera()

            } else {

                Toast.makeText(
                    this,
                    "Camera permission is required to scan a document.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityScanExplainBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        setupSpeech()

        setupClicks()
    }

    // =========================================================
    // TTS
    // =========================================================

    private fun setupSpeech() {

        aiSpeechController =
            AiSpeechController(
                context = this,

                onStart = {

                    runOnUiThread {

                        binding.listenExplanationButton.text =
                            "🔊  Speaking..."
                    }
                },

                onComplete = {

                    runOnUiThread {

                        binding.listenExplanationButton.text =
                            "🔊  Listen"
                    }
                }
            )
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private fun setupClicks() {

        // Back
        binding.backButton.setOnClickListener {

            finish()
        }

        // Camera
        binding.cameraButton.setOnClickListener {

            requestCameraPermission()
        }

        // File
        binding.galleryButton.setOnClickListener {

            openFilePicker()
        }

        // Explain
        binding.explainButton.setOnClickListener {

            explainDocument()
        }

        // Listen
        binding.listenExplanationButton.setOnClickListener {

            if (currentExplanation.isBlank()) {

                Toast.makeText(
                    this,
                    "There is no explanation to read yet.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                aiSpeechController.speak(
                    currentExplanation
                )
            }
        }
    }

    // =========================================================
    // FILE PICKER
    // =========================================================

    private fun openFilePicker() {

        try {

            filePickerLauncher.launch(
                arrayOf("application/pdf", "image/png", "image/jpeg")
            )

        } catch (exception: Exception) {

            // Fallback for devices/providers that don't support
            // multiple MIME filters.

            try {

                filePickerLauncher.launch(
                    arrayOf("*/*")
                )

            } catch (secondException: Exception) {

                Toast.makeText(
                    this,
                    "Unable to open the file picker.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =========================================================
    // HANDLE SELECTED FILE
    // =========================================================

    private fun handleSelectedFile(
        uri: Uri
    ) {

        val mimeType =
            contentResolver.getType(uri)
                ?: ""

        val fileName =
            getFileName(
                contentResolver,
                uri
            )

        binding.documentStatusText.text =
            "Selected: $fileName"

        currentExplanation = ""

        /*
         * If the selected file is an image,
         * show it directly in the preview.
         */

        if (mimeType.startsWith("image/")) {

            binding.documentPreview
                .setImageURI(uri)

            binding.explanationText.text =
                "Image selected successfully. Tap Explain Document to continue."

        } else {

            /*
             * PDF / Word / text / other documents
             * cannot be displayed directly by ImageView.
             *
             * Show a document indicator instead.
             */

            binding.documentPreview
                .setImageResource(
                    android.R.drawable.ic_menu_agenda
                )

            binding.explanationText.text =
                """
                File selected successfully.

                File:
                $fileName

                Tap Explain Document to continue.
                """.trimIndent()
        }
    }

    // =========================================================
    // GET FILE NAME
    // =========================================================

    private fun getFileName(
        resolver: ContentResolver,
        uri: Uri
    ): String {

        var fileName: String? = null

        resolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->

            val nameIndex =
                cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )

            if (
                nameIndex >= 0 &&
                cursor.moveToFirst()
            ) {

                fileName =
                    cursor.getString(
                        nameIndex
                    )
            }
        }

        return fileName
            ?: "Selected document"
    }

    // =========================================================
    // CAMERA PERMISSION
    // =========================================================

    private fun requestCameraPermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            openCamera()

        } else {

            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    // =========================================================
    // OPEN CAMERA
    // =========================================================

    private fun openCamera() {

        try {

            cameraLauncher.launch(
                null
            )

        } catch (exception: Exception) {

            Toast.makeText(
                this,
                "Unable to open the camera.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================================================
    // EXPLAIN DOCUMENT
    // =========================================================

    private fun explainDocument() {
        if (isAnalyzing) return

        val uri = selectedFileUri
        val bitmap = capturedBitmap
        if (uri == null && bitmap == null) {
            Toast.makeText(this, "Please capture or select a document first.", Toast.LENGTH_SHORT).show()
            return
        }

        isAnalyzing = true
        binding.explanationProgress.visibility = View.VISIBLE
        binding.explainButton.isEnabled = false
        binding.explanationText.text = "Uploading the document securely for analysis..."

        lifecycleScope.launch {
            try {
                val fileName: String
                val mimeType: String
                val content: ByteArray

                if (bitmap != null) {
                    fileName = "camera-scan.jpg"
                    mimeType = "image/jpeg"
                    content = ByteArrayOutputStream().use { output ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
                        output.toByteArray()
                    }
                } else {
                    val selectedUri = uri ?: error("No document was selected.")
                    fileName = getFileName(contentResolver, selectedUri)
                    val resolvedMime = contentResolver.getType(selectedUri).orEmpty()
                    mimeType = resolvedMime.takeIf { it.isNotBlank() && it != "application/octet-stream" }
                        ?: mimeTypeFor(fileName)
                    content = contentResolver.openInputStream(selectedUri)?.use { input ->
                        readLimited(input)
                    } ?: error("The selected document could not be opened.")
                }

                val result = DocumentAnalysisClient().analyze(
                    fileName = fileName,
                    mimeType = mimeType,
                    content = content,
                    language = AppLanguage.getCode(this@ScanExplainActivity)
                )
                currentExplanation = buildExplanation(result)
                binding.explanationText.text = currentExplanation
                binding.documentStatusText.text = "Analysis complete: " + fileName
                if (result.legalAnswer.isNotBlank()) {
                    aiSpeechController.speak(result.legalAnswer)
                }
            } catch (exception: Exception) {
                currentExplanation = exception.message ?: "The document could not be analyzed. Please try again."
                binding.explanationText.text = currentExplanation
                binding.documentStatusText.text = "Document analysis unavailable"
                Toast.makeText(this@ScanExplainActivity, currentExplanation, Toast.LENGTH_LONG).show()
            } finally {
                isAnalyzing = false
                binding.explanationProgress.visibility = View.GONE
                binding.explainButton.isEnabled = true
            }
        }
    }

    private fun readLimited(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= BackendApiSettings.MAX_UPLOAD_BYTES) {
                "The document is larger than the 10 MB upload limit."
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun mimeTypeFor(fileName: String): String = when (fileName.substringAfterLast('.', "").lowercase()) {
        "pdf" -> "application/pdf"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        else -> "application/octet-stream"
    }

    private fun buildExplanation(result: DocumentAgentResult): String = buildString {
        append("Document type: ")
        append(result.documentType.replace('_', ' '))
        if (result.summary.isNotBlank()) {
            append("\n\nSummary\n")
            append(result.summary)
        }
        if (result.parties.isNotEmpty()) {
            append("\n\nParties mentioned\n• ")
            append(result.parties.joinToString("\n• "))
        }
        if (result.dates.isNotEmpty()) {
            append("\n\nDates mentioned\n• ")
            append(result.dates.joinToString("\n• "))
        }
        if (result.legalSections.isNotEmpty()) {
            append("\n\nLegal references found in the document\n• ")
            append(result.legalSections.joinToString("\n• "))
        }
        if (result.extractedFacts.isNotEmpty()) {
            append("\n\nExtracted statements (not independently verified)")
            result.extractedFacts.take(8).forEach { fact ->
                append("\n• ")
                append(fact.text)
                if (fact.pageReference.isNotBlank()) append(" (" + fact.pageReference + ")")
            }
        }
        if (result.legalAnswer.isNotBlank()) {
            append("\n\nLegal information from retrieved sources\n")
            append(result.legalAnswer)
        } else if (result.legalError.isNotBlank()) {
            append("\n\nLegal source search\n")
            append(result.legalError)
        }
        if (result.verificationStatus.isNotBlank()) {
            append("\n\nCitation support check: ")
            append(result.verificationStatus)
        }
        if (result.verifiedCitations.isNotEmpty()) {
            append("\nVerified citations\n• ")
            append(result.verifiedCitations.joinToString("\n• "))
        }
        if (result.escalationRequired) {
            append("\n\nHuman legal support recommended")
            if (result.escalationReason.isNotBlank()) append("\n" + result.escalationReason)
            if (result.recommendedAction.isNotBlank()) append("\n" + result.recommendedAction)
        }
        if (result.languageNotice.isNotBlank()) {
            append("\n\n")
            append(result.languageNotice)
        }
        if (result.warnings.isNotEmpty()) {
            append("\n\nReview notes\n• ")
            append(result.warnings.joinToString("\n• "))
        }
        if (result.disclaimer.isNotBlank()) {
            append("\n\n")
            append(result.disclaimer)
        }
        append("\n\nOCR and extracted statements do not independently verify the facts in a document.")
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroy() {

        if (
            ::aiSpeechController.isInitialized
        ) {

            aiSpeechController.shutdown()
        }

        super.onDestroy()
    }
}