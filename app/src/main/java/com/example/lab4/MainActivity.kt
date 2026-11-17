package com.example.lab4

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lab4.ui.theme.LAB4Theme
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.License
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LAB4Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    // zapis stanu w obiekcie Bundle podczas obrotu ekranu
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("CalcState", "Zapisany stan Bundle: $outState")
        for (key in outState.keySet()) {
            Log.d("CalcState", "Klucz: $key -> Wartość: ${outState.get(key)}")
        }
    }
}

// logika obliczen

sealed interface CalcOutcome {
    data object Empty : CalcOutcome              // brak wyniku i brak bledu
    data class Success(val formatted: String) : CalcOutcome // git wynik
    data class Error(val message: String) : CalcOutcome     // czytelny komunikat
}

fun evaluateExpression(rawExpression: String): CalcOutcome {
    // Deklaracja niekomercyjnego użycia mXparser (wymog licencji)
    License.iConfirmNonCommercialUse("Student")

    val trimmed = rawExpression.trim()
    if (trimmed.isEmpty()) return CalcOutcome.Empty

    // polski separator dziesietny
    val normalized = trimmed.replace(',', '.')

    // sprawdzanie czy wyrazenie jest niedokonczone np. nawias otwarty
    val lastChar = normalized.last()
    val openParens = normalized.count { it == '(' }
    val closeParens = normalized.count { it == ')' }

    if (lastChar in listOf('+', '-', '*', '/', '^', '.', '(') || openParens > closeParens) {
        return CalcOutcome.Empty
    }

    // bledy dziedziny np dzielenie przez 0
    val expression = Expression(normalized)
    val value = expression.calculate()

    if (value.isNaN() || value.isInfinite()) {
        return when {
            normalized.contains(Regex("/0(\\.0*)?($|[^0-9.])")) ->
                CalcOutcome.Error("Błąd: dzielenie przez zero")
            normalized.contains("sqrt(-") || normalized.contains("ln(-") || normalized.contains("ln(0") ->
                CalcOutcome.Error("Błąd: wartość poza dziedziną")
            else ->
                CalcOutcome.Error("Niepoprawne wyrażenie")
        }
    }

    return CalcOutcome.Success(formatCalcNumber(value))
}

fun formatCalcNumber(value: Double): String {
    if (value == 0.0) return "0"
    val absVal = abs(value)
    val symbols = DecimalFormatSymbols(Locale.US)

    // notacja naukowa dla szerokiego przedzialu liczb
    return if (absVal >= 1e12 || absVal < 1e-6) {
        DecimalFormat("0.######E0", symbols).format(value)
    } else {
        // zaokraglanie do maks 8 miejsca po przecinku
        DecimalFormat("0.########", symbols).format(value)
    }
}

// przyciski

enum class ButtonRole {
    DIGIT, OPERATOR, ACTION, SCIENTIFIC, EQUALS
}

sealed interface CalcAction {
    data class Insert(val textToInsert: String) : CalcAction
    data object Clear : CalcAction
    data object Backspace : CalcAction
    data object Evaluate : CalcAction
}

data class CalcButton(
    val label: String,
    val role: ButtonRole,
    val action: CalcAction
)

val basicButtons = listOf(
    CalcButton("C", ButtonRole.ACTION, CalcAction.Clear),
    CalcButton("⌫", ButtonRole.ACTION, CalcAction.Backspace),
    CalcButton("(", ButtonRole.OPERATOR, CalcAction.Insert("(")),
    CalcButton(")", ButtonRole.OPERATOR, CalcAction.Insert(")")),

    CalcButton("7", ButtonRole.DIGIT, CalcAction.Insert("7")),
    CalcButton("8", ButtonRole.DIGIT, CalcAction.Insert("8")),
    CalcButton("9", ButtonRole.DIGIT, CalcAction.Insert("9")),
    CalcButton("÷", ButtonRole.OPERATOR, CalcAction.Insert("/")),

    CalcButton("4", ButtonRole.DIGIT, CalcAction.Insert("4")),
    CalcButton("5", ButtonRole.DIGIT, CalcAction.Insert("5")),
    CalcButton("6", ButtonRole.DIGIT, CalcAction.Insert("6")),
    CalcButton("×", ButtonRole.OPERATOR, CalcAction.Insert("*")),

    CalcButton("1", ButtonRole.DIGIT, CalcAction.Insert("1")),
    CalcButton("2", ButtonRole.DIGIT, CalcAction.Insert("2")),
    CalcButton("3", ButtonRole.DIGIT, CalcAction.Insert("3")),
    CalcButton("−", ButtonRole.OPERATOR, CalcAction.Insert("-")),

    CalcButton("0", ButtonRole.DIGIT, CalcAction.Insert("0")),
    CalcButton(",", ButtonRole.DIGIT, CalcAction.Insert(",")),
    CalcButton("+", ButtonRole.OPERATOR, CalcAction.Insert("+")),
    CalcButton("=", ButtonRole.EQUALS, CalcAction.Evaluate)
)

