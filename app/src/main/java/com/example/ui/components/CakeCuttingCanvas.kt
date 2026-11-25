package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BerryRose
import com.example.ui.theme.CandleGold
import com.example.ui.theme.EspressoInk
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CakeCuttingCanvas(
    cutProgress: Float,
    isCakeCut: Boolean,
    onCutProgressChange: (Float) -> Unit,
    onCutCompleted: () -> Unit,
    onCutAnimationFinished: () -> Unit,
    contentDescriptionText: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val sliceOffsetAnim = remember { Animatable(if (isCakeCut) 1f else 0f) }

    LaunchedEffect(isCakeCut) {
        if (isCakeCut) {
            if (sliceOffsetAnim.value < 1f) {
                sliceOffsetAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            onCutAnimationFinished()
        } else {
            sliceOffsetAnim.snapTo(0f)
        }
    }

    val sliceOffsetFraction = sliceOffsetAnim.value

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .semantics { contentDescription = contentDescriptionText }
            .pointerInput(isCakeCut) {
                if (!isCakeCut) {
                    var totalDrag = 0f
                    detectDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount.getDistance()
                            val targetDistance = min(size.width, size.height) * 0.55f
                            val progress = (totalDrag / targetDistance).coerceIn(0f, 1f)
                            onCutProgressChange(progress)
                            if (progress >= 0.88f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCutCompleted()
                            }
                        },
                        onDragEnd = {
                            if (cutProgress >= 0.65f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCutCompleted()
                            }
                        }
                    )
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.48f, h * 0.50f)
        val cakeRadius = min(w, h) * 0.34f
        val plateRadius = cakeRadius * 1.24f

        // Plate shadow & ceramic plate
        drawCircle(
            color = Color.Black.copy(alpha = 0.08f),
            radius = plateRadius,
            center = center + Offset(4f, 10f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFF3E7E2)),
                center = center,
                radius = plateRadius
            ),
            radius = plateRadius,
            center = center
        )
        drawCircle(
            color = Color(0xFFE5D2CA),
            radius = plateRadius * 0.90f,
            center = center,
            style = Stroke(width = 3f)
        )

        // Slice wedge spans from -20 degrees to +32 degrees (52-degree wedge)
        val wedgeStartAngle = -20f
        val wedgeSweepAngle = 52f
        val midAngleRad = ((wedgeStartAngle + wedgeSweepAngle / 2f) * PI / 180.0).toFloat()
        val slideDistance = cakeRadius * 0.38f * sliceOffsetFraction
        val sliceDx = cos(midAngleRad) * slideDistance
        val sliceDy = sin(midAngleRad) * slideDistance

        // Main cake (excluding the wedge when cut)
        val mainStart = wedgeStartAngle + wedgeSweepAngle
        val mainSweep = 360f - wedgeSweepAngle

        // Outer cake base shadow
        drawArc(
            color = Color(0xFFDFB8C2),
            startAngle = mainStart,
            sweepAngle = mainSweep,
            useCenter = true,
            topLeft = Offset(center.x - cakeRadius, center.y - cakeRadius + 10f),
            size = Size(cakeRadius * 2f, cakeRadius * 2f)
        )

        // Main cake frosting top
        drawArc(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEEF1), Color(0xFFFAD0DA)),
                center = center,
                radius = cakeRadius
            ),
            startAngle = mainStart,
            sweepAngle = mainSweep,
            useCenter = true,
            topLeft = Offset(center.x - cakeRadius, center.y - cakeRadius),
            size = Size(cakeRadius * 2f, cakeRadius * 2f)
        )

        // Berry glaze ring on main cake
        drawArc(
            color = BerryRose.copy(alpha = 0.85f),
            startAngle = mainStart,
            sweepAngle = mainSweep,
            useCenter = false,
            topLeft = Offset(center.x - cakeRadius * 0.78f, center.y - cakeRadius * 0.78f),
            size = Size(cakeRadius * 1.56f, cakeRadius * 1.56f),
            style = Stroke(width = 16f, cap = StrokeCap.Round)
        )

        // Decorate main cake with rosettes and berries
        for (i in 0 until 8) {
            val deg = mainStart + 24f + i * (mainSweep - 40f) / 7f
            val rad = (deg * PI / 180.0).toFloat()
            val rx = center.x + cos(rad) * cakeRadius * 0.78f
            val ry = center.y + sin(rad) * cakeRadius * 0.78f
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(rx, ry)
            )
            drawCircle(
                color = if (i % 2 == 0) BerryRose else CandleGold,
                radius = 5.5f,
                center = Offset(rx, ry)
            )
        }

        // Now draw the cut slice wedge (either in place or sliding out!)
        translate(left = sliceDx, top = sliceDy) {
            // Sponge layer visible underneath when separated
            if (sliceOffsetFraction > 0.05f) {
                drawArc(
                    color = Color(0xFFF3C58F),
                    startAngle = wedgeStartAngle,
                    sweepAngle = wedgeSweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - cakeRadius, center.y - cakeRadius + 14f),
                    size = Size(cakeRadius * 2f, cakeRadius * 2f)
                )
            }
            // Slice frosting top
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF4F6), Color(0xFFFAC6D3)),
                    center = center,
                    radius = cakeRadius
                ),
                startAngle = wedgeStartAngle,
                sweepAngle = wedgeSweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - cakeRadius, center.y - cakeRadius),
                size = Size(cakeRadius * 2f, cakeRadius * 2f)
            )
            // Slice berry trim & rosette
            drawArc(
                color = BerryRose.copy(alpha = 0.85f),
                startAngle = wedgeStartAngle + 4f,
                sweepAngle = wedgeSweepAngle - 8f,
                useCenter = false,
                topLeft = Offset(center.x - cakeRadius * 0.78f, center.y - cakeRadius * 0.78f),
                size = Size(cakeRadius * 1.56f, cakeRadius * 1.56f),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )
            val berryX = center.x + cos(midAngleRad) * cakeRadius * 0.78f
            val berryY = center.y + sin(midAngleRad) * cakeRadius * 0.78f
            drawCircle(color = Color.White, radius = 12f, center = Offset(berryX, berryY))
            drawCircle(color = BerryRose, radius = 6f, center = Offset(berryX, berryY))
        }

        // Center heart/gold badge when uncut
        if (!isCakeCut) {
            drawCircle(
                color = CandleGold.copy(alpha = 0.9f),
                radius = 12f,
                center = center
            )

            // Dotted guide line for cutting
            val guideAngle1 = (wedgeStartAngle * PI / 180.0).toFloat()
            val guideEnd1 = Offset(
                center.x + cos(guideAngle1) * cakeRadius * 1.05f,
                center.y + sin(guideAngle1) * cakeRadius * 1.05f
            )
            val guideAngle2 = ((wedgeStartAngle + wedgeSweepAngle) * PI / 180.0).toFloat()
            val guideEnd2 = Offset(
                center.x + cos(guideAngle2) * cakeRadius * 1.05f,
                center.y + sin(guideAngle2) * cakeRadius * 1.05f
            )
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
            drawLine(
                color = EspressoInk.copy(alpha = 0.45f),
                start = center,
                end = guideEnd1,
                strokeWidth = 4f,
                pathEffect = dashEffect,
                cap = StrokeCap.Round
            )
            drawLine(
                color = EspressoInk.copy(alpha = 0.45f),
                start = center,
                end = guideEnd2,
                strokeWidth = 4f,
                pathEffect = dashEffect,
                cap = StrokeCap.Round
            )

            // Active progress slice shimmer
            if (cutProgress > 0.02f) {
                val activeEnd = Offset(
                    center.x + cos(guideAngle1) * (cakeRadius * 1.05f * cutProgress),
                    center.y + sin(guideAngle1) * (cakeRadius * 1.05f * cutProgress)
                )
                drawLine(
                    color = BerryRose,
                    start = center,
                    end = activeEnd,
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
                // Little silver knife tip at activeEnd
                val knifePath = Path().apply {
                    moveTo(activeEnd.x, activeEnd.y)
                    lineTo(activeEnd.x + 28f, activeEnd.y - 26f)
                    lineTo(activeEnd.x + 18f, activeEnd.y - 34f)
                    close()
                }
                drawPath(path = knifePath, color = Color(0xFFD8DEE9))
            }
        }
    }
}
