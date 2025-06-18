package cc.loveloli.screenlistener

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

class ScreenService : Service() {
    companion object {
        var isRunning = false
    }

    private lateinit var screenReceiver: ScreenReceiver
    private var urlM: String = ""
    private var usernameM: String = ""
    private var passwordM: String = ""

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 获取传入的参数
        urlM = intent?.getStringExtra("url") ?: ""
        usernameM = intent?.getStringExtra("username") ?: ""
        passwordM = intent?.getStringExtra("password") ?: ""

        // 注册广播，并将参数传入
        screenReceiver = ScreenReceiver().apply {
            // 使用构造函数或 setter 方法传入参数
            this.url = urlM
            this.username = usernameM
            this.password = passwordM
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

    override fun onBind(intent: Intent?): IBinder? = null
}