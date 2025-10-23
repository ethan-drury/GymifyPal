package com.example.gymifypal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge


/*
This program was created by
Robert David 23009414
Ethan Drury 23006382
Michael Strydom 23005228
*/

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuscleHeatmapPreview()
        }
    }
}

