package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class RoomDetailDataClassResponse(
    @SerializedName("success")
    var success: List<Success>
) {
    data class Success(
        @SerializedName("name")
        var name: String,
        @SerializedName("receiver_id")
        var receiverId: String,
        @SerializedName("role")
        var role: String
    )
}