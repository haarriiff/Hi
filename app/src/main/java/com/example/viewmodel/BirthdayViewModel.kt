package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BirthdayRepository
import com.example.data.CelebrationProfileEntity
import com.example.data.LetterSectionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CelebrationStage {
    WELCOME,
    BLOW_CANDLES,
    MAKE_WISH,
    CUT_CAKE,
    LETTER
}

data class CelebrationUiState(
    val stage: CelebrationStage = CelebrationStage.WELCOME,
    val candlesLit: List<Boolean> = List(5) { true },
    val isBlowing: Boolean = false,
    val isMicBlowEnabled: Boolean = false,
    val wishDraft: String = "",
    val wishReleased: Boolean = false,
    val cutProgress: Float = 0f,
    val isCakeCut: Boolean = false,
    val profile: CelebrationProfileEntity = CelebrationProfileEntity(
        id = 1,
        friendName = "Bestie",
        senderName = "Your Forever Friend",
        letterTitle = "For You, On Your Birthday",
        closingLine = "With all the love and cake in the world,"
    ),
    val letterSections: List<LetterSectionEntity> = emptyList(),
    val isLoaded: Boolean = false
) {
    val litCount: Int get() = candlesLit.count { it }
    val allCandlesOut: Boolean get() = candlesLit.none { it }
}

private data class InteractiveState(
    val stage: CelebrationStage = CelebrationStage.WELCOME,
    val candlesLit: List<Boolean> = List(5) { true },
    val isBlowing: Boolean = false,
    val isMicBlowEnabled: Boolean = false,
    val wishDraft: String = "",
    val wishReleased: Boolean = false,
    val cutProgress: Float = 0f,
    val isCakeCut: Boolean = false
)

