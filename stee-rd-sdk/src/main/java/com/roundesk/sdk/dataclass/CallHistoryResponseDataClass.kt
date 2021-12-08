package com.roundesk_stee_sdk.dataclass


import com.google.gson.annotations.SerializedName

data class CallHistoryResponseDataClass(

    @SerializedName("type")
    var type: String,
    @SerializedName("case_id")
    var case_id: String,
    @SerializedName("user")
    var user: List<User>,
    @SerializedName("date")
    var date: String
) {
    data class User(
        @SerializedName("name")
        var name: String,
        @SerializedName("uuid")
        var uuid: String,
        @SerializedName("receiver_role")
        var receiver_role: String,
        @SerializedName("duration")
        var duration: String,
        @SerializedName("recording_video_url")
        var recording_video_url: String
    )
}
