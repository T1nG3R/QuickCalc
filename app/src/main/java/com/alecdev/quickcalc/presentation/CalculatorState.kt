package com.alecdev.quickcalc.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class HistoryItem(
    val expression: String,
    val result: String
)

class CalculatorState {
    private var expression by mutableStateOf("")
    var display by mutableStateOf("")
    val history = mutableStateListOf<HistoryItem>()

    fun updateExpression(expr: String) {
        expression = expr
        updateDisplay()
    }

    fun setHistory(items: List<HistoryItem>) {
        history.clear()
        history.addAll(items)
    }

    fun onInput(input: String) {
        expression = CalculatorEngine.applyInput(expression, input)
        updateDisplay()
    }

    fun onOperation(op: String) {
        expression = CalculatorEngine.applyOperation(expression, op)
        updateDisplay()
    }

    fun onCalculate() {
        val cleanExpr = CalculatorEngine.trimTrailingOperators(expression)
        val (result, isSuccess) = CalculatorEngine.applyCalculate(expression)
        if (isSuccess) {
            if (cleanExpr.isNotEmpty() && cleanExpr != result && result.isNotEmpty()) {
                history.add(HistoryItem(expression = cleanExpr, result = result))
            }
            display = result
            expression = result
        } else if (result == "Error") {
            display = "Error"
            expression = "Error"
        }
    }

    fun onClear() {
        expression = CalculatorEngine.applyClear()
        updateDisplay()
    }

    fun onDelete() {
        expression = CalculatorEngine.applyDelete(expression)
        updateDisplay()
    }

    fun onReciprocal() {
        expression = CalculatorEngine.applyReciprocal(expression)
        updateDisplay()
    }

    private fun updateDisplay() {
        display = expression
    }
}
