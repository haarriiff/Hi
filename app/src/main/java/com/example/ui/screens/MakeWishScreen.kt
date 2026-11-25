package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CandleGold
import com.example.ui.theme.CandleGoldSoft
import com.example.ui.theme.CaveatFamily
import com.example.ui.theme.MidnightPlum
import com.example.ui.theme.MidnightSurface
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MakeWishScreen(
    friendName: String,
    wishDraft: String,
    wishReleased: Boolean,
    onWishChange: (String) -> Unit,
    onSendWish: () -> Unit,
    onSkipToCake: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_phase"
    )

    val stars = remember {
        val rng = Random(99)
        List(36) {
            Triple(rng.nextFloat(), rng.nextFloat() * 0.72f, 2f + rng.nextFloat() * 3.5f)
        }
    }

    val wishLiftY by animateFloatAsState(
        targetValue = if (wishReleased) -110f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "wish_lift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF130B14),
                        MidnightPlum,
                        Color(0xFF281722)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEachIndexed { idx, (xf, yf, r) ->
                val twinkle = 0.35f + 0.65f * ((sin(starPhase + idx) + 1f) / 2f)
                drawCircle(
                    color = CandleGoldSoft.copy(alpha = twinkle),
                    radius = r * density,
                    center = Offset(xf * size.width, yf * size.height)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .size(84.dp)
                    .offset(y = wishLiftY.dp),
                shape = CircleShape,
                color = CandleGold.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = CandleGold,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.stage_wish_title),
                style = MaterialTheme.typography.displayMedium,
                color = Color(0xFFFFF6EE),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Everything you hope for this year, $friendName—keep it in your heart or seal it into a wish star.",
                fontFamily = CaveatFamily,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                color = CandleGoldSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 460.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = wishReleased,
                enter = fadeIn() + scaleIn()
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MidnightSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stage_wish_sent),
                        style = MaterialTheme.typography.titleMedium,
                        color = CandleGoldSoft,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            if (!wishReleased) {
                OutlinedTextField(
                    value = wishDraft,
                    onValueChange = onWishChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.stage_wish_hint),
                            color = Color(0xFFA79196)
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CandleGold,
                        unfocusedBorderColor = Color(0xFF5D434A),
                        focusedContainerColor = MidnightSurface.copy(alpha = 0.7f),
                        unfocusedContainerColor = MidnightSurface.copy(alpha = 0.55f),
                        cursorColor = CandleGold
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp)
                        .testTag("wish_text_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSendWish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                        .height(56.dp)
                        .testTag("send_wish_button"),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandleGold,
                        contentColor = MidnightPlum
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stage_wish_send),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onSkipToCake,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("skip_wish_button")
                ) {
                    Text(
                        text = stringResource(R.string.stage_wish_skip),
                        color = CandleGoldSoft
                    )
                }
            }
        }
    }
}
