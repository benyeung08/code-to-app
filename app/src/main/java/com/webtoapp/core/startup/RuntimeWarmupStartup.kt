package com.webtoapp.core.startup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RuntimeWarmupStartup(
    private val appContext: android.content.Context,
) {

    fun initialize(appScope: CoroutineScope) {
        // [性能] 预热 WebView 池。
        // 此前 WebViewPool.prewarm() 在全仓库从未被调用过 —— 只有 shutdown() 里
        // 调了 WebViewPool.release()。于是池恒为空，每次点开一个 App 都要在主线程
        // 冷创建 WebView（首次还要初始化 WebView 引擎），这是「打开网页慢」的主要来源。
        // prewarm() 内部：mainHandler.post 异步、try/catch 兜底、isPrewarmed 幂等，
        // 因此在这里调用既不会拖慢启动，重复调用也安全，失败只记日志。
        runCatching { com.webtoapp.core.webview.WebViewPool.prewarm(appContext) }

        appScope.launch {
            com.webtoapp.core.perf.SystemPerfOptimizer.initSystem(appContext)
            com.webtoapp.core.perf.SystemPerfOptimizer.readaheadCriticalFiles(appContext)
            // NOTE: do NOT preload adblock filters here. The full compiled rule set can be huge
            // (many large filter lists) and loading it on every cold start exhausted the heap and
            // crashed the app (issue #356). Filters are loaded lazily on first actual use
            // (preview/runtime via AdBlocker.loadHostsRules / prepareRuntimeFilters).
        }
        // Warm the GitHub mirror probe so the first update check / module-market
        // load / runtime download reads a hot cache instead of paying the probe
        // round up front. Costs a few KB total (one ranged request per channel).
        appScope.launch {
            runCatching { com.webtoapp.core.network.CnMirrorProbe.probe() }
        }
    }

    fun shutdown() {
        com.webtoapp.core.webview.WebViewPool.release()
        com.webtoapp.core.perf.SystemPerfOptimizer.release()
    }
}
