package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorCanvas(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false
) {
    // 1. Infinitely rotating angle
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor_rotation")
    
    // Rotate faster if speaking to simulate high processing states
    val duration = if (isSpeaking) 2500 else 6000
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arc_rotation"
    )

    // 2. Pulsating cyan core glow intensity
    val coreScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 600 else 1500, easing = SineJoinEsing()),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width.coerceAtMost(size.height) / 2 * 0.9f

            // --- Theme Color Tokens ---
            val arcCyan = Color(0xFF00E5FF)
            val arcCyanLight = Color(0xFFA0FBFF)
            val arcGoldDark = Color(0xFFC09000)
            val ironRed = Color(0xFFB01D1D)
            val darkBase = Color(0xFF0B1218)

            // A. Dark Outer Metal Base Shield
            drawCircle(
                color = darkBase,
                radius = radius * 1.05f,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(darkBase, ironRed, Color.Black),
                    center = center,
                    radius = radius * 1.05f
                ),
                radius = radius * 1.05f,
                center = center,
                style = Stroke(width = 8f)
            )

            // B. Outer Glowing Ring
            drawCircle(
                color = arcCyan.copy(alpha = 0.15f),
                radius = radius * 0.95f,
                center = center,
                style = Stroke(width = 24f)
            )
            drawCircle(
                color = arcCyan,
                radius = radius * 0.9f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Rotating Elements
            withTransform({
                rotate(rotationAngle, center)
            }) {
                // C. 12 Golden Metal Coil Segments
                val segmentCount = 12
                for (i in 0 until segmentCount) {
                    val angleDeg = i * (360f / segmentCount)
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val segmentCenter = Offset(
                        (center.x + radius * 0.72f * cos(angleRad)).toFloat(),
                        (center.y + radius * 0.72f * sin(angleRad)).toFloat()
                    )

                    // Drawing metallic gold power cell terminals
                    drawCircle(
                        color = arcGoldDark,
                        radius = radius * 0.08f,
                        center = segmentCenter
                    )
                    drawCircle(
                        color = arcCyanLight,
                        radius = radius * 0.03f,
                        center = segmentCenter
                    )

                    // Connect cells via fine lines
                    drawLine(
                        color = arcCyan.copy(alpha = 0.6f),
                        start = center,
                        end = segmentCenter,
                        strokeWidth = 2f
                    )
                }

                // D. Nanotech Inner Red Triangle Core Holder
                val trianglePath = Path().apply {
                    val sides = 3
                    val triangleRadius = radius * 0.45f
                    for (i in 0 until sides) {
                        val deg = i * (360f / sides) - 90f
                        val rad = Math.toRadians(deg.toDouble())
                        val x = (center.x + triangleRadius * cos(rad)).toFloat()
                        val y = (center.y + triangleRadius * sin(rad)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = ironRed.copy(alpha = 0.85f),
                    style = Stroke(width = 6f)
                )

                // Secondary overlapping reverse triangle
                val secondaryTrianglePath = Path().apply {
                    val sides = 3
                    val triangleRadius = radius * 0.35f
                    for (i in 0 until sides) {
                        val deg = i * (360f / sides) + 90f // inverse offset
                        val rad = Math.toRadians(deg.toDouble())
                        val x = (center.x + triangleRadius * cos(rad)).toFloat()
                        val y = (center.y + triangleRadius * sin(rad)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(
                    path = secondaryTrianglePath,
                    color = arcGoldDark.copy(alpha = 0.7f),
                    style = Stroke(width = 3f)
                )
            }

            // E. Hyper Pulsating Energy Core
            val corePulseRadius = radius * 0.28f * coreScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(arcCyanLight, arcCyan, Color.Transparent),
                    center = center,
                    radius = corePulseRadius * 1.5f
                ),
                radius = corePulseRadius * 1.5f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = corePulseRadius * 0.7f,
                center = center
            )
        }
    }
}

@Composable
fun NeuralFaceCanvas(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_face_loop")
    
    // Wave multipliers based on speaker haptics
    val pulseVelocity = if (isSpeaking) 850 else 2000
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(pulseVelocity, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "neural_wave"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val w = size.width
        val h = size.height
        val scale = w.coerceAtMost(h) / 2 * 0.8f

        val neonCyan = Color(0xFF00E5FF)
        val metallicGold = Color(0xFFFFD700)

        // 1. Draw glowing concentric digital waves (Neural nodes)
        val circlesCount = 6
        for (i in 1..circlesCount) {
            val baseRadius = scale * (i.toFloat() / circlesCount)
            val variance = if (isSpeaking) sin(phaseOffset + i) * 12f else sin(phaseOffset + (i * 0.5f)) * 4f
            val currentRadius = (baseRadius + variance).coerceAtLeast(10f)

            drawCircle(
                color = neonCyan.copy(alpha = (0.05f + (0.06f / i))),
                radius = currentRadius + 8f,
                center = center
            )
            drawCircle(
                color = if (i % 2 == 0) neonCyan.copy(alpha = 0.35f) else metallicGold.copy(alpha = 0.25f),
                radius = currentRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )
        }

        // 2. Draw speaking AI matrices (Horizontal synaptic nodes)
        val nodesCount = 8
        val nodePoints = mutableListOf<Offset>()
        for (i in 0 until nodesCount) {
            val angle = (i * (2 * Math.PI / nodesCount))
            val wave = sin(phaseOffset + i) * (if (isSpeaking) 25f else 8f)
            val r = scale * 0.62f + wave
            val px = (center.x + r * cos(angle)).toFloat()
            val py = (center.y + r * sin(angle)).toFloat()
            nodePoints.add(Offset(px, py))
        }

        // Draw connections representing neural synapse arrays
        for (i in 0 until nodesCount) {
            val start = nodePoints[i]
            val end = nodePoints[(i + 3) % nodesCount] // cross linking
            drawLine(
                color = neonCyan.copy(alpha = 0.2f),
                start = start,
                end = end,
                strokeWidth = 1f
            )

            // Draw synaptic circle nodes
            drawCircle(
                color = neonCyan,
                radius = if (isSpeaking) 7f else 4f,
                center = start
            )
            drawCircle(
                color = Color.White,
                radius = if (isSpeaking) 3f else 2f,
                center = start
            )
        }

        // 3. Draw vertical neural spine
        val spinePointsCount = 5
        for (i in 0 until spinePointsCount) {
            val yOffset = center.y - scale * 0.5f + (scale * (i.toFloat() / (spinePointsCount - 1)))
            val xOffset = center.x + sin(phaseOffset + i) * (if (isSpeaking) 12f else 3f)
            drawCircle(
                color = metallicGold.copy(alpha = 0.7f),
                radius = 5f,
                center = Offset(xOffset, yOffset)
            )
        }
    }
}

// Custom easing for reactive pulsing curves
class SineJoinEsing : Easing {
    override fun transform(fraction: Float): Float {
        return sin(fraction * Math.PI / 2).toFloat()
    }
}
