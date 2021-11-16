package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class CreateCallRequest(
    @SerializedName("caller_id")
    var callerId: String,
    @SerializedName("role")
    var role: String,
    @SerializedName("audio")
    var audio: String,
    @SerializedName("video")
    var video: String,
    @SerializedName("participants")
    var participants: ArrayList<Participant>,
    @SerializedName("apiToken")
    var apiToken: String,
    @SerializedName("case_id")
    var case_id: String
) {
    data class Participant(
        @SerializedName("receiver_id")
        var receiverId: String,
        @SerializedName("role")
        var role: String
    )
}