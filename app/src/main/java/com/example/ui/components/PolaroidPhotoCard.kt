package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.LetterSectionEntity
import com.example.ui.theme.BerryRose
import com.example.ui.theme.CandleGold
import com.example.ui.theme.CaveatFamily
import com.example.ui.theme.CocoaMuted
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.SageAccent
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private enum class ScrapbookFrameStyle {
    SCALLOPED_STAMP,
    PHOTO_CORNERS,
    STITCHED_KRAFT,
    GILDED_MAT
}

@Composable
fun PolaroidPhotoCard(
    section: LetterSectionEntity,
    isEditMode: Boolean,
    onEditCaption: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isRevealed by rememberSaveable(section.id) { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0.84f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 160f
        ),
        label = "scrapbook_card_zoom"
    )

    val photoZoom by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0.86f,
        animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing),
        label = "scrapbook_photo_zoom"
    )

    val photoAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        label = "scrapbook_photo_alpha"
    )

    val veilAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 1f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "scrapbook_veil_alpha"
    )

    val imageModel: Any = when (section.photoSource) {
        "drawable:img_memory_cafe" -> R.drawable.img_memory_cafe
        "drawable:img_memory_picnic" -> R.drawable.img_memory_picnic
        "drawable:img_memory_sparklers" -> R.drawable.img_memory_sparklers
        else -> File(section.photoSource)
    }

    val styles = ScrapbookFrameStyle.entries
    val frameStyle = styles[abs((section.id + section.sortOrder).toInt()) % styles.size]

    val frameBackground = when (frameStyle) {
        ScrapbookFrameStyle.SCALLOPED_STAMP -> Color(0xFFFFFDF8)
        ScrapbookFrameStyle.PHOTO_CORNERS -> Color(0xFFFFFBF3)
        ScrapbookFrameStyle.STITCHED_KRAFT -> Color(0xFFF3E3CD)
        ScrapbookFrameStyle.GILDED_MAT -> Color(0xFFFFF1EE)
    }

    val frameBorderColor = when (frameStyle) {
        ScrapbookFrameStyle.SCALLOPED_STAMP -> Color(0xFFE7D3C9)
        ScrapbookFrameStyle.PHOTO_CORNERS -> Color(0xFFE2D3C5)
        ScrapbookFrameStyle.STITCHED_KRAFT -> Color(0xFFD5BC9D)
        ScrapbookFrameStyle.GILDED_MAT -> CandleGold.copy(alpha = 0.65f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .rotate(section.tiltDegrees)
                .clip(
                    RoundedCornerShape(
                        when (frameStyle) {
                            ScrapbookFrameStyle.SCALLOPED_STAMP -> 10.dp
                            ScrapbookFrameStyle.PHOTO_CORNERS -> 4.dp
                            ScrapbookFrameStyle.STITCHED_KRAFT -> 8.dp
                            ScrapbookFrameStyle.GILDED_MAT -> 12.dp
                        }
                    )
                )
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (isRevealed) "Hide memory photo" else "Reveal memory photo"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isRevealed = !isRevealed
                }
                .testTag("polaroid_card_${section.id}"),
            shape = RoundedCornerShape(
                when (frameStyle) {
                    ScrapbookFrameStyle.SCALLOPED_STAMP -> 10.dp
                    ScrapbookFrameStyle.PHOTO_CORNERS -> 4.dp
                    ScrapbookFrameStyle.STITCHED_KRAFT -> 8.dp
                    ScrapbookFrameStyle.GILDED_MAT -> 12.dp
                }
            ),
            color = frameBackground,
            shadowElevation = if (isRevealed) 8.dp else 4.dp,
            border = BorderStroke(1.2.dp, frameBorderColor)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Decorative outer scrapbook border artwork drawn behind the card content
                ScrapbookOuterBorderCanvas(
                    style = frameStyle,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 18.dp,
                            end = 18.dp,
                            top = 20.dp,
                            bottom = 18.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Inner photo mat + framed photo aperture
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (frameStyle == ScrapbookFrameStyle.GILDED_MAT) {
                                    Color(0xFFFFFDF9)
                                } else {
                                    Color.White
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = if (frameStyle == ScrapbookFrameStyle.GILDED_MAT) 1.5.dp else 1.dp,
                                color = if (frameStyle == ScrapbookFrameStyle.GILDED_MAT) {
                                    CandleGold.copy(alpha = 0.75f)
                                } else {
                                    Color(0xFFE6DDD6)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(if (frameStyle == ScrapbookFrameStyle.GILDED_MAT) 7.dp else 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = if (isRevealed) {
                                    section.photoCaption.ifBlank {
                                        stringResource(R.string.cd_memory_photo)
                                    }
                                } else {
                                    "Hidden scrapbook photo. Tap to reveal."
                                },
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = photoZoom
                                        scaleY = photoZoom
                                        alpha = photoAlpha
                                    }
                            )

                            // Hidden linen memory cover shown before tapping to reveal
                            if (veilAlpha > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = veilAlpha }
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFF6E7DF),
                                                    Color(0xFFFCEFE9),
                                                    Color(0xFFF4DFD8)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = BerryRose.copy(alpha = 0.12f),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.PhotoLibrary,
                                                    contentDescription = null,
                                                    tint = BerryRose,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Hidden Memory",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EspressoInk
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AutoAwesome,
                                                contentDescription = null,
                                                tint = CandleGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Tap to unwrap & zoom in",
                                                fontFamily = CaveatFamily,
                                                fontSize = 20.sp,
                                                color = CocoaMuted
                                            )
                                        }
                                    }
                                }
                            }

                            // Inner frame overlays (photo mounting corners, inner stitch, or gilded corner flourishes)
                            ScrapbookPhotoOverlayCanvas(
                                style = frameStyle,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimatedContent(
                        targetState = isRevealed,
                        transitionSpec = {
                            fadeIn(tween(320)) togetherWith fadeOut(tween(220))
                        },
                        label = "caption_reveal"
                    ) { revealed ->
                        Text(
                            text = if (revealed) section.photoCaption else "Sealed memory — tap to reveal",
                            fontFamily = CaveatFamily,
                            fontSize = 23.sp,
                            lineHeight = 27.sp,
                            color = if (revealed) EspressoInk else CocoaMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp)
                        )
                    }

                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onEditCaption,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("edit_photo_${section.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.cd_edit_item),
                                    tint = CocoaMuted
                                )
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("delete_photo_${section.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.cd_delete_item),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Decorative patterned washi tape strips holding each scrapbook photo onto the letter
        when (frameStyle) {
            ScrapbookFrameStyle.SCALLOPED_STAMP,
            ScrapbookFrameStyle.GILDED_MAT -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.84f)
                        .offset(y = (-8).dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WashiTapeStrip(
                        baseColor = if (frameStyle == ScrapbookFrameStyle.SCALLOPED_STAMP) {
                            BerryRose.copy(alpha = 0.62f)
                        } else {
                            CandleGold.copy(alpha = 0.60f)
                        },
                        patternColor = Color.White.copy(alpha = 0.55f),
                        isPolkaDots = frameStyle == ScrapbookFrameStyle.GILDED_MAT,
                        modifier = Modifier
                            .width(62.dp)
                            .height(20.dp)
                            .rotate(-22f)
                    )
                    WashiTapeStrip(
                        baseColor = if (frameStyle == ScrapbookFrameStyle.SCALLOPED_STAMP) {
                            CandleGold.copy(alpha = 0.58f)
                        } else {
                            BerryRose.copy(alpha = 0.52f)
                        },
                        patternColor = Color.White.copy(alpha = 0.55f),
                        isPolkaDots = frameStyle == ScrapbookFrameStyle.GILDED_MAT,
                        modifier = Modifier
                            .width(62.dp)
                            .height(20.dp)
                            .rotate(20f)
                    )
                }
            }

            ScrapbookFrameStyle.STITCHED_KRAFT -> {
                WashiTapeStrip(
                    baseColor = SageAccent.copy(alpha = 0.65f),
                    patternColor = Color(0xFFFFF6DC).copy(alpha = 0.72f),
                    isPolkaDots = true,
                    modifier = Modifier
                        .offset(y = (-9).dp)
                        .width(86.dp)
                        .height(22.dp)
                        .rotate(-2.5f)
                )
            }

            ScrapbookFrameStyle.PHOTO_CORNERS -> {
                WashiTapeStrip(
                    baseColor = Color(0xFFE996A8).copy(alpha = 0.65f),
                    patternColor = Color.White.copy(alpha = 0.55f),
                    isPolkaDots = false,
                    modifier = Modifier
                        .offset(y = (-9).dp)
                        .width(78.dp)
                        .height(20.dp)
                        .rotate(1.8f)
                )
            }
        }
    }
}

