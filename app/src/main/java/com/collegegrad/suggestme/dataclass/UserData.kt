package com.collegegrad.suggestme.dataclass

import java.util.UUID

data class UserData(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val skillsAndExperience: Pair<String, String>,
    val yourEndGoal: List<String>,
    val createdDate: Long = System.currentTimeMillis()
)
