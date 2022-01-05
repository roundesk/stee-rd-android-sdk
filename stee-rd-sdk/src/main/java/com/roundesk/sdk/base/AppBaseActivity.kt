package com.roundesk.sdk.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.roundesk.sdk.config.AppController
import com.roundesk.sdk.socket.SocketConnection
import com.roundesk.sdk.socket.SocketManager
import com.roundesk.sdk.util.LogUtil

@SuppressLint("Registered")
open class AppBaseActivity : AppCompatActivity() {

    //    val sharedPreferenceManager: SharedPreferenceManager? = AppController.getInstance()?.getSharedPreferenceUtil()
    var socketConnection: SocketConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        socketConnection = AppController.getInstance()?.getSocketInstance()
        LogUtil.e("AppBaseActivity", "socketConnection : ${socketConnection.toString()}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogUtil.e("Hello", "Test Result : $requestCode")
    }
}