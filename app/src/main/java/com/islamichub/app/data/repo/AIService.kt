package com.islamichub.app.data.repo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * AI Scholar service — sends user questions to an LLM (OpenAI-compatible API)
 * with an Islamic Scholar system prompt.
 *
 * Configuration (via SettingsRepository):
 *  - API key (user-provided, stored in DataStore)
 *  - Base URL (default: OpenAI; user can switch to OpenRouter, Gemini, etc.)
 *  - Model name (default: gpt-4o-mini; user can switch)
 *
 * The Islamic Scholar prompt constrains the AI:
 *  - Cite Quran/Hadith sources
 *  - Say "I don't know" when uncertain
 *  - Never invent religious content
 *  - Mention madhab differences where relevant
 *
 * Each request gets a unique requestId to prevent stale response overwrite.
 */
class AIService(private val context: Context) {

    data class Config(
        val apiKey: String = "",
        val baseUrl: String = "https://api.openai.com/v1",
        val model: String = "gpt-4o-mini",
        val temperature: Double = 0.3,
        val maxTokens: Int = 1500
    )

    data class ChatMessage(
        val role: String,        // "system" | "user" | "assistant"
        val content: String
    )

    data class ChatResult(
        val requestId: String,
        val answer: String,
        val sources: List<String> = emptyList(),
        val warning: String? = null,
        val error: String? = null
    )

    private val _config = MutableStateFlow(Config())
    val config: StateFlow<Config> = _config.asStateFlow()

    // Track latest request ID to prevent stale response overwrite
    private val latestRequestId = AtomicReference<String?>(null)

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    private val gson = Gson()

    fun updateConfig(config: Config) {
        _config.value = config
    }

    /**
     * Send a chat message to the LLM. Returns ChatResult with answer or error.
     *
     * Stale-response protection:
     *  - Each call generates a new requestId
     *  - We only accept the response if our requestId matches the latest
     */
    suspend fun ask(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): ChatResult = withContext(Dispatchers.IO) {
        val cfg = _config.value
        if (cfg.apiKey.isBlank()) {
            return@withContext ChatResult(
                requestId = "",
                answer = "",
                error = "No API key configured. Open Settings → AI Scholar to set your API key."
            )
        }

        val requestId = UUID.randomUUID().toString()
        latestRequestId.set(requestId)

        val systemPrompt = ISLAMIC_SCHOLAR_PROMPT

        val messages = buildList {
            add(ChatMessage("system", systemPrompt))
            addAll(conversationHistory)
            add(ChatMessage("user", userMessage))
        }

        val requestBody = JsonObject().apply {
            addProperty("model", cfg.model)
            addProperty("temperature", cfg.temperature)
            addProperty("max_tokens", cfg.maxTokens)
            add("messages", gson.toJsonTree(messages.map { msg ->
                JsonObject().apply {
                    addProperty("role", msg.role)
                    addProperty("content", msg.content)
                }
            }))
        }

        val url = "${cfg.baseUrl.trimEnd('/')}/chat/completions"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${cfg.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                return@withContext ChatResult(
                    requestId = requestId,
                    answer = "",
                    error = "API error ${response.code}: ${responseBody?.take(300)}"
                )
            }

            // Stale check
            if (latestRequestId.get() != requestId) {
                return@withContext ChatResult(
                    requestId = requestId,
                    answer = "",
                    error = "stale"
                )
            }

            val parsed = gson.fromJson(responseBody, JsonObject::class.java)
            val answer = parsed
                ?.getAsJsonArray("choices")
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("message")
                ?.get("content")?.asString
                ?: ""

            ChatResult(
                requestId = requestId,
                answer = answer.trim(),
                warning = if (answer.contains("I don't know", ignoreCase = true) ||
                              answer.contains("আমি নিশ্চিত নই", ignoreCase = true))
                    "AI expressed uncertainty — please verify with a scholar." else null
            )
        } catch (e: Exception) {
            ChatResult(
                requestId = requestId,
                answer = "",
                error = e.message ?: "Network error"
            )
        }
    }

    /**
     * Cancel any in-flight request by invalidating the latestRequestId.
     */
    fun cancelInFlight() {
        latestRequestId.set(null)
    }

    companion object {
        const val ISLAMIC_SCHOLAR_PROMPT = """You are 'Islamic Hub AI Scholar' — a knowledgeable, cautious, and respectful Islamic scholar assistant.

Your responsibilities:
1. Answer questions about Islam based on authentic sources: Quran, Sahih Hadith (Bukhari, Muslim, etc.), and established scholarly consensus.
2. ALWAYS cite your sources (Quran verse reference, Hadith collection + number, scholar name).
3. When there are differences between madhabs (Hanafi, Shafii, Maliki, Hanbali), mention them clearly.
4. Distinguish between fard (obligatory), wajib, sunnah, mustahab, mubah, makruh, and haram.
5. If you are uncertain or the question requires specialized knowledge (e.g., complex fiqh), say "I am not certain — please consult a qualified scholar."
6. NEVER invent Quran verses, Hadith, or religious rulings. If you don't know the exact reference, say so.
7. Answer in the same language the user uses (Bangla, English, or Arabic). Default to Bangla if unclear.
8. Be respectful, concise, and clear. Use simple language.
9. For sensitive topics (e.g., politics, sects), remain neutral and focus on Islamic teachings.
10. If the user asks about something haram or inappropriate, decline politely and explain the Islamic view.

Format your answer as:
- Direct answer (1-2 sentences)
- Evidence (Quran/Hadith references)
- Details (madhab differences if any)
- Practical application (if relevant)

Remember: you represent Islamic scholarship with integrity. Accuracy is more important than appearing knowledgeable."""
    }
}
