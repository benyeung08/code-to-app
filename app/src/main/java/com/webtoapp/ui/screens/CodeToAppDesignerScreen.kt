package com.webtoapp.ui.screens

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.webtoapp.core.codetoapp.CodeToAppUiDesigner
import com.webtoapp.core.codetoapp.CodeToAppUiHtml
import com.webtoapp.core.codetoapp.CodeToAppWorkspace
import com.webtoapp.core.i18n.Strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 可视化 UI 设计器。
 *
 * 上方是即时预览（把节点树渲染成 HTML 交给 WebView），下方三页签：
 * 元件库 / 结构树 / 属性。改任何东西都会重算 HTML 并重新载入，
 * 所以「拖」的效果其实是「加进去 → 调位置 → 立刻看到」。
 *
 * 结构树提供上移／下移／变成子层／删除，比实作真正的手势拖曳可靠得多，
 * 也不会因为不同 Compose 版本的拖曳 API 差异而坏掉。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeToAppDesignerScreen(
    projectId: String,
    title: String = Strings.ctaUiDesigner,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val projectDir = remember(projectId) { CodeToAppWorkspace.projectDir(context.filesDir, projectId) }

    var root by remember { mutableStateOf<CodeToAppUiDesigner.UiNode?>(null) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var revision by remember { mutableIntStateOf(0) }
    var toast by remember { mutableStateOf<String?>(null) }
    var previewUrl by remember { mutableStateOf<String?>(null) }
    var previewToken by remember { mutableIntStateOf(0) }

    // 载入既有版面
    LaunchedEffect(projectId) {
        val loaded = withContext(Dispatchers.IO) {
            CodeToAppUiDesigner.load(projectDir)
        }
        root = loaded
        selectedId = loaded.id
        revision++
    }

    // 结构或属性一变就重算预览
    LaunchedEffect(revision) {
        val current = root ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            CodeToAppUiHtml.writePreview(projectDir, current, title)
        }
        previewUrl = "ui-preview.html?r=$revision"
        previewToken++
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
                    title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = {
                    val current = root ?: return@IconButton
                    val ok = CodeToAppUiDesigner.save(projectDir, current)
                    toast = if (ok) Strings.ctaUiSaved else Strings.ctaUiSaveFailed
                }) {
                    Icon(Icons.Filled.Save, contentDescription = Strings.ctaUiSave)
                }
            }
        }

        // ---------------------------------------------------------- 预览
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val url = previewUrl
            if (url == null || root == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        Strings.ctaStartingRuntime,
                        style = MaterialTheme.typography.bodySmall,
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
                            setBackgroundColor(Color.WHITE)
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }
                        }
                    },
                    update = { view ->
                        val file = File(projectDir, "ui-preview.html")
                        if (file.exists()) {
                            view.loadUrl("file://${file.absolutePath}?r=$previewToken")
                        }
                    }
                )
            }
        }

        if (toast != null) {
            Text(
                toast!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ---------------------------------------------------------- 下方页签
        val tabs = listOf(Strings.ctaUiPalette, Strings.ctaUiTree, Strings.ctaUiProps)
        ScrollableTabRow(
            selectedTabIndex = tab,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 8.dp
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(label, fontSize = 13.sp, maxLines = 1) }
                )
            }
        }

        val current = root
        if (current == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Strings.ctaStartingRuntime)
            }
        } else {
            when (tab) {
                0 -> PalettePane(
                    onAdd = { type ->
                        val target = resolveInsertTarget(current, selectedId)
                        val node = CodeToAppUiDesigner.createNode(type)
                        target.children.add(node)
                        selectedId = node.id
                        revision++
                    }
                )
                1 -> TreePane(
                    root = current,
                    selectedId = selectedId,
                    onSelect = { selectedId = it },
                    onMove = { id, delta ->
                        val parent = current.parentOf(id)
                        val list = parent?.children ?: return@TreePane
                        val idx = list.indexOfFirst { it.id == id }
                        val newIdx = idx + delta
                        if (idx >= 0 && newIdx in list.indices) {
                            val n = list.removeAt(idx)
                            list.add(newIdx, n)
                            revision++
                        }
                    },
                    onIndent = { id ->
                        val parent = current.parentOf(id) ?: return@TreePane
                        val idx = parent.children.indexOfFirst { it.id == id }
                        if (idx > 0) {
                            val prev = parent.children[idx - 1]
                            if (prev.type.canHaveChildren) {
                                val n = parent.children.removeAt(idx)
                                prev.children.add(n)
                                revision++
                            }
                        }
                    },
                    onDelete = { id ->
                        if (id != current.id && current.remove(id)) {
                            if (selectedId == id) selectedId = current.id
                            revision++
                        }
                    }
                )
                2 -> PropsPane(
                    root = current,
                    selectedId = selectedId,
                    onChange = { key, value ->
                        current.find(selectedId ?: return@PropsPane)?.props?.put(key, value)
                        revision++
                    }
                )
            }
        }
    }
}

