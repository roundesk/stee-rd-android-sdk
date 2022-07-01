package com.roundesk.sdk.dataclass


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class RoomDetailDataClassResponse(
    @field:Json(name = "success")
    var success: List<Success> = listOf(),
    @field:Json(name = "error")
    var error: String? = null
) {
    @JsonClass(generateAdapter = true)
    data class Success(
        @field:Json(name = "name")
        var name: String? = null,
        @field:Json(name = "receiver_id")
        var receiver_id: String? = null,
        @field:Json(name = "role")
        var role: String? = null
    )
}