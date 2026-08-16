package com.islamichub.app.data.repo

import com.islamichub.app.data.model.NameOfAllah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NamesRepository(private val names: List<NameOfAllah>) {

    suspend fun all(): List<NameOfAllah> = withContext(Dispatchers.IO) { names }

    suspend fun search(query: String): List<NameOfAllah> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext names
        val q = query.lowercase()
        names.filter {
            it.transliteration.lowercase().contains(q) ||
                it.englishMeaning.lowercase().contains(q) ||
                it.bengaliMeaning.contains(query)
        }
    }
}
