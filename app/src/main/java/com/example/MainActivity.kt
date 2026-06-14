package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.data.*
import com.example.ui.CatViewModel
import com.example.ui.MapScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: CatViewModel = viewModel()) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isPremiumActive by viewModel.isPremium.collectAsState()
    
    val allCats by viewModel.allCats.collectAsState()
    val selectedCat by viewModel.selectedCat.collectAsState()

    var activeTab by remember { mutableStateOf("home") }

    // Helper translation fetch wrapper
    val t: (String) -> String = { key -> Translations.get(currentLang, key) }

    // RTL/LTR layout direction setup based on selected language
    val isRtl = currentLang != AppLanguage.EN
    val layoutDirection = if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == "home",
                        onClick = { activeTab = "home" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = t("dashboard")) },
                        label = { Text(t("dashboard"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = activeTab == "encyclopedia",
                        onClick = { activeTab = "encyclopedia" },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = t("encyclopedia")) },
                        label = { Text(t("encyclopedia"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_encyclopedia")
                    )
                    NavigationBarItem(
                        selected = activeTab == "ai_lab",
                        onClick = { activeTab = "ai_lab" },
                        icon = { Icon(Icons.Default.Science, contentDescription = t("ai_lab")) },
                        label = { Text(t("ai_lab"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_ailab")
                    )
                    NavigationBarItem(
                        selected = activeTab == "assistant",
                        onClick = { activeTab = "assistant" },
                        icon = { Icon(Icons.Default.KeyboardVoice, contentDescription = t("assistant")) },
                        label = { Text(t("assistant"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_assistant")
                    )
                    NavigationBarItem(
                        selected = activeTab == "map",
                        onClick = { activeTab = "map" },
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = t("map")) },
                        label = { Text(t("map"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_map")
                    )
                    NavigationBarItem(
                        selected = activeTab == "premium",
                        onClick = { activeTab = "premium" },
                        icon = { 
                            Icon(
                                Icons.Default.Stars, 
                                contentDescription = t("premium"),
                                tint = if (isPremiumActive) Color(0xFFFFD700) else LocalContentColor.current
                            ) 
                        },
                        label = { Text(t("premium"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_premium")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        "home" -> HomeScreen(viewModel, t, isRtl)
                        "encyclopedia" -> EncyclopediaScreen(viewModel, t)
                        "ai_lab" -> AiLabScreen(viewModel, t)
                        "assistant" -> AssistantChatScreen(viewModel, t)
                        "map" -> MapScreen(viewModel, t)
                        "premium" -> PremiumPaywallScreen(viewModel, t)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HOMESCREEN - CAT METRICS & CHECKLISTS
// -------------------------------------------------------------
@Composable
fun HomeScreen(viewModel: CatViewModel, t: (String) -> String, isRtl: Boolean) {
    val allCats by viewModel.allCats.collectAsState()
    val selectedCat by viewModel.selectedCat.collectAsState()
    val medicalRecords by viewModel.medicalRecords.collectAsState()
    val growthRecords by viewModel.growthRecords.collectAsState()
    val dailyLog by viewModel.currentDailyLog.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val reproductionRecords by viewModel.reproductionRecords.collectAsState()
    val isPremiumActive by viewModel.isPremium.collectAsState()

    var showAddCatDialog by remember { mutableStateOf(false) }
    var showAddMedicalDialog by remember { mutableStateOf(false) }
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var showAddReproductionDialog by remember { mutableStateOf(false) }

    // Floating alert for user if profile limits reached on Free option
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // App header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = t("app_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Yasham Feline Ecosystem",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPremiumActive) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "GOLD PRO",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37)
                            )
                        }
                    } else {
                        Text(
                            t("free_trial"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Horizontal Cat selection row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = t("profile"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allCats) { cat ->
                            val isSelected = cat.id == selectedCat?.id
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.selectCat(cat) }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = cat.name,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = cat.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                                    )
                                }
                            }
                        }

                        // Register button
                        item {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                        if (allCats.size >= 1 && !isPremiumActive) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "محدودیت نسخه رایگان: افزودن بیش از یک گربه نیاز به نسخه طلایی دارد.",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()
                                        } else {
                                            showAddCatDialog = true
                                        }
                                    }
                                    .testTag("add_cat_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = t("add_cat"),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Cat Details Banner
        selectedCat?.let { cat ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${t("breed")}: ${cat.breed}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteSelectedCat() },
                                modifier = Modifier.testTag("delete_cat_profile")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "حذف گربه",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val cardBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            listOf(
                                Pair(t("gender"), if (cat.gender == "male") t("male") else t("female")),
                                Pair(t("age"), "${cat.ageMonths} ${t("age").substringAfter("(").substringBefore(")")}"),
                                Pair(t("weight"), "${cat.weight} kg"),
                                Pair(t("color"), cat.color)
                            ).forEach { (label, value) ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = cardBg)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "میکروچیپ: ${cat.microchip.ifEmpty { "ثبت نشده" }} | ${t("neutered")}: ${if (cat.isNeutered) t("yes") else t("no")}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // DAILY WORKBOARD REMINDERS & WATER MONITOR
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Care Checklist Reminders
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = t("reminders"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "چک‌لیست مراقبت‌های فعال امروز گربه‌ی شما",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Clean Reminder rows
                            val indicators = listOf(
                                Triple(Icons.Default.Restaurant, t("reminder_food"), dailyLog?.dryFoodGrams ?: 0 > 0),
                                Triple(Icons.Default.LocalDrink, t("reminder_water"), dailyLog?.waterIntakeMl ?: 0 >= 100),
                                Triple(Icons.Default.CleanHands, t("reminder_litter"), dailyLog?.litterBoxCleanCount ?: 0 > 0),
                                Triple(Icons.Default.ContentCut, t("reminder_groom"), false), // static simulation for beauty
                                Triple(Icons.Default.MedicalServices, t("medical_records"), medicalRecords.isNotEmpty())
                            )

                            indicators.forEach { (icon, text, done) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(icon, contentDescription = null, tint = if (done) Color(0xFF4CAF50) else Color.Gray, modifier = Modifier.size(20.dp))
                                        Text(text, fontSize = 12.sp, color = if (done) Color.Gray else Color.Black)
                                    }
                                    Checkbox(
                                        checked = done,
                                        onCheckedChange = {
                                            if (icon == Icons.Default.CleanHands) {
                                                viewModel.updateSelfCheckreminders(litterCount = 1)
                                            } else {
                                                Toast.makeText(context, "برای تکمیل این کار، فیلدهای جزئیات تغذیه یا آب را در زیر وارد کنید.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Water Hydrator widget
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Circular Wave indicator
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD))
                                    .border(2.dp, Color(0xFF2196F3), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.LocalDrink, contentDescription = null, tint = Color(0xFF2196F3))
                                    Text("${dailyLog?.waterIntakeMl ?: 0}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                    Text("ml", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Hydration info + inputs
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t("water_monitor"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("هدف روزانه: ۲۵۰ میلی‌لیتر (پیشنهاد یاشام برای وزن ${cat.weight} کیلو)", fontSize = 10.sp, color = Color.Gray)
                                
                                val pct = ((dailyLog?.waterIntakeMl ?: 0).toFloat() / 250f).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFF2196F3),
                                    trackColor = Color(0xFFD1E8FC)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.logWaterIntake(50) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("add_50ml_water")
                                    ) {
                                        Text("+50ml", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.logWaterIntake(100) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("add_100ml_water")
                                    ) {
                                        Text("+100ml", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Food Feed station
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(t("nutrition"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("ثبت وعده‌های غذایی گربه", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("غذای خشک", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("${dailyLog?.dryFoodGrams ?: 0} گرم", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Button(onClick = { viewModel.logFeeding(30, 0, 0) }, modifier = Modifier.padding(top = 8.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                                            Text("+30g", fontSize = 10.sp)
                                        }
                                    }
                                }
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("غذای مرطوب/کنسرو", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("${dailyLog?.wetFoodGrams ?: 0} گرم", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Button(onClick = { viewModel.logFeeding(0, 50, 0) }, modifier = Modifier.padding(top = 8.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                                            Text("+50g", fontSize = 10.sp)
                                        }
                                    }
                                }
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("تشویقی", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("${dailyLog?.treatsCount ?: 0} عدد", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Button(onClick = { viewModel.logFeeding(0, 0, 1) }, modifier = Modifier.padding(top = 8.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                                            Text("+1 عدد", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // GROWTH WEIGHT TRACKER GRAPH (Using dynamic Custom Canvas rendering!)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(t("weight_chart"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("نمودار تغییرات وزن در طول زمان", fontSize = 10.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = { showAddWeightDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("+ ${t("weight")}", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Draw a stunning minimalist coordinate axis and connection lines with Canvas!
                            if (growthRecords.isNotEmpty()) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .padding(8.dp)
                                ) {
                                    val canvasW = size.width
                                    val canvasH = size.height

                                    // Find min/max values safely
                                    val weights = growthRecords.map { it.weight }
                                    val maxVal = (weights.maxOrNull() ?: 10f).coerceAtLeast(1f)
                                    val minVal = (weights.minOrNull() ?: 0f).coerceAtLeast(0f)
                                    val range = (maxVal - minVal).coerceAtLeast(1f)

                                    val count = growthRecords.size
                                    val spacing = canvasW / (if (count > 1) count - 1 else 1)

                                    val linePaint = Paint().apply {
                                        color = android.graphics.Color.BLUE
                                        strokeWidth = 6f
                                        isAntiAlias = true
                                        style = Paint.Style.STROKE
                                    }
                                    val dotPaint = Paint().apply {
                                        color = android.graphics.Color.RED
                                        strokeWidth = 14f
                                        isAntiAlias = true
                                        style = Paint.Style.FILL
                                    }
                                    val textPaint = Paint().apply {
                                        color = android.graphics.Color.DKGRAY
                                        textSize = 28f
                                        isAntiAlias = true
                                    }

                                    // Draw coordinate lines
                                    drawContext.canvas.nativeCanvas.drawLine(0f, canvasH, canvasW, canvasH, Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 3f })
                                    drawContext.canvas.nativeCanvas.drawLine(0f, 0f, 0f, canvasH, Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 3f })

                                    // Map coordinates and trace path
                                    var lastX = 0f
                                    var lastY = 0f
                                    for (i in 0 until count) {
                                        val rec = growthRecords[i]
                                        val percentY = if (range > 0) (rec.weight - minVal) / range else 0.5f
                                        val x = i * spacing
                                        val y = canvasH - (percentY * (canvasH - 40f)) - 20f

                                        // Draw marker dot
                                        drawContext.canvas.nativeCanvas.drawCircle(x, y, 10f, dotPaint)
                                        // Draw weight labels
                                        drawContext.canvas.nativeCanvas.drawText("${rec.weight}kg", x + 10f, y - 10f, textPaint)

                                        if (i > 0) {
                                            drawContext.canvas.nativeCanvas.drawLine(lastX, lastY, x, y, linePaint)
                                        }
                                        lastX = x
                                        lastY = y
                                    }
                                }

                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(growthRecords) { rec ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        ) {
                                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text("${rec.weight} kg", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text("قد: ${rec.height} cm", fontSize = 10.sp, color = Color.Gray)
                                                    Text(rec.date, fontSize = 9.sp, color = Color.Gray)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(onClick = { viewModel.deleteGrowthRecord(rec) }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("داده‌ای ثبت نشده است. وزن گربه خود را برای دریافت نمودار رشد وارد کنید.", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // MEDICAL PROFILE & RECORDS REGISTER SHEET
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(t("medical_records"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("واکسیناسیون، ضد انگل، داروها و حوزه‌های پزشکی", fontSize = 10.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = { showAddMedicalDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("+ نسخه", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (medicalRecords.isNotEmpty()) {
                                GridRecordsList(medicalRecords, onDelete = { viewModel.removeMedicalRecord(it) })
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("پرونده خالی است. واکسن و ضد انگل‌های طی شده را ثبت کنید.", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // REPRODUCTION BOOKLET & HEAT TRACKER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(t("reproduction"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("ثبت فحل، جفت‌گیری و دوره‌های بارداری گربه", fontSize = 10.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = { showAddReproductionDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("+ دوره", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (reproductionRecords.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    reproductionRecords.forEach { record ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Icon(
                                                    imageVector = if (record.type == "heat") Icons.Default.Favorite else Icons.Default.ChildCare,
                                                    contentDescription = null,
                                                    tint = Color.Red
                                                )
                                                Column {
                                                    val cleanType = when (record.type) {
                                                        "heat" -> t("repro_heat")
                                                        "mating" -> "جفت‌گیری"
                                                        "pregnancy" -> t("repro_pregnancy")
                                                        else -> "زایمان"
                                                    }
                                                    Text(cleanType, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("تاریخ شروع: ${record.startDate}", fontSize = 10.sp, color = Color.Gray)
                                                    if (record.notes.isNotEmpty()) {
                                                        Text("یادداشت: ${record.notes}", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                            IconButton(onClick = { viewModel.removeReproduction(record) }) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("هیچ دوره‌ی باروری یا فحلی ثبت نشده است.", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // DAILY SICKNESS/BEHAVIOR TRACKER TRENDS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(t("behavior_logger"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("ثبت اختلال‌های رفتاری به لایو آنالیزور یاشام پت", fontSize = 11.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val buttonBorder = BorderStroke(1.dp, Color.LightGray)
                                Button(
                                    onClick = {
                                        viewModel.updateSelfCheckreminders(vomits = 1)
                                        Toast.makeText(context, "یک استفراغ ثبت شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = buttonBorder,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                                ) {
                                    Text("🤮 استفراغ (${dailyLog?.vomitCount ?: 0})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateSelfCheckreminders(diarrhea = 1)
                                        Toast.makeText(context, "یک اسهال ثبت شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = buttonBorder,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                                ) {
                                    Text("💩 اسهال (${dailyLog?.diarrheaCount ?: 0})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val buttonBorder = BorderStroke(1.dp, Color.LightGray)
                                Button(
                                    onClick = {
                                        viewModel.updateSelfCheckreminders(sneezes = 1)
                                        Toast.makeText(context, "عطسه ثبت شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = buttonBorder,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                                ) {
                                    Text("🤧 عطسه/سرفه (${dailyLog?.sneezingCount ?: 0})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateSelfCheckreminders(lethargic = !(dailyLog?.isLethargic ?: false))
                                        Toast.makeText(context, "وضعیت بی‌حالی معکوس شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = buttonBorder,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (dailyLog?.isLethargic == true) Color.Red.copy(alpha = 0.2f) else Color.White,
                                        contentColor = if (dailyLog?.isLethargic == true) Color.Red else Color.Black
                                    )
                                ) {
                                    Text("💤 بی‌حالی (${if (dailyLog?.isLethargic == true) "فعال" else "عادی"})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (recentLogs.any { it.vomitCount > 0 || it.diarrheaCount > 0 || it.isLethargic }) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                                        Column {
                                            Text("تحلیل سلامت هوش مصنوعی:", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 12.sp)
                                            Text("گربه‌ی شما علائم تکراری گوارشی/بی‌حالی را در روزهای اخیر نشان داده. مانیتور آب را جدی بگیرید و در صورت همزمانی بی‌حالی با پزشک تماس بگیرید.", fontSize = 10.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .clickable { showAddCatDialog = true }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(68.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Text(
                            text = "به خانواده پت هوشمند یاشام خوش آمدید!\nابتدا یک پرونده برای گربه خود ایجاد کنید.",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { showAddCatDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("init_cat_create")
                        ) {
                            Text(t("add_cat"))
                        }
                    }
                }
            }

            item {
                TrainingArticles(t)
            }
        }
    }

    // Modal dialog views
    if (showAddCatDialog) {
        AddCatDialog(
            t = t,
            onDismiss = { showAddCatDialog = false },
            onConfirm = { name, breed, gender, age, weight, color, micro, neutered ->
                viewModel.registerCat(name, breed, gender, age, weight, color, micro, neutered, 0)
                showAddCatDialog = false
            }
        )
    }

    if (showAddMedicalDialog) {
        AddMedicalDialog(
            t = t,
            onDismiss = { showAddMedicalDialog = false },
            onConfirm = { type, title, notes, reminder, path, attType, name ->
                viewModel.addMedicalRecord(type, title, notes, reminder, path, attType, name)
                showAddMedicalDialog = false
            }
        )
    }

    if (showAddWeightDialog) {
        AddWeightDialog(
            t = t,
            onDismiss = { showAddWeightDialog = false },
            onConfirm = { weight, height, note ->
                viewModel.addGrowthRecord(weight, height, note)
                showAddWeightDialog = false
            }
        )
    }

    if (showAddReproductionDialog) {
        AddReproductionDialog(
            t = t,
            onDismiss = { showAddReproductionDialog = false },
            onConfirm = { type, start, notes ->
                viewModel.addReproductionRecord(type, start, notes)
                showAddReproductionDialog = false
            }
        )
    }
}

@Composable
fun GridRecordsList(records: List<MedicalRecord>, onDelete: (MedicalRecord) -> Unit) {
    var activeViewAttachment by remember { mutableStateOf<MedicalRecord?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        records.forEach { record ->
            Row(
                modifier = Modifier
                    .fillMaxWidth() // Corrected typo fillModifier to fillMaxWidth
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (record.type) {
                        "vaccine" -> Icons.Default.Vaccines
                        "antiparasite_internal", "antiparasite_external" -> Icons.Default.BugReport
                        "surgery" -> Icons.Default.Healing
                        else -> Icons.Default.MedicalInformation
                    }
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(record.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${record.date} | ${record.notes}", fontSize = 10.sp, color = Color.Gray)
                        
                        if (record.reminderDate.isNotEmpty()) {
                            Text("یادآور مجدد: ${record.reminderDate}", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        // Attachment Pill Indicator
                        if (record.attachmentPath != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                onClick = { activeViewAttachment = record }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val isPdf = record.attachmentType == "pdf"
                                    Icon(
                                        imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = record.attachmentName ?: "مدرک پیوست",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 140.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "مشاهده",
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = { onDelete(record) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (activeViewAttachment != null) {
        AttachmentViewerDialog(record = activeViewAttachment!!, onDismiss = { activeViewAttachment = null })
    }
}

@Composable
fun AttachmentViewerDialog(record: MedicalRecord, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main clinic letterhead
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏥", fontSize = 18.sp)
                    }
                    Column {
                        Text("بیمارستان تخصصی حیوانات خانگی یاشام", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text("سامانه الکترونیکی مدارک پزشکی و پرونده سلامت گربه‌ها", fontSize = 9.sp, color = Color.Gray)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 2.dp)

                // Render File Content
                if (record.attachmentType == "pdf") {
                    // Modern Styled PDF Medical Prescription sheet
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("📄 سند رسمی درمانی (PDF)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                Text("شماره: RX-${record.id + 1045}", fontSize = 9.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("به نام پت: گربه تحت سرپرستی شما (پرونده #${record.catId})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("تاریخ صدور مدرک: ${record.date}", fontSize = 9.sp, color = Color.Black)
                            Text("تشخیص پزشک: ${record.title}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                            Text("اقدامات تجویز شده و درمان:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("• بررسی کامل بالینی و پایش قلبی عروقی قبل از مداخله بالینی.", fontSize = 9.sp, color = Color.DarkGray)
                            Text("• استفاده ویژه از داروهای ضد انگل معتبر برای دفع ایمن انگل‌های معلق روده.", fontSize = 9.sp, color = Color.DarkGray)
                            Text("• یادداشت دامپزشک: ${record.notes.ifEmpty { "وضعیت عمومی پایدار، رعایت رژیم پروتئین بالا الزامی است." }}", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(14.dp))

                            // Custom Red Stamp of Yasham
                            Box(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .border(2.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp))
                                    .padding(6.dp)
                            ) {
                                Text("تأیید اورژانس دکتر فصیحی\nمهر نظام دامپزشکی کشور", fontSize = 8.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    // Photographic clinic x-ray graphic mockup
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("📸 گزارش تصویربرداری کلینیکی (JPEG)", fontSize = 9.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                                Text("دستگاه: X-RAY GE-HEALTH", fontSize = 8.sp, color = Color.Gray)
                            }

                            // Interactive Draw Custom Medical X-Ray Graph inside black canvas!
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawGrid() // Draw green grid markers
                                    // Draw stylized dental bones
                                    val bonePath = Path().apply {
                                        moveTo(size.width * 0.2f, size.height * 0.5f)
                                        lineTo(size.width * 0.8f, size.height * 0.5f)
                                        moveTo(size.width * 0.2f, size.height * 0.3f)
                                        lineTo(size.width * 0.2f, size.height * 0.7f)
                                        moveTo(size.width * 0.8f, size.height * 0.3f)
                                        lineTo(size.width * 0.8f, size.height * 0.7f)
                                    }
                                    drawPath(
                                        path = bonePath,
                                        color = Color.White.copy(alpha = 0.6f),
                                        style = Stroke(width = 12f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )

                                    // Draw glowing scanner signal
                                    drawLine(
                                        color = Color(0x7F4CAF50),
                                        start = Offset(0f, size.height * 0.4f),
                                        end = Offset(size.width, size.height * 0.6f),
                                        strokeWidth = 4f
                                    )
                                }

                                Text(
                                    text = "YASHAM CATS X-RAY #${100 + record.id}\n[نمای فک متراکم پیشیک]",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "توضیح گراف ارسالی: بررسی گرافی ستون معین و تراکم کلسیمی دندان‌ها. ${record.title}.",
                                fontSize = 9.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بستن مدرک بالینی")
                }
            }
        }
    }
}

// Canvas extension for x-ray grid drawing
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid() {
    val step = 30f
    var x = 0f
    while (x < size.width) {
        drawLine(Color(0x224CAF50), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(Color(0x224CAF50), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

@Composable
fun TrainingArticles(t: (String) -> String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("بخش سیزدهم: مقالات آموزشی یاشام پت", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        
        listOf(
            Triple("بهترین رژیم غذایی بچه گربه", "آموزش روش‌های اصولی تغذیه در ۳ ماه نخست تولد گربه.", Icons.Default.MenuBook),
            Triple("تفسیر تکان دادن دم گربه‌ها", "پیدا کردن افکار و هیجان‌های احتمالی گربه برحسب زبان بدن.", Icons.Default.Language),
            Triple("چگونه گربه سن بالا را شاد نگه داریم؟", "بازیابی فعال توان بیولوژیک و حرکتی در سالخوردگی.", Icons.Default.FamilyRestroom)
        ).forEach { (title, desc, icon) ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(desc, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Helper modifiers
fun Modifier.fillModifier() = this.fillMaxWidth()

// -------------------------------------------------------------
// ENCYCLOPEDIA SCREEN
// -------------------------------------------------------------
@Composable
fun EncyclopediaScreen(viewModel: CatViewModel, t: (String) -> String) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredBreeds = remember(searchQuery, currentLang) {
        BreedEncyclopedia.searchBreeds(searchQuery, currentLang)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(t("encyclopedia"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("دانشنامه تخصصی نژادی یاشام با بیش از ۸۰ نژاد جهانی", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("encyclopedia_search_input"),
            placeholder = { Text(t("breed_search_placeholder")) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredBreeds) { breed ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = breed.names[currentLang] ?: breed.englishName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${breed.englishName} | ${breed.scientificName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Details Table
                            Text(t("history") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(breed.history[currentLang] ?: "فاقد اطلاعات فارسی", fontSize = 11.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(t("personality") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(breed.personality[currentLang] ?: "فاقد اطلاعات فارسی", fontSize = 11.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(t("care") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(breed.feedingCare[currentLang] ?: "فاقد اطلاعات فارسی", fontSize = 11.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(t("diseases") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(breed.geneticDiseases[currentLang] ?: "فاقد اطلاعات فارسی", fontSize = 11.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(12.dp))
                            // Physical traits block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val traits = listOf(
                                    Pair(t("country_origin"), breed.countryOfOrigin[currentLang] ?: "جهان"),
                                    Pair(t("weight"), breed.typicalWeight),
                                    Pair(t("lifespan"), breed.lifespan)
                                )

                                traits.forEach { (lbl, valStr) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(lbl, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(valStr, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredBreeds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("نژادی با فیلتر جستجو یافت نشد.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// AI LAB SCREEN - SCANNERS & CLASSIFIERS
// -------------------------------------------------------------
@Composable
fun AiLabScreen(viewModel: CatViewModel, t: (String) -> String) {
    val aiResult by viewModel.aiResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val isPremiumActive by viewModel.isPremium.collectAsState()
    val context = LocalContext.current

    var activeLabTab by remember { mutableStateOf("breed") }
    var userTextSymptom by remember { mutableStateOf("") }
    var selectedSymptomArea by remember { mutableStateOf("پوست و مو") }

    // Curated simulation images to let user run scanners instantly
    var simulatedImageIndex by remember { mutableStateOf(0) }
    var simulatedTitle by remember { mutableStateOf("گربه پرشین اصیل") }

    val presetImages = listOf(
        Pair("Persian Cat (گربه پرشین)", "breed"),
        Pair("Watery eye secretion (ترشح چشم گربه)", "disease"),
        Pair("Tasty Tuna Can (کنسرو غذای آماده)", "food"),
        Pair("Red House Lily Flower (گل لیلیوم سمی)", "plant")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(t("ai_lab"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("امکانات ویژه تشخیص نژاد، سلامت، سم‌شناسی و مترجم صوت گربه با Gemini-3.5", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // Horizontal tabs for AI Labs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sections = listOf(
                Pair("breed", t("diagnose_breed")),
                Pair("disease", t("diagnose_disease")),
                Pair("food", t("food_inspector")),
                Pair("plant", t("toxic_plants")),
                Pair("meow", "مترجم میو")
            )
            items(sections) { (id, label) ->
                val isSelected = activeLabTab == id
                FilterChip(
                    selected = isSelected,
                    onClick = { activeLabTab = id },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Main layout of Lab Screen
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isPremiumActive && activeLabTab != "breed") {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("بسته طلایی یاشام پت ویژه", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                            Text("امکانات تشخیص سلامت، تشخیص سم گیاهان، مترجم صوت و هوش مصنوعی بررسی غذا شامل اشتراک ویژه طلایی می‌باشند. در حال حاضر نسخه سه روز آزمایشی امکان دسترسی سریع را می‌دهد.", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.DarkGray)
                            Button(
                                onClick = { viewModel.isPremium.value = true }, // activate trial fast
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("فعال‌سازی سریع آزمایشی طلایی", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Mock Image Selector to perform visual analysis
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("انتخاب نمونه برای تصویر اسکنر هوش مصنوعی", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("چون در سرور مجازی هستیم، یک عکس گربه یا آیتم زیر را انتخاب کنید تا با Gemini زنده آنالیز شود:", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(presetImages.size) { idx ->
                                val (title, type) = presetImages[idx]
                                FilterChip(
                                    selected = simulatedImageIndex == idx,
                                    onClick = {
                                        simulatedImageIndex = idx
                                        simulatedTitle = title
                                    },
                                    label = { Text(title, fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render Selected sample representation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(Color.DarkGray, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val vector = when (simulatedImageIndex) {
                                    0 -> Icons.Default.Pets
                                    1 -> Icons.Default.MedicalServices
                                    2 -> Icons.Default.Restaurant
                                    else -> Icons.Default.Eco
                                }
                                Icon(vector, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                Text(simulatedTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Form inputs based on selected tab
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (activeLabTab) {
                            "breed" -> {
                                Text("آنالیز نژاد گربه با هوش مصنوعی", fontWeight = FontWeight.Bold)
                                Text("تصویر فنوتیپ گربه‌ی مدنظر را عکاسی کنید تا نژاد و بیماری پیش فرض را کشف کنیم.", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val bitmap = createMockBitmap(128, 128)
                                        viewModel.runBreedIdentifier(bitmap)
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("scan_breed_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("اجرای تحلیل نژاد با هوش مصنوعی 🔎")
                                }
                            }
                            "disease" -> {
                                Text("تشخیص هوشمند بیمار‌ی و علائم گربه", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("اندام هدف:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                val areas = listOf("چشم", "گوش", "دهان و دندان", "پوست و مو", "پنجه", "مدفوع/استفراغ")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(areas) { area ->
                                        FilterChip(selected = selectedSymptomArea == area, onClick = { selectedSymptomArea = area }, label = { Text(area, fontSize = 10.sp) })
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = userTextSymptom,
                                    onValueChange = { userTextSymptom = it },
                                    placeholder = { Text("علائم فیزیکی (سرفه، اسهال، ترشحات) را بنویسید...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val bitmap = createMockBitmap(128, 128)
                                        viewModel.runSymptomDiagnostic(userTextSymptom, selectedSymptomArea, bitmap)
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("scan_disease_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("شروع اسکن کلینیکی گربه 🏥")
                                }
                            }
                            "food" -> {
                                Text("بررسی سلامت غذا و تغذیه مناسب", fontWeight = FontWeight.Bold)
                                Text("عکس کنسرو یا غذای خانگی را نشان دهید تا فاکتور سمیت آن برای گربه‌ها مشخص گردد.", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val bitmap = createMockBitmap(128, 128)
                                        viewModel.runFoodScan(bitmap, simulatedTitle)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("بررسی سلامت غذای گربه 🍗")
                                }
                            }
                            "plant" -> {
                                Text("اسکن و شناخت گیاهان سمی خطرناک", fontWeight = FontWeight.Bold)
                                Text("بسیاری از گیاهان خانگی مانند نیلوفر و پتوس برای گربه‌ها مرگ‌بار هستند. فورا عکس بگیرید.", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val bitmap = createMockBitmap(128, 128)
                                        viewModel.runPlantCheck(simulatedTitle, bitmap)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("بررسی کشندگی گیاه خانگی 🌿")
                                }
                            }
                            "meow" -> {
                                Text("مترجم رفتار و مترجم صدای میو", fontWeight = FontWeight.Bold)
                                Text("یک صدا یا توصیف از حرکت گربه (مثلاً تکان دادن سریع دم) را ارسال کرده تا ترجمه کنیم.", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = userTextSymptom,
                                    onValueChange = { userTextSymptom = it },
                                    placeholder = { Text("مثال: گربه مداوم میومیو می‌کند و دمش را زمین می‌کوبد") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.runMeowTranslator(true, userTextSymptom)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("ترجمه زبان میو گربه 🗣️")
                                }
                            }
                        }
                    }
                }
            }

            // Result console
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t("ai_response"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SelectionContainer {
                            Text(
                                text = aiResult.ifEmpty { "هنوز هیچ آنالیزی ارسال نشده است. دکمه ارزیابی پای کار را فشار دهید تا پاسخ زنده از Gemini دریافت شود." },
                                fontSize = 12.sp,
                                color = if (aiResult.isEmpty()) Color.Gray else Color.Black,
                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl)
                            )
                        }
                    }
                }
            }

            // Cat talk pacifier calming tools
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(t("cat_talk"), fontWeight = FontWeight.Bold)
                        Text("پخش اصوات آرامش‌بخش فرکانس بالا برای گربه‌های مضطرب در زمان سفر و مسافرت یا ترس حمام:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.speakResponse("خر خرخرخرخرخرخر، صدای آرامش بخش گربه مادر")
                                    Toast.makeText(context, "در حال پخش صدای خرخر گربه مادر...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💓 صدای خرخر مادر", fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel.speakResponse("جیک جیک جیک، صدای آواز گنجشک‌ها در جنگل")
                                    Toast.makeText(context, "در حال پخش صدای آواز پرنده...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🐦 آواز پرندگان زنده", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper mock bitmap drawer
fun createMockBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.BLUE
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    return bitmap
}

// -------------------------------------------------------------
// YASHAM VOICE AI ASSISTANT CHAT SCREEN
// -------------------------------------------------------------
@Composable
fun AssistantChatScreen(viewModel: CatViewModel, t: (String) -> String) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val activePersona by viewModel.currentPersona.collectAsState()
    val ttsPlaybackEnabled by viewModel.ttsEnabled.collectAsState()
    val currentSelectedCat by viewModel.selectedCat.collectAsState()

    val context = LocalContext.current
    var userText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(t("assistant"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                
                // TTS Audio output toggle button
                IconButton(onClick = { viewModel.ttsEnabled.value = !ttsPlaybackEnabled }) {
                    Icon(
                        imageVector = if (ttsPlaybackEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "خواندن صوتی پاسخ",
                        tint = if (ttsPlaybackEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
            Text("مشاور پزشکی، مربی و هم‌صحبت صوتی فرزند گربه شما", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // Adjustable AI Personas selector
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("شخصیت دستیار صوتی:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val personas = listOf(
                        Pair("vet_f", "👩 داکتر خانم"),
                        Pair("vet_m", "👨 داکتر آقا"),
                        Pair("trainer", "🧑 مربی گربه"),
                        Pair("grandma", "👵 مادربزرگ"),
                        Pair("child", "🧒 دوست کودک")
                    )
                    items(personas) { (id, title) ->
                        val selected = activePersona == id
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.currentPersona.value = id },
                            label = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // Active chat screen history
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (chatMessages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val welcomeText = currentSelectedCat?.let {
                            "سلام! من دستیار هوشمند یاشام هستم. امروز چطور می‌تونم به نگهداری از ${it.name} دوست‌داشتنی کمک کنم؟"
                        } ?: "سلام! من دستیار صوتی و پزشک همراه و پت رفیق یاشام هستم. سوال دندان‌ پزشکی، واکسیناسیون یا رفتاری گربه‌تان را بپرسید."

                        Text(welcomeText, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = Color.Gray)
                    }
                } else {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(chatMessages.size) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        chatMessages.forEach { (text, isUser) ->
                            ChatBubble(text, isUser)
                        }
                        if (isChatLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))) {
                                    Text("یاشام در حال پاسخ صوتی...", fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Attached Media Thumbnail Preview (rendered right above the input row)
        var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var attachedBitmapName by remember { mutableStateOf<String?>(null) }
        var showMediaChooser by remember { mutableStateOf(false) }
        var showVoiceController by remember { mutableStateOf(false) }

        if (attachedBitmap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Gray)
                    ) {
                        Image(
                            bitmap = attachedBitmap!!.asImageBitmap(),
                            contentDescription = "Pasted media",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(attachedBitmapName ?: "سند ارسالی", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("آماده جهت بررسی توسط هوش مصنوعی یاشام", fontSize = 8.sp, color = Color.Gray)
                    }
                }
                IconButton(onClick = {
                    attachedBitmap = null
                    attachedBitmapName = null
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "حذف فایل", modifier = Modifier.size(14.dp))
                }
            }
        }

        // Input bottom command area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paperclip Attachments Button
            IconButton(
                onClick = { showMediaChooser = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "پیوست فایل/PDF",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Microphone Dictation Recorder Button
            IconButton(
                onClick = { showVoiceController = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = t("assistant") + " صوتی",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            TextField(
                value = userText,
                onValueChange = { userText = it },
                placeholder = { Text(t("type_symptom"), fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text")
                    .height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            FloatingActionButton(
                onClick = {
                    if (userText.isNotEmpty() || attachedBitmap != null) {
                        viewModel.sendChatMessage(userText, attachedBitmap)
                        userText = ""
                        attachedBitmap = null
                        attachedBitmapName = null
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .testTag("send_chat_msg_btn")
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "ارسال")
            }
        }

        // Media Dialog Chooser
        if (showMediaChooser) {
            Dialog(onDismissRequest = { showMediaChooser = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("ارسال عکس یا مدرک PDF به هوش مصنوعی", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("سند مورد نظر خود را جهت اسکن و چت متقابل با دامپزشک یاشام انتخاب کنید:", fontSize = 10.sp, color = Color.Gray)

                        listOf(
                            Triple("📸 عکس‌برداری زنده با دوربین گوشی", "image", "عکس_دوربین_یاشام.jpg"),
                            Triple("🖼️ انتخاب برگه اسکن دندان گربه از گالری", "image", "اسکن_دندان_پیشیک.png"),
                            Triple("📄 بارگذاری پاسخ آزمایش خون کبدی (PDF)", "image", "آزمایش_کبد_گربه.png") // Loaded as bitmap for Gemini visual analysis
                        ).forEach { (label, typeKey, defaultName) ->
                            OutlinedButton(
                                onClick = {
                                    // Generate a sample colored Bitmap to allow testing Gemini vision API without failing
                                    val localBitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(localBitmap)
                                    val paint = Paint()
                                    paint.color = android.graphics.Color.BLUE
                                    canvas.drawRect(0f, 0f, 150f, 150f, paint)
                                    paint.color = android.graphics.Color.WHITE
                                    paint.textSize = 14f
                                    canvas.drawText("REPORT PDF", 20f, 75f, paint)

                                    attachedBitmap = localBitmap
                                    attachedBitmapName = defaultName
                                    showMediaChooser = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Microphone Voice STT Controller Dialog
        if (showVoiceController) {
            var recordingSeconds by remember { mutableStateOf(0) }
            val transition = rememberInfiniteTransition()
            
            // Loop scale for bouncing visual soundwave levels
            val waveScale1 by transition.animateFloat(
                initialValue = 0.4f, targetValue = 1.6f,
                animationSpec = infiniteRepeatable(animation = tween(400, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )
            val waveScale2 by transition.animateFloat(
                initialValue = 1.4f, targetValue = 0.3f,
                animationSpec = infiniteRepeatable(animation = tween(550, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )
            val waveScale3 by transition.animateFloat(
                initialValue = 0.5f, targetValue = 1.3f,
                animationSpec = infiniteRepeatable(animation = tween(280, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            // Dynamic second timer count
            LaunchedEffect(showVoiceController) {
                while (true) {
                    delay(1000)
                    recordingSeconds++
                }
            }

            Dialog(onDismissRequest = { showVoiceController = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("سیستم تشخیص گفتار هوشمند یاشام", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("یاشام پت در حال شنیدن صدای شماست...", fontSize = 10.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Wave animation visualization (3 spectrum bars bouncing)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(50.dp)
                        ) {
                            Box(modifier = Modifier.width(6.dp).height(30.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)).graphicsLayer { scaleY = waveScale1 })
                            Box(modifier = Modifier.width(6.dp).height(45.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp)).graphicsLayer { scaleY = waveScale2 })
                            Box(modifier = Modifier.width(6.dp).height(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)).graphicsLayer { scaleY = waveScale3 })
                            Box(modifier = Modifier.width(6.dp).height(24.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp)).graphicsLayer { scaleY = waveScale1 })
                        }

                        // Elapsed seconds indicator
                        Text("مدت ضبط صدا: ${recordingSeconds / 60}:${String.format("%02d", recordingSeconds % 60)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                        Divider(color = Color.LightGray.copy(alpha = 0.3f))

                        // Frequently spoken cat voice query templates
                        Text("یا از ورودی‌های گفتاری تست زیر استفاده کنید:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        
                        val speechMocks = listOf(
                            "چطور می‌توانم عفونت چشم گربه پرشینم را تمیز کنم؟",
                            "علت اصلی چنگ زدن شدید گربه به خاک گلدان چیست؟",
                            "آیا تشویقی با پروتئین بالا برای نوزادان گربه مضر است؟"
                        )
                        speechMocks.forEach { mockText ->
                            OutlinedCard(
                                onClick = {
                                    userText = mockText
                                    showVoiceController = false
                                    Toast.makeText(context, "صوت شناسایی و تایپ شد!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🎤 \"$mockText\"",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { showVoiceController = false },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "توقف")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("توقف و درج گفتار صوتی")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val align = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val txtColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val radius = if (isUser) RoundedCornerShape(18.dp, 18.dp, 0.dp, 18.dp) else RoundedCornerShape(18.dp, 18.dp, 18.dp, 0.dp)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
        Card(
            shape = radius,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.ContentOrRtl),
                    color = txtColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PREMIUM GOLD PAYWALL SCREEN
// -------------------------------------------------------------
@Composable
fun PremiumPaywallScreen(viewModel: CatViewModel, t: (String) -> String) {
    val isPremiumActive by viewModel.isPremium.collectAsState()
    val chosenLang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current

    var hasPaidZarinpalSim by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column {
                Text(
                    t("premium"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "توسعه و کالیبراسیون حساب مشترک ناتی یاشام پت طلایی",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Active Status
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremiumActive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, if (isPremiumActive) Color(0xFF81C784) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = if (isPremiumActive) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isPremiumActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = if (isPremiumActive) "حساب طلایی شما فعال است" else "نسخه طلایی غیر فعال است",
                            fontWeight = FontWeight.Bold,
                            color = if (isPremiumActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isPremiumActive) "از تمامی ویژگی‌های نامحدود، اسکنرها و چت صوتی بهره‌مند هستید."
                            else "برای فعال‌سازی کامل، پلن‌های مقرون به صرفه زیر را خریداری کنید.",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Humanitarian & Charity Cause Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "❤️",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "سهم مهر و نوعدوستی در یاشام پت",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Text(
                        text = "همراه گرامی و مهربان، بیایید با هم جهانی گرم‌تر و زیباتر برای فرشته‌های کوچک و بی‌پناه زمین بسازیم. با خرید هر یک از اشتراک‌های ویژه یاشام پت، ۲۰ درصد از درآمد حاصله مستقیماً و با شفافیت کامل صرف درمان، جراحی، تهیه غذا، و مراقبت ویژه از حیوانات بیمار، معلول و بی‌سرپرست آسیب‌دیده خواهد شد. حمایت پرمهر شما، مرهمی التیام‌بخش بر تن رنجور این زبان‌بستگان معصوم است و نویدبخش حیاتی نو برای آنان. با هم به بهتر شدن و شفابخشی دنیای زیبای حیوانات کمک کنیم.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                        textAlign = TextAlign.Justify
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🤝 ۲۰٪ از پرداختی شما = زندگی شادتر برای حیوانات معلول و بیمار",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Modular Iranian banking structure configuration for developers (زرین پال)
        item {
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(t("pay_modular"), fontWeight = FontWeight.Bold)
                    Text("تنظیم اطلاعات اتصال به درگاه پرداختی زرین‌پال در کلاس ModularBilling:", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Merchant ID Config: YES\nAPI Route Validation: Verified\nCallback URL: https://yasham.ai/payment/status", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Language Switcher (بخش پانزدهم / زبان‌ها)
        item {
            Card {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(t("select_language"), fontWeight = FontWeight.Bold)
                    Text("انتخاب زبان زنده کل اپلیکیشن (رابط کاربری و چت صوتی):", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.currentLanguage.value = lang }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lang.displayName, fontWeight = if (chosenLang == lang) FontWeight.Bold else FontWeight.Normal)
                            RadioButton(selected = chosenLang == lang, onClick = { viewModel.currentLanguage.value = lang })
                        }
                    }
                }
            }
        }

        // Tiers pricing packages
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("پلن‌های اشتراکی ویژه یاشام پت:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                val plans = listOf(
                    Pair("monthly", t("monthly")),
                    Pair("3month", t("three_month")),
                    Pair("6month", t("six_month")),
                    Pair("annual", t("annual"))
                )

                plans.forEach { (planId, desc) ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(desc, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("اتصال ماژول با درگاه‌های ملی بانکی شبکه‌ای", fontSize = 10.sp, color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    viewModel.isPremium.value = true
                                    hasPaidZarinpalSim = true
                                    Toast.makeText(context, "${t("pay_btn")}: زرین پال با موفقیت پرداخت شد. حساب کاربری طلایی فعال گردید.", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pay_btn_$planId")
                            ) {
                                Text("خرید سریع", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ADD CAT PROFILE POPUP DIALOG
// -------------------------------------------------------------
@Composable
fun AddCatDialog(
    t: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Float, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("پرشین") }
    var gender by remember { mutableStateOf("male") }
    var age by remember { mutableStateOf("12") }
    var weight by remember { mutableStateOf("4.2") }
    var color by remember { mutableStateOf("سفید") }
    var microchip by remember { mutableStateOf("") }
    var isNeutered by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(t("add_cat"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                TextField(value = name, onValueChange = { name = it }, label = { Text(t("cat_name")) }, modifier = Modifier.fillMaxWidth().testTag("add_cat_name_input"))
                TextField(value = breed, onValueChange = { breed = it }, label = { Text(t("breed")) }, modifier = Modifier.fillMaxWidth().testTag("add_cat_breed_input"))
                
                Text(t("gender") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = gender == "male", onClick = { gender = "male" })
                        Text(t("male"))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = gender == "female", onClick = { gender = "female" })
                        Text(t("female"))
                    }
                }

                TextField(value = age, onValueChange = { age = it }, label = { Text(t("age")) }, modifier = Modifier.fillMaxWidth())
                TextField(value = weight, onValueChange = { weight = it }, label = { Text(t("weight")) }, modifier = Modifier.fillMaxWidth())
                TextField(value = color, onValueChange = { color = it }, label = { Text(t("color")) }, modifier = Modifier.fillMaxWidth())
                TextField(value = microchip, onValueChange = { microchip = it }, label = { Text(t("microchip")) }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNeutered, onCheckedChange = { isNeutered = it })
                    Text(t("neutered"))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(t("cancel"))
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onConfirm(
                                    name,
                                    breed,
                                    gender,
                                    age.toIntOrNull() ?: 12,
                                    weight.toFloatOrNull() ?: 4.2f,
                                    color,
                                    microchip,
                                    isNeutered
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_cat_confirm_btn")
                    ) {
                        Text(t("save"))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ADD MEDICAL RECORD DIALOG
// -------------------------------------------------------------
@Composable
fun AddMedicalDialog(
    t: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String?, String?, String?) -> Unit
) {
    var type by remember { mutableStateOf("vaccine") }
    var title by remember { mutableStateOf("واکسن چندگانه گربه") }
    var notes by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf("2026-09-12") }

    // Attachment States
    var attachmentPath by remember { mutableStateOf<String?>(null) }
    var attachmentType by remember { mutableStateOf<String?>(null) } // "image" or "pdf"
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var showTypeChooser by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(t("medical_records"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                val recordTypes = listOf(
                    Pair("vaccine", t("vaccines")),
                    Pair("antiparasite_internal", "ضد انگل داخلی"),
                    Pair("antiparasite_external", "ضد انگل خارجی"),
                    Pair("disease", "بیماری"),
                    Pair("surgery", "جراحی"),
                    Pair("allergy", "حساسیت")
                )

                recordTypes.forEach { (id, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                type = id
                                title = if (id == "vaccine") "واکسن چندگانه گربه" else "قرص ضد انگل فصلی"
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == id, onClick = {
                            type = id
                            title = if (id == "vaccine") "واکسن چندگانه گربه" else "قرص ضد انگل فصلی"
                        })
                        Text(label, fontSize = 11.sp)
                    }
                }

                TextField(value = title, onValueChange = { title = it }, label = { Text("نوع واکسن یا ضد انگل") }, modifier = Modifier.fillMaxWidth())
                TextField(value = notes, onValueChange = { notes = it }, label = { Text("یادداشت دکتر دامپزشک") }, modifier = Modifier.fillMaxWidth())
                TextField(value = reminderDate, onValueChange = { reminderDate = it }, label = { Text("تاریخ تکرار مجدد برای یادآور") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(6.dp))

                // Attachment UI Panel
                if (attachmentPath != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (attachmentType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(attachmentName ?: "پیوست", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            attachmentPath = null
                            attachmentType = null
                            attachmentName = null
                        }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Cancel, contentDescription = "حذف پیوست", modifier = Modifier.size(14.dp), tint = Color.Gray)
                        }
                    }
                } else {
                    Button(
                        onClick = { showTypeChooser = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("درج عکس با دوربین، گالری یا سند PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(t("cancel"))
                    }
                    Button(onClick = {
                        if (title.isNotEmpty()) {
                            onConfirm(type, title, notes, reminderDate, attachmentPath, attachmentType, attachmentName)
                        }
                    }) {
                        Text(t("save"))
                    }
                }
            }
        }
    }

    if (showTypeChooser) {
        Dialog(onDismissRequest = { showTypeChooser = false }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("انتخاب منبع مدرک سلامتی گربه", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    listOf(
                        Triple("📸 گرفتن عکس فوری با فلاش دوربین", "image", "برگه_نسخه_گرفته_شده_با_دوربین.jpg"),
                        Triple("🖼️ انتخاب کارت واکسیناسیون از گالری", "image", "کارت_واکسیناسیون_گربه.png"),
                        Triple("📄 بارگذاری فایل پزشک معالج (PDF)", "pdf", "گزارش_رادیولوژی_یاشام.pdf")
                    ).forEach { (label, typeKey, defaultName) ->
                        OutlinedButton(
                            onClick = {
                                attachmentType = typeKey
                                attachmentName = defaultName
                                attachmentPath = "simulated_" + typeKey + "_" + System.currentTimeMillis()
                                showTypeChooser = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ADD WEIGHT DIALOG
// -------------------------------------------------------------
@Composable
fun AddWeightDialog(
    t: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (Float, Float, String) -> Unit
) {
    var weight by remember { mutableStateOf("4.5") }
    var height by remember { mutableStateOf("28") }
    var notes by remember { mutableStateOf("رشد عضلانی عالی") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(t("weight_chart"), fontWeight = FontWeight.Bold)

                TextField(value = weight, onValueChange = { weight = it }, label = { Text(t("weight")) }, modifier = Modifier.fillMaxWidth())
                TextField(value = height, onValueChange = { height = it }, label = { Text("قد (سانتی‌متر)") }, modifier = Modifier.fillMaxWidth())
                TextField(value = notes, onValueChange = { notes = it }, label = { Text("یادداشت") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(t("cancel"))
                    }
                    Button(onClick = {
                        val w = weight.toFloatOrNull() ?: 4.5f
                        val h = height.toFloatOrNull() ?: 28f
                        onConfirm(w, h, notes)
                    }, modifier = Modifier.testTag("save_weight_confirm")) {
                        Text(t("save"))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ADD REPRODUCTION TRACK
// -------------------------------------------------------------
@Composable
fun AddReproductionDialog(
    t: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var type by remember { mutableStateOf("heat") }
    var start by remember { mutableStateOf("2026-06-13") }
    var notes by remember { mutableStateOf("آرام بی‌قرار") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(t("reproduction"), fontWeight = FontWeight.Bold)

                val modes = listOf(
                    Pair("heat", t("repro_heat")),
                    Pair("pregnancy", "دوره بارداری فعال")
                )

                modes.forEach { (id, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = id }) {
                        RadioButton(selected = type == id, onClick = { type = id })
                        Text(label)
                    }
                }

                TextField(value = start, onValueChange = { start = it }, label = { Text("تاریخ شروع") }, modifier = Modifier.fillMaxWidth())
                TextField(value = notes, onValueChange = { notes = it }, label = { Text("توصیحات") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(t("cancel"))
                    }
                    Button(onClick = { onConfirm(type, start, notes) }) {
                        Text(t("save"))
                    }
                }
            }
        }
    }
}
