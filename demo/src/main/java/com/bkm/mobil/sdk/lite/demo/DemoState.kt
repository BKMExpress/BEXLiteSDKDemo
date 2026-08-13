package com.bkm.mobil.sdk.lite.demo

import com.bkm.mobil.sdk.lite.api.BexEnvironment
import com.bkm.mobil.sdk.lite.api.BexLiteSdkTheme
import com.bkm.mobil.sdk.lite.api.PaymentSecurity
import com.bkm.mobil.sdk.lite.api.SdkInitParams
import com.bkm.mobil.sdk.lite.api.TransactionType

enum class DemoTheme {
    Default,
    Akbank,
    Getir
}

enum class DemoUxMode {
    FullScreen,
    BottomSheet
}

data class DemoFormState(
    val token: String = "",
    val merchantId: String = "",
    val gsmNo: String = "",
    val merchantUserId: String = "",
    val transactionId: String = "",
    val successUrl: String = "",
    val failUrl: String = "",
    val paymentAmount: String = "",
    val paymentCurrency: String = "",
    /** Passed to [com.bkm.mobil.sdk.lite.api.BexPaymentClient.startPayment]. */
    val orderId: String = "",
    val installmentCount: String = "",
    val transactionType: TransactionType = TransactionType.SALE,
    /** Used by demo dashboard pay only (passed when starting client flow). */
    val paymentSecurity: PaymentSecurity = PaymentSecurity.NONE,
    val environment: BexEnvironment = BexEnvironment.DEV
) {
    fun toInitParams(): SdkInitParams? {
        if (token.isBlank() ||
            merchantId.isBlank() ||
            gsmNo.isBlank() ||
            merchantUserId.isBlank() ||
            transactionId.isBlank() ||
            successUrl.isBlank() ||
            failUrl.isBlank()
        ) return null
        val parsedInstallment = installmentCount.trim().toIntOrNull()
        if (parsedInstallment == null || parsedInstallment < 1) return null
        return SdkInitParams(
            token = token.trim(),
            merchantId = merchantId.trim(),
            gsmNo = gsmNo.trim(),
            merchantUserId = merchantUserId.trim(),
            transactionId = transactionId.trim(),
            successUrl = successUrl.trim(),
            failUrl = failUrl.trim(),
            environment = environment,
            currency = paymentCurrency.trim().uppercase().ifBlank { "TRY" },
            installmentCount = parsedInstallment,
            transactionType = transactionType
        )
    }
}

/** Short label for demo UI (home dropdown + dashboard pay button). */
fun PaymentSecurity.demoShortLabel(): String = when (this) {
    PaymentSecurity.TDS -> "TDS"
    PaymentSecurity.OTP -> "OTP"
    PaymentSecurity.NONE -> "NONE"
}

fun DemoTheme.toBexLiteSdkTheme(): BexLiteSdkTheme? = when (this) {
    DemoTheme.Default -> null
    DemoTheme.Getir -> BexLiteSdkTheme(
        colors = BexLiteSdkTheme.Colors(
            primary = 0xFF5c3cbb.toInt(),
            textPrimary = 0xFF000000.toInt(),
            buttonPrimary = 0xFF5c3cbb.toInt(),
            buttonPrimaryText = 0xFFFFFFFF.toInt(),
            buttonSecondaryText = 0xFF5c3cbb.toInt(),
            buttonSecondaryBorder = 0xFF5c3cbb.toInt()
        ),
        shape = BexLiteSdkTheme.Shape(
            buttonCornerRadius = 8f,
            buttonBorderWidth = 1.5f
        )
    )

    DemoTheme.Akbank -> BexLiteSdkTheme(
        colors = BexLiteSdkTheme.Colors(
            primary = 0xFFDB3931.toInt(),
            textPrimary = 0xFF000000.toInt(),
            buttonPrimary = 0xFFDB3931.toInt(),
            buttonPrimaryText = 0xFFFFFFFF.toInt(),
            buttonSecondaryBorder = 0xFFDB3931.toInt(),
            buttonSecondaryText = 0xFFDB3931.toInt()
        ),
        shape = BexLiteSdkTheme.Shape(
            buttonCornerRadius = 24f,
            buttonBorderWidth = 2f
        )
    )
}
