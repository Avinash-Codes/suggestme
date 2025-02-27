package com.collegegrad.suggestme.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.collegegrad.suggestme.BuildConfig
import com.collegegrad.suggestme.dataclass.CourseRecommendation
import com.collegegrad.suggestme.viewmodel.CourseViewModel
import com.collegegrad.suggestme.viewmodel.UserViewModel
import com.google.ai.client.generativeai.GenerativeModel

@Composable
fun ShowCourses(
    userViewModel: UserViewModel,
    courseViewModel: CourseViewModel,
    userId: String,
    onBackPressed: () -> Unit,
    onRetakeQuiz: () -> Unit
) {
    Log.d("ShowCourses", "ShowCourses: userId=$userId")
    val userData by userViewModel.userData.collectAsState()
    val courseRecommendations by courseViewModel.courseRecommendations.collectAsState()
    val isLoadingUser by userViewModel.isLoading
    val isLoadingCourses by courseViewModel.isLoading
    val error by courseViewModel.error

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Initialize the Gemini model
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = BuildConfig.apiKey
        )
    }

    // Load user data when composable is first displayed
    LaunchedEffect(userId) {
        userViewModel.getCurrentUserDetails(userId)
    }

    // Keep track if suggestions have been requested yet
    var suggestionsRequested by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Course Recommendations",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoadingUser || (isLoadingCourses && suggestionsRequested)) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null && suggestionsRequested) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error getting recommendations",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (userData != null) {
                            suggestionsRequested = true
                            userViewModel.getAssessmentHistory(userId) { assessmentResults ->
                                courseViewModel.suggestCourses(userData!!, assessmentResults, generativeModel)
                            }
                        }
                    }) {
                        Text("Try Again")
                    }
                }
            }
        } else if (courseRecommendations.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(courseRecommendations) { course ->
                    CourseCard(
                        course = course,
                        onCourseClick = { uriHandler.openUri(course.url) }
                    )
                }
            }
        } else {
            if (suggestionsRequested) {
                Text(
                    text = "No course recommendations found",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                Text(
                    text = "Click 'Suggest Courses' to get personalized course recommendations based on your profile and assessment results",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Button row at the bottom
        Button(
            onClick = {
                if (userData != null) {
                    suggestionsRequested = true
                    userViewModel.getAssessmentHistory(userId) { assessmentResults ->
                        courseViewModel.suggestCourses(userData!!, assessmentResults, generativeModel)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = userData != null
        ) {
            Text("Suggest Courses")
        }

        Spacer(modifier = Modifier.height(8.dp))

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

@Composable
fun CourseCard(
    course: CourseRecommendation,
    onCourseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onCourseClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = course.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.url,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.OpenInBrowser,
                contentDescription = "Open Course Link",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}