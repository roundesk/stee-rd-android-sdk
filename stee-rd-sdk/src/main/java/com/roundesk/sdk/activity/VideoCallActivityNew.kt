package com.roundesk.sdk.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk_stee_sdk.util.LogUtil
import de.tavendo.autobahn.WebSocket
import io.antmedia.webrtcandroidframework.ConferenceManager
import io.antmedia.webrtcandroidframework.IDataChannelObserver
import io.antmedia.webrtcandroidframework.IWebRTCListener
import io.antmedia.webrtcandroidframework.StreamInfo
import io.antmedia.webrtcandroidframework.apprtc.CallActivity
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.SurfaceViewRenderer
import java.nio.charset.StandardCharsets
import java.util.*

class VideoCallActivityNew : AppCompatActivity(), View.OnClickListener, IWebRTCListener {

    companion object {
        val TAG: String = VideoCallActivityNew::class.java.simpleName
        private val SERVER_ADDRESS: String = "stee-dev.roundesk.io:5080"
        private val SERVER_URL = "ws://$SERVER_ADDRESS/WebRTCAppEE/websocket"
    }


    private var mRoomId: Int = 0
    private var mMeetingId: Int = 0
    private var mCallerStreamId: String? = null
    private var mReceiverStreamId: String? = null
    private var activityName: String? = null
    private var isIncomingCall: Boolean = false

    private var conferenceManager: ConferenceManager? = null

    private var btnJoinConference: Button? = null
    private var imgCallEnd: ImageView? = null
    private var imgCamera: ImageView? = null
    private var imgVideo: ImageView? = null
    private var imgAudio: ImageView? = null
    private var imgArrowUp: ImageView? = null
    private lateinit var layoutBottomSheet: CardView
    lateinit var sheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(R.layout.activity_video_call_new)
        getIntentData()
        initView()
    }

    private fun getIntentData() {
        val extras = intent.extras
        if (extras != null) {
            activityName = extras.getString("activity")
            mRoomId = extras.getInt("room_id")
            mMeetingId = extras.getInt("meeting_id")
            mCallerStreamId = extras.getString("stream_id")
            mReceiverStreamId = extras.getString("receiver_stream_id")
            isIncomingCall = extras.getBoolean("isIncomingCall")
        }
        LogUtil.e(
            TAG,
            "activity : $activityName"
                    + " room_id : $mRoomId"
                    + " meeting_id : $mMeetingId "
                    + "stream_id : $mCallerStreamId"
                    + "receiver_stream_id : $mReceiverStreamId"
        )
    }

    private fun initView() {
        val publishViewRenderer: SurfaceViewRenderer = findViewById(R.id.publish_view_renderer)
        val playViewRenderers = ArrayList<SurfaceViewRenderer>()

        btnJoinConference = findViewById(R.id.btnJoinConference)
        layoutBottomSheet = findViewById(R.id.bottomSheet)
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        imgCallEnd = findViewById(R.id.imgCallEnd)
        imgCamera = findViewById(R.id.imgCamera)
        imgVideo = findViewById(R.id.imgVideo)
        imgAudio = findViewById(R.id.imgAudio)
        imgArrowUp = findViewById(R.id.imgArrowUp)

        playViewRenderers.add(findViewById(R.id.play_view_renderer1))

        setListeners()

        sheetBehaviour()
        checkPermissions()
        conferenceDetails(publishViewRenderer, playViewRenderers)

        joinConference()
    }

    private fun conferenceDetails(
        publishViewRenderer: SurfaceViewRenderer,
        playViewRenderers: ArrayList<SurfaceViewRenderer>
    ) {
        this.intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true)
//          this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_CALL, false);
        var streamIdUser: String? = ""

        streamIdUser = if (isIncomingCall) {
            mReceiverStreamId
        } else {
            mCallerStreamId
        }
        conferenceManager = ConferenceManager(
            this,
            this,
            intent,
            SERVER_URL,
            mRoomId.toString(),
            publishViewRenderer,
            playViewRenderers,
//            mStreamId,
//            mReceiver_stream_id,
            streamIdUser,
            null
        )

        conferenceManager?.setPlayOnlyMode(false)
        conferenceManager?.setOpenFrontCamera(true)
    }

    private fun setListeners() {
        btnJoinConference?.setOnClickListener(this)
        imgCallEnd?.setOnClickListener(this)
        imgCamera?.setOnClickListener(this)
        imgVideo?.setOnClickListener(this)
        imgAudio?.setOnClickListener(this)
        imgArrowUp?.setOnClickListener(this)
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

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnJoinConference -> {
//                joinConference()
            }

            R.id.imgCallEnd -> {
                conferenceManager?.leaveFromConference()
//                webRTCClient!!.stopStream()
//                stoppedStream = true
                finish()
            }

            R.id.imgCamera -> {
//                webRTCClient!!.switchCamera()
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
        }
    }

    private fun joinConference() {
        if (conferenceManager?.isJoined == false) {
            Log.w(javaClass.simpleName, "Joining Conference")
            btnJoinConference?.text = "Leave"
            conferenceManager?.joinTheConference()
        } else {
            btnJoinConference?.text = "Join"
            conferenceManager?.leaveFromConference()
        }
    }

    private fun controlAudio() {
        if (conferenceManager!!.isPublisherAudioOn) {
            conferenceManager!!.disableAudio()
            imgAudio?.setImageResource(R.drawable.ic_audio_mute)
        } else {
            conferenceManager!!.enableAudio()
            imgAudio?.setImageResource(R.drawable.ic_audio)
        }
    }

    private fun controlVideo() {
        if (conferenceManager?.isPublisherVideoOn == true) {
            conferenceManager?.disableVideo()
            imgVideo?.setImageResource(R.drawable.ic_video_mute)
        } else {
            conferenceManager?.enableVideo()
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

    /*override fun onBufferedAmountChange(previousAmount: Long, dataChannelLabel: String?) {
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
    }*/
}