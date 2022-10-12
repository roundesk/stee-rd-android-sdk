package com.roundesk.sdk.activity

import android.app.ActivityManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.adapter.BottomSheetUserListAdapter
import com.roundesk.sdk.base.AppBaseActivity
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketListener
import com.roundesk.sdk.socket.SocketManager
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil
import com.roundesk.sdk.util.Stopwatch
import com.roundesk.sdk.util.ToastUtil
import de.tavendo.autobahn.WebSocket
import io.webrtc.webrtcandroidframework.*
import io.webrtc.webrtcandroidframework.apprtc.CallActivity
import org.json.JSONObject
import org.webrtc.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit


class VideoCallActivityNew : AppCompatActivity(),
    View.OnClickListener, IWebRTCListener,
    IDataChannelObserver, SocketListener<Any> {

    companion object {
        val TAG: String = VideoCallActivityNew::class.java.simpleName
//        private val SERVER_ADDRESS: String = "stee-dev.roundesk.io:5080"
//        private val SERVER_ADDRESS: String = "stee-rd-uat.roundesk.io:5080"
//        private val SERVER_ADDRESS: String = "stee-rd-uat.roundesk.io:5443"
        private val SERVER_ADDRESS: String = "tele-omnii-lb.intranet.spfoneuat.gov.sg:5443"

//        private val SERVER_URL = "ws://$SERVER_ADDRESS/LiveApp/websocket"
        private val SERVER_URL = "wss://$SERVER_ADDRESS/WebRTCAppEE/websocket"
    }

    private var mRoomId: Int = 0
    private var mMeetingId: Int = 0
    private var mStreamId: String? = null
    private var callerName: String? = null
    private var receiverName: String? = null
    private var mReceiver_stream_id: String? = null
    private var activityName: String? = null
    private var audioStatus: String? = null
    private var videoStatus: String? = null

    private var conferenceManager: ConferenceManager? = null

    private lateinit var publishViewRenderer: SurfaceViewRenderer
    private var play_view_renderer1: SurfaceViewRenderer? = null
    private var play_view_renderer2: SurfaceViewRenderer? = null
    private var play_view_renderer3: SurfaceViewRenderer? = null
    private var play_view_renderer4: SurfaceViewRenderer? = null
    private var imgCallEnd: ImageView? = null
    private var imgBottomCamera: ImageView? = null
    private var imgBottomVideo: ImageView? = null
    private var imgAudio: ImageView? = null
    private var imgArrowUp: ImageView? = null
    private var imgBack: ImageView? = null
    private var switchView: View? = null
    private var dividerView: View? = null
    private var viewHideShowBottomSheet: View? = null
    private var relLayUser12: RelativeLayout? = null
    private var linLayUser34: LinearLayout? = null
    private var relLayToolbar: RelativeLayout? = null
    private var relLayoutMain: RelativeLayout? = null
    private var relLayoutSurfaceViews: RelativeLayout? = null
    private var relLayoutSurfaceViews1: RelativeLayout? = null
    private var chronometer: Chronometer? = null
    private var txtTimer: TextView? = null
    private var txtDoctorName: TextView? = null
    private var linlayCallerDetails: LinearLayout? = null
    private var relLayTopNotification: RelativeLayout? = null
    private var txtCallerName: TextView? = null
    private var txtBottomCallerName: TextView? = null
    private var txtBottomReceiverName: TextView? = null
    private var imgCallRejected: ImageView? = null
    private var txtRinging1: TextView? = null
    private var txtRinging2: TextView? = null
    private var progressBar1: ProgressBar? = null
    private var progressBar2: ProgressBar? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null
    private var recyclerview: RecyclerView? = null

    private lateinit var layoutBottomSheet: RelativeLayout
    lateinit var sheetBehavior: BottomSheetBehavior<View>

    private var isCallerSmall: Boolean = false
    private var isPictureInPictureMode: Boolean = false
    private var isReceiverID: Boolean = false
    private var isOtherCallAccepted: Boolean = false
    private var initialView: Boolean = false
    private var startTimer: Boolean = false
    private var isMultipleUsersConnected: Boolean = false
    var newRoomId: Int? = null
    var newMeetingId: Int? = null
    var tempMeetingId: Int? = null
    var tempRoomId: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var pictureInPictureParamsBuilder = PictureInPictureParams.Builder()
    val MSG_START_TIMER = 0
    val MSG_STOP_TIMER = 1
    val MSG_UPDATE_TIMER = 2

    var timer: Stopwatch = Stopwatch()
    val REFRESH_RATE = 100
    var stoppedTimeDuration: String? = null

    var mpCallReject: MediaPlayer? = null

    private var bottomSheetAdapter: BottomSheetUserListAdapter? = null
    var userStreamIDList: ArrayList<String> = arrayListOf()
    var tempValue: Int? = 0
    var lastStreamSize: Int? = -1


    lateinit var mainHandler: Handler
    private val updateTextTask = object : Runnable {
        override fun run() {
            LogUtil.e(
                TAG,
                "connectedStreamList Size : " + conferenceManager?.connectedStreamList?.size
            )
            runOnUiThread {
                if (tempValue != conferenceManager?.connectedStreamList?.size) {
                    tempValue = conferenceManager?.connectedStreamList?.size
                    refreshRoomDetails()
                }
            }
            mainHandler.postDelayed(this, 1500)
        }
    }

    private fun refreshRoomDetails() {
        if (conferenceManager?.connectedStreamList?.size == 1) {
            lastStreamSize = 1
            userStreamIDList.clear()
            userStreamIDList.add(conferenceManager?.connectedStreamList.toString())
            if (!initialView) {
                imgBack?.isEnabled = true
                switchView?.performClick()
                initialView = true
            }

            if (isMultipleUsersConnected) {
                showTwoUsersUI()
                isMultipleUsersConnected = false
            }
        }

        if (conferenceManager!!.connectedStreamList != null) {
            if (conferenceManager!!.connectedStreamList.size > 0) {
                if (!startTimer) {
                    startCallDurationTimer()
                    linlayCallerDetails?.visibility = View.GONE
                    startTimer = true
                }
            } else {
                if (lastStreamSize != -1) {
                    if (conferenceManager!!.connectedStreamList.isEmpty()) {
                        finish()
                    }
                }
            }
        }

        if (conferenceManager?.connectedStreamList?.size == 2) {
            imgBack?.isEnabled = true
            lastStreamSize = 2
            userStreamIDList.clear()
            userStreamIDList.add(conferenceManager?.connectedStreamList.toString())
            isMultipleUsersConnected = true
            showThreeUsersUI()
        }

        if (conferenceManager?.connectedStreamList?.size == 3) {
            imgBack?.isEnabled = true
            lastStreamSize = 3
            userStreamIDList.clear()
            userStreamIDList.add(conferenceManager?.connectedStreamList.toString())
            isMultipleUsersConnected = true
            showFourUsersUI()
        }

        if (conferenceManager?.connectedStreamList?.size == 4) {
            imgBack?.isEnabled = true
            lastStreamSize = 4
            userStreamIDList.clear()
            userStreamIDList.add(conferenceManager?.connectedStreamList.toString())
            isMultipleUsersConnected = true
            showFiveUsersUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(R.layout.activity_video_call_new)
        storeDataLogsFile()
        mainHandler = Handler(Looper.getMainLooper())
        initSocket()
        getIntentData()
        initView()
    }

    private fun getIntentData() {
        val extras = intent.extras
        if (extras != null) {
            activityName = extras.getString("activity")
            mRoomId = extras.getInt("room_id")
            mMeetingId = extras.getInt("meeting_id")
            mStreamId = extras.getString("stream_id")
            mReceiver_stream_id = extras.getString("receiver_stream_id")
            callerName = extras.getString("caller_name")
            receiverName = extras.getString("receiver_name")
            audioStatus = extras.getString("audioStatus")
            videoStatus = extras.getString("videoStatus")
//            isReceiverID = extras.getBoolean("isIncomingCall")
        }

        LogUtil.e(
            TAG,
            "activity : $activityName"
                    + " room_id : $mRoomId"
                    + " meeting_id : $mMeetingId "
                    + " stream_id : $mStreamId "
                    + " receiver_stream_id : $mReceiver_stream_id"
                    + " callerName : $callerName"
                    + " receiverName : $receiverName"
                    + " isReceiverID : $isReceiverID"
                    + " CALLER_SOCKET_ID : ${Constants.CALLER_SOCKET_ID}"
                    + " audioStatus : $audioStatus"
                    + " videoStatus : $videoStatus"
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
        play_view_renderer2 = findViewById(R.id.play_view_renderer2)
        play_view_renderer3 = findViewById(R.id.play_view_renderer3)
        play_view_renderer4 = findViewById(R.id.play_view_renderer4)
        val playViewRenderers = ArrayList<SurfaceViewRenderer>()

        layoutBottomSheet = findViewById(R.id.bottomSheet)
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        imgCallEnd = findViewById(R.id.imgCallEnd)
        imgBottomCamera = findViewById(R.id.imgBottomCamera)
        imgBottomVideo = findViewById(R.id.imgBottomVideo)
        imgAudio = findViewById(R.id.imgAudio)
        imgArrowUp = findViewById(R.id.imgArrowUp)
        imgBack = findViewById(R.id.imgBack)
        switchView = findViewById(R.id.switchView)
        dividerView = findViewById(R.id.dividerView)
        viewHideShowBottomSheet = findViewById(R.id.viewHideShowBottomSheet)
        relLayToolbar = findViewById(R.id.relLayToolbar)
        relLayoutMain = findViewById(R.id.relLayoutMain)
        relLayoutSurfaceViews = findViewById(R.id.relLayoutSurfaceViews)
        relLayoutSurfaceViews1 = findViewById(R.id.relLayoutSurfaceViews1)
        chronometer = findViewById(R.id.chronometer)
        txtTimer = findViewById(R.id.txtTimer)

        linlayCallerDetails = findViewById(R.id.linlayCallerDetails)
        txtDoctorName = findViewById(R.id.txtDoctorName)
        relLayTopNotification = findViewById(R.id.relLayTopNotification)
        txtCallerName = findViewById(R.id.txtCallerName)
        txtBottomCallerName = findViewById(R.id.txtBottomCallerName)
        txtBottomReceiverName = findViewById(R.id.txtBottomReceiverName)
        imgCallRejected = findViewById(R.id.imgCallRejected)
        txtRinging1 = findViewById(R.id.txtRinging1)
        txtRinging2 = findViewById(R.id.txtRinging2)
        progressBar1 = findViewById(R.id.progressBar1)
        progressBar2 = findViewById(R.id.progressBar2)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)
        recyclerview = findViewById(R.id.recyclerview)

        relLayUser12 = findViewById(R.id.relLayUser12)
        linLayUser34 = findViewById(R.id.linLayUser34)

        imgCallEnd?.isEnabled = false
        imgBottomCamera?.isEnabled = false
        imgBottomVideo?.isEnabled = false
        imgAudio?.isEnabled = false
        imgBack?.isEnabled = false

        linLayUser34?.visibility = View.GONE
        play_view_renderer4?.visibility = View.GONE
        sheetBehavior.isHideable = false

        playViewRenderers.add(findViewById(R.id.play_view_renderer1))
        playViewRenderers.add(findViewById(R.id.play_view_renderer2))
        playViewRenderers.add(findViewById(R.id.play_view_renderer3))
        playViewRenderers.add(findViewById(R.id.play_view_renderer4))

        mpCallReject = MediaPlayer.create(this, R.raw.call_reject_tone);

        bottomSheetAdapter = BottomSheetUserListAdapter(this, listOf())

//        defaultView()
        showDefaultView()
        setListeners()
        sheetBehaviour()
        checkPermissions()

        // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);

        if (mMeetingId != 0 && mRoomId != 0) {
            conferenceDetails(publishViewRenderer, playViewRenderers)
            joinConference()
        }
    }

    private fun conferenceDetails(
        publishViewRenderer: SurfaceViewRenderer,
        playViewRenderers: ArrayList<SurfaceViewRenderer>
    ) {
        this.intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true)
//          this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_CALL, false);
        val strStreamID: String?
        if (mReceiver_stream_id?.contains(Constants.CALLER_SOCKET_ID) == true) {
            strStreamID = mReceiver_stream_id
            linlayCallerDetails?.visibility = View.GONE
            progressBar2?.visibility = View.GONE
            txtRinging2?.visibility = View.GONE
        } else {
            strStreamID = mStreamId
            linlayCallerDetails?.visibility = View.VISIBLE
            progressBar2?.visibility = View.VISIBLE
            txtRinging2?.visibility = View.VISIBLE
        }

        /*if (Constants.CALLER_SOCKET_ID == Constants.UUIDs.USER_HIMANSHU) {
            txtBottomCallerName?.text = "Himanshu"
            txtBottomReceiverName?.text = "Deepak"
            txtDoctorName?.text = "Deepak"
        } else {
            txtBottomCallerName?.text = "Deepak"
            txtBottomReceiverName?.text = "Himanshu"
            txtDoctorName?.text = "Himanshu"
        }*/


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
            null
        )

        conferenceManager?.setPlayOnlyMode(false)
        conferenceManager?.setOpenFrontCamera(true)
    }

    private fun setListeners() {
        imgCallEnd?.setOnClickListener(this)
        imgBottomCamera?.setOnClickListener(this)
        imgBottomVideo?.setOnClickListener(this)
        imgAudio?.setOnClickListener(this)
        imgArrowUp?.setOnClickListener(this)
        imgBack?.setOnClickListener(this)
        switchView?.setOnClickListener(this)
        btnAccept?.setOnClickListener(this)
        btnDecline?.setOnClickListener(this)
        viewHideShowBottomSheet?.setOnClickListener(this)
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
//                finish()
                endCall()
            }

            R.id.imgBottomCamera -> {
                LogUtil.e("imgBottomCamera", "imgBottomCamera")
                runOnUiThread {
                    conferenceManager?.flipCamera()
                }
            }

            R.id.imgAudio -> {
                controlAudio()
            }

            R.id.imgBottomVideo -> {
                controlVideo()
            }

            R.id.imgArrowUp -> {
                toggleBottomSheet()
            }

            R.id.imgBack -> {
                val aspectRatio = Rational(relLayoutMain!!.width, relLayoutMain!!.height)
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
                declineCall(false)
            }

            R.id.viewHideShowBottomSheet -> {
/*                if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                    || sheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN
                ) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                    imgArrowUp?.rotation = 0F
                }

                if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
                }*/
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
            imgBottomVideo?.setImageResource(R.drawable.ic_video_mute)
        } else {
            if (conferenceManager != null) {
                conferenceManager!!.enableVideo()
            }
            imgBottomVideo?.setImageResource(R.drawable.ic_video)
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
        var callTime: String = ""
        if (txtTimer?.text.toString().isEmpty()) {
            callTime = "00:00:00"
        } else {
            callTime = txtTimer?.text.toString()
        }

        val endCallRequest = EndCallRequest(
            mMeetingId.toString(),
            Constants.CALLER_SOCKET_ID,
//            Constants.UUIDs.USER_DEEPAK,
            Constants.API_TOKEN,
            callTime
        )
        val endCallJson = Gson().toJson(endCallRequest)

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val endCall = request.endCall(endCallRequest)
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "API : ${Constants.BASE_URL + Constants.ApiSuffix.API_KEY_END_CALL}")
        LogUtil.e(TAG, "Request Body : $endCallJson")
        LogUtil.e(TAG, "-----------------------")


        endCall.enqueue(object : Callback<BaseDataClassResponse?> {
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
                    if (response.body() != null) {
                        declineCall(true)
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
        imgBottomCamera?.isEnabled = true
        imgBottomVideo?.isEnabled = true
        imgAudio?.isEnabled = true

        getRoomInfoDetails()

        if (audioStatus?.equals("off", ignoreCase = true) == true) {
            if (conferenceManager!!.isPublisherAudioOn) {
                if (conferenceManager != null) {
                    conferenceManager!!.disableAudio()
                }
                imgAudio?.setImageResource(R.drawable.ic_audio_mute)
            }
        }

        if (videoStatus?.equals("off", ignoreCase = true) == true) {
            if (conferenceManager!!.isPublisherVideoOn) {
                if (conferenceManager != null) {
                    conferenceManager!!.disableVideo()
                }
                imgBottomVideo?.setImageResource(R.drawable.ic_video_mute)
            }
        }
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
            LogUtil.e(javaClass.simpleName, e.message!!)
        }
    }

    override fun onMessageSent(buffer: DataChannel.Buffer?, successful: Boolean) {
        val data = buffer!!.data
        val strDataJson = String(data.array(), StandardCharsets.UTF_8)

        LogUtil.e(javaClass.simpleName, "SentEvent: $strDataJson")
    }

    private fun switchLayout(isCallerSmall: Boolean) {
//        dividerView?.visibility = View.GONE

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
            paramsCaller.addRule(RelativeLayout.ALIGN_PARENT_END);
            publishViewRenderer.layoutParams = paramsCaller
            publishViewRenderer.elevation = 2F

//            publishViewRenderer.bringToFront()

//            publishViewRenderer.invalidate()
//            play_view_renderer1?.invalidate()


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
            play_view_renderer1?.layoutParams = paramsReceiver
            play_view_renderer1?.elevation = 2F

//            play_view_renderer1?.bringToFront()

//            publishViewRenderer.invalidate()
//            play_view_renderer1?.invalidate()

        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        LogUtil.e("onPiPMode", "connectedStreamList $tempValue")
        if (isInPictureInPictureMode) {
            isPictureInPictureMode = true
            layoutBottomSheet.visibility = View.GONE
            relLayToolbar?.visibility = View.GONE
            if (tempValue == 1) {
                val paramsReceiver: RelativeLayout.LayoutParams =
                    publishViewRenderer?.getLayoutParams() as RelativeLayout.LayoutParams
                paramsReceiver.height = 210
                paramsReceiver.width = 165
                paramsReceiver.marginEnd = 10
                paramsReceiver.topMargin = 10
                paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                publishViewRenderer?.layoutParams = paramsReceiver
            }
        } else {
            layoutBottomSheet.visibility = View.VISIBLE
            relLayToolbar?.visibility = View.VISIBLE
            if (tempValue == 1) {
                val paramsReceiver: RelativeLayout.LayoutParams =
                    publishViewRenderer?.getLayoutParams() as RelativeLayout.LayoutParams
                paramsReceiver.height = 510
                paramsReceiver.width = 432
                paramsReceiver.marginEnd = 48
                paramsReceiver.topMargin = 48
                paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                publishViewRenderer?.layoutParams = paramsReceiver
            }
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
        if (imgBack?.isEnabled == true) {
            val aspectRatio = Rational(relLayoutMain!!.getWidth(), relLayoutMain!!.getHeight())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build()
                enterPictureInPictureMode(pictureInPictureParamsBuilder.build())
            }
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

        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTextTask)
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
                    bottomSheetAdapter?.manageUIVisibility(createCallSocketDataClass)

                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_ACCEPT_CALL) {
                        receiverName = createCallSocketDataClass.receiver_name
//                        txtBottomReceiverName?.text = receiverName
                        linlayCallerDetails?.visibility = View.GONE
                        if (createCallSocketDataClass.receiverId == Constants.UUIDs.USER_DEEPAK) {
                            txtRinging2?.visibility = View.GONE
                            progressBar2?.visibility = View.GONE
                        }

                    }

                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_NEW_CALL) {
//                        txtDoctorName?.text = "Dr. ${createCallSocketDataClass.receiver_name}"
                        if (createCallSocketDataClass.receiverId == Constants.CALLER_SOCKET_ID) {
                            newRoomId = createCallSocketDataClass.room_id
                            newMeetingId = createCallSocketDataClass.meetingId
                            relLayTopNotification?.visibility = View.VISIBLE
                            txtCallerName?.text = createCallSocketDataClass.msg
                        }
                    }

                    if (createCallSocketDataClass.type == Constants.SocketSuffix.SOCKET_TYPE_REJECT_CALL) {
                        playSong()
                        imgCallEnd?.performClick()
                        txtRinging2?.text = "Rejected"
                        progressBar2?.visibility = View.GONE
                        imgCallRejected?.visibility = View.VISIBLE
                        linlayCallerDetails?.visibility = View.GONE
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
            Constants.CALLER_SOCKET_ID,
            audioStatus.toString(),
            videoStatus.toString(),
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
                        if (response.body()?.roomId != 0 && response.body()?.meetingId != 0) {
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
                LogUtil.e(TAG, "-----------------------")
                LogUtil.e(TAG, "Failure Response : ${t.message}")
                LogUtil.e(TAG, "-----------------------")
            }
        })
    }

    private fun declineCall(endCall: Boolean) {
        if (endCall) {
            tempMeetingId = mMeetingId
            tempRoomId = mRoomId
        } else {
            tempMeetingId = newMeetingId
            tempRoomId = newRoomId
        }
        val declineCallRequest = DeclineCallRequest(
            Constants.CALLER_SOCKET_ID,
            audioStatus.toString(),
            videoStatus.toString(),
            Constants.API_TOKEN,
            tempMeetingId!!,
            tempRoomId!!
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
                isOtherCallAccepted = false
                LogUtil.e(TAG, "Server Header Details : $response")
                LogUtil.e(TAG, "Server Response : ${response.body()}")
                LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                if (response.isSuccessful) {
                    LogUtil.e(TAG, "-----------------------")
                    LogUtil.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                    LogUtil.e(TAG, "-----------------------")
                    if (response.body() != null) {
                        relLayTopNotification?.visibility = View.GONE
                        if (endCall) {
                            finish()
                        }
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

    override fun onDestroy() {
        super.onDestroy()
        conferenceManager?.leaveFromConference()
        initialView = false
        stopSong()
        /*SocketManager(
            this, Constants.InitializeSocket.socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).offAllEvent()*/
    }

    private fun defaultView() {
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
        play_view_renderer1?.layoutParams = paramsReceiver
        play_view_renderer1?.elevation = 2F
    }

    private fun showDefaultView() {
        relLayUser12?.visibility = View.VISIBLE
        linLayUser34?.visibility = View.GONE
        play_view_renderer4?.visibility = View.GONE
        dividerView?.visibility = View.GONE

        val paramsCaller: RelativeLayout.LayoutParams =
            publishViewRenderer?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.marginEnd = 0
        paramsCaller.topMargin = 0
        publishViewRenderer?.layoutParams = paramsCaller

        val paramsReceiver: RelativeLayout.LayoutParams =
            play_view_renderer1?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsReceiver.height = 510
        paramsReceiver.width = 432
        paramsReceiver.marginEnd = 48
        paramsReceiver.topMargin = 48
        paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        play_view_renderer1?.layoutParams = paramsReceiver
        play_view_renderer1?.elevation = 2F
    }

    private fun showTwoUsersUI() {
        relLayUser12?.visibility = View.VISIBLE
        linLayUser34?.visibility = View.GONE
        play_view_renderer2?.visibility = View.GONE
        play_view_renderer3?.visibility = View.GONE
        play_view_renderer4?.visibility = View.GONE
        dividerView?.visibility = View.GONE

        val paramsCaller: RelativeLayout.LayoutParams =
            play_view_renderer1?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.marginEnd = 0
        paramsCaller.topMargin = 0
        play_view_renderer1?.layoutParams = paramsCaller

        val paramsReceiver: RelativeLayout.LayoutParams =
            publishViewRenderer?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsReceiver.height = 510
        paramsReceiver.width = 432
        paramsReceiver.marginEnd = 48
        paramsReceiver.marginStart = 48
        paramsReceiver.topMargin = 48
        paramsReceiver.addRule(RelativeLayout.END_OF, R.id.dividerView);
        paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_END);
        publishViewRenderer?.layoutParams = paramsReceiver
        publishViewRenderer?.elevation = 2F
    }

    private fun showThreeUsersUI() {
        twoUsersTopView()
        linLayUser34?.visibility = View.VISIBLE
        play_view_renderer2?.visibility = View.VISIBLE
        play_view_renderer3?.visibility = View.GONE
        play_view_renderer4?.visibility = View.GONE
    }

    private fun showFourUsersUI() {
        twoUsersTopView()
        linLayUser34?.visibility = View.VISIBLE
        play_view_renderer3?.visibility = View.VISIBLE
        play_view_renderer4?.visibility = View.GONE
    }

    private fun showFiveUsersUI() {
        twoUsersTopView()
        linLayUser34?.visibility = View.VISIBLE
        play_view_renderer3?.visibility = View.VISIBLE
        play_view_renderer4?.visibility = View.VISIBLE
    }

    private fun twoUsersTopView() {
        dividerView?.visibility = View.VISIBLE
        val paramsCaller: RelativeLayout.LayoutParams =
            publishViewRenderer?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.marginEnd = 0
        paramsCaller.topMargin = 0
        paramsCaller.addRule(RelativeLayout.END_OF, R.id.dividerView);
        paramsCaller.addRule(RelativeLayout.ALIGN_PARENT_END);
        publishViewRenderer?.layoutParams = paramsCaller

        val paramsReceiver: RelativeLayout.LayoutParams =
            play_view_renderer1?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsReceiver.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsReceiver.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsReceiver.marginEnd = 0
        paramsReceiver.topMargin = 0

        paramsReceiver.addRule(RelativeLayout.START_OF, R.id.dividerView)
        paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_START)
        play_view_renderer1?.layoutParams = paramsReceiver
        play_view_renderer1?.elevation = 2F
    }

    private fun playSong() {
        mpCallReject?.start()
    }

    private fun pauseSong() {
        mpCallReject?.pause()
    }

    private fun stopSong() {
        mpCallReject?.stop()
//        mp = MediaPlayer.create(this, R.raw.abcd)
    }


    private fun getRoomInfoDetails() {
        val roomDetailRequest = RoomDetailRequest(
            mRoomId.toString(),
            Constants.API_TOKEN
        )
        val roomInfoJson = Gson().toJson(roomDetailRequest)

        val request = ServiceBuilder.buildService(ApiInterface::class.java)
        val getRoomInfoDetail = request.getRoomDetail(roomDetailRequest)
        LogUtil.e(TAG, "-----------------------")
        LogUtil.e(TAG, "API : ${Constants.BASE_URL + Constants.ApiSuffix.API_KEY_ROOM_DETAIL}")
        LogUtil.e(TAG, "Request Body : $roomInfoJson")
        LogUtil.e(TAG, "-----------------------")


        getRoomInfoDetail.enqueue(object : Callback<RoomDetailDataClassResponse?> {
            override fun onResponse(
                call: Call<RoomDetailDataClassResponse?>,
                response: Response<RoomDetailDataClassResponse?>
            ) {
                LogUtil.e(TAG, "Server Header Details : $response")
                LogUtil.e(TAG, "Server Response : ${response.body()}")
                LogUtil.e(TAG, "Server Parsed Response : " + Gson().toJson(response.body()))
                if (response.isSuccessful) {
                    LogUtil.e(TAG, "-----------------------")
                    LogUtil.e(TAG, "Success Response : ${Gson().toJson(response.body())}")
                    LogUtil.e(TAG, "-----------------------")
                    if (response.body() != null) {
                        recyclerview?.layoutManager = LinearLayoutManager(this@VideoCallActivityNew)
                        bottomSheetAdapter =
                            response.body()
                                ?.let {
                                    BottomSheetUserListAdapter(
                                        this@VideoCallActivityNew,
                                        it.success
                                    )
                                }

                        // Setting the Adapter with the recyclerview
                        recyclerview?.adapter = bottomSheetAdapter
                    }
                }
            }

            override fun onFailure(
                call: Call<RoomDetailDataClassResponse?>,
                t: Throwable
            ) {
                LogUtil.e(TAG, "-----------------------")
                LogUtil.e(TAG, "Failure Response : ${t.message}")
                LogUtil.e(TAG, "-----------------------")
            }
        })
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