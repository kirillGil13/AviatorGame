package com.mycompany.aviatorgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import com.mycompany.aviatorgame.R
import com.mycompany.aviatorgame.ui.theme.DarkSurface
import kotlin.math.*

@Composable
fun AviatorGameCanvas(
    modifier: Modifier = Modifier,
    multiplier: Float,
    isPlaying: Boolean,
    isCrashed: Boolean,
    shouldPlayCrashAnimation: Boolean = false,
    onCrashAnimationComplete: () -> Unit = {}
) {
    val airplanePainter = painterResource(id = R.drawable.ic_aviator)

    // Анимация движения точек
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotsOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots_offset"
    )

    // Анимация улета при краше
    var flyAwayProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(shouldPlayCrashAnimation) {
        if (shouldPlayCrashAnimation) {
            // Проигрываем анимацию
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(600, easing = FastOutLinearInEasing)
            ) { value, _ ->
                flyAwayProgress = value
            }
            // После завершения анимации сообщаем об этом
            onCrashAnimationComplete()
        }
    }

    // Сбрасываем прогресс только при начале нового раунда
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            flyAwayProgress = 0f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Плашка с точками - ВСЕГДА ВИДНА
        // Точки двигаются только во время полета
        drawBottomDots(
            width = width,
            height = height,
            animationOffset = dotsOffset,
            shouldAnimate = isPlaying && !isCrashed
        )

        if (isPlaying || isCrashed) {
            val progress = calculateProgress(multiplier)

            // Красная заполненная область под траекторией - только во время полета
            if (isPlaying && !isCrashed) {
                drawFilledTrajectory(width, height, progress, isCrashed)
            }

            // Самолет - только во время полета или анимации улета
            if (isPlaying || shouldPlayCrashAnimation) {
                drawAirplane(
                    painter = airplanePainter,
                    width = width,
                    height = height,
                    progress = progress,
                    isCrashed = isCrashed,
                    flyAwayProgress = flyAwayProgress
                )
            }
        } else {
            // Самолет на старте
            val startX = width * 0.1f
            val planeHeight = 125f
            val borderY = height * 0.916f
            val startY = borderY - planeHeight / 2
            drawPlane(airplanePainter, startX, startY, 8f, 1f)
        }
    }
}

fun calculateProgress(multiplier: Float): Float {
    return when {
        multiplier <= 1f -> 0f
        multiplier >= 10f -> 1f
        else -> {
            val normalized = (multiplier - 1f) / 9f
            normalized.pow(0.7f)
        }
    }
}

fun DrawScope.drawBottomDots(
    width: Float,
    height: Float,
    animationOffset: Float,
    shouldAnimate: Boolean
) {
    val bottomAreaHeight = height * 0.084f
    val bottomAreaTop = height * 0.916f

    drawRect(
        color = DarkSurface,
        topLeft = Offset(0f, bottomAreaTop),
        size = Size(width, bottomAreaHeight)
    )

    drawLine(
        color = Color.White,
        start = Offset(0f, bottomAreaTop),
        end = Offset(width, bottomAreaTop),
        strokeWidth = 1.5f
    )

    val dotSpacing = 120f
    val dotY = bottomAreaTop + (bottomAreaHeight / 2f)
    val dotColor = Color.White

    // Применяем движение только если shouldAnimate = true
    val movementOffset = if (shouldAnimate) -animationOffset else 0f

    var x = (movementOffset % dotSpacing + dotSpacing) % dotSpacing
    while (x <= width + dotSpacing) {
        drawCircle(
            color = dotColor,
            radius = 2.5f,
            center = Offset(x, dotY)
        )
        x += dotSpacing
    }
}

fun DrawScope.drawFilledTrajectory(
    width: Float,
    height: Float,
    progress: Float,
    isCrashed: Boolean
) {
    if (progress <= 0f) return

    val planeWidth = 140f
    val planeHeight = 125f
    val borderY = height * 0.916f
    val planeCenterY = borderY - planeHeight / 2
    val startX = width * 0.1f - (planeWidth * 0.29f)
    val startY = planeCenterY + (planeHeight * 0.48f)

    val fillColor = Color(0xFFFF033C)

    val path = Path()
    path.moveTo(startX, startY)

    val steps = 100
    val points = mutableListOf<Offset>()
    points.add(Offset(startX, startY))

    for (i in 1..steps) {
        val t = (i.toFloat() / steps) * progress
        val x = startX + (width * 0.85f) * t
        val curveHeight = height * 0.9f
        val y = startY - curveHeight * t * sqrt(t)

        points.add(Offset(x, y))
        path.lineTo(x, y)
    }

    val lastPoint = points.last()
    path.lineTo(lastPoint.x, startY)
    path.lineTo(startX, startY)
    path.close()

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                fillColor.copy(alpha = 0.6f),
                fillColor.copy(alpha = 0.35f),
                fillColor.copy(alpha = 0.15f)
            ),
            startY = startY - height * 0.9f * progress,
            endY = startY
        )
    )

    val topPath = Path()
    topPath.moveTo(startX, startY)
    for (point in points) {
        topPath.lineTo(point.x, point.y)
    }

    drawPath(
        path = topPath,
        color = fillColor,
        style = Stroke(
            width = 4f,
            cap = StrokeCap.Round
        )
    )
}

fun DrawScope.drawAirplane(
    painter: androidx.compose.ui.graphics.painter.Painter,
    width: Float,
    height: Float,
    progress: Float,
    isCrashed: Boolean,
    flyAwayProgress: Float
) {
    val startX = width * 0.1f
    val planeHeight = 125f
    val borderY = height * 0.916f
    val startY = borderY - planeHeight / 2

    val t = progress.coerceIn(0f, 1f)
    var planeX = startX + (width * 0.85f) * t
    var planeY = startY - (height * 0.9f) * t * sqrt(t)

    if (isCrashed && flyAwayProgress > 0) {
        planeX += width * 0.5f * flyAwayProgress
        planeY -= height * 0.5f * flyAwayProgress
    }

    // Угол наклона - всегда одинаковый
    val angle = 8f

    val opacity = if (flyAwayProgress > 0.7f) {
        1f - (flyAwayProgress - 0.7f) / 0.3f
    } else {
        1f
    }

    if (opacity > 0.05f) {
        drawPlane(painter, planeX, planeY, angle, opacity)
    }
}

fun DrawScope.drawPlane(
    painter: androidx.compose.ui.graphics.painter.Painter,
    x: Float,
    y: Float,
    angle: Float,
    opacity: Float
) {
    drawIntoCanvas { canvas ->
        canvas.save()

        val planeWidth = 140f
        val planeHeight = 125f

        canvas.translate(x, y)
        canvas.rotate(angle)

        translate(
            left = -planeWidth / 2,
            top = -planeHeight / 2
        ) {
            drawIntoCanvas { innerCanvas ->
                if (opacity < 1f) {
                    val paint = Paint().apply { alpha = opacity }
                    innerCanvas.saveLayer(
                        androidx.compose.ui.geometry.Rect(
                            0f, 0f, planeWidth, planeHeight
                        ),
                        paint
                    )
                }

                with(painter) {
                    draw(size = Size(planeWidth, planeHeight))
                }

                if (opacity < 1f) {
                    innerCanvas.restore()
                }
            }
        }

        canvas.restore()
    }
}