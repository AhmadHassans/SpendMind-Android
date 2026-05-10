package com.spendmindai.app.features.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendmindai.app.core.domain.model.Category
import com.spendmindai.app.shared.ui.theme.AccentColor
import com.spendmindai.app.shared.ui.theme.DarkGrayColor
import kotlinx.coroutines.delay

// ── Emoji data model ──────────────────────────────────────────────────────────

private data class EmojiItem(val emoji: String, val keywords: List<String>)

private enum class EmojiPickerCategory(val label: String) {
    POPULAR("Popular"),
    SMILEYS("Smileys"),
    FOOD("Food"),
    TRAVEL("Travel"),
    ACTIVITIES("Activities"),
    OBJECTS("Objects"),
    SYMBOLS("Symbols")
}

private val EMOJI_BY_CATEGORY: Map<EmojiPickerCategory, List<EmojiItem>> = mapOf(
    EmojiPickerCategory.POPULAR to listOf(
        EmojiItem("🍔", listOf("food", "burger", "eat")),
        EmojiItem("🚗", listOf("car", "transport", "drive")),
        EmojiItem("🛍️", listOf("shopping", "bag", "buy")),
        EmojiItem("💡", listOf("idea", "light", "bills", "electricity")),
        EmojiItem("🏥", listOf("hospital", "health", "medical", "doctor")),
        EmojiItem("🎬", listOf("movie", "film", "entertainment")),
        EmojiItem("📚", listOf("books", "education", "study", "read")),
        EmojiItem("✈️", listOf("travel", "flight", "airplane", "plane")),
        EmojiItem("💪", listOf("fitness", "gym", "workout", "strong")),
        EmojiItem("📱", listOf("phone", "mobile", "app", "subscriptions")),
        EmojiItem("🏠", listOf("home", "house", "rent")),
        EmojiItem("💰", listOf("money", "cash", "finance", "wallet")),
        EmojiItem("🎁", listOf("gift", "present", "birthday")),
        EmojiItem("☕", listOf("coffee", "cafe", "hot", "drink")),
        EmojiItem("🍕", listOf("pizza", "food", "italian")),
        EmojiItem("🎮", listOf("gaming", "video games", "entertainment", "game")),
        EmojiItem("🌮", listOf("taco", "mexican", "food")),
        EmojiItem("💊", listOf("medicine", "pill", "health", "drug")),
        EmojiItem("🏋️", listOf("gym", "weightlifting", "fitness", "workout")),
        EmojiItem("📦", listOf("box", "package", "delivery")),
        EmojiItem("🎵", listOf("music", "song", "note", "audio")),
        EmojiItem("🔑", listOf("key", "access", "unlock", "rent")),
        EmojiItem("🧴", listOf("beauty", "skincare", "cosmetics", "personal care")),
        EmojiItem("🐾", listOf("pet", "animal", "dog", "cat")),
        EmojiItem("🌴", listOf("vacation", "holiday", "beach", "travel")),
        EmojiItem("💸", listOf("money", "expense", "spend", "cash", "flying")),
        EmojiItem("🎓", listOf("education", "graduation", "school", "university")),
        EmojiItem("👔", listOf("clothing", "shirt", "formal", "work")),
        EmojiItem("🧳", listOf("luggage", "travel", "suitcase", "trip")),
        EmojiItem("🛒", listOf("shopping", "grocery", "cart", "supermarket"))
    ),
    EmojiPickerCategory.SMILEYS to listOf(
        EmojiItem("😀", listOf("smile", "happy", "grin", "joy")),
        EmojiItem("😂", listOf("laugh", "cry", "funny", "lol")),
        EmojiItem("😍", listOf("heart eyes", "love", "adore", "beautiful")),
        EmojiItem("🥰", listOf("love", "heart", "affection", "cute")),
        EmojiItem("😎", listOf("cool", "sunglasses", "awesome", "chill")),
        EmojiItem("🤩", listOf("excited", "star", "wow", "amazing")),
        EmojiItem("😴", listOf("sleep", "tired", "rest", "zzz")),
        EmojiItem("😡", listOf("angry", "mad", "rage", "fury")),
        EmojiItem("😢", listOf("sad", "cry", "unhappy", "tears")),
        EmojiItem("🤔", listOf("think", "ponder", "hmm", "wonder")),
        EmojiItem("🤑", listOf("money", "rich", "cash", "greedy")),
        EmojiItem("😷", listOf("sick", "mask", "ill", "health")),
        EmojiItem("🥳", listOf("party", "celebrate", "birthday", "fun")),
        EmojiItem("😤", listOf("frustrated", "annoyed", "steam")),
        EmojiItem("🤗", listOf("hug", "warm", "friend", "embrace")),
        EmojiItem("🥺", listOf("puppy eyes", "plead", "sad", "please")),
        EmojiItem("😬", listOf("awkward", "nervous", "cringe")),
        EmojiItem("🤭", listOf("surprised", "giggle", "hehe", "oops")),
        EmojiItem("😇", listOf("angel", "innocent", "halo", "good")),
        EmojiItem("🤠", listOf("cowboy", "western", "yeehaw"))
    ),
    EmojiPickerCategory.FOOD to listOf(
        EmojiItem("🍔", listOf("burger", "hamburger", "food", "fast food")),
        EmojiItem("🍕", listOf("pizza", "food", "italian", "cheese")),
        EmojiItem("🌮", listOf("taco", "mexican", "food", "wrap")),
        EmojiItem("🍜", listOf("noodles", "ramen", "soup", "asian", "noodle")),
        EmojiItem("🍣", listOf("sushi", "japanese", "fish", "food")),
        EmojiItem("🍩", listOf("donut", "dessert", "sweet", "doughnut")),
        EmojiItem("🎂", listOf("cake", "birthday", "dessert", "celebrate")),
        EmojiItem("🍎", listOf("apple", "fruit", "healthy", "red")),
        EmojiItem("🥗", listOf("salad", "healthy", "vegetables", "greens")),
        EmojiItem("🍺", listOf("beer", "drink", "alcohol", "pub", "bar")),
        EmojiItem("☕", listOf("coffee", "cafe", "hot", "drink", "espresso", "latte")),
        EmojiItem("🍰", listOf("cake", "dessert", "slice", "sweet")),
        EmojiItem("🥤", listOf("drink", "soda", "juice", "smoothie", "cup")),
        EmojiItem("🍱", listOf("bento", "lunch", "box", "japanese")),
        EmojiItem("🍷", listOf("wine", "drink", "alcohol", "dinner", "red wine")),
        EmojiItem("🥂", listOf("champagne", "celebrate", "toast", "cheers")),
        EmojiItem("🌯", listOf("wrap", "sandwich", "food", "burrito")),
        EmojiItem("🍿", listOf("popcorn", "movie", "snack", "cinema")),
        EmojiItem("🧋", listOf("bubble tea", "boba", "drink", "tea")),
        EmojiItem("🍫", listOf("chocolate", "sweet", "dessert", "candy")),
        EmojiItem("🍪", listOf("cookie", "sweet", "dessert", "bake", "biscuit")),
        EmojiItem("🥐", listOf("croissant", "breakfast", "bread", "bakery", "french")),
        EmojiItem("🍗", listOf("chicken", "food", "meat", "drumstick")),
        EmojiItem("🥩", listOf("meat", "steak", "food", "beef")),
        EmojiItem("🍦", listOf("ice cream", "dessert", "cold", "sweet", "summer")),
        EmojiItem("🧃", listOf("juice", "drink", "box", "fruit")),
        EmojiItem("🥞", listOf("pancakes", "breakfast", "syrup", "brunch")),
        EmojiItem("🍟", listOf("fries", "chips", "fast food", "potato")),
        EmojiItem("🌽", listOf("corn", "vegetable", "food", "maize")),
        EmojiItem("🥑", listOf("avocado", "healthy", "food", "green"))
    ),
    EmojiPickerCategory.TRAVEL to listOf(
        EmojiItem("🚗", listOf("car", "drive", "transport", "vehicle", "auto")),
        EmojiItem("✈️", listOf("plane", "airplane", "flight", "travel", "fly")),
        EmojiItem("🏠", listOf("home", "house", "rent", "property")),
        EmojiItem("🏖️", listOf("beach", "vacation", "holiday", "summer", "sand")),
        EmojiItem("⛺", listOf("camping", "tent", "outdoor", "camp", "nature")),
        EmojiItem("🚂", listOf("train", "transport", "rail", "subway", "metro")),
        EmojiItem("🚢", listOf("ship", "boat", "cruise", "travel", "ocean")),
        EmojiItem("🏔️", listOf("mountain", "hike", "outdoor", "peak", "climb")),
        EmojiItem("🛻", listOf("truck", "pickup", "transport", "vehicle")),
        EmojiItem("🚕", listOf("taxi", "cab", "transport", "uber", "lyft")),
        EmojiItem("🛵", listOf("scooter", "moped", "transport", "motor")),
        EmojiItem("🚲", listOf("bike", "bicycle", "cycling", "cycle")),
        EmojiItem("🏨", listOf("hotel", "stay", "travel", "accommodation", "room")),
        EmojiItem("🚀", listOf("rocket", "space", "launch", "fast")),
        EmojiItem("⛽", listOf("gas", "fuel", "petrol", "station", "fill")),
        EmojiItem("🏦", listOf("bank", "finance", "building", "atm")),
        EmojiItem("🗺️", listOf("map", "travel", "navigate", "direction")),
        EmojiItem("🏕️", listOf("camp", "outdoor", "nature", "tent", "forest")),
        EmojiItem("🚁", listOf("helicopter", "transport", "air", "fly")),
        EmojiItem("🛳️", listOf("cruise", "ship", "travel", "ocean", "sea")),
        EmojiItem("🚌", listOf("bus", "transport", "public", "commute")),
        EmojiItem("🛺", listOf("rickshaw", "transport", "tuk tuk")),
        EmojiItem("🏙️", listOf("city", "urban", "skyline", "buildings")),
        EmojiItem("🌍", listOf("world", "earth", "global", "travel", "international"))
    ),
    EmojiPickerCategory.ACTIVITIES to listOf(
        EmojiItem("💪", listOf("fitness", "gym", "strong", "workout", "muscle")),
        EmojiItem("🏋️", listOf("gym", "weightlifting", "fitness", "workout", "lift")),
        EmojiItem("⚽", listOf("soccer", "football", "sport", "ball")),
        EmojiItem("🎮", listOf("gaming", "video games", "entertainment", "play", "game")),
        EmojiItem("🎵", listOf("music", "song", "note", "audio", "melody")),
        EmojiItem("🎨", listOf("art", "paint", "creative", "draw", "design")),
        EmojiItem("🎬", listOf("movie", "film", "entertainment", "cinema", "video")),
        EmojiItem("💃", listOf("dance", "party", "entertainment", "music")),
        EmojiItem("🏊", listOf("swim", "pool", "sport", "water")),
        EmojiItem("🎯", listOf("target", "goal", "dart", "aim", "bullseye")),
        EmojiItem("🏆", listOf("trophy", "win", "award", "achievement", "winner")),
        EmojiItem("🎸", listOf("guitar", "music", "instrument", "rock")),
        EmojiItem("🧘", listOf("yoga", "meditation", "relax", "wellness", "zen")),
        EmojiItem("🏄", listOf("surf", "beach", "sport", "wave", "ocean")),
        EmojiItem("🎤", listOf("microphone", "sing", "music", "karaoke", "vocal")),
        EmojiItem("🏀", listOf("basketball", "sport", "ball", "nba")),
        EmojiItem("🎻", listOf("violin", "music", "instrument", "classical")),
        EmojiItem("🤸", listOf("gymnastics", "acrobat", "fitness", "flexible")),
        EmojiItem("🎲", listOf("dice", "game", "board game", "chance")),
        EmojiItem("📸", listOf("photography", "photo", "camera", "picture", "hobby"))
    ),
    EmojiPickerCategory.OBJECTS to listOf(
        EmojiItem("📱", listOf("phone", "mobile", "smartphone", "app", "device")),
        EmojiItem("💻", listOf("laptop", "computer", "work", "tech", "pc")),
        EmojiItem("📷", listOf("camera", "photo", "picture", "photography")),
        EmojiItem("💰", listOf("money", "cash", "finance", "wallet", "savings")),
        EmojiItem("🔑", listOf("key", "access", "unlock", "rent", "door")),
        EmojiItem("🎁", listOf("gift", "present", "birthday", "surprise")),
        EmojiItem("📦", listOf("box", "package", "delivery", "shipping")),
        EmojiItem("🏥", listOf("hospital", "medical", "health", "doctor", "clinic")),
        EmojiItem("💡", listOf("idea", "light", "electricity", "bills", "bulb")),
        EmojiItem("🔧", listOf("tool", "fix", "repair", "maintenance", "wrench")),
        EmojiItem("⚙️", listOf("settings", "gear", "engineering", "mechanical")),
        EmojiItem("📺", listOf("tv", "television", "entertainment", "screen")),
        EmojiItem("🎒", listOf("backpack", "bag", "school", "travel", "carry")),
        EmojiItem("📚", listOf("books", "study", "education", "read", "library")),
        EmojiItem("💎", listOf("diamond", "luxury", "gem", "valuable", "jewel")),
        EmojiItem("👑", listOf("crown", "royal", "premium", "king", "queen")),
        EmojiItem("🧴", listOf("beauty", "skincare", "cosmetics", "lotion")),
        EmojiItem("🖥️", listOf("computer", "desktop", "monitor", "screen", "pc")),
        EmojiItem("🧳", listOf("luggage", "travel", "suitcase", "trip", "bag")),
        EmojiItem("💊", listOf("medicine", "pill", "health", "drug", "tablet")),
        EmojiItem("🔌", listOf("plug", "electricity", "power", "charge", "bills")),
        EmojiItem("🛁", listOf("bath", "bathroom", "hygiene", "relax", "home")),
        EmojiItem("🛒", listOf("shopping", "grocery", "cart", "supermarket", "store")),
        EmojiItem("🧹", listOf("cleaning", "broom", "sweep", "home", "chores")),
        EmojiItem("🪴", listOf("plant", "indoor", "garden", "nature", "home"))
    ),
    EmojiPickerCategory.SYMBOLS to listOf(
        EmojiItem("❤️", listOf("heart", "love", "favorite", "red", "care")),
        EmojiItem("⭐", listOf("star", "favorite", "rating", "gold")),
        EmojiItem("🌟", listOf("star", "shine", "excellent", "glow")),
        EmojiItem("💫", listOf("star", "dizzy", "sparkle", "spin")),
        EmojiItem("🔥", listOf("fire", "hot", "trending", "burn")),
        EmojiItem("✨", listOf("sparkle", "magic", "special", "glitter")),
        EmojiItem("🌈", listOf("rainbow", "colorful", "hope", "bright")),
        EmojiItem("💯", listOf("perfect", "hundred", "excellent", "100")),
        EmojiItem("🌙", listOf("moon", "night", "sleep", "crescent")),
        EmojiItem("☀️", listOf("sun", "day", "energy", "bright", "summer")),
        EmojiItem("🌊", listOf("wave", "ocean", "water", "sea")),
        EmojiItem("🌿", listOf("plant", "nature", "green", "leaf", "environment")),
        EmojiItem("💸", listOf("money", "flying", "expense", "spend", "cash")),
        EmojiItem("🏅", listOf("medal", "award", "achievement", "gold")),
        EmojiItem("🎯", listOf("target", "goal", "aim", "bullseye")),
        EmojiItem("⚡", listOf("lightning", "electric", "fast", "power", "energy")),
        EmojiItem("🌺", listOf("flower", "nature", "beautiful", "bloom")),
        EmojiItem("🍀", listOf("clover", "lucky", "green", "nature")),
        EmojiItem("🎪", listOf("circus", "carnival", "fun", "entertainment")),
        EmojiItem("🔮", listOf("crystal ball", "magic", "predict", "future"))
    )
)

