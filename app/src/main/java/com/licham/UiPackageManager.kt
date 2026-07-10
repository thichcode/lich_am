package com.licham

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipInputStream

data class UiPackageInfo(
    val uiVersion: Int,
    val minAppVersionCode: Int,
    val sha256: String
)

class UiPackageManager(private val appContext: Context) {
    companion object {
        const val UPDATE_BASE_URL = "https://lich-am-1.vercel.app/ui"
        const val MANIFEST_FILE = "manifest.json"
        const val PACKAGE_ZIP = "ui-package.zip"
        const val PACKAGE_DIR = "ui_package"
        const val BUNDLED_ASSET_PATH = "ui/default"

        @Volatile
        private var instance: UiPackageManager? = null

        fun getInstance(context: Context): UiPackageManager {
            return instance ?: synchronized(this) {
                instance ?: UiPackageManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val activeDir: File
        get() = File(appContext.filesDir, PACKAGE_DIR)

    fun getActivePackagePath(): String {
        if (activeDir.exists() && File(activeDir, "index.html").exists()) {
            return activeDir.absolutePath
        }
        return BUNDLED_ASSET_PATH
    }

    fun isUsingDownloaded(): Boolean {
        return activeDir.exists() && File(activeDir, "index.html").exists()
    }

    fun getCurrentUiVersion(): Int {
        return if (isUsingDownloaded()) {
            readManifest(activeDir)?.uiVersion ?: 1
        } else {
            readBundledManifest()?.uiVersion ?: 1
        }
    }

    private fun readBundledManifest(): UiPackageInfo? {
        return try {
            val json = appContext.assets.open("$BUNDLED_ASSET_PATH/$MANIFEST_FILE")
                .bufferedReader().use { it.readText() }
            parseManifest(json)
        } catch (e: Exception) { null }
    }

    private fun readManifest(dir: File): UiPackageInfo? {
        return try {
            val json = File(dir, MANIFEST_FILE).readText()
            parseManifest(json)
        } catch (e: Exception) { null }
    }

    private fun parseManifest(json: String): UiPackageInfo {
        val obj = JSONObject(json)
        return UiPackageInfo(
            uiVersion = obj.optInt("uiVersion", 1),
            minAppVersionCode = obj.optInt("minAppVersionCode", 1),
            sha256 = obj.optString("sha256", "")
        )
    }

    suspend fun checkForUpdate(): UiPackageInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$UPDATE_BASE_URL/$MANIFEST_FILE")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            if (conn.responseCode != 200) return@withContext null

            val json = conn.inputStream.bufferedReader().readText()
            val serverManifest = parseManifest(json)

            val appVersionCode = try {
                appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionCode
            } catch (e: Exception) { 0 }

            if (appVersionCode < serverManifest.minAppVersionCode) {
                Log.w("UiPackageManager", "App version $appVersionCode < min ${serverManifest.minAppVersionCode}")
                return@withContext null
            }

            if (serverManifest.uiVersion > getCurrentUiVersion()) {
                return@withContext serverManifest
            }
            return@withContext null
        } catch (e: Exception) {
            Log.e("UiPackageManager", "checkForUpdate failed", e)
            return@withContext null
        }
    }

    suspend fun downloadAndInstall(manifest: UiPackageInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$UPDATE_BASE_URL/$PACKAGE_ZIP")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 30000

            if (conn.responseCode != 200) {
                Log.e("UiPackageManager", "Download failed: HTTP ${conn.responseCode}")
                return@withContext false
            }

            val zipBytes = conn.inputStream.readBytes()

            if (manifest.sha256.isNotEmpty()) {
                val digest = MessageDigest.getInstance("SHA-256").digest(zipBytes)
                val hex = digest.joinToString("") { "%02x".format(it) }
                if (hex != manifest.sha256) {
                    Log.e("UiPackageManager", "SHA-256 mismatch")
                    return@withContext false
                }
            }

            activeDir.deleteRecursively()
            activeDir.mkdirs()

            val zis = ZipInputStream(zipBytes.inputStream())
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val file = File(activeDir, entry.name)
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos -> zis.copyTo(fos) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()

            val success = File(activeDir, "index.html").exists()
            if (!success) activeDir.deleteRecursively()
            return@withContext success
        } catch (e: Exception) {
            Log.e("UiPackageManager", "downloadAndInstall failed", e)
            activeDir.deleteRecursively()
            return@withContext false
        }
    }

    fun resetToBundled() {
        activeDir.deleteRecursively()
    }
}
