package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BerryRose
import com.example.ui.theme.CandleGold
import com.example.ui.theme.FlameCore
import com.example.ui.theme.FlameMid
import com.example.ui.theme.FlameOuter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun CandleCakeCanvas(
    candlesLit: List<Boolean>,
    isBlowing: Boolean,
    onTapCandle: (Int) -> Unit,
    contentDescriptionText: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "candle_flicker")

    val flicker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flicker_phase"
    )

    val smokeDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smoke_drift"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .semantics { contentDescription = contentDescriptionText }
            .pointerInput(candlesLit) {
                detectTapGestures { tapOffset ->
                    val count = candlesLit.size.coerceAtLeast(1)
                    val cakeLeft = size.width * 0.22f
                    val cakeRight = size.width * 0.78f
                    val topTierTop = size.height * 0.42f
                    if (tapOffset.y < topTierTop + size.height * 0.12f) {
                        var closestIdx = -1
                        var minDist = Float.MAX_VALUE
                        for (i in 0 until count) {
                            val cx = cakeLeft + (cakeRight - cakeLeft) * ((i + 0.5f) / count)
                            val dist = abs(tapOffset.x - cx)
                            if (dist < minDist) {
                                minDist = dist
                                closestIdx = i
                            }
                        }
                        if (closestIdx in candlesLit.indices && minDist < size.width * 0.14f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTapCandle(closestIdx)
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val centerX = w * 0.5f

        // Cake stand shadow & ceramic pedestal
        drawOval(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft = Offset(centerX - w * 0.28f, h * 0.89f),
            size = Size(w * 0.56f, h * 0.06f)
        )
        drawRoundRect(
            color = Color(0xFFEEDFD9),
            topLeft = Offset(centerX - w * 0.045f, h * 0.79f),
            size = Size(w * 0.09f, h * 0.11f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFFFFF8F5),
            topLeft = Offset(centerX - w * 0.18f, h * 0.87f),
            size = Size(w * 0.36f, h * 0.042f),
            cornerRadius = CornerRadius(24f, 24f)
        )
        // Cake plate top
        drawRoundRect(
            color = Color(0xFFFFFBF8),
            topLeft = Offset(centerX - w * 0.35f, h * 0.77f),
            size = Size(w * 0.70f, h * 0.036f),
            cornerRadius = CornerRadius(30f, 30f)
        )

        // Bottom tier sponge
        val bottomTierLeft = centerX - w * 0.30f
        val bottomTierWidth = w * 0.60f
        val bottomTierTop = h * 0.57f
        val bottomTierHeight = h * 0.21f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD8DE), Color(0xFFF3B3C0))
            ),
            topLeft = Offset(bottomTierLeft, bottomTierTop),
            size = Size(bottomTierWidth, bottomTierHeight),
            cornerRadius = CornerRadius(28f, 28f)
        )
        // Cream filling stripe on bottom tier
        drawRect(
            color = Color(0xFFFFF5EB),
            topLeft = Offset(bottomTierLeft + 6f, bottomTierTop + bottomTierHeight * 0.48f),
            size = Size(bottomTierWidth - 12f, bottomTierHeight * 0.13f)
        )

        // Top tier sponge
        val topTierLeft = centerX - w * 0.22f
        val topTierWidth = w * 0.44f
        val topTierTop = h * 0.41f
        val topTierHeight = h * 0.18f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF0DB), Color(0xFFF7D9B4))
            ),
            topLeft = Offset(topTierLeft, topTierTop),
            size = Size(topTierWidth, topTierHeight),
            cornerRadius = CornerRadius(24f, 24f)
        )

        // Berry frosting drip cap on top tier
        val dripPath = Path().apply {
            moveTo(topTierLeft, topTierTop + 18f)
            quadraticTo(topTierLeft, topTierTop, topTierLeft + 24f, topTierTop)
            lineTo(topTierLeft + topTierWidth - 24f, topTierTop)
            quadraticTo(topTierLeft + topTierWidth, topTierTop, topTierLeft + topTierWidth, topTierTop + 18f)
            lineTo(topTierLeft + topTierWidth, topTierTop + topTierHeight * 0.42f)
            val lobes = 6
            val step = topTierWidth / lobes
            for (i in lobes downTo 1) {
                val xEnd = topTierLeft + (i - 1) * step
                val xMid = xEnd + step * 0.5f
                val depth = if (i % 2 == 0) topTierHeight * 0.68f else topTierHeight * 0.44f
                quadraticTo(xMid, topTierTop + depth, xEnd, topTierTop + topTierHeight * 0.36f)
            }
            close()
        }
        drawPath(
            path = dripPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE75A7C), BerryRose)
            )
        )

        // Sprinkles on frosting
        val sprinkleColors = listOf(Color(0xFFFFE066), Color.White, Color(0xFF8CE0C8), CandleGold)
        for (i in 0..10) {
            val sx = topTierLeft + topTierWidth * (0.1f + 0.08f * i)
            val sy = topTierTop + topTierHeight * (0.14f + (i % 3) * 0.07f)
            drawCircle(
                color = sprinkleColors[i % sprinkleColors.size],
                radius = 4.2f,
                center = Offset(sx, sy)
            )
        }

        // Candles
        val candleCount = candlesLit.size.coerceAtLeast(1)
        val candleColors = listOf(
            Color(0xFFFF8FAB),
            Color(0xFF7BDFF2),
            Color(0xFFFFD166),
            Color(0xFFB8E0D2),
            Color(0xFFFF9F68)
        )

        val litCount = candlesLit.count { it }
        if (litCount > 0) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FlameMid.copy(alpha = 0.16f + 0.04f * litCount),
                        Color.Transparent
                    ),
                    center = Offset(centerX, h * 0.25f),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(centerX, h * 0.25f)
            )
        }

        val candleAreaLeft = topTierLeft + topTierWidth * 0.10f
        val candleAreaRight = topTierLeft + topTierWidth * 0.90f
        val candleW = (topTierWidth * 0.07f).coerceAtLeast(14f)
        val candleH = h * 0.145f
        val candleBottom = topTierTop + 6f

        candlesLit.forEachIndexed { idx, isLit ->
            val cx = candleAreaLeft + (candleAreaRight - candleAreaLeft) * ((idx + 0.5f) / candleCount)
            val candleTop = candleBottom - candleH
            val bodyColor = candleColors[idx % candleColors.size]

            // Candle body
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(cx - candleW / 2f, candleTop),
                size = Size(candleW, candleH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Diagonal stripe accent
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(cx - candleW * 0.35f, candleTop + candleH * 0.25f),
                end = Offset(cx + candleW * 0.35f, candleTop + candleH * 0.42f),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(cx - candleW * 0.35f, candleTop + candleH * 0.58f),
                end = Offset(cx + candleW * 0.35f, candleTop + candleH * 0.75f),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Wick
            val wickTop = candleTop - 12f
            drawLine(
                color = Color(0xFF3A2428),
                start = Offset(cx, candleTop),
                end = Offset(cx, wickTop),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            if (isLit) {
                val sway = sin(flicker + idx * 1.3f) * 3.5f + if (isBlowing) 12f else 0f
                val flameScale = if (isBlowing) 0.75f else 1f + 0.08f * sin(flicker * 2f + idx)
                val flameBaseY = wickTop - 2f
                val flameTipY = flameBaseY - 34f * flameScale

                // Flame halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(FlameMid.copy(alpha = 0.42f), Color.Transparent),
                        center = Offset(cx + sway * 0.4f, flameBaseY - 14f),
                        radius = 44f
                    ),
                    radius = 44f,
                    center = Offset(cx + sway * 0.4f, flameBaseY - 14f)
                )

                // Outer flame tear drop
                val outerFlame = Path().apply {
                    moveTo(cx, flameBaseY)
                    cubicTo(
                        cx - 14f, flameBaseY - 10f,
                        cx - 10f + sway * 0.5f, flameTipY + 12f,
                        cx + sway, flameTipY
                    )
                    cubicTo(
                        cx + 12f + sway * 0.5f, flameTipY + 12f,
                        cx + 14f, flameBaseY - 10f,
                        cx, flameBaseY
                    )
                    close()
                }
                drawPath(
                    path = outerFlame,
                    brush = Brush.verticalGradient(
                        colors = listOf(FlameOuter, FlameMid)
                    )
                )

                // Inner bright core
                val innerFlame = Path().apply {
                    moveTo(cx, flameBaseY - 2f)
                    cubicTo(
                        cx - 7f, flameBaseY - 8f,
                        cx - 4f + sway * 0.4f, flameTipY + 18f,
                        cx + sway * 0.6f, flameTipY + 12f
                    )
                    cubicTo(
                        cx + 6f + sway * 0.4f, flameTipY + 18f,
                        cx + 7f, flameBaseY - 8f,
                        cx, flameBaseY - 2f
                    )
                    close()
                }
                drawPath(path = innerFlame, color = FlameCore)
            } else {
                // Rising gentle smoke wisp when candle is blown out
                val smokeY = wickTop - 10f - smokeDrift * 38f
                val smokeX = cx + sin(smokeDrift * 2f * PI.toFloat() + idx) * 8f
                val smokeAlpha = (1f - smokeDrift) * 0.45f
                val smokePath = Path().apply {
                    moveTo(cx, wickTop - 2f)
                    quadraticTo(cx - 8f, wickTop - 16f, smokeX, smokeY)
                }
                drawPath(
                    path = smokePath,
                    color = Color.White.copy(alpha = smokeAlpha),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }
        }

        // Wind gusts visual when blowing
        if (isBlowing) {
            for (g in 0..2) {
                val gy = h * (0.20f + g * 0.065f)
                val gxStart = w * (0.08f + g * 0.05f)
                val gxEnd = w * (0.45f + (flicker / (2f * PI.toFloat())) * 0.35f)
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(gxStart, gy),
                    end = Offset(gxEnd, gy - 6f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
