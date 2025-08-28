package com.roundesk.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import com.roundesk.sdk.activity.ApiFunctions
import com.roundesk.sdk.activity.IncomingCallActivity
import com.roundesk.sdk.activity.VideoCallActivityNew
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketListener
import com.roundesk.sdk.util.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import com.roundesk.sdk.socket.AppSocketManager
import com.roundesk.sdk.socket.SocketListenerHelper
import kotlinx.coroutines.*

class ChatActivity : SocketController(), View.OnClickListener,
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks, SocketListener<Any> {

    private val TAG = ChatActivity::class.java.simpleName
    private var pid = 0
    private var imgVideo: ImageView? = null
    private var txtCallerName: TextView? = null
    private var relLayTopNotification: RelativeLayout? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null
    private var txtUserName: TextView? = null
    var arraylistReceiverId: ArrayList<String> = arrayListOf()
    var newRoomId: Int? = null
    var newMeetingId: Int? = null

    private val RC_CAMERA_PERM = 123
    private val RC_MICROPHONE_PERM = 124
    private val RC_STORAGE_PERM = 125
    var isChatscreenOpened: Boolean? = false
    private val logJob = CoroutineScope(Dispatchers.IO)
    //    private var isIncomingCall: Boolean = false
//    private var socketConnection: SocketConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initSocket()
        initView()

        ApiFunctions(this).getCallerRole(SocketConstants.showIncomingCallUI)
        storeDataLogsFile()
    }



    private fun initSocket() {
        ApiFunctions(this).getSocketInstance(mSocket)

        Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT =
            SocketConstants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT

        Constants.CALLER_SOCKET_ID = SocketConstants.CALLER_SOCKET_ID

        AppSocketManager(
            this, mSocket,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).emitSocketEvents()
    }

    private fun initView() {
        imgVideo = findViewById(R.id.imgVideo)
        relLayTopNotification = findViewById(R.id.relLayTopNotification)
        txtCallerName = findViewById(R.id.txtCallerName)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)
        txtUserName = findViewById(R.id.txtUserName)


