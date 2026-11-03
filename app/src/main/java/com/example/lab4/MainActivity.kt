package com.example.lab4

import android.content.res.Configuration
import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab4.ui.theme.LAB4Theme

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
}


enum class ButtonRole {
    DIGIT,      // cyfry i przecinek
    OPERATOR,   // działania podstawowe i nawiasy
    ACTION,     // czyszczenie i kasowanie znaku
    SCIENTIFIC, // funkcje naukowe
    EQUALS      // przycisk (=)
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
    CalcButton(".", ButtonRole.DIGIT, CalcAction.Insert(".")),
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


@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by rememberSaveable { mutableStateOf("") }
    var previewResult by rememberSaveable { mutableStateOf("") }

    // Rozpoznanie orientacji ekranu (Zadanie 3.1)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun onButtonClick(action: CalcAction) {
        when (action) {
            is CalcAction.Insert -> expression += action.textToInsert
            CalcAction.Clear -> {
                expression = ""
                previewResult = ""
            }
            CalcAction.Backspace -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            CalcAction.Evaluate -> {
                // Obliczenia będą zaimplementowane w Konspekcie 5
                previewResult = if (expression.isNotEmpty()) "= $expression" else ""
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
            result = previewResult,
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
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(expression) {
        scrollState.scrollTo(scrollState.maxValue)
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
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp
                ),
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            )

            Text(
                text = result,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                maxLines = 1
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
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CalculatorPortraitPreview() {
    LAB4Theme {
        CalculatorScreen()
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun CalculatorLandscapePreview() {
    LAB4Theme {
        CalculatorScreen()
    }
}