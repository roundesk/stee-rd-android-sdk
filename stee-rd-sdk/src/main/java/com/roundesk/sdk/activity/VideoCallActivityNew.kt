package com.roundesk.sdk.activity

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.*
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketConnection
import com.roundesk.sdk.socket.SocketListener
import com.roundesk.sdk.socket.SocketManager
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil
import com.roundesk.sdk.util.Stopwatch
import com.roundesk.sdk.util.ToastUtil
import de.tavendo.autobahn.WebSocket
import io.antmedia.webrtcandroidframework.*
import io.antmedia.webrtcandroidframework.apprtc.CallActivity
import org.json.JSONObject
import org.webrtc.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class VideoCallActivityNew : AppCompatActivity(),
    View.OnClickListener, IWebRTCListener,
    IDataChannelObserver, SocketListener<Any> {

    companion object {
        val TAG: String = VideoCallActivityNew::class.java.simpleName
        private val SERVER_ADDRESS: String = "stee-dev.roundesk.io:5080"
        private val SERVER_URL = "ws://$SERVER_ADDRESS/LiveApp/websocket"
    }

    private var mRoomId: Int = 0
    private var mMeetingId: Int = 0
    private var mStreamId: String? = null
    private var mReceiver_stream_id: String? = null
    private var activityName: String? = null

    private var conferenceManager: ConferenceManager? = null

    private lateinit var publishViewRenderer: SurfaceViewRenderer
    private var play_view_renderer1: SurfaceViewRenderer? = null
    private var imgCallEnd: ImageView? = null
    private var imgCamera: ImageView? = null
    private var imgVideo: ImageView? = null
    private var imgAudio: ImageView? = null
    private var imgArrowUp: ImageView? = null
    private var imgBack: ImageView? = null
    private var switchView: View? = null
    private var relLayToolbar: RelativeLayout? = null
    private var relLayoutMain: RelativeLayout? = null
    private var relLayoutSurfaceViews: RelativeLayout? = null
    private var chronometer: Chronometer? = null
    private var txtTimer: TextView? = null
    private var txtDoctorName: TextView? = null
    private var linlayCallerDetails: LinearLayout? = null
    private var relLayTopNotification: RelativeLayout? = null
    private var txtCallerName: TextView? = null
    private var txtBottomCallerName: TextView? = null
    private var txtBottomReceiverName: TextView? = null
    private var txtRinging1: TextView? = null
    private var txtRinging2: TextView? = null
    private var progressBar1: ProgressBar? = null
    private var progressBar2: ProgressBar? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null

    private lateinit var layoutBottomSheet: RelativeLayout
    lateinit var sheetBehavior: BottomSheetBehavior<View>

    private var isCallerSmall: Boolean = false
    private var isPictureInPictureMode: Boolean = false
    private var isReceiverID: Boolean = false
    private var isOtherCallAccepted: Boolean = false
    var newRoomId: Int? = null
    var newMeetingId: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var pictureInPictureParamsBuilder = PictureInPictureParams.Builder()
    val MSG_START_TIMER = 0
    val MSG_STOP_TIMER = 1
    val MSG_UPDATE_TIMER = 2

    var timer: Stopwatch = Stopwatch()
    val REFRESH_RATE = 100
    var stoppedTimeDuration: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(R.layout.activity_video_call_new)
        initSocket()
        getIntentData()
        initView()
        startCallDurationTimer()
    }

    private fun getIntentData() {
        val extras = intent.extras
        if (extras != null) {
            activityName = extras.getString("activity")
            mRoomId = extras.getInt("room_id")
            mMeetingId = extras.getInt("meeting_id")
            mStreamId = extras.getString("stream_id")
            mReceiver_stream_id = extras.getString("receiver_stream_id")
            isReceiverID = extras.getBoolean("isIncomingCall")
        }

        LogUtil.e(
            TAG,
            "activity : $activityName"
                    + " room_id : $mRoomId"
                    + " meeting_id : $mMeetingId "
                    + " stream_id : $mStreamId "
                    + " receiver_stream_id : $mReceiver_stream_id"
                    + " isReceiverID : $isReceiverID"
        )
    }

    private fun initSocket() {
        SocketManager(
            this, Constants.InitializeSocket.socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).createCallSocket()
    }

    private fun initView() {
        publishViewRenderer = findViewById(R.id.publish_view_renderer)
        play_view_renderer1 = findViewById(R.id.play_view_renderer1)
        val playViewRenderers = ArrayList<SurfaceViewRenderer>()

        layoutBottomSheet = findViewById(R.id.bottomSheet)
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        imgCallEnd = findViewById(R.id.imgCallEnd)
        imgCamera = findViewById(R.id.imgCamera)
        imgVideo = findViewById(R.id.imgVideo)
        imgAudio = findViewById(R.id.imgAudio)
        imgArrowUp = findViewById(R.id.imgArrowUp)
        imgBack = findViewById(R.id.imgBack)
        switchView = findViewById(R.id.switchView)
        relLayToolbar = findViewById(R.id.relLayToolbar)
        relLayoutMain = findViewById(R.id.relLayoutMain)
        relLayoutSurfaceViews = findViewById(R.id.relLayoutSurfaceViews)
        chronometer = findViewById(R.id.chronometer)
        txtTimer = findViewById(R.id.txtTimer)

        linlayCallerDetails = findViewById(R.id.linlayCallerDetails)
        txtDoctorName = findViewById(R.id.txtDoctorName)
        relLayTopNotification = findViewById(R.id.relLayTopNotification)
        txtCallerName = findViewById(R.id.txtCallerName)
        txtBottomCallerName = findViewById(R.id.txtBottomCallerName)
        txtBottomReceiverName = findViewById(R.id.txtBottomReceiverName)
        txtRinging1 = findViewById(R.id.txtRinging1)
        txtRinging2 = findViewById(R.id.txtRinging2)
        progressBar1 = findViewById(R.id.progressBar1)
        progressBar2 = findViewById(R.id.progressBar2)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)

        imgCallEnd?.isEnabled = false
        imgCamera?.isEnabled = false
        imgVideo?.isEnabled = false
        imgAudio?.isEnabled = false

        playViewRenderers.add(findViewById(R.id.play_view_renderer1))

        setListeners()

        sheetBehaviour()
        checkPermissions()

        // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);

        conferenceDetails(publishViewRenderer, playViewRenderers)
        joinConference()
    }

    private fun conferenceDetails(
        publishViewRenderer: SurfaceViewRenderer,
        playViewRenderers: ArrayList<SurfaceViewRenderer>
    ) {
        this.intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true)
