package com.example.gymifypal

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Slider
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

data class MuscleFatigue(
    val name: String,
    @DrawableRes val drawableResId: Int,
    val fatigueLevel: Float = 0f,
    val onClick: () -> Unit = {}
)

@Composable
fun MuscleHeatmap(
    muscleFatigues: List<MuscleFatigue>,
    @DrawableRes baseBodyResId: Int
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.weight(1f)) {
            Image(
                painter = painterResource(id = baseBodyResId),
                contentDescription = "Base Anatomy Map",
                modifier = Modifier.fillMaxSize()
            )

            muscleFatigues.forEach { muscle ->
                val targetColor = interpolateColor(muscle.fatigueLevel)

                val staticColor = targetColor

                Image(
                    painter = painterResource(id = muscle.drawableResId),
                    contentDescription = "${muscle.name} Fatigue: ${muscle.fatigueLevel * 100}%",
                    colorFilter = ColorFilter.tint(staticColor, blendMode = BlendMode.SrcIn),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = muscle.onClick)
                )
            }
        }
    }
}

// Changes from Transparent (fatigue 0.0) to Red (fatigue 1.0).
fun interpolateColor(fatigueLevel: Float): Color {
    val recoveredColor = Color.Red.copy(alpha = 0.0f)

    val fatiguedColor = Color.Red.copy(alpha = 0.8f)

    val t = fatigueLevel.coerceIn(0f, 1f)

    val red = recoveredColor.red * (1f - t) + fatiguedColor.red * t
    val green = recoveredColor.green * (1f - t) + fatiguedColor.green * t
    val blue = recoveredColor.blue * (1f - t) + fatiguedColor.blue * t
    val alpha = recoveredColor.alpha * (1f - t) + fatiguedColor.alpha * t

    return Color(red, green, blue, alpha)
}

@Preview(showBackground = true)
@Composable
fun MuscleHeatmapPreview() {
    var bicepsFatigue by remember { mutableFloatStateOf(0.1f) }

    val muscleList = listOf(

        MuscleFatigue(
            name = "Biceps",
            drawableResId = R.drawable.png_biceps_transparent,
            fatigueLevel = bicepsFatigue,
            onClick = {
                println("Biceps clicked, current fatigue: $bicepsFatigue")
            }
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {

        Slider(
            value = bicepsFatigue,
            onValueChange = { newValue ->
                bicepsFatigue = newValue.coerceIn(0f, 1f)
            },
            steps = 9,
            valueRange = 0f..1f,
            modifier = Modifier.padding(16.dp)
        )
        MuscleHeatmap(
            muscleFatigues = muscleList,
            baseBodyResId = R.drawable.base_anatomy_map
        )
    }
}
