package com.spendmindai.app.features.record

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendmindai.app.features.categories.CategoryManagementScreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ManualExpenseScreen(
    onClose: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ManualExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isCategoryExpanded by remember { mutableStateOf(true) }
    var showCategoryManagement by remember { mutableStateOf(false) }
    var showRecurrenceMenu by remember { mutableStateOf(false) }
    var selectedRecurrence by remember { mutableStateOf("Once") }
    val recurrenceOptions = listOf("Once", "Daily", "Weekly", "Monthly", "Yearly")

    LaunchedEffect(uiState.selectedCategory) {
        if (uiState.selectedCategory != null) isCategoryExpanded = false
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    val dateLabel = remember(uiState.date) {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }
        val d = Calendar.getInstance().also { it.time = uiState.date }
        when {
            d.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    d.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
            d.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    d.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
            else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(uiState.date)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Scrollable body ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 60.dp, bottom = 120.dp)
        ) {

            // ── Date + Recurrence pills ──────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date pill
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF2F2F7),
                    modifier = Modifier.clickable {
                        val cal = Calendar.getInstance().also { it.time = uiState.date }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val sel = Calendar.getInstance().also { it.set(year, month, day) }
                                viewModel.setDate(sel.time)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(dateLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF8E8E93))
                    }
                }

                // Recurrence pill
                Box {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF2F2F7),
                        modifier = Modifier.clickable { showRecurrenceMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(selectedRecurrence, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF8E8E93))
                        }
                    }
                    DropdownMenu(
                        expanded = showRecurrenceMenu,
                        onDismissRequest = { showRecurrenceMenu = false }
                    ) {
                        recurrenceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { selectedRecurrence = option; showRecurrenceMenu = false },
                                trailingIcon = if (selectedRecurrence == option) ({
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }) else null
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Description field ────────────────────────────────────────────
            BasicTextField(
                value = uiState.note,
                onValueChange = { viewModel.setNote(it) },
                textStyle = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, color = Color.Black),
                cursorBrush = SolidColor(Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                decorationBox = { inner ->
                    if (uiState.note.isEmpty()) {
                        Text("Description", fontSize = 34.sp, fontWeight = FontWeight.SemiBold, color = Color.Black.copy(alpha = 0.25f))
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(16.dp))

            // ── Amount field ─────────────────────────────────────────────────
            BasicTextField(
                value = uiState.amountText,
                onValueChange = { viewModel.setAmount(it) },
                textStyle = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, color = Color.Black),
                cursorBrush = SolidColor(Color.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                decorationBox = { inner ->
                    if (uiState.amountText.isEmpty()) {
                        Text("Amount", fontSize = 34.sp, fontWeight = FontWeight.SemiBold, color = Color.Black.copy(alpha = 0.25f))
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Category section ─────────────────────────────────────────────
            val selected = uiState.selectedCategory
            if (selected != null && !isCategoryExpanded) {
                // Collapsed — single outlined pill
                Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier.clickable { isCategoryExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(selected.emoji ?: "📦", fontSize = 18.sp)
                            Text(selected.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        }
                    }
                }
            } else {
                // Expanded — horizontal scrollable pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // + button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F7))
                            .clickable { showCategoryManagement = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add category", modifier = Modifier.size(22.dp), tint = Color.Black)
                    }

                    // Category pills
                    uiState.categories.forEach { category ->
                        val isSelected = uiState.selectedCategory?.id == category.id
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.Black.copy(alpha = 0.08f) else Color(0xFFF2F2F7),
                            border = if (isSelected) BorderStroke(2.dp, Color.Black) else null,
                            modifier = Modifier.clickable { viewModel.setCategory(category) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(category.emoji ?: "📦", fontSize = 18.sp)
                                Text(category.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // ── X close button top-right ─────────────────────────────────────────
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF2F2F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp), tint = Color.Black)
            }
        }

        // ── Category management sheet ────────────────────────────────────────
        if (showCategoryManagement) {
            CategoryManagementScreen(onDismiss = { showCategoryManagement = false })
        }

        // ── Floating Save button ─────────────────────────────────────────────
        Button(
            onClick = { viewModel.save() },
            enabled = uiState.amountText.isNotBlank() && !uiState.isSaving,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                disabledContainerColor = Color.Black.copy(alpha = 0.3f)
            )
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}
