package com.islamichub.app.ui.screens.namaz

/**
 * Complete Namaz step-by-step data for all 5 daily prayers.
 * Each prayer has a list of steps with Arabic, transliteration, Bangla, and audio file.
 */
object NamazStepsData {

    data class NamazStep(
        val title: String,
        val titleEn: String,
        val arabic: String = "",
        val transliteration: String = "",
        val bangla: String = "",
        val description: String = "",
        val audioFile: String = ""
    )

    data class NamazInfo(
        val id: String,
        val nameBn: String,
        val nameEn: String,
        val rakat: Int,
        val fardRakat: Int,
        val sunnahBefore: Int,
        val sunnahAfter: Int,
        val time: String,
        val steps: List<NamazStep>
    )

    val prayers: Map<String, NamazInfo> = mapOf(
        "fajr" to NamazInfo(
            id = "fajr",
            nameBn = "ফজর",
            nameEn = "Fajr",
            rakat = 4,
            fardRakat = 2,
            sunnahBefore = 2,
            sunnahAfter = 0,
            time = "ভোর থেকে সূর্যোদয় পর্যন্ত",
            steps = buildList {
                addAll(commonSteps())
                add(NamazStep(
                    title = "নিয়ত (ফজরের সুন্নত)",
                    titleEn = "Niyyah (Fajr Sunnah)",
                    description = "২ রাকাআত সুন্নত নামাজের নিয়ত করুন। হাত কান পর্যন্ত তুলে বলুন:"
                ))
                add(NamazStep(
                    title = "তাকবীর তাহরিমা",
                    titleEn = "Takbir Tahrimah",
                    arabic = "اللَّهُ أَكْبَرُ",
                    transliteration = "Allahu Akbar",
                    bangla = "আল্লাহু আকবার",
                    description = "হাত কান পর্যন্ত তুলে বলুন, তারপর হাত বাঁধুন।",
                    audioFile = "takbir-tahrimah.mp3"
                ))
                add(NamazStep(
                    title = "ছানা (সানা)",
                    titleEn = "Thana",
                    arabic = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَى جَدُّكَ وَلَا إِلَهَ غَيْرُكَ",
                    transliteration = "Subhanaka Allahumma wa bihamdika, wa tabaraka asmuka, wa ta'ala jadduka, wa la ilaha ghayruk",
                    bangla = "হে আল্লাহ! আপনি পবিত্র এবং আপনার জন্য সমস্ত প্রশংসা। আপনার নাম বরকতময় এবং আপনার মর্যাদা সর্বোচ্চ। আপনি ছাড়া অন্য কোনো উপাস্য নেই।",
                    audioFile = "tasmiah.mp3"
                ))
                add(NamazStep(
                    title = "তাআওয়ুয",
                    titleEn = "Ta'awwuz",
                    arabic = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
                    transliteration = "A'oodhu billahi minash-shaytanir-rajeem",
                    bangla = "আমি বিতাড়িত শয়তান থেকে আল্লাহর আশ্রয় প্রার্থনা করছি।",
                    audioFile = "taawwuz.mp3"
                ))
                add(NamazStep(
                    title = "তাসমিয়া",
                    titleEn = "Tasmiyah",
                    arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    transliteration = "Bismillahir-Rahmanir-Raheem",
                    bangla = "পরম করুণাময় অসীম দয়ালু আল্লাহর নামে।",
                    audioFile = "tasmiah.mp3"
                ))
                add(NamazStep(
                    title = "সূরা আল-ফাতিহা",
                    titleEn = "Surah Al-Fatihah",
                    arabic = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمَٰنِ الرَّحِيمِ ۝ مَالِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                    transliteration = "Al-hamdu lillahi Rabbil-'alameen. Ar-Rahmanir-Raheem. Maliki yawmid-deen. Iyyaka na'budu wa iyyaka nasta'een. Ihdinas-siratal-mustaqeem. Siratallazeena an'amta alaihim, ghayril-maghdubi alaihim wa lad-dalleen.",
                    bangla = "যাবতীয় প্রশংসা আল্লাহরই, যিনি সকল সৃষ্টিজগতের পালনকর্তা। নিগূঢ় করুণাময়, অসীম দয়ালু। বিচার দিনের মালিক। আমরা কেবল তোমারই ইবাদত করি এবং কেবল তোমারই সাহায্য প্রার্থনা করি। আমাদেকে সরল পথ প্রদর্শন কর। তাদের পথ, যাদেরকে তুমি অনুগ্রহ করেছ; যাদের উপর নয় (তোমার) ক্রোধ, আর যারা পথভ্রষ্টও নয়।",
                    audioFile = "fatiha.mp3"
                ))
                add(NamazStep(
                    title = "অন্য সূরা",
                    titleEn = "Another Surah",
                    description = "আল-ফাতিহার পর কুরআনের যেকোনো একটি সূরা পড়ুন (যেমন সূরা ইখলাস)।",
                    arabic = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                    transliteration = "Qul huwa Allahu ahad. Allahu as-Samad. Lam yalid wa lam yoolad. Wa lam yakun lahu kufuwan ahad.",
                    bangla = "বলুন, তিনি আল্লাহ, এক। আল্লাহ সর্বশ্রেষ্ঠ, অমুখাপেক্ষী। তিনি কাউকে জন্ম দেননি এবং তাকেও জন্ম দেওয়া হয়নি। এবং তার সমতুল্য কেউ নয়।",
                    audioFile = "ikhlas.mp3"
                ))
                add(NamazStep(
                    title = "রুকু",
                    titleEn = "Ruku",
                    description = "তাকবীর বলে রুকু করুন। পিঠ সমান রাখুন।",
                    arabic = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                    transliteration = "Subhana Rabbiyal-Azeem",
                    bangla = "আমার মহান রব পবিত্র। (৩ বার)",
                    audioFile = "ruku.mp3"
                ))
                add(NamazStep(
                    title = "রুকু থেকে উঠা",
                    titleEn = "Stand from Ruku",
                    arabic = "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ",
                    transliteration = "Sami'Allahu liman hamidah",
                    bangla = "আল্লাহ তার জন্য শুনেছেন যে তার প্রশংসা করেছে।",
                    audioFile = "qawamah.mp3"
                ))
                add(NamazStep(
                    title = "সিজদা",
                    titleEn = "Sajdah",
                    description = "তাকবীর বলে সিজদায় যান। কপাল, নাক, দুই হাত, দুই হাঁটু, দুই পা মাটিতে স্পর্শ রাখুন।",
                    arabic = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                    transliteration = "Subhana Rabbiyal-A'la",
                    bangla = "আমার সর্বোচ্চ রব পবিত্র। (৩ বার)",
                    audioFile = "sajdah.mp3"
                ))
                add(NamazStep(
                    title = "দুই সিজদার মাঝে বসা",
                    titleEn = "Sitting between two Sajdahs",
                    arabic = "رَبِّ اغْفِرْ لِي",
                    transliteration = "Rabbighfir li",
                    bangla = "হে আমার রব! আমাকে ক্ষমা করুন।",
                    audioFile = "jalsah.mp3"
                ))
                add(NamazStep(
                    title = "দ্বিতীয় রাকাআত",
                    titleEn = "Second Rakah",
                    description = "প্রথম রাকাআত শেষে দাঁড়িয়ে দ্বিতীয় রাকাআত শুরু করুন। বিসমিল্লাহ পড়ে আল-ফাতিহা ও অন্য সূরা পড়ুন, তারপর রুকু-সিজদা করুন।"
                ))
                add(NamazStep(
                    title = "কায়দায়ে আখিরা (শেষ বৈঠক)",
                    titleEn = "Final Sitting (Qa'dah Akhirah)",
                    description = "দ্বিতীয় রাকাআতের পরে বসুন। তাশাহুদ পড়ুন।",
                    arabic = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ ۝ السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ ۝ السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ ۝ أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                    transliteration = "At-tahiyyatu lillahi was-salawatu wat-tayyibat. As-salamu alayka ayyuhan-Nabiyyu wa rahmatullahi wa barakatuh. As-salamu alayna wa ala ibadillahis-saliheen. Ashhadu an la ilaha illallah wa ashhadu anna Muhammadan abduhu wa rasuluh.",
                    bangla = "সমস্ত সম্মান, প্রার্থনা এবং পবিত্র কাজ আল্লাহর জন্য। হে নবী! আপনার প্রতি শান্তি বর্ষিত হোক, আল্লাহর রহমত ও বরকত। আমাদের এবং আল্লাহর সৎ বান্দাদের প্রতিও শান্তি বর্ষিত হোক। আমি সাক্ষ্য দিচ্ছি যে, আল্লাহ ছাড়া কোনো উপাস্য নেই এবং মুহাম্মদ তার বান্দা ও রাসূল।",
                    audioFile = "tashahud.mp3"
                ))
                add(NamazStep(
                    title = "দরূদ শরীফ",
                    titleEn = "Salat alan-Nabi (Durood)",
                    arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
                    transliteration = "Allahumma salli ala Muhammadin wa ala aali Muhammad, kama sallayta ala Ibraheem wa ala aali Ibraheem, innaka hameedun majeed.",
                    bangla = "হে আল্লাহ! মুহাম্মদ ওর বংশের প্রতি রহমত বর্ষণ করুন, যেমন ইব্রাহিম ও তার বংশের প্রতি রহমত বর্ষণ করেছেন। নিশ্চয় আপনি প্রশংসনীয় ও মর্যাদাবান।",
                    audioFile = "salat-alan-nabi-darud.mp3"
                ))
                add(NamazStep(
                    title = "সালাম",
                    titleEn = "Salam",
                    description = "ডানে ও বামে মুখ ঘুরিয়ে সালাম দিন।",
                    arabic = "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                    transliteration = "As-salamu alaykum wa rahmatullah",
                    bangla = "আপনাদের প্রতি শান্তি এবং আল্লাহর রহমত বর্ষিত হোক।",
                    audioFile = "salam.mp3"
                ))
            }
        ),
        "dhuhr" to NamazInfo(
            id = "dhuhr",
            nameBn = "যোহর",
            nameEn = "Dhuhr",
            rakat = 12,
            fardRakat = 4,
            sunnahBefore = 4,
            sunnahAfter = 2,
            time = "সূর্য ঢলে পড়ার পর থেকে আসর পর্যন্ত",
            steps = buildList {
                addAll(commonSteps())
                add(NamazStep(
                    title = "নিয়ত (যোহরের ফরয)",
                    titleEn = "Niyyah (Dhuhr Fard)",
                    description = "৪ রাকাআত ফরয নামাজের নিয়ত করুন। ইমামের পিছনে দাঁড়িয়ে নিয়ত করুন।"
                ))
                add(NamazStep(
                    title = "প্রথম রাকাআত",
                    titleEn = "First Rakah",
                    description = "তাকবীর তাহরিমা → ছানা → তাআওয়ুয → তাসমিয়া → সূরা আল-ফাতিহা → অন্য সূরা → রুকু → দুই সিজদা"
                ))
                add(NamazStep(
                    title = "দ্বিতীয় রাকাআত",
                    titleEn = "Second Rakah",
                    description = "বিসমিল্লাহ → আল-ফাতিহা → অন্য সূরা → রুকু → দুই সিজদা → প্রথম বৈঠক (তাশাহুদ)"
                ))
                add(NamazStep(
                    title = "তৃতীয় রাকাআত",
                    titleEn = "Third Rakah",
                    description = "দাঁড়িয়ে বিসমিল্লাহ → শুধু আল-ফাতিহা → রুকু → দুই সিজদা"
                ))
                add(NamazStep(
                    title = "চতুর্থ রাকাআত",
                    titleEn = "Fourth Rakah",
                    description = "বিসমিল্লাহ → আল-ফাতিহা → রুকু → দুই সিজদা → শেষ বৈঠক"
                ))
                add(NamazStep(
                    title = "শেষ বৈঠক",
                    titleEn = "Final Sitting",
                    description = "তাশাহুদ → দরূদ শরীফ → দোয়া → সালাম"
                ))
            }
        ),
        "asr" to NamazInfo(
            id = "asr",
            nameBn = "আসর",
            nameEn = "Asr",
            rakat = 8,
            fardRakat = 4,
            sunnahBefore = 4,
            sunnahAfter = 0,
            time = "ছায়া বস্তুর দ্বিগুণ হলে থেকে সূর্যাস্ত পর্যন্ত",
            steps = buildList {
                addAll(commonSteps())
                add(NamazStep(
                    title = "নিয়ত (আসরের ফরয)",
                    titleEn = "Niyyah (Asr Fard)",
                    description = "৪ রাকাআত ফরয নামাজের নিয়ত করুন।"
                ))
                add(NamazStep(
                    title = "৪ রাকাআত ফরয",
                    titleEn = "4 Fard Rakahs",
                    description = "যোহরের মতো ৪ রাকাআত ফরয আদায় করুন: তাকবীর → ছানা → আল-ফাতিহা → অন্য সূরা → রুকু-সিজদা → প্রথম বৈঠক → ৩য় ও ৪র্থ রাকাআতে শুধু আল-ফাতিহা → শেষ বৈঠক → সালাম"
                ))
            }
        ),
        "maghrib" to NamazInfo(
            id = "maghrib",
            nameBn = "মাগরিব",
            nameEn = "Maghrib",
            rakat = 7,
            fardRakat = 3,
            sunnahBefore = 0,
            sunnahAfter = 2,
            time = "সূর্যাস্তের পর থেকে সন্ধ্যা পর্যন্ত",
            steps = buildList {
                addAll(commonSteps())
                add(NamazStep(
                    title = "নিয়ত (মাগরিবের ফরয)",
                    titleEn = "Niyyah (Maghrib Fard)",
                    description = "৩ রাকাআত ফরয নামাজের নিয়ত করুন।"
                ))
                add(NamazStep(
                    title = "প্রথম ও দ্বিতীয় রাকাআত",
                    titleEn = "First and Second Rakah",
                    description = "ফজরের মতো ২ রাকাআত আদায় করুন: আল-ফাতিহা + অন্য সূরা → রুকু-সিজদা → প্রথম বৈঠক"
                ))
                add(NamazStep(
                    title = "তৃতীয় রাকাআত",
                    titleEn = "Third Rakah",
                    description = "দাঁড়িয়ে শুধু আল-ফাতিহা পড়ুন → রুকু → দুই সিজদা → শেষ বৈঠক → সালাম"
                ))
            }
        ),
        "isha" to NamazInfo(
            id = "isha",
            nameBn = "এশা",
            nameEn = "Isha",
            rakat = 17,
            fardRakat = 4,
            sunnahBefore = 4,
            sunnahAfter = 2,
            time = "সন্ধ্যা থেকে ভোর পর্যন্ত",
            steps = buildList {
                addAll(commonSteps())
                add(NamazStep(
                    title = "নিয়ত (এশার ফরয)",
                    titleEn = "Niyyah (Isha Fard)",
                    description = "৪ রাকাআত ফরয নামাজের নিয়ত করুন।"
                ))
                add(NamazStep(
                    title = "৪ রাকাআত ফরয",
                    titleEn = "4 Fard Rakahs",
                    description = "যোহরের মতো ৪ রাকাআত ফরয আদায় করুন।"
                ))
                add(NamazStep(
                    title = "বিতির (১ রাকাআত ওয়াজিব)",
                    titleEn = "Witr (1 Wajib Rakah)",
                    description = "এশার পরে ১ রাকাআত বিতির ওয়াজিব আদায় করুন। রুকুর আগে কুনুত দোয়া পড়ুন।",
                    arabic = "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنُؤْمِنُ بِكَ",
                    transliteration = "Allahumma inna nasta'eenuka wa nastaghfiruka wa nu'minu bika",
                    bangla = "হে আল্লাহ! আমরা তোমার সাহায্য চাই, তোমার কাছে ক্ষমা প্রার্থনা করি এবং তোমার প্রতি ঈমান আনি।",
                    audioFile = "qunut.mp3"
                ))
            }
        )
    )

