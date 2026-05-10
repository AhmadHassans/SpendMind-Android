package com.spendmindai.app.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.domain.model.Expense
import com.spendmindai.app.core.infrastructure.export.DataExportService
import com.spendmindai.app.core.infrastructure.export.DataImportService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val AccentColor = Color(0xFFE5534B)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

sealed class DataManagementEvent {
    data class ExportReady(val csvContent: String) : DataManagementEvent()
    data class ImportSuccess(val count: Int) : DataManagementEvent()
    data class ImportError(val message: String) : DataManagementEvent()
    object DeleteSuccess : DataManagementEvent()
    data class Error(val message: String) : DataManagementEvent()
}

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val dataExportService: DataExportService,
    private val dataImportService: DataImportService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val expenseCount: StateFlow<Int> =
        expenseRepository.getAllExpenses()
            .map { it.size }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableStateFlow<DataManagementEvent?>(null)
    val event: StateFlow<DataManagementEvent?> = _event

    fun clearEvent() { _event.value = null }

    fun export() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val csv = withContext(Dispatchers.IO) {
                    val expenses = mutableListOf<Expense>()
                    expenseRepository.getAllExpenses().collect { list -> expenses.addAll(list) }
                    val categories = categoryRepository.getAllCategories()
                        .let { flow ->
                            val result = mutableListOf<com.spendmindai.app.core.domain.model.Category>()
                            flow.collect { result.addAll(it) }
                            result
                        }
                    val categoryMap = categories.associateBy { it.id }
                    dataExportService.exportToCSV(expenses, categoryMap)
                }
                _event.value = DataManagementEvent.ExportReady(csv)
            } catch (e: Exception) {
                _event.value = DataManagementEvent.Error(e.message ?: "Export failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val content = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.use { it.readText() }
                        ?: return@withContext Result.failure<List<Expense>>(Exception("Cannot read file"))
                    dataImportService.importFromCSV(content)
                }
                result
                    .onSuccess { expenses ->
                        withContext(Dispatchers.IO) {
                            expenseRepository.insertExpenses(expenses)
                        }
                        _event.value = DataManagementEvent.ImportSuccess(expenses.size)
                    }
                    .onFailure { e ->
                        _event.value = DataManagementEvent.ImportError(e.message ?: "Import failed")
                    }
            } catch (e: Exception) {
                _event.value = DataManagementEvent.Error(e.message ?: "Import failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    expenseRepository.deleteAllExpenses()
                }
                _event.value = DataManagementEvent.DeleteSuccess
            } catch (e: Exception) {
                _event.value = DataManagementEvent.Error(e.message ?: "Delete failed")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val expenseCount by viewModel.expenseCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // CSV file picker
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.import(it) }
    }

    // Handle events
    LaunchedEffect(event) {
        when (val e = event) {
            is DataManagementEvent.ExportReady -> {
                shareCSV(context, e.csvContent)
                viewModel.clearEvent()
            }
            is DataManagementEvent.ImportSuccess -> {
                snackbarMessage = "Imported ${e.count} expense(s) successfully"
                viewModel.clearEvent()
            }
            is DataManagementEvent.ImportError -> {
                snackbarMessage = "Import failed: ${e.message}"
                viewModel.clearEvent()
            }
            is DataManagementEvent.DeleteSuccess -> {
                snackbarMessage = "All data deleted"
                viewModel.clearEvent()
            }
            is DataManagementEvent.Error -> {
                snackbarMessage = e.message
                viewModel.clearEvent()
            }
            null -> Unit
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import / Export", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Summary card ─────────────────────────────────────────────────
            SummaryCard(expenseCount = expenseCount)

            // ── Export section ───────────────────────────────────────────────
            DataSectionCard(title = "Export Data", icon = Icons.Filled.Upload) {
                Text(
                    "Export all your expenses as a CSV file that can be opened in Excel or Google Sheets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.export() },
                    enabled = !isLoading && expenseCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(
                            Icons.Filled.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Export to CSV", fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Import section ───────────────────────────────────────────────
            DataSectionCard(title = "Import Data", icon = Icons.Filled.Download) {
                Text(
                    "Import expenses from a CSV file. Columns: Date, Amount, Currency, Category, Note, Source.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { importLauncher.launch("text/*") },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor)
                ) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Select CSV File", fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Danger zone ──────────────────────────────────────────────────
            DataSectionCard(
                title = "Danger Zone",
                icon = Icons.Filled.Warning,
                iconTint = Color(0xFFE53935),
                titleColor = Color(0xFFE53935)
            ) {
                Text(
                    "Permanently delete all expenses from this device. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    enabled = !isLoading && expenseCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Delete All Data", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Delete confirmation ───────────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color(0xFFE53935)) },
            title = { Text("Delete All Data?") },
            text = {
                Text("This will permanently delete all $expenseCount expense(s). This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAll()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                ) { Text("Delete Everything") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(expenseCount: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AccentColor.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Storage,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "$expenseCount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentColor
                )
                Text(
                    "Expense records stored",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun DataSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = AccentColor,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = titleColor)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────────────────────

private fun shareCSV(context: Context, csvContent: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_TEXT, csvContent)
        putExtra(Intent.EXTRA_SUBJECT, "SpendMind Expense Export")
    }
    context.startActivity(Intent.createChooser(intent, "Export expenses via…"))
}