@Composable
private fun ScrapbookOuterBorderCanvas(
    style: ScrapbookFrameStyle,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (style) {
            ScrapbookFrameStyle.SCALLOPED_STAMP -> {
                // Postage-stamp scalloped inner dot frame & hairline gold rule
                val inset = 11.dp.toPx()
                drawRoundRect(
                    color = CandleGold.copy(alpha = 0.55f),
                    topLeft = Offset(inset, inset),
                    size = Size(w - inset * 2f, h - inset * 2f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
                val dotStep = 14.dp.toPx()
                val edgeInset = 5.dp.toPx()
                var x = edgeInset + dotStep * 0.5f
                while (x < w - edgeInset) {
                    drawCircle(
                        color = Color(0xFFE7CFC6),
                        radius = 2.2.dp.toPx(),
                        center = Offset(x, edgeInset)
                    )
                    drawCircle(
                        color = Color(0xFFE7CFC6),
                        radius = 2.2.dp.toPx(),
                        center = Offset(x, h - edgeInset)
                    )
                    x += dotStep
                }
                var y = edgeInset + dotStep * 0.5f
                while (y < h - edgeInset) {
                    drawCircle(
                        color = Color(0xFFE7CFC6),
                        radius = 2.2.dp.toPx(),
                        center = Offset(edgeInset, y)
                    )
                    drawCircle(
                        color = Color(0xFFE7CFC6),
                        radius = 2.2.dp.toPx(),
                        center = Offset(w - edgeInset, y)
                    )
                    y += dotStep
                }
            }

            ScrapbookFrameStyle.STITCHED_KRAFT -> {
                // Hand-stitched dashed thread border along outer kraft cardstock
                val inset = 9.dp.toPx()
                drawRoundRect(
                    color = BerryRose.copy(alpha = 0.60f),
                    topLeft = Offset(inset, inset),
                    size = Size(w - inset * 2f, h - inset * 2f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(9.dp.toPx(), 6.dp.toPx()),
                            0f
                        )
                    )
                )
            }

            ScrapbookFrameStyle.GILDED_MAT -> {
                // Ornate double-line gold foil border + corner star flourishes
                val outerInset = 8.dp.toPx()
                val innerInset = 12.dp.toPx()
                drawRoundRect(
                    color = CandleGold.copy(alpha = 0.72f),
                    topLeft = Offset(outerInset, outerInset),
                    size = Size(w - outerInset * 2f, h - outerInset * 2f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx())
                )
                drawRoundRect(
                    color = BerryRose.copy(alpha = 0.35f),
                    topLeft = Offset(innerInset, innerInset),
                    size = Size(w - innerInset * 2f, h - innerInset * 2f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
                val cornerOffset = 10.dp.toPx()
                listOf(
                    Offset(cornerOffset, cornerOffset),
                    Offset(w - cornerOffset, cornerOffset),
                    Offset(cornerOffset, h - cornerOffset),
                    Offset(w - cornerOffset, h - cornerOffset)
                ).forEach { c ->
                    drawCircle(color = CandleGold, radius = 3.dp.toPx(), center = c)
                }
            }

            ScrapbookFrameStyle.PHOTO_CORNERS -> {
                // Subtle archival mat line + tiny stamped star seal at bottom right
                val inset = 8.dp.toPx()
                drawRect(
                    color = Color(0xFFEADCD0),
                    topLeft = Offset(inset, inset),
                    size = Size(w - inset * 2f, h - inset * 2f),
                    style = Stroke(width = 1.dp.toPx())
                )
                drawMiniStar(
                    center = Offset(w - 22.dp.toPx(), h - 22.dp.toPx()),
                    outerRadius = 7.dp.toPx(),
                    innerRadius = 3.2.dp.toPx(),
                    color = CandleGold.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun ScrapbookPhotoOverlayCanvas(
    style: ScrapbookFrameStyle,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (style) {
            ScrapbookFrameStyle.PHOTO_CORNERS -> {
                // Classic scrapbook triangular photo mounting corners with gold accent edge
                val cSize = 24.dp.toPx()
                val cornerFill = EspressoInk.copy(alpha = 0.86f)
                val cornerAccent = CandleGold.copy(alpha = 0.85f)

                val corners = listOf(
                    // Top-left
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(cSize, 0f)
                        lineTo(0f, cSize)
                        close()
                    } to (Offset(cSize, 0f) to Offset(0f, cSize)),
                    // Top-right
                    Path().apply {
                        moveTo(w, 0f)
                        lineTo(w - cSize, 0f)
                        lineTo(w, cSize)
                        close()
                    } to (Offset(w - cSize, 0f) to Offset(w, cSize)),
                    // Bottom-left
                    Path().apply {
                        moveTo(0f, h)
                        lineTo(cSize, h)
                        lineTo(0f, h - cSize)
                        close()
                    } to (Offset(cSize, h) to Offset(0f, h - cSize)),
                    // Bottom-right
                    Path().apply {
                        moveTo(w, h)
                        lineTo(w - cSize, h)
                        lineTo(w, h - cSize)
                        close()
                    } to (Offset(w - cSize, h) to Offset(w, h - cSize))
                )

                corners.forEach { (triPath, edge) ->
                    drawPath(path = triPath, color = cornerFill)
                    drawLine(
                        color = cornerAccent,
                        start = edge.first,
                        end = edge.second,
                        strokeWidth = 1.6.dp.toPx()
                    )
                }
            }

            ScrapbookFrameStyle.STITCHED_KRAFT -> {
                // Inner cream stitched trim inside the photo aperture
                val inset = 5.dp.toPx()
                drawRect(
                    color = Color.White.copy(alpha = 0.65f),
                    topLeft = Offset(inset, inset),
                    size = Size(w - inset * 2f, h - inset * 2f),
                    style = Stroke(
                        width = 1.4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(6.dp.toPx(), 5.dp.toPx()),
                            0f
                        )
                    )
                )
            }

            ScrapbookFrameStyle.GILDED_MAT -> {
                // Delicate L-shaped gold corner brackets inside the photo edge
                val pad = 7.dp.toPx()
                val arm = 14.dp.toPx()
                val stroke = 1.8.dp.toPx()
                val gold = Color(0xFFFFE59E).copy(alpha = 0.85f)
                // Top-left
                drawLine(gold, Offset(pad, pad), Offset(pad + arm, pad), stroke, StrokeCap.Round)
                drawLine(gold, Offset(pad, pad), Offset(pad, pad + arm), stroke, StrokeCap.Round)
                // Top-right
                drawLine(gold, Offset(w - pad, pad), Offset(w - pad - arm, pad), stroke, StrokeCap.Round)
                drawLine(gold, Offset(w - pad, pad), Offset(w - pad, pad + arm), stroke, StrokeCap.Round)
                // Bottom-left
                drawLine(gold, Offset(pad, h - pad), Offset(pad + arm, h - pad), stroke, StrokeCap.Round)
                drawLine(gold, Offset(pad, h - pad), Offset(pad, h - pad - arm), stroke, StrokeCap.Round)
                // Bottom-right
                drawLine(gold, Offset(w - pad, h - pad), Offset(w - pad - arm, h - pad), stroke, StrokeCap.Round)
                drawLine(gold, Offset(w - pad, h - pad), Offset(w - pad, h - pad - arm), stroke, StrokeCap.Round)
            }

            ScrapbookFrameStyle.SCALLOPED_STAMP -> {
                // Clean crisp inner highlight border
                drawRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun WashiTapeStrip(
    baseColor: Color,
    patternColor: Color,
    isPolkaDots: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Jagged torn-edge washi tape silhouette
        val notch = 3.dp.toPx()
        val tapePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w - notch, h * 0.25f)
            lineTo(w, h * 0.5f)
            lineTo(w - notch, h * 0.75f)
            lineTo(w, h)
            lineTo(0f, h)
            lineTo(notch, h * 0.75f)
            lineTo(0f, h * 0.5f)
            lineTo(notch, h * 0.25f)
            close()
        }
        drawPath(path = tapePath, color = baseColor)

        if (isPolkaDots) {
            val cols = 5
            for (c in 0 until cols) {
                val cx = w * (0.14f + c * 0.18f)
                val cy = if (c % 2 == 0) h * 0.36f else h * 0.64f
                drawCircle(
                    color = patternColor,
                    radius = 2.2.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
        } else {
            val step = 10.dp.toPx()
            var x = -h
            while (x < w + h) {
                val startX = x.coerceIn(2f, w - 2f)
                val endX = (x + h * 0.7f).coerceIn(2f, w - 2f)
                if (endX > startX) {
                    drawLine(
                        color = patternColor,
                        start = Offset(startX, h - 2f),
                        end = Offset(endX, 2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                x += step
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (-PI / 2.0 + i * PI / points).toFloat()
        val px = center.x + cos(angle) * radius
        val py = center.y + sin(angle) * radius
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path = path, color = color)
}
