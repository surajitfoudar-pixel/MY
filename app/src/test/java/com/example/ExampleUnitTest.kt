package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBasicAddition() {
        val viewModel = CalculatorViewModel()
        assertEquals("0", viewModel.uiState.value.displayValue)
        
        viewModel.onDigitClick("2")
        viewModel.onDigitClick("5")
        assertEquals("25", viewModel.uiState.value.displayValue)
        
        viewModel.onOperatorClick("+")
        assertEquals("25", viewModel.uiState.value.displayValue)
        assertEquals("25 +", viewModel.uiState.value.expression)
        
        viewModel.onDigitClick("5")
        assertEquals("5", viewModel.uiState.value.displayValue)
        
        viewModel.onEqualClick()
        assertEquals("30", viewModel.uiState.value.displayValue)
        assertEquals("25 + 5 =", viewModel.uiState.value.expression)
    }

    @Test
    fun testDecimalAndToggleSign() {
        val viewModel = CalculatorViewModel()
        viewModel.onDigitClick("9")
        viewModel.onDecimalClick()
        viewModel.onDigitClick("5")
        assertEquals("9.5", viewModel.uiState.value.displayValue)
        
        viewModel.onToggleSignClick()
        assertEquals("-9.5", viewModel.uiState.value.displayValue)
        
        viewModel.onToggleSignClick()
        assertEquals("9.5", viewModel.uiState.value.displayValue)
    }

    @Test
    fun testClearOperations() {
        val viewModel = CalculatorViewModel()
        viewModel.onDigitClick("1")
        viewModel.onDigitClick("2")
        assertEquals("12", viewModel.uiState.value.displayValue)
        
        // C clears current input but keeps state
        viewModel.onClearClick()
        assertEquals("0", viewModel.uiState.value.displayValue)
        
        viewModel.onDigitClick("5")
        viewModel.onOperatorClick("+")
        viewModel.onDigitClick("5")
        viewModel.onEqualClick()
        assertEquals("10", viewModel.uiState.value.displayValue)
        
        // AC clears everything
        viewModel.onClearClick()
        assertEquals("0", viewModel.uiState.value.displayValue)
        assertEquals("", viewModel.uiState.value.expression)
    }

    @Test
    fun testSequentialCalculations() {
        val viewModel = CalculatorViewModel()
        viewModel.onDigitClick("5")
        viewModel.onOperatorClick("+")
        viewModel.onDigitClick("3")
        // Pressing another operator performs the pending operation first (5 + 3 = 8)
        viewModel.onOperatorClick("×")
        assertEquals("8", viewModel.uiState.value.displayValue)
        assertEquals("8 ×", viewModel.uiState.value.expression)
        
        viewModel.onDigitClick("4")
        viewModel.onEqualClick()
        assertEquals("32", viewModel.uiState.value.displayValue)
        assertEquals("8 × 4 =", viewModel.uiState.value.expression)
    }

    @Test
    fun testHistoryFlow() {
        val viewModel = CalculatorViewModel()
        assertTrue(viewModel.uiState.value.history.isEmpty())
        
        viewModel.onDigitClick("1")
        viewModel.onOperatorClick("-")
        viewModel.onDigitClick("1")
        viewModel.onEqualClick()
        
        val history = viewModel.uiState.value.history
        assertEquals(1, history.size)
        assertEquals("1 - 1", history[0].expression)
        assertEquals("0", history[0].result)
    }
}
