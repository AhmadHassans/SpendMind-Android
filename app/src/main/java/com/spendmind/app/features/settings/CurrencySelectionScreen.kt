package com.spendmindai.app.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val AccentColor = Color(0xFFE5534B)

private data class CurrencyEntry(
    val code: String,
    val symbol: String,
    val country: String
)

private val ALL_CURRENCIES = listOf(
    CurrencyEntry("USD", "$", "United States Dollar"),
    CurrencyEntry("EUR", "€", "Euro"),
    CurrencyEntry("GBP", "£", "British Pound Sterling"),
    CurrencyEntry("JPY", "¥", "Japanese Yen"),
    CurrencyEntry("CAD", "C$", "Canadian Dollar"),
    CurrencyEntry("AUD", "A$", "Australian Dollar"),
    CurrencyEntry("CHF", "Fr", "Swiss Franc"),
    CurrencyEntry("CNY", "¥", "Chinese Yuan"),
    CurrencyEntry("INR", "₹", "Indian Rupee"),
    CurrencyEntry("PKR", "₨", "Pakistani Rupee"),
    CurrencyEntry("BRL", "R$", "Brazilian Real"),
    CurrencyEntry("MXN", "MX$", "Mexican Peso"),
    CurrencyEntry("KRW", "₩", "South Korean Won"),
    CurrencyEntry("SGD", "S$", "Singapore Dollar"),
    CurrencyEntry("HKD", "HK$", "Hong Kong Dollar"),
    CurrencyEntry("NOK", "kr", "Norwegian Krone"),
    CurrencyEntry("SEK", "kr", "Swedish Krona"),
    CurrencyEntry("DKK", "kr", "Danish Krone"),
    CurrencyEntry("NZD", "NZ$", "New Zealand Dollar"),
    CurrencyEntry("ZAR", "R", "South African Rand"),
    CurrencyEntry("AED", "د.إ", "UAE Dirham"),
    CurrencyEntry("SAR", "﷼", "Saudi Riyal"),
    CurrencyEntry("TRY", "₺", "Turkish Lira"),
    CurrencyEntry("THB", "฿", "Thai Baht"),
    CurrencyEntry("IDR", "Rp", "Indonesian Rupiah"),
    CurrencyEntry("MYR", "RM", "Malaysian Ringgit"),
    CurrencyEntry("PHP", "₱", "Philippine Peso"),
    CurrencyEntry("ILS", "₪", "Israeli New Shekel"),
    CurrencyEntry("EGP", "£", "Egyptian Pound"),
    CurrencyEntry("NGN", "₦", "Nigerian Naira"),
    CurrencyEntry("KES", "KSh", "Kenyan Shilling"),
    CurrencyEntry("GHS", "₵", "Ghanaian Cedi"),
    CurrencyEntry("MAD", "د.م.", "Moroccan Dirham"),
    CurrencyEntry("BDT", "৳", "Bangladeshi Taka"),
    CurrencyEntry("VND", "₫", "Vietnamese Dong"),
    CurrencyEntry("CLP", "CLP$", "Chilean Peso"),
    CurrencyEntry("COP", "COL$", "Colombian Peso"),
    CurrencyEntry("PEN", "S/", "Peruvian Sol"),
    CurrencyEntry("ARS", "ARS$", "Argentine Peso"),
    CurrencyEntry("UAH", "₴", "Ukrainian Hryvnia"),
    CurrencyEntry("PLN", "zł", "Polish Zloty"),
    CurrencyEntry("CZK", "Kč", "Czech Koruna"),
    CurrencyEntry("HUF", "Ft", "Hungarian Forint"),
    CurrencyEntry("RON", "lei", "Romanian Leu"),
    CurrencyEntry("BGN", "лв", "Bulgarian Lev"),
    CurrencyEntry("HRK", "kn", "Croatian Kuna"),
    CurrencyEntry("RSD", "дин", "Serbian Dinar"),
    CurrencyEntry("TWD", "NT$", "New Taiwan Dollar"),
    CurrencyEntry("QAR", "﷼", "Qatari Riyal"),
    CurrencyEntry("KWD", "د.ك", "Kuwaiti Dinar")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPreferences.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ALL_CURRENCIES
        } else {
            val q = searchQuery.trim().lowercase()
            ALL_CURRENCIES.filter { entry ->
                entry.code.lowercase().contains(q) ||
                        entry.country.lowercase().contains(q) ||
                        entry.symbol.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Currency", fontWeight = FontWeight.Bold) },
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
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search currency…") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = AccentColor)
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    cursorColor = AccentColor
                )
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredCurrencies, key = { it.code }) { entry ->
                    CurrencyRow(
                        entry = entry,
                        isSelected = (prefs?.currency ?: "USD") == entry.code,
                        onClick = {
                            viewModel.updateCurrency(entry.code)
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
private fun CurrencyRow(
    entry: CurrencyEntry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol badge
        Text(
            text = entry.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AccentColor,
            modifier = Modifier.width(40.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.code,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = entry.country,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AccentColor)
        )
    }
}
