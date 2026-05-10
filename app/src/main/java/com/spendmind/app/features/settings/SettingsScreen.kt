package com.spendmindai.app.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendmindai.app.BuildConfig
import com.spendmindai.app.features.categories.CategoryManagementScreen
import com.spendmindai.app.features.premium.PremiumScreen
import kotlinx.coroutines.launch
import java.io.File

// ── iOS system palette ────────────────────────────────────────────────────────
private val IosGreen      = Color(0xFF34C759)
private val IosBlue       = Color(0xFF007AFF)
private val IosOrange     = Color(0xFFFF9500)
private val IosGray       = Color(0xFF8E8E93)
private val IosPurple     = Color(0xFFAF52DE)
private val IosRed        = Color(0xFFFF3B30)
private val IosSettingsBg = Color(0xFFF2F2F7)
private val IosCardBg     = Color.White
private val IosHeaderText = Color(0xFF6D6D72)
private val IosDivider    = Color(0xFFE5E5EA)
private val IosChevron    = Color(0xFFC7C7CC)

private const val PRIVACY_URL    = "https://spendmind.ai/privacy"
private const val TERMS_URL      = "https://spendmind.ai/terms"
private const val PREFS_NAME     = "spendmind_settings"
private const val KEY_APPEARANCE = "appearance_mode"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs     by viewModel.userPreferences.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val scope     = rememberCoroutineScope()
    val context   = LocalContext.current

    var showCategorySheet    by remember { mutableStateOf(false) }
    var showRecurringSheet   by remember { mutableStateOf(false) }
    var showMonthSheet       by remember { mutableStateOf(false) }
    var showReminderSheet    by remember { mutableStateOf(false) }
    var showLanguageSheet    by remember { mutableStateOf(false) }
    var showCurrencySheet    by remember { mutableStateOf(false) }
    var showAppearanceSheet  by remember { mutableStateOf(false) }
    var showVoiceTipsSheet   by remember { mutableStateOf(false) }
    var showAISheet          by remember { mutableStateOf(false) }
    var showDataSheet        by remember { mutableStateOf(false) }
    var showPremiumSheet     by remember { mutableStateOf(false) }
    var isExporting          by remember { mutableStateOf(false) }

    val sp = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var appearanceLabel by remember {
        mutableStateOf(
            when (sp.getString(KEY_APPEARANCE, "system")) {
                "light" -> "Light"
                "dark"  -> "Dark"
                else    -> "System default"
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IosSettingsBg)
            )
        },
        containerColor = IosSettingsBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Expenses ──────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Expenses", topPadding = 8.dp) {
                    SettingsRow(
                        icon = Icons.Filled.Category, iconColor = IosGreen,
                        title = "Categories", subtitle = "Manage expense categories",
                        onClick = { showCategorySheet = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Filled.Repeat, iconColor = IosBlue,
                        title = "Recurring Expenses", subtitle = "Manage scheduled expenses",
                        onClick = { showRecurringSheet = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Filled.CalendarToday, iconColor = IosGray,
                        title = "Monthly Cycle Start",
                        subtitle = if ((prefs?.monthStartDay ?: 1) == 1) "Standard (1st of month)"
                                   else "Day ${prefs?.monthStartDay}",
                        onClick = { showMonthSheet = true }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Filled.Notifications, iconColor = IosOrange,
                        title = "Set Reminder",
                        subtitle = if (prefs?.notificationEnabled == true) "Tap to set reminder time"
                                   else "Daily expense reminder",
                        checked = prefs?.notificationEnabled ?: false,
                        onCheckedChange = {
                            viewModel.updateNotificationEnabled(it)
                            if (it) showReminderSheet = true
                        }
                    )
                }
            }

            // ── Preferences ───────────────────────────────────────────────────
            item {
                SettingsSection(title = "Preferences") {
                    SettingsRow(
                        icon = Icons.Filled.Language, iconColor = IosOrange,
                        title = "Voice Language", subtitle = languageDisplayName(prefs?.language),
                        onClick = { showLanguageSheet = true }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Filled.FlashOn, iconColor = IosBlue,
                        title = "Auto-Add Voice",
                        subtitle = if (prefs?.autoSubmitTranscription == true)
                                       "Saves automatically without review"
                                   else "Review expenses before saving",
                        checked = prefs?.autoSubmitTranscription ?: false,
                        onCheckedChange = { viewModel.updateAutoSubmit(it) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Filled.AttachMoney, iconColor = IosGreen,
                        title = "Default Currency", subtitle = prefs?.currency ?: "USD",
                        onClick = { showCurrencySheet = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Filled.Palette, iconColor = IosPurple,
                        title = "Appearance", subtitle = appearanceLabel,
                        onClick = { showAppearanceSheet = true }
                    )
                }
            }

            // ── Help & Tips ───────────────────────────────────────────────────
            item {
                SettingsSection(title = "Help & Tips") {
                    SettingsRow(
                        icon = Icons.Filled.Lightbulb, iconColor = IosOrange,
                        title = "Voice Recording Tips", subtitle = "Learn to record expenses by voice",
                        onClick = { showVoiceTipsSheet = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Filled.AutoAwesome, iconColor = IosPurple,
                        title = "AI Improvements", subtitle = "Help improve AI accuracy",
                        onClick = { showAISheet = true }
                    )
                }
            }

            // ── Data ──────────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Data") {
                    SettingsRow(
                        icon = Icons.Filled.Share, iconColor = IosGreen,
                        title = if (isExporting) "Exporting…" else "Export Expenses",
                        subtitle = "Save expenses as CSV file",
                        onClick = {
                            if (!isExporting) {
                                scope.launch {
                                    isExporting = true
                                    try {
                                        val csv = viewModel.exportExpensesAsCSV()
                                        val file = File(context.cacheDir, "spendmind_export.csv")
                                        file.writeText(csv)
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/csv"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            putExtra(Intent.EXTRA_SUBJECT, "SpendMind Expense Export")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Export Expenses"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Filled.Settings, iconColor = IosBlue,
                        title = "Manage Data", subtitle = "Backup, restore, and delete data",
                        onClick = { showDataSheet = true }
                    )
                }
            }

            // ── Account ───────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Account") {
                    SettingsRow(
                        icon = Icons.Filled.Star, iconColor = IosBlue,
                        title = "Premium Plan",
                        subtitle = if (isPremium) "Active" else "Upgrade to Premium",
                        onClick = { showPremiumSheet = true }
                    )
                }
            }

            // ── Legal ─────────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Legal") {
                    SettingsRow(
                        icon = Icons.Filled.Lock, iconColor = IosBlue,
                        title = "Privacy Policy", subtitle = "How we handle your data",
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL))) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Filled.Description, iconColor = IosGray,
                        title = "Terms and Conditions", subtitle = "Usage terms and agreements",
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL))) }
                    )
                }
            }

            // ── Debug ─────────────────────────────────────────────────────────
            if (BuildConfig.DEBUG) {
                item {
                    SettingsSection(title = "Debug") {
                        SettingsRow(
                            icon = Icons.Filled.Warning, iconColor = IosRed,
                            title = "Test Crash", subtitle = "Crashes the app to test Crashlytics",
                            titleColor = IosRed,
                            onClick = { throw RuntimeException("🔥 Test crash — debug-only") }
                        )
                    }
                }
            }

            // ── Footer ────────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Developed for 🌍 with ❤️ by SpendMind-AI", style = MaterialTheme.typography.bodySmall, color = IosGray, textAlign = TextAlign.Center)
                    Text("App version: 1.0.0", style = MaterialTheme.typography.bodySmall, color = IosGray, textAlign = TextAlign.Center)
                }
            }
        }
    }

    // ── Sheets ───────────────────────────────────────────────────────────────

    if (showCategorySheet) {
        CategoryManagementScreen(onDismiss = { showCategorySheet = false })
    }

    if (showRecurringSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecurringSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            RecurringExpenseScreen(onNavigateBack = { showRecurringSheet = false })
        }
    }

    if (showMonthSheet) {
        MonthPickerSheet(
            currentDay = prefs?.monthStartDay ?: 1,
            onDismiss = { showMonthSheet = false },
            onSave = { day -> viewModel.updateMonthStartDay(day); showMonthSheet = false }
        )
    }

    if (showReminderSheet) {
        ReminderTimeSheet(
            currentHour = prefs?.notificationHour ?: 21,
            currentMinute = prefs?.notificationMinute ?: 0,
            onDismiss = { showReminderSheet = false },
            onSave = { h, m -> viewModel.updateNotificationTime(h, m); showReminderSheet = false }
        )
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LanguageSelectionScreen(onNavigateBack = { showLanguageSheet = false })
        }
    }

    if (showCurrencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCurrencySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CurrencySelectionScreen(onNavigateBack = { showCurrencySheet = false })
        }
    }

    if (showAppearanceSheet) {
        AppearanceSheet(
            context = context,
            onDismiss = { showAppearanceSheet = false },
            onLabelChanged = { appearanceLabel = it }
        )
    }

    if (showVoiceTipsSheet) {
        VoiceTipsSheet(onDismiss = { showVoiceTipsSheet = false })
    }

    if (showAISheet) {
        AIImprovementsSheet(onDismiss = { showAISheet = false })
    }

    if (showDataSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDataSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            DataManagementScreen(onNavigateBack = { showDataSheet = false })
        }
    }

    if (showPremiumSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPremiumSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            PremiumScreen(onNavigateBack = { showPremiumSheet = false })
        }
    }
}