private val ALL_EMOJIS: List<EmojiItem> by lazy {
    EMOJI_BY_CATEGORY.values.flatten().distinctBy { it.emoji }
}

private fun searchEmojis(query: String): List<EmojiItem> {
    val q = query.trim().lowercase()
    if (q.isBlank()) return emptyList()
    return ALL_EMOJIS.filter { item -> item.keywords.any { it.contains(q) } }
}

// ── Color options ─────────────────────────────────────────────────────────────

private val COLOR_OPTIONS = listOf(
    "orange"   to Color(0xFFFF9800),
    "blue"     to Color(0xFF2196F3),
    "red"      to Color(0xFFF44336),
    "yellow"   to Color(0xFFFFEB3B),
    "darkred"  to Color(0xFFB71C1C),
    "green"    to Color(0xFF4CAF50),
    "teal"     to Color(0xFF009688),
    "cyan"     to Color(0xFF00BCD4),
    "lime"     to Color(0xFF8BC34A),
    "purple"   to Color(0xFF9C27B0)
)

// ── Pastel palette (matches iOS CategoryColors) ──────────────────────────────

private val PASTEL_CYCLE = listOf(
    Color(0xFFF3C5C2),
    Color(0xFFFFDFAE),
    Color(0xFFD9D9D9),
    Color(0xFFAED9E6),
    Color(0xFFCDEEDC),
    Color(0xFFD1C7E3),
    Color(0xFFFFE0B2),
    Color(0xFFB3E5FC),
    Color(0xFFDCEDC8),
    Color(0xFFF8BBD0)
)

