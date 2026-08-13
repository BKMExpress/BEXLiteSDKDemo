package com.bkm.mobil.sdk.lite.demo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bkm.mobil.sdk.lite.api.BexLiteSdk
import com.bkm.mobil.sdk.lite.api.BexPaymentClient
import com.bkm.mobil.sdk.lite.api.BexSdkError
import com.bkm.mobil.sdk.lite.api.BexTokenizedPan
import com.bkm.mobil.sdk.lite.api.PaymentSecurity
import com.bkm.mobil.sdk.lite.api.model.AgreementItem
import com.bkm.mobil.sdk.lite.api.model.SdkInitResult
import com.bkm.mobil.sdk.lite.api.model.WalletCheckResult
import com.bkm.mobil.sdk.lite.api.result.BexResult
import com.bkm.mobil.sdk.lite.demo.flow.AddCardScreen
import com.bkm.mobil.sdk.lite.demo.flow.AddCardXmlScreen
import com.bkm.mobil.sdk.lite.demo.flow.ClientFlowEvent
import com.bkm.mobil.sdk.lite.demo.flow.ClientFlowState
import com.bkm.mobil.sdk.lite.demo.flow.DashboardScreen
import com.bkm.mobil.sdk.lite.demo.flow.EnterOtpScreen
import com.bkm.mobil.sdk.lite.demo.flow.LinkScreen
import com.bkm.mobil.sdk.lite.demo.flow.PartialRegisterScreen
import com.bkm.mobil.sdk.lite.demo.flow.ThreeDSWebView
import com.bkm.mobil.sdk.lite.demo.flow.TransactionErrorResultScreen
import com.bkm.mobil.sdk.lite.demo.flow.TransactionResultScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LINK_SCREEN = "LinkScreen"
private const val WALLET_SCREEN = "WalletScreen"
private const val PARTIAL_REGISTER_SCREEN = "PartialRegisterScreen"
private const val ENTER_OTP_SCREEN = "EnterOtpScreen"
private const val DASHBOARD_SCREEN = "DashboardScreen"
private const val TRANSACTION_CONTROL_SCREEN = "TransactionControlScreen"
private const val CHECKOUT_ODEMEGECIDI = "checkout-odemegecidi"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFlowScreen(
    modifier: Modifier = Modifier,
    initParams: com.bkm.mobil.sdk.lite.api.SdkInitParams,
    paymentAmount: Double,
    orderId: String,
    paymentSecurity: PaymentSecurity,
    onError: (BexSdkError) -> Unit,
    onCardSelected: (com.bkm.mobil.sdk.lite.api.CardSelectionResult) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<ClientFlowState>(ClientFlowState.Loading) }
    var linkLoading by remember { mutableStateOf(false) }
    var otpVerifyLoading by remember { mutableStateOf(false) }
    var otpResendLoading by remember { mutableStateOf(false) }
    var registerLoading by remember { mutableStateOf(false) }
    var insertCardLoading by remember { mutableStateOf(false) }
    var deletingCardId by remember { mutableStateOf<String?>(null) }
    var selectedCardId by remember { mutableStateOf<String?>(null) }
    var globalLoadingMessage by remember { mutableStateOf<String?>(null) }
    var pendingAgreements by remember { mutableStateOf<List<AgreementItem>>(emptyList()) }

    fun getClient(): BexPaymentClient = BexLiteSdk.getPaymentClient()

    fun mapWalletResultToState(result: WalletCheckResult): ClientFlowState {
        return when {
            !result.cards.isNullOrEmpty() -> ClientFlowState.Dashboard(result.cards!!)
            result.screen == ENTER_OTP_SCREEN && result.otpData != null ->
                ClientFlowState.EnterOtp(result.otpData!!)

            result.screen == LINK_SCREEN -> ClientFlowState.Link
            result.screen == PARTIAL_REGISTER_SCREEN -> ClientFlowState.PartialRegister
            else -> ClientFlowState.Link
        }
    }

    /** Shared insert-card logic used by both Compose and XML add-card flows. */
    fun handleInsertCard(
        tokenizedPan: BexTokenizedPan,
        aliasName: String?,
        onDone: () -> Unit
    ) {
        insertCardLoading = true
        scope.launch {
            withContext(Dispatchers.Main) {
                when (val res =
                    getClient().storeCard(tokenizedPan, aliasName)) {
                    is BexResult.Success -> {
                        insertCardLoading = false
                        if (res.data.otpData != null) {
                            state = ClientFlowState.EnterOtp(res.data.otpData!!)
                        } else {
                            when (val refresh = getClient().checkStatus()) {
                                is BexResult.Success -> state =
                                    ClientFlowState.Dashboard(refresh.data.cards ?: emptyList())

                                is BexResult.Error -> onDone()
                            }
                        }
                    }

                    is BexResult.Error -> {
                        insertCardLoading = false
                        onError(res.error)
                    }
                }
            }
        }
    }

    fun handleEvent(event: ClientFlowEvent) {
        when (event) {
            ClientFlowEvent.LinkClicked -> {
                linkLoading = true
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (val res = getClient().linkAccount()) {
                            is BexResult.Success -> {
                                linkLoading = false
                                when (res.data.screen) {
                                    ENTER_OTP_SCREEN -> {
                                        val otpData = res.data.otpData
                                        if (otpData != null) {
                                            state = ClientFlowState.EnterOtp(otpData)
                                        } else {
                                            onError(BexSdkError.Unknown("Invalid OTP data received"))
                                        }
                                    }

                                    WALLET_SCREEN -> {
                                        when (val walletRes = getClient().checkStatus()) {
                                            is BexResult.Success -> {
                                                val data = walletRes.data
                                                if (!data.cards.isNullOrEmpty()) {
                                                    state = ClientFlowState.Dashboard(data.cards!!)
                                                    Toast.makeText(
                                                        context,
                                                        "Cüzdan bağlandı",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    state = mapWalletResultToState(data)
                                                }
                                            }

                                            is BexResult.Error -> onError(walletRes.error)
                                        }
                                    }

                                    else -> onError(BexSdkError.Unknown("İşleminizi şu anda gerçekleştiremiyoruz."))
                                }
                            }

                            is BexResult.Error -> {
                                linkLoading = false
                                onError(res.error)
                            }
                        }
                    }
                }
            }

            is ClientFlowEvent.OtpVerify -> {
                val otpData = (state as? ClientFlowState.EnterOtp)?.otpData ?: return
                otpVerifyLoading = true
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (val res = getClient().verifyOTP(
                            otpValue = event.otpValue,
                            transactionId = otpData.transactionId,
                            otpRefNo = otpData.otpRefNo
                        )) {
                            is BexResult.Success -> {
                                otpVerifyLoading = false
                                if (res.data.screen == TRANSACTION_CONTROL_SCREEN &&
                                    !res.data.paymentToken.isNullOrBlank()
                                ) {
                                    globalLoadingMessage = "Ödeme kontrol ediliyor..."
                                    when (val ctrl =
                                        getClient().controlTransaction(res.data.paymentToken!!)) {
                                        is BexResult.Success -> {
                                            globalLoadingMessage = null
                                            state = ClientFlowState.TransactionResult(ctrl.data)
                                            return@withContext
                                        }

                                        is BexResult.Error -> {
                                            globalLoadingMessage = null
                                            onError(ctrl.error)
                                            return@withContext
                                        }
                                    }
                                }
                                res.data.paymentToken?.let { paymentToken ->
                                    Toast.makeText(
                                        context,
                                        "paymentToken: $paymentToken",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                if (!res.data.cards.isNullOrEmpty()) {
                                    state = ClientFlowState.Dashboard(res.data.cards!!)
                                } else if (res.data.screen == WALLET_SCREEN) {
                                    when (val walletRes = getClient().checkStatus()) {
                                        is BexResult.Success ->
                                            state = mapWalletResultToState(walletRes.data)

                                        is BexResult.Error -> onError(walletRes.error)
                                    }
                                } else {
                                    state = ClientFlowState.Link
                                    Toast.makeText(
                                        context,
                                        "Doğrulandı: ${res.data.screen}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            is BexResult.Error -> {
                                otpVerifyLoading = false
                                onError(res.error)
                            }
                        }
                    }
                }
            }

            ClientFlowEvent.OtpResend -> {
                val otpData = (state as? ClientFlowState.EnterOtp)?.otpData ?: return
                otpResendLoading = true
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (val res = getClient().resendOTP(
                            transactionId = otpData.transactionId,
                            otpRefNo = otpData.otpRefNo
                        )) {
                            is BexResult.Success -> {
                                otpResendLoading = false
                                state = ClientFlowState.EnterOtp(
                                    otpData.copy(
                                        transactionId = res.data.transactionId,
                                        durationTimeInSeconds = res.data.durationTimeInSeconds,
                                        otpLength = res.data.otpLength,
                                        gsmNo = res.data.gsmNo,
                                        otpRefNo = res.data.otpRefNo,
                                        sender = res.data.sender.orEmpty()
                                    )
                                )
                                Toast.makeText(
                                    context,
                                    "OTP yeniden gönderildi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is BexResult.Error -> {
                                otpResendLoading = false
                                onError(res.error)
                            }
                        }
                    }
                }
            }

            is ClientFlowEvent.Register -> {
                registerLoading = true
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (val res = getClient().registerWithTokenizedPan(
                            tokenizedPan = event.tokenizedPan,
                            aliasName = event.aliasName
                        )) {
                            is BexResult.Success -> {
                                registerLoading = false
                                if (res.data.otpData != null) {
                                    state = ClientFlowState.EnterOtp(res.data.otpData!!)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Kayıt tamamlandı: ${res.data.screen}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    state = ClientFlowState.Link
                                }
                            }

                            is BexResult.Error -> {
                                registerLoading = false
                                onError(res.error)
                            }
                        }
                    }
                }
            }

            // ---- Compose add-card flow ----
            ClientFlowEvent.AddCardClicked -> state = ClientFlowState.AddCard

            is ClientFlowEvent.AddCardSave -> {
                handleInsertCard(event.tokenizedPan, event.aliasName) {
                    state = ClientFlowState.AddCard
                }
            }

            ClientFlowEvent.AddCardDismiss -> {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (val res = getClient().checkStatus()) {
                            is BexResult.Success -> state = mapWalletResultToState(res.data)
                            is BexResult.Error -> state = ClientFlowState.Link
                        }
                    }
                }
            }

            // ---- XML add-card flow ----
            ClientFlowEvent.AddCardXmlClicked -> state = ClientFlowState.AddCardXml

            is ClientFlowEvent.AddCardXmlSave -> {
                handleInsertCard(event.tokenizedPan, event.aliasName) {
                    state = ClientFlowState.AddCardXml
                }
            }

            ClientFlowEvent.AddCardXmlDismiss -> {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (val res = getClient().checkStatus()) {
                            is BexResult.Success -> state = mapWalletResultToState(res.data)
                            is BexResult.Error -> state = ClientFlowState.Link
                        }
                    }
                }
            }

            is ClientFlowEvent.DeleteCard -> {
                deletingCardId = event.cardId
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (val res = getClient().cardDelete(event.cardId)) {
                            is BexResult.Success -> {
                                deletingCardId = null
                                state = ClientFlowState.Dashboard(res.data.cards ?: emptyList())
                                Toast.makeText(context, "Kart silindi", Toast.LENGTH_SHORT).show()
                            }

                            is BexResult.Error -> {
                                deletingCardId = null
                                onError(res.error)
                            }
                        }
                    }
                }
            }

            ClientFlowEvent.Back -> onBack()
        }
    }

    LaunchedEffect(initParams) {
        state = ClientFlowState.Loading
        withContext(Dispatchers.Main) {
            val client = getClient()
            when (val initRes = client.initializeSdk()) {
                is BexResult.Success -> {
                    val data: SdkInitResult = initRes.data
                    pendingAgreements = data.pendingAgreements
                    when (val checkRes = client.checkStatus()) {
                        is BexResult.Success -> state = mapWalletResultToState(checkRes.data)
                        is BexResult.Error -> state = ClientFlowState.Error(checkRes.error)
                    }
                }

                is BexResult.Error -> state = ClientFlowState.Error(initRes.error)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("SDK Akışı") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )

            when (val s = state) {
                is ClientFlowState.ThreeDS -> {
                    ThreeDSWebView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        tdsUrl = s.tdsUrl,
                        htmlForm = s.htmlForm,
                        isCompletionUrl = { url ->
                            url.contains(CHECKOUT_ODEMEGECIDI, ignoreCase = true)
                        },
                        onComplete = {
                            scope.launch {
                                globalLoadingMessage = "Ödeme kontrol ediliyor..."
                                when (val ctrl = getClient().controlTransaction(s.paymentToken)) {
                                    is BexResult.Success -> {
                                        globalLoadingMessage = null
                                        state = ClientFlowState.TransactionResult(ctrl.data)
                                    }

                                    is BexResult.Error -> {
                                        globalLoadingMessage = null
                                        state = ClientFlowState.TransactionResultError(
                                            message = ctrl.error.displayMessage,
                                            rawError = ctrl.error.toString()
                                        )
                                    }
                                }
                            }
                        },
                        onError = { message ->
                            state = ClientFlowState.TransactionResultError(
                                message = "3DS WebView error",
                                rawError = message
                            )
                        },
                        onCancel = {
                            scope.launch {
                                globalLoadingMessage = "Kartlar yükleniyor..."
                                when (val walletRes = getClient().checkStatus()) {
                                    is BexResult.Success -> {
                                        globalLoadingMessage = null
                                        state = ClientFlowState.Dashboard(
                                            walletRes.data.cards ?: emptyList()
                                        )
                                    }

                                    is BexResult.Error -> {
                                        globalLoadingMessage = null
                                        onError(walletRes.error)
                                        state = ClientFlowState.Link
                                    }
                                }
                            }
                        }
                    )
                }

                else -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ekran: ${
                            when (val s2 = state) {
                                is ClientFlowState.Loading -> "Loading"
                                is ClientFlowState.Error -> "Error"
                                is ClientFlowState.Link -> "Link"
                                is ClientFlowState.EnterOtp -> "EnterOtp"
                                is ClientFlowState.Dashboard -> "Dashboard (${s2.cards.size} kart)"
                                is ClientFlowState.PartialRegister -> "PartialRegister"
                                is ClientFlowState.AddCard -> "[Compose] Kart Ekle"
                                is ClientFlowState.AddCardXml -> "[XML] Kart Ekle"
                                is ClientFlowState.TransactionResult -> "TransactionResult"
                                is ClientFlowState.TransactionResultError -> "TransactionResultError"
                                is ClientFlowState.ThreeDS -> "ThreeDS"
                            }
                        }",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    when (s) {
                        is ClientFlowState.Loading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        is ClientFlowState.Error -> {
                            Text(
                                text = "Hata: ${s.error.displayMessage}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = onBack) { Text("Geri") }
                        }

                        is ClientFlowState.Link -> {
                            LinkScreen(
                                loading = linkLoading,
                                onLinkClick = { handleEvent(ClientFlowEvent.LinkClicked) }
                            )
                        }

                        is ClientFlowState.EnterOtp -> {
                            EnterOtpScreen(
                                otpData = s.otpData,
                                verifyLoading = otpVerifyLoading,
                                resendLoading = otpResendLoading,
                                onVerify = { handleEvent(ClientFlowEvent.OtpVerify(it)) },
                                onResend = { handleEvent(ClientFlowEvent.OtpResend) }
                            )
                        }

                        is ClientFlowState.Dashboard -> {
                            LaunchedEffect(s.cards) {
                                if (selectedCardId == null) {
                                    selectedCardId = s.cards.firstOrNull()?.cardId
                                } else if (s.cards.none { it.cardId == selectedCardId }) {
                                    selectedCardId = s.cards.firstOrNull()?.cardId
                                }
                            }
                            DashboardScreen(
                                cards = s.cards,
                                deletingCardId = deletingCardId,
                                selectedCardId = selectedCardId,
                                onSelectCard = { selectedCardId = it },
                                onCardSelected = onCardSelected,
                                onAddCardClick = { handleEvent(ClientFlowEvent.AddCardClicked) },
                                onAddCardXmlClick = { handleEvent(ClientFlowEvent.AddCardXmlClicked) },
                                onDeleteCard = { handleEvent(ClientFlowEvent.DeleteCard(it)) },
                                paymentAmountText = paymentAmount.toString(),
                                paymentSecurityLabel = paymentSecurity.demoShortLabel(),
                                onPay = pay@{
                                    val cardId = selectedCardId ?: return@pay
                                    scope.launch {
                                        withContext(Dispatchers.Main) {
                                            val payOrderId = orderId.trim()
                                                .ifBlank { "DEMO-${System.currentTimeMillis()}" }
                                            when (val payRes = getClient().startPayment(
                                                orderId = payOrderId,
                                                cardId = cardId,
                                                amount = paymentAmount,
                                                paymentSecurity = paymentSecurity
                                            )) {
                                                is BexResult.Success -> {
                                                    val data = payRes.data
                                                    if (data.is3D) {
                                                        val token = data.paymentToken
                                                        val tdsUrl = data.tdsUrl
                                                        if (!token.isNullOrBlank() && !tdsUrl.isNullOrBlank()) {
                                                            state = ClientFlowState.ThreeDS(
                                                                tdsUrl = tdsUrl,
                                                                htmlForm = data.htmlForm,
                                                                paymentToken = token
                                                            )
                                                            return@withContext
                                                        }
                                                    }

                                                    val otp = data.otpData
                                                    if (otp != null) {
                                                        state = ClientFlowState.EnterOtp(otp)
                                                        return@withContext
                                                    }

                                                    if (data.screen == TRANSACTION_CONTROL_SCREEN &&
                                                        !data.paymentToken.isNullOrBlank()
                                                    ) {
                                                        globalLoadingMessage =
                                                            "Ödeme kontrol ediliyor..."
                                                        when (val ctrl =
                                                            getClient().controlTransaction(data.paymentToken!!)) {
                                                            is BexResult.Success -> {
                                                                globalLoadingMessage = null
                                                                state =
                                                                    ClientFlowState.TransactionResult(
                                                                        ctrl.data
                                                                    )
                                                                return@withContext
                                                            }

                                                            is BexResult.Error -> {
                                                                globalLoadingMessage = null
                                                                onError(ctrl.error)
                                                                return@withContext
                                                            }
                                                        }
                                                    }

                                                    Toast.makeText(
                                                        context,
                                                        "paymentToken: ${data.paymentToken}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                is BexResult.Error -> onError(payRes.error)
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        is ClientFlowState.PartialRegister -> {
                            PartialRegisterScreen(
                                loading = registerLoading,
                                agreements = pendingAgreements,
                                onRegister = { tokenizedPan, alias ->
                                    handleEvent(
                                        ClientFlowEvent.Register(
                                            tokenizedPan,
                                            alias
                                        )
                                    )
                                }
                            )
                        }

                        is ClientFlowState.AddCard -> {
                            AddCardScreen(
                                loading = insertCardLoading,
                                agreements = pendingAgreements,
                                onSave = { tokenizedPan, alias ->
                                    handleEvent(
                                        ClientFlowEvent.AddCardSave(
                                            tokenizedPan,
                                            alias
                                        )
                                    )
                                },
                                onDismiss = { handleEvent(ClientFlowEvent.AddCardDismiss) }
                            )
                        }

                        is ClientFlowState.AddCardXml -> {
                            AddCardXmlScreen(
                                loading = insertCardLoading,
                                agreements = pendingAgreements,
                                onSave = { tokenizedPan, alias ->
                                    handleEvent(
                                        ClientFlowEvent.AddCardXmlSave(
                                            tokenizedPan,
                                            alias
                                        )
                                    )
                                },
                                onDismiss = { handleEvent(ClientFlowEvent.AddCardXmlDismiss) }
                            )
                        }

                        is ClientFlowState.TransactionResult -> {
                            TransactionResultScreen(result = s.result)
                        }

                        is ClientFlowState.TransactionResultError -> {
                            TransactionErrorResultScreen(
                                title = s.title,
                                message = s.message,
                                rawError = s.rawError
                            )
                        }

                        is ClientFlowState.ThreeDS -> Unit
                    }
                }
            }
        }

        globalLoadingMessage?.let { msg ->
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)
            ) {}
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                tonalElevation = 6.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier)
                    Text(text = msg, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
