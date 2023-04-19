package com.roundesk.sdk.dataclass


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class RoomDetailRequest(
    @field:Json(name = "roomId") var roomId: String,
    @field:Json(name = "apiToken") var apiToken: String
)