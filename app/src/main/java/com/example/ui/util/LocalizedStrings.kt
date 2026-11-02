package com.example.ui.util

object LocalizedStrings {

    fun get(key: String, isSomali: Boolean, vararg args: Any): String {
        val str = if (isSomali) somaliMap[key] ?: englishMap[key] ?: key else englishMap[key] ?: key
        return try {
            if (args.isNotEmpty()) String.format(str, *args) else str
        } catch (_: Exception) {
            str
        }
    }

    private val englishMap = mapOf(
        "app_name" to "VidShare Pro",
        "app_tagline" to "Offline File Sharing & Video Downloader",
        "tab_share" to "Share",
        "tab_downloader" to "Downloader",
        "tab_status" to "Status",
        "tab_tools" to "Tools",
        "tab_vault" to "Vault",
        "tab_downloads" to "Downloads",
        "tab_settings" to "Settings",

        // Share
        "share_title" to "Offline High-Speed Transfer",
        "share_subtitle" to "Transfer files at 40MB/s without internet",
        "btn_send" to "Send",
        "btn_receive" to "Receive",
        "category_videos" to "Videos",
        "category_audios" to "Audio",
        "category_photos" to "Photos",
        "category_apps" to "Apps",
        "category_docs" to "Files",
        "selected_files_count" to "%d files selected (%s)",
        "btn_start_transfer" to "Send Selected (%d)",
        "radar_searching" to "Scanning for nearby devices…",
        "radar_waiting" to "Ready to receive files…",
        "connected_peer" to "Connected to: %s",
        "transferring_file" to "Transferring: %s",
        "transfer_speed" to "%.1f MB/s",
        "transfer_complete" to "Transfer complete!",
        "hotspot_mode" to "Hotspot Mode",
        "wifi_direct_mode" to "Wi-Fi Direct",
        "qr_code_desc" to "Scan QR to connect instantly",

        // Downloader
        "downloader_title" to "Universal Video Downloader",
        "input_hint" to "Paste TikTok, Instagram, or video link…",
        "paste_button" to "Paste",
        "clear_button" to "Clear",
        "download_button" to "Download Video",
        "download_audio_button" to "Download Audio (MP3)",
        "sample_link_prompt" to "Test with sample link",
        "paste_from_clipboard" to "Pasted from clipboard",
        "clipboard_empty" to "Clipboard does not contain a valid URL",
        "status_idle" to "Ready to download",
        "status_extracting" to "Analyzing video stream…",
        "status_downloading" to "Downloading video…",
        "status_verifying" to "Verifying file integrity…",
        "status_success" to "Download completed successfully!",
        "status_error" to "Download failed",
        "cancel_button" to "Cancel",

        // Status Saver
        "status_saver_title" to "WhatsApp Status Saver",
        "status_saver_desc" to "Save status videos and photos before they vanish",
        "status_refresh" to "Refresh",
        "status_save_all" to "Save All",
        "status_saved_to_gallery" to "Saved to Gallery!",
        "no_statuses_found" to "No recent statuses found. View statuses on WhatsApp first.",

        // Tools
        "tools_title" to "Media Studio Tools",
        "tool_video_to_mp3" to "Video to MP3 Converter",
        "tool_video_to_mp3_desc" to "Extract crisp audio and custom ringtone",
        "tool_video_compressor" to "Video Compressor",
        "tool_video_compressor_desc" to "Compress video size for easy WhatsApp sharing",
        "tool_video_trimmer" to "Video Trimmer",
        "tool_video_trimmer_desc" to "Cut unwanted parts from any video",
        "tool_execute" to "Process Media",
        "tool_success" to "Processing complete!",

        // Vault
        "vault_title" to "Private Media Vault",
        "vault_desc" to "Protect personal files with 4-digit PIN",
        "vault_enter_pin" to "Enter 4-digit PIN",
        "vault_setup_pin" to "Set up new PIN",
        "vault_unlock" to "Unlock Vault",
        "vault_lock" to "Lock Vault",
        "vault_empty" to "Private vault is empty. Tap Hide Files to protect media.",
        "vault_add_file" to "Hide File",
        "vault_pin_incorrect" to "Incorrect PIN. Try again.",

        // Downloads & Player
        "downloads_title" to "Downloaded Media",
        "downloads_empty" to "No downloaded files yet",
        "play_action" to "Play",
        "share_action" to "Share",
        "delete_action" to "Delete",
        "player_title" to "Media Player",
        "player_speed" to "Speed",
        "player_loop" to "Loop",

        // Settings
        "settings_title" to "Settings",
        "language_title" to "Language / Luuqadda",
        "lang_english" to "English",
        "lang_somali" to "Af-Soomaali",
        "theme_title" to "Theme Mode",
        "about_title" to "About VidShare Pro",
        "about_desc" to "Offline P2P File Sharing (Xender Mode), Universal Video Downloader, Status Saver & Media Studio."
    )

