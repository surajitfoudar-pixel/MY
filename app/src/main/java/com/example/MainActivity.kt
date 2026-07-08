package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val darkTheme = isSystemInDarkTheme()

    // Professional modern color palette configuration
    val brandPrimary = if (darkTheme) Color(0xFF6366F1) else Color(0xFF4F46E5) // Indigo
    val brandPrimaryContainer = if (darkTheme) Color(0xFF312E81) else Color(0xFFE0E7FF)
    val onBrandPrimary = Color.White
    
    val numberKeyBg = if (darkTheme) Color(0xFF1F1F23) else Color(0xFFF3F4F6)
    val numberKeyText = if (darkTheme) Color(0xFFE5E7EB) else Color(0xFF1F2937)
    
    val actionKeyBg = if (darkTheme) Color(0xFF374151) else Color(0xFFE5E7EB)
    val actionKeyText = if (darkTheme) Color(0xFFF9FAFB) else Color(0xFF374151)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (darkTheme) Color(0xFF0F0F11) else Color(0xFFFAFAFA))
    ) {
        // App Header Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Calculator",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleHistory()
                    },
                    modifier = Modifier.testTag("key_history_toggle")
                ) {
                    Icon(
                        imageVector = if (uiState.isHistoryExpanded) Icons.Default.HistoryToggleOff else Icons.Default.History,
                        contentDescription = "Toggle History"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Collapsible History Panel
                AnimatedVisibility(
                    visible = uiState.isHistoryExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HistoryPanel(
                        history = uiState.history,
                        onItemClick = { item ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectHistoryItem(item)
                        },
                        onClearClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.clearHistory()
                        },
                        modifier = Modifier.height(200.dp)
                    )
                }

                // Display Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (darkTheme) Color(0xFF1A1A1E) else Color(0xFFFFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Formula / Expression Display
                        Text(
                            text = uiState.expression.ifEmpty { " " },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (darkTheme) Color(0xAA9CA3AF) else Color(0xAA6B7280),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.End
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("expression_text")
                        )

                        // Main Output / Typed digits Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Backspace key (visible when typing has started)
                            val showBackspace = uiState.displayValue != "0" && uiState.expression.endsWith("=").not()
                            if (showBackspace) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.onBackspaceClick()
                                    },
                                    modifier = Modifier
                                        .testTag("key_backspace")
                                        .size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = brandPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // Output Number (scrolls horizontally so text never wraps or clips)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState(), reverseScrolling = true),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = uiState.displayValue,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = if (uiState.displayValue.length > 8) 36.sp else 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (darkTheme) Color.White else Color(0xFF111827),
                                        textAlign = TextAlign.End
                                    ),
                                    maxLines = 1,
                                    modifier = Modifier.testTag("display_text")
                                )
                            }
                        }
                    }
                }

                // Calculator Keyboard Pad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: C/AC, +/-, %, ÷
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isClearInputOnly = uiState.displayValue != "0" && uiState.expression.contains("=").not()
                        CalculatorButton(
                            text = if (isClearInputOnly) "C" else "AC",
                            backgroundColor = actionKeyBg,
                            textColor = actionKeyText,
                            testTag = "key_clear",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onClearClick() }
                        )
                        CalculatorButton(
                            text = "±",
                            backgroundColor = actionKeyBg,
                            textColor = actionKeyText,
                            testTag = "key_toggle_sign",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onToggleSignClick() }
                        )
                        CalculatorButton(
                            text = "%",
                            backgroundColor = actionKeyBg,
                            textColor = actionKeyText,
                            testTag = "key_percent",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onPercentClick() }
                        )
                        CalculatorButton(
                            text = "÷",
                            backgroundColor = brandPrimary,
                            textColor = onBrandPrimary,
                            testTag = "key_divide",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperatorClick("÷") }
                        )
                    }

                    // Row 2: 7, 8, 9, ×
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalculatorButton(
                            text = "7",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_7",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("7") }
                        )
                        CalculatorButton(
                            text = "8",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_8",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("8") }
                        )
                        CalculatorButton(
                            text = "9",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_9",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("9") }
                        )
                        CalculatorButton(
                            text = "×",
                            backgroundColor = brandPrimary,
                            textColor = onBrandPrimary,
                            testTag = "key_multiply",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperatorClick("×") }
                        )
                    }

                    // Row 3: 4, 5, 6, -
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalculatorButton(
                            text = "4",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_4",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("4") }
                        )
                        CalculatorButton(
                            text = "5",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_5",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("5") }
                        )
                        CalculatorButton(
                            text = "6",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_6",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("6") }
                        )
                        CalculatorButton(
                            text = "−",
                            backgroundColor = brandPrimary,
                            textColor = onBrandPrimary,
                            testTag = "key_subtract",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperatorClick("-") }
                        )
                    }

                    // Row 4: 1, 2, 3, +
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalculatorButton(
                            text = "1",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_1",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("1") }
                        )
                        CalculatorButton(
                            text = "2",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_2",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("2") }
                        )
                        CalculatorButton(
                            text = "3",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_3",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDigitClick("3") }
                        )
                        CalculatorButton(
                            text = "+",
                            backgroundColor = brandPrimary,
                            textColor = onBrandPrimary,
                            testTag = "key_add",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onOperatorClick("+") }
                        )
                    }

                    // Row 5: 0 (double column span), ., =
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalculatorButton(
                            text = "0",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_0",
                            modifier = Modifier.weight(2f),
                            onClick = { viewModel.onDigitClick("0") }
                        )
                        CalculatorButton(
                            text = ".",
                            backgroundColor = numberKeyBg,
                            textColor = numberKeyText,
                            testTag = "key_decimal",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onDecimalClick() }
                        )
                        CalculatorButton(
                            text = "=",
                            backgroundColor = brandPrimary,
                            textColor = onBrandPrimary,
                            testTag = "key_equal",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onEqualClick() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(if (text == "0") 2.1f else 1f)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .testTag(testTag)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor
            )
        )
    }
}

@Composable
fun HistoryPanel(
    history: List<CalculationHistoryItem>,
    onItemClick: (CalculationHistoryItem) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTheme = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkTheme) Color(0xFF16161A) else Color(0xFFF9FAFB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (darkTheme) Color(0xFF9CA3AF) else Color(0xFF4B5563)
                    )
                )
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onClearClick,
                        modifier = Modifier.testTag("key_history_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear History",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history yet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (darkTheme) Color(0xFF4B5563) else Color(0xFF9CA3AF)
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(history, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onItemClick(item) }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.expression,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (darkTheme) Color(0xFF9CA3AF) else Color(0xFF4B5563)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "= ${item.result}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (darkTheme) Color.White else Color(0xFF111827)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
