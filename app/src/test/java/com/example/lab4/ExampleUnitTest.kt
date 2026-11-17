package com.example.lab4

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun precedence_multiplicationBeforeAddition_returns6() {
        assertEquals(CalcOutcome.Success("6"), evaluateExpression("2+2*2"))
    }

    @Test
    fun parentheses_overridesPrecedence_returns8() {
        assertEquals(CalcOutcome.Success("8"), evaluateExpression("(2+2)*2"))
    }

    @Test
    fun divisionByZero_returnsErrorWithoutCrash() {
        val result = evaluateExpression("1/0")
        assertTrue(result is CalcOutcome.Error)
    }

    @Test
    fun incompleteExpression_trailingOperator_returnsEmpty() {
        assertEquals(CalcOutcome.Empty, evaluateExpression("2+"))
    }

    @Test
    fun unclosedParenthesis_returnsEmpty() {
        assertEquals(CalcOutcome.Empty, evaluateExpression("((2+2)"))
    }

    @Test
    fun emptyInput_returnsEmpty() {
        assertEquals(CalcOutcome.Empty, evaluateExpression(""))
        assertEquals(CalcOutcome.Empty, evaluateExpression("   "))
    }

    @Test
    fun domainError_sqrtNegative_returnsError() {
        val result = evaluateExpression("sqrt(-1)")
        assertTrue(result is CalcOutcome.Error)
    }

    @Test
    fun largeNumbers_formatsInScientificNotation() {
        val result = evaluateExpression("1234567890*1234567890")
        assertTrue(result is CalcOutcome.Success)
        assertTrue((result as CalcOutcome.Success).formatted.contains("E"))
    }

    @Test
    fun decimalSeparator_dotAndComma_giveSameResult() {
        assertEquals(evaluateExpression("2.5+1"), evaluateExpression("2,5+1"))
        assertEquals(CalcOutcome.Success("3.5"), evaluateExpression("2,5+1"))
    }

    @Test
    fun scientificFunctions_sinAndPi_calculateAccurately() {
        assertEquals(CalcOutcome.Success("1"), evaluateExpression("sin(pi/2)"))
    }
}