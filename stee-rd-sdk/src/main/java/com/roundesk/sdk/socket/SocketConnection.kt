package com.roundesk.sdk.socket


import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil
import io.socket.client.IO
import io.socket.client.Socket
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException

class SocketConnection {

    var mSocket: Socket? = null

    fun connectSocket() {
        try {
            mSocket = IO.socket(Constants.SOCKET_URL)
            if (mSocket?.connected() == false) {
                mSocket?.connect()
            }
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