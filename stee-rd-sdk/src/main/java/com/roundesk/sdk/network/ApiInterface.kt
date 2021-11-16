package com.roundesk.sdk.network

import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.dataclass.AcceptCallDataClassResponse
import com.roundesk.sdk.dataclass.AcceptCallRequest
import com.roundesk.sdk.dataclass.CreateCallDataClassResponse
import com.roundesk.sdk.dataclass.CreateCallRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterface {

    @POST(Constants.ApiSuffix.API_KEY_CREATE_CALL)
    fun getCreateCallSocketData(@Body body: CreateCallRequest?): Call<CreateCallDataClassResponse?>

    @POST(Constants.ApiSuffix.API_KEY_ACCEPT_CALL)
    fun getAcceptCallSocketData(@Body body: AcceptCallRequest?): Call<AcceptCallDataClassResponse?>
}