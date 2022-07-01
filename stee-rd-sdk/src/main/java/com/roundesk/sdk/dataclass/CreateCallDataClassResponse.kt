package com.roundesk.sdk.dataclass

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CreateCallDataClassResponse(
    @field:Json(name = "roomId")
    val roomId: Int? = 0,
    @field:Json(name = "caller_name")
    val caller_name: String? = null,
    @field:Json(name = "meetingId")
    val meetingId: Int? = 0,
    @field:Json(name = "streamId")
    val streamId: String? = null,
    @field:Json(name = "rtmp_url")
    val rtmp_url: String? = null,
    @field:Json(name = "error")
    val error: String? = null

)
