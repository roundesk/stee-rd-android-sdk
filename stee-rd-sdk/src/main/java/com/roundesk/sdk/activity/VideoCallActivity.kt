package com.roundesk.sdk.activity

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.roundesk.sdk.R
import de.tavendo.autobahn.WebSocket
import io.antmedia.webrtcandroidframework.*
import io.antmedia.webrtcandroidframework.apprtc.CallActivity
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import java.util.ArrayList

class VideoCallActivity : AppCompatActivity(), IWebRTCListener {

    val SERVER_ADDRESS: String = "stee-dev.roundesk.io:5443"
    private val webRTCMode = IWebRTCClient.MODE_PUBLISH
    val SERVER_URL = "ws://" + SERVER_ADDRESS + "/WebRTCAppEE/websocket"
//    val REST_URL = "https://" + SERVER_ADDRESS + "/WebRTCAppEE/rest/v2"

//    val REST_URL = "https://$SERVER_ADDRESS/LiveApp/conference.html"

    private var cameraViewRenderer: SurfaceViewRenderer? = null
    private var pipViewRenderer: SurfaceViewRenderer? = null
    private var view: View? = null
    private var imgCallEnd: ImageView? = null
    private var imgCamera: ImageView? = null
    private var webRTCClient: WebRTCClient? = null

    //    private val enableDataChannel = true
    private var startStreamingButton: Button? = null
    private var isCameraBack: Boolean? = false
    private var operationName = ""
    private var streamId: String? = null
    private var stoppedStream = false
    val RECONNECTION_PERIOD_MLS = 100

    var reconnectionHandler = Handler()
    var reconnectionRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!webRTCClient!!.isStreaming) {
                attempt2Reconnect()
                // call the handler again in case startStreaming is not successful
                reconnectionHandler.postDelayed(this, RECONNECTION_PERIOD_MLS.toLong())
            }
        }
    }

    private var conferenceManager: ConferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(R.layout.activity_video_call)

        cameraViewRenderer = findViewById(R.id.camera_view_renderer)
        pipViewRenderer = findViewById(R.id.pip_view_renderer)
        startStreamingButton = findViewById(R.id.start_streaming_button)
        imgCallEnd = findViewById(R.id.imgCallEnd)
        imgCamera = findViewById(R.id.imgCamera)
        view = findViewById(R.id.view)
        startStreamingButton?.performClick();
        startStreamingButton?.setOnClickListener {
            startStreaming(startStreamingButton)
        }
        imgCallEnd?.setOnClickListener {
            finish()
        }

        imgCamera?.setOnClickListener {

        }




//        streamInfoListSpinner = findViewById<Spinner>(R.id.stream_info_list)

        /*if (webRTCMode != IWebRTCClient.MODE_PLAY) {
            streamInfoListSpinner.setVisibility(View.INVISIBLE)
        } else {
            streamInfoListSpinner.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                var firstCall = true
                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    //for some reason in android onItemSelected is called automatically at first.
                    //there are some discussions about it in stackoverflow
                    //so we just have simple check
                    if (firstCall) {
                        firstCall = false
                        return
                    }
                    webRTCClient.forceStreamQuality(adapterView.selectedItem as String?. toInt ())
                    Log.i("MainActivity", "Spinner onItemSelected")
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            })
        }*/

        // Check for mandatory permissions.

        // Check for mandatory permissions.
        for (permission: String in CallActivity.MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission $permission is not granted", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }

        if ((webRTCMode == IWebRTCClient.MODE_PUBLISH)) {
            startStreamingButton?.setText("Start Publishing")
            operationName = "Publishing"
        } else if ((webRTCMode == IWebRTCClient.MODE_PLAY)) {
            startStreamingButton?.setText("Start Playing")
            operationName = "Playing"
        } else if ((webRTCMode == IWebRTCClient.MODE_JOIN)) {
            startStreamingButton?.setText("Start P2P")
            operationName = "P2P"
        }

        this.intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true)
        this.intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30)
        this.intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 1500)
        this.intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true)
//        this.intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, enableDataChannel)

        webRTCClient = WebRTCClient(this, this)

        //webRTCClient.setOpenFrontCamera(false);


        webRTCClient?.setOpenFrontCamera(true);
        streamId = "43584295"
        val roomId = "67610329"
        val tokenId = "tokenId"
        webRTCClient?.setVideoRenderers(pipViewRenderer, cameraViewRenderer)

        // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);

        // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);
        webRTCClient?.init(SERVER_URL, roomId, webRTCMode, tokenId, this.intent)
//        webRTCClient?.setDataChannelObserver(this)

        //          this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_CALL, false);
