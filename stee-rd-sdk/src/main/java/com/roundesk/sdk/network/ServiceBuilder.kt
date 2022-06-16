package com.roundesk.sdk.network

import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.roundesk.sdk.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import javax.net.ssl.*
import javax.security.cert.CertificateException


object ServiceBuilder {

    private val okHttpClient: OkHttpClient
    val gson: Gson
    val retrofit: Retrofit

//        private val client = OkHttpClient.Builder().build()
    /*private val client = OkHttpClient.Builder().apply {
        ignoreAllSSLCertificates()
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .client(client)
        .build()*/

    init {
        okHttpClient = getOkHttpBuilder()
        .build()
//        okHttpClient.setSslSocketFactory(getSSLSocketFactory())
        gson = GsonBuilder().setLenient().create()


        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }

    /*   private val retrofitToUploadDataLogs = Retrofit.Builder()
           .baseUrl("http://test.roundesk.io/stee-server/public/api/")
           .addConverterFactory(GsonConverterFactory.create())
           .client(client)
           .build()

       fun<T> buildServiceToUploadDataLogs(service: Class<T>): T{
           return retrofitToUploadDataLogs.create(service)
       }*/

    private fun getOkHttpBuilder(): OkHttpClient.Builder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            OkHttpClient().newBuilder()
        } else {
            // Workaround for the error "Caused by: ExtCertPathValidatorException: Could not validate certificate".
            getOkHttpClient()
        }

    private fun getOkHttpClient(): OkHttpClient.Builder =
        try {
            // Create a trust manager that validate certificate chains
            val trustAllSecuredCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<out java.security.cert.X509Certificate>?,
                        authType: String?
                    ) = Unit

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<out java.security.cert.X509Certificate>?,
                        authType: String?
                    ) = Unit

                    override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate> =
                        arrayOf()
                }
            )

            // Install only device trusted ssl certificates
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllSecuredCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(
                sslSocketFactory,
                trustAllSecuredCerts[0] as X509TrustManager
            )
            builder.hostnameVerifier { _, _ -> true }
            builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    private fun OkHttpClient.Builder.ignoreAllSSLCertificates(): OkHttpClient.Builder {
        val naiveTrustManager = object : X509TrustManager {
            override fun checkClientTrusted(
                p0: Array<out java.security.cert.X509Certificate>?,
                p1: String?
            ) = Unit

            override fun checkServerTrusted(
                p0: Array<out java.security.cert.X509Certificate>?,
                p1: String?
            ) = Unit

            override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? =
                arrayOf()
        }

        val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
            val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
            init(null, trustAllCerts, SecureRandom())
        }.socketFactory

        sslSocketFactory(insecureSocketFactory, naiveTrustManager)
        hostnameVerifier(HostnameVerifier { _, _ -> true })
        return this
    }
}