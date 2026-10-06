package com.webtoapp.core.codetoapp

import java.io.File
import java.util.Locale

/**
 * 源码工作区：把「上传代码」这一步从「只能传 ZIP」扩展成多种入口，
 * 并统一负责目录内的增删改查，供编辑器与侦测器共用。
 *
 * 之所以独立成 object，是因为三条路径（ZIP 导入、单档导入、新建空白档）
 * 都要落到同一套「根目录 + 相对路径」的模型上，散落各处很容易出现
 * 路径拼接不一致，进而让侦测器找不到特征文件。
 */
object CodeToAppWorkspace {

    /** 每个项目在 app 私有存储里的根目录。 */
    fun projectDir(filesDir: File, projectId: String): File =
        File(filesDir, "codetoapp/$projectId")

    // ---------------------------------------------------------------- 写入

    /**
     * 写入（或覆盖）一个文件。
     * [relPath] 是相对项目根目录的路径，允许含子目录。
     * 会自动建父目录，并拒绝路径穿越。
     */
    fun writeFile(root: File, relPath: String, content: String): File {
        val safe = sanitizeRelativePath(relPath)
        val out = File(root, safe)
        out.parentFile?.mkdirs()
        out.writeText(content)
        return out
    }

    fun createDirectory(root: File, relPath: String): File {
        val out = File(root, sanitizeRelativePath(relPath))
        out.mkdirs()
        return out
    }

    fun deleteRecursively(root: File, relPath: String): Boolean {
        val target = File(root, sanitizeRelativePath(relPath))
        if (!target.exists()) return false
        return target.deleteRecursively()
    }

    fun rename(root: File, relPath: String, newName: String): File? {
        val src = File(root, sanitizeRelativePath(relPath))
        if (!src.exists()) return null
        val cleanName = newName.trim().trim('/')
        if (cleanName.isBlank() || cleanName == "." || cleanName == "..") return null
        val dst = File(src.parentFile, cleanName)
        if (dst.exists()) return null
        return if (src.renameTo(dst)) dst else null
    }

    // ---------------------------------------------------------------- 读取

    /** 读取文本档内容；不是文本档或读取失败时回传 null。 */
    fun readFile(root: File, relPath: String): String? =
        runCatching {
            File(root, sanitizeRelativePath(relPath))
                .takeIf { it.isFile }
                ?.readText()
        }.getOrNull()

    /**
     * 列出单层目录内容。目录排前面，其次按名称排序。
     * 回传的是相对路径，方便直接喂给 UI 与侦测器。
     */
    fun listChildren(root: File, relDir: String = ""): List<Entry> {
        val dir = if (relDir.isBlank()) root else File(root, sanitizeRelativePath(relDir))
        if (!dir.isDirectory) return emptyList()
        val children = dir.listFiles() ?: return emptyList()
        return children
            .map { f ->
                val rel = f.relativeTo(root).path
                Entry(
                    name = f.name,
                    relPath = rel,
                    isDirectory = f.isDirectory,
                    size = if (f.isFile) f.length() else 0L,
                    language = if (f.isFile) languageOf(f.name) else null
                )
            }
            .sortedWith(compareByDescending<Entry> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    /** 递归列出全部文件（不含目录），用于「总文件数 / 总体积」统计。 */
    fun allFiles(root: File): List<File> =
        root.walkTopDown().filter { it.isFile }.toList()

    data class Entry(
        val name: String,
        val relPath: String,
        val isDirectory: Boolean,
        val size: Long,
        val language: String?
    )

    // ---------------------------------------------------------------- 工具

    /**
     * 把用户输入的路径规范成安全的相对路径：
     * 去掉前导 `/`、`./`，过滤 `..` 与空段，避免写出到项目目录之外。
     */
    fun sanitizeRelativePath(raw: String): String {
        val cleaned = raw
            .replace('\\', '/')
            .split('/')
            .filter { it.isNotBlank() && it != "." && it != ".." }
            .joinToString("/")
        return cleaned.ifBlank { "" }
    }

    /**
     * 按副档名推测语言，用于编辑器语法高亮与图标着色。
     * 认不出的回传 null（调用方可退回纯文本模式）。
     */
    fun languageOf(fileName: String): String? {
        val lower = fileName.substringAfterLast('/').lowercase(Locale.US)
        return when {
            lower.endsWith(".js") || lower.endsWith(".mjs") || lower.endsWith(".cjs") -> "javascript"
            lower.endsWith(".ts") || lower.endsWith(".tsx") -> "typescript"
            lower.endsWith(".json") -> "json"
            lower.endsWith(".html") || lower.endsWith(".htm") -> "html"
            lower.endsWith(".css") -> "css"
            lower.endsWith(".scss") || lower.endsWith(".sass") -> "scss"
            lower.endsWith(".py") -> "python"
            lower.endsWith(".go") -> "go"
            lower.endsWith(".php") -> "php"
            lower.endsWith(".kt") || lower.endsWith(".kts") -> "kotlin"
            lower.endsWith(".java") -> "java"
            lower.endsWith(".md") -> "markdown"
            lower.endsWith(".yml") || lower.endsWith(".yaml") -> "yaml"
            lower.endsWith(".xml") -> "xml"
            lower.endsWith(".sh") -> "shell"
            lower.endsWith(".sql") -> "sql"
            lower.endsWith(".toml") -> "toml"
            lower.endsWith(".ini") || lower.endsWith(".env") -> "ini"
            else -> null
        }
    }

    /** 判断是否可以安全地当文本档编辑（二进制档直接拒绝，避免损坏）。 */
    fun isProbablyText(file: File): Boolean {
        if (!file.isFile) return false
        if (file.length() > 2L * 1024 * 1024) return false
        val head = runCatching { file.inputStream().use { it.readBytes() } }.getOrNull()
            ?: return false
        if (head.isEmpty()) return true
        // 出现 NUL 字节基本可判定为二进制
        return head.take(minOf(4096, head.size)).none { it == 0.toByte() }
    }

    /** 目录规模摘要：文件数 + 总字节数。 */
    fun summarize(root: File): Pair<Int, Long> {
        var count = 0
        var size = 0L
        allFiles(root).forEach { count++; size += it.length() }
        return count to size
    }
}
