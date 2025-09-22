package com.mycompany.aviatorgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PlaneAnimation(
    multiplier: Float,
    isCrashed: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")

    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_offset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw clouds in background
        drawCloud(Offset(cloudOffset, centerY - 50), 30f, Color.White.copy(alpha = 0.1f))
        drawCloud(Offset(cloudOffset - 300, centerY + 50), 40f, Color.White.copy(alpha = 0.1f))
        drawCloud(Offset(cloudOffset - 600, centerY), 35f, Color.White.copy(alpha = 0.1f))

        // Calculate plane position based on multiplier
        val progress = (multiplier - 1f) / 10f // Normalize to 0-1 for first 10x
        val planeX = centerX + progress * 300
        val planeY = centerY - progress * 150
        val rotation = if (isCrashed) 45f else -15f

        // Draw contrail
        if (!isCrashed) {
            drawContrail(
                start = Offset(centerX - 50, centerY + 20),
                end = Offset(planeX - 30, planeY + 10),
                color = Color.White.copy(alpha = 0.3f)
            )
        }

        // Draw plane
        translate(planeX, planeY) {
            rotate(rotation) {
                drawPlane(Color.White, isCrashed)
            }
        }

        // Draw crash effect
        if (isCrashed) {
            drawCrashEffect(Offset(planeX, planeY))
        }
    }
}

fun DrawScope.drawPlane(color: Color, isCrashed: Boolean) {
    val planeColor = if (isCrashed) Color.Red else color

    // Fuselage
    drawRect(
        color = planeColor,
        topLeft = Offset(-40f, -5f),
        size = Size(80f, 10f)
    )

    // Wings
    drawRect(
        color = planeColor,
        topLeft = Offset(-25f, -20f),
        size = Size(50f, 40f)
    )

    // Tail
    val tailPath = Path().apply {
        moveTo(30f, -5f)
        lineTo(45f, -15f)
        lineTo(45f, 5f)
        lineTo(30f, 5f)
        close()
    }
    drawPath(tailPath, planeColor)
}

fun DrawScope.drawCloud(offset: Offset, radius: Float, color: Color) {
    drawCircle(color, radius, offset)
    drawCircle(color, radius * 0.8f, offset + Offset(-radius * 0.5f, 0f))
    drawCircle(color, radius * 0.8f, offset + Offset(radius * 0.5f, 0f))
    drawCircle(color, radius * 0.6f, offset + Offset(0f, -radius * 0.3f))
}

fun DrawScope.drawContrail(start: Offset, end: Offset, color: Color) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 4f
    )
}

fun DrawScope.drawCrashEffect(center: Offset) {
    for (i in 0..8) {
        val angle = (i * 40f) * Math.PI / 180
        val length = 30f + (i % 2) * 10f
        drawLine(
            Color.Red,
            center,
            center + Offset(
                (cos(angle) * length).toFloat(),
                (sin(angle) * length).toFloat()
            ),
            strokeWidth = 3f
        )
    }
}