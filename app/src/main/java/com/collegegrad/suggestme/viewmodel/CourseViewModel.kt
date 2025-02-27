package com.collegegrad.suggestme.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collegegrad.suggestme.dataclass.AssessmentResult
import com.collegegrad.suggestme.dataclass.CourseRecommendation
import com.collegegrad.suggestme.dataclass.UserData
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class CourseViewModel(
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _courseRecommendations = MutableStateFlow<List<CourseRecommendation>>(emptyList())
    val courseRecommendations = _courseRecommendations.asStateFlow()

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun suggestCourses(userData: UserData, assessmentResults: List<AssessmentResult>, generativeModel: GenerativeModel) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val prompt = buildPrompt(userData, assessmentResults)
                Log.d("CourseViewModel", "Sending prompt to Gemini: $prompt")

                val response = generativeModel.generateContent(prompt)
                Log.d("CourseViewModel", "Received response from Gemini")

                val courses = parseGeminiResponse(response)
                _courseRecommendations.value = courses

            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error suggesting courses", e)
                _error.value = "Failed to get course recommendations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildPrompt(userData: UserData, assessmentResults: List<AssessmentResult>): String {
        val latestAssessment = assessmentResults.maxByOrNull { it.timestamp }

        return """
            Based on this user profile and assessment results, suggest 5 relevant courses. Return ONLY a JSON array of course objects with 'name' and 'url' fields.
            
            User Profile:
            - Skills: ${userData.skills}
            - Experience: ${userData.experience}
            - Goals: ${userData.yourEndGoal.joinToString(", ")}
            - Interests: ${userData.interests.joinToString(", ")}
            
            ${latestAssessment?.let {
            """
                Latest Assessment:
                - Score: ${it.score}/${it.totalQuestions}
                - Questions and Answers:
                ${it.questions.joinToString("\n") { q ->
                "  * ${q.question}: ${if (q.isCorrect) "Correct" else "Incorrect"}"
            }}
                """
        } ?: "No assessment results available."}
            
            Return ONLY a JSON array in this format:
            [
              {"name": "Course Name 1", "url": "https://example.com/course1"},
              {"name": "Course Name 2", "url": "https://example.com/course2"},
              ...
            ]
            
            DO NOT include any explanation or additional text outside the JSON array.
        """.trimIndent()
    }

    private fun parseGeminiResponse(response: GenerateContentResponse): List<CourseRecommendation> {
        try {
            val text = response.text?.trim() ?: ""

            // Extract JSON if it's wrapped in markdown code blocks
            val jsonContent = if (text.startsWith("```json") && text.endsWith("```")) {
                text.substring(7, text.length - 3).trim()
            } else if (text.startsWith("[") && text.endsWith("]")) {
                text
            } else {
                // Try to find JSON array within the response
                val startIdx = text.indexOf("[")
                val endIdx = text.lastIndexOf("]")
                if (startIdx >= 0 && endIdx >= 0 && endIdx > startIdx) {
                    text.substring(startIdx, endIdx + 1)
                } else {
                    throw Exception("Could not extract JSON from response")
                }
            }

            val jsonArray = JSONArray(jsonContent)
            val courses = mutableListOf<CourseRecommendation>()

            for (i in 0 until jsonArray.length()) {
                val courseJson = jsonArray.getJSONObject(i)
                val name = courseJson.getString("name")
                val url = courseJson.getString("url")
                courses.add(CourseRecommendation(name = name, url = url))
            }

            return courses
        } catch (e: Exception) {
            Log.e("CourseViewModel", "Error parsing Gemini response", e)
            throw Exception("Failed to parse course recommendations: ${e.message}")
        }
    }
}