/** 元件要插到哪：目前选中的容器，否则选中节点的父容器，再不然就是根。 */
private fun resolveInsertTarget(
    root: CodeToAppUiDesigner.UiNode,
    selectedId: String?
): CodeToAppUiDesigner.UiNode {
    val selected = selectedId?.let { root.find(it) }
    if (selected != null && selected.type.canHaveChildren) return selected
    val parent = selectedId?.let { root.parentOf(it) }
    return parent ?: root
}

// ---------------------------------------------------------------- 元件库

@Composable
private fun PalettePane(onAdd: (CodeToAppUiDesigner.UiComponentType) -> Unit) {
    val types = CodeToAppUiDesigner.UiComponentType.values().toList()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types) { type ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAdd(type) },
                shape = RoundedCornerShape(10.dp),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        labelFor(type),
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    if (type.canHaveChildren) {
                        Text(
                            Strings.ctaUiContainer,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.Add,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun labelFor(type: CodeToAppUiDesigner.UiComponentType): String = when (type) {
    CodeToAppUiDesigner.UiComponentType.COLUMN -> Strings.ctaUiColumn
    CodeToAppUiDesigner.UiComponentType.ROW -> Strings.ctaUiRow
    CodeToAppUiDesigner.UiComponentType.CARD -> Strings.ctaUiCard
    CodeToAppUiDesigner.UiComponentType.TEXT -> Strings.ctaUiText
    CodeToAppUiDesigner.UiComponentType.BUTTON -> Strings.ctaUiButton
    CodeToAppUiDesigner.UiComponentType.INPUT -> Strings.ctaUiInput
    CodeToAppUiDesigner.UiComponentType.IMAGE -> Strings.ctaUiImage
    CodeToAppUiDesigner.UiComponentType.CHECKBOX -> Strings.ctaUiCheckbox
    CodeToAppUiDesigner.UiComponentType.SWITCH -> Strings.ctaUiSwitch
    CodeToAppUiDesigner.UiComponentType.DIVIDER -> Strings.ctaUiDivider
    CodeToAppUiDesigner.UiComponentType.SPACER -> Strings.ctaUiSpacer
}

// ---------------------------------------------------------------- 结构树

@Composable
private fun TreePane(
    root: CodeToAppUiDesigner.UiNode,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onMove: (String, Int) -> Unit,
    onIndent: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val flat = remember(root, selectedId) { flatten(root) }

    Column(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        Text(
            Strings.ctaUiTreeHint,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            items(flat, key = { it.first.id }) { (node, depth) ->
                val selected = node.id == selectedId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (depth * 14).dp, top = 3.dp)
                        .clickable { onSelect(node.id) }
                        .then(
                            if (selected) {
                                Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                            } else Modifier
                        ),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = if (selected) 3.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            labelFor(node.type),
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (node.id != root.id) {
                            IconButton(onClick = { onMove(node.id, -1) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.ArrowUpward, null, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = { onMove(node.id, 1) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.ArrowDownward, null, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = { onIndent(node.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.SubdirectoryArrowRight, null, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = { onDelete(node.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.Filled.Delete,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun flatten(
    node: CodeToAppUiDesigner.UiNode,
    depth: Int = 0,
    out: MutableList<Pair<CodeToAppUiDesigner.UiNode, Int>> = mutableListOf()
): List<Pair<CodeToAppUiDesigner.UiNode, Int>> {
    out += node to depth
    node.children.forEach { flatten(it, depth + 1, out) }
    return out
}

// ---------------------------------------------------------------- 属性

@Composable
private fun PropsPane(
    root: CodeToAppUiDesigner.UiNode,
    selectedId: String?,
    onChange: (String, String) -> Unit
) {
    val node = selectedId?.let { root.find(it) }
    if (node == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                Strings.ctaUiNoSelection,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val defs = remember(node.id, node.type) { CodeToAppUiDesigner.propsFor(node.type) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            labelFor(node.type),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        defs.forEach { def ->
            val value = node.props[def.key].orEmpty()
            when (def.kind) {
                CodeToAppUiDesigner.PropKind.BOOLEAN -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(propLabel(def), modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Switch(
                            checked = value == "true",
                            onCheckedChange = { onChange(def.key, it.toString()) }
                        )
                    }
                }
                CodeToAppUiDesigner.PropKind.SELECT -> {
                    Text(propLabel(def), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        def.options.forEach { opt ->
                            FilterChip(
                                selected = value == opt,
                                onClick = { onChange(def.key, opt) },
                                label = { Text(opt, fontSize = 12.sp) }
                            )
                        }
                    }
                }
                CodeToAppUiDesigner.PropKind.COLOR -> {
                    ColorField(
                        label = propLabel(def),
                        value = value,
                        onValueChange = { onChange(def.key, it) }
                    )
                }
                else -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { onChange(def.key, it) },
                        label = { Text(propLabel(def), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun propLabel(def: CodeToAppUiDesigner.PropDef): String = when (def.key) {
    "padding" -> Strings.ctaUiPropPadding
    "gap" -> Strings.ctaUiPropGap
    "radius" -> Strings.ctaUiPropRadius
    "background" -> Strings.ctaUiPropBackground
    "text" -> Strings.ctaUiPropText
    "size" -> Strings.ctaUiPropSize
    "color" -> Strings.ctaUiPropColor
    "weight" -> Strings.ctaUiPropWeight
    "align" -> Strings.ctaUiPropAlign
    "hint" -> Strings.ctaUiPropHint
    "src" -> Strings.ctaUiPropSrc
    "height" -> Strings.ctaUiPropHeight
    "checked" -> Strings.ctaUiPropChecked
    else -> def.key
}

@Composable
private fun ColorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val presets = listOf(
        "#00000000" to "none",
        "#FFFFFFFF" to "white",
        "#FF000000" to "black",
        "#FF6200EE" to "purple",
        "#FF03DAC5" to "teal",
        "#FFB00020" to "red",
        "#FF4CAF50" to "green",
        "#FF2196F3" to "blue",
        "#FFFF9800" to "amber",
        "#FF9E9E9E" to "grey"
    )
    Column {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { (hex, name) ->
                val selected = value.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .then(
                            if (selected) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(6.dp)
                            ) else Modifier.border(1.dp, androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(6.dp))
                        )
                        .background(parseColorSafe(hex), RoundedCornerShape(6.dp))
                        .clickable { onValueChange(hex) }
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            placeholder = { Text("#AARRGGBB", fontSize = 11.sp) }
        )
    }
}

private fun parseColorSafe(hex: String): androidx.compose.ui.graphics.Color = runCatching {
    val h = hex.removePrefix("#")
    when (h.length) {
        8 -> {
            val rgb = h.substring(2).toLong(16).toInt()
            androidx.compose.ui.graphics.Color(rgb or (h.substring(0, 2).toInt(16) shl 24))
        }
        6 -> androidx.compose.ui.graphics.Color(h.toLong(16).toInt() or 0xFF000000.toInt())
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
}.getOrDefault(androidx.compose.ui.graphics.Color.Transparent)
