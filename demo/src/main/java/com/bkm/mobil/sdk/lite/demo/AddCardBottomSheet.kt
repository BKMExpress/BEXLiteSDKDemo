package com.bkm.mobil.sdk.lite.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardBottomSheet(
    onDismiss: () -> Unit,
    onSaveCard: (last4: String, holderName: String) -> Unit,
    colorScheme: ColorScheme? = null
) {
    val sheetState = rememberModalBottomSheetState()
    var cardHolderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val displayNumber = remember(cardNumber) {
        if (cardNumber.isBlank()) "**** **** **** ****" else cardNumber
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        val sheetContent = @Composable {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Kart Ekle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Card preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE85D04),
                                    Color(0xFFDC2F02)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        if (cardHolderName.isBlank()) "???" else cardHolderName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (displayNumber.isBlank()) "**** **** **** ****" else displayNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "SKT ${expiry.ifBlank { "**/**" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = cardHolderName,
                    onValueChange = { cardHolderName = it },
                    label = { Text("Kart Sahibi") },
                    placeholder = { Text("Kart Sahibi Adı/Soyadı") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = {
                        cardNumber =
                            it.filter { c -> c.isDigit() }.take(16).chunked(4).joinToString(" ")
                    },
                    label = { Text("Kart Numarası") },
                    placeholder = { Text("Kart Numarası") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = {
                            val digits = it.filter { c -> c.isDigit() }
                            when {
                                digits.length <= 2 -> expiry = digits
                                digits.length <= 4 -> expiry = "${digits.take(2)}/${digits.drop(2)}"
                            }
                        },
                        label = { Text("MM/YY") },
                        placeholder = { Text("MM/YY") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = {
                            if (it.length <= 4) cvv = it.filter { c -> c.isDigit() }
                        },
                        label = { Text("CVV") },
                        placeholder = { Text("***") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        val digits = cardNumber.filter { it.isDigit() }
                        if (digits.length >= 4 && cardHolderName.isNotBlank()) {
                            onSaveCard(digits.takeLast(4), cardHolderName.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kaydet")
                }
            }
        }
        if (colorScheme != null) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes
            ) {
                sheetContent()
            }
        } else {
            sheetContent()
        }
    }
}
