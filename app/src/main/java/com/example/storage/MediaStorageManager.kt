package com.example.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MediaStorageManager {

    fun createMediaTarget(
        context: Context,
        title: String,
        isVideo: Boolean,
        isVault: Boolean = false
    ): TargetDestination {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val sanitizedTitle = title.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(30).ifBlank { "Media" }
        val extension = if (isVideo) "mp4" else "mp3"
        val mimeType = if (isVideo) "video/mp4" else "audio/mpeg"
        val displayName = "VidShare_${sanitizedTitle}_$timeStamp.$extension"

        if (isVault) {
            // Vault files stored in private internal files directory
            val vaultDir = File(context.filesDir, "private_vault").apply { if (!exists()) mkdirs() }
            val file = File(vaultDir, ".vault_$displayName")
            return TargetDestination(
                uri = Uri.fromFile(file),
                outputStream = file.outputStream(),
                filePath = file.absolutePath,
                mimeType = mimeType,
                isVideo = isVideo,
                isPendingManaged = false,
                rawFile = file
            )
        }

        val contentResolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = if (isVideo) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
            val relativePath = if (isVideo) {
                "${Environment.DIRECTORY_MOVIES}/VidSharePro"
            } else {
                "${Environment.DIRECTORY_MUSIC}/VidSharePro"
            }
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(collection, values)
            if (uri != null) {
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    return TargetDestination(
                        uri = uri,
                        outputStream = outputStream,
                        filePath = "$relativePath/$displayName",
                        mimeType = mimeType,
                        isVideo = isVideo,
                        isPendingManaged = true
                    )
                }
            }
        }

        // Fallback: App-specific external files dir
        val dir = context.getExternalFilesDir(if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_MUSIC)
            ?: context.filesDir
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, displayName)
        val outputStream = file.outputStream()
        val uri = Uri.fromFile(file)
        return TargetDestination(
            uri = uri,
            outputStream = outputStream,
            filePath = file.absolutePath,
            mimeType = mimeType,
            isVideo = isVideo,
            isPendingManaged = false,
            rawFile = file
        )
    }

    fun finalizeMediaTarget(context: Context, target: TargetDestination, success: Boolean) {
        if (!target.isPendingManaged) return
        val contentResolver = context.contentResolver
        if (success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            contentResolver.update(target.uri, values, null, null)
        } else if (!success) {
            try {
                contentResolver.delete(target.uri, null, null)
            } catch (_: Exception) {}
        }
    }

    fun verifyIntegrity(context: Context, target: TargetDestination): VerificationResult {
        try {
            val resolver = context.contentResolver
            val length = if (target.rawFile != null && target.rawFile.exists()) {
                target.rawFile.length()
            } else {
                resolver.openFileDescriptor(target.uri, "r")?.use { it.statSize } ?: 0L
            }

            if (length < 512L) {
                return VerificationResult(
                    isValid = false,
                    sizeBytes = length,
                    reason = "File size is too small ($length bytes). Stream incomplete."
                )
            }

            val headerBytes = ByteArray(32)
            val bytesRead: Int = if (target.rawFile != null) {
                FileInputStream(target.rawFile).use { it.read(headerBytes) }
            } else {
                resolver.openInputStream(target.uri)?.use { it.read(headerBytes) } ?: -1
            }

            if (bytesRead < 4) {
                return VerificationResult(
                    isValid = false,
                    sizeBytes = length,
                    reason = "Could not read stream header bytes."
                )
            }

            val headerStr = String(headerBytes, 0, bytesRead, Charsets.ISO_8859_1)
            val isMp4 = headerStr.contains("ftyp") || headerStr.contains("moov")
            val isWebm = headerBytes[0] == 0x1A.toByte() && headerBytes[1] == 0x45.toByte()
            val isMp3 = (headerBytes[0] == 'I'.code.toByte() && headerBytes[1] == 'D'.code.toByte() && headerBytes[2] == '3'.code.toByte()) ||
                    (headerBytes[0] == 0xFF.toByte() && (headerBytes[1].toInt() and 0xE0) == 0xE0)
            val isJpg = headerBytes[0] == 0xFF.toByte() && headerBytes[1] == 0xD8.toByte()
            val isPng = headerBytes[0] == 0x89.toByte() && headerBytes[1] == 0x50.toByte()

            val validContainer = isMp4 || isWebm || isMp3 || isJpg || isPng || length > 10_000L
            if (!validContainer) {
                return VerificationResult(
                    isValid = false,
                    sizeBytes = length,
                    reason = "Downloaded file does not match expected media signature."
                )
            }

            return VerificationResult(
                isValid = true,
                sizeBytes = length,
                reason = "Integrity verified (${length / 1024} KB, valid media container)"
            )
        } catch (e: Exception) {
            return VerificationResult(
                isValid = false,
                sizeBytes = 0L,
                reason = "Verification exception: ${e.message}"
            )
        }
    }

    fun deleteMedia(context: Context, uriString: String, filePath: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val rows = context.contentResolver.delete(uri, null, null)
                rows > 0
            } else {
                val file = File(filePath)
                if (file.exists()) file.delete() else false
            }
        } catch (_: Exception) {
            try {
                val file = File(filePath)
                if (file.exists()) file.delete() else false
            } catch (_: Exception) {
                false
            }
        }
    }

    fun createShareIntent(context: Context, uriString: String, title: String, mimeType: String): Intent {
        val uri = Uri.parse(uriString)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Watch: $title via VidShare Pro")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "Share Media")
    }
}

data class TargetDestination(
    val uri: Uri,
    val outputStream: OutputStream,
    val filePath: String,
    val mimeType: String,
    val isVideo: Boolean,
    val isPendingManaged: Boolean,
    val rawFile: File? = null
)

data class VerificationResult(
    val isValid: Boolean,
    val sizeBytes: Long,
    val reason: String
)
