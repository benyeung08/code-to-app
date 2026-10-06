package com.webtoapp.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.webtoapp.core.codetoapp.CodeToAppConsoleBridge
import com.webtoapp.core.codetoapp.CodeToAppRunner
import com.webtoapp.core.codetoapp.CodeToAppRuntimeDetector
import com.webtoapp.core.codetoapp.CodeToAppWorkspace
import com.webtoapp.core.codetoapp.injectConsoleHook
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.webview.LocalHttpServer
import com.webtoapp.data.model.CodeToAppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * CodeToApp 运行与调试页。
 *
 * 上半部是预览（真正跑起来的服务，或静态预览），下半部是控制台：
 * 拦截页面的 console.* / window.onerror / unhandledrejection，
 * 并可直接输入 JS 在页面环境里求值 —— 这就是「轻量 JS 解释器 + 调试器」。
 *
 * 完整 runtime（Node / Python / Go / PHP）交给 [CodeToAppRunner] 用 fork+exec 启动；
 * 起不来时才退回 [LocalHttpServer] 的静态预览，并在顶部说明原因。
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CodeToAppRunScreen(
    projectId: String,
    title: String = Strings.appTypeCodeToApp,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val root = remember(projectId) { CodeToAppWorkspace.projectDir(context.filesDir, projectId) }
    val localHttpServer = remember { LocalHttpServer.getInstance(context) }

    var url by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf(CodeToAppRunner.Mode.STATIC) }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var reloadToken by remember { mutableStateOf(0) }

    val logs = remember { mutableStateListOf<CodeToAppConsoleBridge.LogEntry>() }
    var consoleOpen by remember { mutableStateOf(false) }
    var jsInput by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val bridge = remember {
        CodeToAppConsoleBridge { entry ->
            // 回呼来自 WebView 的执行绪，切回主执行绪再动状态
            scope.launch(Dispatchers.Main) { logs.add(entry) }
        }
    }

    // 启动：先试 fork+exec，失败退回静态预览
    LaunchedEffect(projectId) {
        isLoading = true
        val detection = withContext(Dispatchers.IO) {
            if (root.exists()) CodeToAppRuntimeDetector.detect(root) else null
        }
        val config = detection?.let {
            CodeToAppConfig(
                projectId = projectId,
                sourcePath = root.absolutePath,
                detectedRuntime = it.runtime.name,
                entryFile = it.entryFile,
                serverPort = it.serverPort,
                staticDir = it.staticDir
            )
        }
        val result = CodeToAppRunner.start(context, root, config)
        mode = result.mode
        note = result.note

        val target = if (result.port > 0) {
            "http://127.0.0.1:${result.port}"
        } else {
            // 静态预览：把源码目录整个挂成本端 HTTP 服务
            val serveDir = config?.staticDir?.takeIf { it.isNotBlank() }
                ?.let { File(root, it).takeIf { f -> f.isDirectory } }
                ?: root
            val owner = "codetoapp-$projectId"
            runCatching {
                localHttpServer.start(
                    rootDir = serveDir,
                    enableCrossOriginIsolation = false,
                    owner = owner
                )
            }.getOrNull()
        }
        url = target
        isLoading = false
    }

    DisposableEffect(projectId) {
        onDispose {
            CodeToAppRunner.stopAll()
            runCatching { localHttpServer.stop() }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ---------------------------------------------------------- 顶部状态
        Surface(tonalElevation = 2.dp) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                    Text(
                        title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(if (mode == CodeToAppRunner.Mode.STATIC) Strings.ctaModeStatic else mode.key) }
                    )
                    IconButton(onClick = { reloadToken++ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                    IconButton(
                        onClick = { consoleOpen = !consoleOpen },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Terminal, contentDescription = null)
                    }
                }
                if (note.isNotBlank() || isLoading) {
                    Text(
                        if (isLoading) Strings.ctaStartingRuntime else note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ---------------------------------------------------------- 预览
        Box(modifier = Modifier.weight(1f)) {
            val target = url
            if (target == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (isLoading) Strings.ctaStartingRuntime else Strings.ctaSourceMissing,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(Color.TRANSPARENT)
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView, finishedUrl: String) {
                                    // 每次载入完成都要重新注入，重新导向后 hook 会消失
                                    injectConsoleHook(view)
                                }
                            }
                            webChromeClient = WebChromeClient()
                            CodeToAppConsoleBridge.attach(this, bridge)
                            loadUrl(target)
                        }.also { webViewRef = it }
                    },
                    update = { view ->
                        if (reloadToken > 0) {
                            view.reload()
                            // 用过的 token 归零，避免重组时重复 reload
                            reloadToken = 0
                        }
                    }
                )
            }
        }

        // ---------------------------------------------------------- 控制台
        if (consoleOpen) {
            ConsolePanel(
                logs = logs,
                jsInput = jsInput,
                onJsInputChange = { jsInput = it },
                onClear = { logs.clear() },
                onRunJs = { code ->
                    val view = webViewRef ?: return@ConsolePanel
                    val trimmed = code.trim()
                    if (trimmed.isEmpty()) return@ConsolePanel
                    logs.add(
                        CodeToAppConsoleBridge.LogEntry(
                            CodeToAppConsoleBridge.LogEntry.Level.DEBUG,
                            "› $trimmed",
                            "js"
                        )
                    )
                    view.evaluateJavascript(trimmed) { result ->
                        scope.launch(Dispatchers.Main) {
                            logs.add(
                                CodeToAppConsoleBridge.LogEntry(
                                    CodeToAppConsoleBridge.LogEntry.Level.LOG,
                                    "← ${result ?: "undefined"}",
                                    "js"
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .imePadding()
            )
        }
    }
}

@Composable
private fun ConsolePanel(
    logs: List<CodeToAppConsoleBridge.LogEntry>,
    jsInput: String,
    onJsInputChange: (String) -> Unit,
    onClear: () -> Unit,
    onRunJs: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            runCatching { listState.animateScrollToItem(logs.lastIndex) }
        }
    }

    Surface(tonalElevation = 3.dp, modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Terminal,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    Strings.ctaConsole,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Text("${logs.size}", style = MaterialTheme.typography.labelSmall)
                TextButton(onClick = onClear) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                    Text(Strings.ctaClear, modifier = Modifier.padding(start = 4.dp))
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            Strings.ctaConsoleEmpty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(logs) { entry ->
                    val color = when (entry.level) {
                        CodeToAppConsoleBridge.LogEntry.Level.ERROR ->
                            MaterialTheme.colorScheme.error
                        CodeToAppConsoleBridge.LogEntry.Level.WARN ->
                            MaterialTheme.colorScheme.tertiary
                        CodeToAppConsoleBridge.LogEntry.Level.DEBUG ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            entry.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = color,
                            maxLines = 3
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = jsInput,
                    onValueChange = onJsInputChange,
                    placeholder = { Text(Strings.ctaJsHint, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = { onRunJs(jsInput) }) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Text(Strings.ctaRunJs, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
