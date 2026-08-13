package com.bkm.mobil.sdk.lite.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bkm.mobil.sdk.lite.api.BexEnvironment
import com.bkm.mobil.sdk.lite.api.BexLiteSdk
import com.bkm.mobil.sdk.lite.api.BexSdkError
import com.bkm.mobil.sdk.lite.api.CardSelectionResult
import com.bkm.mobil.sdk.lite.api.PaymentSecurity
import com.bkm.mobil.sdk.lite.api.TransactionType
import com.bkm.mobil.sdk.lite.demo.ui.theme.BexLiteSdkTheme
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BexLiteSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoApp(
                        modifier = Modifier.systemBarsPadding(),
                        onError = ::handleError
                    )
                }
            }
        }
    }

    fun handleError(error: BexSdkError) {
        Toast.makeText(this, error.displayMessage, Toast.LENGTH_SHORT).show()
    }
}

sealed class DemoScreen {
    data object Home : DemoScreen()
    data class ClientFlow(
        val initParams: com.bkm.mobil.sdk.lite.api.SdkInitParams,
        /** Amount used by dashboard pay buttons (not part of [SdkInitParams]). */
        val paymentAmount: Double,
        /** Chosen on home screen; passed to [startPayment]. */
        val orderId: String,
        /** Chosen on home screen; passed to [startPayment]. */
        val paymentSecurity: PaymentSecurity,
        val onCardSelected: (CardSelectionResult) -> Unit
    ) : DemoScreen()
}

