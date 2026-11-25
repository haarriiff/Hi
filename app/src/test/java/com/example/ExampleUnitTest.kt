package com.example

import com.example.viewmodel.CelebrationStage
import com.example.viewmodel.CelebrationUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun celebration_initial_state_has_five_lit_candles() {
        val state = CelebrationUiState()
        assertEquals(CelebrationStage.WELCOME, state.stage)
        assertEquals(5, state.litCount)
        assertFalse(state.allCandlesOut)
    }

    @Test
    fun celebration_all_candles_out_detected_correctly() {
        val state = CelebrationUiState(
            stage = CelebrationStage.BLOW_CANDLES,
            candlesLit = listOf(false, false, false, false, false)
        )
        assertEquals(0, state.litCount)
        assertTrue(state.allCandlesOut)
    }
}
