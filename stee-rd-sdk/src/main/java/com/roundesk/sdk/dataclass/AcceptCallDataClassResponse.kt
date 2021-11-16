package com.roundesk.sdk.dataclass

data class AcceptCallDataClassResponse(
    val roomId: Int,
    val meetingId: Int,
    val streamId: String,
    val rtmp_url: String,
    val caller_streamId: String,
    val caller_rtmp_url: String
)
