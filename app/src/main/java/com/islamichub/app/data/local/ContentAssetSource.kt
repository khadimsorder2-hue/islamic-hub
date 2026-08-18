package com.islamichub.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Loads extended content data from bundled JSON assets.
 * Fixed to match actual JSON structure from web source.
 */
class ContentAssetSource(private val context: Context) {

    private val gson = Gson()
    private val cache = mutableMapOf<String, Any>()

    suspend fun <T> loadAsset(fileName: String, type: Class<T>): T = withContext(Dispatchers.IO) {
        @Suppress("UNCHECKED_CAST")
        cache[fileName]?.let { return@withContext it as T }
        val text = readAsset("data/$fileName")
        val parsed = try {
            gson.fromJson(text, type)
        } catch (e: JsonSyntaxException) {
            null
        } ?: throw IOException("Failed to parse data/$fileName")
        cache[fileName] = parsed!!
        parsed
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

// ─── Misconceptions ────────────────────────────────────────────────────────

data class MisconceptionsData(
    @SerializedName("metadata") val metadata: MisconceptionsMetadata?,
    @SerializedName("categories") val categories: List<MisconceptionCategory>
)

data class MisconceptionsMetadata(
    @SerializedName("version") val version: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("total_items") val totalItems: Int?,
    @SerializedName("last_updated") val lastUpdated: String?
)

data class MisconceptionCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("questions") val questions: List<MisconceptionItem>
)

data class MisconceptionItem(
    @SerializedName("id") val id: String,
    @SerializedName("q") val question: String,
    @SerializedName("a") val answer: String?
)

// ─── Q&A (ans.json) — web source format: {categoryKey: {name, icon, color, questions: [{q, a, ...}]}} ───

data class AnsData(
    @SerializedName("namaz") val namaz: AnsCategory? = null,
    @SerializedName("roza") val roza: AnsCategory? = null,
    @SerializedName("hajj") val hajj: AnsCategory? = null,
    @SerializedName("zakat") val zakat: AnsCategory? = null,
    @SerializedName("quran") val quran: AnsCategory? = null,
    @SerializedName("hadith") val hadith: AnsCategory? = null,
    @SerializedName("wudu") val wudu: AnsCategory? = null,
    @SerializedName("ghusl") val ghusl: AnsCategory? = null,
    @SerializedName("tayammum") val tayammum: AnsCategory? = null,
    @SerializedName("lifestyle") val lifestyle: AnsCategory? = null,
    @SerializedName("dua") val dua: AnsCategory? = null
) {
    /** Returns all categories as a list with their keys. */
    fun toList(): List<Pair<String, AnsCategory>> {
        return listOf(
            "namaz" to namaz, "roza" to roza, "hajj" to hajj, "zakat" to zakat,
            "quran" to quran, "hadith" to hadith, "wudu" to wudu, "ghusl" to ghusl,
            "tayammum" to tayammum, "lifestyle" to lifestyle, "dua" to dua
        ).filter { it.second != null } as List<Pair<String, AnsCategory>>
    }
}

data class AnsCategory(
    @SerializedName("name") val name: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("questions") val questions: List<AnsQA>? = null
)

data class AnsQA(
    @SerializedName("q") val question: String?,
    @SerializedName("a") val answer: String?,
    @SerializedName("ref") val reference: String?,
    @SerializedName("arabic") val arabic: String?
)

// ─── Extended Duas — web source format: {categories: [{id, name, icon, color}], duas: [{id, category, title, arabic, ...}]} ───

data class ExtendedDuasData(
    @SerializedName("categories") val categories: List<ExtendedDuaCategory>?,
    @SerializedName("duas") val duas: List<ExtendedDua>?
) {
    /** Groups duas by their category field. */
    fun getGroupedDuas(): Map<String, List<ExtendedDua>> {
        val allDuas = duas ?: emptyList()
        return allDuas.groupBy { it.category ?: "misc" }
    }
}

data class ExtendedDuaCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?
)

