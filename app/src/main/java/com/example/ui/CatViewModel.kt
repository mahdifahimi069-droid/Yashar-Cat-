package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CatViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val TAG = "CatViewModel"
    private val repository: CatRepository

    // Global settings & preferences states
    val currentLanguage = MutableStateFlow(AppLanguage.FA)
    val isPremium = MutableStateFlow(false) // Toggle Premium status
    val currentPersona = MutableStateFlow("vet_f") // Chat companion speaker persona
    val ttsEnabled = MutableStateFlow(true)

    // Database derived states
    val allCats = MutableStateFlow<List<Cat>>(emptyList())
    val selectedCat = MutableStateFlow<Cat?>(null)

    val medicalRecords = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val growthRecords = MutableStateFlow<List<GrowthRecord>>(emptyList())
    val currentDailyLog = MutableStateFlow<DailyLog?>(null)
    val recentLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val reproductionRecords = MutableStateFlow<List<ReproductionRecord>>(emptyList())

    // Chatbot States
    val chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList()) // text to isUser
    val isChatLoading = MutableStateFlow(false)

    // AI Lab results
    val aiResult = MutableStateFlow("")
    val isAiLoading = MutableStateFlow(false)

    // Text To Speech engine
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CatRepository(db)

        // Initialize Native Speech Synthesis Engine
        tts = TextToSpeech(application, this)

        // Reactively observe registered cats
        viewModelScope.launch {
            repository.allCats.collect { cats ->
                allCats.value = cats
                if (selectedCat.value == null && cats.isNotEmpty()) {
                    selectCat(cats.first())
                }
            }
        }

        // Keep observing child records when selectedCat shifts
        viewModelScope.launch {
            selectedCat.collect { cat ->
                if (cat != null) {
                    observeCatRecords(cat.id)
                } else {
                    clearCatObservations()
                }
            }
        }
    }

    private fun observeCatRecords(catId: Int) {
        viewModelScope.launch {
            repository.getMedicalRecords(catId).collect {
                medicalRecords.value = it
            }
        }
        viewModelScope.launch {
            repository.getGrowthRecords(catId).collect {
                growthRecords.value = it
            }
        }
        viewModelScope.launch {
            repository.getRecentDailyLogs(catId).collect {
                recentLogs.value = it
            }
        }
        viewModelScope.launch {
            repository.getReproductionRecords(catId).collect {
                reproductionRecords.value = it
            }
        }
        // Fetch or create standard daily log for the current calendar date
        viewModelScope.launch {
            val todayStr = getTodayDateString()
            repository.getDailyLog(catId, todayStr).collect { log ->
                if (log != null) {
                    currentDailyLog.value = log
                } else {
                    // Create missing blank record for today
                    val freshLog = DailyLog(catId = catId, date = todayStr)
                    repository.insertDailyLog(freshLog)
                }
            }
        }
    }

    private fun clearCatObservations() {
        medicalRecords.value = emptyList()
        growthRecords.value = emptyList()
        currentDailyLog.value = null
        recentLogs.value = emptyList()
        reproductionRecords.value = emptyList()
    }

    fun selectCat(cat: Cat) {
        selectedCat.value = cat
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- Cat CRU Operations ---
    fun registerCat(
        name: String,
        breed: String,
        gender: String,
        age: Int,
        weight: Float,
        color: String,
        microchip: String,
        isNeutered: Boolean,
        photoIndex: Int
    ) {
        viewModelScope.launch {
            val freshCat = Cat(
                name = name,
                breed = breed,
                gender = gender,
                ageMonths = age,
                weight = weight,
                color = color,
                microchip = microchip,
                isNeutered = isNeutered,
                photoIndex = photoIndex
            )
            val newId = repository.insertCat(freshCat)
            val insertedCat = freshCat.copy(id = newId.toInt())
            selectCat(insertedCat)
        }
    }

    fun updateCatProfile(cat: Cat) {
        viewModelScope.launch {
            repository.updateCat(cat)
            selectedCat.value = cat
        }
    }

    fun deleteSelectedCat() {
        val cat = selectedCat.value ?: return
        viewModelScope.launch {
            repository.deleteCat(cat)
            selectedCat.value = null
            // Select another cat if available
            val remaining = allCats.value.filter { it.id != cat.id }
            if (remaining.isNotEmpty()) {
                selectCat(remaining.first())
            }
        }
    }

    // --- Medical Record Adding ---
    fun addMedicalRecord(type: String, title: String, notes: String = "", reminderDate: String = "", attachmentPath: String? = null, attachmentType: String? = null, attachmentName: String? = null) {
        val catId = selectedCat.value?.id ?: return
        viewModelScope.launch {
            val record = MedicalRecord(
                catId = catId,
                type = type,
                title = title,
                date = getTodayDateString(),
                notes = notes,
                reminderDate = reminderDate,
                isCompleted = false,
                attachmentPath = attachmentPath,
                attachmentType = attachmentType,
                attachmentName = attachmentName
            )
            repository.insertMedicalRecord(record)
        }
    }

    fun removeMedicalRecord(record: MedicalRecord) {
        viewModelScope.launch {
            repository.deleteMedicalRecord(record)
        }
    }

    // --- Growth Log ---
    fun addGrowthRecord(weight: Float, height: Float, note: String) {
        val catId = selectedCat.value?.id ?: return
        viewModelScope.launch {
            val record = GrowthRecord(
                catId = catId,
                date = getTodayDateString(),
                weight = weight,
                height = height,
                note = note
            )
            repository.insertGrowthRecord(record)

            // Update main cat's current weight as well for consistency
            selectedCat.value?.let { cat ->
                repository.updateCat(cat.copy(weight = weight))
            }
        }
    }

    fun deleteGrowthRecord(growth: GrowthRecord) {
        viewModelScope.launch {
            repository.deleteGrowthRecord(growth)
        }
    }

    // --- Daily Logging Updates ---
    fun logWaterIntake(addMl: Int) {
        val log = currentDailyLog.value ?: return
        viewModelScope.launch {
            val updated = log.copy(waterIntakeMl = log.waterIntakeMl + addMl)
            repository.insertDailyLog(updated)
            currentDailyLog.value = updated
        }
    }

    fun logFeeding(dryGrams: Int, wetGrams: Int, treats: Int) {
        val log = currentDailyLog.value ?: return
        viewModelScope.launch {
            val updated = log.copy(
                dryFoodGrams = log.dryFoodGrams + dryGrams,
                wetFoodGrams = log.wetFoodGrams + wetGrams,
                treatsCount = log.treatsCount + treats
            )
            repository.insertDailyLog(updated)
            currentDailyLog.value = updated
        }
    }

    fun updateSelfCheckreminders(
        litterCount: Int = 0,
        vomits: Int = 0,
        diarrhea: Int = 0,
        sneezes: Int = 0,
        scratching: Int = 0,
        lethargic: Boolean = false
    ) {
        val log = currentDailyLog.value ?: return
        viewModelScope.launch {
            val updated = log.copy(
                litterBoxCleanCount = log.litterBoxCleanCount + litterCount,
                vomitCount = log.vomitCount + vomits,
                diarrheaCount = log.diarrheaCount + diarrhea,
                sneezingCount = log.sneezingCount + sneezes,
                scratchingLevel = scratching,
                isLethargic = lethargic
            )
            repository.insertDailyLog(updated)
            currentDailyLog.value = updated
        }
    }

    // --- Reproduction Diary ---
    fun addReproductionRecord(type: String, startDate: String, notes: String) {
        val catId = selectedCat.value?.id ?: return
        viewModelScope.launch {
            val record = ReproductionRecord(
                catId = catId,
                type = type,
                startDate = startDate,
                notes = notes
            )
            repository.insertReproductionRecord(record)
        }
    }

    fun removeReproduction(record: ReproductionRecord) {
        viewModelScope.launch {
            repository.deleteReproductionRecord(record)
        }
    }

    // --- AI Lab Methods (Breed, Illness, Plants, Food, Sound) ---
    fun runBreedIdentifier(bitmap: Bitmap) {
        isAiLoading.value = true
        aiResult.value = "در حال تحلیل تصویر نژاد توسط هوش مصنوعی یاشام..."
        viewModelScope.launch {
            val outcome = GeminiService.diagnoseBreed(bitmap, currentLanguage.value)
            aiResult.value = outcome
            isAiLoading.value = false
            speakResponse(stripMarkdown(outcome))
        }
    }

    fun runSymptomDiagnostic(symptomNotes: String, bodyArea: String, image: Bitmap? = null) {
        isAiLoading.value = true
        aiResult.value = "در حال بررسی علائم و آزمایش پزشکی توسط دامپزشک هوشمند..."
        viewModelScope.launch {
            val catMeta = selectedCat.value?.let {
                "گربه نام ${it.name}، نژاد ${it.breed}، سن ${it.ageMonths} ماه، وزن ${it.weight} کیلو، عقیم شده: ${it.isNeutered}"
            } ?: "گربه خانگی عمومی"
            val outcome = GeminiService.diagnoseSymptom(
                symptomText = symptomNotes,
                bodyArea = bodyArea,
                image = image,
                catDetails = catMeta,
                lang = currentLanguage.value
            )
            aiResult.value = outcome
            isAiLoading.value = false
            speakResponse(stripMarkdown(outcome))
        }
    }

    fun runFoodScan(bitmap: Bitmap, textTitle: String) {
        isAiLoading.value = true
        aiResult.value = "در حال تحلیل ترکیبات غذایی گربه..."
        viewModelScope.launch {
            val outcome = GeminiService.inspectFood(bitmap, textTitle, currentLanguage.value)
            aiResult.value = outcome
            isAiLoading.value = false
            speakResponse(stripMarkdown(outcome))
        }
    }

    fun runPlantCheck(plantName: String, bitmap: Bitmap? = null) {
        isAiLoading.value = true
        aiResult.value = "در حال اسکن پایگاه داده گیاه‌شناسی سمی..."
        viewModelScope.launch {
            val outcome = GeminiService.scanPlants(plantName, bitmap, currentLanguage.value)
            aiResult.value = outcome
            isAiLoading.value = false
            speakResponse(stripMarkdown(outcome))
        }
    }

    fun runMeowTranslator(soundAttached: Boolean, description: String) {
        isAiLoading.value = true
        aiResult.value = "در حال رمزگشایی صدای گربه و ترجمه رفتار..."
        viewModelScope.launch {
            val outcome = GeminiService.translateMeow(soundAttached, description, currentLanguage.value)
            aiResult.value = outcome
            isAiLoading.value = false
            speakResponse(stripMarkdown(outcome))
        }
    }

    // --- Smart Assistant Chatbox Dialogue ---
    fun sendChatMessage(msg: String, attachedImage: Bitmap? = null) {
        if (msg.trim().isEmpty() && attachedImage == null) return
        val displayMsg = if (attachedImage != null) "📎 [پیوست مدرک/سند] $msg" else msg
        val currentMessages = chatMessages.value.toMutableList()
        currentMessages.add(Pair(displayMsg, true))
        chatMessages.value = currentMessages

        isChatLoading.value = true
        viewModelScope.launch {
            val catMeta = selectedCat.value?.let {
                "گربه نام ${it.name}، نژاد ${it.breed}، سن ${it.ageMonths} ماه، وزن ${it.weight} کیلو، عقیم شده: ${it.isNeutered}"
            } ?: "گربه خانگی عمومی"

            val outcome = GeminiService.chatWithVoiceAssistant(
                chatHistory = currentMessages.dropLast(1),
                userMessage = msg,
                catProfile = catMeta,
                persona = currentPersona.value,
                lang = currentLanguage.value,
                image = attachedImage
            )

            val updatedMessages = chatMessages.value.toMutableList()
            updatedMessages.add(Pair(outcome, false))
            chatMessages.value = updatedMessages
            isChatLoading.value = false

            // Automatically vocalize speech response!
            speakResponse(stripMarkdown(outcome))
        }
    }

    // Clean meow translator / text history
    fun clearChat() {
        chatMessages.value = emptyList()
    }

    // --- Text To Speech Methods ---
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Setup default language locales
            val locale = if (currentLanguage.value == AppLanguage.EN) Locale.ENGLISH else Locale("fa", "IR")
            val result = tts?.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsReady = true
                tts?.setSpeechRate(1.0f)
            }
        } else {
            Log.e(TAG, "TTS Initialization failed!")
        }
    }

    fun speakResponse(rawText: String) {
        if (ttsEnabled.value && ttsReady && tts != null) {
            tts?.stop()
            // Android TTS takes string parameters for playback
            tts?.speak(rawText, TextToSpeech.QUEUE_FLUSH, null, "YashamSpeechOutput")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    private fun stripMarkdown(markdown: String): String {
        return markdown
            .replace(Regex("[#*`_\\-]"), "")
            .replace(Regex("\\[.*?\\]"), "")
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}
