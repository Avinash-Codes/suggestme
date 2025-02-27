package com.collegegrad.suggestme.navigation

import kotlinx.serialization.Serializable

interface Screen {

    @Serializable
    data object SkillSelectionForm : Screen

    @Serializable
    data class  ShowCourses(val userId: String) : Screen

}