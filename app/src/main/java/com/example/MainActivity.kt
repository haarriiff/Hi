package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BirthdayDatabase
import com.example.data.BirthdayRepository
import com.example.ui.components.PersonalizeGiftSheet
import com.example.ui.screens.BirthdayLetterScreen
import com.example.ui.screens.BlowCandlesScreen
import com.example.ui.screens.CutCakeScreen
import com.example.ui.screens.MakeWishScreen
import com.example.ui.screens.WelcomeWishScreen
import com.example.ui.theme.BirthdayGiftTheme
import com.example.viewmodel.BirthdayViewModel
import com.example.viewmodel.CelebrationStage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = BirthdayDatabase.getInstance(applicationContext)
        val repository = BirthdayRepository(
            context = applicationContext,
            dao = database.birthdayDao()
        )

        setContent {
            BirthdayGiftTheme {
                val viewModel: BirthdayViewModel = viewModel(
                    factory = BirthdayViewModel.Factory(repository)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var showPersonalizeSheet by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = uiState.stage,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(420)) +
                                scaleIn(initialScale = 0.96f, animationSpec = tween(420)))
                                .togetherWith(fadeOut(animationSpec = tween(300)))
                        },
                        label = "celebration_stage_transition"
                    ) { stage ->
                        when (stage) {
                            CelebrationStage.WELCOME -> {
                                WelcomeWishScreen(
                                    friendName = uiState.profile.friendName,
                                    onStartCelebration = { viewModel.startCandleStage() },
                                    onOpenPersonalize = { showPersonalizeSheet = true }
                                )
                            }

                            CelebrationStage.BLOW_CANDLES -> {
                                BlowCandlesScreen(
                                    friendName = uiState.profile.friendName,
                                    candlesLit = uiState.candlesLit,
                                    isBlowing = uiState.isBlowing,
                                    isMicBlowEnabled = uiState.isMicBlowEnabled,
                                    onBack = { viewModel.goBackStage() },
                                    onTapCandle = { idx -> viewModel.extinguishCandle(idx) },
                                    onSetBlowing = { blowing -> viewModel.setBlowing(blowing) },
                                    onBlowNextCandle = { viewModel.blowNextLitCandle() },
                                    onBlowAllQuickly = { viewModel.blowAllCandlesQuickly() },
                                    onSetMicBlowEnabled = { enabled -> viewModel.setMicBlowEnabled(enabled) }
                                )
                            }

                            CelebrationStage.MAKE_WISH -> {
                                MakeWishScreen(
                                    friendName = uiState.profile.friendName,
                                    wishDraft = uiState.wishDraft,
                                    wishReleased = uiState.wishReleased,
                                    onWishChange = { viewModel.updateWishDraft(it) },
                                    onSendWish = { viewModel.releaseWishToStars() },
                                    onSkipToCake = { viewModel.skipToCutCake() },
                                    onBack = { viewModel.goBackStage() }
                                )
                            }

                            CelebrationStage.CUT_CAKE -> {
                                CutCakeScreen(
                                    friendName = uiState.profile.friendName,
                                    cutProgress = uiState.cutProgress,
                                    isCakeCut = uiState.isCakeCut,
                                    onCutProgressChange = { viewModel.updateCutProgress(it) },
                                    onCutCompleted = { viewModel.completeCakeCut() },
                                    onOpenLetter = { viewModel.openLetter() },
                                    onBack = { viewModel.goBackStage() }
                                )
                            }

                            CelebrationStage.LETTER -> {
                                BirthdayLetterScreen(
                                    profile = uiState.profile,
                                    sections = uiState.letterSections,
                                    onBack = { viewModel.goBackStage() },
                                    onReplaySurprise = { viewModel.replaySurprise() },
                                    onOpenPersonalize = { showPersonalizeSheet = true },
                                    onAddPhoto = { uri, caption, order ->
                                        viewModel.addPhoto(uri, caption, order)
                                    },
                                    onAddParagraph = { text ->
                                        viewModel.addParagraph(text)
                                    },
                                    onUpdateSection = { section ->
                                        viewModel.updateSection(section)
                                    },
                                    onDeleteSection = { section ->
                                        viewModel.deleteSection(section)
                                    }
                                )
                            }
                        }
                    }

                    if (showPersonalizeSheet) {
                        PersonalizeGiftSheet(
                            profile = uiState.profile,
                            onDismiss = { showPersonalizeSheet = false },
                            onSave = { friend, sender, title, closing ->
                                viewModel.updateProfile(friend, sender, title, closing)
                            }
                        )
                    }
                }
            }
        }
    }
}
