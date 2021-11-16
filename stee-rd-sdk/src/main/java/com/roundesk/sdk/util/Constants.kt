package com.roundesk.sdk.util

object Constants {
    const val BASE_URL: String = "http://test.roundesk.io/stee-server/public/api/"
    const val SOCKET_URL: String = "http://socket.roundesk.io:3000"

    object ApiSuffix {
        const val API_KEY_CREATE_CALL: String = "call"
        const val API_KEY_ACCEPT_CALL: String = "accept-call"
        const val API_KEY_DECLINE_CALL: String = "decline-call"
    }

    object SocketSuffix {
        const val SOCKET_SEND_CALL_TO_CLIENT: String = "sendCallToClientdrpbzfjiouhqkaegcvtl"
        const val SOCKET_ACCEPT_CALL: String = "acceptCall"
    }

}