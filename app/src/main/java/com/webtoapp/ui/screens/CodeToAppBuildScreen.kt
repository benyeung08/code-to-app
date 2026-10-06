package com.webtoapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.ApkExportPreflight
import com.webtoapp.core.apkbuilder.BuildResult
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.WebApp
import kotlinx.coroutines.launch
import java.io.File

/**
 * CodeToApp 的 APK 构建页。
 *
 * 刻意不自己实作打包逻辑：整条导出流水线（二进位 AXML/ARSC 修补、权限裁剪、
 * V1/V2/V3 签署、增量缓存）已经存在于 [ApkBuilder]，这里只负责三件事——
 * 载入 WebApp、跑预检、把构建结果呈现出来并提供安装／分享入口。
 *
 * 预检先跑一次再让人按按钮，是为了让「源码目录不见了」「缺 manifest」这类问题
 * 在等待几分钟之前就暴露出来。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeToAppBuildScreen(
    appId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apkBuilder = remember(context) { ApkBuilder(context.applicationContext) }

    var app by remember { mutableStateOf<WebApp?>(null) }
    var loading by remember { mutableStateOf(true) }

    // 预检结果只保存成纯资料，不保存 report 物件本身，
    // 免得依赖预检 item 的确切型别。
    var preflightPassed by remember { mutableStateOf<Boolean?>(null) }
    var preflightErrors by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var preflightWarningCount by remember { mutableIntStateOf(0) }

    var building by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var progressText by remember { mutableStateOf("") }
    var forceFull by remember { mutableStateOf(false) }

    var successPath by remember { mutableStateOf<String?>(null) }
    var successSize by remember { mutableStateOf(0L) }
    var successMode by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var logPath by remember { mutableStateOf<String?>(null) }

    fun runPreflight(webApp: WebApp) {
        preflightPassed = null
        preflightErrors = emptyList()
        preflightWarningCount = 0
        val report = runCatching { ApkExportPreflight.check(context, webApp) }.getOrNull()
            ?: return
        preflightPassed = report.passed
        preflightErrors = report.errors.map { it.key.toString() to it.message }
        preflightWarningCount = report.warnings.size
    }

    LaunchedEffect(appId) {
        loading = true
        val repo = org.koin.java.KoinJavaComponent.get<com.webtoapp.data.repository.WebAppRepository>(
            com.webtoapp.data.repository.WebAppRepository::class.java
        )
        val loaded = runCatching {
            repo.getWebAppById(appId).kotlinx.coroutines.flow.first()
        }.getOrNull()
        app = loaded
        loading = false
        if (loaded != null) runPreflight(loaded)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ---------------------------------------------------------- 顶部
        Surface(tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                }
                Text(
                    Strings.ctaBuildTitle,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                if (app != null && !building) {
                    IconButton(onClick = { runPreflight(app!!) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
                return@Column
            }

            val current = app
            if (current == null) {
                Text(
                    Strings.ctaBuildNoApp,
                    color = MaterialTheme.colorScheme.error
                )
                return@Column
            }

            // ------------------------------------------------------ 应用资讯
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(current.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        Strings.ctaBuildAppInfo.replace(
                            "%s",
                            current.codeToAppConfig?.detectedRuntime?.takeIf { it.isNotBlank() } ?: "static"
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ------------------------------------------------------ 预检
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            Strings.ctaBuildPreflight,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        when (preflightPassed) {
                            true -> Text(
                                Strings.ctaBuildPreflightPassed,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            false -> Text(
                                Strings.ctaBuildPreflightFailed,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            null -> Text("…", fontSize = 12.sp)
                        }
                    }

                    if (preflightErrors.isNotEmpty()) {
                        preflightErrors.forEach { (key, message) ->
                            Text(
                                "• $key: $message",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    if (preflightWarningCount > 0) {
                        Text(
                            Strings.ctaBuildWarnings.replace("%s", preflightWarningCount.toString()),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // ------------------------------------------------------ 选项
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = forceFull, onCheckedChange = { forceFull = it })
                Text(Strings.ctaBuildForceFull, fontSize = 13.sp)
            }

            // ------------------------------------------------------ 构建
            Button(
                onClick = {
                    scope.launch {
                        building = true
                        progress = 0
                        progressText = ""
                        successPath = null
                        errorText = null
                        logPath = null
                        val result = apkBuilder.buildApk(current, forceFull) { percent, text ->
                            progress = percent
                            progressText = text
                        }
                        when (result) {
                            is BuildResult.Success -> {
                                successPath = result.apkFile.absolutePath
                                successSize = result.apkFile.length()
                                successMode = result.buildMode
                                logPath = result.logPath
                            }
                            is BuildResult.Error -> {
                                errorText = result.message
                                logPath = result.logPath
                            }
                        }
                        building = false
                    }
                },
                enabled = !building,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Build, null, modifier = Modifier.size(18.dp))
                Text(
                    if (building) Strings.ctaBuildBuilding else Strings.ctaBuildStart,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (building) {
                val ratio = (progress.coerceIn(0, 100)) / 100f
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "$progress% · $progressText",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ------------------------------------------------------ 结果
            val path = successPath
            if (path != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            Strings.ctaBuildSuccess,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            "${File(path).name} · ${successSize / 1024 / 1024} MB · $successMode",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { installApk(context, File(path)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Download, null, modifier = Modifier.size(16.dp))
                                Text(Strings.ctaBuildInstall, modifier = Modifier.padding(start = 4.dp), fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = { shareApk(context, File(path), current.name) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Share, null, modifier = Modifier.size(16.dp))
                                Text(Strings.ctaBuildShare, modifier = Modifier.padding(start = 4.dp), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            if (errorText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            Strings.ctaBuildFailed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            errorText ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (logPath != null) {
                Text(
                    Strings.ctaBuildLog.replace("%s", logPath ?: ""),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun apkUri(context: android.content.Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

private fun installApk(context: android.content.Context, file: File) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri(context, file), "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun shareApk(context: android.content.Context, file: File, name: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.android.package-archive"
        putExtra(Intent.EXTRA_STREAM, apkUri(context, file))
        putExtra(Intent.EXTRA_SUBJECT, name)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, name)) }
}
