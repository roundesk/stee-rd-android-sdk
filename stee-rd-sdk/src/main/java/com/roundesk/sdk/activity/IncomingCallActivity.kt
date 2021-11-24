package com.roundesk.sdk.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk_stee_sdk.util.LogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.dataclass.AcceptCallDataClassResponse
import com.roundesk.sdk.dataclass.AcceptCallRequest
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.socket.SocketInstance
import com.roundesk.sdk.util.Constants


class IncomingCallActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = IncomingCallActivity::class.java.simpleName
    private var mSocket: Socket? = null
    private var imgCallEnd: ImageView? = null
    private var imgCallAccept: ImageView? = null
    private var imgBack: ImageView? = null
    private var txtDoctorName: TextView? = null
    private var room_id: Int = 0
    private var meeting_id: Int = 0
    private var receiver_name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        val app: SocketInstance = application as SocketInstance
        mSocket = app.getMSocket()
        //connecting socket
        mSocket?.connect()
        val options = IO.Options()
        options.reconnection = true //reconnection
        options.forceNew = true

        if (mSocket?.connected() == true) {
            Toast.makeText(this, "Socket is connected", Toast.LENGTH_SHORT).show()
        }

        mSocket?.on(Constants.SocketSuffix.SOCKET_ACCEPT_CALL, onCreateCallEmitter)

        val extras = intent.extras
        if (extras != null) {
            room_id = extras.getInt("room_id")
            meeting_id = extras.getInt("meeting_id")
            receiver_name = extras.getString("receiver_name")
            //The key argument here must match that used in the other activity
        }
        initView()

        imgCallEnd?.setOnClickListener {
            /*val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)*/
            finish()
        }
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

                val user = AcceptCallRequest(
                    "agjqticlbhvredpouzkf",
                    "on",
                    "on",
                    "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9",
                    meeting_id,
                    room_id
                )
                val json = Gson().toJson(user)
                LogUtil.e(TAG, "json : $json")

                val request = ServiceBuilder.buildService(ApiInterface::class.java)
                val call = request.getAcceptCallSocketData(user)

                call.enqueue(object : Callback<AcceptCallDataClassResponse?> {
                    override fun onResponse(
                        call: Call<AcceptCallDataClassResponse?>,
                        response: Response<AcceptCallDataClassResponse?>
                    ) {
                        LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")
                        if (response.isSuccessful) {
                            LogUtil.e(TAG, "onSuccess: $response")
                            val intent = Intent(this@IncomingCallActivity, VideoCallActivity::class.java)
                            intent.putExtra("activity","Incoming")
                            intent.putExtra("room_id",response.body()?.roomId)
                            intent.putExtra("meeting_id",response.body()?.meetingId)
                            intent.putExtra("stream_id",response.body()?.caller_streamId)
                            intent.putExtra("caller_streamId",response.body()?.caller_streamId)
                            startActivity(intent)
                        }
                    }

                    override fun onFailure(call: Call<AcceptCallDataClassResponse?>, t: Throwable) {
                        Log.e(TAG, "onFailure : ${t.message}")
                    }
                })
                finish()

            }
            R.id.imgCallEnd -> {
                finish()
            }
            R.id.imgBack -> {
                finish()
            }
        }
    }

    private val onCreateCallEmitter = Emitter.Listener { args ->
        val response = "" + args[0]
        LogUtil.e(TAG, "accept call socket response : $response")
/*        val acceptCallSocketDataClass: AcceptCallSocketDataClass =
            Gson().fromJson(response, CreateCallSocketDataClass::class.java)

        runOnUiThread {
//            if(createCallSocketDataClass.receiverId == "drpbzfjiouhqkaegcvtl"){
            if(createCallSocketDataClass.type == "new"){
                val intent = Intent(this@ChatActivity, IncomingCallActivity::class.java)
                intent.putExtra("room_id",createCallSocketDataClass.room_id)
                intent.putExtra("meeting_id",createCallSocketDataClass.meetingId)
                intent.putExtra("receiver_name",createCallSocketDataClass.receiver_name)
                startActivity(intent)
            }

            if(createCallSocketDataClass.type == "accepted"){
                val intent = Intent(this@ChatActivity, MainActivity::class.java)
                startActivity(intent)
            }*/
    }
}