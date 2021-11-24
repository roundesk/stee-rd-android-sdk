package com.roundesk.sdk.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.R

class OutgoingCallActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = OutgoingCallActivity::class.java.simpleName

    private var imgCallEnd: ImageView? = null
    private var imgBack: ImageView? = null
    private var mSocket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_call)

        /*  //Socket instance
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

        mSocket?.on(Constants.SocketSuffix.SOCKET_SEND_CALL_TO_CLIENT, onCreateCallEmitter)
*/

        initView()
    }

    private fun initView() {
        imgCallEnd = findViewById(R.id.imgCallEnd)
        imgBack = findViewById(R.id.imgBack)

        imgCallEnd?.setOnClickListener(this)
        imgBack?.setOnClickListener(this)
    }


    /*  private val onCreateCallEmitter = Emitter.Listener { args ->
          val response = "" + args[0]
          LogUtil.e(TAG, "socket response : $response")
          *//*val acceptCallSocketDataClass: AcceptCallSocketDataClass =
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
            }*//*
    }*/

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgCallEnd -> {
                finish()
//                val intent = Intent(this, VideoCallActivity::class.java)
//                startActivity(intent)
            }
            R.id.imgBack -> {
                finish()
            }

        }
    }
}