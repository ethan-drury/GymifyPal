package com.example.gymifypal

import android.R.attr.onClick
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymifypal.ui.theme.AppTypography
import com.example.gymifypal.ui.theme.GymifyPalTheme
import kotlin.collections.emptyList

class DatabaseViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ExerciseDatabaseViewModel(this)
        setContent {
            GymifyPalTheme {
                Nav(viewModel)
            }
        }
    }
}

@Preview
@Composable
fun Nav(viewModel: ExerciseDatabaseViewModel= ExerciseDatabaseViewModel(LocalContext.current)) {
    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = "home") {
        composable("home") { DisplayExercises(viewModel=viewModel, navController = navController) }
        composable(
            route = "exercise/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
            EditExerciseScreen(
                viewModel = viewModel,
                exerciseId = exerciseId,
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun DisplayExercises (
    modifier: Modifier=Modifier,
    viewModel: ExerciseDatabaseViewModel= ExerciseDatabaseViewModel(LocalContext.current),
    navController: NavHostController=NavHostController(LocalContext.current)
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val exercises = viewModel.getAllExercises().collectAsState(emptyList()).value
    val selected =
        if (selectedIndex < exercises.size) exercises[selectedIndex].exerciseName else "Select Exercise"

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = modifier.padding(16.dp),
                onClick = { },
                content = { Icon(Icons.Filled.Add, contentDescription = "Add") }
            )
        },

        topBar = { /* Add later */ }
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Dropdown("Exercise: ", exercises.map { it.exerciseName }, selected, modifier = Modifier.padding(16.dp)) {
                selectedIndex = it
            }
            Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                TitleText("Exercise")
//                TitleText("Type")
//                TitleText("Muscle")
//                TitleText("Difficulty")
                TitleText("Week")
//                TitleText("Sets")
//                TitleText("Reps")
            }
            LazyColumn(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                items(exercises) { exercise ->
                    ExerciseRow(exercise) {
                        navController.navigate("exercise/${exercise.id}")
                    }
                }
            }
        }
    }
}


@Composable
fun EditExerciseScreen(
    viewModel: ExerciseDatabaseViewModel,
    exerciseId: Long,
    navigateBack: () -> Unit
) {
    val exercises by viewModel.getAllExercises().collectAsState(emptyList())
    val exercise = exercises.firstOrNull { it.id == exerciseId } ?: return

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Exercise Details", style = AppTypography.titleLarge)
        Text("Name: ${exercise.exerciseName}")
        Text("Type: ${exercise.type}")
        Text("Muscle: ${exercise.muscle}")
        Text("Difficulty: ${exercise.difficulty}")
        Text("Week: ${exercise.week}")
        Text("Sets: ${exercise.sets}")
        Text("Reps: ${exercise.reps}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = navigateBack) {
            Text("Back")
        }
    }
}
@Composable
fun ExerciseRow(exercise: Exercise, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(exercise.exerciseName, modifier = Modifier.weight(1f))
        Text(exercise.week.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
fun RowScope.TitleText(t:String) {
    Text(t, style = AppTypography.titleMedium, modifier = Modifier.weight(1f))
}

@Composable
fun Dropdown(label:String, names: List<String>, selected: String, modifier: Modifier=Modifier, onSelected: (Int) -> Unit) {
    var showDropDownMenu by remember { mutableStateOf(false) }
    Row(modifier) {
        Text(label, style = AppTypography.titleLarge)
        Box(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text(
                    selected, modifier = Modifier.clickable(onClick = { showDropDownMenu = true }),
                    style = AppTypography.titleLarge
                )
                Icon(Icons.Filled.ArrowDropDown,"Select Value",
                    modifier = Modifier.padding(0.dp).clickable(true, onClick = { showDropDownMenu = true }))
            }

            DropdownMenu(
                onDismissRequest = { showDropDownMenu = false },
                expanded = showDropDownMenu
            ) {
                names.forEachIndexed { index, name ->
                    Text(name, modifier = Modifier.fillMaxWidth().clickable(onClick = {
                        onSelected(index)
                        showDropDownMenu = false }).padding(8.dp)
                    )
                }
            }
        }
    }
}





















