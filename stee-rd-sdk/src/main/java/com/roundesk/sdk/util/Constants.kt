package com.roundesk.sdk.util

//import com.roundesk.sdk.socket.SocketConnection
import com.github.nkzawa.socketio.client.Socket

object Constants {

     var BASE_URL_DEV: String = "https://stee-rd-uat.roundesk.io/api/"
     var SOCKET_URL_DEV: String = "https://stee-rd-uat.roundesk.io:3000/"
     var SERVER_ADDRESS_DEV: String = "stee-rd-uat.roundesk.io:5443"
     var SERVER_URL_DEV = "wss://$SERVER_ADDRESS_DEV/WebRTCAppEE/websocket"
     var STUN_SERVER_URI = arrayListOf(
         "turn:tele-omnii-lb.intranet.spfoneuat.gov.sg:5080",
         "turn:tele-omnii-lb.intranet.spfone.gov.sg:3478",
         "turn:stee-rd-uat.roundesk.io:3478"
         )

    const val API_TOKEN: String = "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9"

    object ApiSuffix {
        const val API_KEY_CREATE_CALL: String = "call"
        const val API_KEY_ACCEPT_CALL: String = "accept-call"
        const val API_KEY_DECLINE_CALL: String = "decline-call"
        const val API_KEY_END_CALL: String = "end-call"
        const val API_KEY_ALL_CALL: String = "all-calls"
        const val API_KEY_ROOM_DETAIL: String = "room-detail"
        const val API_KEY_UPLOAD_DATA_LOG: String = "upload-file-content"
        const val API_KEY_CAMERA_STATUS: String = "camera-status"
        const val API_KEY_AUDIO_STATUS: String = "microphone-status"
        const val API_KEY_ORIENTATION_STATUS : String = "get-dimension"
    }

    object UUIDs {
        const val USER_1: String = "rfhuzkfvodpajiletqss" // Name : Roundesk Admin
        const val USER_2: String = "crhuzkfvodpajiletqgb" // Name : deepak yahoo
        const val USER_3: String = "agjqticlbhvredpouzkf" // Name : deepak outlook
        const val USER_4: String = "gihuzkfvodpajiletlmn" // Name : Priyanka
        const val USER_5: String =  "wqhuzkfvodpajiletqst" // Name : himanshu
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