package com.roundesk.sdk.dataclass

data class MuteVideoRequestData(
    val caller_id : String,
    val camera : String,
    val roomId : Int
)
