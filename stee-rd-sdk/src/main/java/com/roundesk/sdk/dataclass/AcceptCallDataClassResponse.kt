package com.roundesk.sdk.dataclass

data class AcceptCallDataClassResponse(
    val roomId: Int,
    val meetingId: Int,
    val receiver_name: String,
    val streamId: String,
    val rtmp_url: String,
    val caller_streamId: String,
    val caller_name: String,
    val caller_rtmp_url: String
)
