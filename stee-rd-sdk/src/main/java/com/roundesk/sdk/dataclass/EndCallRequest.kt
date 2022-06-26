package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EndCallRequest(
    @field:Json(name = "meeting_id")
    var meeting_id: String,
    @field:Json(name = "receiver_id")
    var receiver_id: String,
    @field:Json(name = "apiToken")
    var apiToken: String,
    @field:Json(name = "call_time")
    var call_time: String
)