package com.example.gymifypal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymifypal.ui.theme.GymifyPalTheme
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class AgeComparisonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymifyPalTheme {
                AgeComparisonScreen()
            }
        }
    }
}
//Compares Different Values to Give a Goal for Age Training
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeComparisonScreen() {
    val scope = rememberCoroutineScope()
    var exercise by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf("") }

    //Gemini AI Response
    fun queryGemini() {
        if (exercise.isBlank() || age.isBlank() || weight.isBlank() || sex.isBlank()) {
            aiResponse = "Enter both exercise, age, weight and sex first."
            return
        }
        scope.launch {
            isLoading = true
            val model = GenerativeModel(
                modelName = "gemini-2.5-flash-lite",
                apiKey = BuildConfig.apiKey
            )

            val prompt = """
                
                Entered Input:
                - Exercise: $exercise
                - Age: $age
                - Weight $weight
                - sex $sex
                
                Return 5 short lines ONLY, in this exact order and format all weight should be in KG:
                First Line Say: You Should Aim For This.
                Training Weight : (suggested working weight)
                Reps: (rep range or fixed reps) × (sets) (change if it a plank say time instead then say seconds)
                Intensity: (%1RM)
                Max Weight : (suggested working weight or 1RM) 1 Rep Max. 
                Future Goal: (a realistic goal to reach for this exercise i.e weight to build up too after years of training)
                
                If the exercise is similar to a plank or moving like star jumps, burpees  e.t.c. Return this instead:
                First Line Say: You Should Aim For This.
                Time :
                Future Time Goal : 
                Reps : (Only if applicable)

                Notes:
                - Men should Be Higher for all weights.
                - These are Active people using this, so give higher suggested weight (Gym People).
                - If the exercise is bodyweight, base it of entered weight.
                - Adjust the reps and weight for age when needed.
                - No extra Weird formating or unneeded comments.
                
            """
            val res = model.generateContent(prompt)
            aiResponse = res.text.orEmpty().ifEmpty { "No response" }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            val context = LocalContext.current
            CenterAlignedTopAppBar(
                title = { Text("Age Comparisons") },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            OutlinedTextField(
                value = exercise,
                onValueChange = { exercise = it },
                label = { Text("Exercise (Bench Press)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = sex,
                onValueChange = { sex = it },
                label = { Text("Sex : M or F") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp)
            )
                FloatingActionButton(
                    onClick = { if (!isLoading) queryGemini() }

                ) { Text(text = "AI") }

            Spacer(Modifier.height(24.dp))

            if (isLoading || aiResponse.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isLoading) "AI Loading..." else aiResponse,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
