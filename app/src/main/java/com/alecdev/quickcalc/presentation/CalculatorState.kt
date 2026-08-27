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
        if (input == "." && (expression.lastOrNull()?.isDigit() != true || CalculatorEngine.lastNumberContainsDecimal(expression))) {
            return
        }

        expression += input
        updateDisplay()
    }

    fun onOperation(op: String) {
        val sanitizedOp = if (op == "−") "-" else op

        if (expression.isEmpty() && sanitizedOp == "-") {
            expression += sanitizedOp
            updateDisplay()
            return
        }

        if (sanitizedOp == "-" && CalculatorEngine.isLastCharOperation(expression) && expression.last() != '-') {
            expression += sanitizedOp
            updateDisplay()
            return
        }

        if (expression.isNotEmpty() && !CalculatorEngine.isLastCharOperation(expression)) {
            expression += sanitizedOp
            updateDisplay()
        }
    }

    fun onCalculate() {
        try {
            val output = CalculatorEngine.evaluate(expression)
            if (expression.isNotEmpty() && expression != output && output.isNotEmpty()) {
                history.add(HistoryItem(expression = expression, result = output))
            }
            display = output
            expression = display
        } catch (e: Exception) {
            // do nothing on error
        }
    }

    fun onClear() {
        expression = ""
        updateDisplay()
    }

    fun onDelete() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            updateDisplay()
        }
    }

    fun onReciprocal() {
        if (expression.isEmpty()) {
            expression = "1/"
        } else if (expression == "1/") {
            expression = ""
        } else {
            expression = "1/($expression)"
        }
        updateDisplay()
    }

    private fun updateDisplay() {
        display = expression
    }
}