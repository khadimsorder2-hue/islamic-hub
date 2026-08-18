package com.islamichub.app.data.repo

/**
 * Free LLM model presets — pre-filled defaults so user doesn't have to remember
 * exact model names.
 *
 * Each provider has a list of currently-free models. User can also type a
 * custom model name in Settings.
 *
 * Last updated: 2026-08-18
 * Note: Gemini API v2 models use "gemini-2.x" naming. The API endpoint
 * is generativelanguage.googleapis.com/v1beta for all models.
 */
data class AIModelPreset(
    val id: String,
    val provider: String,
    val modelName: String,
    val displayName: String,
    val displayNameBn: String,
    val descriptionBn: String,
    val baseUrl: String,
    val isFree: Boolean = true,
    val contextWindow: String = "—",
    val recommended: Boolean = false
)

object AIModelPresets {

    /** Google Gemini — free tier (generativelanguage.googleapis.com/v1beta) */
    val GEMINI_MODELS: List<AIModelPreset> = listOf(
        AIModelPreset(
            id = "gemini-2.5-flash",
            provider = "gemini",
            modelName = "gemini-2.5-flash",
            displayName = "Gemini 2.5 Flash",
            displayNameBn = "জেমিনাই ২.৫ ফ্ল্যাশ",
            descriptionBn = "দ্রুত, সাশ্রয়ী, বহুমুখী — সাধারণ ব্যবহারের জন্য সেরা। ফ্রি টিয়ারে ১৫ RPM।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens",
            recommended = true
        ),
        AIModelPreset(
            id = "gemini-2.5-flash-lite",
            provider = "gemini",
            modelName = "gemini-2.5-flash-lite",
            displayName = "Gemini 2.5 Flash Lite",
            displayNameBn = "জেমিনাই ২.৫ ফ্ল্যাশ লাইট",
            descriptionBn = "সবচেয়ে সাশ্রয়ী — সাধারণ প্রশ্নের জন্য যথেষ্ট। ফ্রি টিয়ারে ৩০ RPM।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        ),
        AIModelPreset(
            id = "gemini-2.5-flash-8b",
            provider = "gemini",
            modelName = "gemini-2.5-flash-8b",
            displayName = "Gemini 2.5 Flash (8B)",
            displayNameBn = "জেমিনাই ২.৫ ফ্ল্যাশ ৮বি",
            descriptionBn = "ছোট মডেল — দ্রুত উত্তর প্রয়োজনে ব্যবহার করুন।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        ),
        AIModelPreset(
            id = "gemini-2.5-pro",
            provider = "gemini",
            modelName = "gemini-2.5-pro",
            displayName = "Gemini 2.5 Pro",
            displayNameBn = "জেমিনাই ২.৫ প্রো",
            descriptionBn = "সবচেয়ে শক্তিশালী — কঠিন প্রশ্নের জন্য। ফ্রি টিয়ারে সীমিত (৫ RPM)।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "2M tokens",
            isFree = false
        ),
        AIModelPreset(
            id = "gemini-2.0-flash",
            provider = "gemini",
            modelName = "gemini-2.0-flash",
            displayName = "Gemini 2.0 Flash",
            displayNameBn = "জেমিনাই ২.০ ফ্ল্যাশ",
            descriptionBn = "পূর্ববর্তী স্থিতিশীল ভার্সন — নির্ভরযোগ্য।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        ),
        AIModelPreset(
            id = "gemini-2.0-flash-lite",
            provider = "gemini",
            modelName = "gemini-2.0-flash-lite",
            displayName = "Gemini 2.0 Flash Lite",
            displayNameBn = "জেমিনাই ২.০ ফ্ল্যাশ লাইট",
            descriptionBn = "পূর্ববর্তী লাইট ভার্সন — খুব দ্রুত।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        ),
        AIModelPreset(
            id = "gemini-1.5-flash",
            provider = "gemini",
            modelName = "gemini-1.5-flash",
            displayName = "Gemini 1.5 Flash (legacy)",
            displayNameBn = "জেমিনাই ১.৫ ফ্ল্যাশ (পুরোনো)",
            descriptionBn = "পুরোনো ভার্সন — পরবর্তী ভার্সন পাওয়া না গেলে ব্যবহার করুন।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        )
    )

    /** OpenRouter — free models (openrouter.ai/api/v1) */
    val OPENROUTER_FREE_MODELS: List<AIModelPreset> = listOf(
        AIModelPreset(
            id = "google/gemini-2.5-flash-lite-preview:free",
            provider = "openrouter",
            modelName = "google/gemini-2.5-flash-lite-preview:free",
            displayName = "Gemini 2.5 Flash Lite (free)",
            displayNameBn = "জেমিনাই ২.৫ ফ্ল্যাশ লাইট (ফ্রি)",
            descriptionBn = "OpenRouter-এর ফ্রি টিয়ার — দৈনিক সীমিত রিকোয়েস্ট।",
            baseUrl = "https://openrouter.ai/api/v1",
            contextWindow = "1M tokens",
            recommended = true
        ),
        AIModelPreset(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            provider = "openrouter",
            modelName = "meta-llama/llama-3.3-70b-instruct:free",
            displayName = "Llama 3.3 70B (free)",
            displayNameBn = "লামা ৩.৩ ৭০বি (ফ্রি)",
            descriptionBn = "Meta-র শক্তিশালী মডেল — ফ্রি।",
            baseUrl = "https://openrouter.ai/api/v1",
            contextWindow = "128K tokens"
        ),
        AIModelPreset(
            id = "deepseek/deepseek-chat-v3-0324:free",
            provider = "openrouter",
            modelName = "deepseek/deepseek-chat-v3-0324:free",
            displayName = "DeepSeek V3 (free)",
            displayNameBn = "ডিপসিক ভি৩ (ফ্রি)",
            descriptionBn = "চমৎকার যুক্তি দেওয়ার ক্ষমতা — ফ্রি।",
            baseUrl = "https://openrouter.ai/api/v1",
            contextWindow = "128K tokens"
        ),
        AIModelPreset(
            id = "qwen/qwen-2.5-72b-instruct:free",
            provider = "openrouter",
            modelName = "qwen/qwen-2.5-72b-instruct:free",
            displayName = "Qwen 2.5 72B (free)",
            displayNameBn = "কিউয়েন ২.৫ ৭২বি (ফ্রি)",
            descriptionBn = "Alibaba-র মডেল — মাল্টিলিঙ্গুয়াল।",
            baseUrl = "https://openrouter.ai/api/v1",
            contextWindow = "32K tokens"
        ),
        AIModelPreset(
            id = "mistralai/mistral-7b-instruct:free",
            provider = "openrouter",
            modelName = "mistralai/mistral-7b-instruct:free",
            displayName = "Mistral 7B (free)",
            displayNameBn = "মিস্ট্রাল ৭বি (ফ্রি)",
            descriptionBn = "দ্রুত, ছোট মডেল — সাধারণ প্রশ্নের জন্য।",
            baseUrl = "https://openrouter.ai/api/v1",
            contextWindow = "32K tokens"
        )
    )

    /** All presets grouped by provider */
    fun all(): List<AIModelPreset> = GEMINI_MODELS + OPENROUTER_FREE_MODELS

    /** Get presets for a specific provider */
    fun forProvider(provider: String): List<AIModelPreset> = when (provider) {
        "gemini" -> GEMINI_MODELS
        "openrouter" -> OPENROUTER_FREE_MODELS
        else -> emptyList()
    }

    /** Recommended preset for a provider */
    fun recommended(provider: String): AIModelPreset? = forProvider(provider).firstOrNull { it.recommended }
}
