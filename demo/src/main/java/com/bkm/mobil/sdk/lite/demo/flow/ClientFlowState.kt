package com.bkm.mobil.sdk.lite.demo.flow

import com.bkm.mobil.sdk.lite.api.BexCardInfo
import com.bkm.mobil.sdk.lite.api.BexSdkError
import com.bkm.mobil.sdk.lite.api.BexTokenizedPan
import com.bkm.mobil.sdk.lite.api.model.OtpData
import com.bkm.mobil.sdk.lite.api.model.TransactionControlResult

sealed class ClientFlowState {
    data object Loading : ClientFlowState()
    data class Error(val error: BexSdkError) : ClientFlowState()
    data object Link : ClientFlowState()
    data class EnterOtp(val otpData: OtpData) : ClientFlowState()
    data class Dashboard(val cards: List<BexCardInfo>) : ClientFlowState()
    data object PartialRegister : ClientFlowState()
    data class TransactionResult(val result: TransactionControlResult) : ClientFlowState()
    data class TransactionResultError(
        val title: String = "Ödeme Sonucu",
        val message: String,
        val rawError: String? = null
    ) : ClientFlowState()

    data class ThreeDS(
        val tdsUrl: String,
        val htmlForm: String?,
        val paymentToken: String
    ) : ClientFlowState()

    data object AddCard : ClientFlowState()

    data object AddCardXml : ClientFlowState()
}

sealed class ClientFlowEvent {
    data object LinkClicked : ClientFlowEvent()
    data class OtpVerify(val otpValue: String) : ClientFlowEvent()
    data object OtpResend : ClientFlowEvent()
    data class Register(
        val tokenizedPan: BexTokenizedPan,
        val aliasName: String?
    ) : ClientFlowEvent()

    data object AddCardClicked : ClientFlowEvent()
    data class AddCardSave(
        val tokenizedPan: BexTokenizedPan,
        val aliasName: String?
    ) : ClientFlowEvent()

    data object AddCardDismiss : ClientFlowEvent()

    data object AddCardXmlClicked : ClientFlowEvent()
    data class AddCardXmlSave(
        val tokenizedPan: BexTokenizedPan,
        val aliasName: String?
    ) : ClientFlowEvent()

    data object AddCardXmlDismiss : ClientFlowEvent()

    data class DeleteCard(val cardId: String) : ClientFlowEvent()
    data object Back : ClientFlowEvent()
}
