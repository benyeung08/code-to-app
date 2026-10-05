package com.webtoapp.core.codetoapp

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * CodeToApp 支持的语言/框架运行时。
 *
 * 与 [com.webtoapp.data.model.AppType] 里那些「专属类型」不同，CODETOAPP 是
 * **通用源码容器**：它本身不绑定某一种语言，而是在导入源码后侦测出运行时，
 * 再把启动工作委派给既有的 launcher（Node/PHP/Python/Go），
 * 侦测不到就退回「静态前端」（本地文件 + WebView 承载）。
 */
enum class CodeToAppRuntime(
    val id: String,
    val label: String,
    val defaultPort: Int
) {
    NODEJS("NODEJS", "Node.js", 3000),
    PYTHON("PYTHON", "Python", 8000),
    GO("GO", "Go", 8080),
    PHP("PHP", "PHP", 8080),
    STATIC("STATIC", "静态 / 前端", 0),
    UNKNOWN("UNKNOWN", "未识别", 0);

    companion object {
        fun fromId(id: String?): CodeToAppRuntime =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: UNKNOWN
    }
}

/**
 * 侦测结果。除 [runtime] 外，其余字段都是「建议值」，用户可以在界面上改。
 */
data class CodeToAppDetection(
    val runtime: CodeToAppRuntime,
    val entryFile: String = "",
    val serverPort: Int = 0,
    val buildCommand: String = "",
    val startCommand: String = "",
    val staticDir: String = "",
    /** 命中的特征文件，用于在界面上解释「为什么判定成这个运行时」。 */
    val evidence: List<String> = emptyList()
)

object CodeToAppRuntimeDetector {

    /**
     * 扫描源码根目录，按特征文件判定运行时。
     *
     * 判定优先级：Go > Python > Node > PHP > 静态。
     * 之所以把 Go / Python 放前面，是因为 `go.mod`、`requirements.txt`
     * 这类特征文件非常明确；而 `package.json` 在前端静态项目里也很常见
     * （Vite/React 项目也有），所以 Node 要往后排，让 `index.html` 有机会命中。
     */
    fun detect(root: File): CodeToAppDetection {
        if (!root.isDirectory) return CodeToAppDetection(CodeToAppRuntime.UNKNOWN)

        val evidence = mutableListOf<String>()
        val names = root.list()?.toSet() ?: emptySet()

        // ---- Go ----
        if (names.any { it.equals("go.mod", ignoreCase = true) }) {
            evidence += "go.mod"
            val mainGo = listOf("main.go", "cmd/main.go", "app.go")
                .firstOrNull { File(root, it).isFile } ?: "main.go"
            return CodeToAppDetection(
                runtime = CodeToAppRuntime.GO,
                entryFile = mainGo,
                serverPort = CodeToAppRuntime.GO.defaultPort,
                buildCommand = "go build -o app .",
                startCommand = "./app",
                staticDir = "",
                evidence = evidence
            )
        }

        // ---- Python ----
        val pyMarkers = listOf("requirements.txt", "pyproject.toml", "Pipfile", "setup.py", "poetry.lock")
        val pyHit = pyMarkers.firstOrNull { m -> names.any { it.equals(m, ignoreCase = true) } }
        val pyEntry = listOf("main.py", "app.py", "manage.py", "server.py", "wsgi.py", "asgi.py")
            .firstOrNull { File(root, it).isFile }
        if (pyHit != null || pyEntry != null) {
            pyHit?.let { evidence += it }
            pyEntry?.let { evidence += it }
            val entry = pyEntry ?: "main.py"
            val isDjango = pyEntry == "manage.py"
            val isAsgi = pyEntry == "asgi.py" || File(root, "asgi.py").isFile
            val start = when {
                isDjango -> "python manage.py runserver 0.0.0.0:\$PORT"
                isAsgi -> "uvicorn ${entry.removeSuffix(".py")}:app --host 0.0.0.0 --port \$PORT"
                else -> "python $entry"
            }
            return CodeToAppDetection(
                runtime = CodeToAppRuntime.PYTHON,
                entryFile = entry,
                serverPort = CodeToAppRuntime.PYTHON.defaultPort,
                buildCommand = if (pyHit != null) "pip install -r ${pyHit}" else "",
                startCommand = start,
                staticDir = "",
                evidence = evidence
            )
        }

        // ---- Node.js ----
        if (names.any { it.equals("package.json", ignoreCase = true) }) {
            evidence += "package.json"
            val pkg = runCatching { File(root, "package.json").readText() }.getOrDefault("")
            val main = Regex("\"main\"\\s*:\\s*\"([^\"]+)\"").find(pkg)?.groupValues?.get(1)
            val startScript = Regex("\"start\"\\s*:\\s*\"([^\"]+)\"").find(pkg)?.groupValues?.get(1)
            val buildScript = Regex("\"build\"\\s*:\\s*\"([^\"]+)\"").find(pkg)?.groupValues?.get(1)
            val hasVite = pkg.contains("\"vite\"") || names.any { it.equals("vite.config.js", true) || it.equals("vite.config.ts", true) }
            val entry = when {
                main != null && File(root, main).isFile -> main
                File(root, "server.js").isFile -> "server.js"
                File(root, "index.js").isFile -> "index.js"
                File(root, "app.js").isFile -> "app.js"
                hasVite -> "index.html"
                else -> "index.js"
            }
            return CodeToAppDetection(
                runtime = CodeToAppRuntime.NODEJS,
                entryFile = entry,
                serverPort = CodeToAppRuntime.NODEJS.defaultPort,
                buildCommand = buildScript?.let { "npm run build" } ?: "npm install --omit=dev",
                startCommand = startScript?.let { "npm start" } ?: "node $entry",
                staticDir = if (hasVite) (if (File(root, "dist").isDirectory) "dist" else "") else "",
                evidence = evidence + listOfNotNull(
                    if (hasVite) "vite.config" else null,
                    startScript?.let { "scripts.start" }
                )
            )
        }

        // ---- PHP ----
        val phpHit = names.firstOrNull { it.equals("composer.json", ignoreCase = true) }
        val phpEntry = listOf("index.php", "public/index.php", "artisan")
            .firstOrNull { File(root, it).exists() }
        if (phpHit != null || phpEntry != null) {
            phpHit?.let { evidence += it }
            phpEntry?.let { evidence += it }
            val docRoot = if (File(root, "public/index.php").isFile) "public" else ""
            return CodeToAppDetection(
                runtime = CodeToAppRuntime.PHP,
                entryFile = phpEntry ?: "index.php",
                serverPort = CodeToAppRuntime.PHP.defaultPort,
                buildCommand = if (phpHit != null) "composer install --no-dev" else "",
                startCommand = "php -S 127.0.0.1:\$PORT -t ${docRoot.ifBlank { "." }}",
                staticDir = docRoot,
                evidence = evidence
            )
        }

        // ---- 静态 / 前端 ----
        val staticHit = listOf("index.html", "index.htm").firstOrNull { names.any { n -> n.equals(it, true) } }
        if (staticHit != null) {
            evidence += staticHit
            return CodeToAppDetection(
                runtime = CodeToAppRuntime.STATIC,
                entryFile = staticHit,
                serverPort = 0,
                buildCommand = "",
                startCommand = "",
                staticDir = ".",
                evidence = evidence
            )
        }

        return CodeToAppDetection(
            runtime = CodeToAppRuntime.UNKNOWN,
            evidence = if (names.isEmpty()) listOf("目录为空") else listOf("未找到已知特征文件")
        )
    }

