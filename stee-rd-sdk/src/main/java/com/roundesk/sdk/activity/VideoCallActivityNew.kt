package com.roundesk.sdk.activity

import android.app.ActivityManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.adapter.BottomSheetUserListAdapter
import com.roundesk.sdk.dataclass.*
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.AppSocketManager
import com.roundesk.sdk.socket.SocketListener
import com.roundesk.sdk.util.*
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

    private var frameLaySurfaceViews: FrameLayout? = null
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

    private var relLayParticipant1: RelativeLayout? = null
    private var relLayParticipant2: RelativeLayout? = null
    private var relLayParticipant3: RelativeLayout? = null
    private var relLayParticipant4: RelativeLayout? = null
    private var relLayParticipant5: RelativeLayout? = null
    private var txtInitialViewParticipant1: TextView? = null
    private var txtInitialViewParticipant2: TextView? = null
    private var txtParticipant1: TextView? = null
    private var txtParticipant2: TextView? = null
    private var txtParticipant3: TextView? = null
    private var txtParticipant4: TextView? = null
    private var txtParticipant5: TextView? = null
    private var dividerView1: View? = null
    private var dividerView2: View? = null
    private var relLayNames12: RelativeLayout? = null
    private var relLayNames34: RelativeLayout? = null
    private var relLayNames5: RelativeLayout? = null
    private var relLay2ParticipantsName: RelativeLayout? = null
    private var linLayMultipleParticipantsName: LinearLayout? = null

    private lateinit var layoutBottomSheet: RelativeLayout
    lateinit var sheetBehavior: BottomSheetBehavior<View>

    private var isCallerSmall: Boolean = true
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
    private val displayMetrics = DisplayMetrics()

    var screenWidth = displayMetrics.widthPixels
    var screenHeight = displayMetrics.heightPixels

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
    private var userStreamIDList: ArrayList<String> = arrayListOf()
    private var tempUserStreamIDList: ArrayList<String> = arrayListOf()
    private var joinedUserStreamIds: ArrayList<String> = arrayListOf()
    private var allJoinedUserArray: ArrayList<String> = arrayListOf()
    private var getRoomDetailsDataArrayList: ArrayList<RoomDetailDataClassResponse.Success> =
        arrayListOf()
    private var tempValue: Int? = 0
    private var lastStreamSize: Int? = -1
    private var addedOrRemovedStreamId: String? = ""
    var strParticipant1Name: String? = ""
    var strParticipant2Name: String? = ""
    var strParticipant3Name: String? = ""
    var strParticipant4Name: String? = ""
    var strParticipant5Name: String? = ""
    private var participant1Visible: Boolean = false
    private var participant2Visible: Boolean = false
    private var participant3Visible: Boolean = false
    private var participant4Visible: Boolean = false
    private var participant5Visible: Boolean = false

    private var participantsList: ArrayList<RoomDetailDataClassResponse.Success> = arrayListOf()

    lateinit var mainHandler: Handler
    private val updateTextTask = object : Runnable {
        override fun run() {
            LogUtil.e(
                TAG, "connectedStreamList Size : " + conferenceManager?.connectedStreamList?.size
            )
            runOnUiThread {
                if (NetworkUtils.isConnectedFast(this@VideoCallActivityNew)) {
                    if (tempValue != conferenceManager?.connectedStreamList?.size) {
                        tempValue = conferenceManager?.connectedStreamList?.size
                        refreshRoomDetails()
                    }
                } else {
                    ToastUtil.displayLongDurationToast(
                        this@VideoCallActivityNew,
                        "Your Connection is not Stable. For video calling your connection should be stable"
                    )
                    finish()
                }
            }
            mainHandler.postDelayed(this, 1500)
        }
    }

    private fun refreshRoomDetails() {
        if (conferenceManager?.connectedStreamList?.size != null) {
            if (conferenceManager?.connectedStreamList?.size!! > 0) {
                getRoomInfoDetails()
            } else {
                if (initialView) {
                    conferenceManager?.leaveFromConference()
                    stopCallDurationTimer()
                    endCall(true)
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        windowManager.defaultDisplay.getMetrics(displayMetrics)
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
//                    + " isReceiverID : $isReceiverID"
                    + " CALLER_SOCKET_ID : ${Constants.CALLER_SOCKET_ID}"
                    + " audioStatus : $audioStatus"
                    + " videoStatus : $videoStatus"
        )
    }

    private fun initSocket() {
        /*SocketManager(
            this, Constants.InitializeSocket.socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).createCallSocket()*/
        AppSocketManager(
            this, Constants.InitializeSocket.socketConnection,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).emitSocketEvents()

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

        frameLaySurfaceViews = findViewById(R.id.frameLaySurfaceViews)
        linLayUser34 = findViewById(R.id.linLayUser34)

        relLayParticipant1 = findViewById(R.id.relLayParticipant1)
        relLayParticipant2 = findViewById(R.id.relLayParticipant2)
        relLayParticipant3 = findViewById(R.id.relLayParticipant3)
        relLayParticipant4 = findViewById(R.id.relLayParticipant4)
        relLayParticipant5 = findViewById(R.id.relLayParticipant5)

        txtInitialViewParticipant1 = findViewById(R.id.txtInitialViewParticipant1)
        txtInitialViewParticipant2 = findViewById(R.id.txtInitialViewParticipant2)
        txtParticipant1 = findViewById(R.id.txtParticipant1)
        txtParticipant2 = findViewById(R.id.txtParticipant2)
        txtParticipant3 = findViewById(R.id.txtParticipant3)
        txtParticipant4 = findViewById(R.id.txtParticipant4)
        txtParticipant5 = findViewById(R.id.txtParticipant5)

        dividerView1 = findViewById(R.id.dividerView1)
        dividerView2 = findViewById(R.id.dividerView2)

        relLay2ParticipantsName = findViewById(R.id.relLay2ParticipantsName)
        linLayMultipleParticipantsName = findViewById(R.id.linLayMultipleParticipantsName)
        relLayNames12 = findViewById(R.id.relLayNames12)
        relLayNames34 = findViewById(R.id.relLayNames34)
        relLayNames5 = findViewById(R.id.relLayNames5)

        imgCallEnd?.isEnabled = false
        imgBottomCamera?.isEnabled = false
        imgBottomVideo?.isEnabled = false
        imgAudio?.isEnabled = false
        imgBack?.isEnabled = false

        linLayUser34?.visibility = View.GONE
        displayParticipant5View(false)
        sheetBehavior.isHideable = false

        playViewRenderers.add(findViewById(R.id.play_view_renderer1))
        playViewRenderers.add(findViewById(R.id.play_view_renderer2))
        playViewRenderers.add(findViewById(R.id.play_view_renderer3))
        playViewRenderers.add(findViewById(R.id.play_view_renderer4))

        mpCallReject = MediaPlayer.create(this, R.raw.call_reject_tone);

        bottomSheetAdapter = BottomSheetUserListAdapter(this, listOf())

        showDefaultView()
        setListeners()
        sheetBehaviour()
        checkPermissions()

        // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);
//         this.getIntent().putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, true);

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

        LogUtil.e(TAG, "SERVER_URL : ${URLConfigurationUtil.getServerURL()}")
        conferenceManager = ConferenceManager(
            this,
            this,
            intent,
            URLConfigurationUtil.getServerURL(),
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
                if (conferenceManager?.connectedStreamList?.size == 1) {
                    endCall(true)
                } else {
                    endCall(false)
                }
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
                val sourceRectHint = Rect()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    pictureInPictureParamsBuilder
                        .setAspectRatio(aspectRatio)
                        .setSourceRectHint(sourceRectHint)
                        .build()
                    enterPictureInPictureMode(pictureInPictureParamsBuilder.build())
                }
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
            Log.w(TAG, "Joining Conference")
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

    private fun endCall(endCallForAllUsers: Boolean) {
        var callTime: String = ""
        if (endCallForAllUsers) {
            callTime = if (txtTimer?.text.toString().isEmpty()) {
                "00:00:00"
            } else {
                txtTimer?.text.toString()
            }

            val endCallRequest = EndCallRequest(
                mMeetingId.toString(),
                Constants.CALLER_SOCKET_ID,
                Constants.API_TOKEN,
                callTime
            )

            val endCallJson = Gson().toJson(endCallRequest)
            val request = ServiceBuilder.buildService(ApiInterface::class.java)
            val endCall = request.endCall(endCallRequest)
            LogUtil.e(TAG, "-----------------------")
            LogUtil.e(
                TAG,
                "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_END_CALL}"
            )
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
                            finish()
//                        declineCall(true)
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
        } else {
            finish()
        }
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

//        getRoomInfoDetails()

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
            LogUtil.e(TAG, e.message!!)
        }
    }

    override fun onMessageSent(buffer: DataChannel.Buffer?, successful: Boolean) {
        val data = buffer!!.data
        val strDataJson = String(data.array(), StandardCharsets.UTF_8)

        LogUtil.e(TAG, "SentEvent: $strDataJson")
    }

    private fun switchLayout(isCallerSmall: Boolean) {
        val paramsRemoteVideo: RelativeLayout.LayoutParams =
            play_view_renderer1?.layoutParams as RelativeLayout.LayoutParams
        val paramsLocalVideo: RelativeLayout.LayoutParams =
            publishViewRenderer.layoutParams as RelativeLayout.LayoutParams

        val paramsRelLayLocal: FrameLayout.LayoutParams =
            relLayParticipant1?.layoutParams as FrameLayout.LayoutParams
        val paramsRelLayRemote: FrameLayout.LayoutParams =
            relLayParticipant2?.layoutParams as FrameLayout.LayoutParams

        publishViewRenderer.visibility = View.GONE
        play_view_renderer1?.visibility = View.GONE
        relLayParticipant1?.removeView(publishViewRenderer)
        relLayParticipant2?.removeView(play_view_renderer1)
        publishViewRenderer.setZOrderMediaOverlay(isCallerSmall)
        play_view_renderer1?.setZOrderMediaOverlay(!isCallerSmall)

        if (isCallerSmall) {
            setSmallLocalVideoView(true, paramsLocalVideo)
            setSmallRemoteVideoView(false, paramsRemoteVideo)

            paramsRelLayLocal.height = 510
            paramsRelLayLocal.width = 432
            paramsRelLayLocal.marginEnd = 40
            paramsRelLayLocal.topMargin = 40
            paramsRelLayLocal.gravity = Gravity.END

            paramsRelLayRemote.height = FrameLayout.LayoutParams.MATCH_PARENT
            paramsRelLayRemote.width = FrameLayout.LayoutParams.MATCH_PARENT
            paramsRelLayRemote.marginEnd = 0
            paramsRelLayRemote.topMargin = 0

            relLayParticipant1?.addView(publishViewRenderer, paramsRelLayLocal)
            relLayParticipant2?.addView(play_view_renderer1, paramsRelLayRemote)

//            if (activityName == "Incoming") {
//                txtInitialViewParticipant1?.text = strParticipant1Name
//                txtInitialViewParticipant2?.text = strParticipant2Name
//                txtParticipant1?.text = strParticipant1Name
//                txtParticipant2?.text = strParticipant2Name
//            } else {
            txtInitialViewParticipant1?.text = strParticipant1Name
            txtInitialViewParticipant2?.text = strParticipant2Name
            txtParticipant1?.text = strParticipant1Name
            txtParticipant2?.text = strParticipant2Name
//            }
        } else {
            setSmallLocalVideoView(false, paramsLocalVideo)
            setSmallRemoteVideoView(true, paramsRemoteVideo)

            paramsRelLayLocal.height = FrameLayout.LayoutParams.MATCH_PARENT
            paramsRelLayLocal.width = FrameLayout.LayoutParams.MATCH_PARENT
            paramsRelLayLocal.marginEnd = 0
            paramsRelLayLocal.topMargin = 0

            paramsRelLayRemote.height = 510
            paramsRelLayRemote.width = 432
            paramsRelLayRemote.marginEnd = 40
            paramsRelLayRemote.topMargin = 40
            paramsRelLayRemote.gravity = Gravity.END

            relLayParticipant1?.addView(publishViewRenderer, paramsRelLayLocal)
            relLayParticipant2?.addView(play_view_renderer1, paramsRelLayRemote)

//            if (activityName == "Incoming") {
//                txtInitialViewParticipant1?.text = strParticipant1Name
//                txtInitialViewParticipant2?.text = strParticipant2Name
//                txtParticipant1?.text = strParticipant1Name
//                txtParticipant2?.text = strParticipant2Name
//            } else {
            txtInitialViewParticipant1?.text = strParticipant2Name
            txtInitialViewParticipant2?.text = strParticipant1Name
            txtParticipant1?.text = strParticipant2Name
            txtParticipant2?.text = strParticipant1Name
//            }
        }

        publishViewRenderer.visibility = View.VISIBLE
        play_view_renderer1?.visibility = View.VISIBLE
    }

    private fun setSmallLocalVideoView(
        enableSmallView: Boolean,
        paramsLocalVideo: RelativeLayout.LayoutParams
    ) {
        if (enableSmallView) {
            paramsLocalVideo.height = 510
            paramsLocalVideo.width = 432
        } else {
            paramsLocalVideo.height = FrameLayout.LayoutParams.MATCH_PARENT
            paramsLocalVideo.width = FrameLayout.LayoutParams.MATCH_PARENT
        }

        publishViewRenderer.layoutParams = paramsLocalVideo
    }

    private fun setSmallRemoteVideoView(
        enableSmallView: Boolean,
        paramsRemoteVideo: RelativeLayout.LayoutParams
    ) {
        if (enableSmallView) {
            paramsRemoteVideo.height = 510
            paramsRemoteVideo.width = 432
        } else {
            paramsRemoteVideo.height = FrameLayout.LayoutParams.MATCH_PARENT
            paramsRemoteVideo.width = FrameLayout.LayoutParams.MATCH_PARENT
        }

        play_view_renderer1?.layoutParams = paramsRemoteVideo
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
                if (isCallerSmall) {
                    val paramsReceiver: RelativeLayout.LayoutParams =
                        publishViewRenderer.layoutParams as RelativeLayout.LayoutParams
                    paramsReceiver.height = 210
                    paramsReceiver.width = 165
                    paramsReceiver.marginEnd = 10
                    paramsReceiver.topMargin = 10
                    paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    publishViewRenderer.layoutParams = paramsReceiver
                } else {
                    val paramsReceiver: RelativeLayout.LayoutParams =
                        play_view_renderer1?.layoutParams as RelativeLayout.LayoutParams
                    paramsReceiver.height = 210
                    paramsReceiver.width = 165
                    paramsReceiver.marginEnd = 10
                    paramsReceiver.topMargin = 10
                    paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    play_view_renderer1?.layoutParams = paramsReceiver
                }
            }

            if (tempValue == 2) {
                val paramsReceiver: RelativeLayout.LayoutParams =
                    publishViewRenderer.layoutParams as RelativeLayout.LayoutParams
                val paramsRemote: RelativeLayout.LayoutParams =
                    play_view_renderer1?.layoutParams as RelativeLayout.LayoutParams

                paramsReceiver.height = RelativeLayout.LayoutParams.MATCH_PARENT
                paramsReceiver.width = displayMetrics.widthPixels / 2
                paramsReceiver.marginEnd = 0
                paramsReceiver.topMargin = 0
                paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                publishViewRenderer?.layoutParams = paramsReceiver

                paramsRemote.height = RelativeLayout.LayoutParams.MATCH_PARENT
                paramsRemote.width = displayMetrics.widthPixels / 2
                paramsRemote.marginEnd = 0
                paramsRemote.topMargin = 0
                paramsRemote.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                play_view_renderer1?.layoutParams = paramsReceiver
            }
            if (strParticipant1Name?.isNotEmpty() == true) {
                txtParticipant1?.text = getInitials(strParticipant1Name)
                txtInitialViewParticipant1?.text = getInitials(strParticipant1Name)
            }

            if (strParticipant2Name?.isNotEmpty() == true) {
                txtParticipant2?.text = getInitials(strParticipant2Name)
                txtInitialViewParticipant2?.text = getInitials(strParticipant2Name)
            }

            if (strParticipant3Name?.isNotEmpty() == true) {
                txtParticipant3?.text = getInitials(strParticipant3Name)
            }

            if (strParticipant4Name?.isNotEmpty() == true) {
                txtParticipant4?.text = getInitials(strParticipant4Name)
            }

            if (strParticipant5Name?.isNotEmpty() == true) {
                txtParticipant5?.text = getInitials(strParticipant5Name)
            }
        } else {
            layoutBottomSheet.visibility = View.VISIBLE
            relLayToolbar?.visibility = View.VISIBLE
            if (tempValue == 1) {
                if (isCallerSmall) {
                    val paramsReceiver: RelativeLayout.LayoutParams =
                        publishViewRenderer.layoutParams as RelativeLayout.LayoutParams
                    paramsReceiver.height = 510
                    paramsReceiver.width = 432
                    paramsReceiver.marginEnd = 48
                    paramsReceiver.topMargin = 48
                    paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    publishViewRenderer.layoutParams = paramsReceiver
                }else{
                    val paramsReceiver: RelativeLayout.LayoutParams =
                        play_view_renderer1?.layoutParams as RelativeLayout.LayoutParams
                    paramsReceiver.height = 510
                    paramsReceiver.width = 432
                    paramsReceiver.marginEnd = 48
                    paramsReceiver.topMargin = 48
                    paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    play_view_renderer1?.layoutParams = paramsReceiver
                }
            }
            if (strParticipant1Name?.isNotEmpty() == true) {
                if(isCallerSmall){
                    txtParticipant1?.text = strParticipant1Name
                    txtInitialViewParticipant1?.text = strParticipant1Name
                }else{
                    txtParticipant1?.text = strParticipant2Name
                    txtInitialViewParticipant1?.text = strParticipant2Name
                }
            }

            if (strParticipant2Name?.isNotEmpty() == true) {
                if(isCallerSmall){
                    txtInitialViewParticipant2?.text = strParticipant2Name
                    txtParticipant2?.text = strParticipant2Name
                }else{
                    txtInitialViewParticipant2?.text = strParticipant1Name
                    txtParticipant2?.text = strParticipant1Name
                }
            }

            if (strParticipant3Name?.isNotEmpty() == true) {
                txtParticipant3?.text = strParticipant3Name
            }

            if (strParticipant4Name?.isNotEmpty() == true) {
                txtParticipant4?.text = strParticipant4Name
            }

            if (strParticipant5Name?.isNotEmpty() == true) {
                txtParticipant5?.text = strParticipant5Name
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
            val sourceRectHint = Rect()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pictureInPictureParamsBuilder
                    .setAspectRatio(aspectRatio)
                    .setSourceRectHint(sourceRectHint)
                    .build()
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
                        if (createCallSocketDataClass.receiverId == Constants.UUIDs.USER_2) {
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
        LogUtil.e(
            TAG,
            "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_ACCEPT_CALL}"
        )
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
        LogUtil.e(
            TAG,
            "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_DECLINE_CALL}"
        )
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
        allJoinedUserArray.clear()
        tempUserStreamIDList.clear()
        userStreamIDList.clear()
        joinedUserStreamIds.clear()
        /*SocketManager(
            this, Constants.InitializeSocket.socketConnection!!,
            Constants.SocketSuffix.SOCKET_CONNECT_SEND_CALL_TO_CLIENT
        ).offAllEvent()*/
        participant1Visible = false
        participant2Visible = false
        participant3Visible = false
        participant4Visible = false
        participant5Visible = false
    }

    private fun showDefaultView() {
        frameLaySurfaceViews?.visibility = View.VISIBLE
        linLayUser34?.visibility = View.GONE
        displayParticipant5View(false)
        dividerView?.visibility = View.GONE

        val paramsCaller: RelativeLayout.LayoutParams =
            publishViewRenderer.layoutParams as RelativeLayout.LayoutParams
        paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.marginEnd = 0
        paramsCaller.topMargin = 0
        publishViewRenderer.layoutParams = paramsCaller

        val paramsReceiver: RelativeLayout.LayoutParams =
            play_view_renderer1?.layoutParams as RelativeLayout.LayoutParams
        paramsReceiver.height = 510
        paramsReceiver.width = 432
        paramsReceiver.marginEnd = 40
        paramsReceiver.topMargin = 40
        paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        play_view_renderer1?.layoutParams = paramsReceiver
        play_view_renderer1?.elevation = 2F
    }

    private fun showTwoUsersUI() {
        frameLaySurfaceViews?.visibility = View.VISIBLE
        linLayUser34?.visibility = View.GONE
        displayParticipant3View(false)
        displayParticipant4View(false)
        displayParticipant5View(false)
        dividerView?.visibility = View.GONE

        switchLayout(isCallerSmall)
    }

    private fun manageTopTwoUsersView() {
        val paramsLocalVideo: RelativeLayout.LayoutParams =
            publishViewRenderer.layoutParams as RelativeLayout.LayoutParams
        val paramsRemoteVideo: RelativeLayout.LayoutParams =
            play_view_renderer1?.layoutParams as RelativeLayout.LayoutParams
        val paramsRelLayLocal: FrameLayout.LayoutParams =
            relLayParticipant1?.layoutParams as FrameLayout.LayoutParams
        val paramsRelLayRemote: FrameLayout.LayoutParams =
            relLayParticipant2?.layoutParams as FrameLayout.LayoutParams

        dividerView?.visibility = View.VISIBLE

        publishViewRenderer.visibility = View.GONE
        play_view_renderer1?.visibility = View.GONE
        relLayParticipant1?.removeView(publishViewRenderer)
        relLayParticipant2?.removeView(play_view_renderer1)

        // set Local Video View Params
        paramsLocalVideo.height = RelativeLayout.LayoutParams.MATCH_PARENT
        paramsLocalVideo.width = displayMetrics.widthPixels / 2
        paramsLocalVideo.marginEnd = 0
        paramsLocalVideo.topMargin = 0

        // set Remote Video View Params
        paramsRemoteVideo.height = RelativeLayout.LayoutParams.MATCH_PARENT
        paramsRemoteVideo.width = displayMetrics.widthPixels / 2
        paramsRemoteVideo.marginEnd = 0
        paramsRemoteVideo.topMargin = 0

        publishViewRenderer.layoutParams = paramsLocalVideo
        play_view_renderer1?.layoutParams = paramsRemoteVideo


        // set Relative Layout Local Video View Params
        paramsRelLayLocal.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsRelLayLocal.width = displayMetrics.widthPixels / 2
        paramsRelLayLocal.marginEnd = 0
        paramsRelLayLocal.topMargin = 0
        paramsRelLayLocal.gravity = Gravity.START

        // set Relative Layout Remote Video View Params
        paramsRelLayRemote.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsRelLayRemote.width = displayMetrics.widthPixels / 2
        paramsRelLayRemote.marginEnd = 0
        paramsRelLayRemote.topMargin = 0
        paramsRelLayRemote.gravity = Gravity.END

        relLayParticipant1?.addView(publishViewRenderer, paramsRelLayLocal)
        relLayParticipant2?.addView(play_view_renderer1, paramsRelLayRemote)

        publishViewRenderer.visibility = View.VISIBLE
        play_view_renderer1?.visibility = View.VISIBLE
    }

    private fun showThreeUsersUI() {
        manageTopTwoUsersView()
        linLayUser34?.visibility = View.VISIBLE
        displayParticipant3View(true)
        displayParticipant4View(false)
        displayParticipant5View(false)
    }

    private fun showFourUsersUI() {
        manageTopTwoUsersView()
        linLayUser34?.visibility = View.VISIBLE
        displayParticipant4View(true)
        displayParticipant5View(false)
    }

    private fun showFiveUsersUI() {
        manageTopTwoUsersView()
        linLayUser34?.visibility = View.VISIBLE
        displayParticipant4View(true)
        displayParticipant5View(true)
    }

    private fun displayParticipant3View(visible: Boolean) {
        if (!visible) {
            relLayParticipant3?.visibility = View.GONE
            play_view_renderer2?.visibility = View.GONE
        } else {
            relLayParticipant3?.visibility = View.VISIBLE
            play_view_renderer2?.visibility = View.VISIBLE
        }
    }

    private fun displayParticipant4View(visible: Boolean) {
        if (!visible) {
            relLayParticipant4?.visibility = View.GONE
            play_view_renderer3?.visibility = View.GONE
        } else {
            relLayParticipant4?.visibility = View.VISIBLE
            play_view_renderer3?.visibility = View.VISIBLE
        }
    }

    private fun displayParticipant5View(visible: Boolean) {
        if (!visible) {
            relLayParticipant5?.visibility = View.GONE
            play_view_renderer4?.visibility = View.GONE
        } else {
            relLayParticipant5?.visibility = View.VISIBLE
            play_view_renderer4?.visibility = View.VISIBLE
        }
    }

    private fun manageParticipantsUIVisibility(participantNumber: Int) {
        Log.e(TAG, "participantNumber : $participantNumber")
        when (participantNumber) {
            0 -> {
                val paramsRemoteVideo: RelativeLayout.LayoutParams =
                    play_view_renderer1?.layoutParams as RelativeLayout.LayoutParams
                val paramsRelLayRemote: FrameLayout.LayoutParams =
                    relLayParticipant2?.layoutParams as FrameLayout.LayoutParams

                dividerView?.visibility = View.GONE

                play_view_renderer1?.visibility = View.GONE
                relLayParticipant2?.removeView(play_view_renderer1)

                // set Remote Video View Params
                paramsRemoteVideo.height = RelativeLayout.LayoutParams.MATCH_PARENT
                paramsRemoteVideo.width = displayMetrics.widthPixels / 2
                paramsRemoteVideo.marginEnd = 0
                paramsRemoteVideo.topMargin = 0

                play_view_renderer1?.layoutParams = paramsRemoteVideo

                // set Relative Layout Remote Video View Params
                paramsRelLayRemote.height = FrameLayout.LayoutParams.MATCH_PARENT
                paramsRelLayRemote.width = FrameLayout.LayoutParams.MATCH_PARENT
                paramsRelLayRemote.marginEnd = 0
                paramsRelLayRemote.topMargin = 0
                paramsRelLayRemote.gravity = Gravity.END

                relLayParticipant2?.addView(play_view_renderer1, paramsRelLayRemote)

                publishViewRenderer.visibility = View.GONE
                relLayParticipant1?.visibility = View.GONE
                play_view_renderer1?.visibility = View.VISIBLE
            }
            1 -> {
                displayParticipant3View(false)
                txtParticipant3?.visibility = View.GONE
                dividerView2?.visibility = View.GONE
                if (relLayParticipant5?.visibility == View.VISIBLE) {
                    displayParticipant5View(true)
                } else {
                    displayParticipant5View(false)
                }

                if (relLayParticipant4?.visibility == View.VISIBLE) {
                    displayParticipant4View(true)
                } else {
                    displayParticipant4View(false)
                    linLayUser34?.visibility = View.GONE
                }

            }
            2 -> {
                displayParticipant4View(false)
                txtParticipant4?.visibility = View.GONE
                if (relLayParticipant3?.visibility == View.VISIBLE) {
                    displayParticipant3View(true)
                } else {
                    displayParticipant3View(false)
                    linLayUser34?.visibility = View.GONE
                }

                if (relLayParticipant5?.visibility == View.VISIBLE) {
                    displayParticipant5View(true)
                } else {
                    displayParticipant5View(false)
                }

            }
            3 -> {
                displayParticipant5View(false)
                txtParticipant5?.visibility = View.GONE

                if (relLayParticipant3?.visibility == View.VISIBLE) {
                    displayParticipant3View(true)
                } else {
                    displayParticipant3View(false)
                }

                if (relLayParticipant4?.visibility == View.VISIBLE) {
                    displayParticipant4View(true)
                } else {
                    displayParticipant4View(false)
                }

            }
        }
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
        LogUtil.e(
            TAG,
            "API : ${URLConfigurationUtil.getBaseURL() + Constants.ApiSuffix.API_KEY_ROOM_DETAIL}"
        )
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
                        // Below code is for to show the list of participants in the BottomSheet
                        //-------------------------
                        /*recyclerview?.layoutManager = LinearLayoutManager(this@VideoCallActivityNew)
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

                        participantsList.clear()
                        response.body()?.let { participantsList.addAll(it.success) }*/
                        //-------------------------

                        if (response.body()?.success?.size!! > 0) {
                            response.body()?.success?.let { getRoomDetailsDataArrayList.addAll(it) }
                        }

                        manageUserViews()
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

    private fun manageUserViews() {
        if (conferenceManager?.connectedStreamList?.size == 1) {
            switchView?.isClickable = true
            switchView?.isFocusable = true
            lastStreamSize = 1
            userStreamIDList.clear()
            userStreamIDList.addAll(conferenceManager?.connectedStreamList!!)
            Log.e("$TAG Connected Users", "userStreamIDList : " + Gson().toJson(userStreamIDList))

            if (!initialView) {
                imgBack?.isEnabled = true
                switchLayout(isCallerSmall)
                initialView = true
            }

            if (isMultipleUsersConnected) {
                showTwoUsersUI()
                isMultipleUsersConnected = false
            }

            if (tempUserStreamIDList.isEmpty()) {
                tempUserStreamIDList.add(userStreamIDList[0])
                joinedUserStreamIds.add(tempUserStreamIDList[0])
                allJoinedUserArray.addAll(joinedUserStreamIds)

                relLay2ParticipantsName?.visibility = View.VISIBLE
                linLayMultipleParticipantsName?.visibility = View.GONE
                if (activityName == "Incoming") {
                    txtInitialViewParticipant1?.text = getConnectedUserName(joinedUserStreamIds[0])
                    txtParticipant1?.text = getConnectedUserName(joinedUserStreamIds[0])
                    strParticipant1Name = getConnectedUserName(joinedUserStreamIds[0])

                    txtInitialViewParticipant2?.text = receiverName
                    txtParticipant2?.text = receiverName
                    strParticipant2Name = receiverName
                } else {
                    txtInitialViewParticipant1?.text = getConnectedUserName(joinedUserStreamIds[0])
                    txtParticipant1?.text = getConnectedUserName(joinedUserStreamIds[0])
                    strParticipant1Name = getConnectedUserName(joinedUserStreamIds[0])

                    txtInitialViewParticipant2?.text = callerName
                    txtParticipant2?.text = callerName
                    strParticipant2Name = callerName
                }
            } else {
                if (tempUserStreamIDList.size > userStreamIDList.size) {
                    tempUserStreamIDList.removeAll(userStreamIDList)
                    addedOrRemovedStreamId = tempUserStreamIDList[0]
                    manageParticipantsUIVisibility(allJoinedUserArray.indexOf(addedOrRemovedStreamId))
                    Log.e(
                        "$TAG Connected Users",
                        "Index of Removed Participant : ${
                            allJoinedUserArray.indexOf(addedOrRemovedStreamId)
                        }"
                    )
                    Log.e(
                        "$TAG Connected Users",
                        "Removed Participant Name : $addedOrRemovedStreamId"
                    )
                    joinedUserStreamIds.remove(joinedUserStreamIds.filter { it == addedOrRemovedStreamId }[0])
                    relLay2ParticipantsName?.visibility = View.VISIBLE
                    linLayMultipleParticipantsName?.visibility = View.GONE
                    relLayNames34?.visibility = View.GONE
                    txtParticipant3?.visibility = View.GONE
                }
                tempUserStreamIDList.clear()
                tempUserStreamIDList.addAll(joinedUserStreamIds)
            }
        }

        if (conferenceManager!!.connectedStreamList != null) {
            if (conferenceManager!!.connectedStreamList.isNotEmpty()) {
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
            switchView?.isClickable = false
            switchView?.isFocusable = false
            imgBack?.isEnabled = true
            lastStreamSize = 2
            userStreamIDList.clear()
            userStreamIDList.addAll(conferenceManager?.connectedStreamList!!)
            Log.e("$TAG Connected Users", "userStreamIDList : " + Gson().toJson(userStreamIDList))

            if (tempUserStreamIDList.isNotEmpty()) {
                if (tempUserStreamIDList.size > userStreamIDList.size) {
                    tempUserStreamIDList.removeAll(userStreamIDList)
                    addedOrRemovedStreamId = tempUserStreamIDList[0]
                    manageParticipantsUIVisibility(allJoinedUserArray.indexOf(addedOrRemovedStreamId))
                    Log.e(
                        "$TAG Connected Users",
                        "Index of Removed Participant : ${
                            allJoinedUserArray.indexOf(addedOrRemovedStreamId)
                        }"
                    )
                    Log.e(
                        "$TAG Connected Users",
                        "Removed Participant Name : $addedOrRemovedStreamId"
                    )
                    joinedUserStreamIds.remove(joinedUserStreamIds.filter { it == addedOrRemovedStreamId }[0])
                } else {
                    userStreamIDList.removeAll(tempUserStreamIDList)
                    joinedUserStreamIds.add(userStreamIDList[0])
                    allJoinedUserArray.clear()
                    allJoinedUserArray.addAll(joinedUserStreamIds)
                    showThreeUsersUI()
                    relLay2ParticipantsName?.visibility = View.GONE
                    linLayMultipleParticipantsName?.visibility = View.VISIBLE
                    relLayNames34?.visibility = View.VISIBLE
                    txtParticipant3?.alightParentRightIs(true)
                    dividerView1?.visibility = View.GONE
                    txtParticipant3?.visibility = View.VISIBLE
//                    txtParticipant3?.text = getJoinedUserName(joinedUserStreamIds[1])
//                    strParticipant3Name = getJoinedUserName(joinedUserStreamIds[1])
                    txtParticipant3?.text = getConnectedUserName(joinedUserStreamIds[1])
                    strParticipant3Name = getConnectedUserName(joinedUserStreamIds[1])
                }
                tempUserStreamIDList.clear()
                tempUserStreamIDList.addAll(joinedUserStreamIds)
            } else {
                showThreeUsersUI()
                relLay2ParticipantsName?.visibility = View.GONE
                linLayMultipleParticipantsName?.visibility = View.VISIBLE
                relLayNames34?.visibility = View.VISIBLE
                txtParticipant3?.alightParentRightIs(true)
                dividerView1?.visibility = View.GONE
                txtParticipant3?.visibility = View.VISIBLE
                if (activityName == "Incoming") {
                    txtInitialViewParticipant1?.text = receiverName
                    txtParticipant1?.text = receiverName
                    strParticipant1Name = receiverName
                }
                txtParticipant2?.text = getConnectedUserName(userStreamIDList[0])
                strParticipant2Name = getConnectedUserName(userStreamIDList[0])
                txtParticipant3?.text = getConnectedUserName(userStreamIDList[1])
                strParticipant3Name = getConnectedUserName(userStreamIDList[1])
                Log.e(
                    "$TAG ---------->>>>>>>>>>",
                    "userStreamIDList : " + Gson().toJson(userStreamIDList)
                )
            }

            isMultipleUsersConnected = true
        }

        if (conferenceManager?.connectedStreamList?.size == 3) {
            switchView?.isClickable = false
            switchView?.isFocusable = false
            imgBack?.isEnabled = true
            lastStreamSize = 3
            userStreamIDList.clear()
            userStreamIDList.addAll(conferenceManager?.connectedStreamList!!)
            Log.e("$TAG Connected Users", "userStreamIDList : " + Gson().toJson(userStreamIDList))

            isMultipleUsersConnected = true
            if (tempUserStreamIDList.isNotEmpty()) {
                if (tempUserStreamIDList.size > userStreamIDList.size) {
                    tempUserStreamIDList.removeAll(userStreamIDList)
                    addedOrRemovedStreamId = tempUserStreamIDList[0]
                    manageParticipantsUIVisibility(allJoinedUserArray.indexOf(addedOrRemovedStreamId))
                    Log.e(
                        "$TAG Connected Users",
                        "Index of Removed Participant : ${
                            allJoinedUserArray.indexOf(addedOrRemovedStreamId)
                        }"
                    )

                    Log.e(
                        "$TAG Connected Users",
                        "Removed Participant Name : $addedOrRemovedStreamId"
                    )
                    joinedUserStreamIds.remove(joinedUserStreamIds.filter { it == addedOrRemovedStreamId }[0])
                } else {
                    userStreamIDList.removeAll(tempUserStreamIDList)
                    joinedUserStreamIds.add(userStreamIDList[0])
                    allJoinedUserArray.clear()
                    allJoinedUserArray.addAll(joinedUserStreamIds)
                    showFourUsersUI()
                    txtParticipant3?.alightParentRightIs(false)
                    dividerView1?.visibility = View.VISIBLE
                    txtParticipant4?.visibility = View.VISIBLE
//                    txtParticipant4?.text = getJoinedUserName(joinedUserStreamIds[2])
//                    strParticipant4Name = getJoinedUserName(joinedUserStreamIds[2])
                    txtParticipant4?.text = getConnectedUserName(joinedUserStreamIds[2])
                    strParticipant4Name = getConnectedUserName(joinedUserStreamIds[2])
                }

                tempUserStreamIDList.clear()
                tempUserStreamIDList.addAll(joinedUserStreamIds)
            }
        }

        if (conferenceManager?.connectedStreamList?.size == 4) {
            switchView?.isClickable = false
            switchView?.isFocusable = false
            imgBack?.isEnabled = true
            lastStreamSize = 4
            userStreamIDList.clear()
            userStreamIDList.addAll(conferenceManager?.connectedStreamList!!)
            Log.e("$TAG Connected Users", "userStreamIDList : " + Gson().toJson(userStreamIDList))

            isMultipleUsersConnected = true
            if (tempUserStreamIDList.isNotEmpty()) {
                userStreamIDList.removeAll(tempUserStreamIDList)
                joinedUserStreamIds.add(userStreamIDList[0])
                allJoinedUserArray.clear()
                allJoinedUserArray.addAll(joinedUserStreamIds)
                tempUserStreamIDList.clear()
                tempUserStreamIDList.addAll(joinedUserStreamIds)
                showFiveUsersUI()
                relLayNames5?.visibility = View.VISIBLE
//                txtParticipant5?.text = getJoinedUserName(joinedUserStreamIds[3])
//                strParticipant5Name = getJoinedUserName(joinedUserStreamIds[3])
                txtParticipant5?.text = getConnectedUserName(joinedUserStreamIds[3])
                strParticipant5Name = getConnectedUserName(joinedUserStreamIds[3])
            }
        }

//        Log.e("$TAG Connected Users", "participantsList : " + Gson().toJson(participantsList))
        Log.e("$TAG Connected Users", "joinedUserStreamIds : " + Gson().toJson(joinedUserStreamIds))
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

    private fun getJoinedUserName(participantStreamID: String): String {
        var strParticipantName = ""
        for (item in participantsList.indices) {
            strParticipantName =
                if (participantStreamID.contains(participantsList[item].receiver_id.toString())) {
                    return participantsList[item].name.toString()
                } else {
                    participantStreamID
                }
        }
        return strParticipantName
    }

    infix fun View.alightParentRightIs(aligned: Boolean) {
        val layoutParams = this.layoutParams as? RelativeLayout.LayoutParams
        if (aligned) {
            (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        } else {
            (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(
                RelativeLayout.ALIGN_PARENT_RIGHT,
                0
            )
        }
        this.layoutParams = layoutParams
    }

    private fun getConnectedUserName(participantStreamID: String): String {
        var strParticipantName = ""
        LogUtil.e(
            "getConnectedUserName",
            "getRoomDetailsDataArrayList: ${Gson().toJson(getRoomDetailsDataArrayList)}"
        );
        for (item in getRoomDetailsDataArrayList.indices) {
            strParticipantName =
                if (participantStreamID.lowercase()
                        .contains(
                            getRoomDetailsDataArrayList[item].stream_id.toString().lowercase()
                        )
                ) {
                    return getRoomDetailsDataArrayList[item].name.toString()
                } else {
                    participantStreamID
                }
        }
        return strParticipantName
    }

    private fun getInitials(strName: String?): String {
        var initials = ""
        if (strName?.contains(" ") == true) {
            val strFullName = strName.split(" ")
            val strFirstName = strFullName[0]
            val strLastName = strFullName[1]

            if (strFirstName.isNotEmpty()) {
                initials += strFirstName.take(1)
            }
            if (strLastName.isNotEmpty()) {
                initials += strLastName.take(1)
            }
        } else {
            if (strName?.isNotEmpty() == true) {
                initials = strName.toString().take(2)
            }
        }

        return initials.uppercase()
    }
}