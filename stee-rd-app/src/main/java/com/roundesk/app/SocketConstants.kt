package com.roundesk.app

object SocketConstants {

    object UUIDs {
        const val USER_ROUNDESK_ADMIN: String = "rfhuzkfvodpajiletqss"
        const val USER_DEEPAK_YAHOO: String = "crhuzkfvodpajiletqgb"
        const val USER_DEEPAK_OUTLOOK: String = "agjqticlbhvredpouzkf"
        const val USER_DEEPAK: String = "drpbzfjiouhqkaegcvtl"
        const val USER_HIMANSHU: String = "wqhuzkfvodpajiletqst"
        const val USER_PRIYANKA: String = "gihuzkfvodpajiletlmn"
        const val USER_VASU: String = "cvdbzfjiouhqkaegcvtl"
    }

    const val showIncomingCallUI: Boolean = false
    const val showIncomingCallTopBarUI: Boolean = false
    const val SOCKET_SEND_CALL_TO_CLIENT: String = "sendCallToClient"
    const val CALLER_SOCKET_ID: String = UUIDs.USER_HIMANSHU

    object SocketSuffix {
        const val SOCKET_CONNECT_SEND_CALL_TO_CLIENT: String =
            "$SOCKET_SEND_CALL_TO_CLIENT${CALLER_SOCKET_ID}"

        const val SOCKET_TYPE_ACCEPT_CALL: String = "accepted"
        const val SOCKET_TYPE_NEW_CALL: String = "new"
        const val SOCKET_TYPE_REJECT_CALL: String = "rejected"
    }


}