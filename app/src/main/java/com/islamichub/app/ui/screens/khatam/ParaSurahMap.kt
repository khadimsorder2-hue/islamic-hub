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

    /** A single para definition: number + starting surah + starting ayah */
    data class ParaInfo(
        val number: Int,
        val startSurah: Int,
        val startAyah: Int
    )

    /** 30 paras — each entry = (paraNumber, startingSurahNumber, startingAyah) */
    val paras: List<ParaInfo> = listOf(
        ParaInfo(1, 1, 1),     // Al-Fatiha start
        ParaInfo(2, 2, 142),   // Al-Baqarah ayah 142
        ParaInfo(3, 2, 253),   // Al-Baqarah ayah 253
        ParaInfo(4, 3, 92),    // Al-Imran ayah 92
        ParaInfo(5, 4, 24),    // An-Nisa ayah 24
        ParaInfo(6, 4, 148),   // An-Nisa ayah 148
        ParaInfo(7, 5, 82),    // Al-Maidah ayah 82
        ParaInfo(8, 6, 111),   // Al-An'am ayah 111
        ParaInfo(9, 7, 88),    // Al-A'raf ayah 88
        ParaInfo(10, 8, 41),   // Al-Anfal ayah 41
        ParaInfo(11, 9, 93),   // At-Tawbah ayah 93
        ParaInfo(12, 11, 6),   // Hud ayah 6
        ParaInfo(13, 12, 53),  // Yusuf ayah 53
        ParaInfo(14, 15, 1),   // Al-Hijr start
        ParaInfo(15, 17, 1),   // Al-Isra start
        ParaInfo(16, 18, 75),  // Al-Kahf ayah 75
        ParaInfo(17, 21, 1),   // Al-Anbiya start
        ParaInfo(18, 23, 1),   // Al-Mu'minun start
        ParaInfo(19, 25, 21), // Al-Furqan ayah 21
        ParaInfo(20, 27, 56),  // An-Naml ayah 56
        ParaInfo(21, 29, 46),  // Al-Ankabut ayah 46
        ParaInfo(22, 33, 31),  // Al-Ahzab ayah 31
        ParaInfo(23, 36, 28),  // Ya-Sin ayah 28
        ParaInfo(24, 39, 32),  // Az-Zumar ayah 32
        ParaInfo(25, 41, 47),  // Fussilat ayah 47
        ParaInfo(26, 46, 1),   // Al-Ahqaf start
        ParaInfo(27, 51, 31),  // Adh-Dhariyat ayah 31
        ParaInfo(28, 58, 1),   // Al-Mujadila start
        ParaInfo(29, 67, 1),   // Al-Mulk start
        ParaInfo(30, 78, 1)    // An-Naba start
    )

    /** Get the para number for a given surah (using first surah of each para) */
    fun paraForSurah(surahNumber: Int): Int {
        var result = 1
        for (p in paras) {
            if (p.startSurah <= surahNumber) {
                result = p.number
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
    fun paraStart(para: Int): ParaInfo? = paras.find { it.number == para }

    /** Total paras: 30 */
    const val totalParas: Int = 30
}
