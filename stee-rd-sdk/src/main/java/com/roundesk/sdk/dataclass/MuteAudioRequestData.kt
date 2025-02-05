package com.roundesk.sdk.dataclass

data class MuteAudioRequestData(
    val caller_id : String,
    val microphone : String,
    val roomId : Int
)
