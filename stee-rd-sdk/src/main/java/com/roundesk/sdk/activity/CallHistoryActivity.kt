package com.roundesk.sdk.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.adapter.CallHistoryAdapter
import com.roundesk.sdk.base.AppBaseActivity
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.socket.SocketListener
import com.roundesk.sdk.socket.SocketManager
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil
import com.roundesk.sdk.util.ToastUtil
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallHistoryActivity : AppBaseActivity(), SocketListener<Any>, View.OnClickListener,
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val TAG = CallHistoryActivity::class.java.simpleName

    private var recyclerview: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var relLayTopNotification: RelativeLayout? = null
    private var txtCallerName: TextView? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null
    private var imgBack: ImageView? = null
    private var isIncomingCall: Boolean = false
    private var showTopBarUI: Boolean = false
    var newRoomId: Int? = null
    var newMeetingId: Int? = null

    private val RC_CAMERA_PERM = 123
    private val RC_MICROPHONE_PERM = 124
    private val RC_STORAGE_PERM = 125

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_history)
        initSocket()
        getIntentData()
        recyclerview = findViewById(R.id.recyclerview)
        progressBar = findViewById(R.id.progressBar)
        relLayTopNotification = findViewById(R.id.relLayTopNotification)
        txtCallerName = findViewById(R.id.txtCallerName)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)
        imgBack = findViewById(R.id.imgBack)

        btnAccept?.setOnClickListener(this)
        btnDecline?.setOnClickListener(this)
        imgBack?.setOnClickListener(this)

        callAPI()
    }

    private fun initSocket() {
        SocketManager(
            this, socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).createCallSocket()
    }

    private fun getIntentData() {
        val extras = intent.extras
        if (extras != null) {
            isIncomingCall = extras.getBoolean("isIncomingCall")
            showTopBarUI = extras.getBoolean("showTopBarUI")

        }
        LogUtil.e(
            TAG, "isIncomingCall : $isIncomingCall"
                    + " showTopBarUI : $showTopBarUI"
        )
    }

    private fun callAPI() {
        val request = ServiceBuilder.buildService(ApiInterface::class.java)

        val call = request.getCallHistoryData(
            "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9",
            "drpbzfjiouhqkaegcvtl",
            "all"
        )

        call.enqueue(object : Callback<List<CallHistoryResponseDataClass?>> {
            override fun onResponse(
                call: Call<List<CallHistoryResponseDataClass?>>,
                response: Response<List<CallHistoryResponseDataClass?>>
            ) {
                if (response.isSuccessful) {
                    progressBar?.visibility = View.GONE
                    LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")
                    val callHistoryResponseDataClass: List<CallHistoryResponseDataClass?> =
                        response.body()!!

                    recyclerview?.layoutManager = LinearLayoutManager(this@CallHistoryActivity)
                    val adapter =
                        CallHistoryAdapter(this@CallHistoryActivity, callHistoryResponseDataClass)

                    // Setting the Adapter with the recyclerview
                    recyclerview?.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<CallHistoryResponseDataClass?>>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
            }
        })
    }

    override fun handleSocketSuccessResponse(response: String, type: String) {
        LogUtil.e(VideoCallActivityNew.TAG, "handleSocketSuccessResponse: $response")
        when (type) {
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT -> {
                val createCallSocketDataClass: CreateCallSocketDataClass =
                    Gson().fromJson(response, CreateCallSocketDataClass::class.java)
                runOnUiThread {
                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_NEW_CALL) {
                        if (isIncomingCall) {
                            val intent =
                                Intent(this@CallHistoryActivity, IncomingCallActivity::class.java)
                            intent.putExtra("room_id", createCallSocketDataClass.room_id)
                            intent.putExtra("meeting_id", createCallSocketDataClass.meetingId)
                            intent.putExtra(
                                "receiver_name",
                                createCallSocketDataClass.msg
                            )
                            startActivity(intent)
                        }

                        if (showTopBarUI) {
                            if (createCallSocketDataClass.receiverId == Constants.UUIDs.USER_DEEPAK) {
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
        LogUtil.e(VideoCallActivityNew.TAG, "handleSocketErrorResponse: ${Gson().toJson(error)}")
        ToastUtil.displayShortDurationToast(
            this,
            "" + error.toString() + "\n" + resources.getString(R.string.toast_err_in_response) + " " +
                    resources.getString(R.string.toast_request_to_try_later)
        )
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.imgBack -> {
                finish()
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
        val acceptCallRequest = AcceptCallRequest(
            Constants.UUIDs.USER_DEEPAK,
            "on",
            "on",
            "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9",
            newMeetingId!!,
            newRoomId!!
        )
        val acceptCallJson = Gson().toJson(acceptCallRequest)
        LogUtil.e(TAG, "json : $acceptCallJson")

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val acceptCall = request.getAcceptCallSocketData(acceptCallRequest)

        if (hasCameraPermission() && hasMicrophonePermission() && hasStoragePermission()) {

            acceptCall.enqueue(object : Callback<AcceptCallDataClassResponse?> {
                override fun onResponse(
                    call: Call<AcceptCallDataClassResponse?>,
                    response: Response<AcceptCallDataClassResponse?>
                ) {
                    LogUtil.e(TAG, "onSuccess: $response")
                    LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")

                    if (response.isSuccessful) {
                        relLayTopNotification?.visibility = View.GONE
                        val intent =
                            Intent(this@CallHistoryActivity, VideoCallActivityNew::class.java)
                        intent.putExtra("activity", "ChatActivity")
                        intent.putExtra("room_id", response.body()?.roomId)
                        intent.putExtra("meeting_id", response.body()?.meetingId)
                        intent.putExtra("receiver_stream_id", response.body()?.caller_streamId)
                        intent.putExtra("stream_id", response.body()?.streamId)
                        startActivity(intent)

                    }
                }

                override fun onFailure(
                    call: Call<AcceptCallDataClassResponse?>,
                    t: Throwable
                ) {
                    Log.e(TAG, "onFailure : ${t.message}")
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
            Constants.UUIDs.USER_DEEPAK,
            "on",
            "on",
            "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9",
            newMeetingId!!,
            newRoomId!!
        )
        val declineCallJson = Gson().toJson(declineCallRequest)
        LogUtil.e(TAG, "json : $declineCallJson")

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val declineCall = request.declineCall(declineCallRequest)

        declineCall.enqueue(object : Callback<BaseDataClassResponse?> {
            override fun onResponse(
                call: Call<BaseDataClassResponse?>,
                response: Response<BaseDataClassResponse?>
            ) {
                LogUtil.e(TAG, "onSuccess: $response")
                LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")
                if (response.isSuccessful) {
                    relLayTopNotification?.visibility = View.GONE
                }
            }

            override fun onFailure(
                call: Call<BaseDataClassResponse?>,
                t: Throwable
            ) {
                LogUtil.e(TAG, "onFailure : ${t.message}")
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
        LogUtil.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        LogUtil.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        LogUtil.d(TAG, "onRationaleAccepted: $requestCode")
    }

    override fun onRationaleDenied(requestCode: Int) {
        LogUtil.d(TAG, "onRationaleDenied: $requestCode")
    }
}