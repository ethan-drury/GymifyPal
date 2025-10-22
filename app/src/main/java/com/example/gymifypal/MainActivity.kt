package com.example.gymifypal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gymifypal.ui.theme.GymifyPalTheme
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymifyPalTheme {
                MainButtons()
            }
            MuscleHeatmapPreview()
        }
    }
}
@Composable
fun MainButtons() {
    val context = LocalContext.current

    Column(
    ) {
        Button(onClick = {
            val intent = Intent(context, MuscleMapActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("Click me")
        }
        Button(onClick = {
            val intent = Intent(context, DatabaseViewActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("Click me!!!")
        }
    }
}