val scientificButtons = listOf(
    CalcButton("sin", ButtonRole.SCIENTIFIC, CalcAction.Insert("sin(")),
    CalcButton("cos", ButtonRole.SCIENTIFIC, CalcAction.Insert("cos(")),
    CalcButton("tan", ButtonRole.SCIENTIFIC, CalcAction.Insert("tan(")),
    CalcButton("π", ButtonRole.SCIENTIFIC, CalcAction.Insert("pi")),

    CalcButton("√", ButtonRole.SCIENTIFIC, CalcAction.Insert("sqrt(")),
    CalcButton("x^y", ButtonRole.SCIENTIFIC, CalcAction.Insert("^")),
    CalcButton("ln", ButtonRole.SCIENTIFIC, CalcAction.Insert("ln(")),
    CalcButton("e", ButtonRole.SCIENTIFIC, CalcAction.Insert("e"))
)

// interfejs uzytkownika

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    // rememberSaveable zapisuje stan w Bundle przy obrocie ekranu (Zadanie 1)
    var expression by rememberSaveable { mutableStateOf("") }
    var resultText by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun onButtonClick(action: CalcAction) {
        when (action) {
            is CalcAction.Insert -> {
                expression += action.textToInsert
                isError = false
            }
            CalcAction.Clear -> {
                expression = ""
                resultText = ""
                isError = false
            }
            CalcAction.Backspace -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    isError = false
                }
            }
            CalcAction.Evaluate -> {
                when (val outcome = evaluateExpression(expression)) {
                    CalcOutcome.Empty -> {
                        resultText = ""
                        isError = false
                    }
                    is CalcOutcome.Success -> {
                        resultText = outcome.formatted
                        isError = false
                    }
                    is CalcOutcome.Error -> {
                        resultText = outcome.message
                        isError = true
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DisplayPanel(
            expression = expression,
            result = resultText,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isLandscape) 0.32f else 0.28f)
        )

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.68f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonGrid(
                    buttons = scientificButtons,
                    columns = 4,
                    onButtonClick = ::onButtonClick,
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                )
                ButtonGrid(
                    buttons = basicButtons,
                    columns = 4,
                    onButtonClick = ::onButtonClick,
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                )
            }
        } else {
            ButtonGrid(
                buttons = basicButtons,
                columns = 4,
                onButtonClick = ::onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.72f)
            )
        }
    }
}

@Composable
fun DisplayPanel(
    expression: String,
    result: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val exprScroll = rememberScrollState()
    val resScroll = rememberScrollState()

    LaunchedEffect(expression) {
        exprScroll.scrollTo(exprScroll.maxValue)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = if (expression.isEmpty()) "0" else expression,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(exprScroll)
            )

            Text(
                text = result,
                style = MaterialTheme.typography.titleMedium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(resScroll)
            )
        }
    }
}

@Composable
fun ButtonGrid(
    buttons: List<CalcButton>,
    columns: Int,
    onButtonClick: (CalcAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = buttons.chunked(columns)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowButtons ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowButtons.forEach { button ->
                    CalculatorButtonItem(
                        button = button,
                        onClick = { onButtonClick(button.action) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButtonItem(
    button: CalcButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (button.role) {
        ButtonRole.DIGIT -> MaterialTheme.colorScheme.surfaceVariant
        ButtonRole.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer
        ButtonRole.ACTION -> MaterialTheme.colorScheme.tertiaryContainer
        ButtonRole.SCIENTIFIC -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
        ButtonRole.EQUALS -> MaterialTheme.colorScheme.primary
    }

    val contentColor = when (button.role) {
        ButtonRole.DIGIT -> MaterialTheme.colorScheme.onSurfaceVariant
        ButtonRole.OPERATOR -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonRole.ACTION -> MaterialTheme.colorScheme.onTertiaryContainer
        ButtonRole.SCIENTIFIC -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonRole.EQUALS -> MaterialTheme.colorScheme.onPrimary
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = button.label,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
    }
}