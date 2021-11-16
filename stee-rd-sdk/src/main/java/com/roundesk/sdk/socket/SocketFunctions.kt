package com.roundesk.sdk.socket

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import com.roundesk.sdk.activity.IncomingCallActivity
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.dataclass.CreateCallSocketDataClass
import com.roundesk_stee_sdk.util.LogUtil

class SocketFunctions(private var mContext: Activity?) {

    private var mSocket: Socket? = null
    private var isIncomingCall: Boolean = false

    fun initiateSocket(socket: Socket?, socketId: String, showIncomingScreen:Boolean) {
        mSocket = socket
        isIncomingCall = showIncomingScreen
        val app: SocketInstance = mContext as SocketInstance
        mSocket = app.getMSocket()
        //connecting socket
        mSocket?.connect()
        val options = IO.Options()
        options.reconnection = true //reconnection
        options.forceNew = true

        if (mSocket?.connected() == true) {
            Toast.makeText(mContext, "Socket is connected", Toast.LENGTH_SHORT).show()
        }

        mSocket?.on(socketId, onCreateCallEmitter)
    }

    private val onCreateCallEmitter = Emitter.Listener { args ->
        val response = "" + args[0]
        LogUtil.e("onCreateCallEmitter", "socket response : $response")
        val createCallSocketDataClass: CreateCallSocketDataClass =
            Gson().fromJson(response, CreateCallSocketDataClass::class.java)

        mContext?.runOnUiThread {
            if (createCallSocketDataClass.type == "new") {
                if (isIncomingCall) {
                    val intent = Intent(mContext, IncomingCallActivity::class.java)
                    intent.putExtra("room_id", createCallSocketDataClass.room_id)
                    intent.putExtra("meeting_id", createCallSocketDataClass.meetingId)
                    intent.putExtra("receiver_name", createCallSocketDataClass.receiver_name)
                    mContext?.startActivity(intent)
                }
            }
        }
    }
}