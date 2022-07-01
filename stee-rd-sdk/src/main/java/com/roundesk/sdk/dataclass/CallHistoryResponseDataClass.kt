package com.roundesk.sdk.dataclass

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CallHistoryResponseDataClass(
    @field:Json(name = "type")
    var type: String? = null,
    @field:Json(name = "case_id")
    var case_id: String? = null,
    @field:Json(name = "user")
    var user: List<User> = listOf(),
    @field:Json(name = "date")
    var date: String? = null
) {
    @JsonClass(generateAdapter = true)
    data class User(
        @field:Json(name = "name")
        var name: String? = null,
        @field:Json(name = "uuid")
        var uuid: String? = null,
        @field:Json(name = "receiver_role")
        var receiver_role: String? = null,
        @field:Json(name = "duration")
        var duration: String? = null,
        @field:Json(name = "recording_video_url")
        var recording_video_url: String? = null
    )
}
