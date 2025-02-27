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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.collegegrad.suggestme.UserInterface.SkillSelectionForm
import com.collegegrad.suggestme.dataclass.UserData
import com.collegegrad.suggestme.ui.theme.SuggestmeTheme
import com.collegegrad.suggestme.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuggestmeTheme {
                val navController = rememberNavController()
                val userDataViewModel: UserViewModel = viewModel()
                SkillSelectionForm(userDataViewModel)
            }
        }
    }
}

