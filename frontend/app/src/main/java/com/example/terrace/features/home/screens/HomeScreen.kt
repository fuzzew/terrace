package com.example.terrace.features.home.screens

import StarData
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import kotlin.math.sqrt
import kotlin.random.Random
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity

import com.example.terrace.R

import android.util.Log

import androidx.compose.material3.*
import androidx.compose.ui.zIndex
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import com.example.terrace.features.home.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val starCount = 100
    val clusterCount = 10

    var offsetX by remember { mutableStateOf(0f) }

    val starPositions = remember(screenSize) {
        mutableStateListOf<StarData>().apply {
            if (screenSize.width > 100 && screenSize.height > 100) {
                while (size < starCount) {
                    val newPosition = getRandomPosition(screenSize)
                    if (none { it.position.distanceTo(newPosition) < 100 }) {
                        val size = Random.nextInt(10, 60).dp
                        add(
                            StarData(
                                position = newPosition,
                                size = size,
                                rotation = Random.nextFloat() * 360f,
                                twinkleSpeed = Random.nextInt(200, 800),
                                drawableRes = if (Random.nextInt(3) == 0) R.drawable.star_2 else R.drawable.star_1,
                                sizeCategory = classifyStarBySize(size)
                            )
                        )
                    }
                }

                repeat(clusterCount) {
                    val clusterCenter = getRandomPosition(screenSize)
                    val clusterSize = Random.nextInt(5, 15)
                    repeat(clusterSize) {
                        val angle = Random.nextFloat() * 360f
                        val distance = Random.nextInt(90, 200).toFloat()
                        val offsetX = (clusterCenter.x + distance * kotlin.math.cos(angle)).coerceIn(-screenSize.width.toFloat(), screenSize.width.toFloat() * 2)
                        val offsetY = (clusterCenter.y + distance * kotlin.math.sin(angle)).coerceIn(0f, screenSize.height.toFloat())
                        add(
                            StarData(
                                position = Offset(offsetX, offsetY),
                                size = Random.nextInt(10, 40).dp,
                                rotation = Random.nextFloat() * 360f,
                                twinkleSpeed = Random.nextInt(200, 800),
                                drawableRes = if (Random.nextInt(3) == 0) R.drawable.star_2 else R.drawable.star_1,
                                sizeCategory = classifyStarBySize(Random.nextInt(10, 40).dp)
                            )
                        )
                    }
                }
            }
        }
    }

    // Play audio when entering HomeScreen
    val context = LocalContext.current
    DisposableEffect(context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.celestia)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Transparent Top Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Text("Terrace")
                },
                actions = {
                    // Add score to the right
                    Text(
                        text = "Score: 100", // Replace 100 with your dynamic score
                        color = Color.White,
                        modifier = Modifier.padding(end = 16.dp) // Add padding to the right
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.hsv(0f, 0f, 0f, 0.2f),  // Transparent background
                    titleContentColor = Color.White // Title color
                ),
                modifier = Modifier.zIndex(1f) // Ensure TopAppBar stays above other content
            )
            BottomNavBar()
        }

        // Starry Sky Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // Adjust padding to prevent overlap with TopAppBar
                .onGloballyPositioned { coordinates ->
                    screenSize = IntSize(coordinates.size.width, coordinates.size.height)
                    Log.d("HomeScreen", "Screen Size: $screenSize")
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, _, _ ->
                        // Clamp offsetX within the range
                        offsetX = (offsetX + pan.x).coerceIn(-screenSize.width.toFloat(), screenSize.width * 2f)
                        Log.d("HomeScreen", "Dragging: offsetX = $offsetX")
                    }
                }
        )  {
            // Background gradients (Starry Sky)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x0CA506EF), Color(0x1E000000), Color.Black),
                        center = Offset(
                            x = screenSize.width / 2f,
                            y = screenSize.height / 2f
                        ),
                        radius = size.minDimension * 5f
                    ),
                    size = size
                )

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1D0088FF), Color(0x1E050001), Color.Black),
                        center = Offset(
                            x = screenSize.width / 2f,
                            y = 0f
                        ),
                        radius = size.minDimension * 5f
                    ),
                    size = size
                )
            }

            // Different layers for parallax effect
            Box(modifier = Modifier.fillMaxSize()) {
                // Small Stars
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset((offsetX * 0.5).toInt(), 0) } // Slower movement
                ) {
                    starPositions.filter { it.sizeCategory == StarSizeCategory.SMALL }.forEach { star ->
                        renderStar(star, density, twinkleAlpha = Random.nextFloat())
                    }
                }

                // Medium Stars
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset((offsetX * 0.7).toInt(), 0) } // Moderate movement
                ) {
                    starPositions.filter { it.sizeCategory == StarSizeCategory.MEDIUM }.forEach { star ->
                        renderStar(star, density, twinkleAlpha = Random.nextFloat())
                    }
                }

                // Large Stars
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset((offsetX * 0.9).toInt(), 0) } // Fastest movement
                ) {
                    starPositions.filter { it.sizeCategory == StarSizeCategory.LARGE }.forEach { star ->
                        renderStar(star, density, twinkleAlpha = Random.nextFloat())
                    }
                }
            }
        }
    }
}



fun getRandomPosition(screenSize: IntSize): Offset {
    if (screenSize.width < 100 || screenSize.height < 100) return Offset.Zero
    return Offset(
        x = Random.nextInt(-screenSize.width, screenSize.width * 2).toFloat(),
        y = Random.nextInt(50, screenSize.height - 50).toFloat()
    )
}

fun Offset.distanceTo(other: Offset) = sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))

// Classification based on size
enum class StarSizeCategory {
    SMALL, MEDIUM, LARGE
}

fun classifyStarBySize(size: Dp): StarSizeCategory {
    return when {
        size.value < 20 -> StarSizeCategory.SMALL
        size.value in 20f..40f -> StarSizeCategory.MEDIUM
        else -> StarSizeCategory.LARGE
    }
}

@Composable
fun renderStar(star: StarData, density: Density, twinkleAlpha: Float) {
    val starImage: Painter = painterResource(id = star.drawableRes)

    val twinkleAlphaAnimation by animateFloatAsState(
        targetValue = if (Random.nextFloat() < 0.5f) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(star.twinkleSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .offset(
                with(density) { star.position.x.toDp() },
                with(density) { star.position.y.toDp() }
            )
            .size(star.size)
    ) {
        Image(
            painter = starImage,
            contentDescription = null,
            modifier = Modifier
                .size(star.size)
                .graphicsLayer {
                    rotationZ = star.rotation
                    alpha = twinkleAlphaAnimation * 0.6f
                    shadowElevation = 15f
                    scaleX = 1.3f
                    scaleY = 1.3f
                },
            contentScale = ContentScale.Fit
        )

        Image(
            painter = starImage,
            contentDescription = "Twinkling Star",
            modifier = Modifier
                .size(star.size)
                .graphicsLayer {
                    rotationZ = star.rotation
                    alpha = twinkleAlphaAnimation
                },
            contentScale = ContentScale.Fit
        )
    }
}
