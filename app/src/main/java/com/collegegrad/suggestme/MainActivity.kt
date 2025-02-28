package com.collegegrad.suggestme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.collegegrad.suggestme.UserInterface.SkillSelectionForm
import com.collegegrad.suggestme.dataclass.Course
import com.collegegrad.suggestme.dataclass.UserData
import com.collegegrad.suggestme.navigation.Screen
import com.collegegrad.suggestme.ui.screens.ShowCourses
import com.collegegrad.suggestme.ui.theme.SuggestmeTheme
import com.collegegrad.suggestme.userinterface.CourseApp
import com.collegegrad.suggestme.viewmodel.CourseViewModel
import com.collegegrad.suggestme.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuggestmeTheme {
                val navController = rememberNavController()
                val userDataViewModel: UserViewModel = viewModel()
                val courseViewModel: CourseViewModel = viewModel()
                NavHost(
                    navController = navController,
                    startDestination = Screen.SkillSelectionForm,
                ) {
                    // Skill Selection Form
                    composable<Screen.SkillSelectionForm> {
                        SkillSelectionForm(userDataViewModel, navController)
                    }
                    // Show Courses Screen

                    composable<Screen.ShowCourses> {
                        val userData = userDataViewModel.userData.value?.id
                        if (userData != null) {
                            ShowCourses(userDataViewModel, courseViewModel,userData, onBackPressed = {
                                navController.popBackStack()
                            }, onRetakeQuiz = {
                                navController.navigate(Screen.SkillSelectionForm)
                            })
                        }
                    }

                    // Show Course Details Screen
                    composable<Screen.ShowCourseDetails> {
                        CourseApp()
                    }
                }
            }
        }
    }
}