@Composable
fun DemoApp(
    modifier: Modifier = Modifier,
    onError: (BexSdkError) -> Unit
) {
    var currentScreen by remember { mutableStateOf<DemoScreen>(DemoScreen.Home) }

    when (val screen = currentScreen) {
        is DemoScreen.Home -> {
            DemoScreen(
                modifier = modifier,
                onError = onError,
                onStartClientFlow = { initParams, paymentAmount, orderId, paymentSecurity, onCardSelected ->
                    currentScreen = DemoScreen.ClientFlow(
                        initParams = initParams,
                        paymentAmount = paymentAmount,
                        orderId = orderId,
                        paymentSecurity = paymentSecurity,
                        onCardSelected = onCardSelected
                    )
                }
            )
        }

        is DemoScreen.ClientFlow -> {
            ClientFlowScreen(
                modifier = modifier,
                initParams = screen.initParams,
                paymentAmount = screen.paymentAmount,
                orderId = screen.orderId,
                paymentSecurity = screen.paymentSecurity,
                onError = onError,
                onCardSelected = screen.onCardSelected,
                onBack = { currentScreen = DemoScreen.Home }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    onError: (BexSdkError) -> Unit,
    onStartClientFlow: (
        com.bkm.mobil.sdk.lite.api.SdkInitParams,
        Double,
        String,
        PaymentSecurity,
        (CardSelectionResult) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val formStateState = remember { mutableStateOf(DemoFormState()) }
    val defaultsState = remember { mutableStateOf<DemoFormState?>(null) }
    var selectedCard by remember { mutableStateOf<CardSelectionResult?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showSetDefaultsDialog by remember { mutableStateOf(false) }
    var showEnvironmentDialog by remember { mutableStateOf(false) }
    var paymentSecurityMenuExpanded by remember { mutableStateOf(false) }
    var paymentInfoExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val loaded = context.loadDefaults()
        defaultsState.value = loaded
        formStateState.value =
            (loaded ?: DemoFormState()).copy(transactionId = UUID.randomUUID().toString())
    }

    val defaults = defaultsState.value ?: DemoFormState()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("BEX LITE SDK Demo") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            actions = {
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set Defaults") },
                            onClick = {
                                menuExpanded = false
                                showSetDefaultsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Environment") },
                            onClick = {
                                menuExpanded = false
                                showEnvironmentDialog = true
                            }
                        )
                    }
                }
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Init params inputs
            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SDK Init Parameters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { formStateState.value = defaults }) {
                    Text("Reset to defaults")
                }
            }
            OutlinedTextField(
                value = formStateState.value.token,
                onValueChange = { formStateState.value = formStateState.value.copy(token = it) },
                label = { Text("Token") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                trailingIcon = {
                    if (formStateState.value.token.isNotBlank()) {
                        IconButton(onClick = {
                            formStateState.value = formStateState.value.copy(token = "")
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = formStateState.value.merchantId,
                onValueChange = {
                    formStateState.value = formStateState.value.copy(merchantId = it)
                },
                label = { Text("Merchant ID") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (formStateState.value.merchantId.isNotBlank()) {
                        IconButton(onClick = {
                            formStateState.value =
                                formStateState.value.copy(merchantId = "")
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = formStateState.value.gsmNo,
                onValueChange = {
                    formStateState.value = formStateState.value.copy(gsmNo = it)
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                trailingIcon = {
                    if (formStateState.value.gsmNo.isNotBlank()) {
                        IconButton(onClick = {
                            formStateState.value =
                                formStateState.value.copy(gsmNo = "")
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = formStateState.value.merchantUserId,
                onValueChange = {
                    formStateState.value = formStateState.value.copy(merchantUserId = it)
                },
                label = { Text("Merchant User ID") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (formStateState.value.merchantUserId.isNotBlank()) {
                        IconButton(onClick = {
                            formStateState.value =
                                formStateState.value.copy(merchantUserId = "")
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ödeme Bilgileri",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { paymentInfoExpanded = !paymentInfoExpanded }) {
                    Icon(
                        imageVector = if (paymentInfoExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (paymentInfoExpanded) "Daralt" else "Genişlet"
                    )
                }
            }
            AnimatedVisibility(visible = paymentInfoExpanded) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formStateState.value.paymentAmount,
                            onValueChange = {
                                formStateState.value =
                                    formStateState.value.copy(paymentAmount = it)
                            },
                            label = { Text("Payment Amount") },
                            modifier = Modifier.weight(0.75f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                if (formStateState.value.paymentAmount.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            formStateState.value =
                                                formStateState.value.copy(paymentAmount = "")
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        )
                        OutlinedTextField(
                            value = formStateState.value.paymentCurrency,
                            onValueChange = {
                                formStateState.value =
                                    formStateState.value.copy(paymentCurrency = it)
                            },
                            label = { Text("Currency") },
                            modifier = Modifier.weight(0.25f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formStateState.value.orderId,
                        onValueChange = {
                            formStateState.value = formStateState.value.copy(orderId = it)
                        },
                        label = { Text("Order ID") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (formStateState.value.orderId.isNotBlank()) {
                                IconButton(onClick = {
                                    formStateState.value =
                                        formStateState.value.copy(orderId = "")
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formStateState.value.installmentCount,
                        onValueChange = {
                            formStateState.value =
                                formStateState.value.copy(installmentCount = it)
                        },
                        label = { Text("Taksit Sayısı") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            if (formStateState.value.installmentCount.isNotBlank()) {
                                IconButton(onClick = {
                                    formStateState.value =
                                        formStateState.value.copy(installmentCount = "")
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var transactionTypeExpanded by remember { mutableStateOf(false) }
                    Text(
                        text = "İşlem Türü",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        OutlinedButton(
                            onClick = { transactionTypeExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    when (formStateState.value.transactionType) {
                                        TransactionType.SALE -> "Satış (SALE)"
                                        TransactionType.PRE_AUTH -> "Ön Provizyon (PRE_AUTH)"
                                        TransactionType.RECURRING -> "Tekrarlayan (RECURRING)"
                                    },
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = transactionTypeExpanded,
                            onDismissRequest = { transactionTypeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Satış (SALE)") },
                                onClick = {
                                    formStateState.value = formStateState.value.copy(
                                        transactionType = TransactionType.SALE
                                    )
                                    transactionTypeExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ön Provizyon (PRE_AUTH)") },
                                onClick = {
                                    formStateState.value = formStateState.value.copy(
                                        transactionType = TransactionType.PRE_AUTH
                                    )
                                    transactionTypeExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Tekrarlayan (RECURRING)") },
                                onClick = {
                                    formStateState.value = formStateState.value.copy(
                                        transactionType = TransactionType.RECURRING
                                    )
                                    transactionTypeExpanded = false
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formStateState.value.successUrl,
                        onValueChange = {
                            formStateState.value =
                                formStateState.value.copy(successUrl = it)
                        },
                        label = { Text("Success URL") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        trailingIcon = {
                            if (formStateState.value.successUrl.isNotBlank()) {
                                IconButton(onClick = {
                                    formStateState.value =
                                        formStateState.value.copy(successUrl = "")
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = formStateState.value.failUrl,
                        onValueChange = {
                            formStateState.value = formStateState.value.copy(failUrl = it)
                        },
                        label = { Text("Fail URL") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        trailingIcon = {
                            if (formStateState.value.failUrl.isNotBlank()) {
                                IconButton(onClick = {
                                    formStateState.value =
                                        formStateState.value.copy(failUrl = "")
                                }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = paymentSecurityMenuExpanded,
                onExpandedChange = { paymentSecurityMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = formStateState.value.paymentSecurity.demoShortLabel(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ödeme güvenliği") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentSecurityMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = paymentSecurityMenuExpanded,
                    onDismissRequest = { paymentSecurityMenuExpanded = false }
                ) {
                    PaymentSecurity.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.demoShortLabel()) },
                            onClick = {
                                formStateState.value =
                                    formStateState.value.copy(paymentSecurity = option)
                                paymentSecurityMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Selected Card (if any)
            selectedCard?.let { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selected Card",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.selectedCard.cardAlias.ifBlank {
                                card.selectedCard.bankInformation.bankShortName
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${card.selectedCard.bankInformation.cardBrand} • ${card.selectedCard.maskCardNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val form = formStateState.value
                    val payAmount = form.paymentAmount.toDoubleOrNull()
                    if (payAmount == null) {
                        Toast.makeText(
                            context,
                            "Please enter a valid payment amount",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val params = form.copy(environment = defaults.environment).toInitParams()
                    if (params == null) {
                        Toast.makeText(
                            context,
                            "Please fill all init fields",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    initPaymentSdk(
                        context = context,
                        initParams = params,
                        onError = onError,
                        onCardSelected = { selectedCard = it }
                    )
                    onStartClientFlow(params, payAmount, form.orderId, form.paymentSecurity) {
                        selectedCard = it
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SDK Başlat")
            }
        }
    }

    if (showSetDefaultsDialog) {
        SetDefaultsDialog(
            currentDefaults = defaults,
            onDismiss = { showSetDefaultsDialog = false },
            onSave = { newDefaults ->
                scope.launch {
                    context.saveDefaults(newDefaults)
                    defaultsState.value = newDefaults
                    showSetDefaultsDialog = false
                }
            }
        )
    }
    if (showEnvironmentDialog) {
        EnvironmentDialog(
            current = defaults.environment,
            onDismiss = { showEnvironmentDialog = false },
            onSave = { env ->
                scope.launch {
                    val updated = defaults.copy(environment = env)
                    context.saveDefaults(updated)
                    defaultsState.value = updated
                    formStateState.value = formStateState.value.copy(environment = env)
                    showEnvironmentDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetDefaultsDialog(
    currentDefaults: DemoFormState,
    onDismiss: () -> Unit,
    onSave: (DemoFormState) -> Unit
) {
    var token by remember(currentDefaults) { mutableStateOf(currentDefaults.token) }
    var merchantId by remember(currentDefaults) { mutableStateOf(currentDefaults.merchantId) }
    var gsmNo by remember(currentDefaults) { mutableStateOf(currentDefaults.gsmNo) }
    var merchantUserId by remember(currentDefaults) { mutableStateOf(currentDefaults.merchantUserId) }
    var successUrl by remember(currentDefaults) { mutableStateOf(currentDefaults.successUrl) }
    var failUrl by remember(currentDefaults) { mutableStateOf(currentDefaults.failUrl) }
    var paymentAmount by remember(currentDefaults) { mutableStateOf(currentDefaults.paymentAmount) }
    var paymentCurrency by remember(currentDefaults) { mutableStateOf(currentDefaults.paymentCurrency) }
    var orderId by remember(currentDefaults) { mutableStateOf(currentDefaults.orderId) }
    var installmentCount by remember(currentDefaults) { mutableStateOf(currentDefaults.installmentCount) }
    var transactionType by remember(currentDefaults) { mutableStateOf(currentDefaults.transactionType) }
    var paymentSecurity by remember(currentDefaults) { mutableStateOf(currentDefaults.paymentSecurity) }
    var paymentSecurityExpanded by remember { mutableStateOf(false) }
    var transactionTypeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Defaults") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Token") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = merchantId,
                    onValueChange = { merchantId = it },
                    label = { Text("Merchant ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gsmNo,
                    onValueChange = { gsmNo = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = merchantUserId,
                    onValueChange = { merchantUserId = it },
                    label = { Text("Merchant User ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = successUrl,
                    onValueChange = { successUrl = it },
                    label = { Text("Success URL") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                OutlinedTextField(
                    value = failUrl,
                    onValueChange = { failUrl = it },
                    label = { Text("Fail URL") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Payment Amount") },
                        modifier = Modifier.weight(0.75f)
                    )
                    OutlinedTextField(
                        value = paymentCurrency,
                        onValueChange = { paymentCurrency = it },
                        label = { Text("Currency") },
                        modifier = Modifier.weight(0.25f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = orderId,
                    onValueChange = { orderId = it },
                    label = { Text("Order ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = installmentCount,
                    onValueChange = { installmentCount = it },
                    label = { Text("Taksit Sayısı") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ExposedDropdownMenuBox(
                    expanded = transactionTypeExpanded,
                    onExpandedChange = { transactionTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = when (transactionType) {
                            TransactionType.SALE -> "Satış (SALE)"
                            TransactionType.PRE_AUTH -> "Ön Provizyon (PRE_AUTH)"
                            TransactionType.RECURRING -> "Tekrarlayan (RECURRING)"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("İşlem Türü") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = transactionTypeExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = transactionTypeExpanded,
                        onDismissRequest = { transactionTypeExpanded = false }
                    ) {
                        TransactionType.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (option) {
                                            TransactionType.SALE -> "Satış (SALE)"
                                            TransactionType.PRE_AUTH -> "Ön Provizyon (PRE_AUTH)"
                                            TransactionType.RECURRING -> "Tekrarlayan (RECURRING)"
                                        }
                                    )
                                },
                                onClick = {
                                    transactionType = option
                                    transactionTypeExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = paymentSecurityExpanded,
                    onExpandedChange = { paymentSecurityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentSecurity.demoShortLabel(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment security (demo)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentSecurityExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = paymentSecurityExpanded,
                        onDismissRequest = { paymentSecurityExpanded = false }
                    ) {
                        PaymentSecurity.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.demoShortLabel()) },
                                onClick = {
                                    paymentSecurity = option
                                    paymentSecurityExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        DemoFormState(
                            token = token,
                            merchantId = merchantId,
                            gsmNo = gsmNo,
                            merchantUserId = merchantUserId,
                            successUrl = successUrl,
                            failUrl = failUrl,
                            paymentAmount = paymentAmount,
                            paymentCurrency = paymentCurrency,
                            orderId = orderId,
                            installmentCount = installmentCount,
                            transactionType = transactionType,
                            paymentSecurity = paymentSecurity,
                            environment = currentDefaults.environment
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EnvironmentDialog(
    current: BexEnvironment,
    onDismiss: () -> Unit,
    onSave: (BexEnvironment) -> Unit
) {
    var selected by remember(current) { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Environment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BexEnvironment.entries.forEach { env ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = env },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == env,
                            onClick = { selected = env }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(env.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selected) }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun initPaymentSdk(
    context: android.content.Context,
    initParams: com.bkm.mobil.sdk.lite.api.SdkInitParams,
    onError: (BexSdkError) -> Unit,
    onCardSelected: (CardSelectionResult) -> Unit
) {
    BexLiteSdk.init(
        context = context,
        initParams = initParams
    )
}
