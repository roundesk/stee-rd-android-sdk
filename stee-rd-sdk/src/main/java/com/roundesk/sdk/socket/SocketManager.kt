package com.roundesk.sdk.socket

import com.roundesk.sdk.util.Constants
import io.socket.emitter.Emitter

class SocketManager(
    private val socketListener: SocketListener<Any>,
    private val socketConnection: SocketConnection
) {

    fun createCallSocket() {
        if (socketConnection.mSocket!!.connected()) {

            if (!socketConnection.mSocket!!.hasListeners(Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT)) {
                socketConnection.mSocket?.on(
                    Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT,
                    onCreateCallEmitter
                )//Listener call for getting data
            } else {
                socketConnection.mSocket!!.off(Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT)
                if (socketConnection.mSocket!!.connected()) {
                    socketConnection.mSocket?.on(
                        Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT,
                        onCreateCallEmitter
                    )//Listener call for getting data
                }
            }
        }
    }

    private val onCreateCallEmitter = Emitter.Listener { args ->
        val response = "" + args[0]
        socketListener.handleSocketSuccessResponse(
            response,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        )
    }

    /*fun leaveRoomSocket() {
        socketConnection.mSocket!!.emit(Constant.SocketSuffix.SOCKET_CODE_ROOM_LEAVE)
    }*/

    fun offAllEvent() {
        socketConnection.mSocket!!.off(Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT)
    }


}