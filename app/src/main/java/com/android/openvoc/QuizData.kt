package com.android.openvoc

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WordEntry(
    val word: String,
    val letterCount: Int,
    val definitions: List<Definition>
)

@Serializable
data class Definition(
    val text: String,
    val partOfSpeech: String
)

enum class QuizMode(val label: String) {
    CH_TO_EN("中翻英"), EN_TO_CH("英翻中")
}

class VocabularyRepository(private val context: Context) {
    companion object {
        private val jsonInstance = Json { ignoreUnknownKeys = true }
    }

    suspend fun loadLevel(level: Int): List<WordEntry> = withContext(Dispatchers.IO) {
        try {
            context.assets.open("level$level.json").bufferedReader().use { reader ->
                jsonInstance.decodeFromString<List<WordEntry>>(reader.readText())
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}