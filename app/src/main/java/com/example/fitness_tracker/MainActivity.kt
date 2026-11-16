package com.example.fitness_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fitness_tracker.ui.app.FitnessApp
import com.example.fitness_tracker.ui.theme.Fitness_TrackerTheme

// Android entry point.
// Navigation, state and persistence live in their own layers.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { Fitness_TrackerTheme { FitnessApp() } }
    }
}