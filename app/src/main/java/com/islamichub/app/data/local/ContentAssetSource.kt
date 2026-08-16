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
 *
 * Files (all in assets/data/):
 *  - misconceptions.json     — 300 misconceptions, categorized
 *  - questions.json          — categorized Q&A
 *  - ans.json                — Answer database (345 KB)
 *  - namaz_shikkha.json      — Complete Namaz learning (Hanafi/Shafii, Male/Female)
 *  - namaz_extras.json       — Jumma, Janaza, Eid, Nafl + small surahs
 *  - duas_extended.json      — Extended dua collection
 *  - stories.json            — Islamic stories (Prophets, Khalifas, Me'raj, Sirat)
 *  - kalima.json             — 6 Kalimas
 *  - hadith_extended.json    — Extended hadith collection
 *  - asmaul_husna_extended.json — 99 names with extended metadata
 *  - location_data.json      — Bangladesh locations
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

// ─── Q&A ───────────────────────────────────────────────────────────────────

data class QuestionData(
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("categories") val categories: Map<String, QuestionCategory>?
) {
    /**
     * The questions.json file uses category keys at top level (e.g., "namaz", "rojha").
     * Each value has {name, icon, color, questions: [...]}
     */
}

data class QuestionCategory(
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("questions") val questions: List<String>
)

// ─── Namaz Shikkha ─────────────────────────────────────────────────────────

data class NamazShikkhaData(
    @SerializedName("metadata") val metadata: NamazShikkhaMetadata?,
    @SerializedName("madhhab_options") val madhhabOptions: List<String>?,
    @SerializedName("default_madhhab") val defaultMadhhab: String?,
    @SerializedName("gender_options") val genderOptions: List<String>?,
    @SerializedName("default_gender") val defaultGender: String?,
    @SerializedName("common_steps") val commonSteps: Map<String, NamazStep>?,
    @SerializedName("prayers") val prayers: Map<String, NamazPrayer>?
)

data class NamazShikkhaMetadata(
    @SerializedName("version") val version: String?,
    @SerializedName("last_updated") val lastUpdated: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("source") val source: String?
)

data class NamazStep(
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("name_en") val nameEn: String?,
    @SerializedName("content") val content: NamazStepContent?,
    @SerializedName("audio") val audio: String?
)

data class NamazStepContent(
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("translation") val translation: String?,
    @SerializedName("bangla") val bangla: String?
)

data class NamazPrayer(
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("name_en") val nameEn: String?,
    @SerializedName("rakat") val rakat: Int?,
    @SerializedName("steps") val steps: List<String>?,
    @SerializedName("wajib") val wajib: List<String>?,
    @SerializedName("fard") val fard: List<String>?,
    @SerializedName("sunnah") val sunnah: List<String>?
)

// ─── Namaz Extras (Jumma, Janaza, Eid, Nafl, etc.) ─────────────────────────

data class NamazExtrasData(
    @SerializedName("namazSurahs") val namazSurahs: List<NamazSurah>?,
    @SerializedName("extraPrayers") val extraPrayers: Map<String, ExtraPrayer>?
)

data class NamazSurah(
    @SerializedName("id") val id: String,
    @SerializedName("name_bn") val nameBn: String,
    @SerializedName("ayat_count") val ayatCount: Int?,
    @SerializedName("content") val content: NamazSurahContent?
)

data class NamazSurahContent(
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("translation") val translation: String?
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
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("translation") val translation: String?
)

// ─── Extended Duas ─────────────────────────────────────────────────────────

data class ExtendedDuasData(
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("categories") val categories: List<ExtendedDuaCategory>?
)

data class ExtendedDuaCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("duas") val duas: List<ExtendedDua>?
)

data class ExtendedDua(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("title_bn") val titleBn: String?,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("translation") val translation: String?,
    @SerializedName("translation_bn") val translationBn: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("audio") val audio: String?
)

// ─── ANS data (Q&A answers) ────────────────────────────────────────────────

data class AnsData(
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("categories") val categories: List<AnsCategory>?
)

data class AnsCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("qa") val qa: List<AnsQA>?
)

data class AnsQA(
    @SerializedName("id") val id: String,
    @SerializedName("q") val question: String?,
    @SerializedName("q_bn") val questionBn: String?,
    @SerializedName("a") val answer: String?,
    @SerializedName("a_bn") val answerBn: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("grade") val grade: String?
)