    /**
     * 把用户选择的 ZIP 解压到 [destDir]。
     *
     * 只做两件防护：
     * 1. 过滤 `../` 路径穿越（Zip Slip）
     * 2. 单个解压层：如果 ZIP 里所有文件都在同一个顶层目录下，自动把这层剥掉，
     *    让 `index.html` / `package.json` 直接落在根目录，否则侦测会全部落空。
     */
    fun extractZip(zipFile: File, destDir: File): File {
        destDir.mkdirs()
        val topLevel = mutableSetOf<String>()

        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val rawName = entry.name
                val first = rawName.substringBefore('/')
                if (first.isNotBlank()) topLevel += first
                if (!entry.isDirectory) {
                    val safe = rawName
                        .split('/')
                        .filter { it.isNotBlank() && it != "." && it != ".." }
                        .joinToString("/")
                    if (safe.isNotBlank()) {
                        val out = File(destDir, safe)
                        out.parentFile?.mkdirs()
                        FileOutputStream(out).use { fos -> zis.copyTo(fos) }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        // 剥掉唯一的顶层目录
        val dirs = topLevel.filter { File(destDir, it).isDirectory }
        if (dirs.size == 1) {
            val wrapper = File(destDir, dirs.first())
            val children = wrapper.listFiles()
            if (children != null && children.none { it.name.equals(dirs.first(), true) }) {
                children.forEach { it.renameTo(File(destDir, it.name)) }
                wrapper.deleteRecursively()
            }
        }
        return destDir
    }

    /** 统计目录里的文件数与总大小，仅用于界面展示。 */
    fun summarize(root: File): Pair<Int, Long> {
        var count = 0
        var size = 0L
        root.walkTopDown().filter { it.isFile }.forEach {
            count++
            size += it.length()
        }
        return count to size
    }
}
