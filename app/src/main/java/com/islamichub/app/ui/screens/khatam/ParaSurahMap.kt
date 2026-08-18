package com.islamichub.app.ui.screens.khatam

/**
 * Quran is divided into 30 para (juz) for traditional khatam reading.
 * Each para contains specific surahs (or parts of surahs).
 *
 * This mapping uses starting surah + ayah for each para — the surah list
 * shows which surah begins in each para. Users following para-wise khatam
 * read 1 para per day = 30 days for full khatam.
 *
 * Sources: Standard 30-para Juz division (tajweedcompanies/mushaf-al-madina)
 */
object ParaSurahMap {

    /** 30 paras — each entry = (paraNumber, startingSurahNumber, startingAyah, startingSurahName) */
    val paras: List<Triple<Int, Pair<Int, Int>>> = listOf(
        Triple(1, 1 to 1),     // Al-Fatiha start
        Triple(2, 2 to 142),  // Al-Baqarah ayah 142
        Triple(3, 2 to 253),   // Al-Baqarah ayah 253
        Triple(4, 3 to 92),    // Al-Imran ayah 92
        Triple(5, 4 to 24),    // An-Nisa ayah 24
        Triple(6, 4 to 148),   // An-Nisa ayah 148
        Triple(7, 5 to 82),    // Al-Maidah ayah 82
        Triple(8, 6 to 111),   // Al-An'am ayah 111
        Triple(9, 7 to 88),   // Al-A'raf ayah 88
        Triple(10, 8 to 41),  // Al-Anfal ayah 41
        Triple(11, 9 to 93),  // At-Tawbah ayah 93
        Triple(12, 11 to 6),  // Hud ayah 6
        Triple(13, 12 to 53), // Yusuf ayah 53
        Triple(14, 15 to 1),  // Al-Hijr start
        Triple(15, 17 to 1),  // Al-Isra start
        Triple(16, 18 to 75), // Al-Kahf ayah 75
        Triple(17, 21 to 1),  // Al-Anbiya start
        Triple(18, 23 to 1),  // Al-Mu'minun start
        Triple(19, 25 to 21), // Al-Furqan ayah 21
        Triple(20, 27 to 56), // An-Naml ayah 56
        Triple(21, 29 to 46), // Al-Ankabut ayah 46
        Triple(22, 33 to 31), // Al-Ahzab ayah 31
        Triple(23, 36 to 28), // Ya-Sin ayah 28
        Triple(24, 39 to 32), // Az-Zumar ayah 32
        Triple(25, 41 to 47), // Fussilat ayah 47
        Triple(26, 46 to 1),  // Al-Ahqaf start
        Triple(27, 51 to 31), // Adh-Dhariyat ayah 31
        Triple(28, 58 to 1),  // Al-Mujadila start
        Triple(29, 67 to 1),  // Al-Mulk start
        Triple(30, 78 to 1)   // An-Naba start
    )

    /** Get the para number for a given surah (using first surah of each para) */
    fun paraForSurah(surahNumber: Int): Int {
        // Find the latest para whose starting surah <= surahNumber
        var result = 1
        for ((paraNum, startRef) in paras) {
            if (startRef.first <= surahNumber) {
                result = paraNum
            } else break
        }
        return result
    }

    /** Get all surahs (1-114) grouped by para (1-30) */
    fun surahsByPara(): Map<Int, List<Int>> {
        val map = mutableMapOf<Int, MutableList<Int>>()
        for (surahNum in 1..114) {
            val para = paraForSurah(surahNum)
            map.getOrPut(para) { mutableListOf() }.add(surahNum)
        }
        return map
    }

    /** Get the starting surah info for a para */
    fun paraStart(para: Int): Pair<Int, Int>? = paras.find { it.first == para }?.second

    /** Total paras: 30 */
    val totalParas: Int = 30
}
