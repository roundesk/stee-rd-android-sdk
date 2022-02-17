package com.roundesk.sdk.dataclass

data class CreateCallDataClassResponse(
    val roomId: Int,
    val caller_name: String,
    val meetingId: Int,
    val streamId: String,
    val rtmp_url: String
)
