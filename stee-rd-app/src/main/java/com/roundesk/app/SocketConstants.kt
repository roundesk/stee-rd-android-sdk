package com.roundesk.app

object SocketConstants {

    const val API_TOKEN: String = "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9"
    const val CALLER_AUDIO_STATUS: String = "on"
    const val CALLER_VIDEO_STATUS: String = "on"
    const val RECEIVER_AUDIO_STATUS: String = "on"
    const val RECEIVER_VIDEO_STATUS: String = "on"

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

    const val showIncomingCallUI: Boolean = false
    const val showIncomingCallTopBarUI: Boolean = false
    const val SOCKET_SEND_CALL_TO_CLIENT: String = "sendCallToClient"
    const val CALLER_SOCKET_ID: String = UUIDs.USER_1

    object SocketSuffix {
        const val SOCKET_CONNECT_SEND_CALL_TO_CLIENT: String =
            "$SOCKET_SEND_CALL_TO_CLIENT${CALLER_SOCKET_ID}"

        const val SOCKET_TYPE_ACCEPT_CALL: String = "accepted"
        const val SOCKET_TYPE_NEW_CALL: String = "new"
        const val SOCKET_TYPE_REJECT_CALL: String = "rejected"
    }
}