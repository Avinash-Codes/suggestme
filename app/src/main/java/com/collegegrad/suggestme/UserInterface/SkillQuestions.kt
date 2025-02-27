package com.collegegrad.suggestme.UserInterface

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.collegegrad.suggestme.dataclass.QuestionResult
import com.collegegrad.suggestme.viewmodel.UserViewModel
import kotlin.math.roundToInt

data class MCQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    var selectedAnswer: String? = null
)

/**
 * Swipeable Skill Assessment screen that displays questions in card format
 * with swipe animation and next button navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableSkillAssessmentScreen(
    questionsText: String,
    userId: String,
    userViewModel: UserViewModel,
    onBackPressed: () -> Unit
) {
    // Parse the questions from text to MCQuestion objects
    val questions = remember { parseQuestions(questionsText) }
    var currentScore by remember { mutableIntStateOf(0) }
    var hasSubmitted by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }

    // Current question index
    var currentQuestionIndex by remember { mutableIntStateOf(0) }

    // Animation state
    val animatedOffsetX = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(1f) }

    // Whether the card is being swiped
    var isSwiping by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf("") }

    // State for tracking drag
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var shouldSpringBack by remember { mutableStateOf(false) }
    var shouldSwipeToNext by remember { mutableStateOf(false) }

    // Animation scope for transitions
    val enterTransition = remember {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300))
    }

    val exitTransition = remember {
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    }

    // Function to handle card swipe
    LaunchedEffect(isSwiping) {
        if (isSwiping) {
            // Animate swipe out with selection disabled
            cardAlpha.animateTo(0f, animationSpec = tween(300))

            // Move to next question
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
            }

            // Reset position and fade in
            animatedOffsetX.snapTo(0f)
            cardAlpha.animateTo(1f, animationSpec = tween(300))
            isSwiping = false
        }
    }

    // Handle spring back animation
    LaunchedEffect(shouldSpringBack) {
        if (shouldSpringBack) {
            animatedOffsetX.animateTo(0f, spring())
            shouldSpringBack = false
        }
    }

    // Handle swipe to next animation
    LaunchedEffect(shouldSwipeToNext) {
        if (shouldSwipeToNext) {
            isSwiping = true
            shouldSwipeToNext = false
        }
    }

    // Apply drag offset to animation
    LaunchedEffect(dragOffset) {
        animatedOffsetX.snapTo(dragOffset)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skill Assessment") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (showResults) {
            // Convert MCQuestion objects to QuestionResult objects
            val questionResults = questions.map { mcQuestion ->
                QuestionResult(
                    question = mcQuestion.question,
                    selectedAnswer = mcQuestion.selectedAnswer ?: "",
                    correctAnswer = mcQuestion.correctAnswer,
                    isCorrect = mcQuestion.selectedAnswer == mcQuestion.correctAnswer,
                    options = mcQuestion.options
                )
            }

            // Save assessment results to Firebase
            LaunchedEffect(Unit) {
                userViewModel.saveAssessmentResult(
                    userId = userId,
                    score = currentScore,
                    totalQuestions = questions.size,
                    questions = questionResults
                )
            }

            // Show results screen
            ResultScreen(
                score = currentScore,
                totalQuestions = questions.size,
                questions = questions,
                onRetakeQuiz = {
                    // Reset quiz
                    questions.forEach { it.selectedAnswer = null }
                    currentScore = 0
                    hasSubmitted = false
                    showResults = false
                    currentQuestionIndex = 0
                },
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Questions screen
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Progress indicator and question counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${questions.count { it.selectedAnswer != null }} answered",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Swipeable question card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (currentQuestionIndex < questions.size) {
                        val currentQuestion = questions[currentQuestionIndex]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(cardAlpha.value)
                                .offset { IntOffset(animatedOffsetX.value.roundToInt(), 0) }
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (dragOffset < -300 &&
                                                currentQuestion.selectedAnswer != null
                                            ) {
                                                shouldSwipeToNext = true
                                            } else {
                                                // Spring back if not swiped enough
                                                shouldSpringBack = true
                                            }
                                        },
                                        onDragCancel = {
                                            // Spring back on cancel
                                            shouldSpringBack = true
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            if (currentQuestion.selectedAnswer != null) {
                                                // Only allow swiping left
                                                if (dragAmount < 0) {
                                                    // Update the drag offset
                                                    dragOffset += dragAmount
                                                }
                                            }
                                        }
                                    )
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            SwipeableQuestionCard(
                                question = currentQuestion,
                                questionNumber = currentQuestionIndex + 1,
                                totalQuestions = questions.size,
                                isSwiping = isSwiping || shouldSwipeToNext, // Pass the swiping state
                                onAnswerSelected = { answer ->
                                    currentQuestion.selectedAnswer = answer
                                }
                            )
                        }
                    }
                }

                // Controls for navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onBackPressed
                    ) {
                        Text("Cancel")
                    }

                    if (currentQuestionIndex < questions.size - 1) {
                        // Next button - only show if not on last question
                        Button(
                            onClick = {
                                if (questions[currentQuestionIndex].selectedAnswer != null) {
                                    shouldSwipeToNext = true
                                }
                            },
                            enabled = questions[currentQuestionIndex].selectedAnswer != null
                        ) {
                            Text("Next")
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = "Next question",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    } else {
                        // Submit button - only show on last question
                        Button(
                            onClick = {
                                // Calculate score
                                currentScore = calculateScore(questions)
                                hasSubmitted = true
                                showResults = true
                            },
                            enabled = isAllQuestionsAnswered(questions)
                        ) {
                            Text("Submit")
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Submit answers",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableQuestionCard(
    question: MCQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    isSwiping: Boolean, // Add this parameter
    onAnswerSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = question.question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Options
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            question.options.forEachIndexed { index, option ->
                val optionLetter = ('A' + index).toString()
                val isSelected = question.selectedAnswer == optionLetter

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        // Disable click when swiping
                        .clickable(enabled = !isSwiping) { onAnswerSelected(optionLetter) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = optionLetter,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(end = 8.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = option,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Swipe hint text
        if (question.selectedAnswer != null) {
            Text(
                text = "Swipe left to continue →",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }
    }
}

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    questions: List<MCQuestion>,
    onRetakeQuiz: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Results",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$score/$totalQuestions",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${(score.toFloat() / totalQuestions * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Performance feedback
                val performanceFeedback = when {
                    score == totalQuestions -> "Excellent! You've mastered this skill!"
                    score >= totalQuestions * 0.8 -> "Great job! You have a strong understanding."
                    score >= totalQuestions * 0.6 -> "Good work! You have a decent grasp of this skill."
                    score >= totalQuestions * 0.4 -> "You're making progress, but could use more practice."
                    else -> "Keep learning! This skill needs more development."
                }

                Text(
                    text = performanceFeedback,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetakeQuiz,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retake Quiz")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackPressed,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

// Helper functions
private fun parseQuestions(questionsText: String): List<MCQuestion> {
    val questions = mutableListOf<MCQuestion>()

    // Split the text by "Question:" to get individual questions
    val questionBlocks = questionsText.split(Regex("(?i)Question(?:\\s*\\d+)?:\\s*"))
        .filter { it.isNotBlank() }

    for (block in questionBlocks) {
        try {
            // Find the question text (everything up to the first "A)")
            val questionText = block.substringBefore("A)").trim()

            // Extract options
            val optionRegex = Regex("([A-D])\\)\\s*([^\\n]+)")
            val options = optionRegex.findAll(block).map { it.groupValues[2].trim() }.toList()

            // Find correct answer
            val correctAnswerRegex = Regex("(?i)Correct Answer:\\s*([A-D])")
            val correctAnswerMatch = correctAnswerRegex.find(block)

            if (correctAnswerMatch != null && options.size >= 4) {
                val correctAnswer = correctAnswerMatch.groupValues[1]
                questions.add(
                    MCQuestion(
                        question = questionText,
                        options = options,
                        correctAnswer = correctAnswer
                    )
                )
            }
        } catch (e: Exception) {
            // Skip malformed questions
            continue
        }
    }

    return questions
}

private fun calculateScore(questions: List<MCQuestion>): Int {
    return questions.count { it.selectedAnswer == it.correctAnswer }
}

private fun isAllQuestionsAnswered(questions: List<MCQuestion>): Boolean {
    return questions.all { it.selectedAnswer != null }
}