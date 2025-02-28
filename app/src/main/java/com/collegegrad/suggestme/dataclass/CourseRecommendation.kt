package com.collegegrad.suggestme.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class CourseRecommendation(
    val name: String,
    val url: String
)