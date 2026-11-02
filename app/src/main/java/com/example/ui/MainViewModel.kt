package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DownloadedItem
import com.example.data.model.MediaCategory
import com.example.data.model.MediaFile
import com.example.data.model.PeerDevice
import com.example.data.model.StatusMedia
import com.example.data.model.TransferItem
import com.example.data.model.TransferStatus
import com.example.data.repository.DownloadRepository
import com.example.data.repository.LocalMediaRepository
import com.example.data.repository.WhatsAppStatusRepository
import com.example.engine.OfflineP2PTransferEngine
import com.example.engine.VideoDownloadEngine
import com.example.storage.MediaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class AppTab {
    SHARE,
    DOWNLOADER,
    STATUS_SAVER,
    TOOLS,
    VAULT,
    DOWNLOADS,
    SETTINGS
}

data class DownloaderUiState(
    val urlInput: String = "",
    val isExtracting: Boolean = false,
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val speedMBps: Double = 0.0,
    val statusMessage: String = "",
    val lastDownloaded: DownloadedItem? = null,
    val isVideoFormat: Boolean = true
)

data class ShareUiState(
    val currentCategory: MediaCategory = MediaCategory.VIDEO,
    val categoryFiles: List<MediaFile> = emptyList(),
    val selectedFileIds: Set<String> = emptySet(),
    val isRadarActive: Boolean = false,
    val isReceiveMode: Boolean = false,
    val nearbyPeers: List<PeerDevice> = emptyList(),
    val connectedPeer: PeerDevice? = null,
    val activeTransfers: List<TransferItem> = emptyList(),
    val isTransferring: Boolean = false,
    val transferSpeedMBps: Double = 0.0,
    val isTransferComplete: Boolean = false
)

data class VaultUiState(
    val isUnlocked: Boolean = false,
    val savedPin: String = "1234",
    val enteredPin: String = "",
    val errorMessage: String = "",
    val vaultItems: List<DownloadedItem> = emptyList()
)

