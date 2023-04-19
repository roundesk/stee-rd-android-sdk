package com.roundesk.sdk.socket

import android.app.Application
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.util.URLConfigurationUtil
import java.net.URISyntaxException

class SocketConfigSDK : Application() {
    private var mSocket: Socket? = null

    companion object {
        private var mInstance: SocketConfigSDK? = null

        @Synchronized
        fun getInstance(): SocketConfigSDK? {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        try {
            //creating socket instance
            mSocket = IO.socket(URLConfigurationUtil.getSocketURL())
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    //return socket instance
    fun getMSocket(): Socket? {
        return mSocket
    }
}