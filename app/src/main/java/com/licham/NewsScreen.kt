package com.licham

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Xml
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class NewsItem(
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String
)

@Composable
fun NewsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("news_cache", Context.MODE_PRIVATE) }
    var news by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val cachedXml = prefs.getString("rss_xml", null)
        val cachedTime = prefs.getLong("rss_time", 0)
        if (cachedXml != null && System.currentTimeMillis() - cachedTime < 3600000L) {
            news = parseRss(cachedXml)
            loading = false
        }
        val result = fetchRss(context)
        result.onSuccess { xml ->
            val items = parseRss(xml)
            news = items
            prefs.edit()
                .putString("rss_xml", xml)
                .putLong("rss_time", System.currentTimeMillis())
                .apply()
            error = null
            loading = false
        }.onFailure { e ->
            error = e.message ?: "Lỗi không xác định"
            if (news.isEmpty()) loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = Spacing1
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing16, vertical = Spacing12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Article,
                    contentDescription = "Tin tức",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(Spacing12))
                Text(
                    text = "Tin tức",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    refreshing = true
                    error = null
                    scope.launch {
                        val result = fetchRss(context)
                        result.onSuccess { xml ->
                            val items = parseRss(xml)
                            news = items
                            prefs.edit()
                                .putString("rss_xml", xml)
                                .putLong("rss_time", System.currentTimeMillis())
                                .apply()
                            error = null
                            refreshing = false
                        }.onFailure { e ->
                            error = e.message ?: "Lỗi không xác định"
                            refreshing = false
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Làm mới",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Spacing24)
                    )
                }
            }
        }

        if (refreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        when {
            loading && news.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(Spacing12))
                        Text(
                            text = "Đang tải tin...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            error != null && news.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Không thể tải tin tức",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(Spacing8))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing16))
                        OutlinedButton(onClick = {
                            refreshing = true
                            error = null
                            scope.launch {
                                val result = fetchRss(context)
                                result.onSuccess { xml ->
                                    val items = parseRss(xml)
                                    news = items
                                    prefs.edit()
                                        .putString("rss_xml", xml)
                                        .putLong("rss_time", System.currentTimeMillis())
                                        .apply()
                                    error = null
                                    refreshing = false
                                }.onFailure { e ->
                                    error = e.message ?: "Lỗi không xác định"
                                    refreshing = false
                                }
                            }
                        }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing12),
                    verticalArrangement = Arrangement.spacedBy(Spacing8)
                ) {
                    items(news) { item ->
                        NewsCard(item, context)
                    }
                }
            }
        }
    }
}

private suspend fun fetchRss(context: Context): Result<String> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://vnexpress.net/rss/tin-moi-nhat.rss")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        if (conn.responseCode != 200) {
            return@withContext Result.failure(IOException("HTTP ${conn.responseCode}"))
        }
        val xml = conn.inputStream.bufferedReader().readText()
        Result.success(xml)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun parseRss(xml: String): List<NewsItem> {
    val items = mutableListOf<NewsItem>()
    try {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))
        var eventType = parser.eventType
        var title = ""
        var description = ""
        var link = ""
        var pubDate = ""
        var inItem = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "item" -> {
                            inItem = true
                            title = ""
                            description = ""
                            link = ""
                            pubDate = ""
                        }
                        "title" -> if (inItem) title = parser.nextText().trim()
                        "description" -> {
                            if (inItem) {
                                val raw = parser.nextText().trim()
                                description = raw.replace(Regex("<[^>]*>"), "").trim()
                                if (description.length > 150) description = description.take(150) + "..."
                            }
                        }
                        "link" -> if (inItem) link = parser.nextText().trim()
                        "pubDate" -> if (inItem) {
                            val raw = parser.nextText().trim()
                            pubDate = formatRssDate(raw)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item" && inItem) {
                        items.add(NewsItem(title, description, link, pubDate))
                        inItem = false
                    }
                }
            }
            eventType = parser.next()
        }
    } catch (_: Exception) {}
    return items.take(30)
}

private fun formatRssDate(rssDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd/MM HH:mm", Locale("vi"))
        val date = inputFormat.parse(rssDate)
        outputFormat.format(date ?: return rssDate.take(16))
    } catch (_: Exception) {
        rssDate.take(16)
    }
}

@Composable
private fun NewsCard(item: NewsItem, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                context.startActivity(intent)
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
    ) {
        Column(modifier = Modifier.padding(Spacing14)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing4))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(Spacing4))
            Text(
                text = item.pubDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}