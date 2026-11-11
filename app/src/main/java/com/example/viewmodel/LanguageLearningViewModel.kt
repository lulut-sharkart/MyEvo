package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.LanguageLearningDao
import com.example.data.local.MyEvoDatabase
import com.example.data.model.LanguageLearningEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * LanguageLearningViewModel manages daily language vocabulary lists and progress tracking
 * for English, Japanese, Korean, and German, backed by Room database.
 */
class LanguageLearningViewModel(
    private val dao: LanguageLearningDao,
    application: Application? = null
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow("All")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Raw stream of all vocabulary
    val allVocabulary: StateFlow<List<LanguageLearningEntity>> = dao.getAllVocabulary()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered vocabulary based on language and search query
    val filteredVocabulary: StateFlow<List<LanguageLearningEntity>> = combine(
        allVocabulary,
        _selectedLanguage,
        _searchQuery
    ) { list, lang, query ->
        list.filter { item ->
            val matchesLang = if (lang == "All") true else item.language.equals(lang, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                item.word.contains(query, ignoreCase = true) ||
                    item.translation.contains(query, ignoreCase = true) ||
                    item.exampleSentence.contains(query, ignoreCase = true)
            }
            matchesLang && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Progress Tracking Metrics
    val totalWordCount: StateFlow<Int> = allVocabulary.combine(_selectedLanguage) { list, lang ->
        if (lang == "All") list.size else list.count { it.language.equals(lang, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val masteredCount: StateFlow<Int> = allVocabulary.combine(_selectedLanguage) { list, lang ->
        list.filter { if (lang == "All") true else it.language.equals(lang, ignoreCase = true) }
            .count { it.isMastered || it.masteryLevel >= 5 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inProgressCount: StateFlow<Int> = combine(totalWordCount, masteredCount) { total, mastered ->
        (total - mastered).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overallMasteryRate: StateFlow<Float> = combine(totalWordCount, masteredCount) { total, mastered ->
        if (total == 0) 0f else (mastered.toFloat() / total).coerceIn(0f, 1f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init {
        seedDefaultVocabularyIfEmpty()
    }

    fun setSelectedLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun recordReview(wordId: Long, remembered: Boolean) {
        viewModelScope.launch {
            val list = allVocabulary.first()
            val item = list.find { it.id == wordId } ?: return@launch

            val newLevel = if (remembered) {
                (item.masteryLevel + 1).coerceAtMost(5)
            } else {
                (item.masteryLevel - 1).coerceAtLeast(0)
            }
            val isNowMastered = newLevel >= 5
            val newReviewCount = item.reviewCount + 1
            val timestamp = System.currentTimeMillis()

            dao.updateMastery(
                id = wordId,
                level = newLevel,
                mastered = isNowMastered,
                reviews = newReviewCount,
                timestamp = timestamp
            )
        }
    }

    fun toggleMastered(wordId: Long) {
        viewModelScope.launch {
            val list = allVocabulary.first()
            val item = list.find { it.id == wordId } ?: return@launch
            val newMastered = !item.isMastered
            val newLevel = if (newMastered) 5 else 0

            dao.updateMastery(
                id = wordId,
                level = newLevel,
                mastered = newMastered,
                reviews = item.reviewCount,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun addVocabulary(
        word: String,
        translation: String,
        language: String,
        phonetic: String = "",
        exampleSentence: String = "",
        exampleTranslation: String = "",
        category: String = "Daily"
    ) {
        viewModelScope.launch {
            val entity = LanguageLearningEntity(
                word = word.trim(),
                translation = translation.trim(),
                language = language,
                phonetic = phonetic.trim(),
                exampleSentence = exampleSentence.trim(),
                exampleTranslation = exampleTranslation.trim(),
                category = category
            )
            dao.insertWord(entity)
        }
    }

    fun deleteVocabulary(word: LanguageLearningEntity) {
        viewModelScope.launch {
            dao.deleteWord(word)
        }
    }

    fun seedDefaultVocabularyIfEmpty() {
        viewModelScope.launch {
            val count = dao.getWordCount()
            if (count == 0) {
                val seedData = listOf(
                    // English
                    LanguageLearningEntity(
                        word = "Persistence",
                        translation = "Kegigihan / Ketekunan",
                        language = "English",
                        phonetic = "/pərˈsɪs.təns/",
                        exampleSentence = "Persistence guarantees that results will follow effort.",
                        exampleTranslation = "Kegigihan memastikan hasil akan mengikuti usaha.",
                        category = "Motivation",
                        masteryLevel = 3
                    ),
                    LanguageLearningEntity(
                        word = "Serendipity",
                        translation = "Keberuntungan yang tak disengaja",
                        language = "English",
                        phonetic = "/ˌser.ənˈdɪp.ə.t̬i/",
                        exampleSentence = "Finding this useful tool was pure serendipity.",
                        exampleTranslation = "Menemukan alat berguna ini adalah keberuntungan yang tak disengaja.",
                        category = "Daily",
                        masteryLevel = 2
                    ),
                    // Japanese
                    LanguageLearningEntity(
                        word = "頑張って (Ganbatte)",
                        translation = "Semangat / Berusahalah yang terbaik!",
                        language = "Japanese",
                        phonetic = "gan-bat-te",
                        exampleSentence = "明日も頑張ってください。(Ashita mo ganbatte kudasai.)",
                        exampleTranslation = "Besok juga tetap semangat ya.",
                        category = "Daily",
                        masteryLevel = 4
                    ),
                    LanguageLearningEntity(
                        word = "木漏れ日 (Komorebi)",
                        translation = "Sinar matahari menembus sela dedaunan",
                        language = "Japanese",
                        phonetic = "ko-mo-re-bi",
                        exampleSentence = "木漏れ日がとても美しいです。(Komorebi ga totemo utsukushii desu.)",
                        exampleTranslation = "Sinar matahari di sela dedaunan sangat indah.",
                        category = "Nature",
                        masteryLevel = 1
                    ),
                    // Korean
                    LanguageLearningEntity(
                        word = "화이팅 (Haiting)",
                        translation = "Semangat / Maju terus!",
                        language = "Korean",
                        phonetic = "hwa-i-ting",
                        exampleSentence = "오늘 하루도 화이팅하세요! (Oneul harudo hwaitinghaseyo!)",
                        exampleTranslation = "Semangat menjalani hari ini!",
                        category = "Daily",
                        masteryLevel = 5,
                        isMastered = true
                    ),
                    LanguageLearningEntity(
                        word = "소확행 (Sohwakhaeng)",
                        translation = "Kebahagiaan kecil namun pasti",
                        language = "Korean",
                        phonetic = "so-hwak-haeng",
                        exampleSentence = "따뜻한 차 한 잔의 소확행. (Ttatteuthan cha han jan-ui sohwakhaeng.)",
                        exampleTranslation = "Kebahagiaan kecil dari secangkir teh hangat.",
                        category = "Lifestyle",
                        masteryLevel = 2
                    ),
                    // German
                    LanguageLearningEntity(
                        word = "Gemütlichkeit",
                        translation = "Kenyamanan dan kehangatan suasana",
                        language = "German",
                        phonetic = "/ɡəˈmyːtlɪçkaɪt/",
                        exampleSentence = "Das Café strahlt pure Gemütlichkeit aus.",
                        exampleTranslation = "Kafe ini memancarkan suasana yang sangat nyaman dan hangat.",
                        category = "Lifestyle",
                        masteryLevel = 3
                    )
                )
                dao.insertWords(seedData)
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = MyEvoDatabase.getDatabase(application)
                    return LanguageLearningViewModel(db.languageLearningDao(), application) as T
                }
            }
    }
}
