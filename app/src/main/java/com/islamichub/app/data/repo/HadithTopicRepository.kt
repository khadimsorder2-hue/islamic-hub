package com.islamichub.app.data.repo

import com.islamichub.app.data.local.HadithAssetSource
import com.islamichub.app.data.local.HadithCollectionMeta
import com.islamichub.app.data.local.HadithJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hadith Topic Study — uses bundled 24,424 hadiths (Bukhari, Muslim, Tirmidhi, Abu Dawud)
 * categorized by Islamic themes via keyword matching.
 *
 * No external Hadith topic API is freely available without auth, so we use
 * verified keyword mapping against the bundled hadith corpus. This gives
 * topic-based hadith study without requiring API access.
 *
 * Each topic has:
 *  - Title (Bn/En/Ar)
 *  - Overview (Bangla)
 *  - Keyword patterns (Bangla + Arabic) for filtering
 *  - Related concepts
 */

data class HadithTopic(
    val slug: String,
    val nameBn: String,
    val nameEn: String,
    val nameAr: String,
    val domain: String,
    val categoryBn: String,
    val overviewBn: String,
    val keywordsBn: List<String>,
    val keywordsAr: List<String>,
    val relatedConcepts: List<String>,
    val accentColor: Long,
    val iconHint: String = "hadith-premium-bg.webp"
)

data class HadithTopicEntry(
    val collectionId: String,
    val collectionNameBn: String,
    val hadithNumber: Int,
    val chapterId: Int,
    val arabic: String,
    val bangla: String,
    val grades: List<Map<String, String>>?,
    val reference: String,
    val relevanceScore: Int          // higher = more relevant
)

data class HadithTopicDetailResult(
    val topic: HadithTopic,
    val hadiths: List<HadithTopicEntry>,
    val totalCount: Int,
    val byCollection: Map<String, Int>
)

object HadithTopicCatalog {

