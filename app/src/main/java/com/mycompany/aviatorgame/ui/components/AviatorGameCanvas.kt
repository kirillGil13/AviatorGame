package com.mycompany.aviatorgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun AviatorGameCanvas(
    modifier: Modifier = Modifier,
    multiplier: Float,
    isPlaying: Boolean,
    isCrashed: Boolean
) {
    val textMeasurer = rememberTextMeasurer()

    // Анимация фоновых элементов
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val starAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stars"
    )

    val gridAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Рисуем сетку на фоне
            drawGrid(gridAnimation, width, height)

            // Рисуем звезды на фоне
            drawStars(starAnimation, width, height)

            // Рисуем шкалу множителей слева
            drawMultiplierScale(textMeasurer, height)

            // Рисуем траекторию полета
            if (isPlaying || isCrashed) {
                val progress = (multiplier - 1f).coerceIn(0f, 99f) / 99f
                drawFlightPath(width, height, progress, isCrashed)

                // Рисуем самолет
                if (!isCrashed || progress < 0.98f) {
                    drawAirplane(width, height, progress, isCrashed)
                }
            }

            // Рисуем эффект краша
            if (isCrashed) {
                drawCrashEffect(width, height, (multiplier - 1f) / 99f)
            }
        }
    }
}

fun DrawScope.drawGrid(animation: Float, width: Float, height: Float) {
    val gridSize = 50f
    val lineColor = Color(0xFF1a2547).copy(alpha = 0.3f)

    // Вертикальные линии
    var x = animation % gridSize
    while (x < width) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += gridSize
    }

    // Горизонтальные линии
    var y = animation % gridSize
    while (y < height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
        y += gridSize
    }
}

fun DrawScope.drawStars(animation: Float, width: Float, height: Float) {
    val starPositions = listOf(
        Offset(width * 0.1f, height * 0.2f),
        Offset(width * 0.3f, height * 0.1f),
        Offset(width * 0.5f, height * 0.15f),
        Offset(width * 0.7f, height * 0.25f),
        Offset(width * 0.9f, height * 0.1f),
        Offset(width * 0.2f, height * 0.35f),
        Offset(width * 0.8f, height * 0.3f),
        Offset(width * 0.6f, height * 0.05f),
    )

    starPositions.forEachIndexed { index, position ->
        val alpha = ((sin(animation * 6.28f + index) + 1f) / 2f * 0.5f + 0.2f)
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 2f,
            center = position
        )

        // Свечение
        drawCircle(
            color = Color(0xFF00d4ff).copy(alpha = alpha * 0.3f),
            radius = 4f,
            center = position
        )
    }
}

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawMultiplierScale(textMeasurer: TextMeasurer, height: Float) {
    val multipliers = listOf(100f, 50f, 20f, 10f, 5f, 3f, 2f, 1.5f, 1.2f, 1f)
    val scaleX = 50f

    multipliers.forEach { mult ->
        val y = height - (height * 0.8f * ((mult - 1f) / 99f)) - height * 0.1f

        // Линия
        drawLine(
            color = Color(0xFF3a4560),
            start = Offset(scaleX - 10f, y),
            end = Offset(scaleX + 10f, y),
            strokeWidth = 1f
        )

        // Текст
        val textLayoutResult = textMeasurer.measure(
            text = AnnotatedString("${mult}x"),
            style = TextStyle(
                color = Color(0xFF6a7490),
                fontSize = 10.sp
            )
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(scaleX - 35f, y - textLayoutResult.size.height / 2)
        )
    }
}

