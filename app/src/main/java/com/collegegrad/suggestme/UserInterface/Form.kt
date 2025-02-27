package com.collegegrad.suggestme.UserInterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillSelectionForm() {
    var selectedSkill by remember { mutableStateOf<Skill?>(null) }
    var customSkillText by remember { mutableStateOf("") }
    var selectedSkills by remember { mutableStateOf(listOf<Skill>()) }
    var isAddingCustomSkill by remember { mutableStateOf(false) }
    var showSkillDropdown by remember { mutableStateOf(false) }

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
                        mainAxisSpacing = 8,
                        crossAxisSpacing = 8
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
                        mainAxisSpacing = 8,
                        crossAxisSpacing = 8
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
                onClick = { /* Handle form submission */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSkills.isNotEmpty() &&
                        selectedInterests.isNotEmpty() &&
                        selectedEndGoals.isNotEmpty()
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Int = 0,
    crossAxisSpacing: Int = 0,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<Pair<Int, Int>>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Pair<Int, Int>>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        // Measure and place children
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)

            val mainAxisSize = placeable.width
            val crossAxisSize = placeable.height

            if (currentMainAxisSize + mainAxisSize + (if (currentSequence.isEmpty()) 0 else mainAxisSpacing) > constraints.maxWidth) {
                // Create a new sequence
                sequences += currentSequence.toList()
                crossAxisSizes += currentCrossAxisSize
                crossAxisPositions += crossAxisSpace

                crossAxisSpace += currentCrossAxisSize + crossAxisSpacing
                mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

                currentSequence.clear()
                currentMainAxisSize = 0
                currentCrossAxisSize = 0
            }

            currentSequence.add(mainAxisSize to crossAxisSize)
            currentMainAxisSize += mainAxisSize + (if (currentSequence.size > 1) mainAxisSpacing else 0)
            currentCrossAxisSize = max(currentCrossAxisSize, crossAxisSize)

            placeable
        }

        // Add the last sequence
        if (currentSequence.isNotEmpty()) {
            sequences += currentSequence
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace
            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)
        }

        // Set the size of the layout
        layout(
            width = constraints.maxWidth,
            height = max(crossAxisSpace, constraints.minHeight)
        ) {
            // Track which child we have placed
            var childIndex = 0
            sequences.forEachIndexed { i, sequence ->
                // For each sequence in the list, place the children
                var mainAxisPosition = 0
                sequence.forEach { (childMainAxisSize, _) ->
                    // Place the child
                    placeables[childIndex].placeRelative(
                        x = mainAxisPosition,
                        y = crossAxisPositions[i]
                    )

                    mainAxisPosition += childMainAxisSize + mainAxisSpacing
                    childIndex++
                }
            }
        }
    }
}