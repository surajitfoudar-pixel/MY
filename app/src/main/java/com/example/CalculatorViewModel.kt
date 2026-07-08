package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
    val displayValue: String = "0",
    val expression: String = "",
    val history: List<CalculationHistoryItem> = emptyList(),
    val isHistoryExpanded: Boolean = false
)

data class CalculationHistoryItem(
    val id: Long,
    val expression: String,
    val result: String
)

class CalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var operand1: Double? = null
    private var pendingOperator: String? = null
    private var isOperatorClicked = false
    private var isResultShown = false
    private var historyIdCounter = 0L

    fun onDigitClick(digit: String) {
        _uiState.update { currentState ->
            val newValue = when {
                currentState.displayValue == "0" && digit != "." -> digit
                isOperatorClicked -> {
                    isOperatorClicked = false
                    digit
                }
                isResultShown -> {
                    isResultShown = false
                    operand1 = null
                    pendingOperator = null
                    digit
                }
                else -> currentState.displayValue + digit
            }
            currentState.copy(displayValue = newValue)
        }
    }

    fun onDecimalClick() {
        _uiState.update { currentState ->
            val currentValue = currentState.displayValue
            val newValue = when {
                isOperatorClicked -> {
                    isOperatorClicked = false
                    "0."
                }
                isResultShown -> {
                    isResultShown = false
                    operand1 = null
                    pendingOperator = null
                    "0."
                }
                currentValue.contains(".") -> currentValue
                else -> currentValue + "."
            }
            currentState.copy(displayValue = newValue)
        }
    }

    fun onOperatorClick(operator: String) {
        val currentValue = _uiState.value.displayValue.toDoubleOrNull() ?: return
        
        if (pendingOperator != null && !isOperatorClicked && !isResultShown) {
            // Sequential operation: e.g. 5 + 3 + -> performs 5 + 3 = 8, then prepares +
            val op1 = operand1 ?: 0.0
            val res = performCalculation(op1, currentValue, pendingOperator!!)
            val formattedRes = formatResult(res)
            
            operand1 = res
            pendingOperator = operator
            isOperatorClicked = true
            isResultShown = false
            
            _uiState.update { currentState ->
                currentState.copy(
                    displayValue = formattedRes,
                    expression = "${formatResult(res)} $operator"
                )
            }
        } else {
            operand1 = currentValue
            pendingOperator = operator
            isOperatorClicked = true
            isResultShown = false
            
            _uiState.update { currentState ->
                currentState.copy(
                    expression = "${formatResult(currentValue)} $operator"
                )
            }
        }
    }

    fun onEqualClick() {
        val op1 = operand1 ?: return
        val op2 = _uiState.value.displayValue.toDoubleOrNull() ?: return
        val op = pendingOperator ?: return
        
        val res = performCalculation(op1, op2, op)
        val formattedRes = formatResult(res)
        
        val fullExpression = "${formatResult(op1)} $op ${formatResult(op2)}"
        
        val historyItem = CalculationHistoryItem(
            id = ++historyIdCounter,
            expression = fullExpression,
            result = formattedRes
        )
        
        operand1 = res
        pendingOperator = null
        isOperatorClicked = false
        isResultShown = true
        
        _uiState.update { currentState ->
            currentState.copy(
                displayValue = formattedRes,
                expression = "$fullExpression =",
                history = listOf(historyItem) + currentState.history
            )
        }
    }

    fun onClearClick() {
        _uiState.update { currentState ->
            if (currentState.displayValue != "0" && !isResultShown && !isOperatorClicked) {
                // Clear current input only ("C" behavior)
                currentState.copy(displayValue = "0")
            } else {
                // Reset everything ("AC" behavior)
                operand1 = null
                pendingOperator = null
                isOperatorClicked = false
                isResultShown = false
                currentState.copy(displayValue = "0", expression = "")
            }
        }
    }

    fun onBackspaceClick() {
        _uiState.update { currentState ->
            if (isResultShown || isOperatorClicked) {
                currentState
            } else {
                val currentValue = currentState.displayValue
                val newValue = if (currentValue.length <= 1) {
                    "0"
                } else {
                    currentValue.substring(0, currentValue.length - 1)
                }
                currentState.copy(displayValue = newValue)
            }
        }
    }

    fun onPercentClick() {
        val currentValue = _uiState.value.displayValue.toDoubleOrNull() ?: return
        
        val newValue = if (operand1 != null && pendingOperator != null) {
            // Percentage of the first operand, e.g. 100 + 10% -> 10% becomes 10 (10% of 100)
            val base = operand1!!
            (base * currentValue) / 100.0
        } else {
            // Standard division by 100, e.g. 8% -> 0.08
            currentValue / 100.0
        }
        
        val formatted = formatResult(newValue)
        _uiState.update { currentState ->
            currentState.copy(displayValue = formatted)
        }
    }

    fun onToggleSignClick() {
        val currentValue = _uiState.value.displayValue.toDoubleOrNull() ?: return
        val toggled = currentValue * -1.0
        val formatted = formatResult(toggled)
        _uiState.update { currentState ->
            currentState.copy(displayValue = formatted)
        }
    }

    fun toggleHistory() {
        _uiState.update { currentState ->
            currentState.copy(isHistoryExpanded = !currentState.isHistoryExpanded)
        }
    }

    fun selectHistoryItem(item: CalculationHistoryItem) {
        val res = item.result.toDoubleOrNull() ?: return
        operand1 = res
        pendingOperator = null
        isOperatorClicked = false
        isResultShown = true
        
        _uiState.update { currentState ->
            currentState.copy(
                displayValue = item.result,
                expression = "${item.expression} =",
                isHistoryExpanded = false
            )
        }
    }

    fun clearHistory() {
        _uiState.update { currentState ->
            currentState.copy(history = emptyList())
        }
    }

    private fun performCalculation(op1: Double, op2: Double, operator: String): Double {
        return when (operator) {
            "+" -> op1 + op2
            "-" -> op1 - op2
            "×" -> op1 * op2
            "÷" -> {
                if (op2 == 0.0) Double.NaN else op1 / op2
            }
            else -> op2
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isInfinite() || value.isNaN()) return "Error"
        if (value == value.toLong().toDouble()) {
            return value.toLong().toString()
        }
        
        val formatted = String.format(java.util.Locale.US, "%.10f", value)
        var result = formatted
        if (result.contains(".")) {
            result = result.trimEnd('0').trimEnd('.')
        }
        if (result.length > 12) {
            val expFormatted = String.format(java.util.Locale.US, "%.6e", value)
            return expFormatted
        }
        return result
    }
}
