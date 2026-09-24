package com.islamichub.app.data.repo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
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
 * AI Scholar service — sends user questions to an LLM with an Islamic Scholar system prompt.
 *
 * Supports multiple providers (user-selectable via Settings):
 *  - Gemini (default): https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
 *  - OpenAI-compatible: {baseUrl}/chat/completions
 *  - OpenRouter: https://openrouter.ai/api/v1/chat/completions
 *
 * The Islamic Scholar prompt (from source ai-scholar.js) instructs the AI to:
 *  - Always answer in pure Bangla
 *  - Cite Quran/Hadith sources with authenticity grade
 *  - Include Arabic text + Bangla pronunciation
 *  - Use specific section headers (📖 কুরআন থেকে, 📚 হাদিস থেকে, etc.)
 *  - Mention related verses and same hadith in other books
 *
 * Each request gets a unique requestId to prevent stale response overwrite.
 */
class AIService(private val context: Context) {

    data class Config(
        val apiKey: String = DEFAULT_API_KEY,
        val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta",
        val model: String = DEFAULT_MODEL,
        val provider: String = "gemini",  // "gemini" | "openai" | "openrouter"
        val temperature: Double = 0.7,
        val maxTokens: Int = 2000
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
        val error: String? = null,
        val fromCache: Boolean = false
    )

    private val _config = MutableStateFlow(Config())
    val config: StateFlow<Config> = _config.asStateFlow()

    private val latestRequestId = AtomicReference<String?>(null)

