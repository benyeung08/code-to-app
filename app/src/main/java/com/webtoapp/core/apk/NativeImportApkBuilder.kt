
package com.webtoapp.core.apk

import android.content.Context
import com.webtoapp.data.model.NativeProjectInfo
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Phase 1: 二進制疊加打包
 * 複用現有的 ApkBuilder 的 binary AXML/ARSC patch 能力
 * Phase 2: 可擴展為真 Gradle 編譯，調用 NativeBuildRuntime
 */
class NativeImportApkBuilder(
    private val context: Context,
    private val apksigPath: String // apksig jar 位置，原項目已有
) {
    suspend fun build(
        baseShellApk: File, // shell/build/outputs/apk/.../webview_shell.apk 或 app 內置 template
        stagingDir: File,   // files/native_import/appId
        info: NativeProjectInfo,
        packageOverride: String,
        outputApk: File,
        onLog: (String)->Unit
    ): File = withContext(Dispatchers.IO) {
        onLog("開始合併原生專案: ${info.packageName}")
        // 1. 解壓 base APK
        val workDir = File(context.cacheDir, "native_build_${System.currentTimeMillis()}").apply { mkdirs() }
        // 使用 Apache Commons Compress 解壓 (專案已有依賴)
        // 偽代碼，實際請調用你現有的 ApkPatcher / ZipUtil
        // ZipUtil.unzip(baseShellApk, workDir)

        // 2. Manifest 合併
        val finalPackage = packageOverride.ifBlank { info.packageName }
        onLog("包名: $finalPackage")
        // 調用 BinaryXmlPatcher: 把 AndroidManifest.xml 的 package 換成 finalPackage
        // BinaryXmlPatcher.patchPackage(File(workDir, "AndroidManifest.xml"), finalPackage)

        // 3. res 疊加
        val resSrc = File(stagingDir, "res")
        if (resSrc.exists()) {
            resSrc.copyRecursively(File(workDir, "res"), overwrite = true)
            onLog("已合併 res: ${resSrc.walk().filter { it.isFile }.count()} 文件")
        }

        // 4. assets 疊加
        val assetsSrc = File(stagingDir, "assets")
        if (assetsSrc.exists()) {
            assetsSrc.copyRecursively(File(workDir, "assets"), overwrite = true)
            onLog("已合併 assets")
        }

        // 5. so 庫
        val jniSrc = File(stagingDir, "jniLibs")
        if (jniSrc.exists()) {
            // lib/arm64-v8a, lib/armeabi-v7a 等
            jniSrc.listFiles()?.forEach { abiDir ->
                val target = File(workDir, "lib/${abiDir.name}").apply { mkdirs() }
                abiDir.copyRecursively(target, overwrite = true)
            }
            onLog("已合併 native .so")
        }

        // 6. java/kotlin 代碼 -> dex
        // Phase 1 簡化: 要求專案已編譯過，或把 java 直接用 d8 編譯
        // val javaDir = File(stagingDir, "java")
        // val kotlinDir = File(stagingDir, "kotlin")
        // val jar = compileToJar(javaDir, kotlinDir) // 使用 ecj + kotlinc
        // val dex = D8Util.jarToDex(jar)
        // 合併 dex: classes.dex, classes2.dex...

        // 7. 重新壓縮 + 簽名
        // ZipUtil.zip(workDir, outputApk)
        // ApkSigner.sign(outputApk, keystore) // 複用現有 KeystoreManager + apksig

        onLog("打包完成: ${outputApk.absolutePath}")
        outputApk
    }
}
