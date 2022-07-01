package com.roundesk.sdk.dataclass


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CreateCallSocketDataClass(
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
    var meetingId: Int,
    @SerializedName("room_id")
    var room_id: Int
)