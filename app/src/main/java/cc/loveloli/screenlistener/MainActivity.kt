package cc.loveloli.screenlistener

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest;

class MainActivity : Activity() {
    private lateinit var startServiceBtn: Button
    private lateinit var stopServiceBtn: Button
    private lateinit var checkServiceBtn: Button
    private lateinit var serviceState: TextView
    private lateinit var urlText: EditText
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
    private lateinit var saveConfigBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startServiceBtn = findViewById(R.id.startServiceBtn)
        stopServiceBtn = findViewById(R.id.stopServiceBtn)
        checkServiceBtn = findViewById(R.id.checkServiceBtn)
        serviceState = findViewById(R.id.serviceState)
        urlText = findViewById(R.id.urlText)
        usernameText = findViewById(R.id.usernameText)
        passwordText = findViewById(R.id.passwordText)
        saveConfigBtn = findViewById(R.id.saveConfigBtn)

        val prefs = getSharedPreferences("user_input", Context.MODE_PRIVATE)
        urlText.setText(prefs.getString("url", ""))
        usernameText.setText(prefs.getString("username", ""))
        passwordText.setText(prefs.getString("password", ""))

        // 请求通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }


        checkServiceBtn.setOnClickListener {
            if (ScreenService.isRunning) {
                serviceState.text = "Service is Running"
            } else {
                serviceState.text = "Service is Stop"
            }
        }

        saveConfigBtn.setOnClickListener {
            val url = urlText.text.toString()
            val username = usernameText.text.toString()
            val password = passwordText.text.toString()

            prefs.edit()
                .putString("url", url)
                .putString("username", username)
                .putString("password", password)
                .apply()
        }

        startServiceBtn.setOnClickListener {
            if (ScreenService.isRunning) {
                serviceState.text = "Service is already running!"
                return@setOnClickListener
            }

            val url = urlText.text.toString()
            val username = usernameText.text.toString()
            val password = passwordText.text.toString()

            val intent = Intent(this, ScreenService::class.java).apply {
                putExtra("url", url)
                putExtra("username", username)
                putExtra("password", password)
            }
            startForegroundService(intent)
        }

        stopServiceBtn.setOnClickListener {
            val intent = Intent(this, ScreenService::class.java)
            stopService(intent)
        }
    }
}