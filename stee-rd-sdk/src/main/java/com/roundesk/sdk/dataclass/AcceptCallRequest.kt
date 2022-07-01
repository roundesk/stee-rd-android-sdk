package com.roundesk.sdk.dataclass


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class AcceptCallRequest(
    @field:Json(name = "receiver_id")
    var receiver_id: String? = null,
    @field:Json(name = "audio")
    var audio: String? = null,
    @field:Json(name = "video")
    var video: String? = null,
    @field:Json(name = "apiToken")
    var apiToken: String? = null,
    @field:Json(name = "meeting_id")
    var meeting_id: Int? = 0,
    @field:Json(name = "roomId")
    var roomId: Int? = 0
)