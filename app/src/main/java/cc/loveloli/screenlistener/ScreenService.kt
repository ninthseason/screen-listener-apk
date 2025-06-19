package cc.loveloli.screenlistener

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder

class ScreenService : Service() {
    companion object {
        var isRunning = false
    }

    private lateinit var screenReceiver: ScreenReceiver

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("user_input", Context.MODE_PRIVATE)
        // 注册广播，并将参数传入
        screenReceiver = ScreenReceiver().apply {
            // 使用构造函数或 setter 方法传入参数
            this.url = prefs.getString("url", "").toString()
            this.username = prefs.getString("username", "").toString()
            this.password = prefs.getString("password", "").toString()
        }
        registerReceiver(screenReceiver, ScreenReceiver.intentFilter)

        startForegroundServiceWithNotification()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        // 注销广播接收器
        unregisterReceiver(screenReceiver)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    private fun startForegroundServiceWithNotification() {
        val channelId = "screen_service_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "Screen Event Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("屏幕事件监听中")
            .setContentText("正在后台监听屏幕开关")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    fun getTopApp(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val appList = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10,
            time
        )

        if (appList != null && appList.isNotEmpty()) {
            val recentApp = appList.maxByOrNull { it.lastTimeUsed }
            return recentApp?.packageName
        }
        return null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}