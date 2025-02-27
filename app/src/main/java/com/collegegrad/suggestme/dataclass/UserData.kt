package com.collegegrad.suggestme.dataclass

import com.collegegrad.suggestme.UserInterface.Interest
import java.util.UUID

data class UserData(
    val id: String = "",
    val name: String = "",
    val skills: String = "",  // Changed from Pair to String
    val experience: String = "", // Separate field if needed
    val yourEndGoal: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val createdDate: Long = System.currentTimeMillis()
)