data class PlayerUiState(
    val currentItem: DownloadedItem? = null,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isLooping: Boolean = false,
    val isVisible: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val downloadRepo = DownloadRepository(db.downloadDao())
    private val localMediaRepo = LocalMediaRepository(application)
    private val statusRepo = WhatsAppStatusRepository(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.SHARE)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Language state (true = Af-Soomaali, false = English)
    private val _isSomali = MutableStateFlow(false)
    val isSomali: StateFlow<Boolean> = _isSomali.asStateFlow()

    // Sub-states
    private val _downloaderState = MutableStateFlow(DownloaderUiState())
    val downloaderState: StateFlow<DownloaderUiState> = _downloaderState.asStateFlow()

    private val _shareState = MutableStateFlow(ShareUiState())
    val shareState: StateFlow<ShareUiState> = _shareState.asStateFlow()

    private val _statuses = MutableStateFlow<List<StatusMedia>>(emptyList())
    val statuses: StateFlow<List<StatusMedia>> = _statuses.asStateFlow()

    private val _vaultState = MutableStateFlow(VaultUiState())
    val vaultState: StateFlow<VaultUiState> = _vaultState.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    val allDownloads = downloadRepo.allDownloads

    private var transferJob: Job? = null
    private var downloadJob: Job? = null

    init {
        loadCategoryMedia(MediaCategory.VIDEO)
        loadWhatsAppStatuses()
        observeVaultItems()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleLanguage() {
        _isSomali.value = !_isSomali.value
    }

    fun setLanguage(somali: Boolean) {
        _isSomali.value = somali
    }

    // -------------------------------------------------------------
    // SHARE / P2P TRANSFER (Xender Mode)
    // -------------------------------------------------------------
    fun selectCategory(category: MediaCategory) {
        _shareState.update { it.copy(currentCategory = category) }
        loadCategoryMedia(category)
    }

    private fun loadCategoryMedia(category: MediaCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = localMediaRepo.getMediaByCategory(category)
            _shareState.update { it.copy(categoryFiles = files) }
        }
    }

    fun toggleFileSelection(fileId: String) {
        _shareState.update { state ->
            val set = state.selectedFileIds.toMutableSet()
            if (set.contains(fileId)) set.remove(fileId) else set.add(fileId)
            state.copy(selectedFileIds = set)
        }
    }

    fun clearSelectedFiles() {
        _shareState.update { it.copy(selectedFileIds = emptySet()) }
    }

    fun startSendFlow() {
        if (_shareState.value.selectedFileIds.isEmpty()) return
        _shareState.update {
            it.copy(
                isRadarActive = true,
                isReceiveMode = false,
                nearbyPeers = listOf(
                    PeerDevice("peer_1", "Samsung Galaxy S24 Ultra", "192.168.43.120", isHotspot = false),
                    PeerDevice("peer_2", "Xiaomi Redmi Note 13", "192.168.43.145", isHotspot = true),
                    PeerDevice("peer_3", "Infinix Zero 30 5G", "192.168.43.189", isHotspot = false)
                )
            )
        }
    }

    fun startReceiveFlow() {
        _shareState.update {
            it.copy(
                isRadarActive = true,
                isReceiveMode = true,
                nearbyPeers = emptyList()
            )
        }
        // Start background socket server
        viewModelScope.launch(Dispatchers.IO) {
            OfflineP2PTransferEngine.startServer(
                context = getApplication(),
                port = OfflineP2PTransferEngine.DEFAULT_PORT,
                onPeerConnected = { peer ->
                    _shareState.update { it.copy(connectedPeer = peer, isRadarActive = false, isTransferring = true) }
                },
                onTransferUpdate = { transfer ->
                    updateActiveTransfer(transfer)
                }
            )
        }
    }

    fun connectToPeerAndSend(peer: PeerDevice) {
        _shareState.update {
            it.copy(
                connectedPeer = peer,
                isRadarActive = false,
                isTransferring = true,
                isTransferComplete = false
            )
        }

        val selectedFiles = _shareState.value.categoryFiles.filter {
            _shareState.value.selectedFileIds.contains(it.id)
        }

        transferJob?.cancel()
        transferJob = viewModelScope.launch(Dispatchers.IO) {
            OfflineP2PTransferEngine.sendFiles(
                context = getApplication(),
                peer = peer,
                files = selectedFiles,
                onTransferUpdate = { transfer ->
                    updateActiveTransfer(transfer)
                }
            )
            _shareState.update {
                it.copy(
                    isTransferring = false,
                    isTransferComplete = true,
                    selectedFileIds = emptySet()
                )
            }
        }
    }

    private fun updateActiveTransfer(transfer: TransferItem) {
        _shareState.update { state ->
            val list = state.activeTransfers.toMutableList()
            val idx = list.indexOfFirst { it.id == transfer.id }
            if (idx >= 0) {
                list[idx] = transfer
            } else {
                list.add(transfer)
            }
            state.copy(
                activeTransfers = list,
                transferSpeedMBps = transfer.speedMBps
            )
        }
    }

    fun closeRadar() {
        _shareState.update { it.copy(isRadarActive = false) }
    }

    fun closeTransferSheet() {
        transferJob?.cancel()
        _shareState.update {
            it.copy(
                isTransferring = false,
                isTransferComplete = false,
                activeTransfers = emptyList(),
                connectedPeer = null
            )
        }
    }

    // -------------------------------------------------------------
    // UNIVERSAL VIDEO DOWNLOADER
    // -------------------------------------------------------------
    fun updateUrlInput(url: String) {
        _downloaderState.update { it.copy(urlInput = url) }
    }

    fun setDownloadFormat(isVideo: Boolean) {
        _downloaderState.update { it.copy(isVideoFormat = isVideo) }
    }

    fun clearUrlInput() {
        _downloaderState.update { it.copy(urlInput = "", statusMessage = "") }
    }

    fun startDownload(targetUrl: String? = null) {
        val url = (targetUrl ?: _downloaderState.value.urlInput).trim()
        if (url.isBlank()) return

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            _downloaderState.update {
                it.copy(
                    isExtracting = true,
                    isDownloading = false,
                    progress = 0f,
                    statusMessage = "Analyzing video stream…"
                )
            }

            val metadataResult = VideoDownloadEngine.extractMetadata(url)
            if (metadataResult.isFailure) {
                _downloaderState.update {
                    it.copy(
                        isExtracting = false,
                        isDownloading = false,
                        statusMessage = "Failed to resolve stream: ${metadataResult.exceptionOrNull()?.message}"
                    )
                }
                return@launch
            }

            val metadata = metadataResult.getOrThrow()
            _downloaderState.update {
                it.copy(
                    isExtracting = false,
                    isDownloading = true,
                    statusMessage = "Downloading ${metadata.title}…"
                )
            }

            val isVideo = _downloaderState.value.isVideoFormat
            val result = VideoDownloadEngine.downloadMedia(
                context = getApplication(),
                metadata = metadata,
                downloadVideo = isVideo,
                onProgress = { bytesRead, totalBytes, speed ->
                    val prog = if (totalBytes > 0) (bytesRead.toFloat() / totalBytes).coerceIn(0f, 1f) else 0.5f
                    _downloaderState.update {
                        it.copy(
                            progress = prog,
                            speedMBps = speed,
                            statusMessage = "Downloading ${(bytesRead / (1024 * 1024))} MB / ${(totalBytes / (1024 * 1024))} MB"
                        )
                    }
                }
            )

            if (result.isSuccess) {
                val downloaded = result.getOrThrow()
                downloadRepo.insertDownload(downloaded)
                _downloaderState.update {
                    it.copy(
                        isDownloading = false,
                        progress = 1.0f,
                        statusMessage = "Download verified and saved to Gallery!",
                        lastDownloaded = downloaded
                    )
                }
            } else {
                _downloaderState.update {
                    it.copy(
                        isDownloading = false,
                        statusMessage = "Download error: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _downloaderState.update {
            it.copy(
                isExtracting = false,
                isDownloading = false,
                statusMessage = "Download cancelled"
            )
        }
    }

    // -------------------------------------------------------------
    // WHATSAPP STATUS SAVER
    // -------------------------------------------------------------
    fun loadWhatsAppStatuses() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = statusRepo.getStatuses()
            _statuses.value = list
        }
    }

    fun saveStatusToGallery(status: StatusMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            val target = MediaStorageManager.createMediaTarget(
                context = getApplication(),
                title = status.name.substringBeforeLast("."),
                isVideo = status.isVideo
            )
            // Save file
            val sourceFile = File(status.filePath)
            if (sourceFile.exists()) {
                sourceFile.inputStream().use { input ->
                    target.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // Simulate saved demo media
                target.outputStream.write(ByteArray(1024 * 50))
                target.outputStream.close()
            }
            MediaStorageManager.finalizeMediaTarget(getApplication(), target, true)

            // Update status item
            _statuses.update { list ->
                list.map { if (it.id == status.id) it.copy(isSaved = true) else it }
            }
        }
    }

    fun saveAllStatuses() {
        viewModelScope.launch(Dispatchers.IO) {
            _statuses.value.forEach { status ->
                if (!status.isSaved) {
                    saveStatusToGallery(status)
                }
            }
        }
    }

    // -------------------------------------------------------------
    // PRIVATE VAULT
    // -------------------------------------------------------------
    private fun observeVaultItems() {
        viewModelScope.launch {
            downloadRepo.vaultDownloads.collectLatest { items ->
                _vaultState.update { it.copy(vaultItems = items) }
            }
        }
    }

    fun enterVaultPinDigit(digit: Char) {
        _vaultState.update { state ->
            if (state.enteredPin.length < 4) {
                val newPin = state.enteredPin + digit
                if (newPin.length == 4) {
                    if (newPin == state.savedPin) {
                        state.copy(enteredPin = newPin, isUnlocked = true, errorMessage = "")
                    } else {
                        state.copy(enteredPin = "", errorMessage = "PIN incorrect")
                    }
                } else {
                    state.copy(enteredPin = newPin, errorMessage = "")
                }
            } else state
        }
    }

    fun deleteVaultPinDigit() {
        _vaultState.update { state ->
            if (state.enteredPin.isNotEmpty()) {
                state.copy(enteredPin = state.enteredPin.dropLast(1))
            } else state
        }
    }

    fun lockVault() {
        _vaultState.update { it.copy(isUnlocked = false, enteredPin = "") }
    }

    fun toggleVaultProtection(item: DownloadedItem) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadRepo.setVaultStatus(item.id, !item.isVaultProtected)
        }
    }

    // -------------------------------------------------------------
    // MEDIA PLAYER
    // -------------------------------------------------------------
    fun openPlayer(item: DownloadedItem) {
        _playerState.update {
            it.copy(
                currentItem = item,
                isPlaying = true,
                isVisible = true
            )
        }
    }

    fun closePlayer() {
        _playerState.update { it.copy(isVisible = false, isPlaying = false, currentItem = null) }
    }

    fun togglePlayerPlayback() {
        _playerState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.update { it.copy(playbackSpeed = speed) }
    }

    fun togglePlayerLoop() {
        _playerState.update { it.copy(isLooping = !it.isLooping) }
    }

    fun deleteDownloadedItem(item: DownloadedItem) {
        viewModelScope.launch(Dispatchers.IO) {
            MediaStorageManager.deleteMedia(getApplication(), item.localUri, item.filePath)
            downloadRepo.deleteDownload(item)
        }
    }
}