class BirthdayViewModel(
    private val repository: BirthdayRepository
) : ViewModel() {

    private val interactiveState = MutableStateFlow(InteractiveState())

    val uiState: StateFlow<CelebrationUiState> = combine(
        interactiveState,
        repository.profileFlow,
        repository.sectionsFlow
    ) { interactive, profile, sections ->
        CelebrationUiState(
            stage = interactive.stage,
            candlesLit = interactive.candlesLit,
            isBlowing = interactive.isBlowing,
            isMicBlowEnabled = interactive.isMicBlowEnabled,
            wishDraft = interactive.wishDraft,
            wishReleased = interactive.wishReleased,
            cutProgress = interactive.cutProgress,
            isCakeCut = interactive.isCakeCut,
            profile = profile,
            letterSections = sections,
            isLoaded = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CelebrationUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun startCandleStage() {
        interactiveState.update {
            it.copy(
                stage = CelebrationStage.BLOW_CANDLES,
                candlesLit = List(5) { true },
                isBlowing = false
            )
        }
    }

    fun goBackStage() {
        interactiveState.update { current ->
            val previous = when (current.stage) {
                CelebrationStage.WELCOME -> CelebrationStage.WELCOME
                CelebrationStage.BLOW_CANDLES -> CelebrationStage.WELCOME
                CelebrationStage.MAKE_WISH -> CelebrationStage.BLOW_CANDLES
                CelebrationStage.CUT_CAKE -> CelebrationStage.MAKE_WISH
                CelebrationStage.LETTER -> CelebrationStage.CUT_CAKE
            }
            current.copy(stage = previous, isMicBlowEnabled = false)
        }
    }

    fun extinguishCandle(index: Int) {
        interactiveState.update { current ->
            if (index !in current.candlesLit.indices || !current.candlesLit[index]) {
                current
            } else {
                val updated = current.candlesLit.toMutableList().apply { this[index] = false }
                current.copy(candlesLit = updated)
            }
        }
        checkAllCandlesOut()
    }

    fun setBlowing(blowing: Boolean) {
        interactiveState.update { it.copy(isBlowing = blowing) }
    }

    fun blowNextLitCandle() {
        val currentLit = interactiveState.value.candlesLit
        val litIndices = currentLit.indices.filter { currentLit[it] }
        if (litIndices.isNotEmpty()) {
            // Extinguish from center outward or randomly for natural feel
            val nextIdx = litIndices[litIndices.size / 2]
            extinguishCandle(nextIdx)
        }
    }

    fun blowAllCandlesQuickly() {
        viewModelScope.launch {
            interactiveState.update { it.copy(isBlowing = true) }
            while (interactiveState.value.candlesLit.any { it }) {
                blowNextLitCandle()
                delay(160)
            }
            interactiveState.update { it.copy(isBlowing = false, isMicBlowEnabled = false) }
        }
    }

    fun setMicBlowEnabled(enabled: Boolean) {
        interactiveState.update { it.copy(isMicBlowEnabled = enabled) }
    }

    private fun checkAllCandlesOut() {
        if (interactiveState.value.candlesLit.none { it }) {
            interactiveState.update { it.copy(isBlowing = false, isMicBlowEnabled = false) }
            viewModelScope.launch {
                delay(1100)
                if (interactiveState.value.stage == CelebrationStage.BLOW_CANDLES &&
                    interactiveState.value.candlesLit.none { it }
                ) {
                    interactiveState.update { it.copy(stage = CelebrationStage.MAKE_WISH) }
                }
            }
        }
    }

    fun proceedToMakeWish() {
        interactiveState.update { it.copy(stage = CelebrationStage.MAKE_WISH, isMicBlowEnabled = false) }
    }

    fun updateWishDraft(text: String) {
        interactiveState.update { it.copy(wishDraft = text) }
    }

    fun releaseWishToStars() {
        val wish = interactiveState.value.wishDraft
        interactiveState.update { it.copy(wishReleased = true) }
        viewModelScope.launch {
            if (wish.isNotBlank()) {
                repository.saveWish(wish)
            }
            delay(1300)
            interactiveState.update {
                it.copy(
                    stage = CelebrationStage.CUT_CAKE,
                    cutProgress = 0f,
                    isCakeCut = false
                )
            }
        }
    }

    fun skipToCutCake() {
        interactiveState.update {
            it.copy(
                stage = CelebrationStage.CUT_CAKE,
                cutProgress = 0f,
                isCakeCut = false
            )
        }
    }

    fun updateCutProgress(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        interactiveState.update { current ->
            if (current.isCakeCut) current
            else current.copy(
                cutProgress = maxOf(current.cutProgress, clamped),
                isCakeCut = clamped >= 0.88f
            )
        }
    }

    fun completeCakeCut() {
        interactiveState.update { it.copy(cutProgress = 1f, isCakeCut = true) }
    }

    fun openLetter() {
        interactiveState.update { it.copy(stage = CelebrationStage.LETTER) }
    }

    fun replaySurprise() {
        interactiveState.update {
            InteractiveState(stage = CelebrationStage.WELCOME)
        }
    }

    fun addPhoto(uri: Uri, caption: String, insertAtOrder: Int?) {
        viewModelScope.launch {
            repository.addPickedPhoto(uri, caption, insertAtOrder)
        }
    }

    fun addParagraph(text: String, insertAtOrder: Int? = null) {
        viewModelScope.launch {
            repository.addParagraph(text, insertAtOrder)
        }
    }

    fun updateSection(section: LetterSectionEntity) {
        viewModelScope.launch {
            repository.updateSection(section)
        }
    }

    fun deleteSection(section: LetterSectionEntity) {
        viewModelScope.launch {
            repository.deleteSection(section)
        }
    }

    fun updateProfile(
        friendName: String,
        senderName: String,
        letterTitle: String,
        closingLine: String
    ) {
        viewModelScope.launch {
            repository.updateProfile(friendName, senderName, letterTitle, closingLine)
        }
    }

    class Factory(private val repository: BirthdayRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BirthdayViewModel(repository) as T
        }
    }
}
