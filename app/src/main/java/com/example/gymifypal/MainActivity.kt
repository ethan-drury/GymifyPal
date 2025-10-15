package com.example.gymifypal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntRect
import com.example.gymifypal.ui.theme.GymifyPalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymifyPalTheme {
                MainButtons()
            }
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