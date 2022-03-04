package com.roundesk.sdk.activity

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.roundesk.sdk.dataclass.CreateCallDataClassResponse
import com.roundesk.sdk.dataclass.CreateCallRequest
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketConnection
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class ApiFunctions(private var mContext: Activity?) {

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
//        arraylistReceiverId.clear()

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

        call.enqueue(object : Callback<CreateCallDataClassResponse?> {
            override fun onResponse(
                call: Call<CreateCallDataClassResponse?>,
                response: Response<CreateCallDataClassResponse?>
            ) {
                if (response.isSuccessful) {
                    val createCallDataClassResponse: CreateCallDataClassResponse? =
                        response.body()
                    roomId = createCallDataClassResponse?.roomId
                    meetingId = createCallDataClassResponse?.meetingId
                    streamId = createCallDataClassResponse?.streamId
                    callerName = createCallDataClassResponse?.caller_name
                    LogUtil.e(
                        "getCreateCallSocketData",
                        "onSuccess: ${Gson().toJson(response.body())}"
                    )

                    if (!isIncomingCall) {
                        val intent =
                            Intent(mContext, VideoCallActivityNew::class.java)
                        intent.putExtra("activity", "Outgoing")
                        intent.putExtra("room_id", roomId)
                        intent.putExtra("meeting_id", meetingId)
                        intent.putExtra("stream_id", streamId)
                        intent.putExtra("caller_name", callerName)
                        intent.putExtra("isIncomingCall", isIncomingCall)
                        intent.putExtra("audioStatus", audioStatus)
                        intent.putExtra("videoStatus", videoStatus)
                        mContext?.startActivity(intent)
                    }
                }
            }


            override fun onFailure(call: Call<CreateCallDataClassResponse?>, t: Throwable) {
                Log.e("initiateCall", "onFailure : ${t.message}")
            }
        })
    }

    fun getCallerRole(
        isReceiver: Boolean
    ) {
        isIncomingCall = isReceiver
    }

    fun navigateToCallHistory(isIncomingCall: Boolean, showTopBarUI: Boolean) {
        val intent = Intent(mContext, CallHistoryActivity::class.java)
        intent.putExtra("isIncomingCall", isIncomingCall)
        intent.putExtra("showTopBarUI", showTopBarUI)
        mContext?.startActivity(intent)
    }

    fun getSocketInstance(socketInstance : SocketConnection?) {
        Constants.InitializeSocket.socketConnection = socketInstance
    }
}