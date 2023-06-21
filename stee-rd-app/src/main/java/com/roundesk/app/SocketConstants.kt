package com.roundesk.app

object SocketConstants {

    const val API_TOKEN: String = "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9"
    const val CALLER_AUDIO_STATUS: String = "on"
    const val CALLER_VIDEO_STATUS: String = "on"
    const val RECEIVER_AUDIO_STATUS: String = "on"
    const val RECEIVER_VIDEO_STATUS: String = "on"

    object UUIDs {
        const val USER_1: String = "rfhuzkfvodpajiletqss" // Name : Roundesk Admin
        const val USER_2: String = "crhuzkfvodpajiletqgb" // Name : deepak yahoo
        const val USER_3: String = "agjqticlbhvredpouzkf" // Name : deepak outlook
        const val USER_4: String = "wqhuzkfvodpajiletqst" // Name : himanshu
        const val USER_5: String = "gihuzkfvodpajiletlmn" // Name : Priyanka
    }

    const val CALLER_SOCKET_ID: String = UUIDs.USER_1
    const val showIncomingCallUI: Boolean = false
    const val showIncomingCallTopBarUI: Boolean = false
    const val SOCKET_SEND_CALL_TO_CLIENT: String = "sendCallToClient"

    object SocketSuffix {
        const val SOCKET_CONNECT_SEND_CALL_TO_CLIENT: String =
            "$SOCKET_SEND_CALL_TO_CLIENT${CALLER_SOCKET_ID}"

        const val SOCKET_TYPE_ACCEPT_CALL: String = "accepted"
        const val SOCKET_TYPE_NEW_CALL: String = "new"
        const val SOCKET_TYPE_REJECT_CALL: String = "rejected"
    }
}