package com.bkm.mobil.sdk.lite.demo.flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bkm.mobil.sdk.lite.api.BexCardInfo
import com.bkm.mobil.sdk.lite.api.CardSelectionResult

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    cards: List<BexCardInfo>,
    deletingCardId: String?,
    selectedCardId: String?,
    onSelectCard: (cardId: String) -> Unit,
    onCardSelected: (CardSelectionResult) -> Unit,
    onAddCardClick: () -> Unit,
    onAddCardXmlClick: () -> Unit,
    onDeleteCard: (cardId: String) -> Unit,
    paymentAmountText: String,
    /** Home-screen demo selection (TDS / OTP / NONE). */
    paymentSecurityLabel: String,
    onPay: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Kart listesi (${cards.size})",
            style = MaterialTheme.typography.titleSmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onAddCardClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Kart Ekle (CMP)")
            }
            Button(
                onClick = onAddCardXmlClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Kart Ekle (XML)")
            }
        }


        cards.forEach { card ->
            val isSelected = selectedCardId == card.cardId
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectCard(card.cardId)
                            onCardSelected(CardSelectionResult(selectedCard = card))
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.cardAlias.ifBlank { card.bankInformation.bankShortName },
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = card.maskCardNumber,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Seçili",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDeleteCard(card.cardId) },
                        enabled = deletingCardId == null
                    ) {
                        if (deletingCardId == card.cardId) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = "Sil")
                        }
                    }
                }
            }
        }

        Button(
            onClick = onPay,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedCardId != null
        ) {
            Text("₺ $paymentAmountText Öde ($paymentSecurityLabel)")
        }
    }
}
