package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MediaCategory
import com.example.data.model.MediaFile
import com.example.data.model.PeerDevice
import com.example.data.model.TransferStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.util.LocalizedStrings

@Composable
fun ShareScreen(viewModel: MainViewModel) {
    val shareState by viewModel.shareState.collectAsState()
    val isSomali by viewModel.isSomali.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (shareState.selectedFileIds.isNotEmpty()) 80.dp else 16.dp)
        ) {
            // Header / Hero: Send & Receive Big Action Cards
            HeroActionCards(
                isSomali = isSomali,
                onSendClick = { viewModel.startSendFlow() },
                onReceiveClick = { viewModel.startReceiveFlow() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Tabs
            CategoryTabBar(
                currentCategory = shareState.currentCategory,
                isSomali = isSomali,
                onSelectCategory = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // File List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shareState.categoryFiles, key = { it.id }) { file ->
                    val isSelected = shareState.selectedFileIds.contains(file.id)
                    MediaFileRow(
                        file = file,
                        isSelected = isSelected,
                        onToggle = { viewModel.toggleFileSelection(file.id) }
                    )
                }
            }
        }

        // Sticky Bottom Send Bar if files are selected
        if (shareState.selectedFileIds.isNotEmpty()) {
            val totalBytes = shareState.categoryFiles
                .filter { shareState.selectedFileIds.contains(it.id) }
                .sumOf { it.sizeBytes }
            val formattedSize = formatFileSize(totalBytes)

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = LocalizedStrings.get("selected_files_count", isSomali, shareState.selectedFileIds.size, formattedSize),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Offline Direct Wi-Fi",
                            color = EmeraldPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.startSendFlow() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("start_send_button")
                    ) {
                        Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LocalizedStrings.get("btn_send", isSomali),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Radar Scanning Dialog (when searching or waiting)
        if (shareState.isRadarActive) {
            RadarDialog(
                isReceiveMode = shareState.isReceiveMode,
                peers = shareState.nearbyPeers,
                isSomali = isSomali,
                onDismiss = { viewModel.closeRadar() },
                onSelectPeer = { viewModel.connectToPeerAndSend(it) }
            )
        }

        // Active Transfer Progress Dialog
        if (shareState.isTransferring || shareState.isTransferComplete) {
            TransferProgressDialog(
                shareState = shareState,
                isSomali = isSomali,
                onClose = { viewModel.closeTransferSheet() }
            )
        }
    }
}

