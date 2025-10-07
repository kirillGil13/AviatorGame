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
import com.mycompany.aviatorgame.ui.theme.ButtonPrimary
import com.mycompany.aviatorgame.ui.theme.DarkBackground
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

    // Анимация движения точек (горизонтальная для нижней плашки)
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotsOffsetHorizontal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f, // Соответствует новому dotSpacing
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots_offset_horizontal"
    )

    // Анимация движения точек (вертикальная для левой плашки)
    val dotsOffsetVertical by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f, // Соответствует новому dotSpacing
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots_offset_vertical"
    )

    // Анимация улета при краше
    var flyAwayProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(shouldPlayCrashAnimation) {
        if (shouldPlayCrashAnimation) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(600, easing = FastOutLinearInEasing)
            ) { value, _ ->
                flyAwayProgress = value
            }
            onCrashAnimationComplete()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            flyAwayProgress = 0f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Левая плашка с вертикальными точками - ВСЕГДА ВИДНА
        drawLeftDots(
            width = width,
            height = height,
            animationOffset = dotsOffsetVertical,
            shouldAnimate = isPlaying && !isCrashed
        )

        // Нижняя плашка с горизонтальными точками - ВСЕГДА ВИДНА
        drawBottomDots(
            width = width,
            height = height,
            animationOffset = dotsOffsetHorizontal,
            shouldAnimate = isPlaying && !isCrashed
        )

        // Угловой элемент где встречаются плашки
        drawCornerElement(width, height)

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
            val leftPanelWidth = width * 0.06f
            val startX = leftPanelWidth + (width - leftPanelWidth) * 0.15f
            val planeHeight = 150f
            val borderY = height * 0.94f
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

fun DrawScope.drawLeftDots(
    width: Float,
    height: Float,
    animationOffset: Float,
    shouldAnimate: Boolean
) {
    val leftPanelWidth = width * 0.06f
    val leftPanelRight = leftPanelWidth
    val bottomPanelTop = height * 0.94f // Где начинается нижняя плашка

    // Рисуем фон левой плашки (только до нижней плашки)
    drawRect(
        color = DarkBackground,
        topLeft = Offset(0f, 0f),
        size = Size(leftPanelWidth, bottomPanelTop)
    )

    // Рисуем правую границу левой плашки
    drawLine(
        color = Color.White,
        start = Offset(leftPanelRight, 0f),
        end = Offset(leftPanelRight, bottomPanelTop),
        strokeWidth = 1.5f
    )

    val dotSpacing = 180f // Увеличили расстояние между точками
    val dotX = leftPanelWidth / 2f
    val dotColor = Color.White

    // Применяем движение сверху вниз (положительный offset)
    val movementOffset = if (shouldAnimate) animationOffset else 0f

    var y = (movementOffset % dotSpacing + dotSpacing) % dotSpacing
    while (y <= bottomPanelTop + dotSpacing) {
        if (y <= bottomPanelTop) { // Рисуем точки только до нижней плашки
            drawCircle(
                color = dotColor,
                radius = 6f,
                center = Offset(dotX, y)
            )
        }
        y += dotSpacing
    }
}

fun DrawScope.drawBottomDots(
    width: Float,
    height: Float,
    animationOffset: Float,
    shouldAnimate: Boolean
) {
    val leftPanelWidth = width * 0.06f // Ширина левой плашки
    val bottomAreaHeight = height * 0.06f // Уменьшили высоту плашки
    val bottomAreaTop = height * 0.94f // Подняли плашку выше
    val bottomAreaLeft = leftPanelWidth // Начинаем от края левой плашки

    drawRect(
        color = DarkBackground,
        topLeft = Offset(bottomAreaLeft, bottomAreaTop),
        size = Size(width - bottomAreaLeft, bottomAreaHeight)
    )

    drawLine(
        color = Color.White,
        start = Offset(bottomAreaLeft, bottomAreaTop),
        end = Offset(width, bottomAreaTop),
        strokeWidth = 1.5f
    )

    val dotSpacing = 180f // Увеличили расстояние между точками
    val dotY = bottomAreaTop + (bottomAreaHeight / 2f)
    val dotColor = Color.White

    val movementOffset = if (shouldAnimate) -animationOffset else 0f

    var x = bottomAreaLeft + (movementOffset % dotSpacing + dotSpacing) % dotSpacing
    while (x <= width + dotSpacing) {
        drawCircle(
            color = dotColor,
            radius = 6f, // Увеличили размер точек
            center = Offset(x, dotY)
        )
        x += dotSpacing
    }
}

fun DrawScope.drawCornerElement(width: Float, height: Float) {
    val leftPanelWidth = width * 0.06f
    val bottomAreaHeight = height * 0.06f
    val bottomAreaTop = height * 0.94f

    // Рисуем маленький квадратик в углу
    drawRect(
        color = DarkBackground,
        topLeft = Offset(0f, bottomAreaTop),
        size = Size(leftPanelWidth, bottomAreaHeight)
    )
}

fun DrawScope.drawFilledTrajectory(
    width: Float,
    height: Float,
    progress: Float,
    isCrashed: Boolean
) {
    if (progress <= 0f) return

    val planeWidth = 180f
    val planeHeight = 150f
    val leftPanelWidth = width * 0.06f
    val availableWidth = width - leftPanelWidth
    val borderY = height * 0.94f
    val planeCenterY = borderY - planeHeight / 2
    val planeStartX = leftPanelWidth + availableWidth * 0.15f

    // Рассчитываем точку хвоста самолета
    val angleRad = Math.toRadians(8.0).toFloat()
    val tailOffsetX = cos(angleRad) * (planeWidth / 2)
    val tailOffsetY = sin(angleRad) * (planeWidth / 2) + (planeHeight * 0.35f)

    val startX = planeStartX - tailOffsetX
    val startY = planeCenterY + tailOffsetY

    val fillColor = ButtonPrimary

    val path = Path()
    path.moveTo(startX, startY)

    val steps = 100
    val points = mutableListOf<Offset>()
    points.add(Offset(startX, startY))

    for (i in 1..steps) {
        val t = (i.toFloat() / steps) * progress
        val x = startX + (availableWidth * 0.8f) * t
        val curveHeight = height * 0.85f // Уменьшили высоту кривой
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
            startY = startY - height * 0.85f * progress,
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
    val leftPanelWidth = width * 0.06f
    val availableWidth = width - leftPanelWidth
    val startX = leftPanelWidth + availableWidth * 0.15f
    val planeHeight = 150f
    val borderY = height * 0.94f
    val startY = borderY - planeHeight / 2

    val t = progress.coerceIn(0f, 1f)
    var planeX = startX + (availableWidth * 0.8f) * t
    var planeY = startY - (height * 0.85f) * t * sqrt(t)

    if (isCrashed && flyAwayProgress > 0) {
        planeX += width * 0.5f * flyAwayProgress
        planeY -= height * 0.5f * flyAwayProgress
    }

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

        val planeWidth = 180f
        val planeHeight = 150f

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