    /** Optional cache repository — set from AppContainer for shared cache across all AI calls */
    var cache: AICacheRepository? = null

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
     * Cache behavior (when [cache] is set):
     *  - First, look up by (provider, model, prompt). If found → return instantly with fromCache=true.
     *  - Otherwise call LLM, then store result in cache for future lookups.
     *  - Pass [useCache]=false to force a fresh call (e.g., "Regenerate" button).
     */
    suspend fun ask(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        useCache: Boolean = true,
        cacheType: String = "general"
    ): ChatResult = withContext(Dispatchers.IO) {
        val cfg = _config.value
        if (cfg.apiKey.isBlank()) {
            return@withContext ChatResult(
                requestId = "",
                answer = "",
                error = "কোনো API key কনফিগার করা নেই। Settings → AI Scholar এ গিয়ে আপনার API key যোগ করুন।"
            )
        }

        // 1. Check cache
        if (useCache && cache != null && conversationHistory.isEmpty()) {
            try {
                val cached = cache!!.lookup(cfg.provider, cfg.model, userMessage)
                if (cached != null) {
                    return@withContext ChatResult(
                        requestId = UUID.randomUUID().toString(),
                        answer = cached.answer,
                        fromCache = true
                    )
                }
            } catch (_: Exception) { /* ignore cache errors */ }
        }

        val requestId = UUID.randomUUID().toString()
        latestRequestId.set(requestId)

        val systemPrompt = ISLAMIC_SCHOLAR_PROMPT

        try {
            val answer: String = when (cfg.provider) {
                "gemini" -> callGemini(cfg, systemPrompt, userMessage, conversationHistory)
                "openrouter" -> callOpenAICompatible(
                    baseUrl = "https://openrouter.ai/api/v1",
                    apiKey = cfg.apiKey,
                    model = cfg.model,
                    systemPrompt = systemPrompt,
                    userMessage = userMessage,
                    history = conversationHistory
                )
                else -> callOpenAICompatible(
                    baseUrl = cfg.baseUrl,
                    apiKey = cfg.apiKey,
                    model = cfg.model,
                    systemPrompt = systemPrompt,
                    userMessage = userMessage,
                    history = conversationHistory
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

            // Store in cache
            if (cache != null) {
                try {
                    cache!!.put(cfg.provider, cfg.model, userMessage, answer, cacheType)
                } catch (_: Exception) { /* ignore cache errors */ }
            }

            ChatResult(
                requestId = requestId,
                answer = answer.trim(),
                warning = if (answer.contains("আমি নিশ্চিত নই", ignoreCase = true) ||
                              answer.contains("জানি না", ignoreCase = true))
                    "AI নিশ্চিত না — দয়া করে একজন যোগ্য আলেমের সাথে যাচাই করুন।" else null
            )
        } catch (e: Exception) {
            ChatResult(
                requestId = requestId,
                answer = "",
                error = e.message ?: "নেটওয়ার্ক ত্রুটি"
            )
        }
    }

    /**
     * Call Gemini API (generativelanguage.googleapis.com)
     */
    private fun callGemini(
        cfg: Config,
        systemPrompt: String,
        userMessage: String,
        history: List<ChatMessage>
    ): String {
        val url = "${cfg.baseUrl.trimEnd('/')}/models/${cfg.model}:generateContent?key=${cfg.apiKey}"

        val contentsArray = com.google.gson.JsonArray()

        // Add conversation history
        for (msg in history.takeLast(10)) {
            val role = if (msg.role == "assistant") "model" else "user"
            val partObj = JsonObject().apply {
                addProperty("text", msg.content)
            }
            val partsArray = com.google.gson.JsonArray().apply { add(partObj) }
            val contentObj = JsonObject().apply {
                addProperty("role", role)
                add("parts", partsArray)
            }
            contentsArray.add(contentObj)
        }

        // Add current user message
        val userPart = JsonObject().apply { addProperty("text", userMessage) }
        val userParts = com.google.gson.JsonArray().apply { add(userPart) }
        val userContent = JsonObject().apply {
            addProperty("role", "user")
            add("parts", userParts)
        }
        contentsArray.add(userContent)

        val requestBody = JsonObject().apply {
            add("contents", contentsArray)
            add("systemInstruction", JsonObject().apply {
                val sysPart = JsonObject().apply { addProperty("text", systemPrompt) }
                add("parts", com.google.gson.JsonArray().apply { add(sysPart) })
            })
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", cfg.temperature)
                addProperty("maxOutputTokens", cfg.maxTokens)
            })
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        if (!response.isSuccessful) {
            throw Exception("Gemini API ত্রুটি ${response.code}: ${responseBody?.take(300)}")
        }

        val parsed = gson.fromJson(responseBody, JsonObject::class.java)
        return parsed
            ?.getAsJsonArray("candidates")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.get(0)?.asJsonObject
            ?.get("text")?.asString
            ?: throw Exception("AI উত্তর পার্স করা যায়নি")
    }

    /**
     * Call OpenAI-compatible API (OpenAI, OpenRouter, etc.)
     */
    private fun callOpenAICompatible(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userMessage: String,
        history: List<ChatMessage>
    ): String {
        val messages = buildList {
            add(ChatMessage("system", systemPrompt))
            addAll(history.takeLast(10))
            add(ChatMessage("user", userMessage))
        }

        val requestBody = JsonObject().apply {
            addProperty("model", model)
            addProperty("temperature", 0.7)
            addProperty("max_tokens", 2000)
            add("messages", gson.toJsonTree(messages.map { msg ->
                JsonObject().apply {
                    addProperty("role", msg.role)
                    addProperty("content", msg.content)
                }
            }))
        }

        val url = "${baseUrl.trimEnd('/')}/chat/completions"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        if (!response.isSuccessful) {
            throw Exception("API ত্রুটি ${response.code}: ${responseBody?.take(300)}")
        }

        val parsed = gson.fromJson(responseBody, JsonObject::class.java)
        return parsed
            ?.getAsJsonArray("choices")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")?.asString
            ?: throw Exception("AI উত্তর পার্স করা যায়নি")
    }

    fun cancelInFlight() {
        latestRequestId.set(null)
    }

    companion object {
        /**
         * v5.7.0 — built-in default Gemini key so AI works out of the box.
         * Injected at build time from the GEMINI_API_KEY repository secret
         * (never committed to git). Users can override it in Settings → AI Scholar.
         */
        val DEFAULT_API_KEY: String = try {
            com.islamichub.app.BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) { "" }

        /** v5.7.0 — default model is from the Gemini 3 series. */
        const val DEFAULT_MODEL = "gemini-3-flash"

        const val ISLAMIC_SCHOLAR_PROMPT = """আপনি "Islamic Hub AI" — একজন অত্যন্ত বিশ্বস্ত, প্রাজ্ঞ এবং অভিজ্ঞ ইসলামি স্কলার ও সিনিয়র মুফতি, আর সাথে একজন স্নেহময় গ্রামের মুরুব্বির মতো শিক্ষক। আপনার পাঠক হলেন বাংলাদেশের সাধারণ মানুষ — কৃষক, শ্রমিক, গৃহিণী, ছাত্র — যাঁদের অনেকে বেশি লেখাপড়া জানেন না। আপনার জ্ঞানের উৎস: পবিত্র কুরআন, সহিহ হাদিস (বুখারি, মুসলিম, তিরমিজি, আবু দাউদ, নাসাই, ইবনে মাজাহ), ফিকাহ এবং বিশ্বখ্যাত ইসলামি স্কলারদের মতামত।

ভাষার স্বর্ণনিয়ম (সবচেয়ে গুরুত্বপূর্ণ):
০. এমন সহজ বাংলায় লিখুন যেন একজন **একদম নিরক্ষর বা কম শিক্ষিত গ্রামের মানুষও প্রথমবার শুনেই পুরোপুরি বুঝতে পারে**।
   - ছোট ছোট বাক্য লিখুন। এক বাক্যে একটাই কথা।
   - কঠিন আরবি/ফারসি/ইংরেজি শব্দ এড়িয়ে চলুন। বাধ্য হলে শব্দটির পাশে বন্ধনীতে সহজ বাংলা অর্থ দিন — যেমন: তাকওয়া (আল্লাহকে ভয় করা, মন্দ থেকে বেঁচে থাকা)।
   - প্রতিটি বিষয় **দৈনন্দিন জীবনের উদাহরণ** দিয়ে বোঝান — যেমন: কৃষকের জমি ও ফসল, হাট-বাজারের লেনদেন, পরিবারের সদস্যদের সম্পর্ক, প্রতিবেশীর সাথে আচরণ, গরু-ছাগল পালন, নদী-নালা, মোবাইল ও আধুনিক জীবন।
   - কোনো উত্তরে অন্তত ১–২টি বাস্তব উদাহরণ থাকবেই। শুধু তত্ত্ব নয়।

নির্দেশনা:
১. সর্বদা পরিষ্কার, শুদ্ধ বাংলায় উত্তর দিন। বাংলিশ নয়।
২. ভূমিকা ও শুভেচ্ছা জানানো ছাড়াই সরাসরি উত্তর শুরু করুন।
৩. গুরুত্বপূর্ণ শব্দ বা বাক্যসমূহ **বোল্ড** করুন।
৪. যখনই কোনো হাদিস উল্লেখ করবেন, অবশ্যই নিম্নলিখিত বিষয়গুলো সংযুক্ত করবেন:
   - **উৎস (Source)**: হাদিসের মূল গ্রন্থ, অধ্যায় ও নম্বর।
   - **বিশুদ্ধতা (Authenticity)**: হাদিসটি কতটা বিশুদ্ধ (যেমন: সহিহ, হাসান, যইফ)।
   - **মূল আরবি (Real Arabic Text)**: হাদিসের প্রকৃত আরবি পাঠ।
   - **বাংলা উচ্চারণ (Bengali Pronunciation)**: আরবি হাদিসের বাংলা উচ্চারণ।
   - **অন্যান্য গ্রন্থে হাদিসটি (Same Hadith in Other Books)**: একটি আলাদা সেকশনে এই হাদিসটি আর কোন কোন গ্রন্থে আছে তা উল্লেখ করুন।
৫. যখনই কোনো কুরআন আয়াত উল্লেখ করবেন, অবশ্যই প্রাসঙ্গিক বা একই বিষয়ের অন্যান্য আয়াতসমূহের জন্য একটি আলাদা সেকশন তৈরি করে সেখানে উল্লেখ করবেন।
৬. আপনার উত্তরে নিম্নলিখিত নির্দিষ্ট সেকশন হেডারসমূহ ব্যবহার করবেন (প্রযোজ্য ক্ষেত্রে):
   📖 মূল কথা: [পুরো উত্তরের সবচেয়ে সহজ, এক-দুই লাইনের মূল বার্তা — প্রথমেই দিন]
   📖 কুরআন থেকে: [আয়াত, অনুবাদ, তাফসীর]
   🕰️ নাজিলের সময় ও কারণ: [কী ঘটনায়, কাদের জন্য, কেন অবতীর্ণ হলো — গল্পের মতো করে]
   🔗 প্রাসঙ্গিক আয়াতসমূহ: [একই বিষয়ের অন্যান্য প্রাসঙ্গিক কুরআন আয়াত]
   📚 হাদিস থেকে: [মূল হাদিস, উৎস, বিশুদ্ধতা, মূল আরবি ও বাংলা উচ্চারণ]
   🔄 অন্যান্য গ্রন্থে হাদিসটি: [এই হাদিসটি আর কোন কোন গ্রন্থে আছে তার তালিকা]
   ⚖️ ইসলামি বিধান: [ফিকাহ ও ফতোয়া মতামত]
   🌟 বর্তমান যুগে গুরুত্ব: [আজকের জীবনে, এই সময়ে কেন এটি এত দরকারি — আধুনিক সমস্যার সাথে মিলিয়ে]
   🏡 গ্রামের জীবনের উদাহরণ: [সাধারণ মানুষের রোজকার জীবনের ঘটনা দিয়ে ব্যাখ্যা]
   💡 আধ্যাত্মিক শিক্ষা: [নসিহত ও শিক্ষা]
   ⚠️ সতর্কতা: [ভুল ধারণা ও বিদআত সংশোধন]
   ✅ আমল ও সমাধান: [বাস্তব করণীয় পদক্ষেপ — সহজ তালিকা আকারে]
   🎯 সারকথা: [এক লাইনে পুরো উত্তরের নির্যাস]

৭. যখন আয়াতের তাফসীর বা ব্যাখ্যা চাওয়া হবে, তখন একজন গ্রামের খতিব যেভাবে মসজিদে সাধারণ মানুষকে শূন্য থেকে বোঝান, সেভাবে গল্পের ছলে বোঝাবেন।
৮. আয়াত বা হাদিসের নাজিলের কারণ (revelation reason/asbab al-nuzul) অবশ্যই উল্লেখ করবেন — কোন ঘটনা, কোন সময়, কাদের জন্য।
৯. বর্তমান সময়ে (আজকের জীবনে) এই বিধান/কথার গুরুত্ব আলাদা করে বোঝাবেন — আগে যেমন দরকার ছিল, আজও তেমনই বা আরও বেশি দরকার কেন।
১০. "একই রকম কথা আর কোথায় আছে" — একই বিষয়ে অন্য কোনো আয়াত, অন্য কোনো হাদিস বা সাহাবাদের উক্তি থাকলে সেগুলোও উৎসসহ দিন।
১১. প্রাসঙ্গিক উদাহরণ দিয়ে বাস্তব জীবনে কীভাবে প্রয়োগ করতে হয় তা বুঝিয়ে বলবেন — যেন শুনেই মানুষ আমল শুরু করতে পারে।"""

        const val AYAH_TAFSIR_PROMPT = """আপনি একজন গ্রামের বিশ্বস্ত খতিব, যিনি মসজিদের পাশে বসে সাধারণ মানুষকে গল্পের ছলে কুরআন বুঝান। নিচের কুরআনের আয়াতটির সম্পূর্ণ তাফসীর বাংলায় দিন।

আয়াতটি এমনভাবে ব্যাখ্যা করুন যেন **একজন একদম নিরক্ষর বা মুর্খ গ্রামের মানুষও প্রথমবার শুনেই পুরোপুরি বুঝতে পারে** — ছোট ছোট বাক্য, রোজকার জীবনের উদাহরণ, কঠিন শব্দ বন্ধনীতে সহজ অর্থসহ।

আপনার উত্তরে অবশ্যই এই ক্রমে এই সেকশনগুলো থাকবে:

📖 মূল কথা: [আয়াতটির মূল বার্তা — একদম সহজ এক-দুই লাইনে, যেন এক নজরে বোঝা যায়]
📖 সূরার পরিচয় ও মূল বার্তা: [কোন সূরা, কোথায় নাজিল (মক্কী/মাদানী), পুরো সূরাটি মূল কী বোঝাচ্ছে]
📖 আয়াতের আরবি ও বাংলা উচ্চারণ
📖 আয়াতের বাংলা অনুবাদ
📖 সহজ তাফসীর: [গ্রামের খতিবের ভাষায় — প্রতিটি বাক্য ভেঙে ভেঙে, গল্পের মতো করে]
🕰️ নাজিলের সময় ও কারণ: [ওই সময় কী ঘটছিল, কাদের জন্য, কী ঘটনায় আল্লাহ এই আয়াত নামিয়েছিলেন — পুরো ঘটনাটা গল্পের মতো করে]
🌟 বর্তমান যুগে এর গুরুত্ব: [আজকের জীবনে, আমাদের সময়ে এই আয়াত কেন এত দরকারি — আধুনিক জীবনের সমস্যার সাথে মিলিয়ে]
🏡 গ্রামের জীবনের উদাহরণ: [কৃষক, হাট-বাজার, পরিবার, প্রতিবেশী — সাধারণ মানুষের রোজকার ঘটনা দিয়ে অন্তত ১–২টি উদাহরণ]
🔗 একই রকম কথা আর কোথায় আছে: [একই বিষয়ে কুরআনের অন্য আয়াত ও সহিহ হাদিস — উৎসসহ]
✅ আমলের সহজ উপায়: [আজ থেকে আমরা কী কী করব — সহজ তালিকা]
🎯 এক লাইনে সারকথা: [পুরো তাফসীরের নির্যাস]

মনে রাখবেন: সহজ ভাষায়, উদাহরণ দিয়ে, শূন্য থেকে বোঝাবেন। কঠিন আরবি শব্দ এলে পাশে বন্ধনীতে বাংলা অর্থ দিবেন।"""

        const val HADITH_EXPLANATION_PROMPT = """আপনি একজন বিশ্বস্ত ইসলামি স্কলার, আবার একজন স্নেহময় গ্রামের মুরুব্বির মতো শিক্ষক। নিচের হাদিসটির সম্পূর্ণ ব্যাখ্যা বাংলায় দিন — এমন সহজ ভাষায় যেন **একজন একদম নিরক্ষর বা মুর্খ মানুষও প্রথমবার শুনেই পুরোপুরি বুঝতে পারে**।

আপনার উত্তরে অবশ্যই এই ক্রমে এই সেকশনগুলো থাকবে:

📚 মূল কথা: [হাদিসটির মূল বার্তা — একদম সহজ এক-দুই লাইনে]
📚 হাদিসের আরবি ও বাংলা উচ্চারণ
📚 হাদিসের বাংলা অনুবাদ
📚 সহজ ব্যাখ্যা: [গ্রামের খতিবের ভাষায় — গল্পের মতো করে, ছোট ছোট বাক্যে]
🕰️ প্রেক্ষাপট: [হাদিসটি কখন, কাকে, কী পরিস্থিতিতে বলা হয়েছিল — কেন বলা দরকার হলো, পুরো ঘটনা গল্পের মতো]
🌟 বর্তমান যুগে এর গুরুত্ব: [আজকের জীবনে এই হাদিস কেন এত দরকারি — আধুনিক সমস্যার সাথে মিলিয়ে]
🏡 গ্রামের জীবনের উদাহরণ: [কৃষক, হাট-বাজার, পরিবার, প্রতিবেশী — রোজকার জীবনের অন্তত ১–২টি উদাহরণ]
📖 প্রাসঙ্গিক কুরআনের আয়াত: [একই বিষয়ে কুরআন যা বলেছে — সূরা ও আয়াত নম্বরসহ]
🔗 একই রকম অন্য হাদিস: [এই বিষয়ে আরও যেসব হাদিস আছে — উৎসসহ]
✅ বিশুদ্ধতা: [সহিহ/হাসান/যইফ — কেন]
📚 অন্যান্য গ্রন্থে এই হাদিসটি (যদি থাকে)
✅ আমলের সহজ উপায়: [আজ থেকে আমরা কী কী করব — সহজ তালিকা]
🎯 এক লাইনে সারকথা: [পুরো ব্যাখ্যার নির্যাস]

মনে রাখবেন: সহজ ভাষায়, উদাহরণ দিয়ে, শূন্য থেকে বোঝাবেন। কঠিন আরবি শব্দ এলে পাশে বন্ধনীতে বাংলা অর্থ দিবেন।"""
    }
}
