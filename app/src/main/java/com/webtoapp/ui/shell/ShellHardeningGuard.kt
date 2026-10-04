package com.webtoapp.ui.shell

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.webtoapp.core.crypto.RuntimeProtection
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.ApkEncryptionConfig
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random
import kotlin.system.exitProcess

object ShellHardeningGuard {

    private const val TAG = "ShellHardeningGuard"

    private val started = AtomicBoolean(false)
    private val responded = AtomicBoolean(false)

    fun start(activity: Activity, enabled: Boolean, threatResponseName: String) {
        if (!enabled) return
        if (started.getAndSet(true)) return

        val response = parseResponse(threatResponseName)
        val appContext = activity.applicationContext
        val mainHandler = Handler(Looper.getMainLooper())

        val protection = RuntimeProtection.getInstance(appContext)
        protection.setThreatCallback { result ->
            AppLogger.w(TAG, "威胁检测: level=${result.threatLevel}, threats=${result.threats}")

            if (result.shouldBlock && response != ApkEncryptionConfig.ThreatResponse.LOG_ONLY) {
                if (responded.getAndSet(true)) return@setThreatCallback
                mainHandler.post { applyResponse(response) }
            }
        }
        protection.startMonitoring()

        AppLogger.i(TAG, "运行时保护已启动: response=$response")
    }

    private fun parseResponse(name: String): ApkEncryptionConfig.ThreatResponse {
        return try {
            ApkEncryptionConfig.ThreatResponse.valueOf(name.trim().uppercase())
        } catch (e: Exception) {
            ApkEncryptionConfig.ThreatResponse.LOG_ONLY
        }
    }

    private fun applyResponse(response: ApkEncryptionConfig.ThreatResponse) {
        when (response) {
            ApkEncryptionConfig.ThreatResponse.LOG_ONLY -> Unit

            ApkEncryptionConfig.ThreatResponse.SILENT_EXIT,
            ApkEncryptionConfig.ThreatResponse.DATA_WIPE,
            ApkEncryptionConfig.ThreatResponse.FAKE_DATA -> {
                AppLogger.w(TAG, "威胁响应: 静默退出")
                exitProcess(0)
            }

            ApkEncryptionConfig.ThreatResponse.CRASH_RANDOM -> {
                val delayMs = Random.nextLong(500L, 4000L)
                AppLogger.w(TAG, "威胁响应: ${delayMs}ms 后崩溃")
                Handler(Looper.getMainLooper()).postDelayed({
                    throw IllegalStateException("Runtime integrity violation")
                }, delayMs)
            }
        }
    }
}
