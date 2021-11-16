package com.roundesk.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.activity.ApiFunctions
import com.roundesk.sdk.socket.SocketFunctions
import java.util.ArrayList

class ChatActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = ChatActivity::class.java.simpleName

    private var imgVideo: ImageView? = null
    private var txtCallerName: TextView? = null
    private var relLayTopNotification: RelativeLayout? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null
    private var mSocket: Socket? = null
    var arraylistReceiverId: ArrayList<String> = arrayListOf()
    private var callerId: String = "drpbzfjiouhqkaegcvtl"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Below line will initiate the Socket and make it open
        SocketFunctions(this).initiateSocket(
            mSocket,
            SocketConstants.SOCKET_SEND_CALL_TO_CLIENT + callerId,false
        )

        initView()
    }

    private fun initView() {
        imgVideo = findViewById(R.id.imgVideo)
        relLayTopNotification = findViewById(R.id.relLayTopNotification)
        txtCallerName = findViewById(R.id.txtCallerName)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)

        imgVideo?.setOnClickListener(this)
        btnAccept?.setOnClickListener(this)
        btnDecline?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgVideo -> {
                arraylistReceiverId.clear()
                arraylistReceiverId.add("agjqticlbhvredpouzkf")
                arraylistReceiverId.add("drpbzfjiouhqkaegcvtl")

                // Below line will initiate the call
                ApiFunctions(this).initiateCall(
                    arraylistReceiverId,"doctor" ,"drpbzfjiouhqkaegcvtl","on","on","a3dt3ffdd"
                )
            }

            R.id.btnAccept -> {
                relLayTopNotification?.visibility = View.GONE
            }
            R.id.btnDecline -> {
                relLayTopNotification?.visibility = View.GONE
            }
        }
    }
}