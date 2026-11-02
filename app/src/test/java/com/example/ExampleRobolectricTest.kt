package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.DownloadedItem
import com.example.ui.screens.formatFileSize
import com.example.ui.util.LocalizedStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("VidShare Pro", appName)
    }

    @Test
    fun testLocalizedStrings() {
        val engShare = LocalizedStrings.get("btn_send", false)
        val somShare = LocalizedStrings.get("btn_send", true)
        assertEquals("Send", engShare)
        assertEquals("Dir", somShare)

        val engTitle = LocalizedStrings.get("app_name", false)
        val somTitle = LocalizedStrings.get("app_name", true)
        assertEquals("VidShare Pro", engTitle)
        assertEquals("VidShare Pro", somTitle)
    }

    @Test
    fun testFileSizeFormatting() {
        assertEquals("500 B", formatFileSize(500L))
        assertEquals("1.0 KB", formatFileSize(1024L))
        assertEquals("10.0 MB", formatFileSize(10 * 1024 * 1024L))
        assertEquals("1.50 GB", formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testRoomDatabaseOperations() = runBlocking {
        val dao = database.downloadDao()
        val item = DownloadedItem(
            originalUrl = "https://www.tiktok.com/@creator/video/123",
            canonicalUrl = "https://www.tiktok.com/@creator/video/123",
            title = "Test Video",
            author = "TikTok Creator",
            localUri = "content://media/external/video/media/1",
            filePath = "/storage/emulated/0/Movies/VidSharePro/video.mp4",
            fileSize = 10_500_000L,
            isVideo = true
        )

        val id = dao.insertDownload(item)
        assertTrue(id > 0)

        val retrieved = dao.getDownloadById(id)
        assertNotNull(retrieved)
        assertEquals("Test Video", retrieved?.title)
        assertEquals(10_500_000L, retrieved?.fileSize)

        val list = dao.getAllDownloads().first()
        assertEquals(1, list.size)
    }
}
