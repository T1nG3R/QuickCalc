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
        // dot on empty expression is ignored
        state.onInput(".")
        assertEquals("", state.display)

        state.onInput("7")
        state.onInput(".")
        state.onInput("5")
        assertEquals("7.5", state.display)

        // second dot in the same number is ignored
        state.onInput(".")
        assertEquals("7.5", state.display)

        state.onOperation("+")
        state.onInput("2")
        state.onInput(".")
        state.onInput("3")
        assertEquals("7.5+2.3", state.display)
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
    fun testInvalidExpressionIgnored() {
        val state = CalculatorState()
        state.onInput("5")
        state.onOperation("+")
        assertEquals("5+", state.display)
        // invalid expression onCalculate should do nothing (not change display to Error)
        state.onCalculate()
        assertEquals("5+", state.display)
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
}
