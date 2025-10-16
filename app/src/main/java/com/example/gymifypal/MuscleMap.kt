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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {

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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MuscleHeatmapPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var abdominalsFatigue by remember { mutableFloatStateOf(0.0f) }
    var bicepsFatigue by remember { mutableFloatStateOf(0.0f) }
    var chestFatigue by remember { mutableFloatStateOf(0.0f) }
    var frontDeltoidFatigue by remember { mutableFloatStateOf(0.0f) }
    var sideDeltiodFatigue by remember { mutableFloatStateOf(0.0f) }
    var frontForearmFatigue by remember { mutableFloatStateOf(0.0f) }
    var quadsFatigue by remember { mutableFloatStateOf(0.0f) }

    val muscleList = listOf(

        MuscleFatigue(
            name = "Abdominals",
            drawableResId = R.drawable.abs,
            fatigueLevel = abdominalsFatigue,
            onClick = {
            }
        ),
        MuscleFatigue(
            name = "Biceps",
            drawableResId = R.drawable.biceps,
            fatigueLevel = bicepsFatigue,
            onClick = {
            }
        ),
        MuscleFatigue(
                name = "Chest",
        drawableResId = R.drawable.chest,
        fatigueLevel = chestFatigue,
        onClick = {
        }
    ),
        MuscleFatigue(
            name = "Front Deltoids",
            drawableResId = R.drawable.front_deltoids,
            fatigueLevel = frontDeltoidFatigue,
            onClick = {
            }
        ),
        MuscleFatigue(
            name = "Side Deltoids",
            drawableResId = R.drawable.side_deltoids,
            fatigueLevel = sideDeltiodFatigue,
            onClick = {
            }
        ),
        MuscleFatigue(
            name = "Front Forearms",
            drawableResId = R.drawable.front_forearms,
            fatigueLevel = frontForearmFatigue,
            onClick = {
            }
        ),
        MuscleFatigue(
            name = "quads",
            drawableResId = R.drawable.quads,
            fatigueLevel = quadsFatigue,
            onClick = {
            }
        )
    )

    /*
    ModalNavigationDrawer code adapted from official docs:
    https://developer.android.com/develop/ui/compose/components/drawer
    */
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer title", modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Drawer Item") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.ThumbUp, contentDescription = null)},
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                // ...other drawer items
            }
        },
        content = {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("GymifyPal") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Blue,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Open Nav Menu")
                            }
                        },

                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.Search, contentDescription = "Open Search")
                            }
                        }
                    )
                },

                bottomBar = {
                    BottomAppBar(
                        containerColor = Color.Blue,
                        contentColor = Color.White,
                        actions = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Filled.AddCircle, contentDescription = "Open thingy",
                                        modifier = Modifier.size(200.dp)
                                    )
                                }
                                /*
                       IconButton(onClick = {}) {
                           Icon(Icons.Filled.Menu, contentDescription = "Open thingy")
                       }
                       */

                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    MuscleHeatmap(
                        muscleFatigues = muscleList,
                        baseBodyResId = R.drawable.muscle_map_front
                    )
                }
            }
        }
    )



}
