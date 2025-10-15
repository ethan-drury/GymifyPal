package com.example.gymifypal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymifypal.ui.theme.GymifyPalTheme

class DatabaseViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ExerciseDatabaseViewModel(this)
        setContent {
            GymifyPalTheme {
                AllExercisesPreview()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllExercisesPreview() {
    val sampleExercises = listOf(
        Exercise(id = 1, exerciseName = "Push-up", type = "Strength", muscle = "Chest", difficulty = "Easy", week = 1, sets = 3, reps = 12),
        Exercise(id = 2, exerciseName = "Squat", type = "Strength", muscle = "Legs", difficulty = "Medium", week = 1, sets = 4, reps = 10)
    )
    LazyColumn {
        items(sampleExercises) { exercise ->
            Text(exercise.exerciseName, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun AllExercisesScreen(
    viewModel: ExerciseDatabaseViewModel = ExerciseDatabaseViewModel(LocalContext.current)
) {
    val exercises by viewModel.getAllExercises()
        .collectAsState(initial = emptyList())

    LazyColumn {
        items(exercises) { exercise ->
            Text(
                text = exercise.exerciseName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}