//        val streamId: String? = null //"stream1";

        conferenceManager = ConferenceManager(
            this,
            this,
            intent,
            SERVER_URL,
            roomId,
            cameraViewRenderer,
            null,
            roomId,
            null
        )

        conferenceManager?.setPlayOnlyMode(false)
        conferenceManager?.setOpenFrontCamera(true)

    }

    fun startStreaming(v: View?) {
        if (!webRTCClient!!.isStreaming) {
            (v as Button).text = "Stop $operationName"
            webRTCClient!!.startStream()
            joinConference(startStreamingButton)
            if (webRTCMode === IWebRTCClient.MODE_JOIN) {
                pipViewRenderer!!.setZOrderOnTop(true)
            }
        } else {
            (v as Button).text = "Start $operationName"
            webRTCClient!!.stopStream()
            webRTCClient!!.startStream()
            stoppedStream = true
        }
    }

    private fun attempt2Reconnect() {
        Log.w(javaClass.simpleName, "Attempt2Reconnect called")
        if (!webRTCClient!!.isStreaming) {
            webRTCClient!!.startStream()
            if (webRTCMode === IWebRTCClient.MODE_JOIN) {
                pipViewRenderer!!.setZOrderOnTop(true)
            }
        }
    }

    override fun onPlayStarted(streamId: String?) {
        fun onPlayStarted(streamId: String?) {
            Log.w(javaClass.simpleName, "onPlayStarted")
            Toast.makeText(this, "Play started", Toast.LENGTH_LONG).show()
            webRTCClient!!.switchVideoScaling(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
            webRTCClient!!.getStreamInfoList()
        }
    }


    override fun onPublishFinished(streamId: String?) {
        Log.w(javaClass.simpleName, "onPublishFinished")
        Toast.makeText(this, "Publish finished", Toast.LENGTH_LONG).show()
    }

    override fun onPublishStarted(streamId: String?) {
        Log.w(javaClass.simpleName, "onPublishStarted")
        Toast.makeText(this, "Publish started", Toast.LENGTH_LONG).show()
    }

    override fun onPlayFinished(streamId: String?) {
        Log.w(javaClass.simpleName, "onPlayFinished")
        Toast.makeText(this, "Play finished", Toast.LENGTH_LONG).show()
    }

    override fun noStreamExistsToPlay(streamId: String?) {
        Log.w(javaClass.simpleName, "noStreamExistsToPlay")
        Toast.makeText(this, "No stream exist to play", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun streamIdInUse(streamId: String?) {
        Log.w(javaClass.simpleName, "streamIdInUse")
        Toast.makeText(this, "Stream id is already in use.", Toast.LENGTH_LONG).show()
    }

    override fun onIceConnected(streamId: String?) {
        //it is called when connected to ice

        //it is called when connected to ice
        startStreamingButton!!.text = "Stop $operationName"
        // remove scheduled reconnection attempts
        // remove scheduled reconnection attempts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (reconnectionHandler.hasCallbacks(reconnectionRunnable)) {
                reconnectionHandler.removeCallbacks(reconnectionRunnable, null)
            }
        } else {
            reconnectionHandler.removeCallbacks(reconnectionRunnable, null)
        }
    }

    override fun onIceDisconnected(streamId: String?) {
        //it's called when ice is disconnected
    }

    override fun onTrackList(tracks: Array<out String>?) {
    }

    override fun onBitrateMeasurement(
        streamId: String?,
        targetBitrate: Int,
        videoBitrate: Int,
        audioBitrate: Int
    ) {
        Log.e(
            javaClass.simpleName,
            "st:$streamId tb:$targetBitrate vb:$videoBitrate ab:$audioBitrate"
        )
        if (targetBitrate < videoBitrate + audioBitrate) {
            Toast.makeText(this, "low bandwidth", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStreamInfoList(streamId: String?, streamInfoList: ArrayList<StreamInfo>?) {
    }

    override fun onError(description: String?, streamId: String?) {
        Toast.makeText(this, "Error: $description", Toast.LENGTH_LONG).show()
    }

    override fun onSignalChannelClosed(
        code: WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification?,
        streamId: String?
    ) {
        Toast.makeText(this, "Signal channel closed with code $code", Toast.LENGTH_LONG).show()

    }

    override fun onDisconnected(streamId: String?) {
        Log.w(javaClass.simpleName, "disconnected")
        Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show()

        startStreamingButton!!.text = "Start $operationName"
        // handle reconnection attempt
        // handle reconnection attempt
        if (!stoppedStream) {
            Toast.makeText(this, "Disconnected Attempting to reconnect", Toast.LENGTH_LONG).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!reconnectionHandler.hasCallbacks(reconnectionRunnable)) {
                    reconnectionHandler.postDelayed(
                        reconnectionRunnable,
                        RECONNECTION_PERIOD_MLS.toLong()
                    )
                }
            } else {
                reconnectionHandler.postDelayed(
                    reconnectionRunnable,
                    RECONNECTION_PERIOD_MLS.toLong()
                )
            }
        } else {
            Toast.makeText(this, "Stopped the stream", Toast.LENGTH_LONG).show()
            stoppedStream = false
        }
    }

    override fun onStop() {
        super.onStop()
        webRTCClient!!.stopStream()
    }

    fun joinConference(v: View?) {
        if (conferenceManager?.isJoined() ==  false) {
            Log.w(javaClass.simpleName, "Joining Conference")
            (v as Button).text = "Leave"
//            view?.visibility = View.GONE
            conferenceManager?.joinTheConference()
        } else {
            (v as Button).text = "Join"
//            view?.visibility = View.VISIBLE
            conferenceManager?.leaveFromConference()
        }
    }
}