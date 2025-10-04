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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.aviatorgame.R
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
    val airplanePainter = painterResource(id = R.drawable.ic_aviator)

    // Анимация звезд на фоне
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

    // Анимация улета самолета при краше
    var flyAwayProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isCrashed) {
        if (isCrashed) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ) { value, _ ->
                flyAwayProgress = value
            }
        } else {
            flyAwayProgress = 0f
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Фоновая сетка (статичная)
            drawStaticGrid(width, height)

            // Звезды на фоне
            drawStars(starAnimation, width, height)

            // Основная игровая зона
            if (isPlaying || isCrashed) {
                val progress = (multiplier - 1f).coerceIn(0f, 99f) / 99f

                // Рисуем дугу-радар (красную расходящуюся)
                drawRadarArc(width, height, progress, isCrashed)

                // Рисуем след от самолета
                drawPlaneTrail(width, height, progress, isCrashed)

                // Рисуем самолет с изображением
                drawAirplaneImage(
                    painter = airplanePainter,
                    width = width,
                    height = height,
                    progress = progress,
                    isCrashed = isCrashed,
                    flyAwayProgress = flyAwayProgress
                )
            } else {
                // Самолет на старте (неподвижно)
                drawAirplaneAtStart(airplanePainter, width, height)
            }
        }
    }
}

fun DrawScope.drawStaticGrid(width: Float, height: Float) {
    val gridSize = 50f
    val lineColor = Color(0xFF1a2547).copy(alpha = 0.2f)

    // Вертикальные линии
    var x = 0f
    while (x < width) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 0.5f
        )
        x += gridSize
    }

    // Горизонтальные линии
    var y = 0f
    while (y < height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 0.5f
        )
        y += gridSize
    }
}

fun DrawScope.drawStars(animation: Float, width: Float, height: Float) {
    val starPositions = listOf(
        Offset(width * 0.15f, height * 0.2f),
        Offset(width * 0.35f, height * 0.1f),
        Offset(width * 0.55f, height * 0.25f),
        Offset(width * 0.75f, height * 0.15f),
        Offset(width * 0.85f, height * 0.3f),
        Offset(width * 0.25f, height * 0.35f),
        Offset(width * 0.65f, height * 0.05f),
        Offset(width * 0.45f, height * 0.4f),
    )

    starPositions.forEachIndexed { index, position ->
        val alpha = ((sin(animation * 6.28f + index * 0.8f) + 1f) / 2f * 0.4f + 0.2f)

        // Звезда
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 1.5f,
            center = position
        )

        // Свечение звезды
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.3f),
            radius = 3f,
            center = position
        )
    }
}

fun DrawScope.drawRadarArc(width: Float, height: Float, progress: Float, isCrashed: Boolean) {
    val startX = width * 0.15f  // Начало слева
    val startY = height * 0.75f  // Начало внизу

    // Цвет дуги
    val arcColor = if (isCrashed) {
        Color(0xFFff4444).copy(alpha = 0.8f)
    } else {
        Color(0xFFff4444).copy(alpha = 0.6f)
    }

    // Рисуем расходящийся веер/радар
    val path = Path()

    // Начальная точка
    path.moveTo(startX, startY)

    // Верхняя линия веера
    val topEndX = startX + (width * 0.7f * progress)
    val topEndY = startY - (height * 0.6f * progress)
    path.lineTo(topEndX, topEndY - (progress * 30f)) // Расходится вверх

    // Дуга сверху
    if (progress > 0.05f) {
        val arcPath = Path()
        val steps = 30
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val angle = -PI/4 - (PI/6 * progress) + t * (PI/3 + PI/3 * progress)
            val radius = width * 0.7f * progress
            val x = startX + cos(angle).toFloat() * radius
            val y = startY + sin(angle).toFloat() * radius

            if (i == 0) {
                arcPath.moveTo(x, y)
            } else {
                arcPath.lineTo(x, y)
            }
        }

        drawPath(
            path = arcPath,
            color = arcColor,
            style = Stroke(width = 2f)
        )
    }

    // Нижняя линия веера
    path.moveTo(startX, startY)
    val bottomEndX = startX + (width * 0.7f * progress)
    val bottomEndY = startY - (height * 0.4f * progress)
    path.lineTo(bottomEndX, bottomEndY + (progress * 20f)) // Расходится вниз

    // Рисуем основные линии
    drawPath(
        path = path,
        color = arcColor,
        style = Stroke(width = 2f)
    )

    // Заливка внутри веера (полупрозрачная)
    if (progress > 0.05f) {
        val fillPath = Path()
        fillPath.moveTo(startX, startY)

        // Создаем веер
        for (i in 0..20) {
            val t = i / 20f
            val angle = -PI/4 - (PI/6 * progress) + t * (PI/3 + PI/3 * progress)
            val radius = width * 0.7f * progress
            val x = startX + cos(angle).toFloat() * radius
            val y = startY + sin(angle).toFloat() * radius
            fillPath.lineTo(x, y)
        }

        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    arcColor.copy(alpha = 0.1f),
                    arcColor.copy(alpha = 0.02f)
                ),
                center = Offset(startX, startY)
            )
        )
    }
}

