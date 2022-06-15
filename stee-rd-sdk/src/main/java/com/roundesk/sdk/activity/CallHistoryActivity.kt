package com.roundesk.sdk.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.adapter.CallHistoryAdapter
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
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
import java.io.File
import java.io.IOException

class CallHistoryActivity : AppCompatActivity(), SocketListener<Any>, View.OnClickListener,
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
    private var audioStatus: String = ""
    private var videoStatus: String = ""
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
        storeDataLogsFile()
    }

    private fun initSocket() {
        SocketManager(
            this, Constants.InitializeSocket.socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).createCallSocket()
    }

    private fun getIntentData() {
        val extras = intent.extras
        if (extras != null) {
            isIncomingCall = extras.getBoolean("isIncomingCall")
            showTopBarUI = extras.getBoolean("showTopBarUI")
            audioStatus = extras.getString("audioStatus").toString()
            videoStatus = extras.getString("videoStatus").toString()

        }
        LogUtil.e(
            TAG, "isIncomingCall : $isIncomingCall"
                    + " showTopBarUI : $showTopBarUI"
        )
    }

    private fun callAPI() {
        val request = ServiceBuilder.buildService(ApiInterface::class.java)

        val call = request.getCallHistoryData(
            Constants.API_TOKEN,
            Constants.CALLER_SOCKET_ID,
            "all"
        )
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "API : ${Constants.BASE_URL + Constants.ApiSuffix.API_KEY_ALL_CALL}")
        LogUtil.e(
            TAG,
            "Request Parameters : ${
                "apiToken : " + Constants.API_TOKEN
                        + " uuid : " + Constants.CALLER_SOCKET_ID
                        + " type : all"
            }"
        )
        LogUtil.e(TAG, "-----------------------")

        call.enqueue(object : Callback<List<CallHistoryResponseDataClass?>> {
            override fun onResponse(
                call: Call<List<CallHistoryResponseDataClass?>>,
                response: Response<List<CallHistoryResponseDataClass?>>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        progressBar?.visibility = View.GONE
                        LogUtil.e(TAG, "-----------------------")
                        LogUtil.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                        LogUtil.e(TAG, "-----------------------")
                        val callHistoryResponseDataClass: List<CallHistoryResponseDataClass?> =
                            response.body()!!

                        recyclerview?.layoutManager = LinearLayoutManager(this@CallHistoryActivity)
                        val adapter =
                            CallHistoryAdapter(
                                this@CallHistoryActivity,
                                callHistoryResponseDataClass
                            )

                        // Setting the Adapter with the recyclerview
                        recyclerview?.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<List<CallHistoryResponseDataClass?>>, t: Throwable) {
                LogUtil.e(TAG, "-----------------------")
                LogUtil.e(TAG, "Failure Response : ${t.message}")
                LogUtil.e(TAG, "-----------------------")
            }
        })
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
                        if (createCallSocketDataClass.receiverId != createCallSocketDataClass.callerId) {
//                            if (isIncomingCall) {
                            val intent =
                                Intent(this@CallHistoryActivity, IncomingCallActivity::class.java)
                            intent.putExtra("room_id", createCallSocketDataClass.room_id)
                            intent.putExtra("meeting_id", createCallSocketDataClass.meetingId)
                            intent.putExtra("audioStatus", audioStatus)
                            intent.putExtra("videoStatus", videoStatus)
                            intent.putExtra(
                                "receiver_name",
                                createCallSocketDataClass.msg
                            )
                            startActivity(intent)
                        }

                        if (showTopBarUI) {
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
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "handleSocketErrorResponse: ${Gson().toJson(error)}")
        LogUtil.e(TAG, "-----------------------")
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
            Constants.CALLER_SOCKET_ID,
            audioStatus,
            videoStatus,
            Constants.API_TOKEN,
            newMeetingId!!,
            newRoomId!!
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
                    LogUtil.e(TAG, "-----------------------")
                    LogUtil.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                    LogUtil.e(TAG, "-----------------------")

                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            relLayTopNotification?.visibility = View.GONE
                            val intent =
                                Intent(this@CallHistoryActivity, VideoCallActivityNew::class.java)
                            intent.putExtra("activity", "ChatActivity")
                            intent.putExtra("room_id", response.body()?.roomId)
                            intent.putExtra("meeting_id", response.body()?.meetingId)
                            intent.putExtra("receiver_stream_id", response.body()?.caller_streamId)
                            intent.putExtra("stream_id", response.body()?.streamId)
                            intent.putExtra("audioStatus", audioStatus)
                            intent.putExtra("videoStatus", videoStatus)
                            startActivity(intent)
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
            newMeetingId!!,
            newRoomId!!
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
                LogUtil.e(TAG, "-----------------------")
                LogUtil.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                LogUtil.e(TAG, "-----------------------")
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        relLayTopNotification?.visibility = View.GONE
                    }
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


    /*private fun callUploadLogAPI() {
        val request = ServiceBuilder.buildServiceToUploadDataLogs(ApiInterface::class.java)

        val call = request.uploadDataLogs(Constants.API_TOKEN,)

        call.enqueue(object : Callback<BaseDataClassResponse?> {
            override fun onResponse(
                call: Call<BaseDataClassResponse?>,
                response: Response<BaseDataClassResponse?>
            ) {
                if (response.isSuccessful) {
                    progressBar?.visibility = View.GONE
                    LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")

                }
            }

            override fun onFailure(call: Call<BaseDataClassResponse?>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
            }
        })
    }*/

    private fun getFilePath() {
//        val m = packageManager
//        var s = packageName
//        val p = m.getPackageInfo(s, 0)
//        s = p.applicationInfo.dataDir
//        LogUtil.e(TAG, "filePath: $s")

        val appPath: String =
            getApplicationContext().getFilesDir().getAbsolutePath() + "/STEE_APP_DATA_LOGS/logs"
        LogUtil.e(TAG, "filePath: $appPath")
        getAllFilesInAppPackage()
    }

    private fun getAllFilesInAppPackage() {
/*
        val listOfFiles: Array<String> = this.getFilesDir().list()
        Log.d("Files", "Size: " + listOfFiles.size)
*/

/*        val path = Environment.getExternalStorageDirectory().toString() + "/files/STEE_APP_DATA_LOGS/logs"
        Log.d("Files", "Path: $path")
        val directory = File(path)
        val files = directory.listFiles()
        Log.d("Files", "Size: " + files.size)
        for (i in files.indices) {
            Log.d("Files", "FileName:" + files[i].name)
        }*/
    }


    private fun storeDataLogsFile() {
        if (isExternalStorageWritable()) {
//            val appDirectory = File(Environment.getExternalStorageDirectory().toString() + "/STEE_APP_DATA_LOGS")
            val cDir: File? = applicationContext?.getExternalFilesDir(null);
            val appDirectory = File(cDir?.path + "/" + "STEE_APP_DATA_LOGS")
            val logDirectory = File("$appDirectory/logs")
            val logFile = File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt")
            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir()
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir()
            }

            // clear the previous logcat and then write the new one to the file
            try {
//                Process process = Runtime.getRuntime().exec("logcat -c");
                val process = Runtime.getRuntime().exec("logcat -f $logFile")

                LogUtil.e("SocketConfig", "File Path $process");

            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (isExternalStorageReadable()) {
            // only readable
        } else {
            // not accessible
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
}