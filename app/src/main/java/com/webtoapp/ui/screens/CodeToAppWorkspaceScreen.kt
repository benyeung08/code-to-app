package com.webtoapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.codetoapp.CodeToAppDetection
import com.webtoapp.core.codetoapp.CodeToAppRuntimeDetector
import com.webtoapp.core.codetoapp.CodeToAppWorkspace
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.components.WtaCodeEditorDialog
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import com.webtoapp.ui.screens.create.WtaCreateFlowSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * CodeToApp 源码工作区页：上传代码 + 内置编辑器。
 *
 * 入口有三个，都落到同一个项目根目录：
 * 1. 导入 ZIP（[CodeToAppRuntimeDetector.extractZip]，会自动剥掉顶层目录）
 * 2. 导入单个文件（可指定子目录）
 * 3. 新建空白文件 / 目录
 *
 * 编辑部分直接复用既有的 [WtaCodeEditorDialog]（搜寻、取代、语法配色都在里面），
 * 这里只负责挑档、读档、写档与档案树导航。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeToAppWorkspaceScreen(
    projectId: String,
    title: String = Strings.appTypeCodeToApp,
    onBack: () -> Unit,
    onChanged: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val root = remember(projectId) { CodeToAppWorkspace.projectDir(context.filesDir, projectId) }

    var currentDir by remember { mutableStateOf("") }
    // bump 用来强制刷新档案树：文件内容/结构变动后 +1 即触发重组
    var bump by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<Pair<Int, Long>?>(null) }
    var detected by remember { mutableStateOf<CodeToAppDetection?>(null) }

    val entries = remember(currentDir, bump, projectId) {
        if (root.exists()) CodeToAppWorkspace.listChildren(root, currentDir) else emptyList()
    }

    // 进入页面时先算一次侦测结果（放到 IO 线程，避免大项目卡住主线程）
    androidx.compose.runtime.LaunchedEffect(projectId) {
        val result = withContext(Dispatchers.IO) {
            if (root.exists()) CodeToAppRuntimeDetector.detect(root) else null
        }
        detected = result
    }

    // ---------------- 编辑器状态 ----------------
    var editorRelPath by remember { mutableStateOf<String?>(null) }
    var editorContent by remember { mutableStateOf("") }
    var editorLanguage by remember { mutableStateOf<String?>(null) }

    fun refreshSummary() {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                if (root.exists()) CodeToAppRuntimeDetector.detect(root) else null
            }
            detected = result
        }
        summary = if (root.exists()) CodeToAppWorkspace.summarize(root) else null
        bump++
        onChanged?.invoke()
    }

    fun openFile(rel: String) {
        scope.launch {
            val content = withContext(Dispatchers.IO) { CodeToAppWorkspace.readFile(root, rel) }
            if (content == null) {
                errorMessage = Strings.ctaFileReadFailed
                return@launch
            }
            editorRelPath = rel
            editorContent = content
            editorLanguage = CodeToAppWorkspace.languageOf(rel.substringAfterLast('/'))
        }
    }

    // ---------------- ZIP 导入 ----------------
    val zipLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            errorMessage = null
            runCatching {
                withContext(Dispatchers.IO) {
                    val tmp = File(context.cacheDir, "cta_import_${System.currentTimeMillis()}.zip")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tmp.outputStream().use { output -> input.copyTo(output) }
                    }
                    CodeToAppRuntimeDetector.extractZip(tmp, root)
                    tmp.delete()
                }
                currentDir = ""
                refreshSummary()
            }.onFailure { errorMessage = it.message ?: it.toString() }
            isBusy = false
        }
    }

    // ---------------- 单档导入 ----------------
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            errorMessage = null
            runCatching {
                val name = withContext(Dispatchers.IO) {
                    val display = uri.lastPathSegment
                        ?.substringAfterLast('/')
                        ?.substringAfterLast(':')
                        ?.takeIf { it.isNotBlank() }
                        ?: "imported_${System.currentTimeMillis()}.txt"
                    val rel = if (currentDir.isBlank()) display else "$currentDir/$display"
                    val tmp = File(context.cacheDir, "cta_single_${System.currentTimeMillis()}")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tmp.outputStream().use { output -> input.copyTo(output) }
                    }
                    val content = tmp.readText()
                    tmp.delete()
                    CodeToAppWorkspace.writeFile(root, rel, content)
                    rel
                }
                editorRelPath = name
                editorContent = CodeToAppWorkspace.readFile(root, name).orEmpty()
                editorLanguage = CodeToAppWorkspace.languageOf(name.substringAfterLast('/'))
                refreshSummary()
            }.onFailure { errorMessage = it.message ?: it.toString() }
            isBusy = false
        }
    }

    // ---------------- 新建 / 重命名 / 删除 对话框 ----------------
    var showCreateFile by remember { mutableStateOf(false) }
    var showCreateDir by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var newName by remember { mutableStateOf("") }

    WtaCreateFlowScaffold(
        title = title,
        onBack = onBack,
        actions = {
            IconButton(onClick = ::refreshSummary, enabled = !isBusy) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ---------- 上传代码 ----------
            WtaCreateFlowSection(title = Strings.ctaUploadCode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { zipLauncher.launch("application/zip") },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.FolderZip, null, modifier = Modifier.size(18.dp))
                        Text(Strings.ctaSelectProjectZip, modifier = Modifier.padding(start = 6.dp))
                    }
                    OutlinedButton(
                        onClick = { fileLauncher.launch("*/*") },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.UploadFile, null, modifier = Modifier.size(18.dp))
                        Text(Strings.ctaImportSingleFile, modifier = Modifier.padding(start = 6.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { newName = ""; showCreateFile = true }) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Text(Strings.ctaNewFile, modifier = Modifier.padding(start = 6.dp))
                    }
                    OutlinedButton(onClick = { newName = ""; showCreateDir = true }) {
                        Icon(Icons.Filled.CreateNewFolder, null, modifier = Modifier.size(18.dp))
                        Text(Strings.ctaNewFolder, modifier = Modifier.padding(start = 6.dp))
                    }
                }
                summary?.let { (count, size) ->
                    Text(
                        "$count ${Strings.ctaFilesCount} · ${size / 1024} KB",
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

            // ---------- 档案树 ----------
            WtaCreateFlowSection(title = Strings.ctaSourceFiles) {
                // 返回上一层
                if (currentDir.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentDir = currentDir.substringBeforeLast('/', "") }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("..", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (entries.isEmpty()) {
                    Text(
                        Strings.ctaNoFilesYet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    entries.forEach { e ->
                        FileRow(
                            entry = e,
                            onOpen = {
                                if (e.isDirectory) currentDir = e.relPath else openFile(e.relPath)
                            },
                            onDelete = { pendingDelete = e.relPath }
                        )
                    }
                }
            }

            // ---------- 侦测结果（导入后即时反馈） ----------
            detected?.let { det ->
                WtaCreateFlowSection(title = Strings.ctaDetectedRuntime) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            det.runtime.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        det.evidence.forEach { ev ->
                            AssistChip(onClick = {}, label = { Text(ev) })
                        }
                    }
                }
            }
        }
    }

    // ---------------- 编辑器 ----------------
    editorRelPath?.let { rel ->
        WtaCodeEditorDialog(
            language = editorLanguage ?: "text",
            initialContent = editorContent,
            placeholder = "",
            onDismiss = { editorRelPath = null },
            canSaveEmpty = true,
            onSave = { newContent ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        CodeToAppWorkspace.writeFile(root, rel, newContent)
                    }
                    editorContent = newContent
                    editorRelPath = null
                    refreshSummary()
                }
            }
        )
    }

    // ---------------- 新建档案 ----------------
    if (showCreateFile) {
        NameInputDialog(
            title = Strings.ctaNewFile,
            value = newName,
            hint = Strings.ctaFileNameHint,
            onValueChange = { newName = it },
            onDismiss = { showCreateFile = false },
            onConfirm = {
                if (newName.isNotBlank()) {
                    val rel = if (currentDir.isBlank()) newName else "$currentDir/$newName"
                    CodeToAppWorkspace.writeFile(root, rel, "")
                    showCreateFile = false
                    openFile(CodeToAppWorkspace.sanitizeRelativePath(rel))
                    refreshSummary()
                }
            }
        )
    }

    // ---------------- 新建目录 ----------------
    if (showCreateDir) {
        NameInputDialog(
            title = Strings.ctaNewFolder,
            value = newName,
            hint = Strings.ctaFolderNameHint,
            onValueChange = { newName = it },
            onDismiss = { showCreateDir = false },
            onConfirm = {
                if (newName.isNotBlank()) {
                    CodeToAppWorkspace.createDirectory(
                        root,
                        if (currentDir.isBlank()) newName else "$currentDir/$newName"
                    )
                    showCreateDir = false
                    refreshSummary()
                }
            }
        )
    }

    // ---------------- 删除确认 ----------------
    pendingDelete?.let { rel ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(Strings.ctaConfirmDelete) },
            text = { Text(rel, style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                TextButton(onClick = {
                    CodeToAppWorkspace.deleteRecursively(root, rel)
                    pendingDelete = null
                    refreshSummary()
                }) { Text(Strings.btnDelete) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(Strings.btnCancel) }
            }
        )
    }
}

@Composable
private fun FileRow(
    entry: CodeToAppWorkspace.Entry,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (entry.isDirectory) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!entry.isDirectory) {
                Text(
                    buildString {
                        append("${entry.size / 1024} KB")
                        entry.language?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!entry.isDirectory) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun NameInputDialog(
    title: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(hint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(Strings.btnCreate) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(Strings.btnCancel) } }
    )
}
