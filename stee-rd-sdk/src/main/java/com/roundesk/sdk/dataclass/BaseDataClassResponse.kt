package com.roundesk.sdk.dataclass

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class BaseDataClassResponse(
    @field:Json(name = "message")
    val message: String? = null,
    @field:Json(name = "success")
    val success: String? = null,
    @field:Json(name = "error")
    val error: String? = null
)
