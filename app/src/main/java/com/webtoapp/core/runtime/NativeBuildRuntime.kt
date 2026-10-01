
package com.webtoapp.core.runtime

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

class NativeBuildRuntime(private val context: Context) {

    companion object {
        init {
            try {
                System.loadLibrary("gradle_launcher")
            } catch (e: Exception) {
                Log.e("NativeBuildRuntime", "loadLibrary failed", e)
            }
        }
        const val TAG = "NativeBuildRuntime"
    }

    external fun nativeLaunchGradle(
        projectRoot: String,
        task: String,
        jdkDir: String,
        sdkDir: String,
        gradleHome: String
    ): Int

    external fun getBuildLogPath(projectRoot: String): String

    private val _logFlow = MutableStateFlow("")
    val logFlow: StateFlow<String> = _logFlow

    fun getJdkDir(): File = File(context.filesDir, "toolchain/jdk17").apply { mkdirs() }
    fun getSdkDir(): File = File(context.filesDir, "toolchain/android-sdk").apply { mkdirs() }
    fun getGradleDir(): File = File(context.filesDir, "toolchain/gradle-8.7").apply { mkdirs() }
    fun getCmdlineToolsDir(): File = File(getSdkDir(), "cmdline-tools/latest")

    suspend fun ensureToolchain(onLog: (String)->Unit) = withContext(Dispatchers.IO) {
        onLog("檢查工具鏈...")
        // 複用現有 LinuxEnvManager 的下載邏輯
        // 這裡只做占位，實際下載邏輯在 GradleInstaller.kt
        val installer = GradleInstaller(context)
        if (!getJdkDir().exists() || File(getJdkDir(), "bin/java").exists().not()) {
            onLog("下載 OpenJDK 17 arm64...")
            installer.installJdk(onLog)
        }
        if (!getCmdlineToolsDir().exists()) {
            onLog("下載 Android cmdline-tools...")
            installer.installCmdlineTools(onLog)
        }
        if (!getGradleDir().exists() || File(getGradleDir(), "bin/gradle").exists().not()) {
            onLog("下載 Gradle 8.7...")
            installer.installGradle(onLog)
        }
        onLog("工具鏈就緒")
    }

    suspend fun buildProject(
        projectRoot: File,
        task: String = "assembleDebug",
        onLog: (String)->Unit
    ): BuildResult = withContext(Dispatchers.IO) {
        try {
            ensureToolchain(onLog)
            
            // 設置 local.properties
            val localProps = File(projectRoot, "local.properties")
            localProps.writeText("sdk.dir=${getSdkDir().absolutePath}\n")

            // 確保 gradlew 可執行
            val gradlew = File(projectRoot, "gradlew")
            if (gradlew.exists()) {
                gradlew.setExecutable(true)
                onLog("gradlew 可執行權限已設置")
            }

            onLog("開始編譯: $task")
            onLog("Project: ${projectRoot.absolutePath}")

            // 調用 native 層 fork+exec
            val exitCode = nativeLaunchGradle(
                projectRoot.absolutePath,
                task,
                getJdkDir().absolutePath,
                getSdkDir().absolutePath,
                getGradleDir().absolutePath
            )

            onLog("編譯結束，退出碼: $exitCode")

            // 查找輸出的 APK
            val apkFiles = listOf(
                File(projectRoot, "app/build/outputs/apk/debug/app-debug.apk"),
                File(projectRoot, "app/build/outputs/apk/release/app-release.apk"),
                File(projectRoot, "build/outputs/apk/debug/app-debug.apk")
            )
            val outputApk = apkFiles.firstOrNull { it.exists() }

            if (exitCode == 0 && outputApk != null) {
                onLog("APK 生成成功: ${outputApk.absolutePath} (${outputApk.length()/1024/1024} MB)")
                BuildResult.Success(outputApk)
            } else {
                // 讀取 build log
                val logFile = File(projectRoot, "app/build/outputs/logs/build.log")
                val log = if (logFile.exists()) logFile.readText().takeLast(2000) else "無日誌"
                BuildResult.Failure("編譯失敗，退出碼 $exitCode\n$log")
            }
        } catch (e: Exception) {
            onLog("異常: ${e.message}")
            BuildResult.Failure(e.message ?: "unknown error")
        }
    }

    sealed class BuildResult {
        data class Success(val apk: File): BuildResult()
        data class Failure(val reason: String): BuildResult()
    }
}
