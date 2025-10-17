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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class MuscleFatigue(
    val name: String,
    @DrawableRes val drawableResId: Int,
    val fatigueLevel: Float = 0f,
    val onClick: () -> Unit = {}
)

@Composable
fun MuscleHeatmap(
    muscleFatigues: List<MuscleFatigue>,
    onFlipButtonClick: () -> Unit,
    flipTrigger: Boolean
) {
    var targetRotation by remember {mutableFloatStateOf(0f)}
    var isCurrentlyFront by remember { mutableStateOf(false) }
    var currentBaseBodyResId by remember { mutableStateOf(R.drawable.muscle_map_front) }

    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 500),
        label = "flip anim"
    )

    val onFlipClick by rememberUpdatedState(onFlipButtonClick)

    LaunchedEffect(flipTrigger) {
        if (rotation % 180 == 0f) {
            val startRotation = targetRotation
            val midRotation = startRotation + 90f
            val endRotation = startRotation + 180f

            targetRotation = midRotation
            delay(250)

            isCurrentlyFront = !isCurrentlyFront
            currentBaseBodyResId = if (isCurrentlyFront)
                R.drawable.muscle_map_front
            else
                R.drawable.muscle_map_back

            targetRotation = endRotation
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {

        Box(modifier = Modifier.weight(1f)) {

            val baseImageModifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
            Image(
                painter = painterResource(id = currentBaseBodyResId),
                contentDescription = "Base Anatomy Map",
                modifier = baseImageModifier
            )

            muscleFatigues.forEach { muscle ->
                val staticColor = interpolateColor(muscle.fatigueLevel)

                Image(
                    painter = painterResource(id = muscle.drawableResId),
                    contentDescription = "${muscle.name} Fatigue: ${muscle.fatigueLevel * 100}%",
                    colorFilter = ColorFilter.tint(staticColor, blendMode = BlendMode.SrcIn),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = muscle.onClick)
                        .graphicsLayer{
                            rotationY = rotation
                            cameraDistance = 8 * density
                        }
                )
            }
            Button(
                onClick = onFlipClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(1.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                )
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Flip")
            }
            Button(
                    onClick = onFlipClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = 1.dp, horizontal = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Gray
            )
            ) {
            Icon(Icons.Filled.Person, contentDescription = "Flip")
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

    var isFrontView by remember { mutableStateOf(true) }
    var flipTrigger by remember { mutableStateOf(false) }

    val onFlipButtonClick = { flipTrigger = !flipTrigger }


    var abdominalFatigue by remember { mutableFloatStateOf(0.0f) }
    var bicepsFatigue by remember { mutableFloatStateOf(0.0f) }
    var chestFatigue by remember { mutableFloatStateOf(0.0f) }
    var frontDeltoidFatigue by remember { mutableFloatStateOf(0.0f) }
    var sideDeltoidFatigue by remember { mutableFloatStateOf(0.0f) }
    var frontForearmFatigue by remember { mutableFloatStateOf(0.0f) }
    var quadsFatigue by remember { mutableFloatStateOf(0.0f) }

    val muscleList = listOf(

        MuscleFatigue(
            name = "Abdominals",
            drawableResId = R.drawable.abs,
            fatigueLevel = abdominalFatigue,
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
            fatigueLevel = sideDeltoidFatigue,
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
                    val baseBodyResId = if (isFrontView) R.drawable.muscle_map_front else R.drawable.muscle_map_back

                    MuscleHeatmap(
                        muscleFatigues = muscleList,
                        onFlipButtonClick = onFlipButtonClick,
                        flipTrigger = flipTrigger
                    )
                }
            }
        }
    )



}