data class ExtendedDua(
    @SerializedName("id") val id: String,
    @SerializedName("category") val category: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("bangla") val bangla: String?,
    @SerializedName("ref") val reference: String?,
    @SerializedName("virtue") val virtue: String?
)

// ─── Namaz Shikkha (full from web source) ─────────────────────────────────

data class NamazShikkhaData(
    @SerializedName("metadata") val metadata: NamazShikkhaMetadata?,
    @SerializedName("madhhab_options") val madhhabOptions: List<String>?,
    @SerializedName("default_madhhab") val defaultMadhhab: String?,
    @SerializedName("gender_options") val genderOptions: List<String>?,
    @SerializedName("default_gender") val defaultGender: String?,
    @SerializedName("common_steps") val commonSteps: Map<String, NamazStep>?,
    @SerializedName("categories") val categories: List<NamazShikkhaCategory>?
)

data class NamazShikkhaMetadata(
    @SerializedName("version") val version: String?,
    @SerializedName("last_updated") val lastUpdated: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("source") val source: String?
)

data class NamazShikkhaCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name_bn") val nameBn: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("prayers") val prayers: List<NamazShikkhaPrayer>?
)

data class NamazStep(
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("content") val content: NamazStepContent?,
    @SerializedName("gender_notes") val genderNotes: Map<String, String>?,
    @SerializedName("audio_url") val audioUrl: String?
)

data class NamazStepContent(
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("translation") val translation: String?
)

data class NamazShikkhaPrayer(
    @SerializedName("id") val id: String,
    @SerializedName("name_bn") val nameBn: String,
    @SerializedName("type") val type: String?,
    @SerializedName("total_rakats") val rakat: Int?,
    @SerializedName("niyyah") val niyyah: String?
)

// ─── Namaz Extras ──────────────────────────────────────────────────────────

data class NamazExtrasData(
    @SerializedName("namazSurahs") val namazSurahs: List<NamazSurah>?,
    @SerializedName("extraPrayers") val extraPrayers: Map<String, ExtraPrayer>?
)

data class NamazSurah(
    @SerializedName("id") val id: String,
    @SerializedName("name_bn") val nameBn: String,
    @SerializedName("ayat_count") val ayatCount: Int?,
    @SerializedName("content") val content: NamazSurahContent?,
    @SerializedName("audio_url") val audioUrl: String?,
    @SerializedName("pronunciation_bn") val pronunciationBn: String?
)

data class NamazSurahContent(
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("translation") val translation: String?,
    @SerializedName("bangla") val bangla: String?
)

data class ExtraPrayer(
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("name_en") val nameEn: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("rakat") val rakat: Int?,
    @SerializedName("time") val time: String?,
    @SerializedName("method") val method: String?
)

// ─── Stories ───────────────────────────────────────────────────────────────

data class StoriesData(
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("prophets") val prophets: List<StoryItem>?,
    @SerializedName("khalifas") val khalifas: List<StoryItem>?,
    @SerializedName("miraj") val miraj: StoryItem?,
    @SerializedName("sirat") val sirat: StoryItem?
)

data class StoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("title_bn") val titleBn: String?,
    @SerializedName("period") val period: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("story") val story: String?,
    @SerializedName("description") val description: String?
)

// ─── Kalima ────────────────────────────────────────────────────────────────

data class KalimaData(
    @SerializedName("kalimas") val kalimas: List<Kalima>?
)

data class Kalima(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("nameEn") val nameEn: String?,
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("banglaPronunciation") val banglaPronunciation: String?,
    @SerializedName("bangla") val bangla: String?,
    @SerializedName("translation") val translation: String?,
    @SerializedName("meaning") val meaning: String?,
    @SerializedName("explanation") val explanation: String?,
    @SerializedName("audioFile") val audioFile: String?
)