//          this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_CALL, false);
        val strStreamID: String? = if (isReceiverID) {
            mReceiver_stream_id
        } else {
            mStreamId
        }

        txtBottomCallerName?.text = "Himanshu"
        txtBottomReceiverName?.text = "Deepak"
//        txtBottomCallerName?.text = "Deepak"
//        txtBottomReceiverName?.text = "Himanshu"
        progressBar2?.visibility = View.VISIBLE
        txtRinging2?.visibility = View.VISIBLE

        LogUtil.e(TAG, "SERVER_URL : $SERVER_URL")
        conferenceManager = ConferenceManager(
            this,
            this,
            intent,
            SERVER_URL,
            mRoomId.toString(),
            publishViewRenderer,
            playViewRenderers,
            strStreamID,
//            mStreamId,
//            mReceiver_stream_id,
            null
        )

        conferenceManager?.setPlayOnlyMode(false)
        conferenceManager?.setOpenFrontCamera(true)
    }

    private fun setListeners() {
        imgCallEnd?.setOnClickListener(this)
        imgCamera?.setOnClickListener(this)
        imgVideo?.setOnClickListener(this)
        imgAudio?.setOnClickListener(this)
        imgArrowUp?.setOnClickListener(this)
        imgBack?.setOnClickListener(this)
        switchView?.setOnClickListener(this)
        btnAccept?.setOnClickListener(this)
        btnDecline?.setOnClickListener(this)
    }

    private fun checkPermissions() {
        // Check for mandatory permissions.
        for (permission in CallActivity.MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission $permission is not granted", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgCallEnd -> {
                conferenceManager?.leaveFromConference()
                stopCallDurationTimer()
                finish()
                endCall()
            }

            R.id.imgCamera -> {
                Log.e("imgCamera", "imgCamera")
                runOnUiThread {
                    conferenceManager?.flipCamera()
                }
            }

            R.id.imgAudio -> {
                controlAudio()
            }

            R.id.imgVideo -> {
                controlVideo()
            }

            R.id.imgArrowUp -> {
                toggleBottomSheet()
            }

            R.id.imgBack -> {
                val aspectRatio = Rational(relLayoutMain!!.getWidth(), relLayoutMain!!.getHeight())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build()
                    enterPictureInPictureMode(pictureInPictureParamsBuilder.build())
                }
//                enterPictureInPictureMode()
            }

            R.id.switchView -> {
                isCallerSmall = !isCallerSmall
                switchLayout(isCallerSmall)
            }

            R.id.btnAccept -> {
                showAlertDialog()
            }

            R.id.btnDecline -> {
                declineCall()
            }
        }
    }

    private fun joinConference() {
        if (conferenceManager?.isJoined == false) {
            Log.w(javaClass.simpleName, "Joining Conference")
            conferenceManager?.joinTheConference()
        } else {
            conferenceManager?.leaveFromConference()
        }
    }

    private fun controlAudio() {
        if (conferenceManager!!.isPublisherAudioOn) {
            if (conferenceManager != null) {
                conferenceManager!!.disableAudio()
            }
            imgAudio?.setImageResource(R.drawable.ic_audio_mute)
        } else {
            if (conferenceManager != null) {
                conferenceManager!!.enableAudio()
            }
            imgAudio?.setImageResource(R.drawable.ic_audio)
        }
    }

    private fun controlVideo() {
        if (conferenceManager!!.isPublisherVideoOn) {
            if (conferenceManager != null) {
                conferenceManager!!.disableVideo()
            }
            imgVideo?.setImageResource(R.drawable.ic_video_mute)
        } else {
            if (conferenceManager != null) {
                conferenceManager!!.enableVideo()
            }
            imgVideo?.setImageResource(R.drawable.ic_video)
        }
    }

    private fun sheetBehaviour() {
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        imgArrowUp?.rotation = 180F
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        imgArrowUp?.rotation = 0F
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            imgArrowUp?.rotation = 180F
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            imgArrowUp?.rotation = 0F
        }
    }

    private fun endCall() {
        val endCallRequest = EndCallRequest(
            mMeetingId.toString(),
            Constants.UUIDs.USER_HIMANSHU,
//            Constants.UUIDs.USER_DEEPAK,
            "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9",
            txtTimer?.text.toString()
            )
        val endCallJson = Gson().toJson(endCallRequest)
        LogUtil.e(TAG, "json : $endCallJson")

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val endCall = request.endCall(endCallRequest)

        endCall.enqueue(object : Callback<BaseDataClassResponse?> {
            override fun onResponse(
                call: Call<BaseDataClassResponse?>,
                response: Response<BaseDataClassResponse?>
            ) {
                LogUtil.e(TAG, "onSuccess: $response")
                LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")
                if (response.isSuccessful) {
                    finish()
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

    override fun onDisconnected(streamId: String?) {
        LogUtil.e(TAG, "onDisconnected streamId: $streamId")
    }

    override fun onPublishFinished(streamId: String?) {
        LogUtil.e(TAG, "onPublishFinished streamId: $streamId")
    }

    override fun onPlayFinished(streamId: String?) {
        LogUtil.e(TAG, "onPlayFinished streamId: $streamId")
    }

    override fun onPublishStarted(streamId: String?) {
        LogUtil.e(TAG, "onPublishStarted streamId: $streamId")
        imgCallEnd?.isEnabled = true
        imgCamera?.isEnabled = true
        imgVideo?.isEnabled = true
        imgAudio?.isEnabled = true

//        startCallDurationTimer()
        val handler = Handler()
//        runOnUiThread {
        handler.postDelayed({
            progressBar2?.visibility = View.GONE
            txtRinging2?.visibility = View.GONE
        }, 1000)
//        }


    }

    override fun onPlayStarted(streamId: String?) {
        LogUtil.e(TAG, "onPlayStarted streamId: $streamId")
    }

    override fun noStreamExistsToPlay(streamId: String?) {
        LogUtil.e(TAG, "noStreamExistsToPlay streamId: $streamId")
    }

    override fun onError(description: String?, streamId: String?) {
        LogUtil.e(TAG, "onError streamId: $streamId description : $description")
    }

    override fun onSignalChannelClosed(
        code: WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification?,
        streamId: String?
    ) {
        LogUtil.e(TAG, "onSignalChannelClosed streamId: $streamId code : ${Gson().toJson(code)}")
    }

    override fun streamIdInUse(streamId: String?) {
        LogUtil.e(TAG, "streamIdInUse streamId: $streamId")
    }

    override fun onIceConnected(streamId: String?) {
        LogUtil.e(TAG, "onIceConnected streamId: $streamId")
    }

    override fun onIceDisconnected(streamId: String?) {
        LogUtil.e(TAG, "onIceDisconnected streamId: $streamId")
    }

    override fun onTrackList(tracks: Array<out String>?) {
        LogUtil.e(TAG, "onTrackList tracks: ${Gson().toJson(tracks)}")
    }

    override fun onBitrateMeasurement(
        streamId: String?,
        targetBitrate: Int,
        videoBitrate: Int,
        audioBitrate: Int
    ) {
        LogUtil.e(
            TAG, "streamId : $streamId"
                    + "targetBitrate $targetBitrate"
                    + "videoBitrate $videoBitrate"
                    + "audioBitrate $audioBitrate"
        )
    }

    override fun onStreamInfoList(streamId: String?, streamInfoList: ArrayList<StreamInfo>?) {
        LogUtil.e(
            TAG,
            "onStreamInfoList streamId: $streamId streamInfoList: ${Gson().toJson(streamInfoList)}"
        )
    }

    override fun onBufferedAmountChange(previousAmount: Long, dataChannelLabel: String?) {
    }

    override fun onStateChange(state: DataChannel.State?, dataChannelLabel: String?) {
    }

    override fun onMessage(buffer: DataChannel.Buffer?, dataChannelLabel: String?) {
        val data = buffer!!.data
        val strDataJson = String(data.array(), StandardCharsets.UTF_8)

        try {
            val json = JSONObject(strDataJson)
            val eventType = json.getString("eventType")
            val streamId = json.getString("streamId")
            Toast.makeText(this, "$eventType : $streamId", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message!!)
        }
    }

    override fun onMessageSent(buffer: DataChannel.Buffer?, successful: Boolean) {
        val data = buffer!!.data
        val strDataJson = String(data.array(), StandardCharsets.UTF_8)

        Log.e(javaClass.simpleName, "SentEvent: $strDataJson")
    }

    private fun switchLayout(isCallerSmall: Boolean) {
        if (isCallerSmall) {
            val paramsReceiver: RelativeLayout.LayoutParams =
                play_view_renderer1?.getLayoutParams() as RelativeLayout.LayoutParams
            paramsReceiver.height = FrameLayout.LayoutParams.MATCH_PARENT
            paramsReceiver.width = FrameLayout.LayoutParams.MATCH_PARENT
            paramsReceiver.marginEnd = 0
            paramsReceiver.topMargin = 0
            play_view_renderer1?.layoutParams = paramsReceiver

            val paramsCaller: RelativeLayout.LayoutParams =
                publishViewRenderer.getLayoutParams() as RelativeLayout.LayoutParams
            paramsCaller.height = 510
            paramsCaller.width = 432
            paramsCaller.marginEnd = 48
            paramsCaller.topMargin = 48
            paramsCaller.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

//            paramsCaller.gravity = Gravity.TOP or Gravity.END
            publishViewRenderer.layoutParams = paramsCaller
            publishViewRenderer.elevation = 2F

        } else {
            val paramsCaller: RelativeLayout.LayoutParams =
                publishViewRenderer.getLayoutParams() as RelativeLayout.LayoutParams
            paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
            paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
            paramsCaller.marginEnd = 0
            paramsCaller.topMargin = 0
            publishViewRenderer.layoutParams = paramsCaller

            val paramsReceiver: RelativeLayout.LayoutParams =
                play_view_renderer1?.getLayoutParams() as RelativeLayout.LayoutParams
            paramsReceiver.height = 510
            paramsReceiver.width = 432
            paramsReceiver.marginEnd = 48
            paramsReceiver.topMargin = 48
            paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            paramsReceiver.gravity = Gravity.TOP or Gravity.END
            play_view_renderer1?.layoutParams = paramsReceiver
            play_view_renderer1?.elevation = 2F

        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (isInPictureInPictureMode) {
            isPictureInPictureMode = true
            layoutBottomSheet.visibility = View.GONE
            relLayToolbar?.visibility = View.GONE
            val paramsReceiver: RelativeLayout.LayoutParams =
                play_view_renderer1?.getLayoutParams() as RelativeLayout.LayoutParams
            paramsReceiver.height = 210
            paramsReceiver.width = 165
            paramsReceiver.marginEnd = 10
            paramsReceiver.topMargin = 10
            paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            play_view_renderer1?.layoutParams = paramsReceiver
        } else {
            layoutBottomSheet.visibility = View.VISIBLE
            relLayToolbar?.visibility = View.VISIBLE
            val paramsReceiver: RelativeLayout.LayoutParams =
                play_view_renderer1?.getLayoutParams() as RelativeLayout.LayoutParams
            paramsReceiver.height = 510
            paramsReceiver.width = 432
            paramsReceiver.marginEnd = 48
            paramsReceiver.topMargin = 48
            paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            play_view_renderer1?.layoutParams = paramsReceiver
        }
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_START_TIMER -> {
                    timer.start()
                    sendEmptyMessage(MSG_UPDATE_TIMER)
                }
                MSG_UPDATE_TIMER -> {
                    val millis = timer.getElapsedTime()
                    val hh = TimeUnit.MILLISECONDS.toHours(millis)
                    val mm = (TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millis)
                    ))
                    val ss = (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millis)
                    ))
                    stoppedTimeDuration = String.format("%02d:%02d:%02d", hh, mm, ss)
                    txtTimer?.text = stoppedTimeDuration

                    sendEmptyMessageDelayed(
                        MSG_UPDATE_TIMER,
                        REFRESH_RATE.toLong()
                    )
                }
                MSG_STOP_TIMER -> {
                    this.removeMessages(MSG_UPDATE_TIMER)
                    timer.stop()
//                    txtTimer?.setText("" + timer.getElapsedTime())
                    txtTimer?.text = stoppedTimeDuration
                }
                else -> {
                }
            }
        }
    }

    private fun startCallDurationTimer() {
        mHandler.sendEmptyMessage(MSG_START_TIMER);
    }

    private fun stopCallDurationTimer() {
        mHandler.sendEmptyMessage(MSG_STOP_TIMER);
    }

    override fun onBackPressed() {
        val aspectRatio = Rational(relLayoutMain!!.getWidth(), relLayoutMain!!.getHeight())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build()
            enterPictureInPictureMode(pictureInPictureParamsBuilder.build())
        }
    }

    private fun navToLauncherTask(appContext: Context) {
        val activityManager =
            (appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        val appTasks = activityManager.appTasks
        for (task in appTasks) {
            val baseIntent = task.taskInfo.baseIntent
            val categories = baseIntent.categories
            if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                task.moveToFront()
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPictureInPictureMode)
            navToLauncherTask(this)
    }

    override fun handleSocketSuccessResponse(response: String, type: String) {
        LogUtil.e(TAG, "handleSocketSuccessResponse: $response")
        when (type) {
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT -> {
                val createCallSocketDataClass: CreateCallSocketDataClass =
                    Gson().fromJson(response, CreateCallSocketDataClass::class.java)
                runOnUiThread {
                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_ACCEPT_CALL) {
                        linlayCallerDetails?.visibility = View.GONE
                        if (createCallSocketDataClass.receiverId == Constants.UUIDs.USER_DEEPAK) {
                            txtRinging2?.visibility = View.GONE
                            progressBar2?.visibility = View.GONE
                        }
                    }

                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_NEW_CALL) {
//                        txtDoctorName?.text = "Dr. ${createCallSocketDataClass.receiver_name}"
                        if (createCallSocketDataClass.receiverId == Constants.UUIDs.USER_HIMANSHU) {
                            newRoomId = createCallSocketDataClass.room_id
                            newMeetingId = createCallSocketDataClass.meetingId
                            relLayTopNotification?.visibility = View.VISIBLE
                            txtCallerName?.text = createCallSocketDataClass.msg
                        }
                    }

                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_REJECT_CALL) {
                        imgCallEnd?.performClick()
                    }
                }
            }
        }
    }

    override fun handleSocketErrorResponse(error: Any) {
        LogUtil.e(TAG, "handleSocketErrorResponse: ${Gson().toJson(error)}")
        ToastUtil.displayShortDurationToast(
            this,
            "" + error.toString() + "\n" + resources.getString(R.string.toast_err_in_response) + " " +
                    resources.getString(R.string.toast_request_to_try_later)
        )
    }

    private fun showAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Accepting the call will end the current meeting")
            // if the dialog is cancelable
            .setCancelable(false)
            .setPositiveButton("YES", DialogInterface.OnClickListener { dialog, id ->
                acceptCall()
                dialog.dismiss()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })

        val alert = dialogBuilder.create()
        alert.show()
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

        acceptCall.enqueue(object : Callback<AcceptCallDataClassResponse?> {
            override fun onResponse(
                call: Call<AcceptCallDataClassResponse?>,
                response: Response<AcceptCallDataClassResponse?>
            ) {
                LogUtil.e(TAG, "onSuccess: $response")
                LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")

                if (response.isSuccessful) {
                    imgCallEnd?.performClick()
                    relLayTopNotification?.visibility = View.GONE
                    isOtherCallAccepted = true
                    val intent =
                        Intent(this@VideoCallActivityNew, VideoCallActivityNew::class.java)
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
                isOtherCallAccepted = false
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

    override fun onDestroy() {
        super.onDestroy()
        conferenceManager?.leaveFromConference()
    }
}