    private fun commonSteps(): List<NamazStep> = listOf(
        NamazStep(
            title = "অজু ও পরিচ্ছন্নতা",
            titleEn = "Wudu and Cleanliness",
            description = "নামাজের আগে অজু করুন। পরিচ্ছন্ন পোশাক পরুন। কিবলামুখী হয়ে দাঁড়ান।"
        ),
        NamazStep(
            title = "নিয়ত",
            titleEn = "Niyyah (Intention)",
            description = "অন্তরে নামাজের নিয়ত করুন। কোন নামাজ, কত রাকাআত — তা মনে মনে স্থির করুন।"
        )
    )

    /**
     * Common mistakes during namaz and how to fix them.
     */
    val commonMistakes = listOf(
        "তাড়াহুড়ো করে নামাজ পড়া — ধীরে-সুস্থে প্রতিটি অঙ্গ স্থির করে নামাজ পড়ুন।",
        "রুকু-সিজদায় পিঠ সমান না রাখা — পিঠ সমান রাখুন।",
        "সিজদায় নাক মাটিতে না রাখা — কপাল ও নাক উভয় মাটিতে স্পর্শ রাখুন।",
        "আল-ফাতিহা না পড়া — প্রতিটি রাকাআতে আল-ফাতিহা পড়া ফরয।",
        "সালাম ফেরানোর সময় মুখ পুরোপুরি না ঘোরানো — ডানে ও বামে পুরোপুরি মুখ ঘোরান।",
        "খেয়াল না রাখা যে কোন রাকাআতে আছেন — মনোযোগ রাখুন।",
        "ফজরের সুন্নত ছেড়ে দেওয়া — ফজরের সুন্নত অত্যন্ত গুরুত্বপূর্ণ।",
        "এশার পরে বিতির না পড়া — বিতির ওয়াজিব, ছাড়া উচিত নয়।"
    )
}
