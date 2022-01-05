package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class EndCallRequest(
    @SerializedName("meeting_id")
    var meeting_id: String,
    @SerializedName("receiver_id")
    var receiver_id: String,
    @SerializedName("apiToken")
    var apiToken: String,
    @SerializedName("call_time")
    var call_time: String
)