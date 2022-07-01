package com.roundesk.sdk.dataclass


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Keep
@JsonClass(generateAdapter = true)
data class CreateCallRequest(
//    @SerializedName("caller_id")
    @field:Json(name = "caller_id")
    var caller_id: String? = null,
//    @SerializedName("role")
    @field:Json(name = "role")
    var role: String? = null,
//    @SerializedName("audio")
    @field:Json(name = "audio")
    var audio: String? = null,
//    @SerializedName("video")
    @field:Json(name = "video")
    var video: String? = null,
//    @SerializedName("participants")
    @field:Json(name = "participants")
    var participants: List<Participant> = listOf(),
//    @SerializedName("apiToken")
    @field:Json(name = "apiToken")
    var apiToken: String? = null,
//    @SerializedName("case_id")
    @field:Json(name = "case_id")
    var case_id: String? = null
) {
    @JsonClass(generateAdapter = true)
    data class Participant(
//        @SerializedName("receiver_id")
        @field:Json(name = "receiver_id")
        var receiver_id: String? = null,

//        @SerializedName("role")
        @field:Json(name = "role")
        var role: String?= null
    )
}