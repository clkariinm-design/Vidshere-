package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_items")
data class DownloadedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalUrl: String,
    val canonicalUrl: String,
    val title: String,
    val author: String,
    val localUri: String,
    val filePath: String,
    val fileSize: Long,
    val durationSeconds: Long = 0,
    val isVideo: Boolean = true,
    val mimeType: String = "video/mp4",
    val thumbnailUrl: String? = null,
    val downloadTimestamp: Long = System.currentTimeMillis(),
    val isVaultProtected: Boolean = false
)

enum class MediaCategory {
    VIDEO,
    AUDIO,
    PHOTO,
    APP,
    DOCUMENT
}

data class MediaFile(
    val id: String,
    val name: String,
    val path: String,
    val uri: String,
    val sizeBytes: Long,
    val category: MediaCategory,
    val mimeType: String,
    val packageName: String? = null,
    val dateModified: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val isSelected: Boolean = false
)

enum class TransferStatus {
    PENDING,
    TRANSFERRING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class TransferItem(
    val id: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val transferredBytes: Long = 0,
    val progress: Float = 0f,
    val speedMBps: Double = 0.0,
    val isSending: Boolean = true,
    val status: TransferStatus = TransferStatus.PENDING,
    val localPath: String = ""
)

data class PeerDevice(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 8988,
    val isHotspot: Boolean = false,
    val isConnected: Boolean = false
)

data class StatusMedia(
    val id: String,
    val name: String,
    val uri: String,
    val filePath: String,
    val isVideo: Boolean,
    val fileSize: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false
)