    val topics: List<HadithTopic> = listOf(
        HadithTopic(
            slug = "iman",
            nameBn = "ঈমান",
            nameEn = "Faith",
            nameAr = "الإيمان",
            domain = "আকিদা",
            categoryBn = "মূল বিশ্বাস",
            overviewBn = "ঈমান ইসলামের ভিত্তি। ঈমানের ৬টি অঙ্গ: আল্লাহ, ফেরেশতা, কিতাব, নবী, আখিরাত, তাকদিরে বিশ্বাস। ঈমান অন্তরের বিশ্বাস, জিহ্বার স্বীকার ও অঙ্গের আমল। ঈমান ৭০+ শাখা আছে, সর্বোচ্চ হলো লা ইলাহা ইল্লাল্লাহ, সর্বনিম্ন হলো পথে কষ্টদায়ক বস্তু সরিয়ে দেওয়া।",
            keywordsBn = listOf("ঈমান", "মুমিন", "বিশ্বাস", "আল্লাহর উপর", "আখিরাতে ঈমান"),
            keywordsAr = listOf("الإيمان", "المؤمن", "آمن", "إيمان"),
            relatedConcepts = listOf("তাওহিদ", "ইসলাম", "ইখলাস", "নিয়ত", "আমল"),
            accentColor = 0xFF1B5E20
        ),
        HadithTopic(
            slug = "salat",
            nameBn = "নামাজ",
            nameEn = "Prayer",
            nameAr = "الصلاة",
            domain = "ইবাদত",
            categoryBn = "মূল ইবাদত",
            overviewBn = "নামাজ ইসলামের দ্বিতীয় স্তম্ভ এবং মিরাজের উপহার। দিনে ৫ ওয়াক্ত নামাজ ফরজ। নামাজ মুমিনের মিরাজ, জীবনের সবচেয়ে গুরুত্বপূর্ণ আমল। হাশরের ময়দানে প্রথম হিসাব হবে নামাজের।",
            keywordsBn = listOf("নামাজ", "সালাত", "সালাম", "ইমাম", "জামাত", "রাকাত", "সিজদা", "রুকু", "অযু", "ওযু", "আজান", "ইকামত", "মসজিদ"),
            keywordsAr = listOf("الصلاة", "صلى", "سجود", "ركوع", "الوضوء", "المسجد"),
            relatedConcepts = listOf("অযু", "জামাত", "ইমাম", "কিবলা", "তাহারত"),
            accentColor = 0xFFC9A34E
        ),
        HadithTopic(
            slug = "sawm",
            nameBn = "রোজা",
            nameEn = "Fasting",
            nameAr = "الصيام",
            domain = "ইবাদত",
            categoryBn = "মাসিক ইবাদত",
            overviewBn = "রোজা ইসলামের চতুর্থ স্তম্ভ। রমজান মাসে ফরজ, অন্য সময় সুন্নত ও নফল। রোজা শুধু খাদ্য-পানীয় থেকে নয়, প্রতিটি অঙ্গকে পাপ থেকে বিরত রাখা। রোজার বিশেষ ফজিলত: আল্লাহর নিজের জন্য প্রতিদান।",
            keywordsBn = listOf("রোজা", "সাওম", "রমজান", "সেহরি", "ইফতার", "তারাবিহ", "কদর"),
            keywordsAr = listOf("الصوم", "صام", "رمضان", "السحور", "الإفطار", "التراويح"),
            relatedConcepts = listOf("রমজান", "তারাবিহ", "সেহরি", "ইফতার", "কদর"),
            accentColor = 0xFFD84315
        ),
        HadithTopic(
            slug = "zakat",
            nameBn = "যাকাত",
            nameEn = "Zakat",
            nameAr = "الزكاة",
            domain = "ইবাদত",
            categoryBn = "আর্থিক ইবাদত",
            overviewBn = "যাকাত ইসলামের তৃতীয় স্তম্ভ। সম্পদের ২.৫% প্রতি বছর দরিদ্রদের দেওয়া ফরজ। যাকাত সমাজ থেকে দারিদ্র্য দূর করে এবং সম্পদের পবিত্রতা আনে। নিসাব সীমা পূরণ হলে যাকাত ফরজ।",
            keywordsBn = listOf("যাকাত", "সদকা", "দান", "নিসাব", "দরিদ্র", "গরিব", "মাল"),
            keywordsAr = listOf("الزكاة", "الصدقة", "زكاة", "النصاب"),
            relatedConcepts = listOf("সদকা", "নিসাব", "হক", "দানশীলতা"),
            accentColor = 0xFF00ACC1
        ),
        HadithTopic(
            slug = "hajj",
            nameBn = "হজ",
            nameEn = "Hajj",
            nameAr = "الحج",
            domain = "ইবাদত",
            categoryBn = "জীবনে একবার",
            overviewBn = "হজ ইসলামের পঞ্চম স্তম্ভ। সামর্থ্যবানদের জন্য জীবনে একবার ফরজ। কাবা ঘরের তওয়াফ, সাফা-মারওয়া, আরাফাত ও মিনার শয়তান লাঞ্ছন — হজের মূল আনুষ্ঠানিকতা। হজ মাবরুরের পুরস্কার জান্নাত।",
            keywordsBn = listOf("হজ", "কাবা", "তওয়াফ", "আরাফাত", "মিনা", "সাফা", "মারওয়া", "ইহরাম", "কুরবানী"),
            keywordsAr = listOf("الحج", "الكعبة", "الطواف", "عرفة", "الإحرام"),
            relatedConcepts = listOf("কাবা", "ইহরাম", "তওয়াফ", "কুরবানী", "আরাফাত"),
            accentColor = 0xFF5C6BC0
        ),
        HadithTopic(
            slug = "akhlaq",
            nameBn = "আখলাক",
            nameEn = "Morals",
            nameAr = "الأخلاق",
            domain = "আচরণ",
            categoryBn = "চরিত্র গঠন",
            overviewBn = "উত্তম চরিত্র হলো মুমিনের সর্বোচ্চ মর্যাদা। রাসূল সা. উত্তম চরিত্র পূর্ণ করতে এসেছেন। সত্যবাদিতা, আমানতদারি, দয়া, ক্ষমা, ধৈর্য, বিনয় — মুমিনের অপরিহার্য গুণ।",
            keywordsBn = listOf("আখলাক", "চরিত্র", "সত্য", "আমানত", "দয়া", "ক্ষমা", "বিনয়", "ধৈর্য", "হালিম", "মুহসিন"),
            keywordsAr = listOf("الأخلاق", "الخلق", "الصدق", "الأمانة", "الرحمة", "العفو"),
            relatedConcepts = listOf("সত্যবাদিতা", "আমানত", "দয়া", "ক্ষমা", "বিনয়"),
            accentColor = 0xFF8D6E63
        ),
        HadithTopic(
            slug = "sabr",
            nameBn = "সবর",
            nameEn = "Patience",
            nameAr = "الصبر",
            domain = "আচরণ",
            categoryBn = "আত্মিক গুণ",
            overviewBn = "সবর ঈমানের অর্ধেক। বিপদে অস্থির না হওয়া, আনুগত্যে অটল থাকা, গুনাহ থেকে বিরত থাকা — এই তিন ধরনের সবর। আল্লাহ সবরকারীদের ভালোবাসেন।",
            keywordsBn = listOf("সবর", "ধৈর্য", "সহ্য", "বিপদ", "পরীক্ষা", "বালা"),
            keywordsAr = listOf("الصبر", "صابر", "patience"),
            relatedConcepts = listOf("তাকওয়া", "তাওয়াক্কুল", "রিদা", "শুকরি"),
            accentColor = 0xFF1565C0
        ),
        HadithTopic(
            slug = "ilm",
            nameBn = "জ্ঞান",
            nameEn = "Knowledge",
            nameAr = "العلم",
            domain = "জ্ঞান",
            categoryBn = "অনুসন্ধান",
            overviewBn = "জ্ঞান অর্জন প্রতিটি মুসলিমের উপর ফরজ। আলেম ও গবীরের মধ্যে আলেমের মর্যাদা ৭০ গুণ। যে জ্ঞানের পথে চলে, আল্লাহ তার জন্য জান্নাতের পথ সহজ করেন।",
            keywordsBn = listOf("জ্ঞান", "ইলম", "আলেম", "শিক্ষা", "শিক্ষক", "ছাত্র", "কলম", "লেখা"),
            keywordsAr = listOf("العلم", "العالِم", "تعلّم", "علماء"),
            relatedConcepts = listOf("শিক্ষা", "জ্ঞান অর্জন", "শিক্ষক", "ছাত্র"),
            accentColor = 0xFF3949AB
        ),
        HadithTopic(
            slug = "family",
            nameBn = "পরিবার",
            nameEn = "Family",
            nameAr = "الأسرة",
            domain = "সমাজ",
            categoryBn = "পারিবারিক জীবন",
            overviewBn = "পরিবার সমাজের মূল একক। পিতা-মাতার সাথে সদ্ব্যবহার, স্ত্রীর প্রতি ভালো ব্যবহার, সন্তানদের তরবিয়ত — পরিবারের গুরুত্বপূর্ণ দায়িত্ব। জান্নাত মায়ের পায়ের নিচে।",
            keywordsBn = listOf("পরিবার", "মা", "বাবা", "পিতা-মাতা", "স্ত্রী", "স্বামী", "সন্তান", "তরবিয়ত", "আত্মীয়"),
            keywordsAr = listOf("الأسرة", "الوالدين", "الأم", "الأب", "الزوجة", "الولد"),
            relatedConcepts = listOf("পিতা-মাতা", "স্ত্রী", "সন্তান", "তরবিয়ত"),
            accentColor = 0xFF7E57C2
        ),
        HadithTopic(
            slug = "business",
            nameBn = "ব্যবসা",
            nameEn = "Business",
            nameAr = "التجارة",
            domain = "সমাজ",
            categoryBn = "অর্থনৈতিক আমল",
            overviewBn = "হালাল উপার্জন ইবাদত। ব্যবসায় সততা, ওজনে কম না দেওয়া, চুকিয়ে দেওয়া — মুমিন ব্যবসায়ীর গুণ। রিবা (সুদ) হারাম এবং কবিরা গুনাহ।",
            keywordsBn = listOf("ব্যবসা", "বাণিজ্য", "ক্রয়", "বিক্রয়", "মূল্য", "সুদ", "রিবা", "হালাল", "ওজন", "মাপ"),
            keywordsAr = listOf("التجارة", "البيع", "الربا", "الحلال"),
            relatedConcepts = listOf("হালাল উপার্জন", "সুদ", "ন্যায় বাণিজ্য"),
            accentColor = 0xFF558B2F
        ),
        HadithTopic(
            slug = "jannah-jahannam",
            nameBn = "জান্নাত ও জাহান্নাম",
            nameEn = "Paradise & Hell",
            nameAr = "الجنة والنار",
            domain = "আখিরাত",
            categoryBn = "পরকালীন বিভব",
            overviewBn = "জান্নাত আল্লাহর প্রতিশ্রুতি সৎকর্মশীলদের জন্য, জাহান্নাম পাপীদের জন্য। জান্নাতের নিয়ামত চোখে দেখা যায়নি, জাহান্নামের আজাব ভয়াবহ। প্রতিটি মুসলিমের উচিত জান্নাতের আশা ও জাহান্নামের ভয় রাখা।",
            keywordsBn = listOf("জান্নাত", "জান্নাম", "জাহান্নাম", "নরক", "বেহেশত", "দোজখ", "আজাব", "নেয়ামত", "হাশর", "কিয়ামত"),
            keywordsAr = listOf("الجنة", "النار", "جهنم", "الحشر", "القيامة"),
            relatedConcepts = listOf("কিয়ামত", "হাশর", "আজাব", "নেয়ামত"),
            accentColor = 0xFF2E7D32
        ),
        HadithTopic(
            slug = "dhikr",
            nameBn = "জিকির ও দোয়া",
            nameEn = "Dhikr & Dua",
            nameAr = "الذكر والدعاء",
            domain = "ইবাদত",
            categoryBn = "আত্মিক সংযোগ",
            overviewBn = "জিকির আল্লাহর স্মরণ, দোয়া আল্লাহর কাছে প্রার্থনা। আল্লাহ বলেন: যে আমাকে স্মরণ করে, আমি তাকে স্মরণ করি। সকাল-সন্ধ্যার আজকার, তাসবিহ, ইস্তিগফার — মুমিনের দৈনন্দিন আমল।",
            keywordsBn = listOf("জিকির", "দোয়া", "তাসবিহ", "ইস্তিগফার", "স্মরণ", "প্রার্থনা", "দুআ", "আল্লাহর নাম"),
            keywordsAr = listOf("الذكر", "الدعاء", "التسبيح", "الاستغفار"),
            relatedConcepts = listOf("তাসবিহ", "ইস্তিগফার", "সালাম", "দরূদ"),
            accentColor = 0xFF00897B
        )
    )

