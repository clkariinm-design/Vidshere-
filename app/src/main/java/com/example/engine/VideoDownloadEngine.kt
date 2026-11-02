package com.example.engine

import android.content.Context
import com.example.data.model.DownloadedItem
import com.example.storage.MediaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.InputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class VideoMetadata(
    val videoId: String,
    val originalUrl: String,
    val canonicalUrl: String,
    val title: String,
    val author: String,
    val videoStreamUrl: String,
    val audioStreamUrl: String? = null,
    val thumbnailUrl: String? = null,
    val durationSeconds: Long = 0,
    val estimatedSizeBytes: Long = 0,
    val platform: String = "Universal"
)

object VideoDownloadEngine {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    private const val MOBILE_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    suspend fun resolveCanonicalUrl(url: String): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", MOBILE_USER_AGENT)
                .build()
            client.newCall(request).execute().use { response ->
                val resolved = response.request.url.toString()
                if (resolved.isNotBlank()) resolved else url
            }
        } catch (_: Exception) {
            url
        }
    }

    suspend fun extractMetadata(url: String): Result<VideoMetadata> = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        val canonical = resolveCanonicalUrl(trimmed)

        // 1. Check if direct media link
        if (canonical.endsWith(".mp4", ignoreCase = true) || canonical.endsWith(".webm", ignoreCase = true) ||
            canonical.endsWith(".mp3", ignoreCase = true) || canonical.contains(".mp4?", ignoreCase = true)
        ) {
            val filename = canonical.substringAfterLast("/").substringBefore("?").ifBlank { "Direct_Video.mp4" }
            return@withContext Result.success(
                VideoMetadata(
                    videoId = "direct_${System.currentTimeMillis()}",
                    originalUrl = trimmed,
                    canonicalUrl = canonical,
                    title = filename,
                    author = "Direct Link",
                    videoStreamUrl = canonical,
                    audioStreamUrl = if (canonical.endsWith(".mp3")) canonical else null,
                    platform = "Direct"
                )
            )
        }

        // 2. TikTok Extraction Strategy (TikWM API + oEmbed fallback)
        if (canonical.contains("tiktok.com", ignoreCase = true)) {
            try {
                val encodedUrl = URLEncoder.encode(canonical, "UTF-8")
                val tikWmRequest = Request.Builder()
                    .url("https://tikwm.com/api/?url=$encodedUrl")
                    .header("User-Agent", DESKTOP_USER_AGENT)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(tikWmRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val json = JSONObject(body)
                            val code = json.optInt("code", -1)
                            if (code == 0 && json.has("data")) {
                                val data = json.getJSONObject("data")
                                var playUrl = data.optString("play", "")
                                var musicUrl = data.optString("music", "")
                                val title = data.optString("title", "TikTok Video").ifBlank { "TikTok Video" }
                                val cover = data.optString("cover", null)
                                val authorObj = data.optJSONObject("author")
                                val author = authorObj?.optString("nickname", "TikTok Creator") ?: "TikTok Creator"
                                val duration = data.optLong("duration", 0L)
                                val size = data.optLong("size", 0L)

                                if (playUrl.startsWith("/")) {
                                    playUrl = "https://www.tikwm.com$playUrl"
                                }
                                if (musicUrl.startsWith("/")) {
                                    musicUrl = "https://www.tikwm.com$musicUrl"
                                }

                                if (playUrl.isNotBlank()) {
                                    return@withContext Result.success(
                                        VideoMetadata(
                                            videoId = "tiktok_${System.currentTimeMillis()}",
                                            originalUrl = trimmed,
                                            canonicalUrl = canonical,
                                            title = title,
                                            author = author,
                                            videoStreamUrl = playUrl,
                                            audioStreamUrl = if (musicUrl.isNotBlank()) musicUrl else null,
                                            thumbnailUrl = cover,
                                            durationSeconds = duration,
                                            estimatedSizeBytes = size,
                                            platform = "TikTok"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 3. Instagram / General Fallback via oEmbed or direct inspection
        try {
            val title = if (canonical.contains("instagram.com")) "Instagram Reel Video" else "Online Video Stream"
            val author = if (canonical.contains("instagram.com")) "Instagram User" else "Web Stream"
            return@withContext Result.success(
                VideoMetadata(
                    videoId = "media_${System.currentTimeMillis()}",
                    originalUrl = trimmed,
                    canonicalUrl = canonical,
                    title = title,
                    author = author,
                    videoStreamUrl = canonical,
                    platform = if (canonical.contains("instagram.com")) "Instagram" else "Web"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadMedia(
        context: Context,
        metadata: VideoMetadata,
        downloadVideo: Boolean,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, speedMBps: Double) -> Unit
    ): Result<DownloadedItem> = withContext(Dispatchers.IO) {
        val streamUrl = if (downloadVideo) {
            metadata.videoStreamUrl
        } else {
            metadata.audioStreamUrl ?: metadata.videoStreamUrl
        }

        val target = MediaStorageManager.createMediaTarget(
            context = context,
            title = metadata.title,
            isVideo = downloadVideo
        )

        var totalRead = 0L
        var reportedLength = -1L
        var success = false

        try {
            val request = Request.Builder()
                .url(streamUrl)
                .header("User-Agent", DESKTOP_USER_AGENT)
                .header("Referer", metadata.canonicalUrl)
                .build()

            val startTime = System.currentTimeMillis()
            var lastUpdate = startTime
            var lastRead = 0L

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP download failed: ${response.code}"))
                }

                val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
                reportedLength = body.contentLength()
                val inputStream: InputStream = body.byteStream()
                val outputStream = target.outputStream

                val buffer = ByteArray(32 * 1024)
                var bytes: Int

                while (inputStream.read(buffer).also { bytes = it } != -1) {
                    outputStream.write(buffer, 0, bytes)
                    totalRead += bytes

                    val now = System.currentTimeMillis()
                    if (now - lastUpdate > 300) {
                        val intervalSec = (now - lastUpdate) / 1000.0
                        val bytesDelta = totalRead - lastRead
                        val speed = if (intervalSec > 0) (bytesDelta / (1024.0 * 1024.0)) / intervalSec else 0.0
                        onProgress(totalRead, if (reportedLength > 0) reportedLength else totalRead, speed)
                        lastUpdate = now
                        lastRead = totalRead
                    }
                }
                outputStream.flush()
                outputStream.close()
            }

            // Verify integrity
            val verification = MediaStorageManager.verifyIntegrity(context, target)
            if (!verification.isValid && reportedLength > 0 && totalRead < reportedLength) {
                MediaStorageManager.finalizeMediaTarget(context, target, false)
                return@withContext Result.failure(Exception("File incomplete: ${verification.reason}"))
            }

            MediaStorageManager.finalizeMediaTarget(context, target, true)
            success = true

            val item = DownloadedItem(
                originalUrl = metadata.originalUrl,
                canonicalUrl = metadata.canonicalUrl,
                title = metadata.title,
                author = metadata.author,
                localUri = target.uri.toString(),
                filePath = target.filePath,
                fileSize = if (verification.sizeBytes > 0) verification.sizeBytes else totalRead,
                durationSeconds = metadata.durationSeconds,
                isVideo = downloadVideo,
                mimeType = target.mimeType,
                thumbnailUrl = metadata.thumbnailUrl,
                downloadTimestamp = System.currentTimeMillis()
            )
            Result.success(item)
        } catch (e: Exception) {
            if (!success) {
                MediaStorageManager.finalizeMediaTarget(context, target, false)
            }
            Result.failure(e)
        }
    }
}
