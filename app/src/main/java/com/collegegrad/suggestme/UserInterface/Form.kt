package com.collegegrad.suggestme.UserInterface

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.collegegrad.suggestme.BuildConfig
import com.collegegrad.suggestme.viewmodel.UserOperationResult
import com.collegegrad.suggestme.viewmodel.UserViewModel
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max

data class Skill(
    val name: String,
    var level: SkillLevel = SkillLevel.BEGINNER,
    val isCustom: Boolean = false
)

enum class SkillLevel {
    BEGINNER, INTERMEDIATE, EXPERT
}

data class Interest(val name: String)
data class EndGoal(val name: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillSelectionForm(userViewModel: UserViewModel) {
    var selectedSkill by remember { mutableStateOf<Skill?>(null) }
    var customSkillText by remember { mutableStateOf("") }
    var selectedSkills by remember { mutableStateOf(listOf<Skill>()) }
    var isAddingCustomSkill by remember { mutableStateOf(false) }
    var showSkillDropdown by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Add state for course suggestions and skill tests
    var courseSuggestions by remember { mutableStateOf("") }
    var skillQuestions by remember { mutableStateOf("") }
    var isLoadingSuggestions by remember { mutableStateOf(false) }
    var showSuggestionsDialog by remember { mutableStateOf(false) }

    // Track submission state
    val operationState = userViewModel.operationState.value
    val isLoading = userViewModel.isLoading.value
    val successMessage = userViewModel.successMessage.value

    val users by userViewModel.userData.collectAsState()

    // Initialize the Gemini model
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = BuildConfig.apiKey
        )
    }

    // Predefined lists
    val predefinedSkills = remember {
        listOf(
            "Kotlin", "Java", "Android", "Jetpack Compose",
            "Flutter", "JavaScript", "Python", "Swift",
            "React Native", "Other"
        )
    }

    val interests = remember {
        listOf(
            Interest("Mobile Development"),
            Interest("Web Development"),
            Interest("Backend Development"),
            Interest("DevOps"),
            Interest("Machine Learning"),
            Interest("UI/UX Design")
        )
    }

    val endGoals = remember {
        listOf(
            EndGoal("Become a professional developer"),
            EndGoal("Build personal projects"),
            EndGoal("Career transition"),
            EndGoal("Enhance current skills"),
            EndGoal("Freelancing")
        )
    }

    var selectedInterests by remember { mutableStateOf(setOf<Interest>()) }
    var selectedEndGoals by remember { mutableStateOf(setOf<EndGoal>()) }

    // Display success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text("Your profile has been saved successfully!") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    // After dismissing success dialog, generate course suggestions
                    generateCourseSuggestions(
                        userName = userName,
                        skills = selectedSkills,
                        interests = selectedInterests,
                        endGoals = selectedEndGoals,
                        generativeModel = generativeModel,
                        onSuggestionsReady = { suggestions, questions ->
                            courseSuggestions = suggestions
                            skillQuestions = questions
                            isLoadingSuggestions = false
                            showSuggestionsDialog = true
                        },
                        onLoadingChange = { isLoading ->
                            isLoadingSuggestions = isLoading
                        }
                    )
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Display the suggestions dialog
    if (showSuggestionsDialog) {
        AlertDialog(
            onDismissRequest = { showSuggestionsDialog = false },
            title = { Text("Personalized Recommendations") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Course Suggestions:", style = MaterialTheme.typography.titleMedium)
                    Text(courseSuggestions)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Skill Assessment Questions:", style = MaterialTheme.typography.titleMedium)
                    Text(skillQuestions)
                }
            },
            confirmButton = {
                Button(onClick = { showSuggestionsDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Handle operation state changes
    LaunchedEffect(operationState) {
        when (operationState) {
            is UserOperationResult.Success -> {
                showSuccessDialog = true
            }
            is UserOperationResult.Error -> {
                // You could show an error dialog here
            }
            else -> {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Skill Selection Form",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Select your skills and expertise level",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // User name field
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Your Name",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Enter your name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Skills selection section
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Technical Skills",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Dropdown for selecting skills
                    Box {
                        OutlinedTextField(
                            value = if (isAddingCustomSkill) customSkillText else selectedSkill?.name ?: "",
                            onValueChange = {
                                if (isAddingCustomSkill) {
                                    customSkillText = it
                                }
                            },
                            label = { Text("Select or enter a skill") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = !isAddingCustomSkill,
                            trailingIcon = {
                                IconButton(onClick = { showSkillDropdown = !showSkillDropdown }) {
                                    Icon(
                                        imageVector = if (showSkillDropdown)
                                            Icons.Filled.ArrowDropUp
                                        else
                                            Icons.Filled.ArrowDropDown,
                                        contentDescription = "Toggle Dropdown"
                                    )
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = showSkillDropdown,
                            onDismissRequest = { showSkillDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            predefinedSkills.forEach { skill ->
                                DropdownMenuItem(
                                    text = { Text(skill) },
                                    onClick = {
                                        if (skill == "Other") {
                                            isAddingCustomSkill = true
                                            customSkillText = ""
                                        } else {
                                            selectedSkill = Skill(skill)
                                            isAddingCustomSkill = false
                                        }
                                        showSkillDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Skill level selection (only show when a skill is selected)
                    if (selectedSkill != null || isAddingCustomSkill) {
                        Text(
                            "Select your expertise level:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SkillLevel.values().forEach { level ->
                                FilterChip(
                                    selected = if (selectedSkill != null) {
                                        selectedSkill!!.level == level
                                    } else false,
                                    onClick = {
                                        if (selectedSkill != null) {
                                            selectedSkill = selectedSkill!!.copy(level = level)
                                        }
                                    },
                                    label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isAddingCustomSkill && customSkillText.isNotBlank()) {
                                    val newSkill = Skill(
                                        customSkillText,
                                        SkillLevel.BEGINNER,
                                        true
                                    )
                                    selectedSkills = selectedSkills + newSkill
                                    customSkillText = ""
                                    isAddingCustomSkill = false
                                } else if (selectedSkill != null) {
                                    selectedSkills = selectedSkills + selectedSkill!!
                                    selectedSkill = null
                                }
                            },
                            enabled = (isAddingCustomSkill && customSkillText.isNotBlank()) || selectedSkill != null,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Skill")
                        }
                    }
                }
            }
        }

        // Display selected skills
        if (selectedSkills.isNotEmpty()) {
            item {
                Text(
                    "Your Skills",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(selectedSkills) { skill ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                skill.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Level: ${skill.level.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedSkills = selectedSkills.filter { it != skill }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remove Skill"
                            )
                        }
                    }
                }
            }
        }

        // Interests section
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Your Interests",
                        style = MaterialTheme.typography.titleMedium
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        interests.forEach { interest ->
                            FilterChip(
                                selected = interest in selectedInterests,
                                onClick = {
                                    selectedInterests = if (interest in selectedInterests) {
                                        selectedInterests - interest
                                    } else {
                                        selectedInterests + interest
                                    }
                                },
                                label = { Text(interest.name) }
                            )
                        }
                    }
                }
            }
        }

        // End Goals section
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Your End Goals",
                        style = MaterialTheme.typography.titleMedium
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),

                    ) {
                        endGoals.forEach { goal ->
                            FilterChip(
                                selected = goal in selectedEndGoals,
                                onClick = {
                                    selectedEndGoals = if (goal in selectedEndGoals) {
                                        selectedEndGoals - goal
                                    } else {
                                        selectedEndGoals + goal
                                    }
                                },
                                label = { Text(goal.name) }
                            )
                        }
                    }
                }
            }
        }

        // Submit button
        item {
            Button(
                onClick = {
                    // Format the data for Firebase
                    val userId = UUID.randomUUID().toString()

                    // Format skills as a string
                    val formattedSkills = selectedSkills.joinToString(", ") {
                        "${it.name}:${it.level.name}"
                    }

                    // Format interests and goals
                    val formattedInterests = selectedInterests.map { it.name }
                    val formattedEndGoals = selectedEndGoals.map { it.name }

                    // Add the user to Firestore with separate skills and experience
                    userViewModel.addUserToFireStore(
                        id = userId,
                        name = userName,
                        skills = formattedSkills,
                        // experience = "" - Using default empty string
                        yourEndGoal = formattedEndGoals,
                        interests = formattedInterests
                    )
                    userViewModel.getAllUserDetails(onComplete = {
                        userViewModel.userData
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = userName.isNotBlank() &&
                        selectedSkills.isNotEmpty() &&
                        selectedInterests.isNotEmpty() &&
                        selectedEndGoals.isNotEmpty() &&
                        !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Show loading indicator for suggestions
        if (isLoadingSuggestions) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Generating personalized course suggestions and skill tests...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

// Function to generate course suggestions using Gemini API
private fun generateCourseSuggestions(
    userName: String,
    skills: List<Skill>,
    interests: Set<Interest>,
    endGoals: Set<EndGoal>,
    generativeModel: GenerativeModel,
    onSuggestionsReady: (String, String) -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    onLoadingChange(true)

    // Create a coroutine scope
    val scope = CoroutineScope(Dispatchers.IO)

    scope.launch {
        try {
            // Format the user profile data
            val skillsText = skills.joinToString(", ") {
                "${it.name} (${it.level.name.lowercase().replaceFirstChar { it.uppercase() }})"
            }

            val interestsText = interests.joinToString(", ") { it.name }
            val endGoalsText = endGoals.joinToString(", ") { it.name }

            // Build the prompt for Gemini
            val prompt = """
                Based on the following user profile, suggest:
                1. Five specific courses or learning resources that would help this user advance their skills and reach their goals.
                2. Five technical assessment questions related to their skills to test their knowledge level.
                
                User Profile:
                Name: $userName
                Skills: $skillsText
                Interests: $interestsText
                End Goals: $endGoalsText
                
                Please format your response with clear sections for "Course Suggestions" and "Skill Assessment Questions".
                For each course, include a brief description of why it's relevant to the user's goals.
                For each question, provide a difficulty level matching their skill level.
            """.trimIndent()

            // Call Gemini API
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: "Sorry, I couldn't generate recommendations at this time."

            // Parse the results
            val parts = parseGeminiResponse(responseText)

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                onSuggestionsReady(parts.first, parts.second)
            }

        } catch (e: Exception) {
            Log.e("GeminiAPI", "Error generating suggestions: ${e.message}")

            // Update UI with error
            withContext(Dispatchers.Main) {
                onSuggestionsReady(
                    "Error generating course suggestions. Please try again.",
                    "Error generating skill assessment questions. Please try again."
                )
            }
        } finally {
            withContext(Dispatchers.Main) {
                onLoadingChange(false)
            }
        }
    }
}

// Helper function to parse Gemini's response into course suggestions and skill questions
private fun parseGeminiResponse(response: String): Pair<String, String> {
    // Default values in case parsing fails
    var courseSuggestions = response
    var skillQuestions = ""

    // Try to find the sections in the response
    val courseSectionRegex = "(?i)course suggestions:?(.+?)(?=skill assessment|$)".toRegex(RegexOption.DOT_MATCHES_ALL)
    val questionSectionRegex = "(?i)skill assessment questions:?(.+)$".toRegex(RegexOption.DOT_MATCHES_ALL)

    val courseMatch = courseSectionRegex.find(response)
    val questionMatch = questionSectionRegex.find(response)

    if (courseMatch != null) {
        courseSuggestions = courseMatch.groupValues[1].trim()
    }

    if (questionMatch != null) {
        skillQuestions = questionMatch.groupValues[1].trim()
    }

    return Pair(courseSuggestions, skillQuestions)
}