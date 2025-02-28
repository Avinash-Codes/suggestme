package com.collegegrad.suggestme.dataclass

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