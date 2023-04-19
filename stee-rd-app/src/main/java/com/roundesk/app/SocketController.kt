package com.roundesk.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.util.LogUtil

@SuppressLint("Registered")
open class SocketController : AppCompatActivity() {

    var mSocket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app: SocketConfig = application as SocketConfig
        mSocket = app.getMSocket()
        mSocket?.connect()
        val options = IO.Options()
        options.reconnection = true
        options.forceNew = true
        LogUtil.e("-------------->", "Socket status : " + mSocket?.connected())
    }
}