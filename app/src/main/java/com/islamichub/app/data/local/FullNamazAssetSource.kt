package com.islamichub.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads full namaz data from web source converted JSON.
 * Source: namazshikkha-data.js → namaz_shikkha_full.json (72KB)
 * Source: extended-namaz-data.js → namaz_extended_full.json (30KB)
 */
class FullNamazAssetSource(private val context: Context) {

    private val gson = Gson()

    suspend fun loadFullNamazData(): FullNamazData = withContext(Dispatchers.IO) {
        val text = context.assets.open("data/namaz_shikkha_full.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        gson.fromJson(text, FullNamazData::class.java)
    }

    suspend fun loadExtendedNamazData(): ExtendedNamazData = withContext(Dispatchers.IO) {
        val text = context.assets.open("data/namaz_extended_full.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        gson.fromJson(text, ExtendedNamazData::class.java)
    }
}

// ─── Full namaz data ──────────────────────────────────────────────────────────

data class FullNamazData(
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("madhhab_options") val madhhabOptions: List<String>?,
    @SerializedName("default_madhhab") val defaultMadhhab: String?,
    @SerializedName("gender_options") val genderOptions: List<String>?,
    @SerializedName("default_gender") val defaultGender: String?,
    @SerializedName("common_steps") val commonSteps: Map<String, FullNamazStep>?,
    @SerializedName("categories") val categories: List<FullNamazCategory>?
)

data class FullNamazCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name_bn") val nameBn: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("prayers") val prayers: List<FullNamazPrayer>?
)

data class FullNamazPrayer(
    @SerializedName("id") val id: String,
    @SerializedName("name_bn") val nameBn: String,
    @SerializedName("type") val type: String?,
    @SerializedName("total_rakats") val totalRakats: Int?,
    @SerializedName("niyyah") val niyyah: String?
)

data class FullNamazStep(
    @SerializedName("name_bn") val nameBn: String?,
    @SerializedName("content") val content: FullNamazStepContent?,
    @SerializedName("gender_notes") val genderNotes: Map<String, String>?,
    @SerializedName("audio_url") val audioUrl: String?
)

data class FullNamazStepContent(
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("translation") val translation: String?
)

// ─── Extended namaz data ──────────────────────────────────────────────────────

data class ExtendedNamazData(
    @SerializedName("additional_namaz") val additionalNamaz: List<ExtendedNamazItem>?
)

data class ExtendedNamazItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("rakats") val rakats: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("importance") val importance: String?,
    @SerializedName("steps") val steps: List<ExtendedNamazStep>?
)

data class ExtendedNamazStep(
    @SerializedName("step") val step: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("pronunciation") val pronunciation: String?,
    @SerializedName("meaning") val meaning: String?,
    @SerializedName("audio") val audio: String?
)
