
package com.webtoapp.core.importer

import android.content.Context
import com.webtoapp.data.model.NativeProjectInfo
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NativeProjectImporter {

    fun isValidAndroidStudioProject(root: File): Boolean {
        val manifest = File(root, "app/src/main/AndroidManifest.xml")
        val manifest2 = File(root, "src/main/AndroidManifest.xml")
        val gradle1 = File(root, "app/build.gradle")
        val gradle2 = File(root, "app/build.gradle.kts")
        val settings = File(root, "settings.gradle")
        val settingsKts = File(root, "settings.gradle.kts")
        return (manifest.exists() || manifest2.exists()) && (gradle1.exists() || gradle2.exists() || settings.exists() || settingsKts.exists())
    }

    suspend fun unzipIfNeeded(context: Context, zipFile: File): File = withContext(Dispatchers.IO) {
        val dest = File(context.filesDir, "native_import/tmp_${System.currentTimeMillis()}")
        dest.mkdirs()
        ZipFile(zipFile).use { zip ->
            for (entry in zip.entries()) {
                val out = File(dest, entry.name)
                if (entry.isDirectory) { out.mkdirs(); continue }
                out.parentFile?.mkdirs()
                zip.getInputStream(entry).use { ins -> out.outputStream().use { o -> ins.copyTo(o) } }
            }
        }
        // 有些 zip 包一層資料夾，自動下鑽一層
        val children = dest.listFiles()
        if (children != null && children.size == 1 && children[0].isDirectory && isValidAndroidStudioProject(children[0])) {
            return@withContext children[0]
        }
        dest
    }

    suspend fun parseProject(root: File): NativeProjectInfo = withContext(Dispatchers.IO) {
        val appDir = when {
            File(root, "app").exists() -> File(root, "app")
            else -> root
        }
        val manifest = File(appDir, "src/main/AndroidManifest.xml")
        if (!manifest.exists()) throw Exception("找不到 AndroidManifest.xml: ${manifest.absolutePath}")

        val text = manifest.readText()
        val pkgRegex = Regex("""package\s*=\s*["']([^"']+)["']""")
        val pkg = pkgRegex.find(text)?.groupValues?.get(1) ?: "com.example.imported"

        val minSdk = Regex("""minSdk\s*=?\s*(\d+)""").find(File(appDir, "build.gradle").readText() + File(appDir, "build.gradle.kts").readText())?.groupValues?.get(1)?.toIntOrNull() ?: 23

        val hasKotlin = File(appDir, "src/main/java").walk().any { it.name.endsWith(".kt") } ||
                File(appDir, "src/main/kotlin").exists()

        val resDir = File(appDir, "src/main/res")
        val assetsDir = File(appDir, "src/main/assets")
        val jniDir = File(appDir, "src/main/jniLibs")

        val soFiles = if (jniDir.exists()) jniDir.walk().filter { it.extension == "so" }.toList() else emptyList()

        NativeProjectInfo(
            projectRoot = root,
            appModuleDir = appDir,
            manifestFile = manifest,
            packageName = pkg,
            minSdk = minSdk,
            targetSdk = 33,
            hasKotlin = hasKotlin,
            hasNativeLibs = soFiles.isNotEmpty(),
            resCount = if (resDir.exists()) resDir.walk().filter { it.isFile }.count() else 0,
            assetsCount = if (assetsDir.exists()) assetsDir.walk().filter { it.isFile }.count() else 0,
            soFiles = soFiles
        )
    }

    suspend fun copyToStaging(context: Context, info: NativeProjectInfo, appId: String): File = withContext(Dispatchers.IO) {
        val staging = File(context.filesDir, "native_import/$appId").apply { mkdirs(); deleteRecursively(); mkdirs() }
        val srcMain = File(info.appModuleDir, "src/main")
        srcMain.copyRecursively(staging, overwrite = true)
        // 額外把 app/build.gradle 保存作參考
        File(info.appModuleDir, "build.gradle").takeIf { it.exists() }?.copyTo(File(staging, "build.gradle"), true)
        File(info.appModuleDir, "build.gradle.kts").takeIf { it.exists() }?.copyTo(File(staging, "build.gradle.kts"), true)
        staging
    }
}
