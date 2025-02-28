package com.collegegrad.suggestme.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Int,
    val title: String,
    val description: String,
    val imageResId: Int,
    val instructor: String,
    val duration: String,
    val level: String,
    val url: String
)