package com.roundesk.sdk.activity

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.base.AppBaseActivity
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketConnection
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


class IncomingCallActivity : AppCompatActivity(), View.OnClickListener,
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks, SocketListener<Any> {

    private val TAG = IncomingCallActivity::class.java.simpleName

    //    private var mSocket: Socket? = null
    private var imgCallEnd: ImageView? = null
    private var imgCallAccept: ImageView? = null
    private var imgBack: ImageView? = null
    private var txtDoctorName: TextView? = null
    private var room_id: Int = 0
    private var meeting_id: Int = 0
    private var receiver_name: String? = null
    private var audioStatus: String = ""
    private var videoStatus: String = ""

    private val RC_CAMERA_PERM = 123
    private val RC_MICROPHONE_PERM = 124
    private val RC_STORAGE_PERM = 125
    private val RC_TELEPHONE_PERM = 126
    var mpCallRing: MediaPlayer? = null
    lateinit var mainHandler: Handler
    private val updateTask = object : Runnable {
        override fun run() {
            runOnUiThread {
                playSong()
            }
            mainHandler.postDelayed(this, 15000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)
        mpCallRing = MediaPlayer.create(this, R.raw.call_ring_tone);
        mainHandler = Handler(Looper.getMainLooper())

        val extras = intent.extras
        if (extras != null) {
            room_id = extras.getInt("room_id")
            meeting_id = extras.getInt("meeting_id")
            receiver_name = extras.getString("receiver_name")
            audioStatus = extras.getString("audioStatus").toString()
            videoStatus = extras.getString("videoStatus").toString()

            //The key argument here must match that used in the other activity
            LogUtil.e(
                TAG,
                " room_id : $room_id"
                        + " meeting_id : $meeting_id "
                        + " receiver_name : $receiver_name"
                        + " CALLER_SOCKET_ID : ${Constants.CALLER_SOCKET_ID}"
            )
        }
        initSocket()
        initView()
        playSong()
    }

    private fun initSocket() {
        SocketManager(
            this, Constants.InitializeSocket.socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).createCallSocket()
    }

    private fun initView() {
        imgCallEnd = findViewById(R.id.imgCallEnd)
        imgCallAccept = findViewById(R.id.imgCallAccept)
        imgBack = findViewById(R.id.imgBack)
        txtDoctorName = findViewById(R.id.txtDoctorName)

        imgCallEnd?.setOnClickListener(this)
        imgCallAccept?.setOnClickListener(this)
        imgBack?.setOnClickListener(this)

        txtDoctorName?.text = receiver_name
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgCallAccept -> {
                acceptCall()
            }
            R.id.imgCallEnd -> {
                declineCall()
            }
            R.id.imgBack -> {
                finish()
            }
        }
    }

    private fun acceptCall() {
        val acceptCallRequest = AcceptCallRequest(
            Constants.CALLER_SOCKET_ID,
            audioStatus,
            videoStatus,
            Constants.API_TOKEN,
            meeting_id,
            room_id
        )
        val acceptCallJson = Gson().toJson(acceptCallRequest)

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val acceptCall = request.getAcceptCallSocketData(acceptCallRequest)
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "API : ${Constants.BASE_URL + Constants.ApiSuffix.API_KEY_ACCEPT_CALL}")
        LogUtil.e(TAG, "Request Body : $acceptCallJson")
        LogUtil.e(TAG, "-----------------------")

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
                        LogUtil.e(TAG, "-----------------------")
                        LogUtil.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                        LogUtil.e(TAG, "-----------------------")
                        if (response.body() != null) {
//                            Handler(Looper.getMainLooper()).postDelayed({
                            if (response.body()?.roomId != 0 && response.body()?.meetingId != 0) {
                                val intent =
                                    Intent(
                                        this@IncomingCallActivity,
                                        VideoCallActivityNew::class.java
                                    )
                                intent.putExtra("activity", "Incoming")
                                intent.putExtra("room_id", response.body()?.roomId)
                                intent.putExtra("meeting_id", response.body()?.meetingId)
//                            intent.putExtra("receiver_stream_id", response.body()?.streamId)
//                            intent.putExtra("stream_id", response.body()?.caller_streamId)
                                intent.putExtra(
                                    "receiver_stream_id",
                                    response.body()?.caller_streamId
                                )
                                intent.putExtra("stream_id", response.body()?.streamId)
                                intent.putExtra("isIncomingCall", true)
                                intent.putExtra("caller_name", response.body()?.caller_name)
                                intent.putExtra("receiver_name", response.body()?.receiver_name)
                                intent.putExtra("audioStatus", audioStatus)
                                intent.putExtra("videoStatus", videoStatus)
                                startActivity(intent)
//                            }, 3000)
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<AcceptCallDataClassResponse?>,
                    t: Throwable
                ) {
                    LogUtil.e(TAG, "-----------------------")
                    LogUtil.e(TAG, "Failure Response : ${t.message}")
                    LogUtil.e(TAG, "-----------------------")
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
            Constants.CALLER_SOCKET_ID,
            audioStatus,
            videoStatus,
            Constants.API_TOKEN,
            meeting_id,
            room_id
        )
        val declineCallJson = Gson().toJson(declineCallRequest)

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val declineCall = request.declineCall(declineCallRequest)
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "API : ${Constants.BASE_URL + Constants.ApiSuffix.API_KEY_DECLINE_CALL}")
        LogUtil.e(TAG, "Request Body : $declineCallJson")
        LogUtil.e(TAG, "-----------------------")

        declineCall.enqueue(object : Callback<BaseDataClassResponse?> {
            override fun onResponse(
                call: Call<BaseDataClassResponse?>,
                response: Response<BaseDataClassResponse?>
            ) {
                LogUtil.e(TAG, "Server Header Details : $response")
                LogUtil.e(TAG, "Server Response : ${response.body()}")
                LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                if (response.isSuccessful) {
                    LogUtil.e(TAG, "-----------------------")
                    LogUtil.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                    LogUtil.e(TAG, "-----------------------")
                    finish()
                }
            }

            override fun onFailure(
                call: Call<BaseDataClassResponse?>,
                t: Throwable
            ) {
                LogUtil.e(TAG, "-----------------------")
                LogUtil.e(TAG, "Failure Response : ${t.message}")
                LogUtil.e(TAG, "-----------------------")
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

    override fun handleSocketSuccessResponse(response: String, type: String) {
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "handleSocketSuccessResponse: $response")
        LogUtil.e(TAG, "-----------------------")
        when (type) {
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT -> {
                val createCallSocketDataClass: CreateCallSocketDataClass =
                    Gson().fromJson(response, CreateCallSocketDataClass::class.java)

                runOnUiThread {
                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_NEW_CALL) {

                    }

                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_REJECT_CALL) {
//                        playSong()
                        finish()
                    }

                }
            }
        }
    }

    override fun handleSocketErrorResponse(error: Any) {
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "handleSocketErrorResponse: ${Gson().toJson(error)}")
        LogUtil.e(TAG, "-----------------------")
        ToastUtil.displayShortDurationToast(
            this,
            "" + error.toString() + "\n" + resources.getString(R.string.toast_err_in_response) + " " +
                    resources.getString(R.string.toast_request_to_try_later)
        )
    }

    private fun playSong() {
        mpCallRing?.start()
    }

    private fun pauseSong() {
        mpCallRing?.pause()
    }

    private fun stopSong() {
        mpCallRing?.stop()
//        mp = MediaPlayer.create(this, R.raw.abcd)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTask)
    }

    override fun onStop() {
        super.onStop()
        stopSong()
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTask)
    }
}