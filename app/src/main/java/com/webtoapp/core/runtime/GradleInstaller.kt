
package com.webtoapp.core.runtime

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.zip.ZipFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.FileInputStream

class GradleInstaller(private val context: Context) {
    private val client = OkHttpClient()

    // 這些 URL 複用你現有的 mirror 邏輯 (類似 Node.js/PHP 下載)
    companion object {
        // OpenJDK 17 arm64 from Adoptium / mirror
        const val JDK_URL = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.11_9.tar.gz"
        const val JDK_CN_MIRROR = "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/aarch64/linux/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.11_9.tar.gz"

        const val CMDLINE_TOOLS_URL = "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
        const val GRADLE_URL = "https://services.gradle.org/distributions/gradle-8.7-bin.zip"
    }

    suspend fun installJdk(onLog: (String)->Unit) = withContext(Dispatchers.IO) {
        val runtime = NativeBuildRuntime(context)
        val jdkDir = runtime.getJdkDir()
        jdkDir.mkdirs()
        val tarFile = File(context.cacheDir, "jdk17.tar.gz")
        try {
            downloadWithMirror(JDK_URL, JDK_CN_MIRROR, tarFile, onLog)
            onLog("解壓 JDK...")
            untarGz(tarFile, jdkDir.parentFile!!)
            // 找到解壓後的 jdk-17.0.11+9 目錄，重命名為 jdk17
            val extracted = jdkDir.parentFile!!.listFiles()?.firstOrNull { it.name.startsWith("jdk-17") }
            extracted?.renameTo(jdkDir)
            File(jdkDir, "bin/java").setExecutable(true)
            onLog("JDK 安裝完成")
        } catch (e: Exception) {
            onLog("JDK 安裝失敗: ${e.message}")
            throw e
        }
    }

    suspend fun installCmdlineTools(onLog: (String)->Unit) = withContext(Dispatchers.IO) {
        val runtime = NativeBuildRuntime(context)
        val sdkDir = runtime.getSdkDir()
        val zipFile = File(context.cacheDir, "cmdline-tools.zip")
        downloadWithMirror(CMDLINE_TOOLS_URL, CMDLINE_TOOLS_URL, zipFile, onLog)
        onLog("解壓 cmdline-tools...")
        val tmp = File(context.cacheDir, "cmdline_tmp").apply { mkdirs(); deleteRecursively(); mkdirs() }
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val out = File(tmp, entry.name)
                if (entry.isDirectory) out.mkdirs() else {
                    out.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { ins -> out.outputStream().use { o -> ins.copyTo(o) } }
                }
            }
        }
        // cmdline-tools/latest
        val latest = File(sdkDir, "cmdline-tools/latest").apply { mkdirs() }
        File(tmp, "cmdline-tools").copyRecursively(latest, overwrite = true)
        File(latest, "bin/sdkmanager").setExecutable(true)
        onLog("cmdline-tools 安裝完成，請在 UI 中執行 sdkmanager 安裝 platforms;build-tools")

        // 自動安裝必要組件
        try {
            val sdkmanager = File(latest, "bin/sdkmanager")
            val pb = ProcessBuilder(
                sdkmanager.absolutePath,
                "--install",
                "platforms;android-34",
                "build-tools;34.0.0",
                "platform-tools"
            )
            pb.environment()["ANDROID_HOME"] = sdkDir.absolutePath
            pb.environment()["JAVA_HOME"] = runtime.getJdkDir().absolutePath
            pb.directory(sdkDir)
            val proc = pb.start()
            proc.waitFor()
            onLog("Android platforms 安裝完成")
        } catch (e: Exception) {
            onLog("自動安裝 platforms 失敗，請手動: ${e.message}")
        }
    }

    suspend fun installGradle(onLog: (String)->Unit) = withContext(Dispatchers.IO) {
        val runtime = NativeBuildRuntime(context)
        val gradleDir = runtime.getGradleDir()
        val zipFile = File(context.cacheDir, "gradle.zip")
        downloadWithMirror(GRADLE_URL, GRADLE_URL, zipFile, onLog)
        onLog("解壓 Gradle...")
        val tmp = File(context.cacheDir, "gradle_tmp").apply { mkdirs(); deleteRecursively(); mkdirs() }
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val out = File(tmp, entry.name)
                if (entry.isDirectory) out.mkdirs() else {
                    out.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { ins -> out.outputStream().use { o -> ins.copyTo(o) } }
                }
            }
        }
        val extracted = tmp.listFiles()?.firstOrNull { it.name.startsWith("gradle-8") }
        extracted?.copyRecursively(gradleDir, overwrite = true)
        File(gradleDir, "bin/gradle").setExecutable(true)
        onLog("Gradle 安裝完成")
    }

    private suspend fun downloadWithMirror(primary: String, mirror: String, dest: File, onLog: (String)->Unit) {
        val urls = listOf(primary, mirror).distinct()
        var lastErr: Exception? = null
        for (url in urls) {
            try {
                onLog("下載: $url")
                val req = Request.Builder().url(url).header("User-Agent", "web-to-app").build()
                val resp = client.newCall(req).execute()
                if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
                val body = resp.body ?: throw Exception("empty body")
                val total = body.contentLength()
                var read = 0L
                body.byteStream().use { ins ->
                    dest.outputStream().use { out ->
                        val buf = ByteArray(8192)
                        var len: Int
                        while (ins.read(buf).also { len = it } != -1) {
                            out.write(buf, 0, len)
                            read += len
                            if (total > 0 && read % (1024*1024) == 0L) {
                                onLog("已下載 ${read/1024/1024} MB / ${total/1024/1024} MB")
                            }
                        }
                    }
                }
                onLog("下載完成: ${dest.length()/1024/1024} MB")
                return
            } catch (e: Exception) {
                lastErr = e
                onLog("下載失敗 $url: ${e.message}，嘗試鏡像...")
            }
        }
        throw lastErr ?: Exception("download failed")
    }

    private fun untarGz(tarGzFile: File, destDir: File) {
        FileInputStream(tarGzFile).use { fis ->
            GzipCompressorInputStream(fis).use { gz ->
                TarArchiveInputStream(gz).use { tar ->
                    var entry = tar.nextTarEntry
                    while (entry != null) {
                        val out = File(destDir, entry.name)
                        if (entry.isDirectory) out.mkdirs() else {
                            out.parentFile?.mkdirs()
                            out.outputStream().use { o -> tar.copyTo(o) }
                        }
                        entry = tar.nextTarEntry
                    }
                }
            }
        }
    }
}