// ── Month Picker Sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthPickerSheet(
    currentDay: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var selected by remember { mutableStateOf(currentDay) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Month Start Day", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Set a custom start day so your month runs from day $selected to day ${if (selected == 1) "last" else "${selected - 1} of next month"}.",
                style = MaterialTheme.typography.bodyMedium, color = IosGray
            )

            // Day grid 7 per row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..28).chunked(7).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(if (day == selected) Color.Black else Color(0xFFF2F2F7))
                                    .clickable { selected = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$day",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (day == selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (day == selected) Color.White else Color.Black
                                )
                            }
                        }
                        repeat(7 - row.size) { Box(modifier = Modifier.weight(1f)) }
                    }
                }
            }

            // Preview
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF2F2F7), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (selected == 1) {
                        Text("Standard calendar month", fontWeight = FontWeight.SemiBold)
                        Text("From 1st to last day of each month", color = IosGray, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Custom month period", fontWeight = FontWeight.SemiBold)
                        Text("From ${selected}th to ${selected - 1}th of next month", color = IosGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = { onSave(selected) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Reminder Time Sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeSheet(
    currentHour: Int,
    currentMinute: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = currentHour, initialMinute = currentMinute, is24Hour = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daily Reminder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Text("Set a daily reminder to log your expenses.", style = MaterialTheme.typography.bodyMedium, color = IosGray, modifier = Modifier.fillMaxWidth())
            TimePicker(state = state)
            Button(
                onClick = { onSave(state.hour, state.minute) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Appearance Sheet ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSheet(
    context: Context,
    onDismiss: () -> Unit,
    onLabelChanged: (String) -> Unit
) {
    val sp = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var selected by remember { mutableStateOf(sp.getString(KEY_APPEARANCE, "system") ?: "system") }

    val options = listOf(
        Triple("system", "System Default", Icons.Filled.Settings),
        Triple("light",  "Light",          Icons.Filled.WbSunny),
        Triple("dark",   "Dark",            Icons.Filled.NightsStay)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Surface(shape = RoundedCornerShape(12.dp), color = IosCardBg, modifier = Modifier.fillMaxWidth()) {
                Column {
                    options.forEachIndexed { i, (mode, label, icon) ->
                        val iconBg = when (mode) {
                            "light" -> Color(0xFFFFCC00)
                            "dark"  -> Color(0xFF1C1C1E)
                            else    -> IosGray
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = mode
                                    sp.edit().putString(KEY_APPEARANCE, mode).apply()
                                    val lbl = when (mode) { "light" -> "Light"; "dark" -> "Dark"; else -> "System default" }
                                    onLabelChanged(lbl)
                                    Toast.makeText(context, "Restart app to apply theme", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (selected == mode) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = IosBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (i < options.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(start = 64.dp), color = IosDivider, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

// ── Voice Tips Sheet ───────────────────────────────────────────────────────────

private data class TipItem(val icon: ImageVector, val title: String, val body: String)

private val VOICE_TIPS = listOf(
    TipItem(Icons.Filled.Mic,       "Hold Close",         "Hold your phone close to your mouth for best results."),
    TipItem(Icons.Filled.Settings,  "Speak Clearly",      "Speak at a natural pace — not too fast, not too slow."),
    TipItem(Icons.Filled.Warning,   "Quiet Environment",  "Record in a quiet place to reduce background noise."),
    TipItem(Icons.Filled.Check,     "Natural Speech",     "Speak naturally, as if telling a friend what you spent."),
    TipItem(Icons.Filled.Schedule,  "Take Your Time",     "Pause briefly before and after each expense for clarity."),
    TipItem(Icons.Filled.Star,      "Be Specific",        "Mention what it was for, e.g. 'coffee at Starbucks'."),
    TipItem(Icons.Filled.AttachMoney,"Include Amount",    "Always say the amount, e.g. 'spent 5 dollars on lunch'.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceTipsSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = IosOrange, modifier = Modifier.size(48.dp))
                Text("Voice Recording Tips", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Follow these tips for best AI expense recognition", style = MaterialTheme.typography.bodyMedium, color = IosGray, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(4.dp))

            VOICE_TIPS.forEach { tip ->
                Surface(shape = RoundedCornerShape(12.dp), color = IosCardBg, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(IosOrange.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(tip.icon, contentDescription = null, tint = IosOrange, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tip.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(tip.body, style = MaterialTheme.typography.bodySmall, color = IosGray)
                        }
                    }
                }
            }
        }
    }
}

// ── AI Improvements Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIImprovementsSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = IosPurple, modifier = Modifier.size(48.dp))
                Text("AI Improvements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Surface(shape = RoundedCornerShape(12.dp), color = IosCardBg, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Help improve SpendMind AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "When you correct an AI-suggested category or amount, these corrections help us improve accuracy for everyone. Your corrections are anonymized and never linked to personal data.",
                        style = MaterialTheme.typography.bodyMedium, color = IosGray
                    )
                    HorizontalDivider(color = IosDivider, thickness = 0.5.dp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(IosPurple.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = IosPurple, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacy First", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Corrections are anonymized", style = MaterialTheme.typography.bodySmall, color = IosGray)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(IosGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = IosGreen, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Better Accuracy", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Improves category detection over time", style = MaterialTheme.typography.bodySmall, color = IosGray)
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Reusable row composables ───────────────────────────────────────────────────

@Composable
private fun SettingsSection(title: String, topPadding: Dp = 16.dp, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = topPadding)
            .clip(RoundedCornerShape(12.dp))
            .background(IosCardBg)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = IosHeaderText,
            fontWeight = FontWeight.Medium
        )
        content()
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(iconColor), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = titleColor)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = IosGray)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = IosChevron, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(iconColor), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.Black)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = IosGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IosGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5EA)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = IosDivider, thickness = 0.5.dp)
}

private val LANGUAGE_DISPLAY_NAMES = mapOf(
    "en-US"   to "English (United States)",
    "en-GB"   to "English (United Kingdom)",
    "en-AU"   to "English (Australia)",
    "en-CA"   to "English (Canada)",
    "en-IN"   to "English (India)",
    "en-PK"   to "Roman Urdu (Pakistan)",
    "ur-PK"   to "Urdu · اردو (Pakistan)",
    "hi-IN"   to "Hindi · हिंदी (India)",
    "hi-Latn" to "Hindi Translit (India)",
    "ar-SA"   to "Arabic · العربية (Saudi Arabia)",
    "zh-CN"   to "Chinese · 中文 (China)",
    "zh-HK"   to "Chinese · 中文 (Hong Kong)",
    "zh-TW"   to "Chinese · 中文 (Taiwan)",
    "fr-FR"   to "French · Français (France)",
    "fr-CA"   to "French · Français (Canada)",
    "de-DE"   to "German · Deutsch (Germany)",
    "de-AT"   to "German · Deutsch (Austria)",
    "es-ES"   to "Spanish · Español (Spain)",
    "es-MX"   to "Spanish · Español (Mexico)",
    "es-US"   to "Spanish · Español (United States)",
    "es-419"  to "Spanish · Español (Latin America)",
    "tr-TR"   to "Turkish · Türkçe (Türkiye)",
    "bn-BD"   to "Bengali · বাংলা (Bangladesh)",
    "pa-IN"   to "Punjabi · ਪੰਜਾਬੀ (India)",
    "sd-PK"   to "Sindhi · سنڌي (Pakistan)",
    "ps-AF"   to "Pashto · پښتو (Afghanistan)",
    "fa-IR"   to "Persian · فارسی (Iran)",
    "ru-RU"   to "Russian · Русский (Russia)",
    "ja-JP"   to "Japanese · 日本語 (Japan)",
    "ko-KR"   to "Korean · 한국어 (South Korea)",
    "pt-BR"   to "Portuguese · Português (Brazil)",
    "pt-PT"   to "Portuguese · Português (Portugal)",
    "it-IT"   to "Italian · Italiano (Italy)",
    "nl-NL"   to "Dutch · Nederlands (Netherlands)",
    "pl-PL"   to "Polish · Polski (Poland)",
    "id-ID"   to "Indonesian · Bahasa Indonesia",
    "ms-MY"   to "Malay · Bahasa Melayu (Malaysia)",
    "th-TH"   to "Thai · ภาษาไทย (Thailand)",
    "vi-VN"   to "Vietnamese · Tiếng Việt (Vietnam)",
    "fi-FI"   to "Finnish · Suomi (Finland)",
    "sv-SE"   to "Swedish · Svenska (Sweden)",
    "da-DK"   to "Danish · Dansk (Denmark)",
    "nb-NO"   to "Norwegian · Norsk (Norway)",
    "el-GR"   to "Greek · Ελληνικά (Greece)",
    "he-IL"   to "Hebrew · עברית (Israel)",
    "uk-UA"   to "Ukrainian · Українська (Ukraine)",
    "cs-CZ"   to "Czech · Čeština (Czechia)",
    "sk-SK"   to "Slovak · Slovenčina (Slovakia)",
    "hr-HR"   to "Croatian · Hrvatski (Croatia)",
    "hu-HU"   to "Hungarian · Magyar (Hungary)",
    "ro-RO"   to "Romanian · Română (Romania)",
    "ca-ES"   to "Catalan · Català (Spain)"
)

private fun languageDisplayName(code: String?): String =
    LANGUAGE_DISPLAY_NAMES[code] ?: "English (United States)"
