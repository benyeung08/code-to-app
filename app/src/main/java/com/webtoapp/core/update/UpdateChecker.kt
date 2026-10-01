
package com.webtoapp.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import com.webtoapp.BuildConfig

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("name") val name: String,
    @SerializedName("body") val body: String,
    @SerializedName("assets") val assets: List<GitHubAsset>,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("published_at") val publishedAt: String
)

data class GitHubAsset(
    @SerializedName("name") val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String,
    @SerializedName("size") val size: Long
)

data class OwnUpdateResult(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val currentCode: Int,
    val latestCode: Int,
    val releaseNotes: String,
    val downloadUrl: String?,
    val htmlUrl: String,
    val assetName: String?
)

object UpdateChecker {
    const val OWNER = "benyeung08"
    const val REPO = "web-to-app"
    const val API_LATEST = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    const val API_ALL = "https://api.github.com/repos/$OWNER/$REPO/releases?per_page=20"
    private val client = OkHttpClient()

    // 解析 v1.0.1 / 1.0.1 / v1.0.0-mobilecode -> 1.0.1
    private fun cleanTag(tag: String): String {
        return tag.removePrefix("v").substringBefore("-").trim()
    }

    private fun compareVersion(a: String, b: String): Int {
        val pa = cleanTag(a).split(".").map { it.toIntOrNull() ?: 0 }
        val pb = cleanTag(b).split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val va = pa.getOrElse(i){0}
            val vb = pb.getOrElse(i){0}
            if (va != vb) return va.compareTo(vb)
        }
        return 0
    }

    fun pickBestApkAsset(assets: List<GitHubAsset>): GitHubAsset? {
        // 優先: standardRelease, 然後任意 apk, 最後第一個
        val priority = listOf("standard", "release", "app", ".apk")
        return assets.filter { it.name.endsWith(".apk") }
            .sortedByDescending { asset ->
                var score = 0
                if (asset.name.contains("standard", true)) score += 100
                if (asset.name.contains("release", true)) score += 50
                if (!asset.name.contains("gplay", true)) score += 20
                score
            }.firstOrNull() ?: assets.firstOrNull { it.name.endsWith(".apk") }
    }

    suspend fun checkOwnUpdate(): OwnUpdateResult = withContext(Dispatchers.IO) {
        val currentName = BuildConfig.VERSION_NAME
        val currentCode = BuildConfig.VERSION_CODE
        try {
            val req = Request.Builder()
                .url(API_LATEST)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "benyeung08-web-to-app")
                .build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                return@withContext OwnUpdateResult(false, currentName, currentName, currentCode, currentCode, "檢查失敗: ${resp.code}", null, "https://github.com/$OWNER/$REPO/releases", null)
            }
            val bodyStr = resp.body?.string() ?: ""
            // 簡易解析，避免引入過多依賴，用 Gson
            val gson = com.google.gson.Gson()
            val release = gson.fromJson(bodyStr, GitHubRelease::class.java)
            val latestTag = release.tagName
            val asset = pickBestApkAsset(release.assets)
            val hasUpdate = compareVersion(currentName, latestTag) < 0
            // 如果 versionName 相同但 versionCode 不同也算有更新，方便 mobilecode 1 -> 2
            val hasCodeUpdate = release.assets.isNotEmpty() && currentName == cleanTag(latestTag) // 簡化，實際可解析 release body 內的 versionCode
            return@withContext OwnUpdateResult(
                hasUpdate = hasUpdate,
                currentVersion = currentName,
                latestVersion = latestTag,
                currentCode = currentCode,
                latestCode = currentCode + if (hasUpdate) 1 else 0,
                releaseNotes = release.body ?: "",
                downloadUrl = asset?.downloadUrl,
                htmlUrl = release.htmlUrl,
                assetName = asset?.name
            )
        } catch (e: Exception) {
            return@withContext OwnUpdateResult(false, currentName, currentName, currentCode, currentCode, "檢查失敗: ${e.message}", null, "https://github.com/$OWNER/$REPO/releases", null)
        }
    }

    suspend fun fetchAllReleases(): List<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(API_ALL).header("Accept", "application/vnd.github.v3+json").header("User-Agent", "benyeung08-web-to-app").build()
            val resp = client.newCall(req).execute()
            val str = resp.body?.string() ?: return@withContext emptyList()
            val gson = com.google.gson.Gson()
            val type = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, GitHubRelease::class.java).type
            gson.fromJson<List<GitHubRelease>>(str, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun downloadApk(context: Context, url: String, onProgress: (Int) -> Unit): File = withContext(Dispatchers.IO) {
        val dir = File(context.getExternalFilesDir(null), "update_apks").apply { mkdirs() }
        val outFile = File(dir, "web-to-app-${System.currentTimeMillis()}.apk")
        val req = Request.Builder().url(url).header("User-Agent", "benyeung08-web-to-app").build()
        val resp = client.newCall(req).execute()
        val body = resp.body ?: throw Exception("empty body")
        val total = body.contentLength()
        var readBytes = 0L
        body.byteStream().use { input ->
            outFile.outputStream().use { output ->
                val buf = ByteArray(8192)
                var len: Int
                while (input.read(buf).also { len = it } != -1) {
                    output.write(buf, 0, len)
                    readBytes += len
                    if (total > 0) onProgress(((readBytes * 100) / total).toInt())
                }
            }
        }
        outFile
    }

    fun installApk(context: Context, apkFile: File) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        } else {
            Uri.fromFile(apkFile)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
