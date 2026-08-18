package com.islamichub.app.data.local

import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.DhikrOption
import com.islamichub.app.data.model.Dua
import com.islamichub.app.data.model.NameOfAllah
import com.islamichub.app.data.model.RevelationType
import com.islamichub.app.data.model.Surah

/**
 * Bundled Quran data. For build size and licensing reasons, this reference build
 * ships with a curated selection of shorter surahs (full text + translation).
 * The surah *list* still contains all 114 names so the browsing experience is
 * complete; the reader shows full text for the bundled surahs and a placeholder
 * for the rest.
 */
object QuranData {

    /** Full metadata for all 114 surahs (number, name, type, ayah count). */
    val allSurahMetadata: List<SurahMeta> = listOf(
        SurahMeta(1, "الفاتحة", "Al-Fatihah", "আল-ফাতিহা", "The Opening", RevelationType.MECCAN, 7),
        SurahMeta(2, "البقرة", "Al-Baqarah", "আল-বাকারা", "The Cow", RevelationType.MEDINAN, 286),
        SurahMeta(3, "آل عمران", "Aal-i-Imran", "আলে-ইমরান", "The Family of Imran", RevelationType.MEDINAN, 200),
        SurahMeta(4, "النساء", "An-Nisa", "আন-নিসা", "The Women", RevelationType.MEDINAN, 176),
        SurahMeta(5, "المائدة", "Al-Ma'idah", "আল-মায়িদা", "The Table Spread", RevelationType.MEDINAN, 120),
        SurahMeta(6, "الأنعام", "Al-An'am", "আল-আনআম", "The Cattle", RevelationType.MECCAN, 165),
        SurahMeta(7, "الأعراف", "Al-A'raf", "আল-আরাফ", "The Heights", RevelationType.MECCAN, 206),
        SurahMeta(8, "الأنفال", "Al-Anfal", "আল-আনফাল", "The Spoils of War", RevelationType.MEDINAN, 75),
        SurahMeta(9, "التوبة", "At-Tawbah", "আত-তাওবা", "The Repentance", RevelationType.MEDINAN, 129),
        SurahMeta(10, "يونس", "Yunus", "ইউনুস", "Jonas", RevelationType.MECCAN, 109),
        SurahMeta(11, "هود", "Hud", "হুদ", "Hud", RevelationType.MECCAN, 123),
        SurahMeta(12, "يوسف", "Yusuf", "ইউসুফ", "Joseph", RevelationType.MECCAN, 111),
        SurahMeta(13, "الرعد", "Ar-Ra'd", "আর-রাদ", "The Thunder", RevelationType.MEDINAN, 43),
        SurahMeta(14, "إبراهيم", "Ibrahim", "ইব্রাহিম", "Abraham", RevelationType.MECCAN, 52),
        SurahMeta(15, "الحجر", "Al-Hijr", "আল-হিজর", "The Rocky Tract", RevelationType.MECCAN, 99),
        SurahMeta(16, "النحل", "An-Nahl", "আন-নাহল", "The Bee", RevelationType.MECCAN, 128),
        SurahMeta(17, "الإسراء", "Al-Isra", "আল-ইসরা", "The Night Journey", RevelationType.MECCAN, 111),
        SurahMeta(18, "الكهف", "Al-Kahf", "আল-কাহফ", "The Cave", RevelationType.MECCAN, 110),
        SurahMeta(19, "مريم", "Maryam", "মারইয়াম", "Mary", RevelationType.MECCAN, 98),
        SurahMeta(20, "طه", "Taha", "ত্ব-হা", "Ta-Ha", RevelationType.MECCAN, 135),
        SurahMeta(21, "الأنبياء", "Al-Anbiya", "আল-আম্বিয়া", "The Prophets", RevelationType.MECCAN, 112),
        SurahMeta(22, "الحج", "Al-Hajj", "আল-হাজ্জ", "The Pilgrimage", RevelationType.MEDINAN, 78),
        SurahMeta(23, "المؤمنون", "Al-Mu'minun", "আল-মুমিনুন", "The Believers", RevelationType.MECCAN, 118),
        SurahMeta(24, "النور", "An-Nur", "আন-নূর", "The Light", RevelationType.MEDINAN, 64),
        SurahMeta(25, "الفرقان", "Al-Furqan", "আল-ফুরকান", "The Criterion", RevelationType.MECCAN, 77),
        SurahMeta(26, "الشعراء", "Ash-Shu'ara", "আশ-শুআরা", "The Poets", RevelationType.MECCAN, 227),
        SurahMeta(27, "النمل", "An-Naml", "আন-নামল", "The Ant", RevelationType.MECCAN, 93),
        SurahMeta(28, "القصص", "Al-Qasas", "আল-কাসাস", "The Stories", RevelationType.MECCAN, 88),
        SurahMeta(29, "العنكبوت", "Al-Ankabut", "আল-আনকাবূত", "The Spider", RevelationType.MECCAN, 69),
        SurahMeta(30, "الروم", "Ar-Rum", "আর-রূম", "The Romans", RevelationType.MECCAN, 60),
        SurahMeta(31, "لقمان", "Luqman", "লুকমান", "Luqman", RevelationType.MECCAN, 34),
        SurahMeta(32, "السجدة", "As-Sajdah", "আস-সাজদা", "The Prostration", RevelationType.MECCAN, 30),
        SurahMeta(33, "الأحزاب", "Al-Ahzab", "আল-আহযাব", "The Clans", RevelationType.MEDINAN, 73),
        SurahMeta(34, "سبأ", "Saba", "সাবা", "Sheba", RevelationType.MECCAN, 54),
        SurahMeta(35, "فاطر", "Fatir", "ফাতির", "The Originator", RevelationType.MECCAN, 45),
        SurahMeta(36, "يس", "Ya-Sin", "ইয়াসিন", "Ya Sin", RevelationType.MECCAN, 83),
        SurahMeta(37, "الصافات", "As-Saffat", "আস-সাফফাত", "Those drawn up in Ranks", RevelationType.MECCAN, 182),
        SurahMeta(38, "ص", "Sad", "সাদ", "The Letter Sad", RevelationType.MECCAN, 88),
        SurahMeta(39, "الزمر", "Az-Zumar", "আয-যুমার", "The Groups", RevelationType.MECCAN, 75),
        SurahMeta(40, "غافر", "Ghafir", "গাফির", "The Forgiver", RevelationType.MECCAN, 85),
        SurahMeta(41, "فصلت", "Fussilat", "ফুসসিলাত", "Explained in detail", RevelationType.MECCAN, 54),
        SurahMeta(42, "الشورى", "Ash-Shura", "আশ-শূরা", "Consultation", RevelationType.MECCAN, 53),
        SurahMeta(43, "الزخرف", "Az-Zukhruf", "আয-যুখরুফ", "Ornaments of gold", RevelationType.MECCAN, 89),
        SurahMeta(44, "الدخان", "Ad-Dukhan", "আদ-দুখান", "The Smoke", RevelationType.MECCAN, 59),
        SurahMeta(45, "الجاثية", "Al-Jathiyah", "আল-জাসিয়া", "Crouching", RevelationType.MECCAN, 37),
        SurahMeta(46, "الأحقاف", "Al-Ahqaf", "আল-আহকাফ", "The Dunes", RevelationType.MECCAN, 35),
        SurahMeta(47, "محمد", "Muhammad", "মুহাম্মাদ", "Muhammad", RevelationType.MEDINAN, 38),
        SurahMeta(48, "الفتح", "Al-Fath", "আল-ফাতহ", "The Victory", RevelationType.MEDINAN, 29),
        SurahMeta(49, "الحجرات", "Al-Hujurat", "আল-হুজুরাত", "The Inner Apartments", RevelationType.MEDINAN, 18),
        SurahMeta(50, "ق", "Qaf", "ক্বাফ", "The Letter Qaf", RevelationType.MECCAN, 45),
        SurahMeta(51, "الذاريات", "Adh-Dhariyat", "আয-যারিয়াত", "The Winnowing Winds", RevelationType.MECCAN, 60),
        SurahMeta(52, "الطور", "At-Tur", "আত-তূর", "The Mount", RevelationType.MECCAN, 49),
        SurahMeta(53, "النجم", "An-Najm", "আন-নাজম", "The Star", RevelationType.MECCAN, 62),
        SurahMeta(54, "القمر", "Al-Qamar", "আল-কামার", "The Moon", RevelationType.MECCAN, 55),
        SurahMeta(55, "الرحمن", "Ar-Rahman", "আর-রহমান", "The Beneficent", RevelationType.MEDINAN, 78),
        SurahMeta(56, "الواقعة", "Al-Waqi'ah", "আল-ওয়াকিয়া", "The Inevitable", RevelationType.MECCAN, 96),
        SurahMeta(57, "الحديد", "Al-Hadid", "আল-হাদীদ", "The Iron", RevelationType.MEDINAN, 29),
        SurahMeta(58, "المجادلة", "Al-Mujadila", "আল-মুজাদিলা", "The Pleading Woman", RevelationType.MEDINAN, 22),
        SurahMeta(59, "الحشر", "Al-Hashr", "আল-হাশর", "The Exile", RevelationType.MEDINAN, 24),
        SurahMeta(60, "الممتحنة", "Al-Mumtahanah", "আল-মুমতাহিনা", "She that is to be examined", RevelationType.MEDINAN, 13),
        SurahMeta(61, "الصف", "As-Saff", "আস-সাফ", "The Ranks", RevelationType.MEDINAN, 14),
        SurahMeta(62, "الجمعة", "Al-Jumu'ah", "আল-জুমুয়া", "The Congregation, Friday", RevelationType.MEDINAN, 11),
        SurahMeta(63, "المنافقون", "Al-Munafiqun", "আল-মুনাফিকূন", "The Hypocrites", RevelationType.MEDINAN, 11),
        SurahMeta(64, "التغابن", "At-Taghabun", "আত-তাগাবূন", "The Mutual Disillusion", RevelationType.MEDINAN, 18),
        SurahMeta(65, "الطلاق", "At-Talaq", "আত-তালাক", "The Divorce", RevelationType.MEDINAN, 12),
        SurahMeta(66, "التحريم", "At-Tahrim", "আত-তাহরীম", "The Prohibition", RevelationType.MEDINAN, 12),
        SurahMeta(67, "الملك", "Al-Mulk", "আল-মুলক", "The Sovereignty", RevelationType.MECCAN, 30),
        SurahMeta(68, "القلم", "Al-Qalam", "আল-কালাম", "The Pen", RevelationType.MECCAN, 52),
        SurahMeta(69, "الحاقة", "Al-Haqqah", "আল-হাক্কা", "The Reality", RevelationType.MECCAN, 52),
        SurahMeta(70, "المعارج", "Al-Ma'arij", "আল-মাআরিজ", "The Ascending Stairways", RevelationType.MECCAN, 44),
        SurahMeta(71, "نوح", "Nuh", "নূহ", "Noah", RevelationType.MECCAN, 28),
        SurahMeta(72, "الجن", "Al-Jinn", "আল-জিন", "The Jinn", RevelationType.MECCAN, 28),
        SurahMeta(73, "المزمل", "Al-Muzzammil", "আল-মুজ্জাম্মিল", "The Enshrouded One", RevelationType.MECCAN, 20),
        SurahMeta(74, "المدثر", "Al-Muddaththir", "আল-মুদ্দাস্সির", "The Cloaked One", RevelationType.MECCAN, 56),
        SurahMeta(75, "القيامة", "Al-Qiyamah", "আল-কিয়ামা", "The Resurrection", RevelationType.MECCAN, 40),
        SurahMeta(76, "الإنسان", "Al-Insan", "আল-ইনসান", "The Man", RevelationType.MEDINAN, 31),
        SurahMeta(77, "المرسلات", "Al-Mursalat", "আল-মুরসালাত", "The Emissaries", RevelationType.MECCAN, 50),
        SurahMeta(78, "النبأ", "An-Naba", "আন-নাবা", "The Announcement", RevelationType.MECCAN, 40),
        SurahMeta(79, "النازعات", "An-Nazi'at", "আন-নাযিআত", "Those who drag forth", RevelationType.MECCAN, 46),
        SurahMeta(80, "عبس", "Abasa", "আবাসা", "He frowned", RevelationType.MECCAN, 42),
        SurahMeta(81, "التكوير", "At-Takwir", "আত-তাকভীর", "The Overthrowing", RevelationType.MECCAN, 29),
        SurahMeta(82, "الإنفطار", "Al-Infitar", "আল-ইনফিতার", "The Cleaving", RevelationType.MECCAN, 19),
        SurahMeta(83, "المطففين", "Al-Mutaffifin", "আল-মুতাফফিফীন", "The Defrauding", RevelationType.MECCAN, 36),
        SurahMeta(84, "الإنشقاق", "Al-Inshiqaq", "আল-ইনশিকাক", "The Splitting Open", RevelationType.MECCAN, 25),
        SurahMeta(85, "البروج", "Al-Buruj", "আল-বুরূজ", "The Mansions of the Stars", RevelationType.MECCAN, 22),
        SurahMeta(86, "الطارق", "At-Tariq", "আত-তারিক", "The Morning Star", RevelationType.MECCAN, 17),
        SurahMeta(87, "الأعلى", "Al-A'la", "আল-আলা", "The Most High", RevelationType.MECCAN, 19),
        SurahMeta(88, "الغاشية", "Al-Ghashiyah", "আল-গাশিয়া", "The Overwhelming", RevelationType.MECCAN, 26),
        SurahMeta(89, "الفجر", "Al-Fajr", "আল-ফাজর", "The Dawn", RevelationType.MECCAN, 30),
        SurahMeta(90, "البلد", "Al-Balad", "আল-বালাদ", "The City", RevelationType.MECCAN, 20),
        SurahMeta(91, "الشمس", "Ash-Shams", "আশ-শামস", "The Sun", RevelationType.MECCAN, 15),
        SurahMeta(92, "الليل", "Al-Layl", "আল-লাইল", "The Night", RevelationType.MECCAN, 21),
        SurahMeta(93, "الضحى", "Ad-Duha", "আদ-দুহা", "The Morning Hours", RevelationType.MECCAN, 11),
        SurahMeta(94, "الشرح", "Ash-Sharh", "আশ-শারহ", "The Relief", RevelationType.MECCAN, 8),
        SurahMeta(95, "التين", "At-Tin", "আত-তীন", "The Fig", RevelationType.MECCAN, 8),
        SurahMeta(96, "العلق", "Al-Alaq", "আল-আলাক", "The Clot", RevelationType.MECCAN, 19),
        SurahMeta(97, "القدر", "Al-Qadr", "আল-কদর", "The Power", RevelationType.MECCAN, 5),
        SurahMeta(98, "البينة", "Al-Bayyinah", "আল-বাইয়িনা", "The Clear Proof", RevelationType.MEDINAN, 8),
        SurahMeta(99, "الزلزلة", "Az-Zalzalah", "আয-যিলযিল", "The Earthquake", RevelationType.MEDINAN, 8),
        SurahMeta(100, "العاديات", "Al-Adiyat", "আল-আদিয়াত", "The Courser", RevelationType.MECCAN, 11),
        SurahMeta(101, "القارعة", "Al-Qari'ah", "আল-কারিয়া", "The Calamity", RevelationType.MECCAN, 11),
        SurahMeta(102, "التكاثر", "At-Takathur", "আত-তাকাসুর", "The Rivalry in world increase", RevelationType.MECCAN, 8),
        SurahMeta(103, "العصر", "Al-Asr", "আল-আসর", "The Declining Day", RevelationType.MECCAN, 3),
        SurahMeta(104, "الهمزة", "Al-Humazah", "আল-হুমাযা", "The Traducer", RevelationType.MECCAN, 9),
        SurahMeta(105, "الفيل", "Al-Fil", "আল-ফীল", "The Elephant", RevelationType.MECCAN, 5),
        SurahMeta(106, "قريش", "Quraysh", "কুরাইশ", "Quraysh", RevelationType.MECCAN, 4),
        SurahMeta(107, "الماعون", "Al-Ma'un", "আল-মাউন", "The Small Kindnesses", RevelationType.MECCAN, 7),
        SurahMeta(108, "الكوثر", "Al-Kawthar", "আল-কাওসার", "The Abundance", RevelationType.MECCAN, 3),
        SurahMeta(109, "الكافرون", "Al-Kafirun", "আল-কাফিরূন", "The Disbelievers", RevelationType.MECCAN, 6),
        SurahMeta(110, "النصر", "An-Nasr", "আন-নাসর", "The Divine Support", RevelationType.MEDINAN, 3),
        SurahMeta(111, "المسد", "Al-Masad", "আল-মাসাদ", "The Palm Fiber", RevelationType.MECCAN, 5),
        SurahMeta(112, "الإخلاص", "Al-Ikhlas", "আল-ইখলাস", "The Sincerity", RevelationType.MECCAN, 4),
        SurahMeta(113, "الفلق", "Al-Falaq", "আল-ফালাক", "The Daybreak", RevelationType.MECCAN, 5),
        SurahMeta(114, "الناس", "An-Nas", "আন-নাস", "Mankind", RevelationType.MECCAN, 6)
    )

