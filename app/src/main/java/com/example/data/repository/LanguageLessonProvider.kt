package com.example.data.repository

import com.example.data.model.CrosswordClue
import com.example.data.model.CrosswordPuzzle
import com.example.data.model.DictionaryWord

data class LanguageLesson(
    val id: String,
    val language: String,
    val title: String,
    val level: String, // Beginner, Intermediate, Advanced
    val description: String,
    val iconEmoji: String,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: Int,
    val prompt: String,
    val phrase: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val audioHint: String
)

object LanguageLessonProvider {
    val initialDictionaryWords = listOf(
        // English
        DictionaryWord(word = "Resilience", translation = "Ketangguhan / Daya Lentur", language = "English", exampleSentence = "She showed great resilience in overcoming challenges.", pronunciation = "/rɪˈzɪl.jəns/", category = "Character"),
        DictionaryWord(word = "Meticulous", translation = "Sangat Teliti / Cermat", language = "English", exampleSentence = "He pays meticulous attention to detail.", pronunciation = "/məˈtɪk.jə.ləs/", category = "Work"),
        DictionaryWord(word = "Serendipity", translation = "Keberuntungan yang tak terduga", language = "English", exampleSentence = "Finding this cafe was pure serendipity.", pronunciation = "/ˌser.ənˈdɪp.ə.ti/", category = "Life"),
        DictionaryWord(word = "Ubiquitous", translation = "Ada di mana-mana", language = "English", exampleSentence = "Smartphones have become ubiquitous.", pronunciation = "/juːˈbɪk.wə.t̬əs/", category = "Tech"),
        DictionaryWord(word = "Effervescent", translation = "Penuh semangat / Berbuih ceria", language = "English", exampleSentence = "She has an effervescent personality.", pronunciation = "/ˌef.ɚˈves.ənt/", category = "Mood"),
        DictionaryWord(word = "Eloquent", translation = "Fasih / Pandai berbicara", language = "English", exampleSentence = "His eloquent speech moved the audience.", pronunciation = "/ˈel.ə.kwənt/", category = "Communication"),
        DictionaryWord(word = "Pragmatic", translation = "Praktis / Berdasarkan kenyataan", language = "English", exampleSentence = "We need a pragmatic approach to solve this.", pronunciation = "/præɡˈmæt̬.ɪk/", category = "Work"),

        // Japanese
        DictionaryWord(word = "Komorebi (木漏れ日)", translation = "Sinar matahari yang menembus celah dedaunan", language = "Japanese", exampleSentence = "Komorebi ga kirei desu (Sinar matahari menembus pohon sangat indah).", pronunciation = "ko-mo-re-bi", category = "Nature"),
        DictionaryWord(word = "Ikigai (生きがい)", translation = "Alasan untuk hidup / Tujuan utama keberadaan", language = "Japanese", exampleSentence = "Mainichi no shigoto ga watashi no ikigai desu.", pronunciation = "i-ki-ga-i", category = "Philosophy"),
        DictionaryWord(word = "Kaizen (改善)", translation = "Perbaikan berkesinambungan secara bertahap", language = "Japanese", exampleSentence = "Kaizen wa seikou no kagi desu (Perbaikan kontinu adalah kunci sukses).", pronunciation = "ka-i-zen", category = "Productivity"),
        DictionaryWord(word = "Otsukaresama (お疲れ様)", translation = "Terima kasih atas kerja kerasmu", language = "Japanese", exampleSentence = "Kyou mo otsukaresama deshita!", pronunciation = "o-tsu-ka-re-sa-ma", category = "Daily"),
        DictionaryWord(word = "Wabi-sabi (侘寂)", translation = "Keindahan dalam ketidaksempurnaan", language = "Japanese", exampleSentence = "Wabi-sabi no bi wo kanjiru.", pronunciation = "wa-bi-sa-bi", category = "Art"),

        // Korean
        DictionaryWord(word = "Hwaiting (화이팅)", translation = "Semangat! / Kamu pasti bisa!", language = "Korean", exampleSentence = "Oneuldo hwaiting haeyo! (Semangat untuk hari ini!)", pronunciation = "hwa-i-ting", category = "Daily"),
        DictionaryWord(word = "Daebak (대박)", translation = "Luar biasa / Keren sekali / Jackpot!", language = "Korean", exampleSentence = "I eopuri daebak-ieyo! (Aplikasi ini keren sekali!)", pronunciation = "dae-bak", category = "Slang"),
        DictionaryWord(word = "Inyeon (인연)", translation = "Takdir ikatan pertemuan antar manusia", language = "Korean", exampleSentence = "Urineun joeun inyeon-iya (Kita memiliki takdir pertemuan yang baik).", pronunciation = "in-yeon", category = "Philosophy"),
        DictionaryWord(word = "Nun-chi (눈치)", translation = "Kepekaan membaca situasi sosial", language = "Korean", exampleSentence = "Nunchiga ppallayo (Dia cepat peka terhadap situasi).", pronunciation = "nun-chi", category = "Social"),

        // German
        DictionaryWord(word = "Fernweh", translation = "Kerinduan untuk menjelajahi tempat asing nan jauh", language = "German", exampleSentence = "Ich habe Fernweh nach den Bergen.", pronunciation = "fehrn-vay", category = "Travel"),
        DictionaryWord(word = "Feierabend", translation = "Waktu santai setelah selesai jam kerja", language = "German", exampleSentence = "Schönen Feierabend! (Selamat beristirahat setelah bekerja!)", pronunciation = "fye-er-ah-bent", category = "Work-Life"),
        DictionaryWord(word = "Gemütlichkeit", translation = "Rasa nyaman, hangat, dan akrab", language = "German", exampleSentence = "Hier herrscht echte Gemütlichkeit.", pronunciation = "geh-moot-lish-kite", category = "Feeling")
    )

