
package com.webtoapp.core.runtime

import android.content.Context
import java.io.File

/**
 * Phase 2: 真正的 on-device Gradle 編譯
 * 複用 web-to-app 現有的 Linux Environment 機制
 * 類似 Node.js 的 fork+exec
 */
class NativeBuildRuntime(private val context: Context) {

    fun getJdkDir(): File = File(context.filesDir, "jdk17")
    fun getAndroidSdkDir(): File = File(context.filesDir, "android-sdk")
    fun getGradleDir(): File = File(context.filesDir, "gradle-8.5")

    suspend fun ensureToolchain(onLog: (String)->Unit) {
        // 1. 下載 OpenJDK 17 arm64 tar.gz (類似 PHP 二進制下載)
        // 2. 下載 Android cmdline-tools
        // 3. 下載 Gradle wrapper
        // 參考現有 LinuxEnvManager 的下載邏輯
        onLog("檢查 JDK + Android SDK...")
    }

    fun buildProject(projectRoot: File, task: String = "assembleDebug", onLog: (String)->Unit): File {
        // 類似 app/src/main/cpp/node_launcher
        // 通過 ProcessBuilder fork 一個 :native_build 進程
        // 環境變數:
        // JAVA_HOME = jdkDir
        // ANDROID_HOME = androidSdkDir
        // PATH = jdkDir/bin:gradleDir/bin
        // 執行: ./gradlew task
        // 日誌通過 BroadcastReceiver 回傳到 UI

        // 輸出 APK 位置: app/build/outputs/apk/debug/app-debug.apk
        return File(projectRoot, "app/build/outputs/apk/debug/app-debug.apk")
    }
}
