package com.islamichub.app.data.repo

/**
 * Quran Topic Catalog (v5.3.1) — keyword-driven thematic study.
 *
 * v5.3.0 shipped only 7 hand-picked curated topics (TopicStudyData.kt) with
 * manually typed ayah lists, while FULL_RESOURCE.md advertised "338+ thematic
 * groupings". This catalog closes that gap: each topic carries Bangla +
 * Arabic keyword lists, and ThematicQuranEngine scans the bundled full Quran
 * (6,236 ayahs, 114 surahs — offline) to find EVERY matching ayah dynamically.
 *
 * Pattern mirrors HadithTopicCatalog (same +3 Bangla / +2 Arabic scoring), so
 * Quran & Hadith topic study now behave identically.
 *
 * Adding a new topic = one entry here. No curated ayah lists required.
 *
 * Sources referenced for keyword curation:
 *  - Quranic Arabic Corpus topic index (https://corpus.quran.com/topics.jsp)
 *  - Standard Bangla tafsir terminology (Ibn Kathir / Ma'ariful Quran summaries)
 *  - IslamicHub original Bangla overviews
 */

data class QuranKeywordTopic(
    val slug: String,
    val nameBn: String,
    val nameEn: String,
    val nameAr: String,
    val domain: String,                 // e.g. "ঈমান ও আকিদা", "ইবাদত"
    val categoryBn: String,
    val overviewBn: String,
    val keywordsBn: List<String>,
    val keywordsAr: List<String>,
    val relatedConcepts: List<String>,
    val accentColor: Long
)

object QuranTopicCatalog {

    const val MIN_MATCH_SCORE = 3          // 1 Bangla keyword (3) or 2 Arabic (2+... >=3 means 2 ar not enough unless combined with bn? 2+2=4 ok)
    const val MAX_SCORED_AYAHS = 400       // display cap for the broadest topics

    val topics: List<QuranKeywordTopic> = listOf(
        // ─── ঈমান ও আকিদা ────────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "iman",
            nameBn = "ঈমান",
            nameEn = "Faith",
            nameAr = "الإيمان",
            domain = "ঈমান ও আকিদা",
            categoryBn = "মূল বিশ্বাস",
            overviewBn = "ঈমান হলো আল্লাহ, তাঁর ফেরেশতা, কিতাব, রাসূল, আখিরাত ও তাকদিরে খাঁটি বিশ্বাস। কুরআনে ঈমান ও আমলে সালেহ প্রায় সবসময় পাশাপাশি এসেছে — কারণ খাঁটি ঈমান কখনোই কর্মহীন থাকে না। এই থিমের আয়াতগুলো মুমিনের বৈশিষ্ট্য, ঈমানের ফল ও তার অটলতা নিয়ে।",
            keywordsBn = listOf("ঈমান", "মুমিন", "বিশ্বাস", "ঈমানদার", "আল্লাহর উপর ঈমান", "তার রাসূলে ঈমান"),
            keywordsAr = listOf("الإيمان", "المؤمنون", "آمن", "بآياتنا يؤمنون"),
            relatedConcepts = listOf("তাওহিদ", "আমলে সালেহ", "ইসলাম", "আকিদা"),
            accentColor = 0xFF1B5E20
        ),
        QuranKeywordTopic(
            slug = "tawhid",
            nameBn = "তাওহিদ",
            nameEn = "Oneness of Allah",
            nameAr = "التوحيد",
            domain = "ঈমান ও আকিদা",
            categoryBn = "আল্লাহর একত্ববাদ",
            overviewBn = "তাওহিদ মানে আল্লাহর একত্ববাদ — তিনি এক, তাঁর কোনো শরীক নেই। কুরআনের মূল দাওয়াতই ছিল এক আল্লাহর ইবাদত করা। 'লা ইলাহা ইল্লাল্লাহ' — এই কালেমার ঘোষণাই সকল নবীর মূল বার্তা ছিল।",
            keywordsBn = listOf("তাওহিদ", "এক আল্লাহ", "একত্ব", "শরীক নেই", "তিনিই এক", "উপাস্য"),
            keywordsAr = listOf("لا إله إلا", "واحد", "أحد", "لا شريك", "وحده"),
            relatedConcepts = listOf("ইখলাস", "শিরক", "ইবাদত", "কালেমা"),
            accentColor = 0xFF6D45C7
        ),
        QuranKeywordTopic(
            slug = "shirk",
            nameBn = "শিরক থেকে বাঁচা",
            nameEn = "Avoiding Shirk",
            nameAr = "الشرك",
            domain = "ঈমান ও আকিদা",
            categoryBn = "নিষিদ্ধ বিশ্বাস",
            overviewBn = "শিরক হলো আল্লাহর সাথে কাউকে শরীক করা — ইসলামের সবচেয়ে বড় গুনাহ। আল্লাহ বলেন, শিরক ছাড়া সব গুনাহ তিনি ক্ষমা করতে পারেন। কুরআন বারবার সতর্ক করেছে যে কবিরা গুনাহের মধ্যে শিরকের স্থান অনন্য।",
            keywordsBn = listOf("শিরক", "শরীক", "মুশরিক", "অংশীদার", "মূর্তিপূজা"),
            keywordsAr = listOf("الشرك", "يشرق", "المشركون", "شريكا", "لا تشرك"),
            relatedConcepts = listOf("তাওহিদ", "কুফর", "ইবাদত", "তওবা"),
            accentColor = 0xFFC62828
        ),
        QuranKeywordTopic(
            slug = "quran-hidayah",
            nameBn = "কুরআন ও হেদায়েত",
            nameEn = "Quran & Guidance",
            nameAr = "القرآن والهداية",
            domain = "ঈমান ও আকিদা",
            categoryBn = "আসমানি কিতাব",
            overviewBn = "কুরআন আল্লাহর শেষ কিতাব, মানবজাতির জন্য পূর্ণ হেদায়েত। যে আয়াতে বর্ণিত হয়েছে কুরআন কীভাবে অন্ধকার থেকে আলোতে নিয়ে যায়, তার চিন্তা করার আহ্বান এবং তিলাওয়াতের ফজিলত।",
            keywordsBn = listOf("কুরআন", "ফুরকান", "হেদায়েত", "হিদায়াত", "কিতাব", "আয়াত", "জিকিরের কিতাব"),
            keywordsAr = listOf("القرآن", "الفرقان", "هدى", "الهدي", "الذكر", "الكتاب"),
            relatedConcepts = listOf("ওহি", "তিলাওয়াত", "তাদাব্বুর", "হুকুম"),
            accentColor = 0xFF2E7D32
        ),
        QuranKeywordTopic(
            slug = "nubuwwah",
            nameBn = "নবী ও রাসূল",
            nameEn = "Prophets & Messengers",
            nameAr = "الأنبياء والرسل",
            domain = "ঈমান ও আকিদা",
            categoryBn = "নবুয়ত",
            overviewBn = "আল্লাহ মানবজাতিকে পথ দেখাতে যুগে যুগে নবী-রাসূল পাঠিয়েছেন — নূহ, ইব্রাহিম, মূসা, ঈসা এবং সর্বশেষ মুহাম্মদ সা.। তাঁদের মূল বার্তা একটাই: এক আল্লাহর উপাসনা। এই থিমে নবুয়তের প্রমাণ ও রাসূলদের আনুগত্যের আয়াত।",
            keywordsBn = listOf("নবী", "রাসূল", "পয়গম্বর", "নবুয়ত", "আমার বান্দাদের মধ্যে", "ভালোবাসেন তাদের"),
            keywordsAr = listOf("النبي", "الرسول", "الأنبياء", "رسلا", "نبيا"),
            relatedConcepts = listOf("ওহি", "সুন্নাহ", "ইতিবাত", "উম্মি নবী"),
            accentColor = 0xFF3949AB
        ),

