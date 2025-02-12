package com.roundesk.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.util.LogUtil
import com.roundesk.sdk.util.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("Registered")
open class SocketController : AppCompatActivity() {

    var mSocket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app: SocketConfig = application as SocketConfig
        mSocket = app.getMSocket()
        lifecycleScope.launch {
            connectSocket()
        }
    }

    private suspend fun connectSocket(){
        LogUtil.e("-------------->", "Socket status : " + mSocket?.connected())
        if (mSocket?.connected() == true){
            return
        }
            mSocket?.connect()
            val options = IO.Options()
            options.reconnection = true
            options.forceNew = true
            delay(500)
            if(NetworkUtils.isConnectedFast(this)){
                connectSocket()
            }else{
                Toast.makeText(this, "Socket is not connected network issue", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.e("--------------des>", "Socket status : " + mSocket?.connected())
    }
}