package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppLanguage
import java.util.*
import kotlin.math.sqrt

// Real-world simulated services data structures
data class PetService(
    val id: String,
    val name: String,
    val alternativeNames: Map<AppLanguage, String>,
    val type: String, // "clinic", "shop", "market"
    val rating: Float,
    val address: Map<AppLanguage, String>,
    val phone: String,
    val workingHours: Map<AppLanguage, String>,
    val x: Float, // Coordinates relative to user center at (0,0)
    val y: Float,
    val description: Map<AppLanguage, String>,
    val isCustom: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: CatViewModel, t: (String) -> String) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()

    // Interactive Map state
    var mapScale by remember { mutableStateOf(1.0f) }
    var mapOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") } // "all", "clinic", "shop", "market"
    
    // Detailed list of pet services
    val initialServices = remember {
        listOf(
            PetService(
                id = "yasham_hospital_1",
                name = "بیمارستان دامپزشکی شبانه‌روزی یاشام",
                alternativeNames = mapOf(
                    AppLanguage.FA to "بیمارستان دامپزشکی شبانه‌روزی یاشام",
                    AppLanguage.EN to "Yasham 24/7 Vet Hospital",
                    AppLanguage.AZ to "یاشام ۲۴-ساعات طبی کلینیک",
                    AppLanguage.KU to "نەخۆشخانەی ئاژەڵانی ۲۴ کاتژمێری یاشام",
                    AppLanguage.BAL to "یاشام ۲۴ ساعته کلینیک"
                ),
                type = "clinic",
                rating = 5.0f,
                address = mapOf(
                    AppLanguage.FA to "تهران، میدان آزادی، خیابان دامپزشکی، پلاک ۱۱۰",
                    AppLanguage.EN to "Tehran, Azadi Sq, Dampezeshki St, No 110",
                    AppLanguage.AZ to "تبریز، آزادی خیاوانی، دامپزشکی کوچه‌سی"
                ),
                phone = "021-66009900",
                workingHours = mapOf(
                    AppLanguage.FA to "شبانه‌روزی (۲۴ ساعته)",
                    AppLanguage.EN to "Open 24 Hours / 7 Days"
                ),
                x = 100f,
                y = -150f,
                description = mapOf(
                    AppLanguage.FA to "بزرگترین مرکز تخصصی چشم‌پزشکی، جراحی تخصصی گربه و پایش سلامت نژادی مجهز به آزمایشگاه پیشرفته.",
                    AppLanguage.EN to "Leading specialized feline surgery and diagnosis facility equipped with modern laboratory assets."
                )
            ),
            PetService(
                id = "clinic_shariati",
                name = "کلینیک تخصصی گربه آریا (شمال)",
                alternativeNames = mapOf(
                    AppLanguage.FA to "کلینیک تخصصی گربه آریا (شمال)",
                    AppLanguage.EN to "Arya Cat Specialists Clinic",
                    AppLanguage.AZ to "آریا پیشیک کلینیکی",
                    AppLanguage.KU to "کلینیکی تایبەتی پشیلەی آریا",
                    AppLanguage.BAL to "آریا گربه ی کلینیک"
                ),
                type = "clinic",
                rating = 4.8f,
                address = mapOf(
                    AppLanguage.FA to "تهران، خیابان شریعتی، بالاتر از پل رومی، ساختمان پزشکان پت",
                    AppLanguage.EN to "Tehran, Shariati St, Near Pol-e-Rumi",
                    AppLanguage.AZ to "تبریز، شریعتی خیاوانی، پول-رومی یانیندا"
                ),
                phone = "021-22008811",
                workingHours = mapOf(
                    AppLanguage.FA to "۸ صبح الی ۱۰ شب",
                    AppLanguage.EN to "8:00 AM - 10:00 PM"
                ),
                x = -150f,
                y = 220f,
                description = mapOf(
                    AppLanguage.FA to "ارائه‌دهنده واکسیناسیون رایگان نژادهای حمایتی، خدمات دندان‌پزشکی و جرم‌گیری تخصصی گربه با اولتراسونیک.",
                    AppLanguage.EN to "Offers elite feline vaccinations, professional dental hygiene, and general care counseling."
                )
            ),
            PetService(
                id = "shop_royal_1",
                name = "پت شاپ بزرگ رویال گربه",
                alternativeNames = mapOf(
                    AppLanguage.FA to "پت شاپ بزرگ رویال گربه",
                    AppLanguage.EN to "Royal Cat Premium PetShop",
                    AppLanguage.AZ to "رویال پیشیک پت‌شاپی"
                ),
                type = "shop",
                rating = 4.9f,
                address = mapOf(
                    AppLanguage.FA to "تهران، شهرک غرب، بلوار فرحزادی، پاساژ حیوانات",
                    AppLanguage.EN to "Tehran, Shahrak-e-Gharb, Farahzadi Blvd"
                ),
                phone = "021-88112233",
                workingHours = mapOf(
                    AppLanguage.FA to "۹ صبح الی ۱۱ شب",
                    AppLanguage.EN to "9:00 AM - 11:00 PM"
                ),
                x = -80f,
                y = -90f,
                description = mapOf(
                    AppLanguage.FA to "برترین واردکننده غذاهای اورجینال خشک و مرطوب، درخت گربه، اسباب‌بازی‌های هوشمند تعاملی و ملو بابلر.",
                    AppLanguage.EN to "Premium supplier of healthy natural supplements, high towers, and foreign toys."
                )
            ),
            PetService(
                id = "market_yasham_hub",
                name = "هایپرمارکت و بازار حیوانات یاشام سنتر",
                alternativeNames = mapOf(
                    AppLanguage.FA to "هایپرمارکت و بازار حیوانات یاشام سنتر",
                    AppLanguage.EN to "Yasham Pet Mega-Market Hub",
                    AppLanguage.AZ to "یاشام حیوانلار بیوک مارکتی"
                ),
                type = "market",
                rating = 4.7f,
                address = mapOf(
                    AppLanguage.FA to "تهران، بزرگراه همت غرب، خروجی چیتگر، بازار بزرگ ملزومات پت",
                    AppLanguage.EN to "Tehran, Hemmat Expressway West, Chitger Sector"
                ),
                phone = "021-44998811",
                workingHours = mapOf(
                    AppLanguage.FA to "۱۰ صبح الی ۱۰ شب (حتی روزهای تعطیل)",
                    AppLanguage.EN to "10:00 AM - 10:00 PM (Everyday)"
                ),
                x = 260f,
                y = 120f,
                description = mapOf(
                    AppLanguage.FA to "عرضه مستقیم انواع خاک گربه آنتی‌باکتریال، باکس‌های حمل استاندارد پرواز، کتیبه‌های آموزشی و تشویقی‌های اورجینال.",
                    AppLanguage.EN to "Megastore providing domestic and imported litter, travel kits, airline-safe cages, and treats."
                )
            ),
            PetService(
                id = "clinic_mehr_charity",
                name = "کلینیک حمایتی و درمانی مهرگان",
                alternativeNames = mapOf(
                    AppLanguage.FA to "کلینیک حمایتی و درمانی مهرگان",
                    AppLanguage.EN to "Mehregan Supportive Veterinary",
                    AppLanguage.AZ to "مهرگان درمانگاهی (حیمایتلی)"
                ),
                type = "clinic",
                rating = 4.6f,
                address = mapOf(
                    AppLanguage.FA to "تهران، خیابان ولیعصر، تقاطع مطهری، کوچه بن‌بست نور",
                    AppLanguage.EN to "Tehran, Valiasr St, Motahari Intersection"
                ),
                phone = "021-88907060",
                workingHours = mapOf(
                    AppLanguage.FA to "۹ صبح الی ۵ بعد از ظهر",
                    AppLanguage.EN to "9:00 AM - 5:00 PM"
                ),
                x = -220f,
                y = -250f,
                description = mapOf(
                    AppLanguage.FA to "عقیم‌سازی یارانه‌ای و درمان آسیب‌های گربه‌های بومی خیابانی ده‌ها منطقه، همراه با تیم جراح داوطلب.",
                    AppLanguage.EN to "Subsidized neutering and emergency triage for stray cats done by volunteer doctors."
                )
            ),
            PetService(
                id = "shop_tabriz_deniz",
                name = "پت شاپ و کلینیک دنیزتبریز",
                alternativeNames = mapOf(
                    AppLanguage.FA to "پت شاپ و کلینیک دنیزتبریز",
                    AppLanguage.EN to "Deniz Pet-Shop Tabriz",
                    AppLanguage.AZ to "تبریز دنیز پت‌شاپی و درمانگاهی"
                ),
                type = "shop",
                rating = 4.8f,
                address = mapOf(
                    AppLanguage.FA to "تبریز، بلوار ایل‌گلی، فلکه بزرگ، مجتمع دنیز",
                    AppLanguage.AZ to "تبریز، ائل‌گؤلی بولباری، دنیز مجتمعی"
                ),
                phone = "041-33887766",
                workingHours = mapOf(
                    AppLanguage.FA to "۱۰ صبح الی ۱۰ شب",
                    AppLanguage.AZ to "ساعت ۱۰-دان ۲۲-دک"
                ),
                x = 50f,
                y = 280f,
                description = mapOf(
                    AppLanguage.FA to "فروشگاه بزرگ ملزومات گربه و داروهای ویتامینه وارداتی تحت نظارت دکتر شیبان.",
                    AppLanguage.EN to "Prominent northern boutique providing high quality nutrients and supplements."
                )
            ),
            PetService(
                id = "market_kurdistan_pet",
                name = "بازار و کلینیک حیوانات زاگرس (سنندج)",
                alternativeNames = mapOf(
                    AppLanguage.FA to "بازار و کلینیک حیوانات زاگرس (سنندج)",
                    AppLanguage.EN to "Zagros Pet Center - Sanandaj",
                    AppLanguage.KU to "ناوەندی ئاژەڵانی زاگرۆس لە سنە"
                ),
                type = "market",
                rating = 4.5f,
                address = mapOf(
                    AppLanguage.FA to "سنندج، خیابان پاسداران، روبروی دانشگاه علوم پزشکی",
                    AppLanguage.KU to "سنە، شەقامی پاسداران، بەرامبەر زانکۆ"
                ),
                phone = "087-33221144",
                workingHours = mapOf(
                    AppLanguage.FA to "۹ صبح الی ۹ شب",
                    AppLanguage.KU to "۹ی بەیانی تا ۹ی شەو"
                ),
                x = -280f,
                y = 80f,
                description = mapOf(
                    AppLanguage.FA to "بزرگترین مجموعه تخصصی حیوانات در کردستان، مجهز به آرایشگاه تخصصی و توزیع کلی غذاهای برند جهانی.",
                    AppLanguage.KU to "گەورەترین فرۆشگەی کەلوپەلی ئاژەڵان لە کوردستاندا بە باشترین کوالیتی."
                )
            )
        )
    }

    var serviceList by remember { mutableStateOf(initialServices) }
    var selectedService by remember { mutableStateOf<PetService?>(null) }
    var activeRouteDirection by remember { mutableStateOf(false) } // Render routing path or not
    
    // Add custom clinic location state
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var newLocationClickPoint by remember { mutableStateOf(Offset(0f, 0f)) }
    var newLocName by remember { mutableStateOf("") }
    var newLocPhone by remember { mutableStateOf("") }
    var newLocType by remember { mutableStateOf("clinic") } // "clinic", "shop", "market"
    var newLocAddress by remember { mutableStateOf("") }

    // Floating route notification state
    var currentRouteIndex by remember { mutableStateOf(0) }
    val routeSteps = remember(selectedService) {
        if (selectedService == null) emptyList() else listOf(
            t("route_step_init") + " (0, 0)",
            "۱۰۰ متر حرکت مستقیم در بلوار یاشام",
            if (selectedService!!.x >= 0) "پیچیدن به راست وارد بزرگراه صیاد شیرازی" else "پیچیدن به چپ به سمت خیابان آزادی",
            "مستقیم به طول ${String.format("%.1f", sqrt(selectedService!!.x * selectedService!!.x + selectedService!!.y * selectedService!!.y) / 100.0)} کیلومتر",
            "رسیدن به مقصد نهایی در سمت راست: " + (selectedService!!.alternativeNames[currentLang] ?: selectedService!!.name)
        )
    }

    // Auto-scroll list when route details change
    LaunchedEffect(selectedService) {
        activeRouteDirection = false
        currentRouteIndex = 0
    }

    // Filter services according to Search query & Filter category
    val filteredServices = remember(serviceList, searchQuery, selectedCategory, currentLang) {
        serviceList.filter { service ->
            val matchesCategory = selectedCategory == "all" || service.type == selectedCategory
            val matchText = searchQuery.lowercase()
            val name = (service.alternativeNames[currentLang] ?: service.name).lowercase()
            val addr = (service.address[currentLang] ?: service.address[AppLanguage.FA] ?: "").lowercase()
            val matchesSearch = matchText.isEmpty() || name.contains(matchText) || addr.contains(matchText) || service.type.contains(matchText)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Upper Title & Help Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = t("map_title_header") + " 📍",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "دوبار ضربه روی هر جای نقشه برای ثبت کلینیک جدید شما",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Current Language display badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    Toast.makeText(context, "یاشام پت - سیستم ناوبری دقیق فعال است", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(
                    text = "GPS ACTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // 1. Search Bar & Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی کلینیک، پت‌شاپ، واکسیناسیون...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("map_search_field"),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* clear focus */ })
            )
        }

        // Filter chips row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                Pair("all", "📍 همه مراکز"),
                Pair("clinic", t("clinics")),
                Pair("shop", t("pet_shops")),
                Pair("market", t("pet_markets"))
            )
            items(filters) { (id, label) ->
                val isSelected = selectedCategory == id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategory = id
                        selectedService = null
                    },
                    label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // 2. Map Canvas (The core interactive pannable/zoomable section)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)) // Soft blue sky background for coordinates
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            mapScale = (mapScale * zoom).coerceIn(0.6f, 4.0f)
                            mapOffset += pan
                        }
                    }
                    .pointerInput(Unit) {
                        // Double tap to record coordinate and insert custom clinic marker!
                        detectTransformGestures { centroid, _, _, _ ->
                            // Custom double tap gesture is approximated or invoked by click on emulator
                        }
                    }
                    .clickable {
                        // Safe interactive tap helper to guide placement of markers
                        Toast
                            .makeText(
                                context,
                                "لمس طولانی یا دو بار کلیک جهت ثبت یک مرکز جدید در همین نقطه",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
            ) {
                // Background simulated grid and roads
                val animatedRouteProgress = rememberInfiniteTransition().animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Dynamic map center
                    val centerX = canvasWidth / 2f + mapOffset.x
                    val centerY = canvasHeight / 2f + mapOffset.y

                    // Draw soft background grass parks
                    // Green parks rects
                    drawRoundRect(
                        color = Color(0xFFC8E6C9),
                        topLeft = Offset(centerX - 350f * mapScale, centerY - 250f * mapScale),
                        size = Size(200f * mapScale, 180f * mapScale),
                        cornerRadius = CornerRadius(16f * mapScale),
                        alpha = 0.8f
                    )
                    drawRoundRect(
                        color = Color(0xFFC8E6C9),
                        topLeft = Offset(centerX + 180f * mapScale, centerY + 50f * mapScale),
                        size = Size(250f * mapScale, 200f * mapScale),
                        cornerRadius = CornerRadius(16f * mapScale),
                        alpha = 0.8f
                    )

                    // Draw city river (Light Blue waveline)
                    val riverPath = Path().apply {
                        moveTo(centerX - 600f * mapScale, centerY + 300f * mapScale)
                        cubicTo(
                            centerX - 300f * mapScale, centerY + 100f * mapScale,
                            centerX + 100f * mapScale, centerY + 400f * mapScale,
                            centerX + 600f * mapScale, centerY + 200f * mapScale
                        )
                    }
                    drawPath(
                        path = riverPath,
                        color = Color(0xFF90CAF9),
                        style = Stroke(width = 30f * mapScale),
                        alpha = 0.9f
                    )

                    // Draw main arterial roads (Big cross lines)
                    // Road 1 (Horizontal highway)
                    drawLine(
                        color = Color(0xFFECEFF1),
                        start = Offset(0f, centerY),
                        end = Offset(canvasWidth, centerY),
                        strokeWidth = 40f * mapScale
                    )
                    // Road 2 (Vertical highway)
                    drawLine(
                        color = Color(0xFFECEFF1),
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, canvasHeight),
                        strokeWidth = 40f * mapScale
                    )
                    // Road 3 (Diagonal Boulevard)
                    drawLine(
                        color = Color(0xFFECEFF1),
                        start = Offset(0f, 0f + mapOffset.y),
                        end = Offset(canvasWidth, canvasHeight + mapOffset.y),
                        strokeWidth = 25f * mapScale
                    )

                    // Draw center strip road dash marks
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f * mapScale, 15f * mapScale), 0f)
                    drawLine(
                        color = Color(0xFFFFEB3B),
                        start = Offset(0f, centerY),
                        end = Offset(canvasWidth, centerY),
                        strokeWidth = 2f * mapScale,
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = Color(0xFFFFEB3B),
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, canvasHeight),
                        strokeWidth = 2f * mapScale,
                        pathEffect = dashEffect
                    )

                    // Draw User locator point (0,0) - Glowing pulse sapphire circle
                    drawCircle(
                        color = Color(0x3F1976D2),
                        radius = 35f * mapScale,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color(0xFF1976D2),
                        radius = 10f * mapScale,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f * mapScale,
                        center = Offset(centerX, centerY)
                    )

                    // Draw active routing navigation path if requested
                    if (activeRouteDirection && selectedService != null) {
                        val destinationX = centerX + selectedService!!.x * mapScale
                        val destinationY = centerY + selectedService!!.y * mapScale

                        // Draw path line with glowing animated dash
                        val routePath = Path().apply {
                            moveTo(centerX, centerY)
                            // Draw an angled waypoint for high-fidelity roads
                            lineTo(centerX, destinationY)
                            lineTo(destinationX, destinationY)
                        }

                        drawPath(
                            path = routePath,
                            color = Color(0xFFFF5722),
                            style = Stroke(
                                width = 8f * mapScale,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(20f * mapScale, 15f * mapScale),
                                    -animatedRouteProgress.value * 40f * mapScale
                                )
                            )
                        )
                    }

                    // Draw pins for each filtered clinic/shop
                    filteredServices.forEach { service ->
                        val pinX = centerX + service.x * mapScale
                        val pinY = centerY + service.y * mapScale

                        // Check within frame boundary to optimize rendering
                        if (pinX >= 0 && pinX <= canvasWidth && pinY >= 0 && pinY <= canvasHeight) {
                            val isSelected = selectedService?.id == service.id
                            val pinBaseColor = when (service.type) {
                                "clinic" -> Color(0xFFE53935)  // Red
                                "shop" -> Color(0xFF8E24AA)    // Purple
                                "market" -> Color(0xFFFFB300)  // Golden Star
                                else -> Color(0xFF00ACC1)
                            }

                            // Glowing circle if selected
                            if (isSelected) {
                                drawCircle(
                                    color = pinBaseColor.copy(alpha = 0.3f),
                                    radius = 28f * mapScale,
                                    center = Offset(pinX, pinY)
                                )
                            }

                            // Draw Pin droplet or bubble
                            val pinPath = Path().apply {
                                moveTo(pinX, pinY)
                                cubicTo(
                                    pinX - (12f * mapScale), pinY - (22f * mapScale),
                                    pinX - (15f * mapScale), pinY - (35f * mapScale),
                                    pinX, pinY - (35f * mapScale)
                                )
                                cubicTo(
                                    pinX + (15f * mapScale), pinY - (35f * mapScale),
                                    pinX + (12f * mapScale), pinY - (22f * mapScale),
                                    pinX, pinY
                                )
                            }
                            drawPath(path = pinPath, color = pinBaseColor)

                            // Pin head white container
                            drawCircle(
                                color = Color.White,
                                radius = 7f * mapScale,
                                center = Offset(pinX, pinY - 24f * mapScale)
                            )
                            // Inner core symbol dot
                            drawCircle(
                                color = pinBaseColor,
                                radius = 4f * mapScale,
                                center = Offset(pinX, pinY - 24f * mapScale)
                            )
                        }
                    }
                }

                // Interactive Overlaid Zoom and Pin Custom Location tools (FABs inside Map Card)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Zoom IN
                    FloatingActionButton(
                        onClick = { mapScale = (mapScale * 1.25f).coerceAtMost(4f) },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "بزرگنمایی", modifier = Modifier.size(20.dp))
                    }

                    // Zoom OUT
                    FloatingActionButton(
                        onClick = { mapScale = (mapScale * 0.8f).coerceAtLeast(0.6f) },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "کوچک‌نمایی", modifier = Modifier.size(20.dp))
                    }

                    // Register custom location right at Map Offset!
                    FloatingActionButton(
                        onClick = {
                            newLocationClickPoint = Offset(
                                -mapOffset.x / mapScale + (Random().nextFloat() * 200f - 100f),
                                -mapOffset.y / mapScale + (Random().nextFloat() * 200f - 100f)
                            )
                            showAddLocationDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.AddLocation, contentDescription = "افزودن مرکز جدید")
                    }
                }

                // Guide label layer
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "بزرگنمایی: ${String.format("%.1f", mapScale)}x | کاربران آنلاین یاشام پت: ۴۲ نفر",
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Bottom Screen: Selected Location Sheet OR General Services Cards List
        AnimatedVisibility(
            visible = selectedService != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedService != null) {
                val service = selectedService!!
                val serviceTitle = service.alternativeNames[currentLang] ?: service.name
                val serviceAddr = service.address[currentLang] ?: service.address[AppLanguage.FA] ?: ""
                val serviceDesc = service.description[currentLang] ?: service.description[AppLanguage.FA] ?: ""

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title bar with exit button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val typeIcon = when (service.type) {
                                    "clinic" -> Icons.Default.MedicalServices
                                    "shop" -> Icons.Default.LocalMall
                                    else -> Icons.Default.Storefront
                                }
                                val iconColor = when (service.type) {
                                    "clinic" -> Color(0xFFE53935)
                                    "shop" -> Color(0xFF8E24AA)
                                    else -> Color(0xFFFFB300)
                                }
                                Icon(typeIcon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                                Text(serviceTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            IconButton(onClick = { selectedService = null }) {
                                Icon(Icons.Default.Close, contentDescription = "بستن")
                            }
                        }

                        // Rating + Distance + Hours
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Text("${service.rating} / 5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                val simulatedDist = String.format("%.2f", sqrt(service.x * service.x + service.y * service.y) / 100.0)
                                Text("$simulatedDist کیلومتر", fontSize = 11.sp, color = Color.Gray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Text(service.workingHours[currentLang] ?: service.workingHours[AppLanguage.FA] ?: "", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        // Description & Address Text
                        Text(serviceDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("📍 آدرس: $serviceAddr", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)

                        // If route navigation is active, draw route guide
                        if (activeRouteDirection && routeSteps.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("ناوبری لحظه‌ای یاشام پت:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(routeSteps[currentRouteIndex], fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Row {
                                        IconButton(
                                            onClick = { currentRouteIndex = (currentRouteIndex - 1).coerceAtLeast(0) },
                                            enabled = currentRouteIndex > 0
                                        ) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "قبلی", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { currentRouteIndex = (currentRouteIndex + 1).coerceAtMost(routeSteps.size - 1) },
                                            enabled = currentRouteIndex < routeSteps.size - 1
                                        ) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "بعدی", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Actions: Routing Line and Call Phone!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { activeRouteDirection = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(t("directions"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "در حال برقراری تماس با ${serviceTitle}: ${service.phone}", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(t("call_now"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Default services card list underneath
        if (selectedService == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            ) {
                Text(
                    text = "لیست مراکز خدماتی یافت شده (${filteredServices.size} مورد):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = Color.Gray
                )

                if (filteredServices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("موردی با این مشخصات یافت نشد.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredServices) { service ->
                            val sTitle = service.alternativeNames[currentLang] ?: service.name
                            val sAddr = service.address[currentLang] ?: service.address[AppLanguage.FA] ?: ""

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedService = service
                                        // Auto snap map screen to centered location coordinates!
                                        mapOffset = Offset(-service.x * mapScale, -service.y * mapScale)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val iconType = when (service.type) {
                                            "clinic" -> Icons.Default.MedicalServices
                                            "shop" -> Icons.Default.LocalMall
                                            else -> Icons.Default.Storefront
                                        }
                                        val tintBase = when (service.type) {
                                            "clinic" -> Color(0xFFE53935)
                                            "shop" -> Color(0xFF8E24AA)
                                            else -> Color(0xFFFFB300)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(tintBase.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(iconType, contentDescription = null, tint = tintBase, modifier = Modifier.size(18.dp))
                                        }

                                        Column {
                                            Text(sTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(sAddr, fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                        Text("${service.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Location Dialog Builder
    if (showAddLocationDialog) {
        Dialog(onDismissRequest = { showAddLocationDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("ثبت مرکز جدید روی نقشه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("مختصات قرارگیری بر روی طول/عرض محلی دقیقاً تنظیم شد.", fontSize = 10.sp, color = Color.Gray)

                    TextField(
                        value = newLocName,
                        onValueChange = { newLocName = it },
                        label = { Text("نام کلینیک یا بازار حیوانات") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    TextField(
                        value = newLocPhone,
                        onValueChange = { newLocPhone = it },
                        label = { Text("شماره تلفن مرکز") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )

                    TextField(
                        value = newLocAddress,
                        onValueChange = { newLocAddress = it },
                        label = { Text("نشانی و آدرس دقیق") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    // Sector type selection
                    Text("نوع مرکز خدمات:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("clinic" to "🏨 کلینیک", "shop" to "🛍️ پت‌شاپ", "market" to "🏪 بازار پت").forEach { (typeKey, label) ->
                            val selected = newLocType == typeKey
                            FilterChip(
                                selected = selected,
                                onClick = { newLocType = typeKey },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddLocationDialog = false }) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newLocName.isNotEmpty()) {
                                    val addedService = PetService(
                                        id = "custom_" + UUID.randomUUID().toString(),
                                        name = newLocName,
                                        alternativeNames = mapOf(
                                            AppLanguage.FA to newLocName,
                                            AppLanguage.EN to newLocName
                                        ),
                                        type = newLocType,
                                        rating = 4.5f,
                                        address = mapOf(
                                            AppLanguage.FA to newLocAddress,
                                            AppLanguage.EN to newLocAddress
                                        ),
                                        phone = newLocPhone,
                                        workingHours = mapOf(
                                            AppLanguage.FA to "۹ صبح الی ۹ شب",
                                            AppLanguage.EN to "9:00 AM - 9:00 PM"
                                        ),
                                        x = newLocationClickPoint.x,
                                        y = newLocationClickPoint.y,
                                        description = mapOf(
                                            AppLanguage.FA to "مرکز سفارشی پت ثبت‌شده توسط کاربر.",
                                            AppLanguage.EN to "User added custom clinic asset."
                                        ),
                                        isCustom = true
                                    )

                                    serviceList = serviceList + addedService
                                    selectedService = addedService
                                    showAddLocationDialog = false
                                    newLocName = ""
                                    newLocPhone = ""
                                    newLocAddress = ""
                                    Toast.makeText(context, "مرکز جدید شما با موفقیت بر روی نقشه گربه یاشام فرود آمد!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "لطفا نام معتبر وارد کنید.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("درج در نقشه")
                        }
                    }
                }
            }
        }
    }
}
