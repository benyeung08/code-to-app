package com.webtoapp.core.codetoapp

import android.content.Context
import com.webtoapp.core.golang.GoRuntime
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.nodejs.NodeRuntime
import com.webtoapp.core.php.PhpAppRuntime
import com.webtoapp.core.python.PythonRuntime
import com.webtoapp.data.model.CodeToAppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * CodeToApp 运行器：把「侦测到的 runtime」真正跑起来。
 *
 * 之前 CodeToApp 只会把源码目录当静态档案用本端 HTTP 服务打开，
 * Node / Python / Go / PHP 专案因此只能看到源码树，看不到运行结果。
 * 这里按 [CodeToAppConfig.detectedRuntime] 委派给既有的 fork+exec 启动器
 * （这些启动器在别的应用类型上已经跑得很稳），起不来才退回静态预览。
 *
 * 之所以统一收在 object 里：四个 runtime 都要「先检查可用性 → 启动 → 记住实例以便停止」，
 * 散在 UI 层很容易漏掉 stop，导致端口与行程泄漏。
 */
object CodeToAppRunner {

    private const val TAG = "CodeToAppRunner"

    enum class Mode(val key: String) {
        STATIC("STATIC"),
        NODEJS("NODEJS"),
        PYTHON("PYTHON"),
        GO("GO"),
        PHP("PHP")
    }

    data class LaunchResult(
        /** 起好的服务埠；0 表示要用静态预览。 */
        val port: Int,
        val mode: Mode,
        /** 给使用者看的说明，尤其是「为什么退回静态」这件事。 */
        val note: String = ""
    )

    // ---------------------------------------------------------------- 计划

    /** 依侦测结果决定用哪种模式；不涉及任何行程，纯判断，可在 UI 执行绪呼叫。 */
    fun planFor(config: CodeToAppConfig?): Mode {
        val raw = config?.detectedRuntime?.trim().orEmpty()
        return when (raw.uppercase()) {
            "NODEJS", "NODE", "NODE.JS", "NODE_JS" -> Mode.NODEJS
            "PYTHON", "PY" -> Mode.PYTHON
            "GO", "GOLANG" -> Mode.GO
            "PHP" -> Mode.PHP
            else -> Mode.STATIC
        }
    }

    // ---------------------------------------------------------------- 启动

    @Volatile private var nodeRuntime: NodeRuntime? = null
    @Volatile private var pythonRuntime: PythonRuntime? = null
    @Volatile private var goRuntime: GoRuntime? = null
    @Volatile private var phpRuntime: PhpAppRuntime? = null

    /**
     * 依侦测到的 runtime 启动服务。
     * 回传 port > 0 表示真正跑起来了；port == 0 表示要用静态预览（[note] 会说明原因）。
     */
    suspend fun start(
        context: Context,
        projectDir: File,
        config: CodeToAppConfig?
    ): LaunchResult = withContext(Dispatchers.IO) {
        val mode = planFor(config)
        if (mode == Mode.STATIC) {
            return@withContext LaunchResult(0, Mode.STATIC, "静态预览")
        }
        if (!projectDir.exists()) {
            return@withContext LaunchResult(0, Mode.STATIC, "源码目录不存在")
        }

        val port = config?.serverPort?.takeIf { it > 0 } ?: 0
        val envVars = config?.envVars ?: emptyMap()

        runCatching {
            when (mode) {
                Mode.NODEJS -> startNode(context, projectDir, config, port, envVars)
                Mode.PYTHON -> startPython(context, projectDir, config, port, envVars)
                Mode.GO -> startGo(context, projectDir, port, envVars)
                Mode.PHP -> startPhp(context, projectDir, config, port, envVars)
                Mode.STATIC -> 0
            }
        }.onFailure {
            AppLogger.e(TAG, "start ${mode.key} failed", it)
        }.getOrDefault(0).let { started ->
            if (started > 0) {
                AppLogger.i(TAG, "${mode.key} server up on $started for ${projectDir.name}")
                LaunchResult(started, mode)
            } else {
                LaunchResult(0, Mode.STATIC, "${mode.key} 运行时不可用，已退回静态预览")
            }
        }
    }

    private suspend fun startNode(
        context: Context,
        projectDir: File,
        config: CodeToAppConfig?,
        port: Int,
        envVars: Map<String, String>
    ): Int {
        val runtime = NodeRuntime(context)
        if (!runtime.isNodeAvailable()) return 0
        val entry = config?.entryFile?.takeIf { it.isNotBlank() }
            ?: runtime.detectEntryFile(projectDir)
            ?: "index.js"
        val started = runtime.startServer(
            projectDir = projectDir.absolutePath,
            entryFile = entry,
            port = port,
            envVars = envVars
        )
        if (started > 0) nodeRuntime = runtime
        return started
    }

    private suspend fun startPython(
        context: Context,
        projectDir: File,
        config: CodeToAppConfig?,
        port: Int,
        envVars: Map<String, String>
    ): Int {
        val runtime = PythonRuntime(context)
        if (!runtime.isPythonAvailable()) return 0
        val framework = runtime.detectFramework(projectDir)
        val entry = config?.entryFile?.takeIf { it.isNotBlank() }
            ?: runtime.detectEntryFile(projectDir, framework)
        val started = runtime.startServer(
            projectDir = projectDir.absolutePath,
            entryFile = entry,
            framework = framework,
            port = port,
            envVars = envVars,
            installDeps = false
        )
        if (started > 0) pythonRuntime = runtime
        return started
    }

    private suspend fun startGo(
        context: Context,
        projectDir: File,
        port: Int,
        envVars: Map<String, String>
    ): Int {
        val runtime = GoRuntime(context)
        // Go 需要先有可执行档；只有目录没有 binary 时无法 fork+exec，退回静态。
        val binary = runtime.detectBinary(projectDir) ?: return 0
        val started = runtime.startServer(
            projectDir = projectDir.absolutePath,
            binaryName = binary,
            port = port,
            envVars = envVars
        )
        if (started > 0) goRuntime = runtime
        return started
    }

    private suspend fun startPhp(
        context: Context,
        projectDir: File,
        config: CodeToAppConfig?,
        port: Int,
        envVars: Map<String, String>
    ): Int {
        val runtime = PhpAppRuntime(context)
        if (!runtime.isPhpAvailable()) return 0
        val entry = config?.entryFile?.takeIf { it.isNotBlank() } ?: "index.php"
        val started = runtime.startServer(
            projectDir = projectDir.absolutePath,
            entryFile = entry,
            port = port,
            envVars = envVars
        )
        if (started > 0) phpRuntime = runtime
        return started
    }

    // ---------------------------------------------------------------- 停止

    /** 停掉所有可能还在跑的 runtime；安全重复呼叫。 */
    fun stopAll() {
        runCatching { nodeRuntime?.stopServer() }
        runCatching { pythonRuntime?.stopServer() }
        runCatching { goRuntime?.stopServer() }
        runCatching { phpRuntime?.stopServer() }
        nodeRuntime = null
        pythonRuntime = null
        goRuntime = null
        phpRuntime = null
        AppLogger.i(TAG, "all runtimes stopped")
    }
}
