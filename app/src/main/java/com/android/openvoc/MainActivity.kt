package com.android.openvoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.openvoc.ui.theme.OpenVOCTheme

class MainActivity : ComponentActivity() {
    private val viewModel: QuizViewModel by viewModels {
        QuizViewModel.provideFactory(VocabularyRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OpenVOCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: QuizViewModel) {
    when (viewModel.currentScreen) {
        Screen.START -> StartScreen(onStart = viewModel::startQuiz)
        Screen.QUIZ -> QuizScreen(
            viewModel = viewModel,
            onExitQuiz = { viewModel.currentScreen = Screen.START }
        )

        Screen.RESULT -> ResultScreen(
            viewModel = viewModel,
            onRestart = {
                viewModel.isFinished = false
                viewModel.currentScreen = Screen.START
            },
            onViewWords = { viewModel.currentScreen = Screen.WORD_LIST }
        )

        Screen.WORD_LIST -> WordListScreen(
            viewModel = viewModel,
            onBack = { viewModel.currentScreen = Screen.RESULT }
        )
    }
}

@Composable
fun StartScreen(onStart: (Int, QuizMode, Int) -> Unit) {
    var level by remember { mutableIntStateOf(1) }
    var mode by remember { mutableStateOf(QuizMode.EN_TO_CH) }
    var count by remember { mutableIntStateOf(10) }

    val levels = (1..6).toList()
    val modes = QuizMode.entries
    val counts = listOf(10, 15, 20, 30)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(stringResource(R.string.app_subtitle), style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(64.dp))

        HorizontalPicker(
            label = stringResource(R.string.label_level),
            valueText = "Lv $level",
            onLeftClick = {
                val idx = levels.indexOf(level)
                level = levels[(idx - 1 + levels.size) % levels.size]
            },
            onRightClick = {
                val idx = levels.indexOf(level)
                level = levels[(idx + 1) % levels.size]
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPicker(
            label = stringResource(R.string.label_mode),
            valueText = mode.label,
            onLeftClick = {
                val idx = modes.indexOf(mode)
                mode = modes[(idx - 1 + modes.size) % modes.size]
            },
            onRightClick = {
                val idx = modes.indexOf(mode)
                mode = modes[(idx + 1) % modes.size]
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPicker(
            label = stringResource(R.string.label_count),
            valueText = "$count 題",
            onLeftClick = {
                val idx = counts.indexOf(count)
                count = counts[(idx - 1 + counts.size) % counts.size]
            },
            onRightClick = {
                val idx = counts.indexOf(count)
                count = counts[(idx + 1) % counts.size]
            }
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { onStart(level, mode, count) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(stringResource(R.string.btn_start))
        }
    }
}

@Composable
fun HorizontalPicker(
    label: String,
    valueText: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onLeftClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous"
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onRightClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next"
                )
            }
        }
    }
}

@Composable
fun QuizScreen(viewModel: QuizViewModel, onExitQuiz: () -> Unit) {
    val question = viewModel.currentQuestion ?: return
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            title = { Text(stringResource(R.string.dialog_exit_title)) },
            text = { Text(stringResource(R.string.dialog_exit_text)) },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; onExitQuiz() }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(
                R.string.question_progress,
                viewModel.questionCount,
                viewModel.targetQuestionCount
            ),
            style = MaterialTheme.typography.labelLarge
        )
        LinearProgressIndicator(
            progress = { viewModel.quizProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(CircleShape),
        )

        Spacer(modifier = Modifier.weight(0.5f))

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (question.mode == QuizMode.CH_TO_EN)
                        question.correctWord.definitions.first().text else question.correctWord.word,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        question.options.forEach { option ->
            Button(
                onClick = { viewModel.answer(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(64.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(option, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ResultScreen(viewModel: QuizViewModel, onRestart: () -> Unit, onViewWords: () -> Unit) {
    BackHandler { onRestart() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.quiz_finished),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${viewModel.score} / ${viewModel.targetQuestionCount}",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(stringResource(R.string.score_label), style = MaterialTheme.typography.labelLarge)

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_back_home))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onViewWords, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_view_words))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(viewModel: QuizViewModel, onBack: () -> Unit) {
    BackHandler { onBack() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.word_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(
                items = viewModel.quizHistory,
                key = { it.word.word }
            ) { result ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = result.word.word,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    supportingContent = {
                        Text("${result.word.definitions.first().partOfSpeech}. ${result.word.definitions.first().text}")
                    },
                    trailingContent = {
                        if (result.isCorrect) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "正確",
                                tint = Color(0xFF4CAF50)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "錯誤",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
            }
        }
    }
}