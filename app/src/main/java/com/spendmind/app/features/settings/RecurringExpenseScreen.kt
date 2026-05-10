package com.spendmindai.app.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.RecurringExpenseRepository
import com.spendmindai.app.core.data.repository.UserPreferencesRepository
import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.core.domain.model.RecurrenceFrequency
import com.spendmindai.app.core.domain.model.RecurringExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private val AccentColor = Color(0xFFE5534B)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class RecurringExpenseViewModel @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val recurringExpenses: StateFlow<List<RecurringExpense>> =
        recurringExpenseRepository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val categories: StateFlow<List<Category>> =
        categoryRepository.getAllCategories()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val defaultCurrency: StateFlow<String> =
        kotlinx.coroutines.flow.flow {
            emit(userPreferencesRepository.getUserPreferencesOnce().currency)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "USD"
        )

    fun addRecurring(
        amount: Double,
        currency: String,
        frequency: RecurrenceFrequency,
        categoryId: String?,
        note: String?,
        startDate: Date
    ) {
        viewModelScope.launch {
            val expense = RecurringExpense.create(
                amount = amount,
                currency = currency,
                frequency = frequency,
                isIncome = false,
                note = note.takeIf { !it.isNullOrBlank() },
                categoryId = categoryId,
                startDate = startDate
            )
            recurringExpenseRepository.insert(expense)
        }
    }

    fun deleteRecurring(expense: RecurringExpense) {
        viewModelScope.launch {
            recurringExpenseRepository.delete(expense)
        }
    }

    fun toggleActive(expense: RecurringExpense) {
        viewModelScope.launch {
            recurringExpenseRepository.update(expense.copy(isActive = !expense.isActive))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecurringExpenseViewModel = hiltViewModel()
) {
    val expenses by viewModel.recurringExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<RecurringExpense?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Expenses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = AccentColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add recurring expense")
            }
        }
    ) { padding ->
        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No recurring expenses yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to add a scheduled expense",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(expenses, key = { it.id }) { expense ->
                    val category = categories.find { it.id == expense.categoryId }
                    RecurringExpenseCard(
                        expense = expense,
                        category = category,
                        onToggleActive = { viewModel.toggleActive(expense) },
                        onDelete = { expenseToDelete = expense }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    // ── Delete confirmation ───────────────────────────────────────────────────
    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Recurring Expense?") },
            text = { Text("This will stop future expenses from being created automatically.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecurring(expense)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // ── Add bottom sheet ──────────────────────────────────────────────────────
    if (showAddSheet) {
        AddRecurringSheet(
            categories = categories,
            defaultCurrency = defaultCurrency,
            onDismiss = { showAddSheet = false },
            onSave = { amount, currency, frequency, categoryId, note ->
                viewModel.addRecurring(
                    amount = amount,
                    currency = currency,
                    frequency = frequency,
                    categoryId = categoryId,
                    note = note,
                    startDate = Date()
                )
                showAddSheet = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Recurring Expense Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecurringExpenseCard(
    expense: RecurringExpense,
    category: Category?,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category emoji
            Text(
                text = category?.emoji ?: "🔄",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(40.dp),
                maxLines = 1
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${expense.currency} ${String.format("%.2f", expense.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    FrequencyBadge(expense.frequency)
                }
                if (!expense.note.isNullOrBlank()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
                expense.nextDueDate?.let { nextDue ->
                    Text(
                        text = "Next: ${dateFormatter.format(nextDue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentColor
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = expense.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentColor,
                        checkedTrackColor = AccentColor.copy(alpha = 0.3f)
                    )
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequencyBadge(frequency: RecurrenceFrequency) {
    val label = when (frequency) {
        RecurrenceFrequency.DAILY -> "Daily"
        RecurrenceFrequency.WEEKLY -> "Weekly"
        RecurrenceFrequency.MONTHLY -> "Monthly"
        RecurrenceFrequency.YEARLY -> "Yearly"
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = AccentColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AccentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Recurring Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecurringSheet(
    categories: List<Category>,
    defaultCurrency: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, currency: String, frequency: RecurrenceFrequency, categoryId: String?, note: String?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(defaultCurrency) }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "New Recurring Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = false
                },
                label = { Text("Amount") },
                isError = amountError,
                supportingText = if (amountError) ({ Text("Enter a valid amount") }) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text(
                        currency,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentColor
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    cursorColor = AccentColor
                ),
                singleLine = true
            )

            // Frequency dropdown
            Box {
                OutlinedTextField(
                    value = frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    trailingIcon = {
                        IconButton(onClick = { showFrequencyMenu = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                )
                DropdownMenu(
                    expanded = showFrequencyMenu,
                    onDismissRequest = { showFrequencyMenu = false }
                ) {
                    RecurrenceFrequency.values().forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                frequency = freq
                                showFrequencyMenu = false
                            }
                        )
                    }
                }
            }

            // Category picker
            Box {
                OutlinedTextField(
                    value = categories.find { it.id == selectedCategoryId }
                        ?.let { "${it.emoji} ${it.name}" } ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        IconButton(onClick = { showCategoryMenu = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                )
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            selectedCategoryId = null
                            showCategoryMenu = false
                        }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.emoji} ${cat.name}") },
                            onClick = {
                                selectedCategoryId = cat.id
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    cursorColor = AccentColor
                ),
                maxLines = 2
            )

            // Save button
            Button(
                onClick = {
                    val amount = amountText.trim().toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        amountError = true
                        return@Button
                    }
                    onSave(amount, currency, frequency, selectedCategoryId, noteText.trim().ifEmpty { null })
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
