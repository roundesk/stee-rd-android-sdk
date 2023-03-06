package com.roundesk.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.roundesk.sdk.activity.ApiFunctions
import com.roundesk.sdk.activity.IncomingCallActivity
import com.roundesk.sdk.activity.VideoCallActivityNew
import com.roundesk.sdk.base.AppBaseActivity
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketConnection
import com.roundesk.sdk.socket.SocketListener
import com.roundesk.sdk.socket.SocketManager
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil
import com.roundesk.sdk.util.URLConfigurationUtil
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class SettingsActivity : AppCompatActivity(), SocketListener<Any>, View.OnClickListener,
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val TAG = SettingsActivity::class.java.simpleName
    private var txtStartWithChat: TextView? = null
    private var txtStartWithAudio: TextView? = null
    private var txtCallHistory: TextView? = null
    private var relLayTopNotification: RelativeLayout? = null
    private var txtCallerName: TextView? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null

    //    private var isIncomingCall: Boolean = true
    var newRoomId: Int? = null
    var newMeetingId: Int? = null
    private val RC_CAMERA_PERM = 123
    private val RC_MICROPHONE_PERM = 124
    private val RC_STORAGE_PERM = 125
    private var socketConnection: SocketConnection? = null
    var isSettingsScreenOpened: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ForegroundService.startService(
            this@SettingsActivity,
            "STEE-SDK SOCKET Service is running..."
        )
        isSettingsScreenOpened = true
        initSocket()
        txtStartWithChat = findViewById(R.id.txtStartWithChat)
        txtStartWithAudio = findViewById(R.id.txtStartWithAudio)
        txtCallHistory = findViewById(R.id.txtCallHistory)
        relLayTopNotification = findViewById(R.id.relLayTopNotification)
        txtCallerName = findViewById(R.id.txtCallerName)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)

        txtStartWithChat?.setOnClickListener(this)
        txtCallHistory?.setOnClickListener(this)
        btnAccept?.setOnClickListener(this)
        btnDecline?.setOnClickListener(this)
    }

    private fun initSocket() {
        socketConnection = SocketConfig.getInstance()?.getSocketInstance()
        ApiFunctions(this).getSocketInstance(socketConnection)

        Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT =
            SocketConstants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT

        Constants.CALLER_SOCKET_ID = SocketConstants.CALLER_SOCKET_ID

        SocketManager(
            this, socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).createCallSocket()
    }

    override fun handleSocketSuccessResponse(response: String, type: String) {
        Log.e(TAG, "-----------------------")
        Log.e(TAG, "handleSocketSuccessResponse: $response")
        Log.e(TAG, "-----------------------")
        when (type) {
            SocketConstants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT -> {
                val createCallSocketDataClass: CreateCallSocketDataClass =
                    Gson().fromJson(response, CreateCallSocketDataClass::class.java)

                runOnUiThread {
                    if (createCallSocketDataClass.type == SocketConstants.SocketSuffix.SOCKET_TYPE_NEW_CALL) {
                        if (isSettingsScreenOpened == true) {
                            if (createCallSocketDataClass.receiverId != createCallSocketDataClass.callerId) {
                                val intent =
                                    Intent(this@SettingsActivity, IncomingCallActivity::class.java)
                                intent.putExtra("room_id", createCallSocketDataClass.room_id)
                                intent.putExtra("meeting_id", createCallSocketDataClass.meetingId)
                                intent.putExtra(
                                    "audioStatus", SocketConstants.RECEIVER_AUDIO_STATUS
                                )
                                intent.putExtra(
                                    "videoStatus", SocketConstants.RECEIVER_VIDEO_STATUS
                                )
                                intent.putExtra("activity_name", "SettingsActivity")
                                intent.putExtra(
                                    "receiver_name", createCallSocketDataClass.msg
                                )
                                startActivity(intent)
                            }
                        }

                        if (SocketConstants.showIncomingCallTopBarUI) {
                            if (createCallSocketDataClass.receiverId != createCallSocketDataClass.callerId) {
                                newRoomId = createCallSocketDataClass.room_id
                                newMeetingId = createCallSocketDataClass.meetingId
                                relLayTopNotification?.visibility = View.VISIBLE
                                txtCallerName?.text = createCallSocketDataClass.msg
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handleSocketErrorResponse(error: Any) {
        Log.e(TAG, "-----------------------")
        Log.e(TAG, "handleSocketErrorResponse: ${Gson().toJson(error)}")
        Log.e(TAG, "-----------------------")
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.txtStartWithChat -> {
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
            }

            R.id.txtCallHistory -> {
                ApiFunctions(this).navigateToCallHistory(
                    SocketConstants.showIncomingCallUI,
                    SocketConstants.showIncomingCallTopBarUI,
                    SocketConstants.RECEIVER_AUDIO_STATUS,
                    SocketConstants.RECEIVER_VIDEO_STATUS
                )
            }

            R.id.btnAccept -> {
                relLayTopNotification?.visibility = View.GONE
                acceptCall()
            }

            R.id.btnDecline -> {
                declineCall()
                relLayTopNotification?.visibility = View.GONE
            }
        }
    }

    private fun acceptCall() {
        val audioStatus = SocketConstants.CALLER_AUDIO_STATUS
        val videoStatus = SocketConstants.CALLER_VIDEO_STATUS

        val acceptCallRequest = AcceptCallRequest(
            SocketConstants.CALLER_SOCKET_ID,
            audioStatus,
            videoStatus,
            SocketConstants.API_TOKEN,
            newMeetingId!!,
            newRoomId!!
        )
        val acceptCallJson = Gson().toJson(acceptCallRequest)

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val acceptCall = request.getAcceptCallSocketData(acceptCallRequest)
        Log.e(TAG, "-----------------------")
        Log.e(
            TAG,
            "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_ACCEPT_CALL}"
        )
        Log.e(TAG, "Request Body : $acceptCallJson")
        Log.e(TAG, "-----------------------")

        if (hasCameraPermission() && hasMicrophonePermission() && hasStoragePermission()) {

            acceptCall.enqueue(object : Callback<AcceptCallDataClassResponse?> {
                override fun onResponse(
                    call: Call<AcceptCallDataClassResponse?>,
                    response: Response<AcceptCallDataClassResponse?>
                ) {
                    LogUtil.e(TAG, "Server Header Details : $response")
                    LogUtil.e(TAG, "Server Response : ${response.body()}")
                    LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                    if (response.isSuccessful) {
                        Log.e(TAG, "-----------------------")
                        Log.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                        Log.e(TAG, "-----------------------")
                        if (response.body() != null) {
                            relLayTopNotification?.visibility = View.GONE
                            if (response.body()?.roomId != 0 && response.body()?.meetingId != 0) {
                                val intent =
                                    Intent(this@SettingsActivity, VideoCallActivityNew::class.java)
                                intent.putExtra("activity", "ChatActivity")
                                intent.putExtra("room_id", response.body()?.roomId)
                                intent.putExtra("meeting_id", response.body()?.meetingId)
                                intent.putExtra(
                                    "receiver_stream_id",
                                    response.body()?.caller_streamId
                                )
                                intent.putExtra("stream_id", response.body()?.streamId)
                                intent.putExtra(
                                    "isIncomingCall",
                                    SocketConstants.showIncomingCallUI
                                )
                                intent.putExtra("audioStatus", audioStatus)
                                intent.putExtra("videoStatus", videoStatus)
                                startActivity(intent)
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AcceptCallDataClassResponse?>,
                    t: Throwable
                ) {
                    Log.e(TAG, "-----------------------")
                    Log.e(TAG, "Failure Response : ${t.message}")
                    Log.e(TAG, "-----------------------")
                }
            })
            finish()
        } else {
            if (!hasCameraPermission()) {
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera),
                    RC_CAMERA_PERM,
                    Manifest.permission.CAMERA
                )
            }

            if (!hasMicrophonePermission()) {
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_microphone),
                    RC_MICROPHONE_PERM,
                    Manifest.permission.RECORD_AUDIO
                )
            }

            if (!hasStoragePermission()) {
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_storage),
                    RC_STORAGE_PERM,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun declineCall() {
        val declineCallRequest = DeclineCallRequest(
            SocketConstants.CALLER_SOCKET_ID,
            SocketConstants.CALLER_AUDIO_STATUS,
            SocketConstants.CALLER_VIDEO_STATUS,
            SocketConstants.API_TOKEN,
            newMeetingId!!,
            newRoomId!!
        )
        val declineCallJson = Gson().toJson(declineCallRequest)

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val declineCall = request.declineCall(declineCallRequest)
        Log.e(TAG, "-----------------------")
        Log.e(
            TAG,
            "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_DECLINE_CALL}"
        )
        Log.e(TAG, "Request Body : $declineCallJson")
        Log.e(TAG, "-----------------------")

        declineCall.enqueue(object : Callback<BaseDataClassResponse?> {
            override fun onResponse(
                call: Call<BaseDataClassResponse?>,
                response: Response<BaseDataClassResponse?>
            ) {
                LogUtil.e(TAG, "Server Header Details : $response")
                LogUtil.e(TAG, "Server Response : ${response.body()}")
                LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                if (response.isSuccessful) {
                    Log.e(TAG, "-----------------------")
                    Log.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                    Log.e(TAG, "-----------------------")
                    if (response.body() != null)
                        relLayTopNotification?.visibility = View.GONE
                }
            }

            override fun onFailure(
                call: Call<BaseDataClassResponse?>,
                t: Throwable
            ) {
                Log.e(TAG, "-----------------------")
                Log.e(TAG, "Failure Response : ${t.message}")
                Log.e(TAG, "-----------------------")
            }
        })
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    private fun hasMicrophonePermission(): Boolean {
        return (EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO))
    }

    private fun hasStoragePermission(): Boolean {
        return (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                && EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d(TAG, "onRationaleAccepted: $requestCode")
    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d(TAG, "onRationaleDenied: $requestCode")
    }

    override fun onDestroy() {
        super.onDestroy()
        isSettingsScreenOpened = false
    }
}