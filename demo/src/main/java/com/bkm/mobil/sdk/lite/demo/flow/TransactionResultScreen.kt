package com.bkm.mobil.sdk.lite.demo.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bkm.mobil.sdk.lite.api.model.TransactionControlResult

@Composable
fun TransactionResultScreen(
    modifier: Modifier = Modifier,
    result: TransactionControlResult
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ödeme Sonucu",
            style = MaterialTheme.typography.titleSmall
        )
        ResultRow("success", result.success?.toString())
        ResultRow("message", result.message)
        ResultRow("code", result.code)
        ResultRow("posResponseMessage", result.posResponseMessage)
        ResultRow("successAmount", result.successAmount?.toString())
        ResultRow("authCode", result.authCode)
        ResultRow("hostRefCode", result.hostRefCode)
        ResultRow("procReturnCode", result.procReturnCode)
        ResultRow("secureType", result.secureType?.toString())
        ResultRow("transactionType", result.transactionType?.toString())
        ResultRow("installment", result.installment?.toString())
        ResultRow("terminalInformation", result.terminalInformation)
        ResultRow("cardBrand", result.cardBrand?.toString())
        ResultRow("cardType", result.cardType?.toString())
        ResultRow("cardNumber", result.cardNumber)
        ResultRow("bankTransactionDate", result.bankTransactionDate)
        ResultRow("transactionDate", result.transactionDate)
        ResultRow("paymentId", result.paymentId?.toString())
        ResultRow("hostRefNum", result.hostRefNum)
        ResultRow("bankTransactionId", result.bankTransactionId)
        ResultRow("orderId", result.orderId)
    }
}

@Composable
fun TransactionErrorResultScreen(
    modifier: Modifier = Modifier,
    title: String = "Ödeme Sonucu",
    message: String,
    rawError: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        rawError?.takeIf { it.isNotBlank() }?.let {
            HorizontalDivider()
            ResultRow("rawError", it)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String?) {
    Text(
        text = "$label: ${value ?: "-"}",
        style = MaterialTheme.typography.bodySmall
    )
}

