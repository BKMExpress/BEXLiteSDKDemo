package com.bkm.mobil.sdk.lite.demo.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bkm.mobil.sdk.lite.api.model.OtpData

@Composable
fun EnterOtpScreen(
    modifier: Modifier = Modifier,
    otpData: OtpData,
    verifyLoading: Boolean,
    resendLoading: Boolean,
    onVerify: (otpValue: String) -> Unit,
    onResend: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "OTP doğrulama",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "Telefon: ${otpData.gsmNo.takeLast(4)}... • Süre: ${otpData.durationTimeInSeconds}s",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "OTP Ref No: ${otpData.otpRefNo}",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = otpValue,
            onValueChange = { otpValue = it },
            label = { Text("OTP") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onVerify(otpValue) },
                modifier = Modifier.weight(1f),
                enabled = !verifyLoading && otpValue.length == 6
            ) {
                if (verifyLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Doğrula")
                }
            }
            Button(
                onClick = onResend,
                modifier = Modifier.weight(1f),
                enabled = !resendLoading
            ) {
                if (resendLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Yeniden Gönder")
                }
            }
        }
    }
}
