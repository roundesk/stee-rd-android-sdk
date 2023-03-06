package com.roundesk.sdk.activity

import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.roundesk.sdk.dataclass.CreateCallDataClassResponse
import com.roundesk.sdk.dataclass.CreateCallRequest
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketConnection
import com.roundesk.sdk.util.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ApiFunctions(private var mContext: Activity?) {

    private var TAG: String = "ApiFunctions"
    private lateinit var participants: CreateCallRequest.Participant
    private var participantsArrayList: ArrayList<CreateCallRequest.Participant> = arrayListOf()
    private var arraylistReceiverId: ArrayList<String> = arrayListOf()
    private var streamId: String? = null
    private var callerName: String? = null
    private var roomId: Int? = null
    private var meetingId: Int? = null
    private var isIncomingCall: Boolean = false

    fun initiateCall(
        arraylistReceiverId: ArrayList<String>,
        role: String,
        strCallerId: String,
        audioStatus: String,
        videoStatus: String,
        caseId: String
    ) {
        participants = CreateCallRequest.Participant("", "")
        participantsArrayList.clear()
        for (item in arraylistReceiverId.indices) {
            participants = CreateCallRequest.Participant("", "")
            participants =
                CreateCallRequest.Participant(arraylistReceiverId[item], "patient")
            participantsArrayList.add(participants)
        }

        val user = CreateCallRequest(
            strCallerId,
            role,
            audioStatus,
            videoStatus,
            participantsArrayList,
            Constants.API_TOKEN,
            caseId
        )

        val json = Gson().toJson(user)
        LogUtil.e("initiateCall", "json : $json")

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val call = request.getCreateCallSocketData(user)

        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_CREATE_CALL}")
        LogUtil.e(TAG, "Request Body : $json")
        LogUtil.e(TAG, "-----------------------")

        call.enqueue(object : Callback<CreateCallDataClassResponse?> {
            override fun onResponse(
                call: Call<CreateCallDataClassResponse?>,
                response: Response<CreateCallDataClassResponse?>
            ) {
                LogUtil.e(TAG, "Server Header Details : $response")
                LogUtil.e(TAG, "Server Response : ${response.body()}")
                LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()?.meetingId != 0 && response.body()?.roomId != 0)
                            LogUtil.e(TAG, "-----------------------")
                        LogUtil.e(
                            TAG,
                            "Success Response : ${Gson().toJson(response.body())}"
                        )
                        LogUtil.e(TAG, "-----------------------")
                        val createCallDataClassResponse: CreateCallDataClassResponse? =
                            response.body()

                        roomId = createCallDataClassResponse?.roomId
                        meetingId = createCallDataClassResponse?.meetingId
                        streamId = createCallDataClassResponse?.streamId
                        callerName = createCallDataClassResponse?.caller_name

                        if (!isIncomingCall) {
                            if (roomId != 0 && meetingId != 0) {
                                if (mContext?.let { NetworkUtils.isConnectedFast(it) } == true) {
                                    val intent = Intent(mContext, VideoCallActivityNew::class.java)
                                    intent.putExtra("activity", "Outgoing")
                                    intent.putExtra("room_id", roomId)
                                    intent.putExtra("meeting_id", meetingId)
                                    intent.putExtra("stream_id", streamId)
                                    intent.putExtra("caller_name", callerName)
                                    intent.putExtra("isIncomingCall", isIncomingCall)
                                    intent.putExtra("audioStatus", audioStatus)
                                    intent.putExtra("videoStatus", videoStatus)
                                    mContext?.startActivity(intent)
                                } else {
                                    mContext?.let {
                                        ToastUtil.displayLongDurationToast(
                                            it, "Your Connection is not Stable. For video calling your connection should be stable"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }


            override fun onFailure(call: Call<CreateCallDataClassResponse?>, t: Throwable) {
                LogUtil.e(TAG, "-----------------------")
                LogUtil.e("initiateCall", "Failure Response : ${t.message}")
                LogUtil.e(TAG, "-----------------------")
            }
        })


        // This method is for manual parsing the model class
        /* val request = ServiceBuilder.buildService(ApiInterface::class.java)
         val call = request.sampleManualParsing(user)

         LogUtil.e(TAG, "-----------------------")
         LogUtil.e(TAG, "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_CREATE_CALL}")
         LogUtil.e(TAG, "Request Body : $json")
         LogUtil.e(TAG, "-----------------------")

         call.enqueue(object : Callback<Any?> {
             override fun onResponse(
                 call: Call<Any?>,
                 response: Response<Any?>
             ) {
                 LogUtil.e(TAG, "Server Header Details : $response")
                 LogUtil.e(TAG, "Server Response : ${response.body()}")
                 LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))


                 val createCallDataClassResponse: CreateCallDataClassResponse? =
                     Gson().toJson(response.body())

                 LogUtil.e(TAG, "convertToJson : " + convertToJson)
             }

             override fun onFailure(call: Call<Any?>, t: Throwable) {
                 LogUtil.e(TAG, "-----------------------")
                 LogUtil.e("initiateCall", "Failure Response : ${t.message}")
                 LogUtil.e(TAG, "-----------------------")
             }
         })*/
    }

    fun getCallerRole(
        isReceiver: Boolean
    ) {
        isIncomingCall = isReceiver
    }

    fun navigateToCallHistory(
        isIncomingCall: Boolean,
        showTopBarUI: Boolean,
        audioStatus: String,
        videoStatus: String
    ) {
        val intent = Intent(mContext, CallHistoryActivity::class.java)
        intent.putExtra("isIncomingCall", isIncomingCall)
        intent.putExtra("showTopBarUI", showTopBarUI)
        intent.putExtra("audioStatus", audioStatus)
        intent.putExtra("videoStatus", videoStatus)

        mContext?.startActivity(intent)
    }

    fun getSocketInstance(socketInstance: SocketConnection?) {
        Constants.InitializeSocket.socketConnection = socketInstance
    }
}