package com.roundesk.sdk.network

import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.dataclass.CallHistoryResponseDataClass
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @POST(Constants.ApiSuffix.API_KEY_CREATE_CALL)
    fun getCreateCallSocketData(@Body body: CreateCallRequest?): Call<CreateCallDataClassResponse?>

    @POST(Constants.ApiSuffix.API_KEY_ACCEPT_CALL)
    fun getAcceptCallSocketData(@Body body: AcceptCallRequest?): Call<AcceptCallDataClassResponse?>

    @POST(Constants.ApiSuffix.API_KEY_END_CALL)
    fun endCall(@Body body: EndCallRequest?): Call<BaseDataClassResponse?>

    @POST(Constants.ApiSuffix.API_KEY_DECLINE_CALL)
    fun declineCall(@Body body: DeclineCallRequest?): Call<BaseDataClassResponse?>

    @POST(Constants.ApiSuffix.API_KEY_ROOM_DETAIL)
    fun getRoomDetail(@Body body: RoomDetailRequest?): Call<RoomDetailDataClassResponse?>

    @GET(Constants.ApiSuffix.API_KEY_ALL_CALL)
    fun getCallHistoryData(
        @Query("apiToken") apiToken: String,
        @Query("uuid") uuid: String,
        @Query("type") type: String
    ): Call<List<CallHistoryResponseDataClass?>>

    @Multipart
    @POST(Constants.ApiSuffix.API_KEY_UPLOAD_DATA_LOG)
    fun uploadDataLogs(
        @Part("content") dataLogFile: MultipartBody.Part,
        @Part("apiToken") apiToken: String
    ): Call<BaseDataClassResponse?>

    @POST(Constants.ApiSuffix.API_KEY_CAMERA_STATUS)
    fun muteVideo(
     @Body data : MuteVideoRequestData
    ) : Call <Any>

    @POST(Constants.ApiSuffix.API_KEY_AUDIO_STATUS)
    fun muteAudio(
      @Body data : MuteAudioRequestData
    ) : Call<Any>
}