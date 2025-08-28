package com.roundesk.sdk.network

import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.URLConfigurationUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.cert.CertificateException


object ServiceBuilder {

    private val okHttpClient: OkHttpClient
    val gson: Gson
    val retrofit: Retrofit
    private val interceptor = run {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.apply {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }
    }


    init {

        okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS) //Backend is really slow
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
//        okHttpClient.setSslSocketFactory(getSSLSocketFactory())
        gson = GsonBuilder().setLenient().create()

        val moshi = Moshi.Builder() // adapter
            .add(KotlinJsonAdapterFactory())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(URLConfigurationUtil.getBaseURL())
            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }


}