package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Helper to convert Bitmap to Base64
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Call the Gemini 3.5-flash API directly using OkHttp and classic JSONObject
     */
    suspend fun generateContent(
        prompt: String,
        image: Bitmap? = null,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder!")
            return@withContext "خطا: کلید API مفقود شده است. لطفاً کلید معتبر خود را در تنظیمات وارد کنید."
        }

        val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        try {
            // Build Request JSON
            val requestJson = JSONObject()
            
            // Contents list
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // 1. Text part
            val textPart = JSONObject().put("text", prompt)
            partsArray.put(textPart)

            // 2. Optional Image part
            if (image != null) {
                val imagePart = JSONObject().put(
                    "inlineData",
                    JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", image.toBase64())
                )
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // 3. Optional System Instruction
            if (!systemInstruction.isNullOrEmpty()) {
                val sysInstrObj = JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", systemInstruction))
                )
                requestJson.put("systemInstruction", sysInstrObj)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errCode = response.code
                    val errMsg = response.peekBody(1024).string()
                    Log.e(TAG, "Unsuccessful response: $errCode - $errMsg")
                    return@withContext "خطا در برقراری ارتباط با هوش مصنوعی یاشام ($errCode). لطفاً دوباره امتحان کنید."
                }

                val responseBodyStr = response.body?.string() ?: return@withContext "پاسخ خالی از سرور هوش مصنوعی."
                val responseJson = JSONObject(responseBodyStr)
                
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "پاسخی یافت نشد.")
                    }
                }
                "متأسفم، نتوانستم تجزیه و تحلیل دقیقی انجام دهم."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Call failed", e)
            "خطا در اتصال به اینترنت یا سرور هوش مصنوعی: ${e.localizedMessage}"
        }
    }

    /**
     * 1. AI Breed Diagnosis from photos
     */
    suspend fun diagnoseBreed(bitmap: Bitmap, lang: AppLanguage): String {
        val prompt = """
            Analyze this cat picture. Identify:
            1. Likely Breed (Persian, Siamese, etc.)
            2. Confidence Percentage (%)
            3. Personality and Characteristics
            4. Common genetic illnesses
            
            Format the response nicely in Markdown list format, using the language with code: ${lang.code}.
            Make it friendly, veterinary accurate, and professional.
        """.trimIndent()
        
        val systemInstruction = "You are a professional cat show judge and expert animal geneticist. Answer in ${lang.displayName}."
        return generateContent(prompt, bitmap, systemInstruction)
    }

    /**
     * 2. AI Cat Illness & Health Diagnosis
     */
    suspend fun diagnoseSymptom(
        symptomText: String,
        bodyArea: String,
        image: Bitmap? = null,
        catDetails: String = "",
        lang: AppLanguage
    ): String {
        val prompt = """
            A user reports safety symptoms regarding a cat.
            Cat Context: $catDetails
            Symptom Area: $bodyArea
            User Notes: $symptomText
            
            Identify:
            1. Likely illness or condition
            2. Primary Causes
            3. Disease Severity (Low/Medium/Urgent)
            4. Critical first aid steps / care recommendation
            5. Recommended veterinary clinic timeline
            
            Analyze the provided image if available.
            Format the response and outline clearly with beautiful markdown formatting.
            Language: ${lang.displayName}.
            
            CRITICAL SAFETY INSTRUCTION: Remind the user that this is an AI tool and not a substitute for local professional veterinary diagnoses.
        """.trimIndent()

        val systemInstruction = "You are a seasoned emergency Feline Veterinarian Doctor from the Yasham Elite Animal Hospital. Speak with rich empathy and medical clarity."
        return generateContent(prompt, image, systemInstruction)
    }

    /**
     * 3. AI Food Inspector (Is this safe for cats?)
     */
    suspend fun inspectFood(bitmap: Bitmap, foodName: String, lang: AppLanguage): String {
        val prompt = """
            Identify this food product: "$foodName" and examine the image.
            Provide analysis:
            1. Suitability (Is it safe or toxic for cats?)
            2. Possible health hazards (digestive upset, toxins)
            3. Safe allowance portion (e.g. max grams, occasionally only, never)
            
            Format output in ${lang.displayName} with clear bullet points.
        """.trimIndent()
        
        val systemInstruction = "You are a feline toxicity expert and nutritionist. Ensure precise food safety warnings."
        return generateContent(prompt, bitmap, systemInstruction)
    }

    /**
     * 4. AI Toxic Plant Scan
     */
    suspend fun scanPlants(plantName: String, image: Bitmap? = null, lang: AppLanguage): String {
        val prompt = """
            Identify this plant: "$plantName".
            Provide feline toxicity report:
            1. Is it poisonous/dangerous for cats?
            2. Symptoms of ingestion/poisoning (vomiting, pupil dilation, lethargy, kidney failure)
            3. Vital immediate action and veterinary counter-measures
            
            Report in ${lang.displayName} with clear headers.
        """.trimIndent()

        val systemInstruction = "You are a botanical venom consultant for the National Pet ASPCA. Provide accurate, clear toxicity alerts."
        return generateContent(prompt, image, systemInstruction)
    }

    /**
     * 5. Yasham Chatbot Dialogue & Sound Companion
     */
    suspend fun chatWithVoiceAssistant(
        chatHistory: List<Pair<String, Boolean>>, //Pair of text and isUser
        userMessage: String,
        catProfile: String,
        persona: String, // "vet_f", "vet_m", "trainer", "grandma", "child"
        lang: AppLanguage,
        image: Bitmap? = null
    ): String {
        // Compile history to string
        val historyStr = chatHistory.takeLast(6).joinToString("\n") { (text, isUser) ->
            if (isUser) "User: $text" else "Yasham Assistant: $text"
        }

        val personaDesc = when(persona) {
            "vet_f" -> "A warm, deeply loving female Veterinarian. Speaks with scientific accuracy but maximum clinical empathy."
            "vet_m" -> "A precise male veterinary medical doctor. Highly technical, structured, calm, and reassuring."
            "trainer" -> "An energetic kitten behaviorist and cat-trainer. Inspiring, motivating, and full of interactive play strategies."
            "grandma" -> "A sweet, comforting grandmother who has raised 50 cats. Full of herbal care comfort, old-school affection, and gentle wisdom."
            "child" -> "A joyful, cheerful 7-year-old child companion who treats cats as their best superhero friend."
            else -> "A balanced smart feline assistant."
        }

        val prompt = """
            Current Registered Cat Profile: $catProfile
            Language requested: ${lang.displayName}.
            
            Recent Chat Conversation:
            $historyStr
            User: $userMessage
            
            Answer the user's inquiry naturally, keeping in mind the cat profile (specifically allergies, age, etc. so that your replies are custom tailored!).
            Act strictly under this persona: $personaDesc
            
            An image is attached if the user has provided a medical document, plant photo, or cat photo.
            Keep your response conversational, supportive, and medium-brief, ideal to be read aloud.
        """.trimIndent()

        val systemInstruction = "You are Yasham, a helpful pet assistant. Respond natively in ${lang.displayName}."
        return generateContent(prompt, image, systemInstruction)
    }

    /**
     * 6. Meow Translator Helper
     */
    suspend fun translateMeow(soundUploaded: Boolean, textCue: String, lang: AppLanguage): String {
        val prompt = """
            A user uploads a cat audio/video or describes its meow: "$textCue".
            Translate this feline vocalization into:
            1. Emotion (Hunger, request play, stress/anxiety, pain, anger, attention seeking)
            2. Real-world meaning (e.g. My bowl is 50% empty, I saw a fly outside, My paw is irritated)
            3. Action advice for the owner
            
            Format response playfully but informative in ${lang.displayName}.
        """.trimIndent()
        
        return generateContent(prompt, null, "You are a professional feline linguist and audio behaviour analyst.")
    }
}
