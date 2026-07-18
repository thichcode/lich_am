package com.licham

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewHomeScreen() {
    var wv by remember { mutableStateOf<WebView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                @Suppress("DEPRECATION")
                settings.setAllowUniversalAccessFromFileURLs(false)
                @Suppress("DEPRECATION")
                settings.setAllowFileAccessFromFileURLs(false)
                settings.setSupportMultipleWindows(false)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)

                webViewClient = AppWebViewClient(ctx)
                webChromeClient = WebChromeClient()
                addJavascriptInterface(NativeBridge(ctx), "nativeApp")

                loadUrl("https://appassets.androidplatform.net/assets/ui/default/index.html")
                wv = this
            }
        },
        update = { }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    wv?.onResume()
                    wv?.evaluateJavascript(
                        "window.onNativeResume && window.onNativeResume()",
                        null
                    )
                }
                Lifecycle.Event.ON_PAUSE -> wv?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            wv?.destroy()
        }
    }
}
