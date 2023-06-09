package com.roundesk.sdk.util

//import com.roundesk.sdk.socket.SocketConnection
import com.github.nkzawa.socketio.client.Socket

object Constants {

    const val UAT = "uat"
    const val DEV = "dev"

    // Change this BUILD_TYPE to UAT for generating UAT build
    const val BUILD_TYPE = DEV

//    const val BASE_URL_DEV: String = "https://stee-prod.roundesk.io/api/"
//    const val SOCKET_URL_DEV: String = "https://stee-prod.roundesk.io:3000"
//    private const val SERVER_ADDRESS_DEV: String = "stee-prod.roundesk.io:5443"
//    const val SERVER_URL_DEV = "wss://$SERVER_ADDRESS_DEV/LiveApp/websocket"

    const val BASE_URL_DEV: String = "http://test.roundesk.io/stee-server/public/api/"
    const val SOCKET_URL_DEV: String = "http://test.roundesk.io:3000"
    private const val SERVER_ADDRESS_DEV: String = "stee-dev.roundesk.io:5443"
    const val SERVER_URL_DEV = "wss://$SERVER_ADDRESS_DEV/testing/websocket"

    const val BASE_URL_UAT: String = "https://tele-omnii-lb.intranet.spfoneuat.gov.sg/api/"
    const val SOCKET_URL_UAT: String = "https://tele-omnii-lb.intranet.spfoneuat.gov.sg:3000"
    private const val SERVER_ADDRESS_UAT: String = "tele-omnii-lb.intranet.spfoneuat.gov.sg:5443"
    const val SERVER_URL_UAT = "wss://$SERVER_ADDRESS_UAT/LiveApp/websocket"

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
//        const val USER_HIMANSHU: String = "himanshu@evvoiot.com"
//        const val USER_DEEPAK: String = "deepak@evvoiot.com"
//        const val USER_PRIYANKA: String = "priyanka@evvoiot.com"

        const val USER_1: String = "rfhuzkfvodpajiletqss" // Roundesk Admin
        const val USER_2: String = "crhuzkfvodpajiletqgb" // deepak yahoo
        const val USER_3: String = "agjqticlbhvredpouzkf" // deepak outlook
        const val USER_4: String = "wqhuzkfvodpajiletqst" // himanshu
        const val USER_5: String = "gihuzkfvodpajiletlmn" // Priyanka
    }

    object InitializeSocket {
        var socketConnection: Socket? = null
    }

    var CALLER_SOCKET_ID: String = ""

    object SocketSuffix {
        var SOCKET_CONNECT_SEND_CALL_TO_CLIENT: String = ""
        const val SOCKET_TYPE_ACCEPT_CALL: String = "accepted"
        const val SOCKET_TYPE_NEW_CALL: String = "new"
        const val SOCKET_TYPE_REJECT_CALL: String = "rejected"
    }

}