package com.roundesk.sdk.dataclass

data class SocketOrientationData(
    val caller_id : String,
    val type : String,
    val msg : String,
    val dimension : String,
    val meetingid : String,
    val receiver_id : String,
)
