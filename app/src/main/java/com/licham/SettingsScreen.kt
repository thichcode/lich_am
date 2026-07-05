package com.licham

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var checking by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thông tin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Lịch Âm",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val currentVersion = remember {
                        try {
                            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1"
                        } catch (_: Exception) { "1.1" }
                    }
                    Text(
                        text = "Phiên bản $currentVersion",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Lịch âm dương Việt Nam\nỨng dụng ngoại tuyến hoàn toàn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            checking = true
                            errorMsg = null
                            updateInfo = null
                            checkUpdate(context, onResult = { info ->
                                checking = false
                                if (info != null) updateInfo = info
                                else errorMsg = "Không có kết nối mạng"
                            }, onError = { msg ->
                                checking = false
                                errorMsg = msg
                            })
                        },
                        enabled = !checking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (checking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Đang kiểm tra...",
                                style = MaterialTheme.typography.labelLarge
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.SystemUpdateAlt,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Kiểm tra cập nhật",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMsg!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (updateInfo != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        UpdateInfoCard(updateInfo!!, context)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Thuật toán lịch âm: Hồ Ngọc Đức",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "© 2026 Lịch Âm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class UpdateInfo(
    val tag: String,
    val downloadUrl: String
)

private fun checkUpdate(
    context: Context,
    onResult: (UpdateInfo?) -> Unit,
    onError: (String) -> Unit
) {
    Thread {
        try {
            val url = URL("https://api.github.com/repos/thichcode/lich_am/releases/latest")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")

            if (conn.responseCode != 200) {
                onError("Lỗi kết nối: HTTP ${conn.responseCode}")
                return@Thread
            }

            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            val tag = json.getString("tag_name")

            val currentVer = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (_: Exception) { "" }

            if (tag.contains(currentVer, ignoreCase = true) && tag.endsWith(currentVer)) {
                onResult(null)
                return@Thread
            }

            val assets = json.getJSONArray("assets")
            var apkUrl = ""
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.getString("name").endsWith(".apk")) {
                    apkUrl = asset.getString("browser_download_url")
                    break
                }
            }

            if (apkUrl.isEmpty()) {
                onError("Không tìm thấy file APK")
                return@Thread
            }

            onResult(UpdateInfo(tag, apkUrl))
        } catch (e: Exception) {
            onError(e.message ?: "Lỗi không xác định")
        }
    }.start()
}

@Composable
private fun UpdateInfoCard(info: UpdateInfo, context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "Đã có bản mới: ${info.tag}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { downloadAndInstall(context, info.downloadUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.SystemUpdateAlt,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Tải xuống & Cài đặt",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun downloadAndInstall(context: Context, apkUrl: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(apkUrl)
        val fileName = "licham-${System.currentTimeMillis()}.apk"

        val request = DownloadManager.Request(uri).apply {
            setTitle("Lịch Âm")
            setDescription("Đang tải phiên bản mới...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        downloadManager.enqueue(request)
        Toast.makeText(context, "Đang tải xuống: $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
