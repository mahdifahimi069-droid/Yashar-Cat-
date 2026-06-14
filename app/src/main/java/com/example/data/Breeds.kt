package com.example.data

data class BreedDetail(
    val englishName: String,
    val scientificName: String = "Felis catus",
    val names: Map<AppLanguage, String>,
    val countryOfOrigin: Map<AppLanguage, String>,
    val history: Map<AppLanguage, String>,
    val personality: Map<AppLanguage, String>,
    val feedingCare: Map<AppLanguage, String>,
    val geneticDiseases: Map<AppLanguage, String>,
    val typicalWeight: String,
    val TypicalHeight: String,
    val lifespan: String,
    val colors: Map<AppLanguage, String>,
    val playfulness: Int, // 1-5
    val dependency: Int, // 1-5
    val intelligence: Int, // 1-5
    val activityLevel: Int, // 1-5
    val shedding: Int, // 1-5
    val kidFriendly: Boolean,
    val apartmentFriendly: Boolean,
    val isLongHair: Boolean
)

private data class RawBreed(
    val id: String,
    val engName: String,
    val faName: String,
    val temperament: String,
    val energyLevel: String,
    val grooming: String,
    val sheddingLevel: String,
    val diet: String,
    val healthIssues: List<String>,
    val origin: String,
    val isLongHair: Boolean
)

