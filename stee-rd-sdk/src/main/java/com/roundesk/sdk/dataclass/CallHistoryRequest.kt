package com.roundesk.sdk.dataclass


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CallHistoryRequest(
    @SerializedName("apiToken")
    var apiToken: String,
    @SerializedName("uuid")
    var uuid: String,
    @SerializedName("type")
    var type: String
)