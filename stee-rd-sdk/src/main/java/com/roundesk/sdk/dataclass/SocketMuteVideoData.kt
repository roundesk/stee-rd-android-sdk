package com.roundesk.sdk.dataclass

data class SocketMuteVideoData(
    val type : String,
    val msg : String,
    val caller_id : String,
    val caller_name : String,
    val room_id : String,
    val camera : String,
    val status : Boolean
)


