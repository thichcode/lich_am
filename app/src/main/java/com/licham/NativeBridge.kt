package com.licham

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface

class NativeBridge(private val context: Context) {
    @JavascriptInterface
    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
