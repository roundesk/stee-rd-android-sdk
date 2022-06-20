package com.roundesk.sdk.util

import com.roundesk.sdk.socket.SocketConnection

object Constants {

//    const val BASE_URL: String = "http://test.roundesk.io/stee-server/public/api/"
//    const val SOCKET_URL: String = "http://socket.roundesk.io:3000"

//    const val BASE_URL: String = "https://stee-rd-uat.roundesk.io/api/"
//    const val SOCKET_URL: String = "https://stee-rd-uat.roundesk.io:3000"

    const val BASE_URL: String = "https://tele-omnii-lb.intranet.spfoneuat.gov.sg/api/"
    const val SOCKET_URL: String = "https://tele-omnii-lb.intranet.spfoneuat.gov.sg:3000"

    const val API_TOKEN: String = "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9"

    object ApiSuffix {
        const val API_KEY_CREATE_CALL: String = "call"
        const val API_KEY_ACCEPT_CALL: String = "accept-call"
        const val API_KEY_DECLINE_CALL: String = "decline-call"
        const val API_KEY_END_CALL: String = "end-call"
        const val API_KEY_ALL_CALL: String = "all-calls"
        const val API_KEY_ROOM_DETAIL: String = "room-detail"
        const val API_KEY_UPLOAD_DATA_LOG: String = "upload-file-content"
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

    var CALLER_SOCKET_ID: String = ""

    object SocketSuffix {
        var SOCKET_CONNECT_SEND_CALL_TO_CLIENT: String = ""
        const val SOCKET_TYPE_ACCEPT_CALL: String = "accepted"
        const val SOCKET_TYPE_NEW_CALL: String = "new"
        const val SOCKET_TYPE_REJECT_CALL: String = "rejected"
    }

}