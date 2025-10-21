package com.example.gymifypal

import android.R.attr.onClick
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.gymifypal.ui.theme.AppTypography
import com.example.gymifypal.ui.theme.GymifyPalTheme
import kotlinx.serialization.Serializable
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
        composable("add_exercise") { entry ->
            AddExercise(
                viewModel = viewModel,
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExercise(
    viewModel: ExerciseDatabaseViewModel= ExerciseDatabaseViewModel(LocalContext.current),
    navigateBack: () -> Unit={}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Exercise") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            var exerciseName by remember { mutableStateOf("") }
            var type by remember { mutableStateOf("") }
            var difficulty by remember { mutableStateOf("") }
            var week by remember { mutableStateOf("1") }
            var sets by remember { mutableStateOf("3") }
            var reps by remember { mutableStateOf("10") }

            val muscles = MuscleGroup.entries
            var selectedMuscleIndex by remember { mutableIntStateOf(0) }
            val selectedMuscle = muscles[selectedMuscleIndex]

            Dropdown(
                label = "Muscle:",
                muscles.map { it.displayName },
                selectedMuscle.displayName,
                modifier = Modifier.padding(vertical = 8.dp)
            ) { selectedMuscleIndex = it }

            // --- Other input fields ---
            OutlinedTextField(
                value = exerciseName,
                onValueChange = { exerciseName = it },
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = difficulty,
                onValueChange = { difficulty = it },
                label = { Text("Difficulty") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = week,
                onValueChange = { week = it },
                label = { Text("Week") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = sets,
                onValueChange = { sets = it },
                label = { Text("Sets") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it },
                label = { Text("Reps") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (selectedMuscleIndex !in muscles.indices) {
                        Log.e("AddExercise", "No muscle selected")
                        return@Button
                    }

                    val weekInt = week.toIntOrNull() ?: 1
                    val setsInt = sets.toIntOrNull() ?: 3
                    val repsInt = reps.toIntOrNull() ?: 10

                    val muscles = MuscleGroup.entries.toList()
                    val selectedMuscle = muscles[selectedMuscleIndex]

                    viewModel.newExercise(
                        exerciseName = exerciseName,
                        type = type,
                        muscle = selectedMuscle.name,
                        difficulty = difficulty,
                        week = weekInt,
                        sets = setsInt,
                        reps = repsInt
                    )

                    navigateBack()
                }
            ) { Text("Add Exercise") }
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
        if (selectedIndex < exercises.size) exercises[selectedIndex].muscle else "Select Muscle"

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = modifier.padding(16.dp),
                onClick = { navController.navigate("add_exercise") },
                content = { Icon(Icons.Filled.Add, contentDescription = "Add") }
            )
        },

        topBar = { /* Add later */ }
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Dropdown("Muscle: ", exercises.map { it.muscle }, selected, modifier = Modifier.padding(16.dp)) {
                selectedIndex = it
            }
            Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                TitleText("Exercise")
//                TitleText("Type")
                TitleText("Muscle")
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseScreen(
    viewModel: ExerciseDatabaseViewModel,
    exerciseId: Long,
    navigateBack: () -> Unit
) {
    val exercises by viewModel.getAllExercises().collectAsState(emptyList())
    val exercise = exercises.firstOrNull { it.id == exerciseId } ?: return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            var showDialog by remember { mutableStateOf(false) }

            Text("Exercise Details", style = AppTypography.titleLarge)
            Text("Name: ${exercise.exerciseName}")
            Text("Type: ${exercise.type}")
            Text("Muscle: ${exercise.muscle}")
            Text("Difficulty: ${exercise.difficulty}")
            Text("Week: ${exercise.week}")
            Text("Sets: ${exercise.sets}")
            Text("Reps: ${exercise.reps}")
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialog = true }
            ) {
                Text("Delete Exercise")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirm Delete") },
                    text = { Text("Are you sure you want to delete this exercise?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteExercise(id = exercise.id)
                                navigateBack()
                                showDialog = false
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }

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
        Text(exercise.muscle, modifier = Modifier.weight(1f))
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





















