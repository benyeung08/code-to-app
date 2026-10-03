
package com.webtoapp.data.model

import java.io.File

// ⚠️ 原名 AppType —— 与同包下 WebApp.kt 的 AppType 重名，导致全项目 40 处
// "Unresolved reference"（NODEJS_APP / PHP_APP / requiresProcessExec / fromPersistedName …）。
// 本枚举无任何地方引用，改名即可解除冲突。
enum class NativeImportAppType {
    WEB, MULTI_WEB, HTML, OFFLINE, FRONTEND, PHP, WORDPRESS, NODEJS, PYTHON, GO, MEDIA, GALLERY,
    NATIVE,           // 空殼原生
    NATIVE_IMPORT     // 導入 AS 專案
}

data class NativeImportConfig(
    val projectPath: String, // /storage/emulated/0/.../MyApp 或 files/native_import/xxx
    val originalPackage: String = "",
    val packageOverride: String = "", // 留空則用原包名
    val includeModules: List<String> = listOf("app"),
    val mergeManifest: Boolean = true,
    val includeSo: Boolean = true,
    val includeAssets: Boolean = true,
    val gradleTask: String = "assembleDebug", // Phase 2 用
    val lastImportTime: Long = System.currentTimeMillis()
)

data class NativeProjectInfo(
    val projectRoot: File,
    val appModuleDir: File,
    val manifestFile: File,
    val packageName: String,
    val minSdk: Int,
    val targetSdk: Int,
    val hasKotlin: Boolean,
    val hasNativeLibs: Boolean,
    val resCount: Int,
    val assetsCount: Int,
    val soFiles: List<File>
)
