package com.alecdev.quickcalc

import com.alecdev.quickcalc.presentation.CalculatorEngine
import com.alecdev.quickcalc.presentation.CalculatorState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorStateTest {

    @Test
    fun testInitialState() {
        val state = CalculatorState()
        assertEquals("", state.display)
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun testDigitInput() {
        val state = CalculatorState()
        state.onInput("7")
        assertEquals("7", state.display)
        state.onInput("5")
        assertEquals("75", state.display)
    }

    @Test
    fun testDecimalConstraint() {
        val state = CalculatorState()
        // dot on empty expression automatically inserts "0."
        state.onInput(".")
        assertEquals("0.", state.display)

        // second dot in the same number is ignored
        state.onInput(".")
        assertEquals("0.", state.display)

        state.onInput("5")
        assertEquals("0.5", state.display)

        // second dot after digits is ignored
        state.onInput(".")
        assertEquals("0.5", state.display)

        // dot immediately following an operator inserts "0."
        state.onOperation("+")
        assertEquals("0.5+", state.display)
        state.onInput(".")
        assertEquals("0.5+0.", state.display)

        // second dot in operator number is ignored
        state.onInput(".")
        assertEquals("0.5+0.", state.display)

        state.onInput("3")
        assertEquals("0.5+0.3", state.display)
        state.onCalculate()
        assertEquals("0.8", state.display)
    }

    @Test
    fun testLeadingDecimalDirectInputs() {
        val state = CalculatorState()
        state.onInput("5")
        state.onOperation("−")
        state.onInput(".")
        state.onInput("2")
        assertEquals("5-0.2", state.display)
        state.onCalculate()
        assertEquals("4.8", state.display)
    }

    @Test
    fun testClear() {
        val state = CalculatorState()
        state.onInput("7")
        state.onClear()
        assertEquals("", state.display)
    }

    @Test
    fun testDelete() {
        val state = CalculatorState()
        state.onInput("7")
        state.onInput("5")
        state.onDelete()
        assertEquals("7", state.display)
        state.onDelete()
        assertEquals("", state.display)
    }

    @Test
    fun testBasicOperations() {
        val state = CalculatorState()
        state.onInput("6")
        state.onOperation("÷")
        state.onInput("2")
        assertEquals("6÷2", state.display)
        state.onCalculate()
        assertEquals("3", state.display)
    }

    @Test
    fun testNegativeInput() {
        val state = CalculatorState()
        // minus as first char represents negative number sign
        state.onOperation("−")
        assertEquals("-", state.display)
        state.onInput("5")
        assertEquals("-5", state.display)
        state.onCalculate()
        assertEquals("-5", state.display)
    }

    @Test
    fun testTrailingOperatorCalculation() {
        val state = CalculatorState()
        
        // single number with trailing operator
        state.onInput("5")
        state.onOperation("+")
        assertEquals("5+", state.display)
        state.onCalculate()
        assertEquals("5", state.display)

        // expression with trailing operator
        state.onClear()
        state.onInput("5")
        state.onOperation("−")
        state.onInput("2")
        state.onOperation("+")
        assertEquals("5-2+", state.display)
        state.onCalculate()
        assertEquals("3", state.display)

        // expression with percent or other trailing signs
        assertEquals("3", CalculatorEngine.evaluate("5-2%"))
        assertEquals("3", CalculatorEngine.evaluate("5-2×"))
        assertEquals("3", CalculatorEngine.evaluate("5-2÷"))
        assertEquals("3", CalculatorEngine.evaluate("5-2-"))
        assertEquals("5", CalculatorEngine.evaluate("5+-"))
        assertEquals("5", CalculatorEngine.evaluate("5+("))
        assertEquals("5", CalculatorEngine.evaluate("5+√("))
        assertEquals("3", CalculatorEngine.evaluate("√(9+"))
        assertEquals("5", CalculatorEngine.evaluate("5."))

        // state with unclosed parenthesis and trailing operator
        state.onClear()
        state.onInput("(")
        state.onInput("5")
        state.onOperation("−")
        assertEquals("(5-", state.display)
        state.onCalculate()
        assertEquals("5", state.display)

        // purely operators or empty expression
        val (emptyRes, emptyOk) = CalculatorEngine.applyCalculate("-")
        org.junit.Assert.assertFalse(emptyOk)
        assertEquals("", emptyRes)

        val (plusRes, plusOk) = CalculatorEngine.applyCalculate("+")
        org.junit.Assert.assertFalse(plusOk)
        assertEquals("", plusRes)
    }

    @Test
    fun testSmoothOperatorReplacement() {
        val state = CalculatorState()

        // 5 + -> replace with × -> replace with ÷ -> replace with − -> replace with +
        state.onInput("5")
        state.onOperation("+")
        assertEquals("5+", state.display)

        state.onOperation("×")
        assertEquals("5×", state.display)

        state.onOperation("÷")
        assertEquals("5÷", state.display)

        state.onOperation("−")
        assertEquals("5-", state.display)

        state.onOperation("+")
        assertEquals("5+", state.display)

        state.onInput("3")
        assertEquals("5+3", state.display)
        state.onCalculate()
        assertEquals("8", state.display)

        // Parentheses expression operator replacement: (5+ -> (5×
        state.onClear()
        state.onInput("(")
        state.onInput("5")
        state.onOperation("+")
        assertEquals("(5+", state.display)
        state.onOperation("×")
        assertEquals("(5×", state.display)
        state.onInput("2")
        state.onInput(")")
        assertEquals("(5×2)", state.display)
        state.onCalculate()
        assertEquals("10", state.display)

        // applyOperation unit tests
        assertEquals("5×", CalculatorEngine.applyOperation("5+", "×"))
        assertEquals("5+", CalculatorEngine.applyOperation("5×", "+"))
        assertEquals("5-", CalculatorEngine.applyOperation("5+", "−"))
        assertEquals("5÷", CalculatorEngine.applyOperation("5-", "÷"))
        assertEquals("5×", CalculatorEngine.applyOperation("5+-", "×"))
        assertEquals("(5×", CalculatorEngine.applyOperation("(5+", "×"))
        assertEquals("-", CalculatorEngine.applyOperation("", "−"))
        assertEquals("-", CalculatorEngine.applyOperation("-", "+"))
    }


    @Test
    fun testInvalidExpressionShowsErrorAndRecovers() {
        val state = CalculatorState()
        state.onInput("5")
        state.onOperation("÷")
        state.onInput("0")
        assertEquals("5÷0", state.display)
        state.onCalculate()
        assertEquals("Error", state.display)
        state.onInput("2")
        assertEquals("2", state.display)
    }

    @Test
    fun testDivisionByZeroShowsError() {
        val state = CalculatorState()
        state.onInput("5")
        state.onOperation("÷")
        state.onInput("0")
        assertEquals("5÷0", state.display)
        state.onCalculate()
        assertEquals("Error", state.display)
    }

    @Test
    fun testHistoryTracking() {
        val state = CalculatorState()
        state.onInput("5")
        state.onOperation("+")
        state.onInput("3")
        state.onCalculate()
        assertEquals("8", state.display)
        assertEquals(1, state.history.size)
        assertEquals("5+3", state.history[0].expression)
        assertEquals("8", state.history[0].result)
    }

    @Test
    fun testHistoryWithTrailingOperators() {
        val state = CalculatorState()

        // 5+ calculation should not add "5 = 5" to history
        state.onInput("5")
        state.onOperation("+")
        state.onCalculate()
        assertEquals("5", state.display)
        assertTrue(state.history.isEmpty())

        // 5-2+ calculation should add "5-2" -> "3" to history (stripped trailing operator)
        state.onClear()
        state.onInput("5")
        state.onOperation("−")
        state.onInput("2")
        state.onOperation("+")
        state.onCalculate()
        assertEquals("3", state.display)
        assertEquals(1, state.history.size)
        assertEquals("5-2", state.history[0].expression)
        assertEquals("3", state.history[0].result)
    }

    @Test
    fun testScientificSquareRoot() {
        val state = CalculatorState()
        state.onInput("√(")
        state.onInput("9")
        state.onInput(")")
        assertEquals("√(9)", state.display)
        state.onCalculate()
        assertEquals("3", state.display)
    }

    @Test
    fun testScientificPiAndPower() {
        val state = CalculatorState()
        state.onInput("π")
        state.onOperation("×")
        state.onInput("2")
        state.onCalculate()
        val piDouble = state.display.toDouble()
        assertTrue(piDouble > 6.28 && piDouble < 6.29)
    }

    @Test
    fun testScientificParenthesesAndExponent() {
        val state = CalculatorState()
        state.onInput("(")
        state.onInput("2")
        state.onOperation("+")
        state.onInput("3")
        state.onInput(")")
        state.onInput("^2")
        state.onCalculate()
        assertEquals("25", state.display)
    }

    @Test
    fun testCalculatorEngineDirectEvaluation() {
        assertEquals("10", CalculatorEngine.evaluate("5×2"))
        assertEquals("2.5", CalculatorEngine.evaluate("5÷2"))
        assertEquals("4", CalculatorEngine.evaluate("2^2"))
        assertEquals("8", CalculatorEngine.evaluate("2^3"))
        assertEquals("0.5", CalculatorEngine.evaluate("1/2"))
        assertEquals("4", CalculatorEngine.evaluate("√(16)"))
    }

    @Test
    fun testSetHistory() {
        val state = CalculatorState()
        val loadedItems = listOf(
            com.alecdev.quickcalc.presentation.HistoryItem("10+5", "15"),
            com.alecdev.quickcalc.presentation.HistoryItem("20×2", "40")
        )
        state.setHistory(loadedItems)
        assertEquals(2, state.history.size)
        assertEquals("10+5", state.history[0].expression)
        assertEquals("15", state.history[0].result)
    }

    @Test
    fun testImplicitMultiplication() {
        assertEquals("6", CalculatorEngine.evaluate("2(3)"))
        assertEquals("6", CalculatorEngine.evaluate("(2)(3)"))
        assertEquals("6", CalculatorEngine.evaluate("2√(9)"))
        val piDouble = CalculatorEngine.evaluate("2π").toDouble()
        assertTrue(piDouble > 6.28 && piDouble < 6.29)
        val piParenDouble = CalculatorEngine.evaluate("π(2)").toDouble()
        assertTrue(piParenDouble > 6.28 && piParenDouble < 6.29)
    }

    @Test
    fun testAutoCloseParentheses() {
        assertEquals("3", CalculatorEngine.evaluate("√(9"))
        assertEquals("5", CalculatorEngine.evaluate("(2+3"))
    }

    @Test
    fun testReciprocalWrapping() {
        val state = CalculatorState()
        state.onInput("5")
        state.onReciprocal()
        assertEquals("1/(5)", state.display)
        state.onCalculate()
        assertEquals("0.2", state.display)

        state.onClear()
        state.onReciprocal()
        assertEquals("1/", state.display)
        state.onReciprocal()
        assertEquals("", state.display)
    }

    @Test
    fun testScientificDecimalSupport() {
        val state = CalculatorState()
        state.onInput("2")
        state.onInput(".")
        state.onInput("5")
        state.onInput("^")
        state.onInput("3")
        state.onInput(".")
        state.onInput("5")
        assertEquals("2.5^3.5", state.display)
    }

    @Test
    fun testEngineStateMachineFunctions() {
        // applyInput
        assertEquals("12", CalculatorEngine.applyInput("1", "2"))
        assertEquals("12.", CalculatorEngine.applyInput("12", "."))
        assertEquals("12.", CalculatorEngine.applyInput("12.", ".")) // blocked duplicate dot
        assertEquals("0.", CalculatorEngine.applyInput("", "."))
        assertEquals("0.", CalculatorEngine.applyInput("Error", "."))
        assertEquals("5+0.", CalculatorEngine.applyInput("5+", "."))
        assertEquals("5-0.", CalculatorEngine.applyInput("5-", "."))
        assertEquals("5×0.", CalculatorEngine.applyInput("5×", "."))
        assertEquals("5÷0.", CalculatorEngine.applyInput("5÷", "."))
        assertEquals("5+0.", CalculatorEngine.applyInput("5+0.", ".")) // blocked duplicate dot
        assertEquals("-0.", CalculatorEngine.applyInput("-", "."))
        assertEquals("(0.", CalculatorEngine.applyInput("(", "."))
        assertEquals("2^0.", CalculatorEngine.applyInput("2^", "."))
        assertEquals("√(0.", CalculatorEngine.applyInput("√(", "."))
        assertEquals("π0.", CalculatorEngine.applyInput("π", "."))
        assertEquals("e0.", CalculatorEngine.applyInput("e", "."))

        // applyOperation
        assertEquals("12+", CalculatorEngine.applyOperation("12", "+"))
        assertEquals("-", CalculatorEngine.applyOperation("", "-"))

        // applyDelete & applyClear
        assertEquals("1", CalculatorEngine.applyDelete("12"))
        assertEquals("", CalculatorEngine.applyDelete("1"))
        assertEquals("", CalculatorEngine.applyClear())

        // applyReciprocal
        assertEquals("1/(8)", CalculatorEngine.applyReciprocal("8"))
        assertEquals("1/", CalculatorEngine.applyReciprocal(""))

        // applyCalculate
        val (validRes, validOk) = CalculatorEngine.applyCalculate("8×2")
        assertTrue(validOk)
        assertEquals("16", validRes)

        val (trailingRes, trailingOk) = CalculatorEngine.applyCalculate("8×2+")
        assertTrue(trailingOk)
        assertEquals("16", trailingRes)

        val (errRes, errOk) = CalculatorEngine.applyCalculate("8÷0")
        org.junit.Assert.assertFalse(errOk)
        assertEquals("Error", errRes)
    }

    @Test
    fun testHistoryCappingToMax30Items() {
        val items = (1..40).map { com.alecdev.quickcalc.presentation.HistoryItem("$it+$it", "${it * 2}") }
        val capped = if (items.size > com.alecdev.quickcalc.presentation.HistoryRepository.MAX_HISTORY_ITEMS) {
            items.takeLast(com.alecdev.quickcalc.presentation.HistoryRepository.MAX_HISTORY_ITEMS)
        } else {
            items
        }
        assertEquals(30, capped.size)
        assertEquals("11+11", capped.first().expression)
        assertEquals("40+40", capped.last().expression)
    }
}
