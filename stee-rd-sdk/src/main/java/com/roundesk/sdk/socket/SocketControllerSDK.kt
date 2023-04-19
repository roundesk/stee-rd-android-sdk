package com.roundesk.sdk.socket

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket

@SuppressLint("Registered")
open class SocketControllerSDK : AppCompatActivity() {
    var mSocket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app: SocketConfigSDK = application as SocketConfigSDK
        mSocket = app.getMSocket()
        mSocket?.connect()
        val options = IO.Options()
        options.reconnection = true
        options.forceNew = true
    }
}