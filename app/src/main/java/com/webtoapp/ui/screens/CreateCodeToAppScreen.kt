package com.webtoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.codetoapp.CodeToAppRuntime
import com.webtoapp.core.codetoapp.CodeToAppRuntimeDetector
import com.webtoapp.core.codetoapp.CodeToAppDetection
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.CodeToAppConfig
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import com.webtoapp.ui.screens.create.WtaCreateFlowSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.UUID

/**
 * CodeToApp 创建 / 编辑页。
 *
 * 流程：起名字 → 导入源码 ZIP → 自动侦测运行时 → 微调入口 / 端口 / 命令 → 保存。
 * 侦测逻辑全部在 [CodeToAppRuntimeDetector]，这里只负责把结果呈现出来并允许改写。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCodeToAppScreen(
    existingAppId: Long = 0L,
    onBack: () -> Unit,
    onCreated: (
        name: String,
        codeToAppConfig: CodeToAppConfig,
        iconUri: Uri?,
        themeType: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEdit = existingAppId > 0L

    var appName by remember { mutableStateOf("") }
    var appIcon by remember { mutableStateOf<Uri?>(null) }

    var projectId by remember { mutableStateOf<String?>(null) }
    var sourcePath by remember { mutableStateOf("") }
    var detection by remember { mutableStateOf<CodeToAppDetection?>(null) }

    var entryFile by remember { mutableStateOf("") }
    var serverPort by remember { mutableStateOf("") }
    var buildCommand by remember { mutableStateOf("") }
    var startCommand by remember { mutableStateOf("") }
    var staticDir by remember { mutableStateOf("") }

    var envVars by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var newEnvKey by remember { mutableStateOf("") }
    var newEnvValue by remember { mutableStateOf("") }

    var isImporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fileSummary by remember { mutableStateOf<Pair<Int, Long>?>(null) }

    LaunchedEffect(existingAppId) {
        if (existingAppId > 0L) {
            val repo = org.koin.java.KoinJavaComponent.get<com.webtoapp.data.repository.WebAppRepository>(
                com.webtoapp.data.repository.WebAppRepository::class.java
            )
            val existingApp = repo.getWebAppById(existingAppId).first()
            existingApp?.let { app ->
                appName = app.name
                app.iconPath?.let { appIcon = Uri.parse(it) }
                app.codeToAppConfig?.let { config ->
                    projectId = config.projectId
                    sourcePath = config.sourcePath
                    entryFile = config.entryFile
                    serverPort = if (config.serverPort > 0) config.serverPort.toString() else ""
                    buildCommand = config.buildCommand
                    startCommand = config.startCommand
                    staticDir = config.staticDir
                    envVars = config.envVars.toMutableMap()
                    detection = CodeToAppDetection(
                        runtime = CodeToAppRuntime.fromId(config.detectedRuntime),
                        evidence = emptyList()
                    )
                    if (config.sourcePath.isNotBlank()) {
                        fileSummary = CodeToAppRuntimeDetector.summarize(File(config.sourcePath))
                    }
                }
            }
        }
    }

    val iconPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { appIcon = it } }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isImporting = true
            errorMessage = null
            try {
                val result = withContext(Dispatchers.IO) {
                    val pid = UUID.randomUUID().toString()
                    val destDir = File(context.filesDir, "codetoapp/$pid")
                    val tmpZip = File(context.cacheDir, "codetoapp_import_$pid.zip")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tmpZip.outputStream().use { output -> input.copyTo(output) }
                    }
                    CodeToAppRuntimeDetector.extractZip(tmpZip, destDir)
                    tmpZip.delete()
                    val detected = CodeToAppRuntimeDetector.detect(destDir)
                    Triple(pid, destDir.absolutePath, detected)
                }
                projectId = result.first
                sourcePath = result.second
                detection = result.third
                // 侦测出的建议值填进表单，但不清空用户已经手动改过的东西
                entryFile = result.third.entryFile
                if (serverPort.isBlank() && result.third.serverPort > 0) {
                    serverPort = result.third.serverPort.toString()
                }
                buildCommand = result.third.buildCommand
                startCommand = result.third.startCommand
                staticDir = result.third.staticDir
                fileSummary = CodeToAppRuntimeDetector.summarize(File(result.second))
                if (appName.isBlank()) {
                    appName = result.third.runtime.label
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: e.toString()
            } finally {
                isImporting = false
            }
        }
    }

    val canCreate = projectId != null && sourcePath.isNotBlank()

    WtaCreateFlowScaffold(
        title = Strings.appTypeCodeToApp,
        onBack = onBack,
        actions = {
            TextButton(
                onClick = {
                    val pid = projectId ?: return@TextButton
                    onCreated(
                        appName.ifBlank { Strings.appTypeCodeToApp },
                        CodeToAppConfig(
                            projectId = pid,
                            projectName = appName.ifBlank { Strings.appTypeCodeToApp },
                            sourcePath = sourcePath,
                            detectedRuntime = detection?.runtime?.id ?: CodeToAppRuntime.UNKNOWN.id,
                            entryFile = entryFile,
                            serverPort = serverPort.toIntOrNull() ?: 0,
                            envVars = envVars,
                            buildCommand = buildCommand,
                            startCommand = startCommand,
                            staticDir = staticDir
                        ),
                        appIcon,
                        "AURORA"
                    )
                },
                enabled = canCreate && !isImporting
            ) {
                Text(if (isEdit) Strings.btnSave else Strings.btnCreate)
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- 基本信息 ----------
            WtaCreateFlowSection(title = Strings.labelAppName) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text(Strings.labelAppName) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { iconPickerLauncher.launch("image/*") }) {
                        Text(Strings.labelIcon)
                    }
                }
            }

            // ---------- 导入源码 ----------
            WtaCreateFlowSection(title = Strings.importProject) {
                OutlinedButton(
                    onClick = { zipPickerLauncher.launch("application/zip") },
                    enabled = !isImporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.FolderZip,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        if (isImporting) Strings.importing else Strings.ctaSelectProjectZip,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (sourcePath.isNotBlank()) {
                    Text(
                        sourcePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                fileSummary?.let { (count, size) ->
                    Text(
                        "${count} 个文件 · ${size / 1024} KB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                errorMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // ---------- 侦测结果 ----------
            detection?.let { det ->
                WtaCreateFlowSection(title = Strings.ctaDetectedRuntime) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                det.runtime.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (det.evidence.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    det.evidence.forEach { e ->
                                        AssistChip(onClick = {}, label = { Text(e) })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---------- 运行参数 ----------
            WtaCreateFlowSection(title = Strings.ctaRuntimeSettings) {
                OutlinedTextField(
                    value = entryFile,
                    onValueChange = { entryFile = it },
                    label = { Text(Strings.entryFile) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = serverPort,
                    onValueChange = { serverPort = it.filter { c -> c.isDigit() } },
                    label = { Text(Strings.ctaServerPort) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = buildCommand,
                    onValueChange = { buildCommand = it },
                    label = { Text(Strings.ctaBuildCommand) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = startCommand,
                    onValueChange = { startCommand = it },
                    label = { Text(Strings.ctaStartCommand) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = staticDir,
                    onValueChange = { staticDir = it },
                    label = { Text(Strings.ctaStaticDir) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ---------- 环境变量 ----------
            WtaCreateFlowSection(title = Strings.njsEnvVars) {
                envVars.forEach { (k, v) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "$k=$v",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = { envVars = envVars - k }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newEnvKey,
                        onValueChange = { newEnvKey = it },
                        label = { Text(Strings.njsEnvKey) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEnvValue,
                        onValueChange = { newEnvValue = it },
                        label = { Text(Strings.njsEnvValue) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newEnvKey.isNotBlank()) {
                                envVars = envVars + (newEnvKey to newEnvValue)
                                newEnvKey = ""
                                newEnvValue = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                }
            }

            if (!canCreate) {
                Text(
                    Strings.ctaImportFirstHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 返回时清理未保存的导入目录，避免垃圾堆积。
    DisposableEffect(Unit) {
        onDispose {
            if (!isEdit && projectId != null) {
                scope.launch {
                    // 保存成功后 projectId 会被写入数据库；这里只清理仍留在
                    // 「未保存」状态的情况。删除前再确认一次目录存在。
                    withContext(Dispatchers.IO) {
                        runCatching {
                            File(context.filesDir, "codetoapp/${projectId}").let { dir ->
                                if (dir.exists() && dir.list()?.isEmpty() == true) {
                                    dir.deleteRecursively()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
