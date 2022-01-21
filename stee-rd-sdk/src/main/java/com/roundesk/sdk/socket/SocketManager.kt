package com.roundesk.sdk.socket

import io.socket.emitter.Emitter

class SocketManager(
    private val socketListener: SocketListener<Any>,
    private val socketConnection: SocketConnection,
    private val socketConnectId: String
) {

    fun createCallSocket() {
        if (socketConnection.mSocket!!.connected()) {

            if (!socketConnection.mSocket!!.hasListeners(socketConnectId)) {
                socketConnection.mSocket?.on(
                    socketConnectId,
                    onCreateCallEmitter
                )//Listener call for getting data
            } else {
                socketConnection.mSocket!!.off(socketConnectId)
                if (socketConnection.mSocket!!.connected()) {
                    socketConnection.mSocket?.on(
                        socketConnectId,
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
            socketConnectId
        )
    }

    /*fun leaveRoomSocket() {
        socketConnection.mSocket!!.emit(Constant.SocketSuffix.SOCKET_CODE_ROOM_LEAVE)
    }*/

    fun offAllEvent() {
        socketConnection.mSocket!!.off(socketConnectId)
    }


}