    val sampleLessons = listOf(
        LanguageLesson(
            id = "en_1",
            language = "English",
            title = "Mastering Daily Productivity Idioms",
            level = "Menengah",
            description = "Pelajari ekspresi bahasa Inggris yang paling sering digunakan di tempat kerja modern.",
            iconEmoji = "🇬🇧",
            questions = listOf(
                QuizQuestion(
                    id = 1,
                    prompt = "Apa arti idiom 'Hit the ground running'?",
                    phrase = "We need someone who can hit the ground running.",
                    options = listOf(
                        "Langsung bekerja cepat dan efektif sejak awal",
                        "Terjatuh saat mulai berlari",
                        "Menunda pekerjaan sampai menit terakhir",
                        "Membuat rencana di atas kertas"
                    ),
                    correctIndex = 0,
                    explanation = "'Hit the ground running' artinya memulai sebuah tugas atau posisi baru dengan kecepatan dan antusiasme penuh tanpa butuh waktu adaptasi lama.",
                    audioHint = "Pronounce: /hɪt ðə ɡraʊnd ˈrʌn.ɪŋ/"
                ),
                QuizQuestion(
                    id = 2,
                    prompt = "Lengkapi kalimat: 'Let's ______ base tomorrow regarding the new budget plan.'",
                    phrase = "Let's ______ base tomorrow.",
                    options = listOf("touch", "kick", "jump", "call"),
                    correctIndex = 0,
                    explanation = "'Touch base' adalah idiom bisnis yang berarti menghubungi atau berbicara singkat untuk update status.",
                    audioHint = "Pronounce: /tʌtʃ beɪs/"
                ),
                QuizQuestion(
                    id = 3,
                    prompt = "Manakah sinonim dari kata 'Procrastination'?",
                    phrase = "Procrastination is the thief of time.",
                    options = listOf("Penundaan", "Kecepatan", "Ketelitian", "Kreativitas"),
                    correctIndex = 0,
                    explanation = "Procrastination adalah kebiasaan menunda-nunda pekerjaan penting.",
                    audioHint = "Pronounce: /prəˌkræs.təˈneɪ.ʃən/"
                )
            )
        ),
        LanguageLesson(
            id = "jp_1",
            language = "Japanese",
            title = "Japanese Business & Daily Etiquette",
            level = "Dasar & Menengah",
            description = "Ungkapan sopan, salam kerja, dan istilah efisiensi ala Jepang.",
            iconEmoji = "🇯🇵",
            questions = listOf(
                QuizQuestion(
                    id = 1,
                    prompt = "Ungkapan mana yang diucapkan kepada rekan kerja saat jam pulang?",
                    phrase = "お疲れ様でした (Otsukaresama deshita)",
                    options = listOf(
                        "Otsukaresama deshita",
                        "Itadakimasu",
                        "Sayonara tomodachi",
                        "Oyasuminasai"
                    ),
                    correctIndex = 0,
                    explanation = "'Otsukaresama deshita' berarti terima kasih atas kerja keras hari ini.",
                    audioHint = "Romaji: o-tsu-ka-re-sa-ma de-shi-ta"
                ),
                QuizQuestion(
                    id = 2,
                    prompt = "Apa filosofi Jepang untuk perbaikan berkelanjutan?",
                    phrase = "Continuous improvement philosophy",
                    options = listOf("Kaizen (改善)", "Karaoke", "Bonsai", "Origami"),
                    correctIndex = 0,
                    explanation = "'Kaizen' (Kai = Perubahan, Zen = Baik) adalah konsep perbaikan terus-menerus.",
                    audioHint = "Kanji: 改善"
                )
            )
        ),
        LanguageLesson(
            id = "kr_1",
            language = "Korean",
            title = "Korean Conversation & Daily Vocabulary",
            level = "Dasar",
            description = "Kosa kata penting dan ungkapan motivasi Korea.",
            iconEmoji = "🇰🇷",
            questions = listOf(
                QuizQuestion(
                    id = 1,
                    prompt = "Bagaimana mengucapkan 'Terima kasih banyak' secara formal dalam bahasa Korea?",
                    phrase = "Thank you very much (Formal)",
                    options = listOf(
                        "Gamsahamnida (감사합니다)",
                        "Annyeonghaseyo (안녕하세요)",
                        "Mianhaeyo (미안해요)",
                        "Jaljayo (잘자요)"
                    ),
                    correctIndex = 0,
                    explanation = "'Gamsahamnida' adalah ucapan terima kasih formal yang sopan.",
                    audioHint = "Pronounce: gam-sa-ham-ni-da"
                ),
                QuizQuestion(
                    id = 2,
                    prompt = "Kata seruan untuk memberi semangat pantang menyerah adalah...",
                    phrase = "Fighting / Cheering phrase",
                    options = listOf("Hwaiting! (화이팅!)", "Gajima!", "Yeoboseyo", "Babo"),
                    correctIndex = 0,
                    explanation = "'Hwaiting' sering diserukan untuk menyemangati orang lain.",
                    audioHint = "Hangeul: 화이팅"
                )
            )
        )
    )