private fun pastelForCategory(colorName: String, fallbackIndex: Int): Color = when (colorName) {
    "orange"  -> Color(0xFFFFDFAE)
    "blue"    -> Color(0xFFAED9E6)
    "pink"    -> Color(0xFFF3C5C2)
    "yellow"  -> Color(0xFFFFDFAE)
    "red"     -> Color(0xFFF3C5C2)
    "darkred" -> Color(0xFFF3C5C2)
    "purple"  -> Color(0xFFD1C7E3)
    "teal"    -> Color(0xFFCDEEDC)
    "cyan"    -> Color(0xFFB3E5FC)
    "green"   -> Color(0xFFCDEEDC)
    "lime"    -> Color(0xFFDCEDC8)
    "indigo"  -> Color(0xFFD1C7E3)
    else      -> PASTEL_CYCLE[fallbackIndex % PASTEL_CYCLE.size]
}

// ── Bottom-sheet entry point ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onDismiss: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var seenIds     by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(uiState.categories) {
        if (uiState.categories.isEmpty()) return@LaunchedEffect
        val currentIds = uiState.categories.map { it.id }.toSet()
        val newIds     = currentIds - seenIds
        when {
            seenIds.isEmpty() -> {
                selectedIds = uiState.categories.filter { it.isEnabled }.map { it.id }.toSet()
            }
            newIds.isNotEmpty() -> {
                val newEnabled = uiState.categories
                    .filter { it.id in newIds && it.isEnabled }
                    .map { it.id }
                    .toSet()
                selectedIds = selectedIds + newEnabled
            }
        }
        seenIds = currentIds
    }

    val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor   = Color.White,
        dragHandle       = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.87f)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "Manage your categories",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF555555))
                }
            }

            LazyVerticalGrid(
                columns               = GridCells.Fixed(3),
                modifier              = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement   = Arrangement.spacedBy(16.dp),
                contentPadding        = PaddingValues(top = 12.dp, bottom = 16.dp)
            ) {
                itemsIndexed(
                    items = uiState.categories,
                    key   = { _, cat -> cat.id }
                ) { index, category ->
                    val isSelected = category.id in selectedIds
                    CategoryGridCard(
                        category    = category,
                        isSelected  = isSelected,
                        pastelColor = pastelForCategory(category.colorName, index),
                        onClick     = {
                            selectedIds = if (isSelected) selectedIds - category.id
                                         else            selectedIds + category.id
                        }
                    )
                }
                item {
                    AddCategoryGridCard(onClick = { showAddSheet = true })
                }
            }

            Button(
                onClick = {
                    viewModel.saveEnabledStates(selectedIds)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGrayColor),
                shape  = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showAddSheet) {
        AddCategorySheet(
            onDismiss = { showAddSheet = false },
            onSave    = { name, emoji, colorName ->
                viewModel.addCategory(name, emoji, colorName)
                showAddSheet = false
            }
        )
    }
}

