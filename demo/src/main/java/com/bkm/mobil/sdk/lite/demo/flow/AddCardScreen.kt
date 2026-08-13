package com.bkm.mobil.sdk.lite.demo.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bkm.mobil.sdk.lite.api.BexTokenizedPan
import com.bkm.mobil.sdk.lite.api.model.AgreementItem
import com.bkm.mobil.sdk.lite.ui.BEXAgreementCheckbox
import com.bkm.mobil.sdk.lite.ui.BEXSecureCardNumberField
import com.bkm.mobil.sdk.lite.ui.rememberBEXSecureCardNumberState
import kotlinx.coroutines.launch

/**
 * Demo screen – Compose secure card entry example.
 *
 * Uses [BEXSecureCardNumberField] backed by [com.bkm.mobil.sdk.lite.ui.BEXSecureCardNumberState].
 * The raw card number is never accessible to this screen; only [BexTokenizedPan] is passed to
 * [onSave].
 */
@Composable
fun AddCardScreen(
    modifier: Modifier = Modifier,
    loading: Boolean,
    agreements: List<AgreementItem>,
    onSave: (tokenizedPan: BexTokenizedPan, aliasName: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val cardState = rememberBEXSecureCardNumberState()
    var expiryDigits by remember { mutableStateOf("") }
    var aliasName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Kart Ekle (Compose – Güvenli)",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Ham kart numarası bu ekrana hiçbir zaman ulaşmaz.\nSadece şifreli token iletilir.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        BEXSecureCardNumberField(
            state = cardState,
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )

        OutlinedTextField(
            value = aliasName,
            onValueChange = { aliasName = it },
            label = { Text("Kart adı (isteğe bağlı)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading,
            singleLine = true
        )

        OutlinedTextField(
            value = expiryDigits,
            onValueChange = { expiryDigits = it.filter { c -> c.isDigit() }.take(4) },
            label = { Text("Son kullanma (AA/YY)") },
            placeholder = { Text("MM/YY") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ExpiryDateVisualTransformation(),
            enabled = !loading
        )

        val agreement = agreements.firstOrNull { it.type == "AGREEMENT" }
        if (agreement != null) {
            Text(
                text = "Sözleşmeler",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BEXAgreementCheckbox(enabled = !loading)
                Text(
                    text = buildAgreementLabel(
                        label = agreement.label.orEmpty(),
                        highlight = agreement.labelHighlight
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                enabled = !loading
            ) {
                Text("İptal")
            }
            Button(
                onClick = {
                    val expiryNormalized =
                        ExpiryDateVisualTransformation.normalizeExpiryDigits(expiryDigits)
                    if (cardState.isComplete && expiryNormalized.length == 4) {
                        scope.launch {
                            val tokenized = cardState.getTokenizedPan(expiryNormalized)
                            if (tokenized != null) {
                                val alias = aliasName.trim().takeIf { it.isNotEmpty() }
                                onSave(tokenized, alias)
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !loading && cardState.isComplete && expiryDigits.length == 4
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Kaydet")
                }
            }
        }
    }
}

private fun buildAgreementLabel(label: String, highlight: String?): AnnotatedString {
    if (highlight.isNullOrBlank()) return AnnotatedString(label)
    val idx = label.indexOf(highlight, ignoreCase = true)
    if (idx < 0) return AnnotatedString(label)
    return buildAnnotatedString {
        append(label.substring(0, idx))
        withStyle(SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)) {
            append(label.substring(idx, idx + highlight.length))
        }
        append(label.substring(idx + highlight.length))
    }
}