object BreedEncyclopedia {
    private val rawBreeds = listOf(
        RawBreed("persian_001", "Persian", "پرشین", "آرام", "کم", "بسیار زیاد", "بالا", "پروتئین بالا", listOf("PKD", "مشکلات تنفسی"), "ایران", true),
        RawBreed("maine_coon_002", "Maine Coon", "مین کون", "دوستانه", "متوسط", "متوسط", "متوسط", "متعادل", listOf("دیسپلازی مفصل ران"), "آمریکا", true),
        RawBreed("siamese_003", "Siamese", "سیامی", "باهوش و پرحرف", "بسیار بالا", "کم", "کم", "فعال", listOf("آمیلوئیدوز کبد"), "تایلند", false),
        RawBreed("ragdoll_004", "Ragdoll", "رگدال", "بسیار آرام", "کم", "متوسط", "متوسط", "کنترل وزن", listOf("HCM"), "آمریکا", true),
        RawBreed("bengal_005", "Bengal", "بنگال", "کنجکاو", "بسیار بالا", "کم", "کم", "انرژی بالا", listOf("HCM"), "آمریکا", false),
        RawBreed("sphynx_006", "Sphynx", "اسفینکس (بدون مو)", "عاطفی", "بالا", "ویژه (حمام)", "صفر", "کالری بالا", listOf("مشکلات پوستی"), "کانادا", false),
        RawBreed("abyssinian_007", "Abyssinian", "ابیسینین", "فعال", "بسیار بالا", "کم", "کم", "بالانسی", listOf("آمیلوئیدوز کلیه"), "اتیوپی", false),
        RawBreed("birman_008", "Birman", "بیرمن", "ملایم", "متوسط", "متوسط", "متوسط", "معمولی", listOf("مشکلات ادراری"), "میانمار", true),
        RawBreed("british_shorthair_009", "British Shorthair", "بریتیش شورتهیر", "مستقل", "کم", "کم", "متوسط", "کنترل وزن", listOf("HCM"), "انگلستان", false),
        RawBreed("scottish_fold_010", "Scottish Fold", "اسکاتیش فولد", "باهوش", "متوسط", "متوسط", "متوسط", "معمولی", listOf("Osteochondrodysplasia"), "اسکاتلند", false),
        RawBreed("exotic_shorthair_011", "Exotic Shorthair", "اگزاتیک شورتهیر", "آرام", "کم", "متوسط", "متوسط", "معمولی", listOf("مشکلات تنفسی"), "آمریکا", false),
        RawBreed("russian_blue_012", "Russian Blue", "راشن بلو", "خجالتی", "متوسط", "کم", "متوسط", "معمولی", listOf("سنگ مثانه"), "روسیه", false),
        RawBreed("devon_rex_013", "Devon Rex", "دوون رکس", "شیطان", "بالا", "کم", "کم", "کالری بالا", listOf("Patellar Luxation"), "انگلستان", false),
        RawBreed("cornish_rex_014", "Cornish Rex", "کورنیش رکس", "فعال", "بسیار بالا", "کم", "کم", "کالری بالا", listOf("مشکلات قلبی"), "انگلستان", false),
        RawBreed("norwegian_forest_015", "Norwegian Forest", "نروژی جنگلی", "شجاع", "متوسط", "زیاد", "زیاد", "متعادل", listOf("دیسپلازی"), "نروژ", true),
        RawBreed("oriental_shorthair_016", "Oriental Shorthair", "اورینتال شورتهیر", "باهوش", "بسیار بالا", "کم", "کم", "فعال", listOf("مشکلات قلبی"), "انگلستان", false),
        RawBreed("turkish_angora_017", "Turkish Angora", "آنقره ترکی", "باهوش", "بالا", "متوسط", "متوسط", "معمولی", listOf("ناشنوایی"), "ترکیه", true),
        RawBreed("siberian_018", "Siberian", "سیبری", "دوستانه", "متوسط", "متوسط", "زیاد", "معمولی", listOf("HCM"), "روسیه", true),
        RawBreed("manx_019", "Manx", "منکس (بیدم)", "آرام", "متوسط", "متوسط", "متوسط", "معمولی", listOf("سندرم منکس"), "جزیره من", false),
        RawBreed("burmese_020", "Burmese", "برمهای", "بسیار اجتماعی", "بالا", "کم", "کم", "معمولی", listOf("دیابت"), "تایلند", false),
        RawBreed("himalayan_021", "Himalayan", "هیمالین", "آرام", "کم", "زیاد", "زیاد", "پروتئین بالا", listOf("مشکلات تنفسی"), "آمریکا", true),
        RawBreed("tonkinese_022", "Tonkinese", "تانکینس", "بازیگوش", "بالا", "کم", "کم", "معمولی", listOf("بیماریهای لثه"), "کانادا", false),
        RawBreed("ocicat_023", "Ocicat", "اوسیکت", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("بیماری کلیه"), "آمریکا", false),
        RawBreed("ragamuffin_024", "Ragamuffin", "راگامافین", "مهربان", "کم", "متوسط", "متوسط", "معمولی", listOf("HCM"), "آمریکا", true),
        RawBreed("somali_025", "Somali", "سومالی", "فعال", "بسیار بالا", "متوسط", "متوسط", "فعال", listOf("مشکلات دندانی"), "سومالی", true),
        RawBreed("american_shorthair_026", "American Shorthair", "امریکن شورتهیر", "سازگار", "متوسط", "کم", "متوسط", "معمولی", listOf("HCM"), "آمریکا", false),
        RawBreed("balinese_027", "Balinese", "بالینیز", "باهوش", "بالا", "متوسط", "کم", "معمولی", listOf("مشکلات کبدی"), "آمریکا", true),
        RawBreed("korat_028", "Korat", "کورات", "عاطفی", "متوسط", "کم", "کم", "معمولی", listOf("مشکلات ژنتیکی"), "تایلند", false),
        RawBreed("singapura_029", "Singapura", "سنگاپورا", "شیطان", "بالا", "کم", "کم", "معمولی", listOf("مشکلات بارداری"), "سنگاپور", false),
        RawBreed("chartreux_030", "Chartreux", "شارترو", "ساکت", "متوسط", "کم", "متوسط", "معمولی", listOf("سنگ کلیه"), "فرانسه", false),
        RawBreed("japanese_bobtail_031", "Japanese Bobtail", "ژاپنی دمکوتاه", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "ژاپن", false),
        RawBreed("egyptian_mau_032", "Egyptian Mau", "مائو مصری", "سریع", "بالا", "کم", "کم", "معمولی", listOf("HCM"), "مصر", false),
        RawBreed("selkirk_rex_033", "Selkirk Rex", "سلکرک رکس", "صبور", "متوسط", "متوسط", "متوسط", "معمولی", listOf("HCM"), "آمریکا", true),
        RawBreed("havana_brown_034", "Havana Brown", "هاوانا براون", "اجتماعی", "متوسط", "کم", "کم", "معمولی", listOf("مشکلات تنفسی"), "انگلستان", false),
        RawBreed("pixie_bob_035", "Pixie Bob", "پیکسی باب", "وفادار", "متوسط", "کم", "متوسط", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("javanese_036", "Javanese", "جاوانیز", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("مشکلات چشمی"), "آمریکا", true),
        RawBreed("bombay_037", "Bombay", "بمبئی", "مهربان", "متوسط", "کم", "کم", "معمولی", listOf("مشکلات تنفسی"), "آمریکا", false),
        RawBreed("scottish_straight_038", "Scottish Straight", "اسکاتیش استریت", "آرام", "متوسط", "کم", "متوسط", "معمولی", listOf("HCM"), "اسکاتلند", false),
        RawBreed("american_curl_039", "American Curl", "امریکن کرل", "کنجکاو", "متوسط", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("la_perm_040", "LaPerm", "لاپرم", "دوستانه", "متوسط", "متوسط", "کم", "معمولی", listOf("سالم"), "آمریکا", true),
        RawBreed("snowshoe_041", "Snowshoe", "اسنوشو", "اجتماعی", "متوسط", "کم", "کم", "معمولی", listOf("HCM"), "آمریکا", false),
        RawBreed("cymric_042", "Cymric", "کیمریک", "آرام", "کم", "زیاد", "زیاد", "معمولی", listOf("مشکلات ستون فقرات"), "جزیره من", true),
        RawBreed("colorpoint_shorthair_043", "Colorpoint Shorthair", "کالرپوینت", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("مشکلات قلبی"), "انگلستان", false),
        RawBreed("nebelung_044", "Nebelung", "نبلونگ", "ساکت", "متوسط", "متوسط", "متوسط", "معمولی", listOf("سالم"), "روسیه", true),
        RawBreed("turkish_van_045", "Turkish Van", "ون ترکی", "بازیگوش", "بالا", "کم", "متوسط", "معمولی", listOf("HCM"), "ترکیه", true),
        RawBreed("american_undercut_046", "Domestic Hybrid", "امریکن هایبرید", "آرام", "متوسط", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("karelian_bobtail_047", "Karelian Bobtail", "کارلیان دم‌کوتاه", "آرام", "متوسط", "متوسط", "متوسط", "معمولی", listOf("سالم"), "روسیه", false),
        RawBreed("kurilian_bobtail_048", "Kurilian Bobtail", "کوریلین دم‌کوتاه", "فعال", "بالا", "متوسط", "متوسط", "معمولی", listOf("سالم"), "روسیه", true),
        RawBreed("peterbald_shorthair_049", "Peterbald Shorthair", "پیتربالد مو کوتاه", "عاطفی", "بالا", "متوسط", "صفر", "کالری بالا", listOf("پوستی"), "روسیه", false),
        RawBreed("serengeti_spotted_050", "Serengeti Spotted", "سرنگتی خالدار", "کنجکاو", "بسیار بالا", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("toyger_051", "Toyger", "تویگر", "آرام", "متوسط", "کم", "کم", "معمولی", listOf("مشکلات قلبی"), "آمریکا", false),
        RawBreed("chausie_052", "Chausie", "چاوسی", "فعال", "بسیار بالا", "کم", "کم", "پروتئین بالا", listOf("حساسیت گوارشی"), "مصر", false),
        RawBreed("peterbald_053", "Peterbald", "پیتربالد", "عاطفی", "بالا", "بالا", "صفر", "کالری بالا", listOf("مشکلات پوستی"), "روسیه", false),
        RawBreed("sphynx_donskoy_054", "Donskoy", "دونسکوی", "باهوش", "متوسط", "بالا", "صفر", "کالری بالا", listOf("مشکلات دندانی"), "روسیه", false),
        RawBreed("khao_manee_055", "Khao Manee", "کائو مانی", "ارتباطی", "بالا", "کم", "کم", "معمولی", listOf("ناشنوایی"), "تایلند", false),
        RawBreed("serengeti_056", "Serengeti", "سرنگتی", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("savannah_057", "Savannah", "ساوانا", "ماجراجو", "بسیار بالا", "کم", "کم", "خاص", listOf("سالم"), "آمریکا", false),
        RawBreed("york_chocolate_058", "York Chocolate", "یورک شکلاتی", "مهربان", "متوسط", "متوسط", "متوسط", "معمولی", listOf("سالم"), "آمریکا", true),
        RawBreed("turkish_angora_colored_059", "Colored Angora", "آنقره رنگی", "باهوش", "بالا", "متوسط", "متوسط", "معمولی", listOf("سالم"), "ترکیه", true),
        RawBreed("american_bobtail_060", "American Bobtail", "امریکن بابتیل", "وفادار", "متوسط", "متوسط", "متوسط", "معمولی", listOf("سالم"), "آمریکا", true),
        RawBreed("devon_rex_long_061", "Devon Longhair", "دوون لانگهیر", "شیطان", "بالا", "متوسط", "کم", "کالری بالا", listOf("مفصلی"), "انگلستان", true),
        RawBreed("munchkin_long_062", "Napoleon/Minuet", "ناپلئون", "شیرین", "متوسط", "متوسط", "متوسط", "معمولی", listOf("ستون فقرات"), "آمریکا", true),
        RawBreed("scottish_fold_long_063", "Highland Fold", "هایلند فولد", "باهوش", "متوسط", "زیاد", "زیاد", "معمولی", listOf("غضروفی"), "اسکاتلند", true),
        RawBreed("american_wirehair_064", "American Wirehair", "امریکن وایرهیر", "آرام", "متوسط", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("burmese_eu_065", "European Burmese", "برمهای اروپایی", "اجتماعی", "بالا", "کم", "کم", "معمولی", listOf("دیابت"), "تایلند", false),
        RawBreed("bambino_066", "Bambino", "بامبینو", "بازیگوش", "بالا", "بالا", "صفر", "کالری بالا", listOf("مفصلی"), "آمریکا", false),
        RawBreed("elf_cat_067", "Elf Cat", "الف کت", "اجتماعی", "بالا", "بالا", "صفر", "کالری بالا", listOf("پوستی"), "آمریکا", false),
        RawBreed("dwelf_068", "Dwelf", "دلف", "مهربان", "متوسط", "بالا", "صفر", "کالری بالا", listOf("اسکلتی"), "آمریکا", false),
        RawBreed("skookum_069", "Skookum", "اسکوکم", "خونگرم", "بالا", "متوسط", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("mink_cat_070", "Tonkinese Mink", "تانکینس مینک", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("لثه"), "کانادا", false),
        RawBreed("thai_cat_071", "Thai Cat", "تای (سیامی قدیمی)", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("کلیوی"), "تایلند", false),
        RawBreed("cyprus_072", "Cyprus Cat", "قبرسی", "مستقل", "متوسط", "کم", "متوسط", "طبیعی", listOf("سالم"), "قبرس", false),
        RawBreed("aegean_073", "Aegean", "اژه", "اجتماعی", "بالا", "کم", "متوسط", "ماهی", listOf("سالم"), "یونان", false),
        RawBreed("arabian_mau_074", "Arabian Mau", "مائو عربی", "شجاع", "بالا", "کم", "کم", "طبیعی", listOf("سالم"), "امارات عربی متحده", false),
        RawBreed("donskoy_long_075", "Donskoy Longhair", "دونسکوی لانگهیر", "مهربان", "متوسط", "متوسط", "کم", "کالری بالا", listOf("پوستی"), "روسیه", true),
        RawBreed("himalayan_variant_076", "Cashmere", "کشمیر", "آرام", "کم", "زیاد", "زیاد", "پروتئین", listOf("تنفسی"), "آمریکا", true),
        RawBreed("havana_variant_077", "Havana Longhair", "هاوانا لانگهیر", "اجتماعی", "متوسط", "زیاد", "زیاد", "معمولی", listOf("سالم"), "انگلستان", true),
        RawBreed("sokoke_078", "Sokoke", "سوکوکه", "فعال", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "کنیا", false),
        RawBreed("californian_spangled_079", "Californian Spangled", "کالیفرنیایی خالدار", "اجتماعی", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("havana_brown_long_080", "Oriental Longhair", "اورینتال لانگهیر", "باهوش", "بالا", "متوسط", "متوسط", "معمولی", listOf("قلبی"), "انگلستان", true),
        RawBreed("sphynx_peterbald_081", "Peterbald Long", "پیتربالد لانگ", "عاطفی", "بالا", "متوسط", "متوسط", "کالری", listOf("پوستی"), "روسیه", true),
        RawBreed("burmilla_long_082", "Tiffanie", "تیفانی", "اجتماعی", "متوسط", "زیاد", "زیاد", "معمولی", listOf("کلیوی"), "انگلستان", true),
        RawBreed("german_rex_083", "German Rex", "جرمن رکس", "خونگرم", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "آلمان", false),
        RawBreed("bohemian_rex_084", "Bohemian Rex", "بوهیمین رکس", "آرام", "متوسط", "زیاد", "زیاد", "معمولی", listOf("سالم"), "جمهوری چک", true),
        RawBreed("u_s_shorthair_085", "Domestic Shorthair", "گربه معمولی مو کوتاه", "متغیر", "متوسط", "کم", "متوسط", "معمولی", listOf("سالم"), "جهانی", false),
        RawBreed("u_l_longhair_086", "Domestic Longhair", "گربه معمولی مو بلند", "متغیر", "متوسط", "زیاد", "زیاد", "معمولی", listOf("گوارشی"), "جهانی", true),
        RawBreed("chanchila_087", "Chinchilla Persian", "پرشین چینچیلا", "آرام", "کم", "زیاد", "زیاد", "پروتئین", listOf("تنفسی"), "ایران", true),
        RawBreed("burmese_us_088", "American Burmese", "برمهای آمریکایی", "اجتماعی", "بالا", "کم", "کم", "معمولی", listOf("دیابت"), "تایلند", false),
        RawBreed("tonkinese_long_089", "Tibetan", "تبتی", "باهوش", "بالا", "متوسط", "متوسط", "معمولی", listOf("لثه"), "تبت", true),
        RawBreed("sphynx_ukrainian_090", "Ukrainian Levkoy", "اوکراینی لِوکوی", "وفادار", "متوسط", "بالا", "صفر", "کالری", listOf("پوستی"), "اوکراین", false),
        RawBreed("li_hua_091", "Li Hua", "لیهوا", "مستقل", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "چین", false),
        RawBreed("dragon_li_092", "Dragon Li", "اژدهای لی", "شکارچی", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "چین", false),
        RawBreed("havana_brown_rare_093", "Havana Brown", "هاوانای اصیل", "اجتماعی", "متوسط", "کم", "کم", "معمولی", listOf("تنفسی"), "انگلستان", false),
        RawBreed("thai_seal_094", "Thai Seal Point", "تای سیل پوینت", "باهوش", "بالا", "کم", "کم", "معمولی", listOf("کلیوی"), "تایلند", false),
        RawBreed("scottish_fold_short_095", "Scottish Shorthair", "اسکاتیش شورتهیر", "آرام", "متوسط", "کم", "متوسط", "معمولی", listOf("غضروفی"), "اسکاتلند", false),
        RawBreed("munchkin_short_096", "Munchkin", "مانچکین", "شیرین", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("burmilla_short_097", "Burmilla", "برمیلا", "آرام", "متوسط", "کم", "متوسط", "معمولی", listOf("کلیوی"), "انگلستان", false),
        RawBreed("chantilly_tiffany_098", "Chantilly Tiffany", "چنتیلی تیفانی", "مهربان", "متوسط", "متوسط", "متوسط", "معمولی", listOf("سالم"), "آمریکا", true),
        RawBreed("american_keuda_099", "American Keuda", "امریکن کیودا", "فعال", "بالا", "کم", "کم", "معمولی", listOf("سالم"), "آمریکا", false),
        RawBreed("desert_lynx_100", "Desert Lynx", "سیاه گوش کویری", "شکارچی", "بسیار بالا", "کم", "کم", "طبیعی", listOf("سالم"), "آمریکا", false)
    )

    val breeds: List<BreedDetail> = rawBreeds.map { item ->
        val weight = if (item.engName in listOf("Maine Coon", "Savannah")) "6.0 - 12.0 kg" else "3.5 - 6.5 kg"
        val height = "25 - 35 cm"
        val life = "12 - 15 years"

        // Localized dictionary mapper
        val namesMap = mapOf(
            AppLanguage.FA to "گربه ${item.faName}",
            AppLanguage.EN to "${item.engName} Cat",
            AppLanguage.AZ to "${item.engName} پیشیکی",
            AppLanguage.KU to "پشیلەی ${item.engName}",
            AppLanguage.BAL to "${item.engName} گربه"
        )

        val originMap = mapOf(
            AppLanguage.FA to item.origin,
            AppLanguage.EN to item.origin,
            AppLanguage.AZ to item.origin,
            AppLanguage.KU to item.origin,
            AppLanguage.BAL to item.origin
        )

        val histMap = mapOf(
            AppLanguage.FA to "نژاد ${item.faName} یکی از محبوب‌ترین و شناخته‌شده‌ترین نژادهای گربه در جهان است که مبدا تاریخی آن ${item.origin} می‌باشد.",
            AppLanguage.EN to "The ${item.engName} is a beloved cat breed originating from ${item.origin}, cherished for its unique traits and appearance.",
            AppLanguage.AZ to "${item.engName} دونیانین ان گوزل پیشیک نژادلریندن بیری‌دیر کی اصلی ${item.origin} کؤچور.",
            AppLanguage.KU to "نەژادی ${item.engName} یەکێکە لە ناسراوترین جۆرەکانی پشیلە کە مێژووەکەی دەگەڕێتەوە بۆ ${item.origin}.",
            AppLanguage.BAL to "${item.engName} باز مشهورین نسلی چہ پیشکاں انت کہ اشی اصل جا دگه ${item.origin} انت."
        )

        val persMap = mapOf(
            AppLanguage.FA to "دارای شخصیتی ${item.temperament}، مهربان و باهوش با تراز انرژی ${item.energyLevel} است. از تعامل با انسان بسیار لذت می‌برد.",
            AppLanguage.EN to "A very ${item.temperament} and affectionate breed, with a ${item.energyLevel} energy level. Extremely friendly and responsive.",
            AppLanguage.AZ to "بو نژاد چوخ ${item.temperament} و سئویملی دیر، انرژی حدّی ${item.energyLevel} اولاراق اویناماغی سئور.",
            AppLanguage.KU to "خاوەنی کەسایەتییەکی ${item.temperament}ە لەگەڵ ئاستی وزەی ${item.energyLevel} کە پەیوەندییەکی باشی هەیە.",
            AppLanguage.BAL to "بسیار ${item.temperament} و عاطفی انت، هم گوں وتی واجهہ باز مہر کن ات."
        )

        val careMap = mapOf(
            AppLanguage.FA to "دستورالعمل نگهداری موها: ${item.grooming}. رژیم غذایی هدفمند: ${item.diet}. میزان ریزش مو در فصول مختلف: ${item.sheddingLevel}.",
            AppLanguage.EN to "Grooming standard is ${item.grooming}. Suggested nutrition guideline: ${item.diet}. Seasonal hair shedding level: ${item.sheddingLevel}.",
            AppLanguage.AZ to "توکلرین باخیلاسی: ${item.grooming}. غدا طرزی: ${item.diet}. توک توکولمه درجه‌سی: ${item.sheddingLevel}.",
            AppLanguage.KU to "چاودێری موو: ${item.grooming}. پارێزی خواردن: ${item.diet}. وەرینی مووی کاتی: ${item.sheddingLevel}.",
            AppLanguage.BAL to "مویکانی ستاپ کنگ: ${item.grooming}. ورگ مخصوص: ${item.diet}. موی پرینگ درجه: ${item.sheddingLevel}."
        )

        val disMap = mapOf(
            AppLanguage.FA to "بیماری‌های شایع و پایش ژنتیکی مورد نیاز: ${item.healthIssues.joinToString("، ")}.",
            AppLanguage.EN to "Common genetic issues and recommended clinical monitoring: ${item.healthIssues.joinToString(", ")}.",
            AppLanguage.AZ to "بو نژادین ائرثی خسته‌لیکلری: ${item.healthIssues.joinToString("، ")}.",
            AppLanguage.KU to "نەخۆشییە بۆماوەییە باوەکان: ${item.healthIssues.joinToString("، ")}.",
            AppLanguage.BAL to "امراض ارثی کہ مبتلا بیت: ${item.healthIssues.joinToString("، ")}."
        )

        val colorsMap = mapOf(
            AppLanguage.FA to "سفید، مشکی، طوسی، کرم، خاکستری، دو رنگ",
            AppLanguage.EN to "White, Black, Grey, Cream, Silver, Bicolor",
            AppLanguage.AZ to "آغ، قارا، بوز، کرم، ایکی رنگلی",
            AppLanguage.KU to "سپی، ڕەش، خۆڵەمێشی، دوو ڕەنگ",
            AppLanguage.BAL to "اسپیت، سیاه، طبل، کرمی"
        )

        val energyVal = when (item.energyLevel) {
            "بسیار بالا" -> 5
            "بالا" -> 4
            "متوسط" -> 3
            else -> 2
        }

        val shedVal = when (item.sheddingLevel) {
            "بسیار زیاد", "زیاد" -> 5
            "بالا" -> 4
            "متوسط" -> 3
            "کم" -> 2
            else -> 1
        }

        val groomingVal = when (item.grooming) {
            "بسیار زیاد" -> 5
            "بالا", "زیاد" -> 4
            "متوسط" -> 3
            else -> 2
        }

        BreedDetail(
            englishName = item.engName,
            names = namesMap,
            countryOfOrigin = originMap,
            history = histMap,
            personality = persMap,
            feedingCare = careMap,
            geneticDiseases = disMap,
            typicalWeight = weight,
            TypicalHeight = height,
            lifespan = life,
            colors = colorsMap,
            playfulness = energyVal,
            dependency = groomingVal,
            intelligence = 4,
            activityLevel = energyVal,
            shedding = shedVal,
            kidFriendly = true,
            apartmentFriendly = !item.isLongHair || item.temperament.contains("آرام"),
            isLongHair = item.isLongHair
        )
    }

    fun searchBreeds(query: String, lang: AppLanguage): List<BreedDetail> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return breeds

        return breeds.filter { breed ->
            val name = breed.names[lang]?.lowercase() ?: ""
            val eng = breed.englishName.lowercase()
            val country = breed.countryOfOrigin[lang]?.lowercase() ?: ""
            val personality = breed.personality[lang]?.lowercase() ?: ""
            val history = breed.history[lang]?.lowercase() ?: ""

            name.contains(q) || eng.contains(q) || country.contains(q) ||
                    personality.contains(q) || history.contains(q) ||
                    (q.contains("fluffy") || q.contains("پرمو") || q.contains("توکلو")) && breed.isLongHair ||
                    (q.contains("apartment") || q.contains("آپارتمان") || q.contains("کمرنگ")) && breed.apartmentFriendly ||
                    (q.contains("quiet") || q.contains("آرام") || q.contains("ساکیت")) && breed.playfulness <= 3 ||
                    (q.contains("shed") || q.contains("ریزش")) && breed.shedding >= 4 ||
                    (q.contains("kid") || q.contains("کودک") || q.contains("اوشاق")) && breed.kidFriendly
        }
    }
}
