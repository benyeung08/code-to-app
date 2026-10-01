
package com.webtoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.webtoapp.core.importer.NativeProjectImporter
import kotlinx.coroutines.launch

@Composable
fun NativeImportScreen(
    onProjectReady: (projectRoot: java.io.File, info: com.webtoapp.data.model.NativeProjectInfo) -> Unit
) {
    var status by remember { mutableStateOf("選擇一個 Android Studio 專案的資料夾或 ZIP") }
    var info by remember { mutableStateOf<com.webtoapp.data.model.NativeProjectInfo?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val zipLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    status = "正在解壓..."
                    val file = java.io.File(context.cacheDir, "import.zip")
                    context.contentResolver.openInputStream(it)?.use { ins -> file.outputStream().use { o -> ins.copyTo(o) } }
                    val root = NativeProjectImporter.unzipIfNeeded(context, file)
                    val parsed = NativeProjectImporter.parseProject(root)
                    info = parsed
                    status = "解析成功: ${parsed.packageName}"
                } catch (e: Exception) { status = "失敗: ${e.message}" }
            }
        }
    }

    val dirLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    // SAF 轉 File 路徑簡化版，實際需用 DocumentFile
                    // 這裡示意，MobileCode 環境可直接用 File 路徑
                    status = "SAF 路徑已選，解析中..."
                } catch (e: Exception) { status = "失敗: ${e.message}" }
            }
        }
    }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("導入 Android Studio 專案", style = MaterialTheme.typography.titleLarge)
        Text(status)
        info?.let {
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("包名: ${it.packageName}")
                    Text("minSdk: ${it.minSdk}")
                    Text("Kotlin: ${it.hasKotlin}")
                    Text("res: ${it.resCount} 文件, assets: ${it.assetsCount}, so: ${it.soFiles.size}")
                }
            }
            Button(onClick = { onProjectReady(it.projectRoot, it) }) { Text("開始打包 APK") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { zipLauncher.launch("application/zip") }) { Text("選擇 ZIP") }
            OutlinedButton(onClick = { dirLauncher.launch(null) }) { Text("選擇資料夾") }
        }
        Text("提示: 支援 app/src/main 結構，Phase 1 會直接合併 res/assets/jniLibs，Phase 2 會真編譯 Gradle", style = MaterialTheme.typography.bodySmall)
    }
}
