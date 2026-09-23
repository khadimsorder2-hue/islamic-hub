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

/**
 * Catalog of 32 Hadith study topics (12 original + 20 added in v5.3.1) with
 * Bangla / English / Arabic titles, Bangla + Arabic keyword mappings and
 * related concepts. Used together with [HadithTopicRepository] for
 * topic-based hadith study over the bundled corpus.
 */
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
        ),
        HadithTopic(
            slug = "khatme-nabuwat",
            nameBn = "খাতমে নবুয়ত",
            nameEn = "Finality of Prophethood",
            nameAr = "خاتمة النبوة",
            domain = "আকিদা",
            categoryBn = "মূল বিশ্বাস",
            overviewBn = "হযরত মুহাম্মদ সা. আল্লাহর শেষ নবী ও রাসূল। তার পর আর কোনো নবী বা রাসূল আসবেন না — এটি ইসলামের মৌলিক আকিদা। যে ব্যক্তি নিজেকে নতুন নবী দাবি করে, সে মিথ্যবাদী ও প্রতারক। খাতমে নবুয়তের বিশ্বাস অস্বীকার করলে ঈমান নষ্ট হয়ে যায়।",
            keywordsBn = listOf("খাতমে নবুয়ত", "শেষ নবী", "রাসূল", "ওহি", "নবুয়ত"),
            keywordsAr = listOf("خاتم النبيين", "الوحي", "النبوة"),
            relatedConcepts = listOf("শেষ নবী", "ওহি", "রিসালাত", "আকিদা", "মুজাদ্দিদ"),
            accentColor = 0xFF1565C0
        ),
        HadithTopic(
            slug = "tauba",
            nameBn = "তওবা",
            nameEn = "Repentance",
            nameAr = "التوبة",
            domain = "আচরণ",
            categoryBn = "আত্মিক গুণ",
            overviewBn = "তওবা মানে গুনাহ থেকে ফিরে আসা ও আল্লাহর দরবারে অনুতাপ করা। আল্লাহ তওবাকারীদের ভালোবাসেন এবং সকল গুনাহ ক্ষমা করেন। তওবার শর্ত: গুনাহ ত্যাগ, অনুতাপ ও আর ফিরে না আসার দৃঢ় সংকল্প। ইস্তিগফার পড়া মুমিনের দৈনন্দিন সুন্নত।",
            keywordsBn = listOf("তওবা", "ক্ষমা", "গুনাহ", "ইস্তিগফার", "ফিরে আসা"),
            keywordsAr = listOf("التوبة", "الاستغفار", "المغفرة"),
            relatedConcepts = listOf("অনুতাপ", "ইস্তিগফার", "ক্ষমা", "গুনাহ", "ইলতিয়াব"),
            accentColor = 0xFF2E7D32
        ),
        HadithTopic(
            slug = "dua",
            nameBn = "দোয়া",
            nameEn = "Supplication",
            nameAr = "الدعاء",
            domain = "ইবাদত",
            categoryBn = "আত্মিক সংযোগ",
            overviewBn = "দোয়া ইবাদতের মর্ম ও আল্লাহর কাছে বান্দার প্রার্থনা। আল্লাহ বলেন: আমাকে ডাকো, আমি তোমাদের জবাব দেব। কবুলের বিশেষ সময়: সিজদার মুহূর্ত, রাতের শেষ তৃতীয়াংশ ও ইফতারের আগে। বিপদে-সুখে বেশি বেশি দোয়া চাই ও কবুলে অধ্যবসায় রাখা চাই।",
            keywordsBn = listOf("দোয়া", "প্রার্থনা", "কবুল", "হে আল্লাহ", "চাই"),
            keywordsAr = listOf("الدعاء", "يدعو", "استجابة"),
            relatedConcepts = listOf("কবুল", "আজকার", "রুকইয়া", "ইবাদত", "তাওয়াক্কুল"),
            accentColor = 0xFF00897B
        ),
        HadithTopic(
            slug = "riya",
            nameBn = "রিয়া (লোক দেখানো)",
            nameEn = "Showing Off",
            nameAr = "الرياء",
            domain = "আচরণ",
            categoryBn = "অন্তরের রোগ",
            overviewBn = "রিয়া হলো লোক দেখানো উদ্দেশ্যে ইবাদত করা — অন্তরের গোপন রোগ। হাদিসে বলা হয়েছে, রিয়াকারীকে কিয়ামতের দিন তার প্রচারিত আমলের প্রতিদান লোকের কাছে চাইতে বলা হবে। এই ছোট শিরক আমলের কবুল নষ্ট করে। তাই প্রতিটি আমলের আগে নিয়ত শুদ্ধ করা জরুরি।",
            keywordsBn = listOf("রিয়া", "লোক দেখানো", "প্রচার", "নিয়ত", "লোকলাজ", "ছোট শিরক"),
            keywordsAr = listOf("الرياء", "السمعة", "النية"),
            relatedConcepts = listOf("ইখলাস", "নিয়ত", "শিরক", "প্রচার", "অন্তরের রোগ"),
            accentColor = 0xFF6A1B9A
        ),
        HadithTopic(
            slug = "shukr",
            nameBn = "শুকর",
            nameEn = "Gratitude",
            nameAr = "الشكر",
            domain = "আচরণ",
            categoryBn = "আত্মিক গুণ",
            overviewBn = "শুকর মানে আল্লাহর নিয়ামতের শোকরিয়া আদায় করা। আল্লাহ বলেন: তোমরা কৃতজ্ঞ হলে আমি অবশ্যই তোমাদের প্রতি আরও বাড়িয়ে দেব। মুখে আলহামদুলিল্লাহ বলা, অঙ্গ দিয়ে আনুগত্য করা ও নিয়ামত সঠিক পথে ব্যবহার করা শুকরের অংশ। শোকরকারী সব অবস্থায় সন্তুষ্ট থাকে।",
            keywordsBn = listOf("শুকর", "কৃতজ্ঞ", "শোকর", "নিয়ামত", "কৃতজ্ঞতা", "শুকরিয়া"),
            keywordsAr = listOf("الشكر", "شكرا", "الشاكرين"),
            relatedConcepts = listOf("নিয়ামত", "আলহামদুলিল্লাহ", "সবর", "সন্তুষ্টি", "শোকরিয়া"),
            accentColor = 0xFFF57F17
        ),
        HadithTopic(
            slug = "husn-zan",
            nameBn = "মানুষের মঙ্গল কামনা",
            nameEn = "Good Assumption",
            nameAr = "حسن الظن",
            domain = "আচরণ",
            categoryBn = "সামাজিক আচরণ",
            overviewBn = "মুমিনের উচিত ভাইয়ের জন্য ভাল ধারণা রাখা ও কথা মঙ্গল অর্থে নেওয়া। কুরআনে সতর্ক করা হয়েছে: অনুমানের অধিকাংশই মিথ্যা। দুর্ভাগ্য ভেবে কারও কথার অপব্যাখ্যা করা ও গিবত করা নিষিদ্ধ। হুসনে জান মুসলিম ভ্রাতৃত্বের ভিত্তি।",
            keywordsBn = listOf("ভাইয়ের জন্য", "কল্যাণ", "দুর্ভাগ্য", "অপব্যাখ্যা", "ভাল ধারণা", "হুসনে জান"),
            keywordsAr = listOf("الظن", "الغيبة", "حسن الظن"),
            relatedConcepts = listOf("গিবত", "অনুমান", "ভ্রাতৃত্ব", "কল্যাণ", "অপব্যাখ্যা"),
            accentColor = 0xFF00695C
        ),
        HadithTopic(
            slug = "sadaqa-jariya",
            nameBn = "চলমান সদকা",
            nameEn = "Ongoing Charity",
            nameAr = "الصدقة الجارية",
            domain = "ইবাদত",
            categoryBn = "আর্থিক ইবাদত",
            overviewBn = "চলমান সদকা হলো এমন দান, যার সওয়াব দানকারীর মৃত্যুর পরও বাড়তে থাকে। জ্ঞান শেখানো, কুয়া খনন, নদী খনন ও মসজিদ নির্মাণ সদকায়ে জারিয়ার উদাহরণ। মানুষের উপকারে আসা প্রতিটি কাজ মৃত্যুর পরেও সওয়াব এনে দেয়। তাই জীবদ্দশায় ওয়াকফ ও দানে এগিয়ে আসা উচিত।",
            keywordsBn = listOf("সদকা", "জারিয়া", "দান", "মানুষের উপকার", "চলমান সদকা", "ওয়াকফ"),
            keywordsAr = listOf("الصدقة الجارية", "صدقة", "وقف"),
            relatedConcepts = listOf("সদকা", "ওয়াকফ", "সওয়াব", "দানশীলতা", "উত্তরাধিকার"),
            accentColor = 0xFF558B2F
        ),
        HadithTopic(
            slug = "parents",
            nameBn = "পিতা-মাতা",
            nameEn = "Parents",
            nameAr = "الوالدان",
            domain = "সমাজ",
            categoryBn = "পারিবারিক জীবন",
            overviewBn = "পিতা-মাতার সাথে সদ্ব্যবহার ঈমানের পরে সবচেয়ে বড় ফরজ কাজ। জান্নাত মায়ের পায়ের নিচে এবং পিতা-মাতার সন্তুষ্টির মধ্যে আল্লাহর সন্তুষ্টি নিহিত। মায়ের মর্যাদা তিন গুণ বেশি। তাদের সেবা, নরম কথা ও তাদের জন্য দোয়া মুমিন সন্তানের দায়িত্ব।",
            keywordsBn = listOf("পিতা-মাতা", "মা", "বাবা", "সন্তুষ্ট", "সদ্ব্যবহার"),
            keywordsAr = listOf("الوالدين", "الأم", "الأب", "بر الوالدين"),
            relatedConcepts = listOf("সদ্ব্যবহার", "সেবা", "সন্তুষ্টি", "দোয়া", "পরিবার"),
            accentColor = 0xFFAD1457
        ),
        HadithTopic(
            slug = "mehnat-istiqamat",
            nameBn = "পরিশ্রম ও অবিচলতা",
            nameEn = "Hard Work & Steadfastness",
            nameAr = "الاجتهاد والاستقامة",
            domain = "আচরণ",
            categoryBn = "জীবনচরিত",
            overviewBn = "আল্লাহ পরিশ্রমী ও অবিচল বান্দাকে ভালোবাসেন। কাজে যত্নবান হওয়া ও অক্লান্ত চেষ্টা মুমিনের চরিত্র। ইস্তিকামত অর্থ সৎ পথে অটল থাকা — মৃত্যুর আগ পর্যন্ত এ অবস্থার জন্য দোয়া করা হয়। নবীজির জীবন ছিল উদ্যম ও অধ্যবসায়ের সেরা আদর্শ।",
            keywordsBn = listOf("পরিশ্রম", "অবিচল", "অটল", "চেষ্টা", "উদ্যম"),
            keywordsAr = listOf("الاجتهاد", "الاستقامة", "الثبات"),
            relatedConcepts = listOf("ইস্তিকামত", "মুজাহাদা", "অধ্যবসায়", "ধৈর্য", "অটলতা"),
            accentColor = 0xFF1B5E20
        ),
        HadithTopic(
            slug = "jumma",
            nameBn = "জুমুআ",
            nameEn = "Friday",
            nameAr = "الجمعة",
            domain = "ইবাদত",
            categoryBn = "সাপ্তাহিক ইবাদত",
            overviewBn = "জুমুআ মুসলিমদের সাপ্তাহিক ঈদ ও সবচেয়ে মর্যাদাপূর্ণ দিন। যে ব্যক্তি গোসল করে, দ্রুত মসজিদে আসে ও খুতবা মন দিয়ে শোনে, তার এক সপ্তাহের গুনাহ ক্ষমা হয়। জুমুআর এমন এক মুহূর্ত আছে, যে সময়ের দোয়া ফিরিয়ে দেওয়া হয় না। সূরা কাহফ তিলাওয়াত ও বেশি দরূদ পাঠ জুমুআর বিশেষ আমল।",
            keywordsBn = listOf("জুমুআ", "শুক্রবার", "খুতবা", "গোসল", "জুমার নামাজ", "সূরা কাহফ"),
            keywordsAr = listOf("الجمعة", "الخطبة", "يوم الجمعة"),
            relatedConcepts = listOf("খুতবা", "গোসল", "সূরা কাহফ", "দরূদ", "মসজিদ"),
            accentColor = 0xFFC9A34E
        ),
        HadithTopic(
            slug = "masjid",
            nameBn = "মসজিদ",
            nameEn = "Mosque",
            nameAr = "المسجد",
            domain = "ইবাদত",
            categoryBn = "ইবাদতের স্থান",
            overviewBn = "মসজিদ আল্লাহর ঘর ও নামাজের কেন্দ্র। জামাতে নামাজ একা নামাজের চেয়ে ২৭ গুণ বেশি ফজিলতপূর্ণ। মসজিদে যাওয়ার প্রতি পদক্ষেপে এক নেকি এবং সারিবদ্ধ নামাজে ফেরেশতারা মুমিনদের জন্য দোয়া করেন। আল্লাহর ঘর নির্মাণের প্রতিদান জান্নাতে ঘর।",
            keywordsBn = listOf("মসজিদ", "জামাত", "ইমাম", "সারি", "মিম্বর", "মসজিদ নির্মাণ"),
            keywordsAr = listOf("المسجد", "الجماعة", "المصلين"),
            relatedConcepts = listOf("জামাত", "ইমাম", "সারি", "আল্লাহর ঘর", "ফজিলত"),
            accentColor = 0xFF3949AB
        ),
        HadithTopic(
            slug = "sahaba",
            nameBn = "সাহাবীগণ",
            nameEn = "Companions",
            nameAr = "الصحابة",
            domain = "আকিদা",
            categoryBn = "প্রিয়জন",
            overviewBn = "সাহাবীগণ রাসূল সা.-এর সঙ্গী ও ইসলামের সেরা প্রজন্ম। আল্লাহ তাদের সন্তুষ্ট হয়েছেন এবং তাদের পথ অনুসরণের নির্দেশ দিয়েছেন। আনসার ও মুহাজিরদের ত্যাগ, ভ্রাতৃত্ব ও আনুগত্য আমলের আদর্শ। সাহাবীদের ভালোবাসা ঈমানের অংশ এবং তাদের সম্পর্কে অসম্মান হারাম।",
            keywordsBn = listOf("সাহাবী", "আনসার", "মুহাজির", "নকল করো", "সাহাবায়ে কেরাম", "খোলাফায়ে রাশেদুন"),
            keywordsAr = listOf("الصحابة", "الأنصار", "المهاجرين"),
            relatedConcepts = listOf("আনসার", "মুহাজির", "ভ্রাতৃত্ব", "খোলাফায়ে রাশেদুন", "অনুসরণ"),
            accentColor = 0xFF7E57C2
        ),
        HadithTopic(
            slug = "riba",
            nameBn = "রিবা (সুদ)",
            nameEn = "Riba",
            nameAr = "الربا",
            domain = "সমাজ",
            categoryBn = "অর্থনৈতিক আমল",
            overviewBn = "রিবা (সুদ) মানে সম্পদের নির্ধারিত বাড়তি, যা কুরআনে কঠোরভাবে হারাম। সুদ খাওয়া, দেওয়া, লেখা ও তার সাক্ষী থাকা — সবাই গুনাহগার। সুদ সম্পদ ধ্বংস করে এবং কিয়ামতের দিন সুদখোর পাগলের মতো দাঁড়াবে। তাই হালাল ব্যবসা ও প্রকৃত ঋণের ব্যবস্থা গ্রহণ করা উচিত।",
            keywordsBn = listOf("সুদ", "রিবা", "বাড়তি", "কাবেলা", "দ্বিগুণ", "সুদের লেনদেন"),
            keywordsAr = listOf("الربا", "السود", "أضعافا"),
            relatedConcepts = listOf("সুদ", "হারাম", "হালাল উপার্জন", "ঋণ", "শোষণ"),
            accentColor = 0xFFB71C1C
        ),
        HadithTopic(
            slug = "haya",
            nameBn = "লজ্জাশীলতা",
            nameEn = "Modesty",
            nameAr = "الحياء",
            domain = "আচরণ",
            categoryBn = "চরিত্র গঠন",
            overviewBn = "হায়া (লজ্জাশীলতা) ঈমানের অঙ্গ ও মুমিনের চরিত্রের অলংকার। রাসূল সা. বলেছেন: লজ্জা যা নিয়ে আসে, তার সবই মঙ্গল। হায়া মানুষকে গুনাহ থেকে সংযত রাখে, চোখ ও জিহ্বা হেফাজত করায়। পোশাকে, কথায় ও আচরণে গোপনীয়তা রাখা ইসলামের শিক্ষা।",
            keywordsBn = listOf("লজ্জা", "হায়া", "সংযত", "গোপনীয়তা", "লজ্জাশীল", "চোখ নিচু রাখা"),
            keywordsAr = listOf("الحياء", "الاستحياء", "العفة"),
            relatedConcepts = listOf("লজ্জা", "সংযম", "চোখ নিচু রাখা", "পোশাক", "গোপনীয়তা"),
            accentColor = 0xFF00838F
        ),
        HadithTopic(
            slug = "silm-munafiqat",
            nameBn = "মুনাফিকতা",
            nameEn = "Hypocrisy",
            nameAr = "النفاق",
            domain = "আকিদা",
            categoryBn = "অন্তরের রোগ",
            overviewBn = "মুনাফিকতা হলো অন্তরে এক থাকা, বাহিরে অন্যরকম দ্বিমুখী আচরণ করা। মুনাফিকের তিন লক্ষণ: কথায় মিথ্যা, আমানতে বিশ্বাসঘাতকতা ও ওয়াদা ভঙ্গ। মুনাফিক জাহান্নামের সবচেয়ে নিচতলায় যাবে। তাই কথা ও কাজে অসঙ্গতি থেকে বেঁচে থাকা এবং প্রতারণা পরিহার করা জরুরি।",
            keywordsBn = listOf("মুনাফিক", "দ্বিমুখী", "প্রতারণা", "কথায় কাজ", "মুনাফিকতা", "তিন লক্ষণ"),
            keywordsAr = listOf("المنافقون", "النفاق", "يخادعون"),
            relatedConcepts = listOf("দ্বিমুখী", "মিথ্যা", "আমানতে খেয়ানত", "ওয়াদা ভঙ্গ", "অন্তরের রোগ"),
            accentColor = 0xFF4A148C
        ),
        HadithTopic(
            slug = "ilaj-dua-shifa",
            nameBn = "রোগ ও শিফা",
            nameEn = "Illness & Cure",
            nameAr = "الشفاء",
            domain = "সমাজ",
            categoryBn = "জীবনের পরীক্ষা",
            overviewBn = "রোগ আল্লাহর পক্ষ থেকে পরীক্ষা, যার সঙ্গে গুনাহ মাফের প্রতিশ্রুতি রয়েছে। প্রতিটি রোগের শিফা আছে, তাই চিকিৎসা নেওয়ার সঙ্গে দোয়া করা সুন্নত। অসুস্থ অবস্থায় সবর করা ও আরোগ্যের পর শুকর আদায় মুমিনের আদব। রুকইয়া পড়ে শিফা চাওয়া প্রমাণিত আমল।",
            keywordsBn = listOf("রোগ", "শিফা", "অসুস্থ", "আরোগ্য", "পরীক্ষা"),
            keywordsAr = listOf("الشفاء", "المرض", "سقم"),
            relatedConcepts = listOf("শিফা", "রুকইয়া", "সবর", "চিকিৎসা", "পরীক্ষা"),
            accentColor = 0xFF455A64
        ),
        HadithTopic(
            slug = "mrittu-akhira",
            nameBn = "মৃত্যুর স্মরণ",
            nameEn = "Remembrance of Death",
            nameAr = "ذكرى الموت",
            domain = "আখিরাত",
            categoryBn = "পরকাল",
            overviewBn = "মৃত্যুর স্মরণ গাফলত দূর করে ও অন্তরকে নরম করে। কবর আখিরাতের প্রথম মঞ্জিল, যেখানে ফেরেশতার প্রশ্নের জবাব দিতে হবে। জীবন সংক্ষিপ্ত, তাই তওবা ও সৎকর্মে দেরি করা উচিত নয়। রাসূল সা. মৃতদের জন্য দোয়া করতে ও কবরস্থান জিয়ারত করতে উৎসাহ দিয়েছেন।",
            keywordsBn = listOf("মৃত্যু", "কবর", "গোর", "আখিরাত", "জীবন সংক্ষিপ্ত"),
            keywordsAr = listOf("الموت", "القبر", "الآخرة"),
            relatedConcepts = listOf("কবর", "আখিরাত", "তওবা", "সৎকর্ম", "ওসিয়ত"),
            accentColor = 0xFF37474F
        ),
        HadithTopic(
            slug = "donya-akherat",
            nameBn = "দুনিয়া ও আখিরাত",
            nameEn = "World & Hereafter",
            nameAr = "الدنيا والآخرة",
            domain = "আখিরাত",
            categoryBn = "জীবনদৃষ্টি",
            overviewBn = "দুনিয়া মুসাফিরের ছায়ার মতো ক্ষণস্থায়ী, আখিরাত চিরস্থায়ী ঘর। দুনিয়া আখিরাতের চাষের জমি — এখানে বপন করো, ফল মিলবে সেখানে। দুনিয়ার ভালোবাসা সব গুনাহের মূল, তবে হালাল উপার্জন ও প্রয়োজনীয় জীবনযাপন নিষিদ্ধ নয়। মুসাফিরের মতো দুনিয়ায় অবস্থান করে আখিরাতের প্রস্তুতি নেওয়াই জীবনদৃষ্টি।",
            keywordsBn = listOf("দুনিয়া", "আখিরাত", "মুসাফির", "ক্ষণস্থায়ী", "প্রস্তুতি"),
            keywordsAr = listOf("الدنيا", "الآخرة", "زاد"),
            relatedConcepts = listOf("মুসাফির", "প্রস্তুতি", "রিজিক", "জীবনদৃষ্টি", "তাকওয়া"),
            accentColor = 0xFF283593
        ),
        HadithTopic(
            slug = "santana-tarbiyat",
            nameBn = "সন্তান তরবিয়ত",
            nameEn = "Raising Children",
            nameAr = "تربية الأولاد",
            domain = "সমাজ",
            categoryBn = "পারিবারিক জীবন",
            overviewBn = "সন্তান আল্লাহর আমানত, তার তরবিয়ত পিতা-মাতার দায়িত্ব। ভালো নাম রাখা, আকিদা শেখানো, কুরআন ও আদব শেখানো অপরিহার্য। সাত বছরে নামাজের আদেশ দাও, দশ বছরে তানবিহ করো — হাদিসের স্পষ্ট নির্দেশ। সন্তানকে শাস্তির আগে স্নেহ ও আদর্শ দিয়ে শিক্ষা দাও।",
            keywordsBn = listOf("সন্তান", "তরবিয়ত", "শিক্ষা দাও", "আদব", "শাস্তি"),
            keywordsAr = listOf("الأولاد", "التربية", "الأبناء"),
            relatedConcepts = listOf("আদব", "শিক্ষা", "আমানত", "স্নেহ", "নামাজের অভ্যাস"),
            accentColor = 0xFF7E57C2
        ),
        HadithTopic(
            slug = "parosi-haq",
            nameBn = "প্রতিবেশীর হক",
            nameEn = "Neighbours' Rights",
            nameAr = "حق الجار",
            domain = "সমাজ",
            categoryBn = "সামাজিক আচরণ",
            overviewBn = "প্রতিবেশীর হক আদায় করা ইসলামের গুরুত্বপূর্ণ নির্দেশ। জিবরাইল আ. এত বেশি প্রতিবেশীর হক মনে করিয়ে দিতেন, রাসূল সা. আশা করেছিলেন তিনি তাকে ওয়ারিশ বানাবেন। যে ব্যক্তি নিজে তৃপ্ত থাকে অথচ প্রতিবেশী ক্ষুধার্ত থাকে, সে মুমিন নয়। পাড়ার কষ্ট সহ্য করা, মেহমানদারি ও উপকার প্রতিবেশীর অধিকার।",
            keywordsBn = listOf("প্রতিবেশী", "পাড়া", "মেহমান", "সহযাত্রী", "প্রতিবেশীর হক", "আতিথ্য"),
            keywordsAr = listOf("الجار", "الجيران", "الضيف"),
            relatedConcepts = listOf("আতিথ্য", "মেহমান", "ভ্রাতৃত্ব", "উপকার", "সহযাত্রী"),
            accentColor = 0xFF8D6E63
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
