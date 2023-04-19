package com.roundesk.sdk.dataclass


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class DeclineCallRequest(
    @field:Json(name = "receiver_id") var receiver_id: String,
    @field:Json(name = "audio") var audio: String,
    @field:Json(name = "video") var video: String,
    @field:Json(name = "apiToken") var apiToken: String,
    @field:Json(name = "meeting_id") var meeting_id: Int,
    @field:Json(name = "roomId") var roomId: Int
)