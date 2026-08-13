package com.bkm.mobil.sdk.lite.demo.flow

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bkm.mobil.sdk.lite.api.BexTokenizedPan
import com.bkm.mobil.sdk.lite.api.model.AgreementItem
import com.bkm.mobil.sdk.lite.demo.databinding.LayoutAddCardXmlBinding
import kotlinx.coroutines.launch

/**
 * Demo screen – XML (View-based) secure card entry example.
 *
 * The layout is inflated from [LayoutAddCardXmlBinding] (layout_add_card_xml.xml), which
 * demonstrates how merchants can fully customise [com.bkm.mobil.sdk.lite.ui.BEXSecureCardNumberEditText]
 * using standard Android XML attributes and the SDK's `app:bexCard*` attributes.
 *
 * Customisation shown in the XML layout:
 * - Custom background with rounded corners and brand-coloured stroke.
 * - Custom digit text colour, hint colour, text size, and letter spacing.
 * - `android:padding` for inner whitespace.
 * - `android:elevation` for a subtle shadow.
 *
 * Security: the raw card number never surfaces in this composable. Only [BexTokenizedPan]
 * (and optional alias) is passed to [onSave].
 */
@Composable
fun AddCardXmlScreen(
    modifier: Modifier = Modifier,
    loading: Boolean,
    agreements: List<AgreementItem>,
    onSave: (tokenizedPan: BexTokenizedPan, aliasName: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var binding by remember { mutableStateOf<LayoutAddCardXmlBinding?>(null) }
    var expiryDigits by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Kart Ekle (XML – Güvenli)",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Ham kart numarası bu ekrana hiçbir zaman ulaşmaz.\nSadece şifreli token iletilir.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        AndroidView(
            factory = { context ->
                val inflated = LayoutAddCardXmlBinding.inflate(LayoutInflater.from(context))
                binding = inflated

                inflated.expiryInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable) {
                        expiryDigits = s.toString().filter { it.isDigit() }.take(4)
                    }
                })

                inflated.addCardButton.setOnClickListener {
                    val b = binding ?: return@setOnClickListener
                    val expiry = ExpiryDateVisualTransformation.normalizeExpiryDigits(expiryDigits)
                    if (b.secureCardInput.isComplete && expiry.length == 4) {
                        scope.launch {
                            val tokenized = b.secureCardInput.getTokenizedPan(expiry)
                            if (tokenized != null) {
                                val alias = b.aliasInput.text?.toString()?.trim().orEmpty()
                                onSave(tokenized, alias.takeIf { it.isNotEmpty() })
                            }
                        }
                    }
                }

                inflated.cancelButton.setOnClickListener { onDismiss() }

                inflated.root
            },
            modifier = Modifier.fillMaxWidth(),
            update = { _ ->
                binding?.addCardButton?.isEnabled = !loading
                binding?.cancelButton?.isEnabled = !loading
                binding?.secureCardInput?.isEnabled = !loading
                binding?.expiryInput?.isEnabled = !loading
                binding?.aliasInput?.isEnabled = !loading

                val agreement = agreements.firstOrNull { it.type == "AGREEMENT" }
                val showAgreement = agreement != null
                binding?.agreementRow?.visibility =
                    if (showAgreement) android.view.View.VISIBLE else android.view.View.GONE
                if (showAgreement) {
                    binding?.agreementText?.text = agreement.label.orEmpty()
                }
            }
        )
    }
}
