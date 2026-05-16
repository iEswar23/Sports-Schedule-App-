package com.sports.assessment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sports.assessment.presentation.schedule.ScheduleScreen
import com.sports.assessment.ui.theme.AssessmentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AssessmentTheme {
                ScheduleScreen()
            }
        }
    }
}
