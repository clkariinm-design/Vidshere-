package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.MediaToolsScreen
import com.example.ui.screens.PlayerDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShareScreen
import com.example.ui.screens.StatusSaverScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.VidShareTheme
import com.example.ui.util.LocalizedStrings

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming shared links or files
        handleIncomingIntent(intent)

        setContent {
            VidShareTheme {
                MainContent(
                    viewModel = viewModel,
                    onOpenSettings = { viewModel.setTab(AppTab.SETTINGS) },
                    onOpenVault = { viewModel.setTab(AppTab.VAULT) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                viewModel.setTab(AppTab.DOWNLOADER)
                viewModel.updateUrlInput(text.trim())
            } else {
                viewModel.setTab(AppTab.SHARE)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
    onOpenVault: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isSomali by viewModel.isSomali.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val shareState by viewModel.shareState.collectAsState()

    // Request permissions launcher for Scoped Storage & Nearby Wi-Fi
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Graceful handling */ }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        try {
            permissionLauncher.launch(permissions.toTypedArray())
        } catch (_: Exception) {}
    }

    // BackHandler: Navigate back to Share tab if on another screen
    BackHandler(enabled = currentTab != AppTab.SHARE) {
        viewModel.setTab(AppTab.SHARE)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VidShare Pro",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = LocalizedStrings.get("app_tagline", isSomali),
                                fontSize = 10.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Quick Language Switcher Pill (SO / EN)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleLanguage() }
                            .padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Language",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSomali) "SO" else "EN",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Vault Button
                    IconButton(
                        onClick = onOpenVault,
                        modifier = Modifier.testTag("top_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Vault",
                            tint = if (currentTab == AppTab.VAULT) EmeraldPrimary else Color.White
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentTab == AppTab.SETTINGS) EmeraldPrimary else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                // 1. Share (Xender Mode)
                NavigationBarItem(
                    selected = currentTab == AppTab.SHARE,
                    onClick = { viewModel.setTab(AppTab.SHARE) },
                    icon = {
                        if (shareState.selectedFileIds.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = EmeraldPrimary) {
                                        Text("${shareState.selectedFileIds.size}", color = Color.Black)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.NearMe, contentDescription = "Share")
                            }
                        } else {
                            Icon(Icons.Default.NearMe, contentDescription = "Share")
                        }
                    },
                    label = { Text(LocalizedStrings.get("tab_share", isSomali), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                // 2. Downloader
                NavigationBarItem(
                    selected = currentTab == AppTab.DOWNLOADER,
                    onClick = { viewModel.setTab(AppTab.DOWNLOADER) },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Downloader") },
                    label = { Text(LocalizedStrings.get("tab_downloader", isSomali), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                // 3. Status Saver
                NavigationBarItem(
                    selected = currentTab == AppTab.STATUS_SAVER,
                    onClick = { viewModel.setTab(AppTab.STATUS_SAVER) },
                    icon = { Icon(Icons.Default.SlowMotionVideo, contentDescription = "Status") },
                    label = { Text(LocalizedStrings.get("tab_status", isSomali), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                // 4. Media Tools
                NavigationBarItem(
                    selected = currentTab == AppTab.TOOLS,
                    onClick = { viewModel.setTab(AppTab.TOOLS) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
                    label = { Text(LocalizedStrings.get("tab_tools", isSomali), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                // 5. Downloads / Gallery
                NavigationBarItem(
                    selected = currentTab == AppTab.DOWNLOADS,
                    onClick = { viewModel.setTab(AppTab.DOWNLOADS) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Downloads") },
                    label = { Text(LocalizedStrings.get("tab_downloads", isSomali), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.SHARE -> ShareScreen(viewModel)
                AppTab.DOWNLOADER -> DownloaderScreen(viewModel)
                AppTab.STATUS_SAVER -> StatusSaverScreen(viewModel)
                AppTab.TOOLS -> MediaToolsScreen(viewModel)
                AppTab.VAULT -> VaultScreen(viewModel)
                AppTab.DOWNLOADS -> DownloadsScreen(viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel)
            }

            // Player dialog
            if (playerState.isVisible) {
                PlayerDialog(viewModel)
            }
        }
    }
}
