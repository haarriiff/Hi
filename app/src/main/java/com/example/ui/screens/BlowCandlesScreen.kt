package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.CandleCakeCanvas
import com.example.ui.theme.CandleGold
import com.example.ui.theme.CandleGoldSoft
import com.example.ui.theme.MidnightPlum
import com.example.ui.theme.MidnightSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

@Composable
fun BlowCandlesScreen(
    friendName: String,
    candlesLit: List<Boolean>,
    isBlowing: Boolean,
    isMicBlowEnabled: Boolean,
    onBack: () -> Unit,
    onTapCandle: (Int) -> Unit,
    onSetBlowing: (Boolean) -> Unit,
    onBlowNextCandle: () -> Unit,
    onBlowAllQuickly: () -> Unit,
    onSetMicBlowEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var micNotice by remember { mutableStateOf<String?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micNotice = null
            onSetMicBlowEnabled(true)
        } else {
            onSetMicBlowEnabled(false)
            micNotice = "Mic permission skipped — tap any candle or hold Blow!"
        }
    }

    // While user holds "Hold to Blow", gently blow out candles one by one
    LaunchedEffect(isBlowing) {
        if (isBlowing) {
            while (isActive && isBlowing) {
                delay(280)
                onBlowNextCandle()
            }
        }
    }

    // Optional real microphone blow detection when enabled
    LaunchedEffect(isMicBlowEnabled) {
        if (isMicBlowEnabled) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                listenForBlowAmplitude(
                    onBlowDetected = {
                        onSetBlowing(true)
                        onBlowNextCandle()
                    },
                    onSilence = {
                        onSetBlowing(false)
                    }
                )
            } else {
                onSetMicBlowEnabled(false)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onSetMicBlowEnabled(false)
            onSetBlowing(false)
        }
    }

    val litCount = candlesLit.count { it }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MidnightPlum,
                        Color(0xFF24141B),
                        MidnightPlum
                    )
                )
            )
    ) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("candles_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MidnightSurface
                ) {
                    Text(
                        text = if (litCount > 0) "$litCount candles glowing" else "All candles blown out!",
                        style = MaterialTheme.typography.labelLarge,
                        color = CandleGoldSoft,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (litCount > 0) {
                    stringResource(R.string.stage_candles_title)
                } else {
                    "Yay, $friendName!"
                },
                style = MaterialTheme.typography.displayMedium,
                color = Color(0xFFFFF6EE),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (litCount > 0) {
                    stringResource(R.string.stage_candles_subtitle)
                } else {
                    "Every single candle went out—get ready for your birthday wish…"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD7C2C6),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CandleCakeCanvas(
                candlesLit = candlesLit,
                isBlowing = isBlowing,
                onTapCandle = onTapCandle,
                contentDescriptionText = stringResource(R.string.cd_cake_candles),
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .testTag("candle_cake_canvas")
            )

            if (micNotice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = micNotice!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CandleGoldSoft,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = true))
            Spacer(modifier = Modifier.height(16.dp))

            if (litCount > 0) {
                // Hold to blow primary tactile button
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = if (isBlowing) CandleGold else MaterialTheme.colorScheme.primary,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp)
                        .height(58.dp)
                        .testTag("hold_to_blow_button")
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetBlowing(true)
                                    val released = tryAwaitRelease()
                                    onSetBlowing(false)
                                    if (released) {
                                        onBlowNextCandle()
                                    }
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Air,
                            contentDescription = null,
                            tint = if (isBlowing) MidnightPlum else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBlowing) "Blowing…" else stringResource(R.string.stage_candles_hold_blow),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isBlowing) MidnightPlum else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isMicBlowEnabled) {
                                onSetMicBlowEnabled(false)
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    onSetMicBlowEnabled(true)
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("mic_blow_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isMicBlowEnabled) Icons.Filled.Mic else Icons.Outlined.MicNone,
                            contentDescription = null,
                            tint = CandleGoldSoft,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMicBlowEnabled) {
                                stringResource(R.string.stage_candles_mic_on)
                            } else {
                                stringResource(R.string.stage_candles_mic_off)
                            },
                            color = CandleGoldSoft,
                            maxLines = 1
                        )
                    }

                    FilledTonalButton(
                        onClick = onBlowAllQuickly,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("blow_all_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Whoosh All!")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun listenForBlowAmplitude(
    onBlowDetected: () -> Unit,
    onSilence: () -> Unit
) = withContext(Dispatchers.IO) {
    val sampleRate = 16000
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(2048)

    val recorder = runCatching {
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
    }.getOrNull() ?: return@withContext

    if (recorder.state != AudioRecord.STATE_INITIALIZED) {
        recorder.release()
        return@withContext
    }

    val buffer = ShortArray(bufferSize / 2)
    try {
        recorder.startRecording()
        var lastBlowTime = 0L
        while (isActive) {
            val read = recorder.read(buffer, 0, buffer.size)
            if (read > 0) {
                var sum = 0.0
                for (i in 0 until read) {
                    val sample = buffer[i].toDouble()
                    sum += sample * sample
                }
                val rms = sqrt(sum / read)
                val now = System.currentTimeMillis()
                if (rms > 2400.0) {
                    if (now - lastBlowTime > 320L) {
                        lastBlowTime = now
                        withContext(Dispatchers.Main) { onBlowDetected() }
                    }
                } else if (now - lastBlowTime > 500L) {
                    withContext(Dispatchers.Main) { onSilence() }
                }
            }
            delay(80)
        }
    } finally {
        runCatching { recorder.stop() }
        recorder.release()
    }
}
