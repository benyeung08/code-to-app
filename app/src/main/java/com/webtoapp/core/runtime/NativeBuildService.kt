
package com.webtoapp.core.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 前台服務用於長時間 Gradle 編譯
 * 類似現有 URL-polling foreground service
 */
class NativeBuildService : Service() {

    companion object {
        const val ACTION_BUILD = "com.webtoapp.action.NATIVE_BUILD"
        const val EXTRA_PROJECT_ROOT = "project_root"
        const val EXTRA_TASK = "task"
        const val CHANNEL_ID = "native_build_channel"
        const val NOTIF_ID = 1002
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_BUILD) {
            val root = intent.getStringExtra(EXTRA_PROJECT_ROOT) ?: return START_NOT_STICKY
            val task = intent.getStringExtra(EXTRA_TASK) ?: "assembleDebug"
            startForeground(NOTIF_ID, buildNotification("正在編譯 $task..."))
            CoroutineScope(Dispatchers.IO).launch {
                val runtime = NativeBuildRuntime(this@NativeBuildService)
                val result = runtime.buildProject(File(root), task) { log ->
                    updateNotification(log)
                }
                when (result) {
                    is NativeBuildRuntime.BuildResult.Success -> {
                        updateNotification("編譯完成: ${result.apk.name}")
                    }
                    is NativeBuildRuntime.BuildResult.Failure -> {
                        updateNotification("編譯失敗: ${result.reason}")
                    }
                }
                // 發送廣播通知 UI
                sendBroadcast(Intent("com.webtoapp.NATIVE_BUILD_DONE").apply {
                    putExtra("success", result is NativeBuildRuntime.BuildResult.Success)
                })
            }
        }
        return START_NOT_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "原生編譯", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WebToApp 原生編譯")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
