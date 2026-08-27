package com.alecdev.quickcalc.presentation

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import net.objecthunter.exp4j.ExpressionBuilder

object CalculatorEngine {
    private val df = DecimalFormat("#.########", DecimalFormatSymbols.getInstance(Locale.US))

    fun sanitize(expression: String): String {
        if (expression.isBlank()) return ""
        var sanitized = expression
            .replace('÷', '/')
            .replace('×', '*')
            .replace('−', '-')
            .replace("√", "sqrt")
            .replace("π", "pi")

        // Implicit multiplication patterns:
        // 1. Number or ')' followed by '(' -> e.g. 2(3) -> 2*(3), (2)(3) -> (2)*(3)
        sanitized = sanitized.replace(Regex("(\\d|\\))\\s*\\("), "$1*(")
        // 2. ')' followed by digit or constant/function -> e.g. (2)3 -> (2)*3, (2)pi -> (2)*pi
        sanitized = sanitized.replace(Regex("\\)\\s*(\\d|pi|e|sqrt)"), ")*$1")
        // 3. Digit followed by constant or function -> e.g. 2pi -> 2*pi, 2sqrt -> 2*sqrt, 2e -> 2*e
        sanitized = sanitized.replace(Regex("(\\d)\\s*(pi|e|sqrt)"), "$1*$2")
        // 4. Constant followed by digit, '(', function, or another constant -> e.g. pi2 -> pi*2, pi(2) -> pi*(2)
        sanitized = sanitized.replace(Regex("(pi|e)\\s*(\\d|\\(|sqrt|pi|e)"), "$1*$2")

        // Auto-close missing parentheses if user did not close them before calculating (e.g. √(9 -> √(9))
        val openParens = sanitized.count { it == '(' }
        val closeParens = sanitized.count { it == ')' }
        if (openParens > closeParens) {
            sanitized += ")".repeat(openParens - closeParens)
        }

        return sanitized
    }

    fun evaluate(expression: String): String {
        if (expression.isBlank()) return ""
        val sanitized = sanitize(expression)

        val result = ExpressionBuilder(sanitized).build().evaluate()
        if (result.isInfinite() || result.isNaN()) {
            throw ArithmeticException("Invalid calculation result")
        }
        return df.format(result)
    }

    fun isLastCharOperation(expression: String): Boolean {
        if (expression.isEmpty()) return false
        val last = expression.last()
        return last in listOf('+', '-', '−', '×', '÷', '*', '/')
    }

    fun lastNumberContainsDecimal(expression: String): Boolean {
        if (expression.isEmpty()) return false
        val lastNumber = expression.split(Regex("[-+−×÷*/^()√]")).last()
        return "." in lastNumber
    }
}
