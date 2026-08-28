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

    fun applyInput(currentExpr: String, input: String): String {
        val baseExpr = if (currentExpr == "Error") "" else currentExpr

        if (input == ".") {
            if (baseExpr.lastOrNull()?.isDigit() != true || lastNumberContainsDecimal(baseExpr)) {
                return baseExpr
            }
        }
        return baseExpr + input
    }

    fun applyOperation(currentExpr: String, op: String): String {
        val baseExpr = if (currentExpr == "Error") "" else currentExpr
        val sanitizedOp = if (op == "−") "-" else op

        if (baseExpr.isEmpty() && sanitizedOp == "-") {
            return "-"
        }

        if (sanitizedOp == "-" && isLastCharOperation(baseExpr) && baseExpr.last() != '-') {
            return baseExpr + sanitizedOp
        }

        if (baseExpr.isNotEmpty() && !isLastCharOperation(baseExpr)) {
            return baseExpr + sanitizedOp
        }

        return baseExpr
    }

    fun applyDelete(currentExpr: String): String {
        if (currentExpr == "Error" || currentExpr.isEmpty()) {
            return ""
        }
        return currentExpr.dropLast(1)
    }

    fun applyClear(): String = ""

    fun applyReciprocal(currentExpr: String): String {
        if (currentExpr == "Error" || currentExpr.isEmpty()) {
            return "1/"
        }
        if (currentExpr == "1/") {
            return ""
        }
        return "1/($currentExpr)"
    }

    fun applyCalculate(currentExpr: String): Pair<String, Boolean> {
        if (currentExpr.isBlank() || currentExpr == "Error") {
            return Pair("", false)
        }
        return try {
            val result = evaluate(currentExpr)
            Pair(result, true)
        } catch (e: Exception) {
            Pair("Error", false)
        }
    }
}
