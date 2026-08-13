package com.bkm.mobil.sdk.lite.demo.flow

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun ThreeDSWebView(
    modifier: Modifier = Modifier,
    tdsUrl: String? = null,
    htmlForm: String? = null,
    allowWebViewHistoryBack: Boolean = false,
    enableUrlLogging: Boolean = true,
    logTag: String = "BexSDK-3DS",
    isCompletionUrl: (String) -> Boolean = { false },
    onUrlChanged: (String) -> Unit = {},
    onComplete: (String) -> Unit = {},
    onError: (String) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val latestOnUrlChanged by rememberUpdatedState(onUrlChanged)
    val latestOnComplete by rememberUpdatedState(onComplete)
    val latestOnError by rememberUpdatedState(onError)
    val latestIsCompletionUrl by rememberUpdatedState(isCompletionUrl)
    val latestEnableUrlLogging by rememberUpdatedState(enableUrlLogging)
    val latestLogTag by rememberUpdatedState(logTag)
    val latestAllowWebViewHistoryBack by rememberUpdatedState(allowWebViewHistoryBack)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasCompleted by remember { mutableStateOf(false) }

    BackHandler {
        val webView = webViewRef
        if (latestAllowWebViewHistoryBack && webView?.canGoBack() == true) {
            webView.goBack()
        } else {
            onCancel()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewRef = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString().orEmpty()
                        if (url.isNotBlank()) {
                            if (latestEnableUrlLogging) {
                                Log.d(
                                    latestLogTag,
                                    "3DS shouldOverrideUrlLoading url=$url " +
                                            "isForMainFrame=${request?.isForMainFrame} " +
                                            "method=${request?.method} " +
                                            "isRedirect=${request?.isRedirect}"
                                )
                            }
                            latestOnUrlChanged(url)
                            if (!hasCompleted && latestIsCompletionUrl(url)) {
                                hasCompleted = true
                                latestOnComplete(url)
                                return true
                            }
                        }
                        return false
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        if (url.isNullOrBlank()) return
                        if (latestEnableUrlLogging) {
                            Log.d(latestLogTag, "3DS onPageStarted url=$url")
                        }
                        latestOnUrlChanged(url)
                        if (!hasCompleted && latestIsCompletionUrl(url)) {
                            hasCompleted = true
                            latestOnComplete(url)
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        if (url.isNullOrBlank()) return
                        if (latestEnableUrlLogging) {
                            Log.d(latestLogTag, "3DS onPageFinished url=$url")
                        }
                        latestOnUrlChanged(url)
                        if (!hasCompleted && latestIsCompletionUrl(url)) {
                            hasCompleted = true
                            latestOnComplete(url)
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            if (latestEnableUrlLogging) {
                                Log.d(
                                    latestLogTag,
                                    "3DS onReceivedError url=${request.url} " +
                                            "error=${error?.description}"
                                )
                            }
                            latestOnError(error?.description?.toString() ?: "3DS page load failed")
                        }
                    }
                }

                when {
                    !htmlForm.isNullOrBlank() -> loadDataWithBaseURL(
                        tdsUrl,
                        htmlForm,
                        "text/html",
                        "UTF-8",
                        null
                    )

                    !tdsUrl.isNullOrBlank() -> loadUrl(tdsUrl)
                    else -> latestOnError("3DS content is empty")
                }
            }
        },
        update = { view ->
            webViewRef = view
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                destroy()
            }
            webViewRef = null
        }
    }
}

