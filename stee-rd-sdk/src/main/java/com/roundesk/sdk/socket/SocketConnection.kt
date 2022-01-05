package com.roundesk.sdk.socket


import android.app.Application
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil
import io.socket.client.IO
import io.socket.client.Socket
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException
import java.net.URLEncoder

class SocketConnection {

    var mSocket: Socket? = null

    fun connectSocket() {
        try {
            mSocket = IO.socket(Constants.SOCKET_URL)
            LogUtil.e("SocketConnection", "isSocketConnected : " + mSocket!!.connected())
            if (!mSocket!!.connected()) {
                mSocket!!.connect()
            }
            LogUtil.e("SocketConnection", "getSocket : " + getSocket()?.connected())
        } catch (e: URISyntaxException) {
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun getSocket(): Socket? {
        return mSocket
    }

    fun disConnectSocket() {
        try {
            mSocket?.disconnect()
        } catch (e: URISyntaxException) {
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
}