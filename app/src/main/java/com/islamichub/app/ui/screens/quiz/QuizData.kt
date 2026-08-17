package com.islamichub.app.ui.screens.quiz

/**
 * Islamic Quiz data — 6 categories with 5+ questions each.
 * All questions are in Bengali, suitable for general Muslim audience.
 */

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null
)

data class QuizCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,       // asset image name
    val color: Long,       // accent color
    val questions: List<QuizQuestion>
)

object QuizData {

    val categories: List<QuizCategory> = listOf(
        QuizCategory(
            id = "quran_basics",
            title = "কুরআন বেসিক",
            subtitle = "মূল ধারণা",
            icon = "quran-premium-bg.webp",
            color = 0xFF6D45C7,
            questions = listOf(
                QuizQuestion(
                    question = "পবিত্র কুরআনে মোট কয়টি সূরা আছে?",
                    options = listOf("১১০", "১১৪", "১২০", "১০৪"),
                    correctIndex = 1,
                    explanation = "পবিত্র কুরআনে মোট ১১৪টি সূরা রয়েছে।"
                ),
                QuizQuestion(
                    question = "সবচেয়ে ছোট সূরা কোনটি?",
                    options = listOf("সূরা আল-আসর", "সূরা আল-কাওসার", "সূরা আল-ইখলাস", "সূরা আন-নাস"),
                    correctIndex = 1,
                    explanation = "সূরা আল-কাওসার (১০৮) — মাত্র ৩ আয়াত, সবচেয়ে ছোট।"
                ),
                QuizQuestion(
                    question = "সবচেয়ে বড় সূরা কোনটি?",
                    options = listOf("সূরা আল-বাকারা", "সূরা আলে ইমরান", "সূরা আন-নিসা", "সূরা আল-মায়েদা"),
                    correctIndex = 0,
                    explanation = "সূরা আল-বাকারা — ২৮৬ আয়াত, সবচেয়ে বড়।"
                ),
                QuizQuestion(
                    question = "প্রথম নাজিলকৃত সূরা কোনটি?",
                    options = listOf("সূরা আল-ফাতিহা", "সূরা আল-আলাক", "সূরা আল-মুদ্দাসসির", "সূরা আল-ক্বামার"),
                    correctIndex = 1,
                    explanation = "সূরা আল-আলাক (প্রথম ৫ আয়াত) — গারে হেরায় নাজিল।"
                ),
                QuizQuestion(
                    question = "কুরআনে কতটি সিজদার আয়াত আছে?",
                    options = listOf("১০", "১২", "১৪", "১৫"),
                    correctIndex = 2,
                    explanation = "মতভেদ আছে তবে প্রসিদ্ধ মতে ১৪টি সিজদার আয়াত।"
                ),
                QuizQuestion(
                    question = "বিসমিল্লাহ দিয়ে শুরু নয় এমন সূরা কোনটি?",
                    options = listOf("সূরা আত-তাওবা", "সূরা আল-ফাতিহা", "সূরা আল-বাকারা", "সূরা ইয়াসিন"),
                    correctIndex = 0,
                    explanation = "সূরা আত-তাওবা (৯) — বিসমিল্লাহ দিয়ে শুরু হয় না।"
                )
            )
        ),
        QuizCategory(
            id = "prophets",
            title = "নবী-রাসূল",
            subtitle = "জীবনী",
            icon = "stories-premium-bg.webp",
            color = 0xFF8D6E63,
            questions = listOf(
                QuizQuestion(
                    question = "কুরআনে কতজন নবী-রাসূলের নাম উল্লেখ আছে?",
                    options = listOf("২৫ জন", "২৮ জন", "১২৪ জন", "১ লাখ ২৪ হাজার"),
                    correctIndex = 0,
                    explanation = "কুরআনে ২৫ জন নবী-রাসূলের নাম স্পষ্টভাবে এসেছে।"
                ),
                QuizQuestion(
                    question = "সর্বপ্রথম মানুষ ও নবী কে?",
                    options = listOf("নূহ আ.", "ইব্রাহিম আ.", "আদম আ.", "ঈসা আ."),
                    correctIndex = 2,
                    explanation = "আদম (আ.) — প্রথম মানব এবং প্রথম নবী।"
                ),
                QuizQuestion(
                    question = "'আবুল আম্বিয়া' (নবীদের পিতা) উপাধি কার?",
                    options = listOf("আদম আ.", "ইব্রাহিম আ.", "নূহ আ.", "মুহাম্মদ সা."),
                    correctIndex = 1,
                    explanation = "ইব্রাহিম (আ.) — আবুল আম্বিয়া উপাধি পেয়েছেন।"
                ),
                QuizQuestion(
                    question = "তৌরাত কোন নবীর উপর নাজিল হয়েছিল?",
                    options = listOf("দাউদ আ.", "মুসা আ.", "ঈসা আ.", "ইউনুস আ."),
                    correctIndex = 1,
                    explanation = "তৌরাত — মুসা (আ.) এর উপর নাজিল হয়েছিল।"
                ),
                QuizQuestion(
                    question = "যাবূর (পসম) কাকে দেওয়া হয়েছিল?",
                    options = listOf("সুলাইমান আ.", "দাউদ আ.", "ইয়াহইয়া আ.", "জাকারিয়া আ."),
                    correctIndex = 1,
                    explanation = "যাবূর — দাউদ (আ.) কে দেওয়া হয়েছিল।"
                ),
                QuizQuestion(
                    question = "ইনজিল কোন নবীর কাছে এসেছিল?",
                    options = listOf("মুসা আ.", "ইব্রাহিম আ.", "ঈসা আ.", "মুহাম্মদ সা."),
                    correctIndex = 2,
                    explanation = "ইনজিল — ঈসা (আ.) এর কাছে এসেছিল।"
                )
            )
        ),
        QuizCategory(
            id = "namaz",
            title = "নামাজ",
            subtitle = "শিক্ষা ও বিধান",
            icon = "namaz-premium-bg.webp",
            color = 0xFFC9A34E,
            questions = listOf(
                QuizQuestion(
                    question = "দিনে কত ওয়াক্ত নামাজ ফরজ?",
                    options = listOf("৩", "৫", "৭", "৯"),
                    correctIndex = 1,
                    explanation = "৫ ওয়াক্ত নামাজ ফরজ: ফজর, যোহর, আসর, মাগরিব, এশা।"
                ),
                QuizQuestion(
                    question = "ফজরের নামাজে কয় রাকাত সুন্নত?",
                    options = listOf("২", "৪", "৬", "০"),
                    correctIndex = 0,
                    explanation = "ফজরের আগে ২ রাকাত সুন্নত মুয়াক্কাদা।"
                ),
                QuizQuestion(
                    question = "জুমআর নামাজ কত রাকাত?",
                    options = listOf("২", "৪", "৬", "১০"),
                    correctIndex = 0,
                    explanation = "জুমআর ফরজ ২ রাকাত (ইমামের পেছনে জামাতে)।"
                ),
                QuizQuestion(
                    question = "নামাজে সিজদা করতে হয় কয়বার প্রতি রাকাতে?",
                    options = listOf("১ বার", "২ বার", "৩ বার", "৪ বার"),
                    correctIndex = 1,
                    explanation = "প্রতি রাকাতে ২ বার সিজদা।"
                ),
                QuizQuestion(
                    question = "নামাজে কিবলা কোন দিকে?",
                    options = listOf("পূর্ব", "পশ্চিম", "মক্কা", "মদিনা"),
                    correctIndex = 2,
                    explanation = "নামাজের দিক হলো কাবা ঘর — মক্কা।"
                ),
                QuizQuestion(
                    question = "ভ্রমণ অবস্থায় (কসর) কত রাকাত পড়তে হয়?",
                    options = listOf("পূর্ণ ৪", "অর্ধেক ২", "৩", "৬"),
                    correctIndex = 1,
                    explanation = "ভ্রমণে ৪ রাকাতের জায়গায় ২ রাকাত কসর পড়তে হয়।"
                )
            )
        ),
        QuizCategory(
            id = "ramadan",
            title = "রমজান ও রোজা",
            subtitle = "বিধান ও ফজিলত",
            icon = "salah-premium-bg.webp",
            color = 0xFFD84315,
            questions = listOf(
                QuizQuestion(
                    question = "রমজান মাস কোন মাস?",
                    options = listOf("মুহররম", "রজব", "রমজান (৯ম)", "শাওয়াল"),
                    correctIndex = 2,
                    explanation = "রমজান — হিজরি সনের ৯ম মাস।"
                ),
                QuizQuestion(
                    question = "সেহরি খাওয়ার শেষ সময় কখন?",
                    options = listOf("সূর্যাস্ত", "সুবহে সাদিক", "ফজরের পর", "যোহরের আগে"),
                    correctIndex = 1,
                    explanation = "সুবহে সাদিক (ফজরের আজান) শুরু হলেই সেহরি শেষ।"
                ),
                QuizQuestion(
                    question = "ইফতার কী দিয়ে শুরু করা সুন্নত?",
                    options = listOf("পানি", "খেজুর", "দুধ", "ফল"),
                    correctIndex = 1,
                    explanation = "খেজুর দিয়ে ইফতার করা সুন্নত। পানি দিয়েও করা যায়।"
                ),
                QuizQuestion(
                    question = "তারাবিহ নামাজ কোন মাসে?",
                    options = listOf("শাওয়াল", "রমজান", "রজব", "মুহররম"),
                    correctIndex = 1,
                    explanation = "তারাবিহ — রমজান মাসে এশার পর সুন্নত।"
                ),
                QuizQuestion(
                    question = "লাইলাতুল কদর কোন রাতে হয়ে থাকে?",
                    options = listOf("রমজানের প্রথম রাত", "শেষ ১০ রাতের বিজোড় রাত", "মধ্যবর্তী রাত", "যেকোনো রাত"),
                    correctIndex = 1,
                    explanation = "শেষ ১০ রাতের বিজোড় রাতে (২১, ২৩, ২৫, ২৭, ২৯) লাইলাতুল কদর অনুসন্ধান করতে হয়।"
                ),
                QuizQuestion(
                    question = "রোজা ভঙ্গ করে না কিন্তু মাকরুহ হয় কোনটি?",
                    options = listOf("পানি পান", "দাঁত মাজা", "গোসল", "ঝিমুনো"),
                    correctIndex = 1,
                    explanation = "দাঁত মাজলে রোজা ভাঙে না তবে মাকরুহ। তবে মিসওয়াক করা যায়।"
                )
            )
        ),
        QuizCategory(
            id = "history",
            title = "ইসলামি ইতিহাস",
            subtitle = "গুরুত্বপূর্ণ ঘটনা",
            icon = "topics-premium-bg.webp",
            color = 0xFF00ACC1,
            questions = listOf(
                QuizQuestion(
                    question = "হিজরি সনের সূচনা কোন ঘটনা থেকে?",
                    options = listOf("জন্ম", "হিজরত", "মৃত্যু", "ফাতহে মক্কা"),
                    correctIndex = 1,
                    explanation = "মক্কা থেকে মদিনায় হিজরত (৬২২ খ্রিস্টাব্দ) থেকে হিজরি সন গণনা শুরু।"
                ),
                QuizQuestion(
                    question = "বদর যুদ্ধ কত সনে হয়?",
                    options = listOf("১ম হিজরি", "২য় হিজরি", "৩য় হিজরি", "৫ম হিজরি"),
                    correctIndex = 1,
                    explanation = "বদর যুদ্দ — ২য় হিজরির ১৭ রমজান।"
                ),
                QuizQuestion(
                    question = "ফাতহে মক্কা কোন সনে হয়?",
                    options = listOf("৬ষ্ঠ হিজরি", "৭ম হিজরি", "৮ম হিজরি", "৯ম হিজরি"),
                    correctIndex = 2,
                    explanation = "ফাতহে মক্কা — ৮ম হিজরির রমজান।"
                ),
                QuizQuestion(
                    question = "হুদাইবিয়ার সন্ধি কত বছরের জন্য হয়েছিল?",
                    options = listOf("৫ বছর", "১০ বছর", "১৫ বছর", "২০ বছর"),
                    correctIndex = 1,
                    explanation = "হুদাইবিয়ার সন্ধি — ১০ বছরের জন্য হয়েছিল (তবে ২ বছরেই ভঙ্গ হয়)।"
                ),
                QuizQuestion(
                    question = "খলিফাদের মধ্যে সবচেয়ে দীর্ঘ শাসন কার?",
                    options = listOf("আবু বকর রা.", "ওমর রা.", "ওসমান রা.", "আলি রা."),
                    correctIndex = 2,
                    explanation = "ওসমান (রা.) — ১২ বছর খিলাফত করেছেন (সবচেয়ে দীর্ঘ)।"
                ),
                QuizQuestion(
                    question = "তারিখ বদ্ধ কুরআন কার আমলে সম্পন্ন হয়?",
                    options = listOf("আবু বকর রা.", "ওমর রা.", "ওসমান রা.", "আলি রা."),
                    correctIndex = 2,
                    explanation = "ওসমান (রা.) এর আমলে কুরআন এক মুসহাফে তারিখ বদ্ধ হয়।"
                )
            )
        ),
        QuizCategory(
            id = "fiqh",
            title = "ফিকহ ও আকিদা",
            subtitle = "বিধান ও বিশ্বাস",
            icon = "hadith-premium-bg.webp",
            color = 0xFF1B5E20,
            questions = listOf(
                QuizQuestion(
                    question = "ইসলামের ৫টি স্তম্ভের প্রথমটি কোনটি?",
                    options = listOf("নামাজ", "রোজা", "কালিমা", "হজ"),
                    correctIndex = 2,
                    explanation = "কালিমা — ইসলামের প্রথম স্তম্ভ।"
                ),
                QuizQuestion(
                    question = "ঈমানের ৬টি অঙ্গের প্রথমটি কোনটি?",
                    options = listOf("আল্লাহর উপর ঈমান", "ফেরেশতা", "কিতাব", "নবী"),
                    correctIndex = 0,
                    explanation = "আল্লাহ তাআলার উপর ঈমান — প্রথম।"
                ),
                QuizQuestion(
                    question = "ওযু ছাড়া কোনটি করা যায় না?",
                    options = listOf("কুরআন স্পর্শ", "তসবিহ", "সালাম", "মোবাইল ব্যবহার"),
                    correctIndex = 0,
                    explanation = "অযু ব্যতীত কুরআন স্পর্শ করা যায় না (জুমহুর মতে)।"
                ),
                QuizQuestion(
                    question = "গিয়ারউইন (এতেমাদ) — কোন মাজহাবের অনুসরণ?",
                    options = listOf("হানাফি", "শাফেয়ি", "মালেকি", "হাম্বলি"),
                    correctIndex = 0,
                    explanation = "প্রসিদ্ধ ৪ মাজহাব: হানাফি, শাফেয়ি, মালেকি, হাম্বলি — সবই সঠিক।"
                ),
                QuizQuestion(
                    question = "যাকাত ফরজ হওয়ার শর্ত কোনটি?",
                    options = listOf("প্রতি বছর হিসাব", "নিসাব পরিমাণ সম্পদ", "হাজত হওয়া", "সবগুলো"),
                    correctIndex = 3,
                    explanation = "নিসাব থাকা + এক বছর অতিবাহিত + আয় খরচ উদ্ধার — সব শর্ত পূরণ প্রয়োজন।"
                ),
                QuizQuestion(
                    question = "কোন ধর্মের নাম আল্লাহ দিয়েছেন?",
                    options = listOf("ইসলাম", "খ্রিষ্টধর্ম", "ইহুদি", "হিন্দু"),
                    correctIndex = 0,
                    explanation = "ইসলাম — একমাত্র ধর্ম যার নাম আল্লাহ নিজে দিয়েছেন (সূরা মায়েদা ৩)।"
                )
            )
        )
    )

    fun totalQuestions(): Int = categories.sumOf { it.questions.size }
}