//        txtUserName?.text = SocketConstants.CALLER_SOCKET_ID
        imgVideo?.setOnClickListener(this)
        btnAccept?.setOnClickListener(this)
        btnDecline?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgVideo -> {
                arraylistReceiverId.clear()
                arraylistReceiverId.add(SocketConstants.UUIDs.USER_1)
                arraylistReceiverId.add(SocketConstants.UUIDs.USER_2)
                arraylistReceiverId.add(SocketConstants.UUIDs.USER_3)
                arraylistReceiverId.add(SocketConstants.UUIDs.USER_4)
                arraylistReceiverId.add(SocketConstants.UUIDs.USER_5)

                arraylistReceiverId.remove(SocketConstants.CALLER_SOCKET_ID)

                Log.e("Chat Activity", "arraylistReceiverId: " + Gson().toJson(arraylistReceiverId))
                /*if (SocketConstants.CALLER_SOCKET_ID == SocketConstants.UUIDs.USER_1)
                    arraylistReceiverId.add(SocketConstants.UUIDs.USER_2)
                else
                    arraylistReceiverId.add(SocketConstants.UUIDs.USER_1)*/

                if (hasCameraPermission() && hasMicrophonePermission() && hasStoragePermission()) {

                    if (isNetworkConnected(this)) {
                        // Below line will initiate the call
                        if (isConnectedConnectionFast(this)) {
                            ApiFunctions(this).initiateCall(
                                arraylistReceiverId,
                                "paramedic",
                                SocketConstants.CALLER_SOCKET_ID,
                                SocketConstants.CALLER_AUDIO_STATUS,
                                SocketConstants.CALLER_VIDEO_STATUS,
                                "a3dt3ffdd"
                            )
                        } else {
                            ToastUtil.displayLongDurationToast(
                                this,
                                "Your Connection is not Stable. For video calling your connection should be stable"
                            )
                        }
                    } else {
                        ToastUtil.displayLongDurationToast(
                            this,
                            "Please check your internet Connection"
                        )
                    }
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
//        Log.e(TAG, "-----------------------")
        Log.e(
            TAG,
            "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_ACCEPT_CALL}"
        )
        Log.e(TAG, "Request Body : $acceptCallJson")
//        Log.e(TAG, "-----------------------")

        if (hasCameraPermission() && hasMicrophonePermission() && hasStoragePermission()) {

            acceptCall.enqueue(object : Callback<AcceptCallDataClassResponse?> {
                override fun onResponse(
                    call: Call<AcceptCallDataClassResponse?>,
                    response: Response<AcceptCallDataClassResponse?>
                ) {
                    LogUtil.e(TAG, "Server Header Details : $response")
                    LogUtil.e(TAG, "Server Response : ${response.body()}")
//                    LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                    if (response.isSuccessful) {
//                        Log.e(TAG, "-----------------------")
                        Log.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
//                        Log.e(TAG, "-----------------------")
                        if (response.body() != null) {
                            relLayTopNotification?.visibility = View.GONE
                            if (response.body()?.roomId != 0 && response.body()?.meetingId != 0) {
                                val intent =
                                    Intent(this@ChatActivity, VideoCallActivityNew::class.java)
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
//                    Log.e(TAG, "-----------------------")
                    Log.e(TAG, "Failure Response : ${t.message}")
//                    Log.e(TAG, "-----------------------")
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
//        Log.e(TAG, "-----------------------")
//        Log.e(
//            TAG,
//            "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_DECLINE_CALL}"
//        )
        Log.e(TAG, "Request Body : $declineCallJson")
//        Log.e(TAG, "-----------------------")

        declineCall.enqueue(object : Callback<BaseDataClassResponse?> {
            override fun onResponse(
                call: Call<BaseDataClassResponse?>,
                response: Response<BaseDataClassResponse?>
            ) {
//                LogUtil.e(TAG, "Server Header Details : $response")
//                LogUtil.e(TAG, "Server Response : ${response.body()}")
                LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                if (response.isSuccessful) {
//                    Log.e(TAG, "-----------------------")
                    Log.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
//                    Log.e(TAG, "-----------------------")
                    if (response.body() != null)
                        relLayTopNotification?.visibility = View.GONE
                }
            }

            override fun onFailure(
                call: Call<BaseDataClassResponse?>,
                t: Throwable
            ) {
//                Log.e(TAG, "-----------------------")
                Log.e(TAG, "Failure Response : ${t.message}")
//                Log.e(TAG, "-----------------------")
            }
        })
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    private fun hasMicrophonePermission(): Boolean {
        return (EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO))
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT< Build.VERSION_CODES.S_V2){
            (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    && EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }else{
            true
        }

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

    override fun handleSocketSuccessResponse(response: String, type: String) {
//        Log.e(TAG, "-----------------------")
        Log.e(TAG, "handleSocketSuccessResponse: $response")
//        Log.e(TAG, "-----------------------")
        if (response.contains("\"type\":\"camera status\"")){
            val muteData = Gson().fromJson(response, SocketMuteVideoData::class.java)
            SocketListenerHelper.muteVideoListState(
                muteData.caller_name,
                muteData.camera.contains("on", ignoreCase = true)
            )
        }
        when (type) {
            SocketConstants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT -> {
                val createCallSocketDataClass: CreateCallSocketDataClass =
                    Gson().fromJson(response, CreateCallSocketDataClass::class.java)

                runOnUiThread {
                    if (createCallSocketDataClass.type == SocketConstants.SocketSuffix.SOCKET_TYPE_NEW_CALL) {
                        if (isChatscreenOpened == true) {
                            if (createCallSocketDataClass.receiverId != createCallSocketDataClass.callerId) {
                                val intent =
                                    Intent(this@ChatActivity, IncomingCallActivity::class.java)
                                intent.putExtra("room_id", createCallSocketDataClass.room_id)
                                intent.putExtra("meeting_id", createCallSocketDataClass.meetingId)
                                intent.putExtra(
                                    "audioStatus", SocketConstants.RECEIVER_AUDIO_STATUS
                                )
                                intent.putExtra(
                                    "videoStatus", SocketConstants.RECEIVER_VIDEO_STATUS
                                )
                                intent.putExtra("activity_name", "ChatActivity")
                                intent.putExtra(
                                    "receiver_name", createCallSocketDataClass.msg
                                )
                                startActivity(intent)
                                isChatscreenOpened = false
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
//        Log.e(TAG, "-----------------------")
        Log.e(TAG, "handleSocketErrorResponse: ${Gson().toJson(error)}")
//        Log.e(TAG, "-----------------------")
    }


    private fun storeDataLogsFile() {
        if (isExternalStorageWritable()) {
            lifecycleScope.launch{
                repeatOnLifecycle(Lifecycle.State.RESUMED){
                    SaveLogsToFile(applicationContext).startLog("cht")
                }
            }
        }
    }



    private fun deleteFirstFileFromLogDir(file: File){
        val fileList = file.listFiles()!!.asList().sortedBy { it.lastModified() }
        if(fileList.size > 10){
            fileList[0].delete()
            deleteFirstFileFromLogDir(file)
        }
    }

    /* Checks if external storage is available for read and write */
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    /* Checks if external storage is available to at least read */
    private fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    private fun isNetworkConnected(context: Context): Boolean {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: isPowerSaveMode(context)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun isPowerSaveMode(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isPowerSaveMode
        } else {
            false
        }
    }

    fun isConnectedConnectionFast(context: Context): Boolean {
        val info = NetworkUtils.getNetworkInfo(context)
        return info != null && info.isConnected && checkConnectionSpeed(
            info.type,
            info.subtype
        )
    }


    private fun checkConnectionSpeed(type: Int, subType: Int): Boolean {
        return if (type == ConnectivityManager.TYPE_WIFI) {
            true
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            when (subType) {
                TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
                TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> true // ~ 400-1000 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_A -> true // ~ 600-1400 kbps
                TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
                TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
                TelephonyManager.NETWORK_TYPE_HSPA -> true // ~ 700-1700 kbps
                TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
                TelephonyManager.NETWORK_TYPE_UMTS -> true // ~ 400-7000 kbps
                /*
         * Above API level 7, make sure to set android:targetSdkVersion
         * to appropriate level to use these
         */
                TelephonyManager.NETWORK_TYPE_EHRPD // API level 11
                -> true // ~ 1-2 Mbps
                TelephonyManager.NETWORK_TYPE_EVDO_B // API level 9
                -> true // ~ 5 Mbps
                TelephonyManager.NETWORK_TYPE_HSPAP // API level 13
                -> true // ~ 10-20 Mbps
                TelephonyManager.NETWORK_TYPE_IDEN // API level 8
                -> false // ~25 kbps
                TelephonyManager.NETWORK_TYPE_LTE // API level 11
                -> true // ~ 10+ Mbps
                // Unknown
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
                else -> false
            }
        } else {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isChatscreenOpened = false

//        lifecycleScope.launch(Dispatchers.IO){
//            SaveLogsToFile(applicationContext).stopLog()
//        }
    }

    override fun onPause() {
        super.onPause()
        logJob.cancel()
//        lifecycleScope.launch(Dispatchers.IO){
//            SaveLogsToFile(applicationContext).stopLog()
//        }
    }

    override fun onResume() {
        super.onResume()
        isChatscreenOpened = true
    }
}