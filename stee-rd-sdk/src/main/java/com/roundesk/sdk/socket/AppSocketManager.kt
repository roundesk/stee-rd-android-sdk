package com.roundesk.sdk.socket
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.socket.SocketListener
import com.roundesk.sdk.util.LogUtil


class AppSocketManager(
    private val socketListener: SocketListener<Any>,
    private var mSocket: Socket?,
    private val emitEventName: String
) {

    fun emitSocketEvents() {
        LogUtil.e("AppSocketManager", "------>Socket Emitting & Receiving Data from Server<------")
        mSocket?.on(emitEventName, onEventEmitter)
        mSocket?.on(Socket.EVENT_DISCONNECT) {
            LogUtil.e("AppSocketManager", "------>$emitEventName Socket Disconnected<------")
            mSocket?.on(Socket.EVENT_RECONNECT) {
                mSocket?.connect()
                LogUtil.e("AppSocketManager", "------>Socket Reconnected<------")
            }
        }
    }

    private val onEventEmitter = Emitter.Listener { args ->
        val response = "" + args[0]
        socketListener.handleSocketSuccessResponse(
            response,
            emitEventName
        )
    }

    fun disconnectSocket() {
        mSocket?.disconnect()
        mSocket?.off(emitEventName)
    }
}