package cc.loveloli.screenlistener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class ScreenReceiver : BroadcastReceiver() {
    var url: String = ""
    var username: String = ""
    var password: String = ""

    companion object {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {
                Log.d("ScreenReceiver", "屏幕点亮")
                postEvent("screen-on")
            }

            Intent.ACTION_SCREEN_OFF -> {
                Log.d("ScreenReceiver", "屏幕熄灭")
                postEvent("screen-off")
            }
        }
    }

    private fun postEvent(event: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val credentials = Credentials.basic(username, password)

                val formBody = FormBody.Builder()
                    .add("event", event)
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