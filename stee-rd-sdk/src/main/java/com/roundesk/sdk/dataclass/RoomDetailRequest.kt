package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class RoomDetailRequest(
    @SerializedName("roomId")
    var roomId: String,
    @SerializedName("apiToken")
    var apiToken: String
)