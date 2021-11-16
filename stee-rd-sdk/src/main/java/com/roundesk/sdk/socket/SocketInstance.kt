package com.roundesk.sdk.socket

import android.app.Application
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.util.Constants
import java.net.URISyntaxException

class SocketInstance : Application() {
    //socket.io connection url
    private var mSocket: Socket? = null

    override fun onCreate() {
        super.onCreate()
        try {
            //creating socket instance
            mSocket = IO.socket(Constants.SOCKET_URL)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    //return socket instance
    fun getMSocket(): Socket? {
        return mSocket
    }
}