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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
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
import org.json.JSONArray
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
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing16, vertical = Spacing12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Thông tin ứng dụng",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(Spacing12))
                Text(
                    text = "Thông tin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing8))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = Spacing16))
        Spacer(modifier = Modifier.height(Spacing12))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.DarkMode,
                contentDescription = "Giao diện",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(Spacing12))
            Text(
                text = "Giao diện",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(Spacing8))
        val themeModeState = LocalThemeMode.current
        Column(modifier = Modifier.padding(horizontal = Spacing16)) {
            ThemeMode.entries.forEach { mode ->
                val label = when (mode) {
                    ThemeMode.LIGHT -> "Sáng"
                    ThemeMode.DARK -> "Tối"
                    ThemeMode.SYSTEM -> "Theo hệ thống"
                    ThemeMode.SUNRISE_SUNSET -> "Theo mặt trời"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { themeModeState.value = mode },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeModeState.value == mode,
                        onClick = { themeModeState.value = mode }
                    )
                    Spacer(modifier = Modifier.width(Spacing8))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing12))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = Spacing16))
        Spacer(modifier = Modifier.height(Spacing16))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing16),
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
                        .padding(Spacing24),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Lịch Âm",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing4))
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
                    Spacer(modifier = Modifier.height(Spacing16))
                    Text(
                        text = "Lịch âm dương Việt Nam\nỨng dụng ngoại tuyến hoàn toàn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing16))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(Spacing16))

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
                        shape = RoundedCornerShape(Spacing16)
                    ) {
                        if (checking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Spacing24),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = Spacing2
                            )
                            Spacer(modifier = Modifier.width(Spacing12))
                            Text(
                                text = "Đang kiểm tra...",
                                style = MaterialTheme.typography.labelLarge
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.SystemUpdateAlt,
                                contentDescription = "Kiểm tra cập nhật",
                                modifier = Modifier.size(Spacing24)
                            )
                            Spacer(modifier = Modifier.width(Spacing12))
                            Text(
                                text = "Kiểm tra cập nhật",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(Spacing12))
                        Text(
                            text = errorMsg!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (updateInfo != null) {
                        Spacer(modifier = Modifier.height(Spacing16))
                        UpdateInfoCard(updateInfo!!, context)
                    }

                    Spacer(modifier = Modifier.height(Spacing16))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(Spacing8))
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
            val url = URL("https://api.github.com/repos/thichcode/lich_am/releases")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")

            if (conn.responseCode != 200) {
                onError("Lỗi kết nối: HTTP ${conn.responseCode}")
                return@Thread
            }

            val body = conn.inputStream.bufferedReader().readText()
            val releases = JSONArray(body)
            if (releases.length() == 0) {
                onError("Chưa có bản phát hành nào")
                return@Thread
            }
            val json = releases.getJSONObject(0)
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
            shape = RoundedCornerShape(Spacing12),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "Đã có bản mới: ${info.tag}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = Spacing16, vertical = Spacing8)
            )
        }

        Spacer(modifier = Modifier.height(Spacing12))

        OutlinedButton(
            onClick = { downloadAndInstall(context, info.downloadUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(Spacing16)
        ) {
            Icon(
                imageVector = Icons.Outlined.SystemUpdateAlt,
                contentDescription = "Tải xuống & Cài đặt",
                modifier = Modifier.size(Spacing24)
            )
            Spacer(modifier = Modifier.width(Spacing12))
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
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        downloadManager.enqueue(request)
        Toast.makeText(context, "Đang tải xuống: $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
