package com.roundesk.sdk.base

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.roundesk.sdk.R
import com.roundesk.sdk.util.Screenshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScreenshotService : Service() {
 val scope = CoroutineScope(SupervisorJob())
    companion object{
        private val _screenshotResultMessage = MutableStateFlow<String>("")
        val screenshotResultMessage : StateFlow<String> = _screenshotResultMessage.asStateFlow()
        fun setInitialValueOfScreenshotResult(){
            _screenshotResultMessage.value =""
        }
    }
    override fun onBind(p0: Intent?): IBinder? {
       return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            startForegroundWithNotification()
            delay(500)
            val screenshot = Screenshot(this@ScreenshotService)
            val resultCode = intent?.getIntExtra("result_code",0) ?: 0
            val resultData = intent?.getParcelableExtra<Intent>("result_data")
            screenshot.captureScreenShot(resultCode, resultData, this@ScreenshotService) { result ->
                if (result.contains("success",ignoreCase = true)){
                    stopSelf()
                }
                _screenshotResultMessage.value = when(result){
                    "success"->"Screenshot saved successfully"
                    else->"Screenshot capture failed"
                }

            }
        }
        return START_STICKY

    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media_projection_channel",
                "Screen Capture",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for screen capture in progress"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    private fun startForegroundWithNotification(){
        val notification = NotificationCompat.Builder(this, "media_projection_channel")
            .setContentTitle("Screenshot capture")
            .setContentText("Media Capture")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        startForeground(1, notification)
    }

}