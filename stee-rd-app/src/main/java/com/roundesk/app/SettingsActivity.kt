package com.roundesk.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.activity.IncomingCallActivity
import com.roundesk.sdk.socket.SocketFunctions

class SettingsActivity : AppCompatActivity() {
    private var txtStartWithChat: TextView? = null
    private var txtStartWithAudio: TextView? = null
    private var mSocket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        txtStartWithChat = findViewById(R.id.txtStartWithChat)
        txtStartWithAudio = findViewById(R.id.txtStartWithAudio)

        txtStartWithChat?.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

    }
}