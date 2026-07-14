package com.licham

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val ALLOWED_RSS_URLS = setOf(
    "https://vnexpress.net/rss/tin-moi-nhat.rss",
    "https://xskt.com.vn/rss-feed/mien-bac-xsmb.rss",
    "https://xskt.com.vn/rss-feed/mien-trung-xsmt.rss",
    "https://xskt.com.vn/rss-feed/mien-nam-xsmn.rss"
)

class AppWebViewClient(context: Context) : WebViewClientCompat() {
    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        assetLoader.shouldInterceptRequest(request.url)?.let { return it }
        if (request.url.host != "appassets.androidplatform.net" ||
            request.url.path != "/rss-proxy"
        ) {
            return null
        }

        val target = request.url.getQueryParameter("url")
            ?: return textResponse(400, "Missing RSS URL")
        if (target !in ALLOWED_RSS_URLS) {
            return textResponse(403, "RSS URL is not allowed")
        }
        return fetchRss(target)
    }

    private fun fetchRss(initialUrl: String): WebResourceResponse {
        var target = initialUrl
        repeat(MAX_REDIRECTS + 1) {
            val connection = (URL(target).openConnection() as HttpsURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = false
                setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml")
                setRequestProperty("User-Agent", "LichAm/1.2")
            }
            try {
                val status = connection.responseCode
                if (status in 300..399) {
                    val location = connection.getHeaderField("Location")
                        ?: return textResponse(502, "RSS redirect is missing a location")
                    val redirected = URL(URL(target), location).toString()
                    if (redirected !in ALLOWED_RSS_URLS) {
                        return textResponse(403, "RSS redirect is not allowed")
                    }
                    target = redirected
                    return@repeat
                }

                val stream = if (status >= 400) connection.errorStream else connection.inputStream
                val body = stream?.use(::readLimited) ?: ByteArray(0)
                return WebResourceResponse(
                    "application/xml",
                    "UTF-8",
                    status,
                    connection.responseMessage?.takeIf { it.isNotBlank() } ?: "RSS response",
                    mapOf("Cache-Control" to "no-store"),
                    ByteArrayInputStream(body)
                )
            } catch (_: Exception) {
                return textResponse(502, "Unable to load RSS data")
            } finally {
                connection.disconnect()
            }
        }
        return textResponse(502, "Too many RSS redirects")
    }

    private fun readLimited(input: java.io.InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > MAX_RESPONSE_BYTES) throw IOException("RSS response is too large")
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun textResponse(status: Int, message: String) = WebResourceResponse(
        "text/plain",
        "UTF-8",
        status,
        message,
        mapOf("Cache-Control" to "no-store"),
        ByteArrayInputStream(message.toByteArray(Charsets.UTF_8))
    )

    private companion object {
        const val TIMEOUT_MS = 10_000
        const val MAX_REDIRECTS = 3
        const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024
    }
}
