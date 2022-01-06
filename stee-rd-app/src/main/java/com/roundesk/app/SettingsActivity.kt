package com.roundesk.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.roundesk.sdk.activity.ApiFunctions
import java.util.*


class SettingsActivity : AppCompatActivity() {
    private var txtStartWithChat: TextView? = null
    private var txtStartWithAudio: TextView? = null
    private var txtCallHistory: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        txtStartWithChat = findViewById(R.id.txtStartWithChat)
        txtStartWithAudio = findViewById(R.id.txtStartWithAudio)
        txtCallHistory = findViewById(R.id.txtCallHistory)

        txtStartWithChat?.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        txtCallHistory?.setOnClickListener {
            ApiFunctions(this).navigateToCallHistory()
        }
    }
}