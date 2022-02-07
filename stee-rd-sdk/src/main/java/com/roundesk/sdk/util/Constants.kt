package com.roundesk.sdk.util

import com.roundesk.sdk.socket.SocketConnection

object Constants {
    const val BASE_URL: String = "http://test.roundesk.io/stee-server/public/api/"
    const val SOCKET_URL: String = "http://socket.roundesk.io:3000"


    object ApiSuffix {
        const val API_KEY_CREATE_CALL: String = "call"
        const val API_KEY_ACCEPT_CALL: String = "accept-call"
        const val API_KEY_DECLINE_CALL: String = "decline-call"
        const val API_KEY_END_CALL: String = "end-call"
        const val API_KEY_ALL_CALL: String = "all-calls"
    }

    object UUIDs {
        const val USER_ROUNDESK_ADMIN: String = "rfhuzkfvodpajiletqss"
        const val USER_DEEPAK_YAHOO: String = "crhuzkfvodpajiletqgb"
        const val USER_DEEPAK_OUTLOOK: String = "agjqticlbhvredpouzkf"
        const val USER_DEEPAK: String = "drpbzfjiouhqkaegcvtl"
        const val USER_HIMANSHU: String = "wqhuzkfvodpajiletqst"
        const val USER_PRIYANKA: String = "gihuzkfvodpajiletlmn"
        const val USER_VASU: String = "cvdbzfjiouhqkaegcvtl"
    }

    object InitializeSocket {
        var socketConnection: SocketConnection? = null
    }

    object SocketSuffix {
        var SOCKET_CONNECT_SEND_CALL_TO_CLIENT: String = ""
//            "sendCallToClient${UUIDs.USER_DEEPAK}"
        const val SOCKET_TYPE_ACCEPT_CALL: String = "accepted"
        const val SOCKET_TYPE_NEW_CALL: String = "new"
        const val SOCKET_TYPE_REJECT_CALL: String = "rejected"
    }

}