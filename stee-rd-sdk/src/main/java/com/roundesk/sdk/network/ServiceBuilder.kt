package com.roundesk.sdk.network

import com.roundesk.sdk.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }

    private val retrofitToUploadDataLogs = Retrofit.Builder()
        .baseUrl("http://test.roundesk.io/stee-server/public/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildServiceToUploadDataLogs(service: Class<T>): T{
        return retrofitToUploadDataLogs.create(service)
    }
}