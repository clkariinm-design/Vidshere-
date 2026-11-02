package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.util.LocalizedStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MediaToolsScreen(viewModel: MainViewModel) {
    val isSomali by viewModel.isSomali.collectAsState()
    val scope = rememberCoroutineScope()

    var activeTool by remember { mutableStateOf<String?>("mp3") }
    var isProcessing by remember { mutableStateOf(false) }
    var processProgress by remember { mutableFloatStateOf(0f) }
    var processCompleteMessage by remember { mutableStateOf<String?>(null) }

    var compressionRatio by remember { mutableFloatStateOf(0.4f) }
    var trimStart by remember { mutableFloatStateOf(5f) }
    var trimEnd by remember { mutableFloatStateOf(25f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LocalizedStrings.get("tools_title", isSomali),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Professional offline video to MP3 converter, compressor & trimmer",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Tool Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolTabButton(
                title = "Video to MP3",
                icon = Icons.Default.Audiotrack,
                isSelected = activeTool == "mp3",
                modifier = Modifier.weight(1f)
            ) { activeTool = "mp3"; processCompleteMessage = null }

            ToolTabButton(
                title = "Compressor",
                icon = Icons.Default.Compress,
                isSelected = activeTool == "compress",
                modifier = Modifier.weight(1f)
            ) { activeTool = "compress"; processCompleteMessage = null }

            ToolTabButton(
                title = "Trimmer",
                icon = Icons.Default.ContentCut,
                isSelected = activeTool == "trim",
                modifier = Modifier.weight(1f)
            ) { activeTool = "trim"; processCompleteMessage = null }
        }

        // Active Tool Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (activeTool) {
                    "mp3" -> {
                        Text(
                            text = LocalizedStrings.get("tool_video_to_mp3", isSomali),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = LocalizedStrings.get("tool_video_to_mp3_desc", isSomali),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariantDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Selected: Trending_Challenge_Clip.mp4 (24.5 MB)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Target Output: High-Quality MP3 (320 kbps)", color = CyanAccent, fontSize = 11.sp)
                            }
                        }
                    }
                    "compress" -> {
                        Text(
                            text = LocalizedStrings.get("tool_video_compressor", isSomali),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = LocalizedStrings.get("tool_video_compressor_desc", isSomali),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Compression Target: ${(compressionRatio * 100).toInt()}% of original size",
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Slider(
                            value = compressionRatio,
                            onValueChange = { compressionRatio = it },
                            valueRange = 0.2f..0.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldPrimary,
                                activeTrackColor = EmeraldPrimary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Original: 68.0 MB", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("Compressed: ~${String.format("%.1f", 68.0 * compressionRatio)} MB (Saves ~${String.format("%.1f", 68.0 * (1 - compressionRatio))} MB)", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "trim" -> {
                        Text(
                            text = LocalizedStrings.get("tool_video_trimmer", isSomali),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = LocalizedStrings.get("tool_video_trimmer_desc", isSomali),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Start Time: ${trimStart.toInt()}s  •  End Time: ${trimEnd.toInt()}s (Clip: ${(trimEnd - trimStart).toInt()}s)",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Slider(
                            value = trimStart,
                            onValueChange = { if (it < trimEnd) trimStart = it },
                            valueRange = 0f..60f,
                            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                        )
                        Slider(
                            value = trimEnd,
                            onValueChange = { if (it > trimStart) trimEnd = it },
                            valueRange = 0f..60f,
                            colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Indicator
                if (isProcessing) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { processProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = EmeraldPrimary,
                            trackColor = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Processing on-device… ${(processProgress * 100).toInt()}%",
                            color = EmeraldPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Completion message
                processCompleteMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, color = EmeraldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Execute Button
                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            processCompleteMessage = null
                            for (i in 1..10) {
                                delay(150)
                                processProgress = i / 10f
                            }
                            isProcessing = false
                            processCompleteMessage = when (activeTool) {
                                "mp3" -> "MP3 extracted and saved to Music/VidSharePro!"
                                "compress" -> "Video compressed successfully to ${String.format("%.1f", 68.0 * compressionRatio)} MB!"
                                else -> "Clip trimmed and saved successfully!"
                            }
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = LocalizedStrings.get("tool_execute", isSomali),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ToolTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) EmeraldPrimary else SurfaceDark,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}
