package com.roundesk.sdk.dataclass

data class CreateCallDataClassResponse(
    val roomId: Int,
    val meetingId: Int,
    val streamId: String,
    val rtmp_url: String
)
