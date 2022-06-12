package com.roundesk.sdk.network

import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.roundesk.sdk.R
import com.roundesk.sdk.util.Constants
import okhttp3.OkHttpClient
import org.webrtc.ContextUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.*
import javax.security.cert.Certificate
import javax.security.cert.CertificateException


object ServiceBuilder {

    val okHttpClient: OkHttpClient
    val gson: Gson
    val retrofit: Retrofit

/*    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
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

    fun getOkHttpBuilder(): OkHttpClient.Builder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            OkHttpClient().newBuilder()
        } else {
            // Workaround for the error "Caused by: com.android.org.bouncycastle.jce.exception.ExtCertPathValidatorException: Could not validate certificate: Certificate expired at".
            getOkHttpClient()
        }

    private fun getOkHttpClient(): OkHttpClient.Builder =
        try {
            // Create a trust manager that validate certificate chains
            val trustCerts: Array<TrustManager> = arrayOf(
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

                    override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate> = arrayOf()
                }
            )
            // Install only device trusted ssl certificates
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(
                sslSocketFactory,
                trustCerts[0] as X509TrustManager
            )
            builder.hostnameVerifier { _, _ -> true }
            builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    private fun getSSLSocketFactory(): SSLSocketFactory? {
        return try {
            val cf: CertificateFactory
            cf = CertificateFactory.getInstance("X.509")
            val ca: Certificate
            val cert: InputStream = ContextUtils.getApplicationContext().getResources().openRawResource(
                R.raw.sslcert)
//            ca = cf.generateCertificate(cert)
            cert.close()
            val keyStoreType: String = KeyStore.getDefaultType()
            val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)
//            keyStore.setCertificateEntry("ca", ca)
            val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
            val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.getTrustManagers(), null)
            sslContext.socketFactory
        } catch (e: Exception) {
            null
        }
    }
}