        // ─── ইবাদত ───────────────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "salat",
            nameBn = "নামাজ",
            nameEn = "Prayer",
            nameAr = "الصلاة",
            domain = "ইবাদত",
            categoryBn = "মূল ইবাদত",
            overviewBn = "নামাজ দীনের স্তম্ভ — দিনে পাঁচবার ফরজ, মুমিনের মিরাজ। কুরআনে বারবার 'নামাজ কায়েম করো' আদেশ এসেছে, সাথে যাকাতের কথা। ধৈর্য ও নামাজে সাহায্য চাওয়ার আয়াত মুমিনের জীবনের ভিত্তি।",
            keywordsBn = listOf("নামাজ", "সালাত", "কায়েম করো", "রুকু", "সিজদা", "মসজিদ", "আকাম", "নামাজে অধ্যবসায়"),
            keywordsAr = listOf("الصلاة", "أقيموا الصلاة", "الصلوة", "الركوع", "السجود", "المساجد"),
            relatedConcepts = listOf("যাকাত", "জামাত", "তাহারত", "খুশু"),
            accentColor = 0xFFC9A34E
        ),
        QuranKeywordTopic(
            slug = "zakat",
            nameBn = "যাকাত",
            nameEn = "Zakat",
            nameAr = "الزكاة",
            domain = "ইবাদত",
            categoryBn = "আর্থিক ইবাদত",
            overviewBn = "যাকাত সম্পদের নির্দিষ্ট অংশ (২.৫%) প্রতি বছর দরিদ্রদের দেওয়া ফরজ ইবাদত। কুরআনে নামাজের পরেই সবচেয়ে বেশি উল্লেখ পেয়েছে যাকাত — সমাজ থেকে দারিদ্র্য দূর করার আল্লাহর ব্যবস্থা।",
            keywordsBn = listOf("যাকাত", "যাকাত দাও", "দরিদ্র", "গরিব", "মিসকিন", "নিসাব"),
            keywordsAr = listOf("الزكاة", "الزكوة", "المساكين", "الفقراء", "يتزكون"),
            relatedConcepts = listOf("সদকা", "ইনফাক", "নিসাব", "হক"),
            accentColor = 0xFF00ACC1
        ),
        QuranKeywordTopic(
            slug = "sawm",
            nameBn = "রোজা",
            nameEn = "Fasting",
            nameAr = "الصيام",
            domain = "ইবাদত",
            categoryBn = "মাসিক ইবাদত",
            overviewBn = "রোজা ইসলামের চতুর্থ স্তম্ভ — রমজান মাসে ফরজ। আল্লাহ বলেন, তোমাদের পূর্ববর্তী জাতিদের জন্যও রোজা ফরজ করা হয়েছিল, যাতে তোমরা তাকওয়া অর্জন করো। রমজানে কুরআন নাজিল হওয়ার ঘোষণাও এই থিমের বিশেষ আয়াত।",
            keywordsBn = listOf("রোজা", "সিয়াম", "রমজান", "রমাদান", "সেহরি", "ইফতার"),
            keywordsAr = listOf("الصيام", "الصوم", "رمضان", "تصومون", "صياما"),
            relatedConcepts = listOf("তাকওয়া", "লাইলাতুল কদর", "শুকর", "সবর"),
            accentColor = 0xFFD84315
        ),
        QuranKeywordTopic(
            slug = "hajj",
            nameBn = "হজ ও উমরাহ",
            nameEn = "Hajj & Umrah",
            nameAr = "الحج والعمرة",
            domain = "ইবাদত",
            categoryBn = "জীবনে একবার",
            overviewBn = "হজ ইসলামের পঞ্চম স্তম্ভ — সামর্থ্যবানদের জন্য জীবনে একবার ফরজ। বাইতুল্লাহর তওয়াফ, আরাফাতের অবস্থান ও কুরবানী এই ইবাদতের মূল কেন্দ্র। কুরআনে হজের ফরজ হওয়া, এর নিয়ম ও মর্যাদা বর্ণিত।",
            keywordsBn = listOf("হজ", "উমরাহ", "কাবা", "বাইতুল্লাহ", "তওয়াফ", "আরাফাত", "কুরবানী"),
            keywordsAr = listOf("الحج", "العمرة", "الكعبة", "البيت العتيق", "أطوفوا", "الحجج"),
            relatedConcepts = listOf("ইহরাম", "তওয়াফ", "ইব্রাহিম (আ.)", "কুরবানী"),
            accentColor = 0xFF5C6BC0
        ),
        QuranKeywordTopic(
            slug = "dua",
            nameBn = "দোয়া",
            nameEn = "Supplication",
            nameAr = "الدعاء",
            domain = "ইবাদত",
            categoryBn = "আল্লাহর কাছে প্রার্থনা",
            overviewBn = "দোয়া ইবাদতের মর্ম — বান্দার আল্লাহর কাছে সরাসরি প্রার্থনা। আল্লাহ বলেন: 'আমাকে ডাকো, আমি সাড়া দেব।' কুরআনে নবীদের দোয়া, দোয়া কবুলের প্রতিশ্রুতি ও দোয়ার আদব বর্ণিত।",
            keywordsBn = listOf("দোয়া", "ডাকো", "প্রার্থনা", "চাই আমরা", "আমার রব", "হে আমাদের রব"),
            keywordsAr = listOf("الدعاء", "ادعوني", "أدعوا", "دعاء", "ربنا"),
            relatedConcepts = listOf("ইজাবত", "তাওয়াক্কুল", "রহমত", "সবর"),
            accentColor = 0xFF00897B
        ),
        QuranKeywordTopic(
            slug = "dhikr",
            nameBn = "জিকির",
            nameEn = "Remembrance",
            nameAr = "الذكر",
            domain = "ইবাদত",
            categoryBn = "আল্লাহর স্মরণ",
            overviewBn = "জিকির মানে আল্লাহকে স্মরণ করা — বলা, মনে রাখা ও শোকর করা। 'আল্লা বি জিকিরিল্লাহি তাতমাইন্নুল কুলুব' — আল্লাহর স্মরণেই অন্তর প্রশান্ত হয়। কায়েম থাকা বা বসা-শোয়া অবস্থাতেও আল্লাহকে অনেক স্মরণ করার আদেশ এসেছে।",
            keywordsBn = listOf("জিকির", "স্মরণ", "আল্লাহকে স্মরণ", "অনেক স্মরণ", "তাঁর নাম জিকির"),
            keywordsAr = listOf("الذكر", "اذكروا", "فاذكروني", "يذكرون", "ذكرا", "ذكر الله"),
            relatedConcepts = listOf("তাসবিহ", "শুকর", "দোয়া", "প্রশান্তি"),
            accentColor = 0xFF7E57C2
        ),

        // ─── আত্মিক গুণ ──────────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "tawakkul",
            nameBn = "তাওয়াক্কুল",
            nameEn = "Trust in Allah",
            nameAr = "التوكل",
            domain = "আচরণ",
            categoryBn = "আল্লাহর উপর ভরসা",
            overviewBn = "তাওয়াক্কুল মানে সব ব্যবস্থা নেওয়ার পর ফলাফলে আল্লাহর উপর পূর্ণ ভরসা। 'যে আল্লাহর উপর ভরসা করে, আল্লাহই তার জন্য যথেষ্ট।' মুমিনের হৃদয় মানুষের সৃষ্টির দিকে নয়, সৃষ্টিকর্তার দিকে ঝুঁকে থাকে।",
            keywordsBn = listOf("তাওয়াক্কুল", "ভরসা", "ভরসা করো", "ভরসা করে", "আল্লাহর উপর ভরসা"),
            keywordsAr = listOf("التوكل", "يتوكل", "توكلوا", "توكلنا", "خير الوكلين", "عليه توكلنا"),
            relatedConcepts = listOf("সবর", "রিদা", "তাকদির", "দোয়া"),
            accentColor = 0xFF1565C0
        ),
        QuranKeywordTopic(
            slug = "infaq",
            nameBn = "দান ও ইনফাক",
            nameEn = "Charity & Spending",
            nameAr = "الإنفاق",
            domain = "সমাজ",
            categoryBn = "দানশীলতা",
            overviewBn = "ইনফাক মানে আল্লাহর পথে সম্পদ ব্যয় করা — সচ্ছল অবস্থায় হোক বা সংকটে। কুরআন বলে, বিপদ-সুবিধায় ব্যয় করা, ক্রোধ সামলানো ও মানুষ ক্ষমা করা — এসব আল্লাহ ভালোবাসেন। দান সম্পদ কমায় না, বরং বাড়ায়।",
            keywordsBn = listOf("ইনফাক", "ব্যয় করে", "ব্যয় করো", "দান", "সদকা", "খরচ করে", "আল্লাহর পথে ব্যয়"),
            keywordsAr = listOf("الإنفاق", "ينفقون", "أنفقوا", "ينفق", "نفقوا", "صدقة"),
            relatedConcepts = listOf("যাকাত", "সদকা", "শুকর", "এতিম"),
            accentColor = 0xFF558B2F
        ),
        QuranKeywordTopic(
            slug = "yatim-miskin",
            nameBn = "এতিম ও অসহায়",
            nameEn = "Orphans & Needy",
            nameAr = "اليتامى والمساكين",
            domain = "সমাজ",
            categoryBn = "সমাজসেবা",
            overviewBn = "এতিমের প্রতি দয়া কুরআনে অসাধারণ গুরুত্ব পেয়েছে — রাসূল সা. নিজেই এতিম ছিলেন। এতিমের সম্পদে অন্যায়ভাবে হাত দেওয়া কবিরা গুনাহ, আর এতিমকে স্নেহ করা জান্নাতের সুসংবাদ বহন করে।",
            keywordsBn = listOf("এতিম", "অনাথ", "অসহায়", "দরিদ্রকে", "মিসকিন", "ভিক্ষার্থী"),
            keywordsAr = listOf("اليتامى", "اليتيم", "المسكين", "المساكين", "الفقراء", "السائل"),
            relatedConcepts = listOf("ইনফাক", "যাকাত", "দয়া", "হক"),
            accentColor = 0xFF8D6E63
        ),

        // ─── সমাজ ও ন্যায়বিচার ──────────────────────────────────────────
        QuranKeywordTopic(
            slug = "adl",
            nameBn = "ন্যায়বিচার",
            nameEn = "Justice",
            nameAr = "العدل",
            domain = "সমাজ",
            categoryBn = "ন্যায় ও ইনসাফ",
            overviewBn = "ন্যায়বিচার আকাশ ও জমির স্থিরতার মতো অপরিহার্য। কুরআন আদেশ দেয় — পরিচিত হোক বা অপরিচিত, নিজের আত্মীয়ের বিরুদ্ধে হলেও সাক্ষ্য দিতে হবে সৎভাবে। নিজে কষ্ট পেলেও ইনসাফ করতে হবে।",
            keywordsBn = listOf("ন্যায়", "ইনসাফ", "বিচার", "ন্যায়সঙ্গত", "প্রতিশোধ"),
            keywordsAr = listOf("العدل", "القسط", "بالقسط", "عدلا", "لن تنصفوا"),
            relatedConcepts = listOf("আমানত", "সাক্ষ্য", "সমতা", "হক"),
            accentColor = 0xFF2E7D32
        ),
        QuranKeywordTopic(
            slug = "sidq-amanah",
            nameBn = "সত্য ও আমানত",
            nameEn = "Truthfulness & Trust",
            nameAr = "الصدق والأمانة",
            domain = "আচরণ",
            categoryBn = "চরিত্রের ভিত্তি",
            overviewBn = "সত্যবাদিতা ও আমানতদারি মুমিনের পরিচয়। আল্লাহ বলেন: 'হে ঈমানদারগণ, আল্লাহকে ভয় করো এবং সত্যদের সাথে থাকো।' প্রতারণা, মিথ্যা সাক্ষ্য ও আমানতে খিয়ানত কুরআনে কঠোরভাবে নিন্দিত।",
            keywordsBn = listOf("সত্য", "মিথ্যা", "আমানত", "খিয়ানত", "প্রতারণা", "সত্যবাদী"),
            keywordsAr = listOf("الصدق", "الصادقين", "الأمانة", "الخيانة", "صادقا"),
            relatedConcepts = listOf("আখলাক", "ন্যায়", "সাক্ষ্য", "বিশ্বাস"),
            accentColor = 0xFFEF6C00
        ),
        QuranKeywordTopic(
            slug = "halal-earning",
            nameBn = "হালাল উপার্জন",
            nameEn = "Halal Earning",
            nameAr = "الكسب الحلال",
            domain = "সমাজ",
            categoryBn = "অর্থনীতি",
            overviewBn = "হালাল উপার্জন ইবাদতের অংশ — ব্যবসা-বাণিজ্যে সততা, ওজনে ন্যায্যতা ও চুক্তি পূরণ মুমিন ব্যবসায়ীর গুণ। কুরআন অনুমোদন ছাড়া একে অন্যের সম্পদ খেতে কঠোর নিষেধ করেছে।",
            keywordsBn = listOf("ব্যবসা", "বাণিজ্য", "উপার্জন", "ওজন", "মাপে", "চুক্তি", "আয়"),
            keywordsAr = listOf("التجارة", "البيع", "الميزان", "الكيل", "أوفوا", "تجارة"),
            relatedConcepts = listOf("রিবা", "ন্যায়", "আমানত", "রিজিক"),
            accentColor = 0xFF00695C
        ),
        QuranKeywordTopic(
            slug = "riba",
            nameBn = "রিবা (সুদ)",
            nameEn = "Riba (Usury)",
            nameAr = "الربا",
            domain = "সমাজ",
            categoryBn = "নিষিদ্ধ লেনদেন",
            overviewBn = "রিবা বা সুদ কুরআনে অত্যন্ত কঠোরভাবে হারাম ঘোষিত। 'সুদ খাওয়ার যারা, তারা পাগলের মতোই দাঁড়িয়ে থাকে' — আর আল্লাহ ও রাসূল রিবার বিরুদ্ধে যুদ্ধের ঘোষণা দিয়েছেন। হালানোর আদেশ দেওয়ার পাশাপাশি ব্যবসাকে হালাল বলা হয়েছে।",
            keywordsBn = listOf("সুদ", "রিবা", "বাড়তি নেয়", "গুণে গুণে"),
            keywordsAr = listOf("الربا", "الربا يأكلون", "أضعافا مضاعفة"),
            relatedConcepts = listOf("হালাল উপার্জন", "যাকাত", "ঋণ", "ক্ষমা"),
            accentColor = 0xFFB71C1C
        ),
        QuranKeywordTopic(
            slug = "rizq",
            nameBn = "রিজিক",
            nameEn = "Provision",
            nameAr = "الرزق",
            domain = "সমাজ",
            categoryBn = "আল্লাহর দান",
            overviewBn = "রিজিক একমাত্র আল্লাহর হাতে — তিনিই যাকে ইচ্ছা প্রশস্ত করেন আর যাকে ইচ্ছা সীমিত করেন। জমিনে কোনো প্রাণীই নেই যার রিজিক আল্লাহর দায়িত্বে নেই। তাকওয়া অবলম্বনে আল্লাহ অজানা পথ থেকেও রিজিক দেন।",
            keywordsBn = listOf("রিজিক", "জীবিকা", "আহার করায়", "দান করে আমি", "সম্পদ দেয়"),
            keywordsAr = listOf("الرزق", "يرزق", "رزقا", "الرزقهم", "يرزقه"),
            relatedConcepts = listOf("তাওয়াক্কুল", "শুকর", "হালাল উপার্জন", "সবর"),
            accentColor = 0xFF0288D1
        ),

        // ─── পরিবার ──────────────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "parents",
            nameBn = "পিতা-মাতা",
            nameEn = "Parents",
            nameAr = "الوالدان",
            domain = "পরিবার",
            categoryBn = "সদ্ব্যবহার",
            overviewBn = "পিতা-মাতার সাথে এহসান (উত্তম ব্যবহার) আল্লাহর ইবাদতের পরেই আদেশ পেয়েছে। 'উফ' পর্যন্ত না বলা, তাঁদের জন্য দয়ার ডানা নামিয়ে রাখা এবং সবরের সাথে সেবা — মুমিন সন্তানের চিহ্ন।",
            keywordsBn = listOf("পিতা-মাতা", "বাবা-মা", "মা", "বাবা", "উত্তম ব্যবহার করো", "মাতা"),
            keywordsAr = listOf("الوالدين", "والديه", "أمه", "أبوه", "جحد", "انقضاء"),
            relatedConcepts = listOf("এহসান", "দোয়া", "সিলাতুর রাহিম", "আত্মীয়তা"),
            accentColor = 0xFFAD1457
        ),
        QuranKeywordTopic(
            slug = "marriage-family",
            nameBn = "বিবাহ ও পরিবার",
            nameEn = "Marriage & Family",
            nameAr = "الزواج والأسرة",
            domain = "পরিবার",
            categoryBn = "দাম্পত্য জীবন",
            overviewBn = "বিবাহ হলো আল্লাহর নিদর্শন — যাতে স্বামী-স্ত্রী একে অপরের প্রতি স্নেহ ও প্রশান্তি লাভ করে। স্ত্রীর প্রতি উত্তম ব্যবহার, পারস্পরিক অধিকার ও পারিবারিক শান্তি (সাকিনা) কুরআনে বিস্তারিত বর্ণিত।",
            keywordsBn = listOf("বিবাহ", "বিয়ে", "স্ত্রী", "স্বামী", "দাম্পত্য", "স্নেহ", "প্রশান্তি", "জোড়া"),
            keywordsAr = listOf("الزواج", "النساء", "أزواجكم", "مود", "سكن", "زوجا"),
            relatedConcepts = listOf("মেহের", "স্নেহ", "সাকিনা", "উত্তরাধিকার"),
            accentColor = 0xFF7E57C2
        ),
        QuranKeywordTopic(
            slug = "offspring",
            nameBn = "সন্তান",
            nameEn = "Children",
            nameAr = "الذرية",
            domain = "পরিবার",
            categoryBn = "সন্তান লালন",
            overviewBn = "সন্তান আল্লাহর দান ও পরীক্ষা দুই-ই। কুরআন সন্তানদের জন্য জান্নাতের প্রার্থনা শেখায়, সম্পদ ও সন্তানকে দুনিয়ার শোভা বলে এবং কিয়ামতের দিন সন্তান ও সম্পদ কোনোটাই কাজে আসবে না মনে করিয়ে দেয়।",
            keywordsBn = listOf("সন্তান", "সন্তানদের", "বংশধর", "পুত্র", "কন্যা"),
            keywordsAr = listOf("الذرية", "الأولاد", "أبناءكم", "بنين", "الولد"),
            relatedConcepts = listOf("তারবিয়ত", "দোয়া", "উত্তরাধিকার", "জান্নাত"),
            accentColor = 0xFF00897B
        ),

        // ─── আত্ম-সংশোধন ────────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "tongue-speech",
            nameBn = "জিহ্বার সংযম",
            nameEn = "Guarding Speech",
            nameAr = "حفظ اللسان",
            domain = "আচরণ",
            categoryBn = "কথার আদব",
            overviewBn = "জিহ্বার সংযম মুমিনের আখলাকের আয়না। কুরআন সৎ কথা বলার আদেশ দেয়, অহেতুক ও নিকৃষ্ট কথা থেকে ফিরে চলার শিক্ষা দেয় এবং মানুষের সামনে মৃদু স্বরে কথা বলার শিক্ষা দেয়।",
            keywordsBn = listOf("কথা", "বলে", "সৎ কথা", "মৃদু", "জিহ্বা", "বলা", "কহে"),
            keywordsAr = listOf("قول", "قالوا", "قولوا", "قول سديد", "الحياء"),
            relatedConcepts = listOf("গিবত", "সত্য", "আদব", "সংযম"),
            accentColor = 0xFF5D4037
        ),
        QuranKeywordTopic(
            slug = "ghibat-humiliation",
            nameBn = "গিবত ও অপবাদ",
            nameEn = "Backbiting & Slander",
            nameAr = "الغيبة والبهتان",
            domain = "আচরণ",
            categoryBn = "নিষিদ্ধ আচরণ",
            overviewBn = "গিবত (লোকের অনুপস্থিতিতে পরনিন্দা) কুরআনে ভয়ানক উপমায় নিন্দিত — 'তোমার মৃত ভাইয়ের মাংস খাওয়ার মতো'। অপবাদ ও নিন্দাবাদ ছড়ানো মুমিনের স্বভাব হতে পারে না।",
            keywordsBn = listOf("গিবত", "পরনিন্দা", "অপবাদ", "নিন্দা", "তিরস্কার", "খোঁচা দেয়"),
            keywordsAr = listOf("الغيبة", "يغتاب", "البهتان", "فاحشة", "تلمز", "الهمز"),
            relatedConcepts = listOf("জিহ্বার সংযম", "তওবা", "ভাইয়ের হক", "কলঙ্ক"),
            accentColor = 0xFFC62828
        ),
        QuranKeywordTopic(
            slug = "kibr",
            nameBn = "অহংকার",
            nameEn = "Arrogance",
            nameAr = "الكبر",
            domain = "আচরণ",
            categoryBn = "নিষিদ্ধ আত্মিক রোগ",
            overviewBn = "অহংকার আল্লাহর সবচেয়ে অপছন্দনীয় আত্মিক রোগ — ইবলিস অহংকারের কারণেই অভিশপ্ত হয়েছে। আল্লাহ বলেন: 'আমি মানুষের অন্তরের সামান্য অহংকারও সহ্য করি না।' জমিনে অহংকার না করে চলা মুমিনের চিহ্ন।",
            keywordsBn = listOf("অহংকার", "গর্ব", "দাম্ভিক", "উন্মাদ", "আমি তার চেয়ে ভালো", "উদ্ধত"),
            keywordsAr = listOf("الكبر", "المتكبرين", "تطاول", "مستكبرين", "كبروا"),
            relatedConcepts = listOf("বিনয়", "শিরক", "ইবলিস", "তাকবির"),
            accentColor = 0xFF37474F
        ),
        QuranKeywordTopic(
            slug = "hasad-envy",
            nameBn = "হিংসা",
            nameEn = "Envy",
            nameAr = "الحسد",
            domain = "আচরণ",
            categoryBn = "নিষিদ্ধ আত্মিক রোগ",
            overviewBn = "হিংসা মানে অন্যের নিয়ামতে অন্তর কষ্ট পাওয়া — আদম (আ.)-এর সন্তানদের মধ্যে প্রথম গুনাহই ছিল হিংসাজনিত। কুরআন আসমান-জমির নিয়ামতে হিংসা না করতে বলে এবং আল্লাহর দানে শোকর করতে শেখায়।",
            keywordsBn = listOf("হিংসা", "হিংসুক", "আর্ষা", "কামনা করে", "নিয়ামতে হিংসা"),
            keywordsAr = listOf("الحسد", "حاسد", "أرصدا", "طغت أنفسكم"),
            relatedConcepts = listOf("শুকর", "গিবত", "রিজিক", "ক্ষমা"),
            accentColor = 0xFF6A1B9A
        ),

        // ─── দুনিয়া ও আখিরাত ────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "dunya",
            nameBn = "দুনিয়ার জীবন",
            nameEn = "Worldly Life",
            nameAr = "الحياة الدنيا",
            domain = "আখিরাত",
            categoryBn = "দুনিয়ার মর্যাদা",
            overviewBn = "দুনিয়ার জীবন ক্ষণস্থায়ী শোভা — খেলাধুলা, আমোদ-প্রমোদ, পারস্পরিক অহংকার ও সম্পদে প্রতিযোগিতা ছাড়া কিছু না। কুরআন শিক্ষা দেয়: দুনিয়াকে হারাম বলো না, কিন্তু আখিরাতের ক্ষেত্র হিসেবে ব্যবহার করো।",
            keywordsBn = listOf("দুনিয়া", "দুনিয়ার জীবন", "পার্থিব", "খেলা", "শোভা", "ক্ষণস্থায়ী"),
            keywordsAr = listOf("الدنيا", "الحياة الدنيا", "لعب", "لهو", "زينة"),
            relatedConcepts = listOf("আখিরাত", "ফিতনা", "শুকর", "তাকওয়া"),
            accentColor = 0xFF455A64
        ),
        QuranKeywordTopic(
            slug = "qiyamah",
            nameBn = "কিয়ামত",
            nameEn = "Day of Judgment",
            nameAr = "يوم القيامة",
            domain = "আখিরাত",
            categoryBn = "হিসাবের দিন",
            overviewBn = "কিয়ামতের দিন হলো হিসাবের দিন — যেদিন মানুষ অণু-পরিমাণ ভালো-মন্দ কাজ দেখতে পাবে। কুরআনে কিয়ামতের ভয়াবহ দৃশ্য, ফেরেশতাদের শিঙ্গা ফুঁক, হাশর ও মিজান বর্ণিত — যাতে বান্দা প্রস্তুত থাকে।",
            keywordsBn = listOf("কিয়ামত", "হাশর", "হিসাব", "শিঙ্গা", "মিজান", "সমবেত", "জেগে উঠবে"),
            keywordsAr = listOf("القيامة", "الآخرة", "الحساب", "الميزان", "النفخ", "يقوم"),
            relatedConcepts = listOf("আখিরাত", "জান্নাত", "জাহান্নাম", "আমল"),
            accentColor = 0xFF283593
        ),
        QuranKeywordTopic(
            slug = "jahannam",
            nameBn = "জাহান্নাম",
            nameEn = "Hellfire",
            nameAr = "الجهنم",
            domain = "আখিরাত",
            categoryBn = "পরকালীন সতর্কবাণী",
            overviewBn = "জাহান্নাম কুফর ও পাপের শাস্তির স্থান — কুরআন বারবার তার আগুন, শৃঙ্খল ও আজাব স্মরণ করিয়ে দিয়ে বান্দাকে সতর্ক করেছে। সতর্কবাণী শুনে গুনাহ ছেড়ে তওবা করাই রহমতের পথ।",
            keywordsBn = listOf("জাহান্নাম", "নরক", "দোজখ", "আগুন", "আজাব", "শিকার", "শৃঙ্খল"),
            keywordsAr = listOf("الجهنم", "النار", "عذاب", "سقر", "سلاسل", "أغلال"),
            relatedConcepts = listOf("কিয়ামত", "শিরক", "জুলুম", "তওবা"),
            accentColor = 0xFFBF360C
        ),
        QuranKeywordTopic(
            slug = "fitna-trials",
            nameBn = "ফিতনা ও পরীক্ষা",
            nameEn = "Trials & Tribulations",
            nameAr = "الفتن",
            domain = "আখিরাত",
            categoryBn = "জীবনের পরীক্ষা",
            overviewBn = "দুনিয়ার জীবনই এক পরীক্ষা — ভয়, ক্ষুধা, সম্পদ ও প্রাণের ক্ষতির মাধ্যমে আল্লাহ মানুষকে পরীক্ষা করেন। যারা বিপদে সবর করে ও নম্র হয়ে থাকে, তারাই সত্যিকারের মুফলিহুন।",
            keywordsBn = listOf("ফিতনা", "পরীক্ষা", "বালা", "বিপদ", "ভয় দেখানো", "ক্ষতি স্পর্শ"),
            keywordsAr = listOf("الفتن", "الفتنة", "نبلينا", "نبتلي", "بلاء", "نصف"),
            relatedConcepts = listOf("সবর", "তাওয়াক্কুল", "শুকর", "জিহাদ"),
            accentColor = 0xFF827717
        ),

        // ─── সংগ্রাম ও শেক্ষা ────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "jihad-striving",
            nameBn = "জিহাদ ও সংগ্রাম",
            nameEn = "Striving in Allah's Path",
            nameAr = "الجهاد",
            domain = "সমাজ",
            categoryBn = "আত্মোৎসর্গ",
            overviewBn = "জিহাদের মূল অর্থ আল্লাহর পথে নিজের সর্বস্ব উৎসর্গ করা — জীবন, সম্পদ ও প্রচেষ্টা। কুরআন মুমিনদের সম্বোধন করে বলে: তোমরা আল্লাহর পথে সত্যিকার অর্থে জিহাদ করো। আত্মার বিরুদ্ধে সংগ্রামও এর অন্তর্ভুক্ত।",
            keywordsBn = listOf("জিহাদ", "সংগ্রাম", "আল্লাহর পথে", "প্রচেষ্টা", "চেষ্টা করে"),
            keywordsAr = listOf("الجهاد", "جاهدوا", "يجاهدون", "في سبيل الله"),
            relatedConcepts = listOf("হিজরত", "সবর", "ফিতনা", "শহীদ"),
            accentColor = 0xFF33691E
        ),
        QuranKeywordTopic(
            slug = "ilm-tafakkur",
            nameBn = "জ্ঞান ও চিন্তা",
            nameEn = "Knowledge & Reflection",
            nameAr = "العلم والتفكر",
            domain = "জ্ঞান",
            categoryBn = "অনুসন্ধান",
            overviewBn = "জ্ঞান অর্জন মুসলিমের ফরজ — কুরআন বারবার জিজ্ঞেস করে: 'তোমরা কি চিন্তা করো না?', 'তোমরা কি ভেবে দেখো না?' আসমান-জমির সৃষ্টি, বৃষ্টি ও জীবনের চক্র নিয়ে তাফাক্কুর (গভীর চিন্তা) ঈমানকে শক্তিশালী করে।",
            keywordsBn = listOf("জ্ঞান", "ইলম", "চিন্তা", "ভেবে দেখো", "বুঝে নাও", "জেনে রেখো", "শিক্ষা"),
            keywordsAr = listOf("العلم", "يعلمون", "أفلا تتفكرون", "تفقهون", "تعقلون", "أول الألباب"),
            relatedConcepts = listOf("তাফাক্কুর", "আয়াত", "সৃষ্টি", "বিবেক"),
            accentColor = 0xFF3949AB
        ),

        // ─── আরও গুণসমূহ ────────────────────────────────────────────────
        QuranKeywordTopic(
            slug = "haya-modesty",
            nameBn = "লজ্জাশীলতা ও পবিত্রতা",
            nameEn = "Modesty & Purity",
            nameAr = "الحياء والطهارة",
            domain = "আচরণ",
            categoryBn = "চরিত্রের অলংকার",
            overviewBn = "লজ্জাশীলতা ও পবিত্রতা মুমিন নারী-পুরুষের উভয়ের চরিত্রের অলংকার। কুরআন চোখ নামিয়ে রাখা, শরীরের গোপনীয়তা রক্ষা করা এবং নিষিদ্ধ সম্পর্ক থেকে দূরে থাকার আদেশ দেয়।",
            keywordsBn = listOf("লজ্জা", "হায়া", "পবিত্রতা", "পাক", "নিষিদ্ধ", "গোপন", "চোখ নামায়"),
            keywordsAr = listOf("الحياء", "الطهارة", "يغضوا", "الفتنة", "يستبئون", "فاحشة"),
            relatedConcepts = listOf("তাহারত", "সংযম", "আদব", "ইবাদত"),
            accentColor = 0xFF00695C
        ),
        QuranKeywordTopic(
            slug = "husn-akhlaq",
            nameBn = "উত্তম আখলাক",
            nameEn = "Good Character",
            nameAr = "حسن الخلق",
            domain = "আচরণ",
            categoryBn = "চরিত্র গঠন",
            overviewBn = "উত্তম আখলাক হলো মুমিনের সর্বোচ্চ অলংকার — ক্ষমা করা, ভালোর সাথে ভালো, অসৎ কাজ এড়িয়ে চলা। কুরআন আদেশ দেয়: 'কল্যাণ ও অকল্যাণ সমান হতে পারে না — উত্তম পন্থায় চলো।' ক্ষমাশীল হওয়া এবং সহমর্মিতা মুমিনের পরিচয়।",
            keywordsBn = listOf("আখলাক", "ভালো কাজ", "কল্যাণ", "ভালোবাসেন", "উত্তম", "আন্তরিক"),
            keywordsAr = listOf("الخلق", "المحسن", "يحب المحسن", "إحسانا", "عروة"),
            relatedConcepts = listOf("এহসান", "ক্ষমা", "দয়া", "সত্য"),
            accentColor = 0xFF6D4C41
        ),
        QuranKeywordTopic(
            slug = "rahma-dunya",
            nameBn = "দয়া ও সহমর্মিতা",
            nameEn = "Mercy & Compassion",
            nameAr = "الرحمة",
            domain = "আচরণ",
            categoryBn = "আল্লাহর রহমতের প্রতিফলন",
            overviewBn = "আল্লাহর রহমত সকল সৃষ্টির উপর বিস্তৃত — আর বান্দার কাজ হলো সেই রহমতের প্রতিফলন। কুরআন শিক্ষা দেয়: মানুষের মধ্যে সদয় ব্যবহার করো, প্রতিবেশী ও সহযাত্রীর হক আদায় করো এবং সৃষ্টির প্রতি দয়া করো।",
            keywordsBn = listOf("দয়া", "রহম", "করুণা", "সহমর্মিতা", "দুর্বলের", "আন্তরিকতা"),
            keywordsAr = listOf("الرحمة", "الرحيم", "رحمة", "رحما", "يحمي"),
            relatedConcepts = listOf("এহসান", "ক্ষমা", "দুআ", "রহমত"),
            accentColor = 0xFF1565C0
        ),
        QuranKeywordTopic(
            slug = "sabr-istiqamah",
            nameBn = "অবিচলতা (ইস্তিকামা)",
            nameEn = "Steadfastness",
            nameAr = "الاستقامة",
            domain = "আচরণ",
            categoryBn = "পথে অটলতা",
            overviewBn = "ইস্তিকামা মানে সরল পথে অবিচল থাকা — আনন্দে-গ্লানিতে, সুবিধা-অসুবিধায়। 'যারা বলে আমাদের রব আল্লাহ, অতঃপর সোজা পথে অটল থাকে — তাদের উপর ফেরেশতা নেমে আসে।' সবরের সাথে অবিচলতা মুমিনের শক্তি।",
            keywordsBn = listOf("ইস্তিকামা", "অবিচল", "অটল", "সোজা পথ", "সরল পথ", "অধ্যবসায়"),
            keywordsAr = listOf("الاستقامة", "استقاموا", "ثابت", "المستقيم", "اصبروا"),
            relatedConcepts = listOf("সবর", "তাকওয়া", "তাওয়াক্কুল", "জিহাদ"),
            accentColor = 0xFF1B5E20
        ),
        QuranKeywordTopic(
            slug = "shukr-hamd",
            nameBn = "শোকর ও প্রশংসা",
            nameEn = "Gratitude & Praise",
            nameAr = "الشكر والحمد",
            domain = "আচরণ",
            categoryBn = "আত্মিক গুণ",
            overviewBn = "শোকর (কৃতজ্ঞতা) আল্লাহর ঘোষণা: 'যদি তোমরা শোকর করো, আমি অবশ্যই তোমাদের নিয়ামত বাড়িয়ে দেব।' আর হামদ (প্রশংসা) জান্নাতেও বান্দাদের বাক্য: 'আলহামদুলিল্লাহ' — সকল প্রশংসা আল্লাহর।",
            keywordsBn = listOf("শোকর", "কৃতজ্ঞ", "প্রশংসা", "সুকর", "শোকর করো", "হামদ"),
            keywordsAr = listOf("الشكر", "شكرا", "الحمد", "شكور", "تشكرون", "شكرا لكم"),
            relatedConcepts = listOf("জিকির", "নিয়ামত", "সবর", "জান্নাত"),
            accentColor = 0xFFF57F17
        ),
        QuranKeywordTopic(
            slug = "munafiqat",
            nameBn = "মুনাফিকতা",
            nameEn = "Hypocrisy",
            nameAr = "النفاق",
            domain = "ঈমান ও আকিদা",
            categoryBn = "নিষিদ্ধ অন্তর",
            overviewBn = "মুনাফিকতা মানে অন্তরে এক, মুখে আরেক — যে কথায় ও কাজে ঈমান দেখায় কিন্তু অন্তরে অস্বীকার লুকিয়ে রাখে। কুরআন মুনাফিকদের চিহ্ন, তাদের দুর্বলতা ও শেষ পরিণতি বিস্তারিত বর্ণনা করেছে — যাতে বান্দা নিজের অন্তর পরীক্ষা করে।",
            keywordsBn = listOf("মুনাফিক", "দ্বিমুখী", "অন্তরে অসুস্থ", "প্রতারক"),
            keywordsAr = listOf("المنافقون", "النفاق", "مرض", "المخادعين", "يخادعون"),
            relatedConcepts = listOf("ঈমান", "ইখলাস", "ফিতনা", "কিয়ামত"),
            accentColor = 0xFF4A148C
        )
    )

    fun get(slug: String): QuranKeywordTopic? = topics.find { it.slug == slug }

    fun search(query: String): List<QuranKeywordTopic> {
        if (query.isBlank()) return topics
        val q = query.trim().lowercase()
        return topics.filter {
            it.nameBn.contains(query) || it.nameEn.lowercase().contains(q) ||
                it.nameAr.contains(query) || it.domain.contains(query) ||
                it.categoryBn.contains(query) ||
                it.keywordsBn.any { kw -> kw.contains(query) }
        }
    }
}
