package com.example.gymifypal

import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.IntSize
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Button
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat.startActivity

/*
 * Makes a transparent overlay that uses a bitmap to check the alpha val
 * of a clicked pixel.
 * Its not super accurate yet, but its a start...
 *
 * TODO:
 * fix muscles 'hitboxes' being wider than they should be
 */
@Composable
fun AlphaClickLayer(
    modifier: Modifier,
    muscleFatigues: List<MuscleFatigueMap>,
    rotationY: Float
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val bitmaps = remember(muscleFatigues) {
        muscleFatigues.associate { muscle ->
            try {
                muscle.drawableResId to BitmapFactory.decodeResource(context.resources, muscle.drawableResId)
            } catch (e: Exception) {
                null to null
            }
        }.filterValues { it != null }.mapKeys { it.key as Int } as Map<Int, android.graphics.Bitmap>
    }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { containerSize = it }
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 8 * density
            }
            .pointerInput(muscleFatigues, containerSize, rotationY) {
                detectTapGestures { offset ->
                    // Ignore clicks when the view is flippin
                    if (rotationY % 180f != 0f) return@detectTapGestures

                    // Ignore clicks if container not measured
                    if (containerSize.width == 0 || containerSize.height == 0) return@detectTapGestures

                    val containerWidth = containerSize.width.toFloat()
                    val containerHeight = containerSize.height.toFloat()

                    //Iterate through each muscle layer until reaches a non transparent layer
                    val clickedMuscle = muscleFatigues.find { muscle ->
                        val bitmap = bitmaps[muscle.drawableResId] ?: return@find false

                        val scaleX = bitmap.width.toFloat() / containerWidth
                        val scaleY = bitmap.height.toFloat() / containerHeight

                        val x = (offset.x * scaleX).toInt().coerceIn(0, bitmap.width - 1)
                        val y = (offset.y * scaleY).toInt().coerceIn(0, bitmap.height - 1)

                        // Check the pixel alpha
                        val pixel = bitmap.getPixel(x, y)
                        val alpha = android.graphics.Color.alpha(pixel)

                        alpha > 1// true for if not transparent layer
                    }

                    if (clickedMuscle != null) {
                        scope.launch {
                            clickedMuscle.onClick(clickedMuscle.name)
                        }
                    }
                }
            }
    )
}

data class MuscleFatigueMap(
    val name: String,
    @DrawableRes val drawableResId: Int,
    val fatigueLevel: Float = 0f,
    val onClick: (muscleName: String) -> Unit = {}
)

@Composable
fun MuscleHeatmap(
    muscleFatigues: List<MuscleFatigueMap>,
    onFlipButtonClick: () -> Unit,
    onBodyViewChange: (Boolean) -> Unit,
    flipTrigger: Boolean,
    onIsFlipping: (Boolean) -> Unit
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
    val onBodyViewChangeUpdated by rememberUpdatedState(onBodyViewChange)
    val onIsFlippingUpdated by rememberUpdatedState(onIsFlipping)

    LaunchedEffect(flipTrigger) {

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

        onBodyViewChangeUpdated(isCurrentlyFront)

        targetRotation = endRotation

        delay(400)
        onIsFlippingUpdated(false)
    }

    val density = LocalDensity.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
    ) {

        Box(modifier = Modifier.weight(1f)) {

            val baseImageModifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    //cameraDistance = 8 * density
                    cameraDistance = 8f
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
                        .graphicsLayer{
                            rotationY = rotation
                            //cameraDistance = 8 * density
                            cameraDistance = 8f
                        }
                )
            }

            AlphaClickLayer(
                modifier = Modifier.fillMaxSize(),
                muscleFatigues = muscleFatigues,
                rotationY = rotation
            )

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

    val snackbarHostState = remember { SnackbarHostState() }

    var isFrontView by remember { mutableStateOf(true) }
    var flipTrigger by remember { mutableStateOf(false) }
    var isFlipping by remember { mutableStateOf(false) }

    var showAiSuggestion by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf("") }
    var isLoadingAi by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    /*
    modal bottom sheet code adapted from official docs
    https://developer.android.com/develop/ui/compose/components/bottom-sheets
     */
    val sheetState = rememberModalBottomSheetState()
    //val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    /* causing crashes.........
    val uniDao = remember { UniDatabase.getDatabase(context).UniDao() }
    val fatiguedb = remember { FatigueDatabaseViewModel(context) }