    /** Full-text surahs bundled with this build. */
    val fullSurahs: List<Surah> = listOf(
        Surah(
            number = 1,
            nameArabic = "الفاتحة",
            nameEnglish = "Al-Fatihah",
            nameBengali = "আল-ফাতিহা",
            englishMeaning = "The Opening",
            revelationType = RevelationType.MECCAN,
            ayahCount = 7,
            ayahs = listOf(
                Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "পরম করুণাময় অসীম দয়ালু আল্লাহর নামে।"),
                Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds —", "যাবতীয় প্রশংসা আল্লাহরই, যিনি সকল সৃষ্টিজগতের পালনকর্তা।"),
                Ayah(3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "নিগূঢ় করুণাময়, অসীম দয়ালু।"),
                Ayah(4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "বিচার দিনের মালিক।"),
                Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "আমরা কেবল তোমারই ইবাদত করি এবং কেবল তোমারই সাহায্য প্রার্থনা করি।"),
                Ayah(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path —", "আমাদেকে সরল পথ প্রদর্শন কর।"),
                Ayah(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "তাদের পথ, যাদেরকে তুমি অনুগ্রহ করেছ; যাদের উপর নয় (তোমার) ক্রোধ, আর যারা পথভ্রষ্টও নয়।")
            )
        ),
        Surah(
            number = 103,
            nameArabic = "العصر",
            nameEnglish = "Al-Asr",
            nameBengali = "আল-আসর",
            englishMeaning = "The Declining Day",
            revelationType = RevelationType.MECCAN,
            ayahCount = 3,
            ayahs = listOf(
                Ayah(1, "وَالْعَصْرِ", "By time,", "কসম কালের,"),
                Ayah(2, "إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ", "Indeed, mankind is in loss,", "নিশ্চয় মানুষ ক্ষতিগ্রস্ত,"),
                Ayah(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", "কিন্তু তারা ছাড়া, যারা ঈমান এনেছে এবং সৎকর্ম করেছে এবং পরস্পরকে সত্যের উপদেশ দিয়েছে ও পরস্পরকে ধৈর্যের উপদেশ দিয়েছে।")
            )
        ),
        Surah(
            number = 108,
            nameArabic = "الكوثر",
            nameEnglish = "Al-Kawthar",
            nameBengali = "আল-কাওসার",
            englishMeaning = "The Abundance",
            revelationType = RevelationType.MECCAN,
            ayahCount = 3,
            ayahs = listOf(
                Ayah(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", "নিশ্চয় আমরা তোমাকে কাওসার দান করেছি।"),
                Ayah(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "So pray to your Lord and sacrifice [to Him alone].", "অতএব তুমি তোমার রবের উদ্দেশ্যে নামায আদায় কর ও কুরবানী কর।"),
                Ayah(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Indeed, your enemy is the one cut off.", "নিশ্চয় তোমার শত্রুই নির্বংশ।")
            )
        ),
        Surah(
            number = 110,
            nameArabic = "النصر",
            nameEnglish = "An-Nasr",
            nameBengali = "আন-নাসর",
            englishMeaning = "The Divine Support",
            revelationType = RevelationType.MEDINAN,
            ayahCount = 3,
            ayahs = listOf(
                Ayah(1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", "When the victory of Allah has come and the conquest,", "যখন আল্লাহর সাহায্য ও বিজয় আসবে,"),
                Ayah(2, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", "And you see the people entering into the religion of Allah in multitudes,", "এবং তুমি মানুষকে দলে দলে আল্লাহর দ্বীনে প্রবেশ করতে দেখবে,"),
                Ayah(3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", "Then exalt [Him] with praise of your Lord and ask forgiveness of Him. Indeed, He is ever Accepting of repentance.", "তখন তুমি তোমার রবের প্রশংসাসহ পবিত্রতা ঘোষণা কর এবং তার কাছে ক্ষমা প্রার্থনা কর। নিশ্চয় তিনি তওবা গ্রহণকারী।")
            )
        ),
        Surah(
            number = 112,
            nameArabic = "الإخلاص",
            nameEnglish = "Al-Ikhlas",
            nameBengali = "আল-ইখলাস",
            englishMeaning = "The Sincerity",
            revelationType = RevelationType.MECCAN,
            ayahCount = 4,
            ayahs = listOf(
                Ayah(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", "বলুন, তিনি আল্লাহ, এক,"),
                Ayah(2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "আল্লাহ সর্বশ্রেষ্ঠ, অমুখাপেক্ষী।"),
                Ayah(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "তিনি কাউকে জন্ম দেননি এবং তাকেও জন্ম দেওয়া হয়নি,"),
                Ayah(4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"", "এবং তার সমতুল্য কেউ নয়।\"")
            )
        ),
        Surah(
            number = 113,
            nameArabic = "الفلق",
            nameEnglish = "Al-Falaq",
            nameBengali = "আল-ফালাক",
            englishMeaning = "The Daybreak",
            revelationType = RevelationType.MECCAN,
            ayahCount = 5,
            ayahs = listOf(
                Ayah(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", "বলুন, আমি আশ্রয় প্রার্থনা করি ভোরের পালনকর্তার,"),
                Ayah(2, "مِنْ شَرِّ مَا خَلَقَ", "From the evil of that which He created", "তার সৃষ্টির অমঙ্গল থেকে,"),
                Ayah(3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "এবং রাত্রির অন্ধকারের অমঙ্গল থেকে, যখন তা আচ্ছন্ন করে,"),
                Ayah(4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "এবং গিঁটে ফুঁক দেয়া নারীদের অমঙ্গল থেকে,"),
                Ayah(5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", "এবং বিদ্বেষীর অমঙ্গল থেকে, যখন সে বিদ্বেষ করে।\"")
            )
        ),
        Surah(
            number = 114,
            nameArabic = "الناس",
            nameEnglish = "An-Nas",
            nameBengali = "আন-নাস",
            englishMeaning = "Mankind",
            revelationType = RevelationType.MECCAN,
            ayahCount = 6,
            ayahs = listOf(
                Ayah(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", "বলুন, আমি আশ্রয় প্রার্থনা করি মানুষের পালনকর্তার,"),
                Ayah(2, "مَلِكِ النَّاسِ", "The Sovereign of mankind,", "মানুষের অধিপতির,"),
                Ayah(3, "إِلَٰهِ النَّاسِ", "God of mankind,", "মানুষের উপাস্যের,"),
                Ayah(4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer —", "কুৎসিত ফুসফুসকারীর অমঙ্গল থেকে,"),
                Ayah(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind —", "যে মানুষের বক্ষে ফুসফুস করে,"),
                Ayah(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"", "জিন ও মানুষের মধ্য থেকে।\"")
            )
        ),
        Surah(
            number = 109,
            nameArabic = "الكافرون",
            nameEnglish = "Al-Kafirun",
            nameBengali = "আল-কাফিরূন",
            englishMeaning = "The Disbelievers",
            revelationType = RevelationType.MECCAN,
            ayahCount = 6,
            ayahs = listOf(
                Ayah(1, "قُلْ يَا أَيُّهَا الْكَافِرُونَ", "Say, \"O disbelievers,", "বলুন, হে অবিশ্বাসীগণ!"),
                Ayah(2, "لَا أَعْبُدُ مَا تَعْبُدُونَ", "I do not worship what you worship.", "আমি তার ইবাদত করি না, যার ইবাদত তোমরা কর।"),
                Ayah(3, "وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ", "Nor are you worshippers of what I worship.", "এবং তোমরাও তার ইবাদতকারী নও, যার ইবাদত আমি করি।"),
                Ayah(4, "وَلَا أَنَا عَابِدٌ مَا عَبَدْتُمْ", "Nor will I be a worshipper of what you worship.", "এবং আমিও তার ইবাদতকারী নই, যার ইবাদত তোমরা করেছ।"),
                Ayah(5, "وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ", "Nor will you be worshippers of what I worship.", "এবং তোমরাও তার ইবাদতকারী নও, যার ইবাদত আমি করি।"),
                Ayah(6, "لَكُمْ دِينُكُمْ وَلِيَ دِينِ", "For you is your religion, and for me is my religion.\"", "তোমাদের জন্য তোমাদের দ্বীন এবং আমার জন্য আমার দ্বীন।\"")
            )
        ),
        Surah(
            number = 111,
            nameArabic = "المسد",
            nameEnglish = "Al-Masad",
            nameBengali = "আল-মাসাদ",
            englishMeaning = "The Palm Fiber",
            revelationType = RevelationType.MECCAN,
            ayahCount = 5,
            ayahs = listOf(
                Ayah(1, "تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ", "May the hands of Abu Lahab be ruined, and ruined is he.", "ধ্বংস হোক আবু লাহাবের দু'হাত এবং সে ধ্বংস হোক।"),
                Ayah(2, "مَا أَغْنَىٰ عَنْهُ مَالُهُ وَمَا كَسَبَ", "His wealth will not avail him or that which he gained.", "তার ধন-সম্পদ ও যা সে উপার্জন করেছে তা তার কোনো কাজে আসবে না।"),
                Ayah(3, "سَيَصْلَىٰ نَارًا ذَاتَ لَهَبٍ", "He will [enter to] burn in a Fire of [blazing] flame", "সে শীঘ্রই প্রজ্বলিত অগ্নিতে দগ্ধ হবে,"),
                Ayah(4, "وَامْرَأَتُهُ حَمَّالَةَ الْحَطَبِ", "And his wife [as well] — the carrier of firewood.", "এবং তার স্ত্রীও — যে কাঠ বহনকারী,"),
                Ayah(5, "فِي جِيدِهَا حَبْلٌ مِنْ مَسَدٍ", "Around her neck is a rope of [twisted] fiber.", "তার গলায় খেজুরের শিরার দড়ি।")
            )
        ),
        Surah(
            number = 107,
            nameArabic = "الماعون",
            nameEnglish = "Al-Ma'un",
            nameBengali = "আল-মাউন",
            englishMeaning = "The Small Kindnesses",
            revelationType = RevelationType.MECCAN,
            ayahCount = 7,
            ayahs = listOf(
                Ayah(1, "أَرَأَيْتَ الَّذِي يُكَذِّبُ بِالدِّينِ", "Have you seen the one who denies the Recompense?", "তুমি কি তাকে দেখেছ যে বিচার দিনকে অস্বীকার করে?"),
                Ayah(2, "فَذَٰلِكَ الَّذِي يَدُعُّ الْيَتِيمَ", "For that is the one who drives away the orphan", "এমন ব্যক্তি তো এতীমকে বিতাড়িত করে,"),
                Ayah(3, "وَلَا يَحُضُّ عَلَىٰ طَعَامِ الْمِسْكِينِ", "And does not encourage the feeding of the poor.", "এবং অভাবীকে অন্ন দানে উৎসাহ দেয় না।"),
                Ayah(4, "فَوَيْلٌ لِلْمُصَلِّينَ", "So woe to those who pray", "অতএব দুর্ভোগ সেই নামাযীদের,"),
                Ayah(5, "الَّذِينَ هُمْ عَنْ صَلَاتِهِمْ سَاهُونَ", "But are heedless of their prayer —", "যারা তাদের নামায সম্পর্কে গাফিল,"),
                Ayah(6, "الَّذِينَ هُمْ يُرَاءُونَ", "Those who make show [of their deeds]", "যারা লোক দেখানোর জন্য আমল করে,"),
                Ayah(7, "وَيَمْنَعُونَ الْمَاعُونَ", "And withhold [simple] assistance.", "এবং সামান্য সাহায্যও দিতে অস্বীকার করে।")
            )
        )
    )

    /** Ayah-of-the-day pool. */
    val ayahOfDayPool: List<Triple<String, String, String>> = listOf(
        Triple("وَمَنْ يَتَّقِ اللَّهَ يَجْعَلْ لَهُ مَخْرَجًا", "And whoever fears Allah — He will make for him a way out. (65:2)", "এবং যে আল্লাহকে ভয় করে, তিনি তার জন্য বের হবার পথ করে দেবেন। (৬৫:২)"),
        Triple("إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Indeed, with hardship [will be] ease. (94:6)", "নিশ্চয় কষ্টের সাথেই রয়েছে স্বস্তি। (৯৪:৬)"),
        Triple("فَاذْكُرُونِي أَذْكُرْكُمْ", "So remember Me; I will remember you. (2:152)", "অতএব তোমরা আমাকে স্মরণ কর, আমিও তোমাদের স্মরণ করব। (২:১৫২)")
    )

    /** Hadith-of-the-day pool. */
    val hadithOfDayPool: List<Pair<String, String>> = listOf(
        Pair("ইসলামের ভিত্তি পাঁচটি: আল্লাহ ছাড়া কোনো উপাস্য নেই সাক্ষ্য দেওয়া, নামায কায়েম করা, যাকাত দেওয়া, হজ্জ করা এবং রমজানের রোজা রাখা।", "— সহীহ বুখারী ৮"),
        Pair("যে ব্যক্তি আমার পথে চলে না, সে আমার দলভুক্ত নয়। যে আল্লাহ ও শেষ দিনে বিশ্বাস করে, সে উত্তম কথা বলুক অথবা চুপ থাকুক।", "— সহীহ বুখারী ৬০১৮"),
        Pair("তোমাদের কেউ ততক্ষণ পর্যন্ত প্রকৃত মুমিন হতে পারে না, যতক্ষণ না সে তার ভাইয়ের জন্য তা ভালোবাসে, যা নিজের জন্য ভালোবাসে।", "— সহীহ বুখারী ১৩")
    )

    data class SurahMeta(
        val number: Int,
        val nameArabic: String,
        val nameEnglish: String,
        val nameBengali: String,
        val englishMeaning: String,
        val revelationType: RevelationType,
        val ayahCount: Int
    )
}

object NamesData {
    val names: List<NameOfAllah> = listOf(
        NameOfAllah(1, "الرَّحْمَنُ", "Ar-Rahman", "The Most Compassionate", "পরম করুণাময়"),
        NameOfAllah(2, "الرَّحِيمُ", "Ar-Raheem", "The Most Merciful", "অসীম দয়ালু"),
        NameOfAllah(3, "الْمَلِكُ", "Al-Malik", "The King and Owner of Dominion", "অধিপতি"),
        NameOfAllah(4, "الْقُدُّوسُ", "Al-Quddus", "The Absolutely Pure", "পবিত্র"),
        NameOfAllah(5, "السَّلَامُ", "As-Salam", "The Source of Peace", "শান্তির উৎস"),
        NameOfAllah(6, "الْمُؤْمِنُ", "Al-Mu'min", "The Guardian of Faith", "নিরাপত্তাদাতা"),
        NameOfAllah(7, "الْمُهَيْمِنُ", "Al-Muhaymin", "The Overseer", "রক্ষক"),
        NameOfAllah(8, "الْعَزِيزُ", "Al-Aziz", "The All-Mighty", "পরাক্রমশালী"),
        NameOfAllah(9, "الْجَبَّارُ", "Al-Jabbar", "The Compeller", "মহাপরাক্রমশালী"),
        NameOfAllah(10, "الْمُتَكَبِّرُ", "Al-Mutakabbir", "The Supreme", "মহান"),
        NameOfAllah(11, "الْخَالِقُ", "Al-Khaliq", "The Creator", "স্রষ্টা"),
        NameOfAllah(12, "الْبَارِئُ", "Al-Bari'", "The Originator", "সৃষ্টিকর্তা"),
        NameOfAllah(13, "الْمُصَوِّرُ", "Al-Musawwir", "The Fashioner", "আকার দাতা"),
        NameOfAllah(14, "الْغَفَّارُ", "Al-Ghaffar", "The Constant Forgiver", "বারবার ক্ষমাকারী"),
        NameOfAllah(15, "الْقَهَّارُ", "Al-Qahhar", "The Subduer", "পরাজিতকারী"),
        NameOfAllah(16, "الْوَهَّابُ", "Al-Wahhab", "The Bestower", "দাতা"),
        NameOfAllah(17, "الرَّزَّاقُ", "Ar-Razzaq", "The Provider", "রিযিকদাতা"),
        NameOfAllah(18, "الْفَتَّاحُ", "Al-Fattah", "The Supreme Opener", "বিজয়দাতা"),
        NameOfAllah(19, "اَلْعَلِيْمُ", "Al-Alim", "The All-Knowing", "সর্বজ্ঞ"),
        NameOfAllah(20, "الْقَابِضُ", "Al-Qabid", "The Withholder", "সংকুচিতকারী"),
        NameOfAllah(21, "الْبَاسِطُ", "Al-Basit", "The Extender", "প্রশস্তকারী"),
        NameOfAllah(22, "الْخَافِضُ", "Al-Khafid", "The Abaser", "অবনমিতকারী"),
        NameOfAllah(23, "الرَّافِعُ", "Ar-Rafi'", "The Exalter", "উন্নীতকারী"),
        NameOfAllah(24, "الْمُعِزُّ", "Al-Mu'izz", "The Bestower of Honor", "সম্মানদাতা"),
        NameOfAllah(25, "الْمُذِلُّ", "Al-Mudhill", "The Humiliator", "অপমানকারী"),
        NameOfAllah(26, "السَّمِيعُ", "As-Sami'", "The All-Hearing", "সর্বশ্রোতা"),
        NameOfAllah(27, "الْبَصِيرُ", "Al-Basir", "The All-Seeing", "সর্বদ্রষ্টা"),
        NameOfAllah(28, "الْحَكَمُ", "Al-Hakam", "The Impartial Judge", "ন্যায়বিচারক"),
        NameOfAllah(29, "الْعَدْلُ", "Al-'Adl", "The Utterly Just", "ন্যায়পরায়ণ"),
        NameOfAllah(30, "اللَّطِيفُ", "Al-Latif", "The Subtle One", "সূক্ষ্মজ্ঞানী"),
        NameOfAllah(31, "الْخَبِيرُ", "Al-Khabir", "The All-Aware", "সবকিছু জানেন"),
        NameOfAllah(32, "الْحَلِيمُ", "Al-Halim", "The Forbearing", "ধৈর্যশীল"),
        NameOfAllah(33, "الْعَظِيمُ", "Al-'Azim", "The Magnificent", "মহান"),
        NameOfAllah(34, "الْغَفُورُ", "Al-Ghafur", "The All-Forgiving", "ক্ষমাকারী"),
        NameOfAllah(35, "الشَّكُورُ", "Ash-Shakur", "The Most Appreciative", "কৃতজ্ঞ"),
        NameOfAllah(36, "الْعَلِيُّ", "Al-'Ali", "The Most High", "সর্বোচ্চ"),
        NameOfAllah(37, "الْكَبِيرُ", "Al-Kabir", "The Greatest", "মহাকায়"),
        NameOfAllah(38, "الْحَفِيظُ", "Al-Hafiz", "The Preserver", "সংরক্ষক"),
        NameOfAllah(39, "المُقِيتُ", "Al-Muqit", "The Sustainer", "জীবনধারক"),
        NameOfAllah(40, "الْحسِيبُ", "Al-Hasib", "The Reckoner", "হিসাব গ্রহণকারী"),
        NameOfAllah(41, "الْجَلِيلُ", "Al-Jalil", "The Majestic", "সম্মানিত"),
        NameOfAllah(42, "الْكَرِيمُ", "Al-Karim", "The Most Generous", "দানশীল"),
        NameOfAllah(43, "الرَّقِيبُ", "Ar-Raqib", "The Watchful", "পর্যবেক্ষক"),
        NameOfAllah(44, "الْمُجِيبُ", "Al-Mujib", "The Responsive", "ডাকে সাড়াদাতা"),
        NameOfAllah(45, "الْوَاسِعُ", "Al-Wasi'", "The All-Encompassing", "প্রশস্ত"),
        NameOfAllah(46, "الْحَكِيمُ", "Al-Hakim", "The All-Wise", "প্রজ্ঞাময়"),
        NameOfAllah(47, "الْوَدُودُ", "Al-Wadud", "The Most Loving", "প্রেমময়"),
        NameOfAllah(48, "الْمَجِيدُ", "Al-Majid", "The Most Glorious", "গৌরবময়"),
        NameOfAllah(49, "الْبَاعِثُ", "Al-Ba'ith", "The Resurrector", "পুনরুত্থানকারী"),
        NameOfAllah(50, "الشَّهِيدُ", "Ash-Shahid", "The Witness", "সাক্ষী"),
        NameOfAllah(51, "الْحَقُّ", "Al-Haqq", "The Absolute Truth", "সত্য"),
        NameOfAllah(52, "الْوَكِيلُ", "Al-Wakil", "The Trustee", "কর্মবিধায়ক"),
        NameOfAllah(53, "الْقَوِيُّ", "Al-Qawi", "The All-Strong", "শক্তিমান"),
        NameOfAllah(54, "الْمَتِينُ", "Al-Matin", "The Firm", "দৃঢ়"),
        NameOfAllah(55, "الْوَلِيُّ", "Al-Wali", "The Protecting Friend", "অভিভাবক"),
        NameOfAllah(56, "الْحَمِيدُ", "Al-Hamid", "The All-Praiseworthy", "প্রশংসিত"),
        NameOfAllah(57, "الْمُحْصِي", "Al-Muhsi", "The All-Enumerating", "গণনাকারী"),
        NameOfAllah(58, "الْمُبْدِئُ", "Al-Mubdi'", "The Originator", "স্রষ্টা"),
        NameOfAllah(59, "الْمُعِيدُ", "Al-Mu'id", "The Restorer", "পুনঃস্রষ্টা"),
        NameOfAllah(60, "الْمُحْيِي", "Al-Muhyi", "The Giver of Life", "জীবনদাতা"),
        NameOfAllah(61, "اَلْمُمِيتُ", "Al-Mumit", "The Bringer of Death", "মৃত্যুদাতা"),
        NameOfAllah(62, "الْحَيُّ", "Al-Hayy", "The Ever-Living", "চিরঞ্জীব"),
        NameOfAllah(63, "الْقَيُّومُ", "Al-Qayyum", "The Sustainer of All", "সর্বধারক"),
        NameOfAllah(64, "الْوَاجِدُ", "Al-Wajid", "The Perceiver", "আবিষ্কারক"),
        NameOfAllah(65, "الْمَاجِدُ", "Al-Majid", "The Illustrious", "মহিমান্বিত"),
        NameOfAllah(66, "الْواحِدُ", "Al-Wahid", "The One", "এক"),
        NameOfAllah(67, "اَلاَحَدُ", "Al-Ahad", "The Unique", "অদ্বিতীয়"),
        NameOfAllah(68, "الصَّمَدُ", "As-Samad", "The Eternal Refuge", "অমুখাপেক্ষী"),
        NameOfAllah(69, "الْقَادِرُ", "Al-Qadir", "The Omnipotent", "সর্বশক্তিমান"),
        NameOfAllah(70, "الْمُقْتَدِرُ", "Al-Muqtadir", "The All-Powerful", "ক্ষমতাবান"),
        NameOfAllah(71, "الْمُقَدِّمُ", "Al-Muqaddim", "The Expediter", "অগ্রসারক"),
        NameOfAllah(72, "الْمُؤَخِّرُ", "Al-Mu'akhkhir", "The Delayer", "পিছিয়ে দেওয়া"),
        NameOfAllah(73, "الأوَّلُ", "Al-Awwal", "The First", "প্রথম"),
        NameOfAllah(74, "الآخِرُ", "Al-Akhir", "The Last", "শেষ"),
        NameOfAllah(75, "الظَّاهِرُ", "Az-Zahir", "The Manifest", "প্রকাশ্য"),
        NameOfAllah(76, "الْبَاطِنُ", "Al-Batin", "The Hidden", "গোপন"),
        NameOfAllah(77, "الْوَالِي", "Al-Wali", "The Governor", "শাসক"),
        NameOfAllah(78, "الْمُتَعَالِي", "Al-Muta'ali", "The Most Exalted", "সর্বোচ্চ"),
        NameOfAllah(79, "الْبَرُّ", "Al-Barr", "The Most Kind", "কল্যাণকারী"),
        NameOfAllah(80, "التَّوَابُ", "At-Tawwab", "The Accepter of Repentance", "তওবা গ্রহণকারী"),
        NameOfAllah(81, "الْمُنْتَقِمُ", "Al-Muntaqim", "The Avenger", "প্রতিশোধকারী"),
        NameOfAllah(82, "العَفُوُّ", "Al-'Afu", "The Pardoner", "ক্ষমাকারী"),
        NameOfAllah(83, "الرَّؤُوفُ", "Ar-Ra'uf", "The Most Kind", "স্নেহশীল"),
        NameOfAllah(84, "مَالِكُ الْمُلْكِ", "Malik-ul-Mulk", "The Master of the Kingdom", "রাজত্বের মালিক"),
        NameOfAllah(85, "ذُو الْجَلَالِ وَالْإِكْرَامِ", "Dhul-Jalali wal-Ikram", "The Lord of Majesty and Bounty", "মহত্ত্ব ও সম্মানের অধিকারী"),
        NameOfAllah(86, "الْمُقْسِطُ", "Al-Muqsit", "The Equitable", "ন্যায়পরায়ণ"),
        NameOfAllah(87, "الْجَامِعُ", "Al-Jami'", "The Gatherer", "একত্রকারী"),
        NameOfAllah(88, "الْغَنِيُّ", "Al-Ghani", "The Self-Sufficient", "অভাবমুক্ত"),
        NameOfAllah(89, "الْمُغْنِي", "Al-Mughni", "The Bestower of Wealth", "ধন-সম্পদ দানকারী"),
        NameOfAllah(90, "اَلْمَانِعُ", "Al-Mani'", "The Preventer", "বাধাদাতা"),
        NameOfAllah(91, "الضَّارُّ", "Ad-Darr", "The Distresser", "ক্ষতিকারক"),
        NameOfAllah(92, "النَّافِعُ", "An-Nafi'", "The Benefactor", "উপকারকারী"),
        NameOfAllah(93, "النُّورُ", "An-Nur", "The Light", "আলো"),
        NameOfAllah(94, "الْهَادِي", "Al-Hadi", "The Guide", "পথপ্রদর্শক"),
        NameOfAllah(95, "الْبَدِيعُ", "Al-Badi'", "The Incomparable", "অনুপম স্রষ্টা"),
        NameOfAllah(96, "اَلْبَاقِي", "Al-Baqi", "The Everlasting", "চিরস্থায়ী"),
        NameOfAllah(97, "الْوَارِثُ", "Al-Warith", "The Inheritor", "উত্তরাধিকারী"),
        NameOfAllah(98, "الرَّشِيدُ", "Ar-Rashid", "The Guide to the Right Path", "সঠিক পথপ্রদর্শক"),
        NameOfAllah(99, "الصَّبُورُ", "As-Sabur", "The Patient One", "ধৈর্যশীল")
    )
}

object DuaData {
    val duas: List<Dua> = listOf(
        Dua(
            "morning",
            "Morning Adhkar",
            "সকালের জিকির",
            "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ",
            "Asbahna wa asbahal-mulku lillah, wal-hamdu lillah",
            "We have reached the morning and at this very time the whole kingdom belongs to Allah. Praise is to Allah.",
            "আমরা সকালে উপনীত হয়েছি এবং এই মুহূর্তে সমস্ত রাজত্ব আল্লাহর। সমস্ত প্রশংসা আল্লাহরই।",
            "মুসলিম ৪/২০৮৮"
        ),
        Dua(
            "evening",
            "Evening Adhkar",
            "সন্ধ্যার জিকির",
            "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ",
            "Amsayna wa amsal-mulku lillah, wal-hamdu lillah",
            "We have reached the evening and at this very time the whole kingdom belongs to Allah. Praise is to Allah.",
            "আমরা সন্ধ্যায় উপনীত হয়েছি এবং এই মুহূর্তে সমস্ত রাজত্ব আল্লাহর। সমস্ত প্রশংসা আল্লাহরই।",
            "মুসলিম ৪/২০৮৮"
        ),
        Dua(
            "before_sleep",
            "Before Sleeping",
            "ঘুমানোর আগে",
            "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            "Bismika Allahumma amutu wa ahya",
            "In Your name O Allah, I die and I live.",
            "আপনার নামে হে আল্লাহ, আমি মৃত্যুবরণ করি এবং জীবিত হই।",
            "বুখারী ৬৩২৪"
        ),
        Dua(
            "after_wudu",
            "After Wudu",
            "অজুর পরে",
            "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            "Ashhadu an la ilaha illallah wahdahu la sharika lah, wa ashhadu anna Muhammadan 'abduhu wa rasuluh",
            "I bear witness that none has the right to be worshipped but Allah alone, with no partner, and that Muhammad is His slave and messenger.",
            "আমি সাক্ষ্য দিচ্ছি যে, আল্লাহ ছাড়া কোনো উপাস্য নেই, তিনি এক, তার কোনো শরীক নেই। আর মুহাম্মদ তার বান্দা ও রাসূল।",
            "মুসলিম ২৩৪"
        ),
        Dua(
            "before_eating",
            "Before Eating",
            "খাওয়ার আগে",
            "بِسْمِ اللَّهِ",
            "Bismillah",
            "In the name of Allah.",
            "আল্লাহর নামে।",
            "আবু দাউদ ৩৭৬৭"
        ),
        Dua(
            "after_eating",
            "After Eating",
            "খাওয়ার পরে",
            "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            "Alhamdu lillahil-ladhi at'amana wa saqana wa ja'alana muslimin",
            "Praise is to Allah Who has given us food and drink and made us Muslims.",
            "প্রশংসা আল্লাহর, যিনি আমাদের অন্ন ও পানীয় দিয়েছেন এবং আমাদেরকে মুসলিম করেছেন।",
            "আবু দাউদ ৩৮৫০"
        ),
        Dua(
            "entering_masjid",
            "Entering the Masjid",
            "মসজিদে প্রবেশ",
            "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            "Allahumma-ftah li abwaba rahmatik",
            "O Allah, open the gates of Your mercy for me.",
            "হে আল্লাহ, আমার জন্য আপনার রহমতের দরজাসমূহ খুলে দিন।",
            "মুসলিম ৭১৩"
        ),
        Dua(
            "leaving_masjid",
            "Leaving the Masjid",
            "মসজিদ ত্যাগ",
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            "Allahumma inni as'aluka min fadlik",
            "O Allah, I ask You from Your bounty.",
            "হে আল্লাহ, আমি আপনার অনুগ্রহ প্রার্থনা করি।",
            "মুসলিম ৭১৩"
        )
    )

    val dhikrOptions: List<DhikrOption> = listOf(
        DhikrOption(
            id = "subhanallah",
            arabic = "سُبْحَانَ اللَّهِ",
            transliteration = "SubhanAllah",
            translation = "Glory be to Allah",
            defaultTarget = 33,
            banglaPronunciation = "সুবহানাল্লাহ",
            banglaTranslation = "আল্লাহ পবিত্র",
            banglaMeaning = "আল্লাহ সমস্ত ত্রুটি ও দোষ থেকে পবিত্র",
            whyRecite = "আল্লাহর পবিত্রতা ঘোষণা করার জন্য এবং তাঁর সকল গুণাবলী থেকে তিনি মুক্ত এটা প্রকাশ করতে। এটি তাসবিহে ফাতিমা এর অংশ যা নবী সা. তাঁর মেয়ে ফাতিমাকে শিখিয়েছিলেন।",
            reference = "সহীহ বুখারী ৩৭০৫, সহীহ মুসলিম ২৭২৭",
            reward = "প্রতি উচ্চারণে ১০টি গুনাহ মাফ ও ১০টি নেকী লেখা হয়"
        ),
        DhikrOption(
            id = "alhamdulillah",
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "All praise is for Allah",
            defaultTarget = 33,
            banglaPronunciation = "আলহামদুলিল্লাহ",
            banglaTranslation = "সমস্ত প্রশংসা আল্লাহর জন্য",
            banglaMeaning = "আল্লাহ তাআলার জন্য সমস্ত ধরনের প্রশংসা ও কৃতজ্ঞতা",
            whyRecite = "আল্লাহর প্রতি কৃতজ্ঞতা প্রকাশ করতে এবং তাঁর অনুগ্রহের জন্য ধন্যবাদ জানাতে। হাদিসে এসেছে, যে ব্যক্তি এটি বলে তার জন্য জান্নাতে একটি গাছ লাগানো হয়।",
            reference = "সুনান তিরমিযী ৩৪৬০",
            reward = "প্রতি উচ্চারণে জান্নাতে একটি গাছ রোপণ"
        ),
        DhikrOption(
            id = "allahu_akbar",
            arabic = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah is the Greatest",
            defaultTarget = 34,
            banglaPronunciation = "আল্লাহু আকবার",
            banglaTranslation = "আল্লাহ সবচেয়ে বড়",
            banglaMeaning = "আল্লাহ সমস্ত কিছুর চেয়ে শ্রেষ্ঠ ও মহান",
            whyRecite = "আল্লাহর শ্রেষ্ঠত্ব ঘোষণা করতে এবং পৃথিবীর সব কিছু তাঁর সামনে তুচ্ছ এটা স্মরণ করতে। তাসবিহে ফাতিমার তৃতীয় অংশ।",
            reference = "সহীহ মুসলিম ২৭২৭",
            reward = "আল্লাহর ভয়ে অন্য সব ভয় দূর হয়"
        ),
        DhikrOption(
            id = "la_ilaha",
            arabic = "لَا إِلَهَ إِلَّا اللَّهُ",
            transliteration = "La ilaha illallah",
            translation = "There is no god but Allah",
            defaultTarget = 100,
            banglaPronunciation = "লা ইলাহা ইল্লাল্লাহ",
            banglaTranslation = "আল্লাহ ছাড়া কোনো উপাস্য নেই",
            banglaMeaning = "তাওহিদের ঘোষণা — আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই",
            whyRecite = "এটি ইসলামের মূল স্তম্ভ এবং জান্নাতের চাবি। নবী সা. বলেছেন, যে ব্যক্তি শেষ নিঃশ্বাসে এটি বলে তার জন্য জান্নাত ওয়াজিব হয়ে যায়।",
            reference = "সুনান আবু দাউদ ৩১১৬",
            reward = "১০০টি দাসমুক্তির সমান নেকী"
        ),
        DhikrOption(
            id = "astaghfirullah",
            arabic = "أَسْتَغْفِرُ اللَّهَ",
            transliteration = "Astaghfirullah",
            translation = "I seek forgiveness from Allah",
            defaultTarget = 100,
            banglaPronunciation = "আস্তাগফিরুল্লাহ",
            banglaTranslation = "আমি আল্লাহর কাছে ক্ষমা চাই",
            banglaMeaning = "আল্লাহর কাছে ক্ষমা প্রার্থনা ও তওবা",
            whyRecite = "গুনাহ থেকে তওবা করতে এবং আল্লাহর রহমত কামনা করতে। নবী সা. দিনে ৭০-১০০ বার ইস্তিগফার পড়তেন। এটি জীবিকা বৃদ্ধি ও দুঃখ দূর করার উপায়।",
            reference = "সহীহ বুখারী ৬৩০৭",
            reward = "প্রতিটি গুনাহর জন্য ক্ষমা ও জীবিকা বৃদ্ধি"
        )
    )
}
