package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class AcceptCallSocketDataClass(
    @SerializedName("type")
    var type: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("receiver_id")
    var receiverId: String,
    @SerializedName("receiver_name")
    var receiver_name: String,
    @SerializedName("caller_id")
    var callerId: String,
    @SerializedName("meeting_id")
    var meetingId: String,
    @SerializedName("status")
    var status: Boolean
)