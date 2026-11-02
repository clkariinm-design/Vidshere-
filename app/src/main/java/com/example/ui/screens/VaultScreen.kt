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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadedItem
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.util.LocalizedStrings

@Composable
fun VaultScreen(viewModel: MainViewModel) {
    val vaultState by viewModel.vaultState.collectAsState()
    val isSomali by viewModel.isSomali.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!vaultState.isUnlocked) {
            // Locked: PIN Entry View
            LockedVaultView(
                enteredPin = vaultState.enteredPin,
                errorMessage = vaultState.errorMessage,
                isSomali = isSomali,
                onDigit = { viewModel.enterVaultPinDigit(it) },
                onBackspace = { viewModel.deleteVaultPinDigit() }
            )
        } else {
            // Unlocked: Vault Items View
            UnlockedVaultView(
                items = vaultState.vaultItems,
                isSomali = isSomali,
                onLock = { viewModel.lockVault() },
                onUnhide = { viewModel.toggleVaultProtection(it) },
                onPlay = { viewModel.openPlayer(it) }
            )
        }
    }
}

@Composable
fun LockedVaultView(
    enteredPin: String,
    errorMessage: String,
    isSomali: Boolean,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = LocalizedStrings.get("vault_title", isSomali),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White
        )
        Text(
            text = LocalizedStrings.get("vault_desc", isSomali),
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) EmeraldPrimary else SurfaceVariantDark)
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = LocalizedStrings.get("vault_pin_incorrect", isSomali),
                color = ErrorRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Default PIN: 1234",
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Numeric Keypad
        val rows = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(' ', '0', '<')
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    for (key in row) {
                        when (key) {
                            ' ' -> Spacer(modifier = Modifier.size(64.dp))
                            '<' -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceDark)
                                        .clickable { onBackspace() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = Color.White)
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceDark)
                                        .clickable { onDigit(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = key.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnlockedVaultView(
    items: List<DownloadedItem>,
    isSomali: Boolean,
    onLock: () -> Unit,
    onUnhide: (DownloadedItem) -> Unit,
    onPlay: (DownloadedItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Private Vault (Unlocked)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${items.size} hidden private files", color = EmeraldPrimary, fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = onLock,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = LocalizedStrings.get("vault_lock", isSomali), color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = LocalizedStrings.get("vault_empty", isSomali),
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VideoFile, contentDescription = null, tint = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(formatFileSize(item.fileSize), color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            IconButton(onClick = { onPlay(item) }) {
                                Icon(Icons.Default.LockOpen, contentDescription = "Play", tint = CyanAccent)
                            }
                            IconButton(onClick = { onUnhide(item) }) {
                                Icon(Icons.Default.VisibilityOff, contentDescription = "Unhide", tint = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