fun DrawScope.drawPlaneTrail(width: Float, height: Float, progress: Float, isCrashed: Boolean) {
    if (progress < 0.02f) return

    val startX = width * 0.15f
    val startY = height * 0.75f

    val path = Path()
    path.moveTo(startX, startY)

    // Рисуем след по параболической траектории
    val steps = (100 * progress).toInt()
    for (i in 1..steps) {
        val t = i.toFloat() / 100
        val x = startX + (width * 0.7f * t)
        val y = startY - (height * 0.5f * t * sqrt(1 + t))
        path.lineTo(x, y)
    }

    // След самолета - белая линия
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.8f),
        style = Stroke(
            width = 2f,
            cap = StrokeCap.Round
        )
    )

    // Свечение следа
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.2f),
        style = Stroke(
            width = 6f,
            cap = StrokeCap.Round
        )
    )
}

fun DrawScope.drawAirplaneAtStart(painter: androidx.compose.ui.graphics.painter.Painter, width: Float, height: Float) {
    val planeX = width * 0.15f
    val planeY = height * 0.75f

    drawAirplaneWithImage(painter, planeX, planeY, 0f)
}

fun DrawScope.drawAirplaneImage(
    painter: androidx.compose.ui.graphics.painter.Painter,
    width: Float,
    height: Float,
    progress: Float,
    isCrashed: Boolean,
    flyAwayProgress: Float
) {
    val startX = width * 0.15f
    val startY = height * 0.75f

    // Позиция самолета на параболе
    var planeX = startX + (width * 0.7f * progress)
    var planeY = startY - (height * 0.5f * progress * sqrt(1 + progress))

    // При краше самолет быстро улетает вверх и вправо
    if (isCrashed && flyAwayProgress > 0) {
        planeX += width * 0.3f * flyAwayProgress
        planeY -= height * 0.5f * flyAwayProgress
    }

    // Угол наклона (тангенс к траектории)
    val angle = if (isCrashed && flyAwayProgress > 0.5f) {
        -45f // Резкий взлет вверх
    } else {
        val dx = 0.01f
        val y1 = height * 0.5f * progress * sqrt(1 + progress)
        val y2 = height * 0.5f * (progress + dx) * sqrt(1 + progress + dx)
        -atan2(y2 - y1, width * 0.7f * dx) * 180 / PI.toFloat()
    }

    // Рисуем самолет только если он не улетел
    if (flyAwayProgress < 0.9f) {
        drawAirplaneWithImage(painter, planeX, planeY, angle)
    }
}

fun DrawScope.drawAirplaneWithImage(
    painter: androidx.compose.ui.graphics.painter.Painter,
    x: Float,
    y: Float,
    angle: Float
) {
    drawIntoCanvas { canvas ->
        canvas.save()

        // Размер самолета
        val planeWidth = 200f
        val planeHeight = 170f

        // Перемещаем и поворачиваем
        canvas.translate(x, y)
        canvas.rotate(angle)

        // Рисуем изображение самолета
        translate(
            left = -planeWidth / 2,
            top = -planeHeight / 2
        ) {
            with(painter) {
                draw(
                    size = Size(planeWidth, planeHeight)
                )
            }
        }

        canvas.restore()
    }
}