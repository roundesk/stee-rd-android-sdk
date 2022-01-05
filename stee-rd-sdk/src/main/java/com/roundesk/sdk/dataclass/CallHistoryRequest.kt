package com.roundesk.sdk.dataclass


import com.google.gson.annotations.SerializedName

data class CallHistoryRequest(
    @SerializedName("apiToken")
    var apiToken: String,
    @SerializedName("uuid")
    var uuid: String,
    @SerializedName("type")
    var type: String
)