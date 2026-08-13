package com.bkm.mobil.sdk.lite.demo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bkm.mobil.sdk.lite.api.BexEnvironment
import com.bkm.mobil.sdk.lite.api.PaymentSecurity
import com.bkm.mobil.sdk.lite.api.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.defaultsDataStore: DataStore<Preferences> by preferencesDataStore(name = "demo_defaults")

private val KEY_TOKEN = stringPreferencesKey("token")
private val KEY_MERCHANT_ID = stringPreferencesKey("merchant_id")
private val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
private val KEY_MERCHANT_USER_ID = stringPreferencesKey("merchant_user_id")
private val KEY_SUCCESS_URL = stringPreferencesKey("success_url")
private val KEY_FAIL_URL = stringPreferencesKey("fail_url")
private val KEY_PAYMENT_AMOUNT = stringPreferencesKey("payment_amount")
private val KEY_PAYMENT_CURRENCY = stringPreferencesKey("payment_currency")
private val KEY_ORDER_ID = stringPreferencesKey("order_id")
private val KEY_INSTALLMENT_COUNT = stringPreferencesKey("installment_count")
private val KEY_TRANSACTION_TYPE = stringPreferencesKey("transaction_type")
private val KEY_PAYMENT_SECURITY = stringPreferencesKey("payment_security")
private val KEY_ENVIRONMENT = stringPreferencesKey("environment")

private val BUILTIN_DEFAULTS = DemoFormState()

fun Context.getDefaultsFlow(): Flow<DemoFormState> =
    defaultsDataStore.data.map { prefs ->
        DemoFormState(
            token = prefs[KEY_TOKEN] ?: BUILTIN_DEFAULTS.token,
            merchantId = prefs[KEY_MERCHANT_ID] ?: BUILTIN_DEFAULTS.merchantId,
            gsmNo = prefs[KEY_PHONE_NUMBER] ?: BUILTIN_DEFAULTS.gsmNo,
            merchantUserId = prefs[KEY_MERCHANT_USER_ID] ?: BUILTIN_DEFAULTS.merchantUserId,
            successUrl = prefs[KEY_SUCCESS_URL] ?: BUILTIN_DEFAULTS.successUrl,
            failUrl = prefs[KEY_FAIL_URL] ?: BUILTIN_DEFAULTS.failUrl,
            paymentAmount = prefs[KEY_PAYMENT_AMOUNT] ?: BUILTIN_DEFAULTS.paymentAmount,
            paymentCurrency = prefs[KEY_PAYMENT_CURRENCY] ?: BUILTIN_DEFAULTS.paymentCurrency,
            orderId = prefs[KEY_ORDER_ID] ?: BUILTIN_DEFAULTS.orderId,
            installmentCount = prefs[KEY_INSTALLMENT_COUNT] ?: BUILTIN_DEFAULTS.installmentCount,
            transactionType = prefs[KEY_TRANSACTION_TYPE]?.let { name ->
                runCatching { TransactionType.valueOf(name) }.getOrNull()
            } ?: BUILTIN_DEFAULTS.transactionType,
            paymentSecurity = prefs[KEY_PAYMENT_SECURITY]?.let { name ->
                runCatching { PaymentSecurity.valueOf(name) }.getOrNull()
            } ?: BUILTIN_DEFAULTS.paymentSecurity,
            environment = prefs[KEY_ENVIRONMENT]?.let { name ->
                runCatching { BexEnvironment.valueOf(name) }.getOrNull()
            } ?: BUILTIN_DEFAULTS.environment
        )
    }

suspend fun Context.loadDefaults(): DemoFormState = getDefaultsFlow().first()

suspend fun Context.saveDefaults(state: DemoFormState) {
    defaultsDataStore.edit { prefs ->
        prefs[KEY_TOKEN] = state.token
        prefs[KEY_MERCHANT_ID] = state.merchantId
        prefs[KEY_PHONE_NUMBER] = state.gsmNo
        prefs[KEY_MERCHANT_USER_ID] = state.merchantUserId
        prefs[KEY_SUCCESS_URL] = state.successUrl
        prefs[KEY_FAIL_URL] = state.failUrl
        prefs[KEY_PAYMENT_AMOUNT] = state.paymentAmount
        prefs[KEY_PAYMENT_CURRENCY] = state.paymentCurrency
        prefs[KEY_ORDER_ID] = state.orderId
        prefs[KEY_INSTALLMENT_COUNT] = state.installmentCount
        prefs[KEY_TRANSACTION_TYPE] = state.transactionType.name
        prefs[KEY_PAYMENT_SECURITY] = state.paymentSecurity.name
        prefs[KEY_ENVIRONMENT] = state.environment.name
    }
}