    private val somaliMap = mapOf(
        "app_name" to "VidShare Pro",
        "app_tagline" to "Wadaaga Faylasha Bilaa Internetka & Soo Dejiyaha Muuqaallada",
        "tab_share" to "Wadaag",
        "tab_downloader" to "Soo Dejiye",
        "tab_status" to "Xaaladaha",
        "tab_tools" to "Aalado",
        "tab_vault" to "Qasnad",
        "tab_downloads" to "Kaydka",
        "tab_settings" to "Hagaajin",

        // Share
        "share_title" to "Wadaaga Xawaaraha Sare ee Bilaa Netka",
        "share_subtitle" to "Ku wadaag faylasha 40MB/s adoon internet isticmaalin",
        "btn_send" to "Dir",
        "btn_receive" to "Qaado",
        "category_videos" to "Fiidiyowyo",
        "category_audios" to "Cod/Heeso",
        "category_photos" to "Sawirro",
        "category_apps" to "Apps",
        "category_docs" to "Faylal",
        "selected_files_count" to "%d fayl ayaa la doortay (%s)",
        "btn_start_transfer" to "Dir Waxa La Doortay (%d)",
        "radar_searching" to "Waxa la baadi-goobayaa taleefannada u dhow…",
        "radar_waiting" to "Diyaar u ah helitaanka faylasha…",
        "connected_peer" to "Waxaad ku xiran tahay: %s",
        "transferring_file" to "Waa la dirayaa: %s",
        "transfer_speed" to "%.1f MB/s",
        "transfer_complete" to "Wadaagii wuu dhamaaday!",
        "hotspot_mode" to "Habka Hotspot-ka",
        "wifi_direct_mode" to "Wi-Fi Direct",
        "qr_code_desc" to "Sawir QR Code si aad isla markiiba ugu xiranto",

        // Downloader
        "downloader_title" to "Soo Dejiyaha Guud ee Muuqaallada",
        "input_hint" to "Halkan ku dheji link-ga TikTok, Instagram, ama muuqaal kasta…",
        "paste_button" to "Dheji",
        "clear_button" to "Tirtir",
        "download_button" to "Soo Deji Fiidiyow",
        "download_audio_button" to "Soo Deji Codka (MP3)",
        "sample_link_prompt" to "Tijaabi link tusaale ah",
        "paste_from_clipboard" to "Waxaa laga soo dhejiyay clipboard-ka",
        "clipboard_empty" to "Clipboard-ku kuma jiro link sax ah",
        "status_idle" to "Diyaar u ah soo dejin",
        "status_extracting" to "Waa la falanqeynayaa muuqaalka…",
        "status_downloading" to "Waa la soo dejinayaa…",
        "status_verifying" to "Waa la hubinayaa faylka…",
        "status_success" to "Si guul leh ayaa loo soo dejiyay!",
        "status_error" to "Khalad ayaa dhacay xilligii soo dejinta",
        "cancel_button" to "Jooji",

        // Status Saver
        "status_saver_title" to "Keydiyaha Status-ka WhatsApp",
        "status_saver_desc" to "Kaydso fiidiyowyada iyo sawirrada WhatsApp inta aysan bixin",
        "status_refresh" to "Cusboonaysii",
        "status_save_all" to "Wada Kaydi",
        "status_saved_to_gallery" to "Waxaa lagu keydiyay Gallery-ga!",
        "no_statuses_found" to "Lama helin status cusub. Fur WhatsApp oo marka hore soo daawo qaar.",

        // Tools
        "tools_title" to "Aaladaha Muuqaalka & Codka",
        "tool_video_to_mp3" to "Fiidiyow u beddel MP3",
        "tool_video_to_mp3_desc" to "Ka saar codka fiidiyowga oo ka dhigo ringtone",
        "tool_video_compressor" to "Yaree Xajmiga Fiidiyowga",
        "tool_video_compressor_desc" to "Yaree xajmiga si aad WhatsApp ugu dirto",
        "tool_video_trimmer" to "Jariiqaha Fiidiyowga",
        "tool_video_trimmer_desc" to "Ka jar meelaha aadan rabin fiidiyowga",
        "tool_execute" to "Hadda Shaqee",
        "tool_success" to "Hawshii si guul leh ayay ku dhammaatay!",

        // Vault
        "vault_title" to "Qasnadda Qarsoon",
        "vault_desc" to "Ku ilaali faylashaada gaarka ah 4-lambar sir ah (PIN)",
        "vault_enter_pin" to "Geli 4-ta lambar ee sirta ah",
        "vault_setup_pin" to "Sameyso PIN cusub",
        "vault_unlock" to "Fur Qasnadda",
        "vault_lock" to "Xir Qasnadda",
        "vault_empty" to "Qasnadu waa maran tahay. Taabo 'Qari Fayl'.",
        "vault_add_file" to "Qari Fayl",
        "vault_pin_incorrect" to "PIN-ku waa khalad. Mar kale isku day.",

        // Downloads & Player
        "downloads_title" to "Faylasha La Soo Dejiyay",
        "downloads_empty" to "Waxba lama soo dejin weli",
        "play_action" to "Daaro",
        "share_action" to "La Wadaag",
        "delete_action" to "Tirtir",
        "player_title" to "Ciyaaryahanka Muuqaalka",
        "player_speed" to "Xawaaraha",
        "player_loop" to "Ku Celi",

        // Settings
        "settings_title" to "Hagaajinta",
        "language_title" to "Luuqadda / Language",
        "lang_english" to "English",
        "lang_somali" to "Af-Soomaali",
        "theme_title" to "Muuqaalka Shaashadda",
        "about_title" to "Ku Saabsan VidShare Pro",
        "about_desc" to "Wadaaga Faylasha Bilaa Internetka (Habka Xender), Soo Dejiyaha Muuqaallada Guud, iyo Qasnadda Sirta ah."
    )
}
