package com.roundesk.sdk.socket

interface SocketListener<T> {

    fun handleSocketSuccessResponse(response: String, type: String)

    fun handleSocketErrorResponse(error: T)
}