*/
    var isMuscleEditMode by remember { mutableStateOf(false) }
    var selectedMuscleName by remember { mutableStateOf<String?>(null) }
    val sheetStateFatigue = rememberModalBottomSheetState()
    var showFatigueSlider by remember { mutableStateOf(false) }

    //Initiate Fatigue levels
    val muscleFatigueStates = remember {
        mutableStateMapOf(
            "Abdominal" to mutableStateOf(0.0f),
            "Biceps" to mutableStateOf(0.0f),
            "Chest" to mutableStateOf(0.0f),
            "Front Deltoid" to mutableStateOf(0.0f),
            "Side Deltoids" to mutableStateOf(0.0f),
            "Front Forearms" to mutableStateOf(0.0f),
            "Quadriceps" to mutableStateOf(0.0f),
            "Calves" to mutableStateOf(0.0f),
            "Glutes" to mutableStateOf(0.0f),
            "Hamstrings" to mutableStateOf(0.0f),
            "Rear Deltoids" to mutableStateOf(0.0f),
            "Lats" to mutableStateOf(0.0f),
            "Rear Traps" to mutableStateOf(0.0f),
            "Triceps" to mutableStateOf(0.0f)
        )
    }

    val selectedMuscleFatigue: Float = selectedMuscleName?.let { muscleName ->
        muscleFatigueStates[muscleName]?.value
    } ?: 0f

    val onEditButtonClick: () -> Unit = {
        isMuscleEditMode = !isMuscleEditMode
        scope.launch {
            if (isMuscleEditMode) {
                snackbarHostState.showSnackbar(
                    message = "Edit Mode: Choose a muscle to set its fatigue level."
                )
            } else {
                showFatigueSlider = false
                selectedMuscleName = null
                snackbarHostState.showSnackbar(
                    message = "Edit Mode Disabled."
                )
            }
        }
    }


    val onFlipButtonClick = { flipTrigger = !flipTrigger }

    val onBodyViewChange: (Boolean) -> Unit = { newIsFrontView ->
        isFrontView = newIsFrontView
    }

    fun geminiQuery() {
        scope.launch {
            isLoadingAi = true
            val model = GenerativeModel(
                modelName = "gemini-2.5-flash-lite",
                apiKey = BuildConfig.apiKey
            )
            val response = model.generateContent("""
                Note: these are the muscle areas
                Biceps
                Chest 
                FrontDeltoid
                SideDeltoid
                FrontForearm
                Quadriceps
                Calves
                Glutes
                Hamstring
                Rear Deltoid
                Latissimus (Lats)
                Rear Traps
                Triceps
                
                Say this what area of my body should Only display one. Randomize it every time
                At the end say how many times in a week should you train this muscle 
                So say :Muscle, You should Train this <your value> times a week <Base it of active gym goers>
                No Unneeded comments or anything go straight to the point
                """)
            aiResponse = response.text ?: "No response"
            isLoadingAi = false
        }
    }
    val onMuscleClick: (String) -> Unit = { muscleName ->
        if (isMuscleEditMode) {
            selectedMuscleName = muscleName
            showFatigueSlider = true
        } else {
            val intent = Intent(context, DatabaseViewActivity::class.java)
            intent.putExtra("selectedMuscle", muscleName)
            context.startActivity(intent)
        }
    }


    //Ties drawables to names
    val frontMuscleList = listOf(
        MuscleFatigueMap(name = "Abdominal", drawableResId = R.drawable.abs, fatigueLevel = muscleFatigueStates["Abdominal"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Biceps", drawableResId = R.drawable.biceps, fatigueLevel = muscleFatigueStates["Biceps"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Chest", drawableResId = R.drawable.chest, fatigueLevel = muscleFatigueStates["Chest"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Front Deltoid", drawableResId = R.drawable.front_deltoids, fatigueLevel = muscleFatigueStates["Front Deltoid"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Side Deltoids", drawableResId = R.drawable.side_deltoids, fatigueLevel = muscleFatigueStates["Side Deltoids"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Front Forearms", drawableResId = R.drawable.front_forearms, fatigueLevel = muscleFatigueStates["Front Forearms"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Quadriceps", drawableResId = R.drawable.quads, fatigueLevel = muscleFatigueStates["Quadriceps"]?.value ?: 0f, onClick = onMuscleClick)
    )

    val backMuscleList = listOf(
        MuscleFatigueMap(name = "Calves", drawableResId = R.drawable.calves, fatigueLevel = muscleFatigueStates["Calves"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Glutes", drawableResId = R.drawable.glutes, fatigueLevel = muscleFatigueStates["Glutes"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Hamstrings", drawableResId = R.drawable.hamstrings, fatigueLevel = muscleFatigueStates["Hamstrings"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Rear Deltoids", drawableResId = R.drawable.rear_deltoids, fatigueLevel = muscleFatigueStates["Rear Deltoids"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Lats", drawableResId = R.drawable.lats, fatigueLevel = muscleFatigueStates["Lats"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Rear Traps", drawableResId = R.drawable.rear_traps, fatigueLevel = muscleFatigueStates["Rear Traps"]?.value ?: 0f, onClick = onMuscleClick),
        MuscleFatigueMap(name = "Triceps", drawableResId = R.drawable.triceps, fatigueLevel = muscleFatigueStates["Triceps"]?.value ?: 0f, onClick = onMuscleClick)
    )

    val currentMuscleList = if (isFrontView) frontMuscleList else backMuscleList

    /*
    ModalNavigationDrawer code adapted from official docs:
    https://developer.android.com/develop/ui/compose/components/drawer
    */
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Text("Activity list", modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Age Comparisons") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        context.startActivity(
                            Intent(context, AgeComparisonActivity::class.java)
                        )
                    },
                    icon = { Icon(Icons.Filled.ThumbUp, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                // ...other drawer items
            }
        },
        content = {
            Scaffold(
                snackbarHost = {SnackbarHost(snackbarHostState)},
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
                            IconButton(onClick = {
                                if (!showAiSuggestion) {
                                    showAiSuggestion = true
                                    if (!isLoadingAi && aiResponse.isEmpty()) geminiQuery()
                                } else {
                                    showAiSuggestion = false
                                    aiResponse = ""
                                }
                            }) {
                                Icon(Icons.Filled.Info, contentDescription = "AI Suggestion")
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
                                IconButton(onClick = {
                                    showBottomSheet = true
                                }) {
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
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (showAiSuggestion) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(text = if (isLoadingAi) "Is Loading AI..." else aiResponse)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { if (!isLoadingAi) geminiQuery() },
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh AI")

                                    }
                                    TextButton(onClick = { showAiSuggestion = false;}) {
                                        Text("Hide AI Idea")
                                    }
                                }
                            }
                        }
                    }
                    /*
                    Adapted from official docs
                     */
                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showBottomSheet = false
                            },
                            sheetState = sheetState
                        ) {
                            // Sheet content

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {
                                        // Open database screen
                                        val intent = Intent(context, DatabaseViewActivity::class.java)
                                        context.startActivity(intent)
                                        showBottomSheet = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                ) {
                                    Text("Database")
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

                        MuscleHeatmap(
                            muscleFatigues = currentMuscleList,
                            onFlipButtonClick = onFlipButtonClick,
                            onBodyViewChange = onBodyViewChange,
                            flipTrigger = flipTrigger,
                            onIsFlipping =  {newValue -> isFlipping = newValue}
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .background(color = Color.Transparent),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = onEditButtonClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Gray
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit Fatigue levels manually"
                                )
                            }
                            Row {

                                Button(
                                    onClick = {
                                        isFlipping = true
                                        onFlipButtonClick()
                                    },
                                    enabled = !isFlipping,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Gray
                                    )

                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Flip")
                                }

                            }
                        }

                        val baseBodyResId =
                            if (isFrontView) R.drawable.muscle_map_front else R.drawable.muscle_map_back
                        if (showFatigueSlider && selectedMuscleName != null) {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    showFatigueSlider = false
                                },
                                sheetState = sheetStateFatigue
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Set Fatigue for ${selectedMuscleName}",
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Text("Fatigue Level: ${(selectedMuscleFatigue * 100).toInt()}%")

                                    Slider(
                                        value = selectedMuscleFatigue,
                                        onValueChange = { newValue ->
                                            selectedMuscleName?.let { name ->
                                                muscleFatigueStates[name]?.value = newValue
                                            }
                                        },
                                        steps = 9,
                                        valueRange = 0f..1f,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )

                                    Button(
                                        onClick = {
                                            showFatigueSlider = false
                                        }
                                    ) {
                                        Text("Done")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
