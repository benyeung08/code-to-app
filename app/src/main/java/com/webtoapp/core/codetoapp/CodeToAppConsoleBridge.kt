package com.webtoapp.core.codetoapp

import android.webkit.JavascriptInterface
import android.webkit.WebView

/**
 * 轻量 JS 执行 + 调试桥接。
 *
 * 这里不引入任何额外的 JS 引擎依赖：执行直接走 WebView 的 `evaluateJavascript`，
 * 输出则靠注入一段脚本拦截 `console.*` / `window.onerror` / `unhandledrejection`，
 * 再通过 `@JavascriptInterface` 回传到 Kotlin。
 *
 * 好处是零依赖、体积小，而且拿到的就是页面真实运行环境的 console，
 * 比另开一个引擎更能反映实际行为；代价是只能跑浏览器 API，
 * 完整 Node / Python / Go / PHP 由 [CodeToAppRunner] 用 fork+exec 负责。
 */
class CodeToAppConsoleBridge(
    private val onEntry: (LogEntry) -> Unit
) {

    data class LogEntry(
        val level: Level,
        val message: String,
        val source: String = "",
        val timestamp: Long = System.currentTimeMillis()
    ) {
        enum class Level { LOG, INFO, WARN, ERROR, DEBUG }
    }

    @JavascriptInterface
    fun post(level: String, message: String, source: String) {
        val parsed = when (level.lowercase()) {
            "error" -> LogEntry.Level.ERROR
            "warn", "warning" -> LogEntry.Level.WARN
            "info" -> LogEntry.Level.INFO
            "debug" -> LogEntry.Level.DEBUG
            else -> LogEntry.Level.LOG
        }
        val cleaned = message.replace("\\u0000", "").take(MAX_MESSAGE)
        onEntry(LogEntry(parsed, cleaned, source))
    }

    companion object {
        private const val MAX_MESSAGE = 4000
        const val BRIDGE_NAME = "CodeToAppConsole"

        /**
         * 注入页面的脚本。刻意写成 IIFE 并加上重复执行保护，
         * 因为 WebView 在重新导向或重新载入后会再跑一次，重复 hook 会让每条日志出现两次。
         */
        const val INJECT_SCRIPT = """
(function () {
  if (window.__ctaConsoleHooked) { return; }
  window.__ctaConsoleHooked = true;

  function send(level, args, source) {
    var text;
    try {
      text = Array.prototype.map.call(args, function (a) {
        if (a instanceof Error) { return a.stack || (a.name + ': ' + a.message); }
        if (typeof a === 'object' && a !== null) {
          try { return JSON.stringify(a); } catch (e) { return String(a); }
        }
        return String(a);
      }).join(' ');
    } catch (e) {
      text = String(e);
    }
    try {
      window.$BRIDGE_NAME.post(level, text, source || '');
    } catch (e) { /* bridge 未就绪时静默 */ }
  }

  ['log', 'info', 'warn', 'error', 'debug'].forEach(function (level) {
    var original = console[level] ? console[level].bind(console) : function () {};
    console[level] = function () {
      send(level, arguments, 'console');
      original.apply(null, arguments);
    };
  });

  window.addEventListener('error', function (e) {
    if (e && e.error) {
      send('error', [e.error], e.filename ? (e.filename + ':' + e.lineno) : 'window');
    } else if (e && e.message) {
      send('error', [e.message], e.filename ? (e.filename + ':' + e.lineno) : 'window');
    }
  }, true);

  window.addEventListener('unhandledrejection', function (e) {
    var r = e && e.reason;
    send('error', [r && r.stack ? r.stack : (r || 'Unhandled promise rejection')], 'promise');
  });
})();
"""

        /** 把桥接挂到 WebView 上；重复挂不会出问题。 */
        fun attach(webView: android.webkit.WebView, bridge: CodeToAppConsoleBridge) {
            webView.addJavascriptInterface(bridge, BRIDGE_NAME)
        }
    }
}

/**
 * 在页面载入完成后注入 hook。
 * 用 `evaluateJavascript` 而不是 `loadUrl("javascript:...")`，
 * 后者会把结果当成一次导航，在部分 WebView 版本上会清掉 history。
 */
fun injectConsoleHook(webView: WebView) {
    val bridgeName = CodeToAppConsoleBridge.BRIDGE_NAME
    val script = CodeToAppConsoleBridge.INJECT_SCRIPT.replace(
        "$" + "BRIDGE_NAME",
        bridgeName
    )
    webView.evaluateJavascript(script, null)
}
