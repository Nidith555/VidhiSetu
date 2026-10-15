package com.example.vidhisetu

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vidhisetu.databinding.ActivityScanExplainBinding

class ScanExplainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanExplainBinding

    private lateinit var aiSpeechController: AiSpeechController

    // Selected file
    private var selectedFileUri: Uri? = null

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
                arrayOf(
                    "image/*",
                    "application/pdf",
                    "text/plain",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
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

        val hasCameraDocument =
            capturedBitmap != null

        val hasFile =
            selectedFileUri != null

        if (!hasCameraDocument && !hasFile) {

            Toast.makeText(
                this,
                "Please capture or select a document first.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        binding.explanationProgress.visibility =
            View.VISIBLE

        binding.explainButton.isEnabled =
            false

        binding.explanationText.text =
            "Reading your document..."

        /*
         * TEMPORARY AI DEMO
         *
         * Actual document analysis will later be:
         *
         * Camera/File
         *      ↓
         * OCR / Document extraction
         *      ↓
         * Legal document analysis
         *      ↓
         * Verified legal explanation
         *      ↓
         * Text + TTS
         */

        binding.explanationProgress.postDelayed({

            binding.explanationProgress.visibility =
                View.GONE

            binding.explainButton.isEnabled =
                true

            currentExplanation =
                """
                Your document has been received successfully.

                VidhiSetu will analyse the document and explain its important information in simple language.

                The analysis can identify important dates, names, amounts, responsibilities, conditions, and other relevant information.

                The actual document-analysis AI will be connected in the next development phase.

                This information is for understanding the document and is not a substitute for professional legal advice.
                """.trimIndent()

            // Display explanation
            binding.explanationText.text =
                currentExplanation

            // Speak exactly the same explanation
            aiSpeechController.speak(
                currentExplanation
            )

        }, 1000)
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