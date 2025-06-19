package cc.loveloli.screenlistener

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class AppMonitorService : AccessibilityService() {

    private var lastAppName = "";

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            val appName = try {
                val pm = applicationContext.packageManager
                val pkgInfo = pm.getPackageInfo(packageName, 0)
                val appInfo = pkgInfo.applicationInfo
                if (appInfo == null) {
                    packageName
                } else {
                    pm.getApplicationLabel(appInfo).toString()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                packageName // 如果找不到，就退回包名
            }

            if (appName != lastAppName) {
                val prefs = applicationContext.getSharedPreferences("am_user_input", MODE_PRIVATE)
                val url = prefs.getString("url", "").toString()
                val username = prefs.getString("username", "").toString()
                val password = prefs.getString("password", "").toString()
                Log.d("AppMonitorService", "前台应用: $url : $appName")
                lastAppName = appName;
                // 你可以在这里发送 HTTP 请求，保存日志，或通过广播通知其他组件
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val client = OkHttpClient()
                        val credentials = Credentials.basic(username, password)

                        val formBody = FormBody.Builder()
                            .add("event", appName)
                            .build()

                        val request = Request.Builder()
                            .url(url) // 🔁 换成你自己的地址
                            .header("Authorization", credentials)
                            .post(formBody)
                            .build()

                        val response = client.newCall(request).execute()
                        Log.d(
                            "ScreenReceiver",
                            "HTTP POST 响应: ${response.code} ${response.body?.string()}"
                        )
                    } catch (e: Exception) {
                        Log.e("ScreenReceiver", "发送请求失败: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        // 可忽略
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppMonitorService", "无障碍服务已连接")

        val info = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = null // null 表示监听所有应用
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }
}