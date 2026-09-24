package com.islamichub.app.data.repo

/**
 * Free LLM model presets — pre-filled defaults so user doesn't have to remember
 * exact model names.
 *
 * Last updated: 2026-09-25 — v5.7.0 switches to the Gemini 3 series.
 * All Gemini models use the v1beta endpoint:
 *   https://generativelanguage.googleapis.com/v1beta
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

    /** Google Gemini — v5.7.0: full Gemini 3 series */
    val GEMINI_MODELS: List<AIModelPreset> = listOf(
        AIModelPreset(
            id = "gemini-3-flash",
            provider = "gemini",
            modelName = "gemini-3-flash",
            displayName = "Gemini 3 Flash",
            displayNameBn = "জেমিনাই ৩ ফ্ল্যাশ",
            descriptionBn = "দ্রুত, সাশ্রয়ী, বহুমুখী — সাধারণ ব্যবহারের জন্য সেরা। (ডিফল্ট)",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens",
            recommended = true
        ),
        AIModelPreset(
            id = "gemini-3-pro",
            provider = "gemini",
            modelName = "gemini-3-pro",
            displayName = "Gemini 3 Pro",
            displayNameBn = "জেমিনাই ৩ প্রো",
            descriptionBn = "সবচেয়ে শক্তিশালী — গভীর তাফসীর ও কঠিন প্রশ্নের জন্য।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        ),
        AIModelPreset(
            id = "gemini-3-flash-lite",
            provider = "gemini",
            modelName = "gemini-3-flash-lite",
            displayName = "Gemini 3 Flash Lite",
            displayNameBn = "জেমিনাই ৩ ফ্ল্যাশ লাইট",
            descriptionBn = "সবচেয়ে সাশ্রয়ী — সাধারণ প্রশ্নের জন্য যথেষ্ট।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        ),
        AIModelPreset(
            id = "gemini-3-pro-preview",
            provider = "gemini",
            modelName = "gemini-3-pro-preview",
            displayName = "Gemini 3 Pro (Preview)",
            displayNameBn = "জেমিনাই ৩ প্রো (প্রিভিউ)",
            descriptionBn = "প্রো মডেলের প্রিভিউ সংস্করণ — স্থায়ী নাম কাজ না করলে ব্যবহার করুন।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        ),
        AIModelPreset(
            id = "gemini-3-flash-preview",
            provider = "gemini",
            modelName = "gemini-3-flash-preview",
            displayName = "Gemini 3 Flash (Preview)",
            displayNameBn = "জেমিনাই ৩ ফ্ল্যাশ (প্রিভিউ)",
            descriptionBn = "ফ্ল্যাশ মডেলের প্রিভিউ সংস্করণ।",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            contextWindow = "1M tokens"
        )
    )

    /** OpenRouter — free models (openrouter.ai/api/v1) */
    val OPENROUTER_FREE_MODELS: List<AIModelPreset> = listOf(
        AIModelPreset(
            id = "google/gemini-3-flash-preview:free",
            provider = "openrouter",
            modelName = "google/gemini-3-flash-preview:free",
            displayName = "Gemini 3 Flash (free)",
            displayNameBn = "জেমিনাই ৩ ফ্ল্যাশ (ফ্রি)",
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