    fun getCrosswordForDate(dateStr: String): CrosswordPuzzle {
        // Daily changing puzzle based on day hash or date
        val dayNumber = try {
            dateStr.replace("-", "").toLong() % 3
        } catch (e: Exception) {
            0L
        }

        return when (dayNumber) {
            1L -> CrosswordPuzzle(
                dayId = dateStr,
                title = "Teka-Teki Silang: Produktivitas & Karir",
                topic = "Kosakata Sukses & Manajemen Waktu",
                gridSize = 7,
                clues = listOf(
                    CrosswordClue(1, "ACROSS", "Tenggat waktu akhir pengumpulan tugas (Inggris)", "DEADLINE", 0, 0),
                    CrosswordClue(2, "DOWN", "Pekerjaan atau amanat yang harus diselesaikan", "TUGAS", 0, 0),
                    CrosswordClue(3, "ACROSS", "Rencana pengeluaran dan pemasukan bulanan", "ANGGARAN", 2, 0),
                    CrosswordClue(4, "DOWN", "Perangkat lunak asisten cerdas Anda ini", "MYEVO", 1, 3),
                    CrosswordClue(5, "ACROSS", "Kondisi konsentrasi penuh dan produktif (Inggris)", "FOCUS", 4, 1)
                ),
                xpReward = 200
            )
            2L -> CrosswordPuzzle(
                dayId = dateStr,
                title = "Teka-Teki Silang: Dunia Keuangan & Finansial",
                topic = "Finansial Sehat & Cerdas",
                gridSize = 7,
                clues = listOf(
                    CrosswordClue(1, "ACROSS", "Kewajiban finansial yang wajib dibayarkan kembali", "UTANG", 0, 1),
                    CrosswordClue(2, "DOWN", "Uang yang dipinjamkan kepada orang lain (Hak tagih)", "PIUTANG", 0, 1),
                    CrosswordClue(3, "ACROSS", "Penanaman modal untuk keuntungan di masa depan", "INVESTASI", 2, 0),
                    CrosswordClue(4, "DOWN", "Cadangan dana tersimpan di tempat aman", "TABUNGAN", 1, 4),
                    CrosswordClue(5, "ACROSS", "Imbalan penghasilan tetap bulanan dari pekerjaan", "GAJI", 4, 2)
                ),
                xpReward = 200
            )
            else -> CrosswordPuzzle(
                dayId = dateStr,
                title = "Teka-Teki Silang: Bahasa & Kosakata Global",
                topic = "Bahasa Asing & Pola Pikir Maju",
                gridSize = 7,
                clues = listOf(
                    CrosswordClue(1, "ACROSS", "Alasan hidup atau tujuan keberadaan (Jepang)", "IKIGAI", 0, 0),
                    CrosswordClue(2, "DOWN", "Perbaikan terus-menerus tanpa henti (Jepang)", "KAIZEN", 0, 1),
                    CrosswordClue(3, "ACROSS", "Buku atau aplikasi pemuat arti kata-kata", "KAMUS", 2, 0),
                    CrosswordClue(4, "DOWN", "Kemampuan berbahasa banyak negara", "POLIGLOT", 0, 4),
                    CrosswordClue(5, "ACROSS", "Kecerdasan Buatan dalam bahasa Inggris disingkat...", "AI", 4, 3)
                ),
                xpReward = 250
            )
        }
    }
}