@Composable
fun HeroActionCards(
    isSomali: Boolean,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = LocalizedStrings.get("share_title", isSomali),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = LocalizedStrings.get("share_subtitle", isSomali),
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Send Button Card
                Button(
                    onClick = onSendClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("hero_send_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LocalizedStrings.get("btn_send", isSomali),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Receive Button Card
                Button(
                    onClick = onReceiveClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("hero_receive_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LocalizedStrings.get("btn_receive", isSomali),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTabBar(
    currentCategory: MediaCategory,
    isSomali: Boolean,
    onSelectCategory: (MediaCategory) -> Unit
) {
    val categories = listOf(
        MediaCategory.VIDEO to LocalizedStrings.get("category_videos", isSomali),
        MediaCategory.AUDIO to LocalizedStrings.get("category_audios", isSomali),
        MediaCategory.PHOTO to LocalizedStrings.get("category_photos", isSomali),
        MediaCategory.APP to LocalizedStrings.get("category_apps", isSomali),
        MediaCategory.DOCUMENT to LocalizedStrings.get("category_docs", isSomali)
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (category, label) ->
            val isSelected = currentCategory == category
            val icon = when (category) {
                MediaCategory.VIDEO -> Icons.Default.VideoFile
                MediaCategory.AUDIO -> Icons.Default.AudioFile
                MediaCategory.PHOTO -> Icons.Default.Image
                MediaCategory.APP -> Icons.Default.Android
                MediaCategory.DOCUMENT -> Icons.Default.Description
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) EmeraldPrimary else SurfaceDark,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectCategory(category) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MediaFileRow(
    file: MediaFile,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceVariantDark else SurfaceDark
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldPrimary, CyanAccent))) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon thumbnail
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (file.category) {
                            MediaCategory.VIDEO -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            MediaCategory.AUDIO -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            MediaCategory.PHOTO -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                            MediaCategory.APP -> Color(0xFF10B981).copy(alpha = 0.2f)
                            MediaCategory.DOCUMENT -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (file.category) {
                        MediaCategory.VIDEO -> Icons.Default.VideoFile
                        MediaCategory.AUDIO -> Icons.Default.AudioFile
                        MediaCategory.PHOTO -> Icons.Default.Image
                        MediaCategory.APP -> Icons.Default.Android
                        MediaCategory.DOCUMENT -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = when (file.category) {
                        MediaCategory.VIDEO -> Color(0xFFEF4444)
                        MediaCategory.AUDIO -> Color(0xFF8B5CF6)
                        MediaCategory.PHOTO -> Color(0xFF3B82F6)
                        MediaCategory.APP -> Color(0xFF10B981)
                        MediaCategory.DOCUMENT -> Color(0xFFF59E0B)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatFileSize(file.sizeBytes)} • ${file.mimeType.substringAfterLast("/")}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Selection Checkbox
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) EmeraldPrimary else Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun RadarDialog(
    isReceiveMode: Boolean,
    peers: List<PeerDevice>,
    isSomali: Boolean,
    onDismiss: () -> Unit,
    onSelectPeer: (PeerDevice) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isReceiveMode) LocalizedStrings.get("radar_waiting", isSomali) else LocalizedStrings.get("radar_searching", isSomali),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Radar Canvas animation
                RadarAnimation()

                Spacer(modifier = Modifier.height(16.dp))

                if (isReceiveMode) {
                    // QR Code mode indicator
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = LocalizedStrings.get("qr_code_desc", isSomali),
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // List of discovered devices
                    Text(
                        text = "Discovered Nearby Devices:",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    peers.forEach { peer ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariantDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectPeer(peer) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = peer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${peer.ipAddress} • High-Speed Wi-Fi",
                                        fontSize = 11.sp,
                                        color = EmeraldPrimary
                                    )
                                }
                                Icon(
                                    Icons.Default.NearMe,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            // Concentric rings
            drawCircle(color = Color(0xFF1E293B), radius = radius * 0.33f, center = center, style = Stroke(width = 2f))
            drawCircle(color = Color(0xFF1E293B), radius = radius * 0.66f, center = center, style = Stroke(width = 2f))
            drawCircle(color = EmeraldPrimary.copy(alpha = 0.5f), radius = radius - 4f, center = center, style = Stroke(width = 3f))

            // Sweeping radar beam
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, EmeraldPrimary.copy(alpha = 0.4f)),
                    center = center
                ),
                startAngle = sweepAngle - 45f,
                sweepAngle = 45f,
                useCenter = true
            )
        }

        // Center Device Dot
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun TransferProgressDialog(
    shareState: com.example.ui.ShareUiState,
    isSomali: Boolean,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (shareState.isTransferComplete) LocalizedStrings.get("transfer_complete", isSomali) else "High-Speed File Transfer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = LocalizedStrings.get("connected_peer", isSomali, shareState.connectedPeer?.name ?: "Nearby Phone"),
                            fontSize = 12.sp,
                            color = EmeraldPrimary
                        )
                    }
                    if (shareState.isTransferComplete) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Speed Gauge
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Transfer Speed", color = Color.White, fontSize = 13.sp)
                        }
                        Text(
                            text = LocalizedStrings.get("transfer_speed", isSomali, shareState.transferSpeedMBps),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = EmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Transfers list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shareState.activeTransfers) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariantDark
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.fileName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${(item.progress * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.status == TransferStatus.COMPLETED) EmeraldPrimary else CyanAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { item.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (item.status == TransferStatus.COMPLETED) EmeraldPrimary else CyanAccent,
                                    trackColor = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${formatFileSize(item.transferredBytes)} / ${formatFileSize(item.fileSizeBytes)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = if (item.status == TransferStatus.COMPLETED) "Completed" else "Direct Transfer",
                                        fontSize = 11.sp,
                                        color = if (item.status == TransferStatus.COMPLETED) EmeraldPrimary else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (shareState.isTransferComplete) EmeraldPrimary else Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = if (shareState.isTransferComplete) "Done" else "Cancel Transfer",
                        color = if (shareState.isTransferComplete) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