// ── Category grid card ────────────────────────────────────────────────────────

@Composable
private fun CategoryGridCard(
    category:    Category,
    isSelected:  Boolean,
    pastelColor: Color,
    onClick:     () -> Unit
) {
    val bgColor = if (isSelected) pastelColor else Color(0xFFE5E5EA)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.emoji, fontSize = 44.sp)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint               = Color(0xFF222222),
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text       = category.name,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isSelected) MaterialTheme.colorScheme.onBackground
                         else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign  = TextAlign.Center,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

// ── Add category card (dashed border) ────────────────────────────────────────

@Composable
private fun AddCategoryGridCard(onClick: () -> Unit) {
    val dashColor = Color(0xFFAAAAAA)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(20.dp))
                .drawBehind {
                    drawRoundRect(
                        color        = dashColor,
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        style        = Stroke(
                            width      = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add category", tint = Color(0xFF888888), modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Add", style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888), textAlign = TextAlign.Center)
    }
}

// ── Add category bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategorySheet(
    onDismiss: () -> Unit,
    onSave:    (name: String, emoji: String, colorName: String) -> Unit
) {
    val maxChars = 30
    var name          by rememberSaveable { mutableStateOf("") }
    var selectedEmoji by rememberSaveable { mutableStateOf("") }
    var selectedColor by rememberSaveable { mutableStateOf(COLOR_OPTIONS.first().first) }
    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor   = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text       = "New Category",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            // Large emoji display circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF2F2F7))
                    .clickable { showEmojiPicker = true },
                contentAlignment = Alignment.Center
            ) {
                if (selectedEmoji.isBlank()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🙂", fontSize = 32.sp)
                        Text(
                            text  = "Tap to select",
                            fontSize = 10.sp,
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(selectedEmoji, fontSize = 48.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Category name field with character counter
            OutlinedTextField(
                value         = name,
                onValueChange = { if (it.length <= maxChars) name = it },
                label         = { Text("Category Name") },
                trailingIcon  = {
                    Text(
                        text  = "${name.length}/$maxChars",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (name.length >= maxChars) Color(0xFFE5534B) else Color(0xFF8E8E93),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape         = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Icon section
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "Icon",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text      = "Browse all →",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = AccentColor,
                    modifier  = Modifier.clickable { showEmojiPicker = true }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EMOJI_BY_CATEGORY[EmojiPickerCategory.POPULAR]!!.take(20).forEach { item ->
                    val isSelected = item.emoji == selectedEmoji
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) AccentColor.copy(alpha = 0.12f)
                                else Color(0xFFF2F2F7)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) AccentColor else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedEmoji = item.emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Color section
            Text(
                text       = "Color",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                COLOR_OPTIONS.forEach { (colorName, color) ->
                    val isSelected = colorName == selectedColor
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color(0xFF1C1C1E) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorName },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector        = Icons.Filled.Check,
                                contentDescription = null,
                                tint               = Color.White,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Save button
            val canSave = name.isNotBlank()
            Button(
                onClick = {
                    val emoji = if (selectedEmoji.isBlank()) "📝" else selectedEmoji
                    onSave(name.trim(), emoji, selectedColor)
                },
                enabled  = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = AccentColor,
                    disabledContainerColor = Color(0xFFD1D1D6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text  = "Save Category",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }

        LaunchedEffect(Unit) {
            delay(400)
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    if (showEmojiPicker) {
        EmojiPickerSheet(
            onDismiss      = { showEmojiPicker = false },
            onEmojiSelected = { emoji ->
                selectedEmoji   = emoji
                showEmojiPicker = false
            }
        )
    }
}

// ── Emoji picker bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiPickerSheet(
    onDismiss:       () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    var searchQuery       by remember { mutableStateOf("") }
    var selectedCategory  by remember { mutableStateOf(EmojiPickerCategory.POPULAR) }

    val displayedEmojis by remember {
        derivedStateOf {
            if (searchQuery.isNotBlank()) searchEmojis(searchQuery)
            else EMOJI_BY_CATEGORY[selectedCategory] ?: emptyList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor   = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Title
            Text(
                text       = "Choose Emoji",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Search bar
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = { Text("Search emoji…", color = Color(0xFF8E8E93)) },
                leadingIcon   = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF8E8E93))
                },
                trailingIcon  = if (searchQuery.isNotEmpty()) ({
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }) else null,
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape         = RoundedCornerShape(12.dp),
                colors        = TextFieldDefaults.colors(
                    focusedContainerColor   = Color(0xFFF2F2F7),
                    unfocusedContainerColor = Color(0xFFF2F2F7),
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(8.dp))

            // Category chips — hidden during search
            if (searchQuery.isBlank()) {
                LazyRow(
                    modifier            = Modifier.fillMaxWidth(),
                    contentPadding      = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(EmojiPickerCategory.values()) { category ->
                        FilterChip(
                            selected  = category == selectedCategory,
                            onClick   = { selectedCategory = category },
                            label     = { Text(category.label, style = MaterialTheme.typography.labelMedium) },
                            colors    = FilterChipDefaults.filterChipColors(
                                selectedContainerColor    = AccentColor,
                                selectedLabelColor        = Color.White,
                                containerColor            = Color(0xFFF2F2F7),
                                labelColor                = Color(0xFF333333)
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Emoji grid
            if (displayedEmojis.isEmpty() && searchQuery.isNotBlank()) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = "No results for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns               = GridCells.Fixed(7),
                    modifier              = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp),
                    contentPadding        = PaddingValues(vertical = 4.dp)
                ) {
                    items(displayedEmojis, key = { it.emoji }) { item ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEmojiSelected(item.emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text      = item.emoji,
                                fontSize  = 26.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
