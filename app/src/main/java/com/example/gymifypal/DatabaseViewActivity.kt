package com.example.gymifypal

import android.app.Activity
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymifypal.ui.theme.AppTypography
import com.example.gymifypal.ui.theme.GymifyPalTheme
import kotlin.collections.emptyList
import androidx.compose.runtime.rememberCoroutineScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Favorite
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.PlayArrow
import androidx.core.net.toUri


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
    var exerciseName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var week by remember { mutableStateOf("1") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }

    val scope = rememberCoroutineScope()
    var showAiSuggestion by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf("") }
    var isLoadingAi by remember { mutableStateOf(false) }

    val muscles = MuscleGroup.entries
    var selectedMuscleIndex by remember { mutableIntStateOf(0) }
    val selectedMuscle = muscles[selectedMuscleIndex]
    val context = LocalContext.current


    fun geminiSuggestion() {
        scope.launch {
            isLoadingAi = true
            val model = GenerativeModel(
                modelName = "gemini-2.5-flash-lite",
                apiKey = BuildConfig.apiKey
            )

            val enteredSets = sets.ifBlank { "None entered Make up One" }
            val enteredReps = reps.ifBlank { "None entered Make up One" }
            val enteredType = type.ifBlank { "None entered Make up One" }
            val enteredDiff = difficulty.ifBlank { "Beginner" }

            val prompt =
                "Suggest exactly ONE exercise for ${selectedMuscle.displayName}. Each time choose a random different one don't show unneeded information\n " +
                        "don't add weird Spacing, **, Quotes, :, ; e.c.t\n"+
                        "If entered do Type: $enteredType, For difficulty (Beginner, Intermediate Or Advance) change the suggested weight or time or intensity. Entered difficulty: $enteredDiff.\n" +
                        "If it makes sense, format like: $enteredSets sets × $enteredReps reps\n" +
                        "For the suggested Exercise, base it of the Difficulty entered. If its to hard don't display for beginners and if its to easy don't display for advanced.\n" +
                        "Return a very short single paragraph answer: Exercise Name | Type | Difficulty | Reps. Suggest if applicable targeted max rep/time/intensity if applicable. Brief explanation on the muscles that this exercise targets."

            val response = model.generateContent(prompt)
            aiResponse = response.text ?: "No response"
            isLoadingAi = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Exercise") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // YouTube search for exercise Name fallback to Muscle i.e Abdominal
                FloatingActionButton(
                    onClick = {
                        val baseExercise = exerciseName.ifBlank { "${selectedMuscle.displayName} exercises" }
                        val query = "How to do $baseExercise"
                        val url = "https://www.youtube.com/results?search_query=" + Uri.encode(query)

                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "YouTube search")
                }
                //Gemini Prompt
                FloatingActionButton(
                    onClick = {
                        if (!showAiSuggestion) {
                            showAiSuggestion = true
                            if (!isLoadingAi) geminiSuggestion()
                        } else if (!isLoadingAi) {
                            geminiSuggestion()
                        }
                    }
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = "AI Suggestion")
                }
            }
        }
    )    { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (showAiSuggestion) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = if (isLoadingAi) "Loading AI suggestion..." else aiResponse)
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayExercises(
    modifier: Modifier = Modifier,
    viewModel: ExerciseDatabaseViewModel = ExerciseDatabaseViewModel(LocalContext.current),
    navController: NavHostController = NavHostController(LocalContext.current),
    navigateBack: () -> Unit={}
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val exercises = viewModel.getAllExercises().collectAsState(emptyList()).value
    val muscleList = listOf("All") + exercises.map { it.muscle }.distinct().sorted()
    val selected = muscleList.getOrNull(selectedIndex) ?: "All"
    val filteredExercises = if (selected == "All") exercises else exercises.filter { it.muscle == selected }
    var weekSortedAscending by remember { mutableStateOf(true) }

    val displayedExercises = remember(filteredExercises, weekSortedAscending) {
        if (weekSortedAscending) filteredExercises.sortedBy { it.week }
        else filteredExercises.sortedByDescending { it.week }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = modifier.padding(16.dp),
                onClick = { navController.navigate("add_exercise") },
                content = { Icon(Icons.Filled.Add, contentDescription = "Add") }
            )
        },
        topBar = {
            val context = LocalContext.current
            CenterAlignedTopAppBar(
                title = { Text("Exercise List") },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, MuscleMapActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Dropdown(
                label = "Muscle: ",
                muscleList,
                selected = selected,
                modifier = Modifier.padding(16.dp)
            ) {
                selectedIndex = it
            }

            Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                TitleText("Exercise")
                TitleText("Muscle")
                TitleText("Week") {
                    weekSortedAscending = !weekSortedAscending
                }
                TitleText("Sets")
                TitleText("Reps")
            }


            LazyColumn(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                items(displayedExercises) { exercise ->
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
        Text(exercise.sets.toString(), modifier = Modifier.weight(1f))
        Text(exercise.reps.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
fun RowScope.TitleText(
    t: String,
    onClick: (() -> Unit)? = null
) {
    Text(
        text = t,
        style = AppTypography.titleMedium,
        modifier = Modifier
            .weight(1f)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    )
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





















