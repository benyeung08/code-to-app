package com.webtoapp.core.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger

object WebViewPool {

    private const val TAG = "WebViewPool"
    private const val MAX_POOL_SIZE = 2

    private val mainHandler = Handler(Looper.getMainLooper())
    // [性能] 池元素改为 WtaWebView：消费方 EngineViewFactory 需要的就是 WtaWebView
    // （带 siteId 与硬件键盘修饰键修复）。同包可直接引用，无需 import。
    // WebView 仍是 WtaWebView 的父类，因此原 acquire(): WebView 签名不受影响。
    private val pool = ArrayDeque<WtaWebView>(MAX_POOL_SIZE)

    @Volatile
    private var isPrewarmed = false

    fun prewarm(context: Context) {
        if (isPrewarmed) return
        isPrewarmed = true

        val appContext = context.applicationContext

        mainHandler.post {
            try {
                val startTime = System.currentTimeMillis()
                val webView = createPrewarmedWebView(appContext)
                synchronized(pool) {
                    if (pool.size < MAX_POOL_SIZE) {
                        pool.addLast(webView)
                    } else {
                        webView.destroy()
                    }
                }
                val elapsed = System.currentTimeMillis() - startTime
                AppLogger.i(TAG, "WebView prewarmed in ${elapsed}ms (pool size: ${pool.size})")
            } catch (e: Exception) {
                AppLogger.e(TAG, "WebView prewarm failed", e)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun acquire(context: Context): WebView {
        val webView = synchronized(pool) {
            pool.removeLastOrNull()
        }

        return if (webView != null) {

            (webView.context as? MutableContextWrapper)?.baseContext = context
            AppLogger.d(TAG, "WebView acquired from pool (remaining: ${pool.size})")

            replenishPool(context.applicationContext)

            webView
        } else {
            AppLogger.d(TAG, "Pool empty, creating new WebView")
            WebView(context)
        }
    }

    /**
     * [性能] 取出一个可复用的 WtaWebView。
     *
     * 这是让预热池真正生效的关键：此前 EngineViewFactory 直接 `WtaWebView(context)`
     * 新建，池里的实例永远无人消费，预热白做还白占内存。
     *
     * 池空或类型不符时退回新建，行为与原来一致；整体包在 try/catch 里，
     * 任何异常都退回新建，不会让打开 App 失败。
     */
    fun acquireWta(context: Context): WtaWebView {
        return try {
            val existing = synchronized(pool) { pool.removeLastOrNull() }
            if (existing != null) {
                (existing.context as? MutableContextWrapper)?.baseContext = context
                AppLogger.d(TAG, "WtaWebView acquired from pool (remaining: ${pool.size})")
                replenishPool(context.applicationContext)
                existing
            } else {
                AppLogger.d(TAG, "Pool empty, creating new WtaWebView")
                WtaWebView(context)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "acquireWta failed, falling back to new WtaWebView", e)
            WtaWebView(context)
        }
    }

    /**
     * 归还一个 WebView 到池中。
     *
     * 公开签名保持 [WebView]（父类），外部呼叫点不受影响；但池元素已改为
     * [WtaWebView]（消费方 EngineViewFactory 需要的就是 WtaWebView），
     * 所以这里先做安全转型：不是 WtaWebView 就无从复用，直接销毁。
     */
    fun recycle(webView: WebView) {
        val wta = webView as? WtaWebView
        if (wta == null) {
            AppLogger.d(TAG, "Recycled view is not a WtaWebView, destroying it")
            try { webView.destroy() } catch (_: Exception) {}
            return
        }
        try {

            wta.stopLoading()
            wta.loadUrl("about:blank")
            wta.clearHistory()
            wta.removeAllViews()

            synchronized(pool) {
                if (pool.size < MAX_POOL_SIZE) {
                    pool.addLast(wta)
                    AppLogger.d(TAG, "WebView recycled to pool (pool size: ${pool.size})")
                    return
                }
            }

            wta.destroy()
            AppLogger.d(TAG, "Pool full, WebView destroyed")
        } catch (e: Exception) {
            AppLogger.e(TAG, "WebView recycle failed, destroying", e)
            try { wta.destroy() } catch (_: Exception) {}
        }
    }

    fun release() {
        synchronized(pool) {
            pool.forEach { webView ->
                try {
                    webView.destroy()
                } catch (_: Exception) {}
            }
            pool.clear()
        }
        isPrewarmed = false
        AppLogger.i(TAG, "WebView pool released")
    }

    fun poolSize(): Int = synchronized(pool) { pool.size }

    private fun replenishPool(appContext: Context) {
        mainHandler.post {
            synchronized(pool) {
                if (pool.size >= MAX_POOL_SIZE) return@post
            }
            try {
                val webView = createPrewarmedWebView(appContext)
                synchronized(pool) {
                    if (pool.size < MAX_POOL_SIZE) {
                        pool.addLast(webView)
                        AppLogger.d(TAG, "Pool replenished (pool size: ${pool.size})")
                    } else {
                        webView.destroy()
                    }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Pool replenish failed", e)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createPrewarmedWebView(appContext: Context): WtaWebView {
        val contextWrapper = MutableContextWrapper(appContext)
        // [性能] 预热 WtaWebView 而非基类 WebView，这样池里的实例可以直接交给
        // EngineViewFactory 消费，不必「预热一份、再 new 另一份」。
        val webView = WtaWebView(contextWrapper)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true

            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            @Suppress("DEPRECATION")
            setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
        }

        webView.isScrollbarFadingEnabled = true

        return webView
    }
}
