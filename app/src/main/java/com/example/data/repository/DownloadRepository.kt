package com.example.data.repository

import com.example.data.database.DownloadDao
import com.example.data.model.DownloadedItem
import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val downloadDao: DownloadDao) {
    val allDownloads: Flow<List<DownloadedItem>> = downloadDao.getAllDownloads()
    val vaultDownloads: Flow<List<DownloadedItem>> = downloadDao.getVaultDownloads()

    fun getDownloadsByType(isVideo: Boolean): Flow<List<DownloadedItem>> {
        return downloadDao.getDownloadsByType(isVideo)
    }

    suspend fun getDownloadById(id: Long): DownloadedItem? {
        return downloadDao.getDownloadById(id)
    }

    suspend fun insertDownload(item: DownloadedItem): Long {
        return downloadDao.insertDownload(item)
    }

    suspend fun deleteDownload(item: DownloadedItem) {
        downloadDao.deleteDownload(item)
    }

    suspend fun deleteById(id: Long) {
        downloadDao.deleteById(id)
    }

    suspend fun setVaultStatus(id: Long, isVault: Boolean) {
        downloadDao.updateVaultStatus(id, isVault)
    }
}
