package com.android.openvoc

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { START, QUIZ, RESULT, WORD_LIST }

class QuizViewModel(private val repository: VocabularyRepository) : ViewModel() {
    data class QuizResult(
        val word: WordEntry,
        val isCorrect: Boolean
    )

    var currentScreen by mutableStateOf(Screen.START)
    var allWords by mutableStateOf<List<WordEntry>>(emptyList())
    var currentQuestion by mutableStateOf<Question?>(null)
    var score by mutableIntStateOf(0)
    var questionCount by mutableIntStateOf(0)
    var targetQuestionCount by mutableIntStateOf(10)
    var isFinished by mutableStateOf(false)
    val quizHistory = mutableStateListOf<QuizResult>()
    val quizProgress by derivedStateOf {
        if (targetQuestionCount > 0) {
            questionCount.toFloat() / targetQuestionCount.toFloat()
        } else 0f
    }
    private var quizQueue: List<WordEntry> = emptyList()

    data class Question(
        val correctWord: WordEntry,
        val options: List<String>,
        val mode: QuizMode
    )

    fun startQuiz(level: Int, mode: QuizMode, count: Int) {
        viewModelScope.launch {
            val loadedWords = withContext(Dispatchers.IO) {
                repository.loadLevel(level)
            }
            allWords = loadedWords
            targetQuestionCount = count
            score = 0
            questionCount = 0
            isFinished = false
            quizHistory.clear()
            quizQueue = loadedWords.shuffled().take(count)

            nextQuestion(mode)
            currentScreen = Screen.QUIZ
        }
    }

    fun nextQuestion(mode: QuizMode) {
        if (questionCount >= quizQueue.size) return

        val correct = quizQueue[questionCount]
        val distractors = mutableSetOf<WordEntry>()
        val totalWords = allWords.size
        if (totalWords >= 4) {
            while (distractors.size < 3) {
                val randomIndex = (0 until totalWords).random()
                val candidate = allWords[randomIndex]
                if (candidate.word != correct.word) {
                    distractors.add(candidate)
                }
            }
        }

        val options = when (mode) {
            QuizMode.CH_TO_EN -> (distractors.map { it.word } + correct.word).shuffled()
            QuizMode.EN_TO_CH -> (distractors.map { it.definitions.first().text } + correct.definitions.first().text).shuffled()
        }

        currentQuestion = Question(correct, options, mode)
        questionCount++
    }

    fun answer(selected: String) {
        val question = currentQuestion ?: return
        val isCorrect = when (question.mode) {
            QuizMode.CH_TO_EN -> selected == question.correctWord.word
            QuizMode.EN_TO_CH -> selected == question.correctWord.definitions.first().text
        }

        quizHistory.add(QuizResult(question.correctWord, isCorrect))

        if (isCorrect) score++

        if (questionCount < targetQuestionCount) {
            nextQuestion(question.mode)
        } else {
            isFinished = true
            currentScreen = Screen.RESULT
        }
    }

    companion object {
        fun provideFactory(repository: VocabularyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuizViewModel(repository) as T
                }
            }
    }
}