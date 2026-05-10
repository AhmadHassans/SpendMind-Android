package com.spendmindai.app.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val AccentColor = Color(0xFFE5534B)

private data class LanguageEntry(
    val code: String,
    val name: String,
    val nativeName: String,
    val region: String,
    val flag: String
)

private val SUPPORTED_LANGUAGES = listOf(
    LanguageEntry("en-US", "English", "English", "United States", "🇺🇸"),
    LanguageEntry("en-GB", "English", "English", "United Kingdom", "🇬🇧"),
    LanguageEntry("en-AU", "English", "English", "Australia", "🇦🇺"),
    LanguageEntry("en-CA", "English", "English", "Canada", "🇨🇦"),
    LanguageEntry("en-IN", "English", "English", "India", "🇮🇳"),
    LanguageEntry("en-PK", "Roman Urdu", "Roman Urdu", "Pakistan", "🇵🇰"),
    LanguageEntry("ur-PK", "Urdu", "اردو", "Pakistan", "🇵🇰"),
    LanguageEntry("hi-IN", "Hindi", "हिंदी", "India", "🇮🇳"),
    LanguageEntry("ar-SA", "Arabic", "العربية", "Saudi Arabia", "🇸🇦"),
    LanguageEntry("zh-CN", "Chinese", "中文", "China mainland", "🇨🇳"),
    LanguageEntry("zh-HK", "Chinese", "中文", "Hong Kong", "🇭🇰"),
    LanguageEntry("zh-TW", "Chinese", "中文", "Taiwan", "🇹🇼"),
    LanguageEntry("fr-FR", "French", "Français", "France", "🇫🇷"),
    LanguageEntry("fr-CA", "French", "Français", "Canada", "🇨🇦"),
    LanguageEntry("de-DE", "German", "Deutsch", "Germany", "🇩🇪"),
    LanguageEntry("de-AT", "German", "Deutsch", "Austria", "🇦🇹"),
    LanguageEntry("es-ES", "Spanish", "Español", "Spain", "🇪🇸"),
    LanguageEntry("es-MX", "Spanish", "Español", "Mexico", "🇲🇽"),
    LanguageEntry("es-US", "Spanish", "Español", "United States", "🇺🇸"),
    LanguageEntry("es-419", "Spanish", "Español", "Latin America", "🌎"),
    LanguageEntry("tr-TR", "Turkish", "Türkçe", "Türkiye", "🇹🇷"),
    LanguageEntry("bn-BD", "Bengali", "বাংলা", "Bangladesh", "🇧🇩"),
    LanguageEntry("pa-IN", "Punjabi", "ਪੰਜਾਬੀ", "India", "🇮🇳"),
    LanguageEntry("sd-PK", "Sindhi", "سنڌي", "Pakistan", "🇵🇰"),
    LanguageEntry("ps-AF", "Pashto", "پښتو", "Afghanistan", "🇦🇫"),
    LanguageEntry("fa-IR", "Persian", "فارسی", "Iran", "🇮🇷"),
    LanguageEntry("ru-RU", "Russian", "Русский", "Russia", "🇷🇺"),
    LanguageEntry("ja-JP", "Japanese", "日本語", "Japan", "🇯🇵"),
    LanguageEntry("ko-KR", "Korean", "한국어", "South Korea", "🇰🇷"),
    LanguageEntry("pt-BR", "Portuguese", "Português", "Brazil", "🇧🇷"),
    LanguageEntry("pt-PT", "Portuguese", "Português", "Portugal", "🇵🇹"),
    LanguageEntry("it-IT", "Italian", "Italiano", "Italy", "🇮🇹"),
    LanguageEntry("nl-NL", "Dutch", "Nederlands", "Netherlands", "🇳🇱"),
    LanguageEntry("pl-PL", "Polish", "Polski", "Poland", "🇵🇱"),
    LanguageEntry("id-ID", "Indonesian", "Bahasa Indonesia", "Indonesia", "🇮🇩"),
    LanguageEntry("ms-MY", "Malay", "Bahasa Melayu", "Malaysia", "🇲🇾"),
    LanguageEntry("th-TH", "Thai", "ภาษาไทย", "Thailand", "🇹🇭"),
    LanguageEntry("vi-VN", "Vietnamese", "Tiếng Việt", "Vietnam", "🇻🇳"),
    LanguageEntry("hi-Latn", "Hindi", "Hindi Translit", "India", "🇮🇳"),
    LanguageEntry("fi-FI", "Finnish", "Suomi", "Finland", "🇫🇮"),
    LanguageEntry("sv-SE", "Swedish", "Svenska", "Sweden", "🇸🇪"),
    LanguageEntry("da-DK", "Danish", "Dansk", "Denmark", "🇩🇰"),
    LanguageEntry("nb-NO", "Norwegian", "Norsk", "Norway", "🇳🇴"),
    LanguageEntry("el-GR", "Greek", "Ελληνικά", "Greece", "🇬🇷"),
    LanguageEntry("he-IL", "Hebrew", "עברית", "Israel", "🇮🇱"),
    LanguageEntry("uk-UA", "Ukrainian", "Українська", "Ukraine", "🇺🇦"),
    LanguageEntry("cs-CZ", "Czech", "Čeština", "Czechia", "🇨🇿"),
    LanguageEntry("sk-SK", "Slovak", "Slovenčina", "Slovakia", "🇸🇰"),
    LanguageEntry("hr-HR", "Croatian", "Hrvatski", "Croatia", "🇭🇷"),
    LanguageEntry("hu-HU", "Hungarian", "Magyar", "Hungary", "🇭🇺"),
    LanguageEntry("ro-RO", "Romanian", "Română", "Romania", "🇷🇴"),
    LanguageEntry("ca-ES", "Catalan", "Català", "Spain", "🇪🇸")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val selectedCode = prefs?.language ?: "en-US"
    var searchQuery by remember { mutableStateOf("") }

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) SUPPORTED_LANGUAGES
        else {
            val q = searchQuery.lowercase()
            SUPPORTED_LANGUAGES.filter {
                it.name.lowercase().contains(q) ||
                it.nativeName.lowercase().contains(q) ||
                it.region.lowercase().contains(q) ||
                it.code.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Language", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search languages…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredLanguages, key = { it.code }) { entry ->
                    LanguageRow(
                        entry = entry,
                        isSelected = selectedCode == entry.code,
                        onClick = {
                            viewModel.updateLanguage(entry.code)
                            onNavigateBack()
                        }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    entry: LanguageEntry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = entry.flag, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (entry.nativeName != entry.name) "${entry.nativeName} · ${entry.region}"
                       else entry.region,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (isSelected) {
            RadioButton(
                selected = true,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AccentColor)
            )
        }
    }
}
