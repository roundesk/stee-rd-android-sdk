package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class RoomDetailDataClassResponse(
    @SerializedName("success")
    var success: List<Success> = listOf(),
    @SerializedName("error")
    var error: String? = null
) {
    data class Success(
        @SerializedName("name")
        var name: String? = null,
        @SerializedName("receiver_id")
        var receiverId: String? = null,
        @SerializedName("role")
        var role: String? = null
    )
}