    fun get(slug: String): HadithTopic? = topics.find { it.slug == slug }

    fun search(query: String): List<HadithTopic> {
        if (query.isBlank()) return topics
        return topics.filter {
            it.nameBn.contains(query) || it.nameEn.lowercase().contains(query.lowercase()) ||
                it.nameAr.contains(query) || it.domain.contains(query) || it.categoryBn.contains(query)
        }
    }
}

/**
 * Filters the bundled hadith corpus (24,424 hadiths across 4 collections)
 * by topic keyword matching.
 */
class HadithTopicRepository(private val source: HadithAssetSource) {

    private val collectionCache = mutableMapOf<String, List<HadithJson>>()

    /**
     * Get all hadiths for a topic — searches across all 4 collections.
     */
    suspend fun getTopicHadiths(topic: HadithTopic): HadithTopicDetailResult = withContext(Dispatchers.IO) {
        val allEntries = mutableListOf<HadithTopicEntry>()
        val byCollection = mutableMapOf<String, Int>()

        val collections = try { source.loadIndex() } catch (_: Exception) { emptyList() }

        for (coll in collections) {
            try {
                val hadiths = getCollectionHadiths(coll.id)
                var countForThisCollection = 0
                for (h in hadiths) {
                    val score = scoreHadithForTopic(h, topic)
                    if (score > 0) {
                        allEntries.add(
                            HadithTopicEntry(
                                collectionId = coll.id,
                                collectionNameBn = coll.nameBn,
                                hadithNumber = h.hadithNumber,
                                chapterId = h.chapterId,
                                arabic = h.arabic,
                                bangla = h.bangla,
                                grades = h.grades,
                                reference = "${coll.nameBn} #${h.hadithNumber}",
                                relevanceScore = score
                            )
                        )
                        countForThisCollection++
                    }
                }
                byCollection[coll.nameBn] = countForThisCollection
            } catch (_: Exception) {
                // skip failed collection
            }
        }

        // Sort by relevance (highest first), then by collection
        allEntries.sortByDescending { it.relevanceScore }

        HadithTopicDetailResult(
            topic = topic,
            hadiths = allEntries,
            totalCount = allEntries.size,
            byCollection = byCollection
        )
    }

    private suspend fun getCollectionHadiths(collectionId: String): List<HadithJson> {
        collectionCache[collectionId]?.let { return it }
        val hadiths = try { source.loadCollection(collectionId).hadiths } catch (_: Exception) { emptyList() }
        collectionCache[collectionId] = hadiths
        return hadiths
    }

    /**
     * Score a hadith's relevance to a topic:
     *  - +3 per Bangla keyword match
     *  - +2 per Arabic keyword match
     *  - Higher score = more relevant
     */
    private fun scoreHadithForTopic(h: HadithJson, topic: HadithTopic): Int {
        var score = 0
        val banglaLower = h.bangla
        val arabicText = h.arabic
        for (kw in topic.keywordsBn) {
            if (banglaLower.contains(kw)) score += 3
        }
        for (kw in topic.keywordsAr) {
            if (arabicText.contains(kw)) score += 2
        }
        return score
    }
}
