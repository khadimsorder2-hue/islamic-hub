package com.islamichub.app.data.repo

import com.islamichub.app.data.model.DhikrOption
import com.islamichub.app.data.model.Dua
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DuaRepository(
    private val duas: List<Dua>,
    private val dhikrOptions: List<DhikrOption>
) {

    suspend fun allDuas(): List<Dua> = withContext(Dispatchers.IO) { duas }

    suspend fun duaOfDay(): Dua = withContext(Dispatchers.IO) {
        val idx = (System.currentTimeMillis() / 86_400_000L).toInt().mod(duas.size)
        duas[idx]
    }

    suspend fun dhikrOptions(): List<DhikrOption> = withContext(Dispatchers.IO) { dhikrOptions }
}
