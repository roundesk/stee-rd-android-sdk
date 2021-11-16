package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class AcceptCallRequest(
    @SerializedName("receiver_id")
    var receiver_id: String,
    @SerializedName("audio")
    var audio: String,
    @SerializedName("video")
    var video: String,
    @SerializedName("apiToken")
    var apiToken: String,
    @SerializedName("meeting_id")
    var meeting_id: Int,
    @SerializedName("roomId")
    var roomId: Int
)