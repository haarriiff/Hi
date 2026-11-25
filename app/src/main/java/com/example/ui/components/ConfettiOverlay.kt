package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import com.example.ui.theme.BerryRose
import com.example.ui.theme.CandleGold
import com.example.ui.theme.FlameMid
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SoftBlush
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val xFraction: Float,
    val yOffset: Float,
    val speed: Float,
    val sizeDp: Float,
    val swayAmplitude: Float,
    val phase: Float,
    val rotationSpeed: Float,
    val color: Color,
    val isCircle: Boolean
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 44,
    alpha: Float = 0.85f
) {
    val palette = remember {
        listOf(
            BerryRose,
            CandleGold,
            FlameMid,
            SageAccent,
            Color(0xFFE76F8B),
            Color(0xFFFFD166),
            SoftBlush
        )
    }

    val particles = remember(particleCount) {
        val rng = Random(42)
        List(particleCount) { idx ->
            ConfettiParticle(
                xFraction = rng.nextFloat(),
                yOffset = rng.nextFloat(),
                speed = 0.55f + rng.nextFloat() * 0.65f,
                sizeDp = 6f + rng.nextFloat() * 7f,
                swayAmplitude = 14f + rng.nextFloat() * 22f,
                phase = rng.nextFloat() * (2f * PI.toFloat()),
                rotationSpeed = (rng.nextFloat() - 0.5f) * 360f,
                color = palette[idx % palette.size],
                isCircle = idx % 3 == 0
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        particles.forEach { p ->
            val yFraction = (p.yOffset + progress * p.speed) % 1.15f - 0.08f
            val x = p.xFraction * w + sin(progress * 2f * PI.toFloat() * 2f + p.phase) * p.swayAmplitude
            val y = yFraction * h
            val pxSize = p.sizeDp * density
            val drawColor = p.color.copy(alpha = alpha)

            if (p.isCircle) {
                drawCircle(
                    color = drawColor,
                    radius = pxSize * 0.45f,
                    center = Offset(x, y)
                )
            } else {
                rotate(
                    degrees = p.rotationSpeed * progress,
                    pivot = Offset(x, y)
                ) {
                    drawRect(
                        color = drawColor,
                        topLeft = Offset(x - pxSize * 0.5f, y - pxSize * 0.28f),
                        size = Size(pxSize, pxSize * 0.56f)
                    )
                }
            }
        }
    }
}

private enum class BurstShape {
    RIBBON,
    CIRCLE,
    DIAMOND
}

private class BurstParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    val rotationVelocity: Float,
    var tumblePhase: Float,
    val tumbleSpeed: Float,
    val sizePx: Float,
    val color: Color,
    val shape: BurstShape,
    val drag: Float
)

@Composable
fun ConfettiBurstOverlay(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 96,
    originYFraction: Float = 0.44f
) {
    val palette = remember {
        listOf(
            BerryRose,
            CandleGold,
            FlameMid,
            SageAccent,
            Color(0xFFE76F8B),
            Color(0xFFFFD166),
            Color(0xFF7BDFF2),
            SoftBlush
        )
    }

    var particles by remember { mutableStateOf<List<BurstParticle>>(emptyList()) }
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(trigger, canvasWidth, canvasHeight) {
        if (!trigger) {
            particles = emptyList()
            elapsedSeconds = 0f
            return@LaunchedEffect
        }
        if (canvasWidth <= 0f || canvasHeight <= 0f) return@LaunchedEffect

        val rng = Random(System.currentTimeMillis())
        val originX = canvasWidth * 0.5f
        val originY = canvasHeight * originYFraction
        val minDim = min(canvasWidth, canvasHeight)

        particles = List(particleCount) { idx ->
            // Mix wide upward fountain angles with radial celebratory pop
            val angle = (-PI / 2.0 + (rng.nextDouble() - 0.5) * 2.2).toFloat()
            val speed = minDim * (0.65f + rng.nextFloat() * 1.15f)
            BurstParticle(
                x = originX + (rng.nextFloat() - 0.5f) * minDim * 0.14f,
                y = originY + (rng.nextFloat() - 0.5f) * minDim * 0.08f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - minDim * 0.15f,
                rotation = rng.nextFloat() * 360f,
                rotationVelocity = (rng.nextFloat() - 0.5f) * 520f,
                tumblePhase = rng.nextFloat() * (2f * PI.toFloat()),
                tumbleSpeed = 4f + rng.nextFloat() * 6f,
                sizePx = (14f + rng.nextFloat() * 16f),
                color = palette[idx % palette.size],
                shape = BurstShape.entries[idx % BurstShape.entries.size],
                drag = 0.985f - rng.nextFloat() * 0.015f
            )
        }
        elapsedSeconds = 0f

        val gravity = minDim * 1.15f
        val durationSeconds = 3.6f
        var lastFrameNanos = 0L

        while (elapsedSeconds < durationSeconds) {
            val frameNanos = withFrameNanos { it }
            if (lastFrameNanos != 0L) {
                val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                elapsedSeconds += dt
                particles.forEach { p ->
                    p.vx *= p.drag
                    p.vy = (p.vy * p.drag) + gravity * dt
                    p.x += p.vx * dt + sin(elapsedSeconds * 5f + p.tumblePhase) * 0.6f
                    p.y += p.vy * dt
                    p.rotation += p.rotationVelocity * dt
                    p.tumblePhase += p.tumbleSpeed * dt
                }
            }
            lastFrameNanos = frameNanos
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                canvasWidth = intSize.width.toFloat()
                canvasHeight = intSize.height.toFloat()
            }
    ) {
        if (!trigger || particles.isEmpty()) return@Canvas

        val fadeAlpha = when {
            elapsedSeconds < 2.4f -> 1f
            elapsedSeconds >= 3.6f -> 0f
            else -> 1f - ((elapsedSeconds - 2.4f) / 1.2f)
        }.coerceIn(0f, 1f)

        if (fadeAlpha <= 0f) return@Canvas

        particles.forEach { p ->
            if (p.y > size.height + p.sizePx || p.x < -p.sizePx || p.x > size.width + p.sizePx) {
                return@forEach
            }
            val drawColor = p.color.copy(alpha = fadeAlpha * 0.95f)
            val tumbleScale = (0.35f + 0.65f * abs(cos(p.tumblePhase)))

            when (p.shape) {
                BurstShape.CIRCLE -> {
                    drawCircle(
                        color = drawColor,
                        radius = p.sizePx * 0.32f,
                        center = Offset(p.x, p.y)
                    )
                }

                BurstShape.RIBBON -> {
                    rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                        val h = p.sizePx * 0.55f * tumbleScale
                        drawRect(
                            color = drawColor,
                            topLeft = Offset(p.x - p.sizePx * 0.5f, p.y - h * 0.5f),
                            size = Size(p.sizePx, h)
                        )
                    }
                }

                BurstShape.DIAMOND -> {
                    rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                        val half = p.sizePx * 0.42f
                        val path = Path().apply {
                            moveTo(p.x, p.y - half)
                            lineTo(p.x + half * tumbleScale, p.y)
                            lineTo(p.x, p.y + half)
                            lineTo(p.x - half * tumbleScale, p.y)
                            close()
                        }
                        drawPath(path = path, color = drawColor)
                    }
                }
            }
        }
    }
}