fun DrawScope.drawFlightPath(width: Float, height: Float, progress: Float, isCrashed: Boolean) {
    val path = Path()
    val startX = width * 0.1f
    val startY = height * 0.85f

    path.moveTo(startX, startY)

    // Рисуем параболическую траекторию
    val steps = 100
    val currentSteps = (steps * progress).toInt()

    for (i in 1..currentSteps) {
        val t = i.toFloat() / steps
        val x = startX + (width * 0.8f * t)
        // Парабола: y = ax² + bx + c (перевернутая)
        val y = startY - (height * 0.7f * t * (2 - t * 0.5f))
        path.lineTo(x, y)
    }

    // Основная линия траектории
    drawPath(
        path = path,
        color = if (isCrashed) Color(0xFFff4757) else Color(0xFF00ff88),
        style = Stroke(
            width = 3f,
            cap = StrokeCap.Round
        )
    )

    // Свечение траектории
    drawPath(
        path = path,
        color = if (isCrashed) Color(0xFFff4757).copy(alpha = 0.3f) else Color(0xFF00ff88).copy(alpha = 0.3f),
        style = Stroke(
            width = 8f,
            cap = StrokeCap.Round
        )
    )

    // Эффект следа
    if (!isCrashed && progress > 0.05f) {
        val trailPath = Path()
        val trailStart = max(0, currentSteps - 20)

        for (i in trailStart until currentSteps) {
            val t = i.toFloat() / steps
            val x = startX + (width * 0.8f * t)
            val y = startY - (height * 0.7f * t * (2 - t * 0.5f))

            if (i == trailStart) {
                trailPath.moveTo(x, y)
            } else {
                trailPath.lineTo(x, y)
            }
        }

        drawPath(
            path = trailPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00d4ff).copy(alpha = 0.5f)
                )
            ),
            style = Stroke(
                width = 15f,
                cap = StrokeCap.Round
            )
        )
    }
}

fun DrawScope.drawAirplane(width: Float, height: Float, progress: Float, isCrashed: Boolean) {
    val startX = width * 0.1f
    val startY = height * 0.85f

    // Позиция самолета на параболе
    val planeX = startX + (width * 0.8f * progress)
    val planeY = startY - (height * 0.7f * progress * (2 - progress * 0.5f))

    // Угол наклона самолета (касательная к параболе)
    val angle = atan(height * 0.7f * (2 - progress) / (width * 0.8f)) * 180 / PI

    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.translate(planeX, planeY)
        canvas.rotate(angle.toFloat())

        // Рисуем стилизованный самолет
        val planeSize = 40f

        // Тело самолета
        drawPath(
            path = Path().apply {
                moveTo(-planeSize / 2, 0f)
                lineTo(planeSize / 2, -2f)
                lineTo(planeSize / 2, 2f)
                lineTo(-planeSize / 2, 3f)
                close()
            },
            color = if (isCrashed) Color(0xFFff4757) else Color.White
        )

        // Крылья
        drawPath(
            path = Path().apply {
                moveTo(-planeSize / 4, 0f)
                lineTo(0f, -planeSize / 3)
                lineTo(planeSize / 4, 0f)
                lineTo(0f, planeSize / 4)
                close()
            },
            color = if (isCrashed) Color(0xFFff4757) else Color(0xFF00d4ff)
        )

        // Хвост
        drawPath(
            path = Path().apply {
                moveTo(-planeSize / 2, 0f)
                lineTo(-planeSize / 2 - 5, -planeSize / 4)
                lineTo(-planeSize / 2 - 3, 0f)
                close()
            },
            color = if (isCrashed) Color(0xFFff4757) else Color(0xFF00ff88)
        )

        // Свечение самолета
        if (!isCrashed) {
            drawCircle(
                color = Color(0xFF00d4ff).copy(alpha = 0.3f),
                radius = planeSize / 2,
                center = Offset(0f, 0f)
            )
        }

        canvas.restore()
    }
}

fun DrawScope.drawCrashEffect(width: Float, height: Float, progress: Float) {
    val startX = width * 0.1f
    val startY = height * 0.85f

    val crashX = startX + (width * 0.8f * progress)
    val crashY = startY - (height * 0.7f * progress * (2 - progress * 0.5f))

    // Эффект взрыва
    for (i in 0..8) {
        val angle = (i * 40f) * PI / 180
        val length = 30f + (i % 2) * 20f

        drawLine(
            color = Color(0xFFff4757),
            start = Offset(crashX, crashY),
            end = Offset(
                crashX + (cos(angle) * length).toFloat(),
                crashY + (sin(angle) * length).toFloat()
            ),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }

    // Круг взрыва
    drawCircle(
        color = Color(0xFFff4757).copy(alpha = 0.3f),
        radius = 40f,
        center = Offset(crashX, crashY)
    )

    drawCircle(
        color = Color(0xFFffa502).copy(alpha = 0.2f),
        radius = 60f,
        center = Offset(crashX, crashY)
    )
}