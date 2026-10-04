// 根项目 build.gradle.kts —— benyeung08/web-to-app
// 关键：必须保留 `com.google.devtools.ksp ... apply false` 这一行，
// 否则 .github/workflows/checks.yml 的 AUTO-FIX 步骤会把本文件整个覆盖掉。
// 版本组合与上游 shiaho777/web-to-app 一致（Gradle 9.4.1 + AGP 9.2.0 + KGP 2.2.10）

plugins {
    id("com.android.application") version "9.2.0" apply false
    id("com.android.library") version "9.2.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
    id("com.google.protobuf") version "0.9.6" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

/**
 * Resolves a Python 3 interpreter command for the current platform. Windows usually exposes
 * Python as `python` (or the `py -3` launcher) rather than `python3`, so a hardcoded
 * `python3` fails there. Each candidate is verified to actually be Python 3 before use.
 * NOTE: intentionally duplicated in app/build.gradle.kts (this project has no buildSrc).
 */
fun resolvePython3Command(): List<String> {
    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val candidates: List<List<String>> = if (isWindows) {
        listOf(listOf("python3"), listOf("python"), listOf("py", "-3"))
    } else {
        listOf(listOf("python3"), listOf("python"))
    }
    for (candidate in candidates) {
        try {
            val probe = ProcessBuilder(candidate + "--version").redirectErrorStream(true).start()
            val output = probe.inputStream.bufferedReader().readText()
            if (probe.waitFor() == 0 && output.contains("Python 3")) {
                return candidate
            }
        } catch (_: Exception) {
            // Candidate unavailable; try the next one.
        }
    }
    return listOf("python3")
}

tasks.register<Exec>("checkUiDesignSystem") {
    group = "verification"
    description = "Checks Compose UI files against the WebToApp design-system debt baseline."
    workingDir = rootDir
    commandLine(
        resolvePython3Command() + listOf(
            ".github/scripts/audit_ui_design_system.py",
            "--enforce-baseline",
            "--allowlist",
            ".github/scripts/ui_design_allowlist.txt",
            "--top",
            "12"
        )
    )
}
