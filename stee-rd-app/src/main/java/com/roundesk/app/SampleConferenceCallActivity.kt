package com.roundesk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

private var usersJoined: Int = 3

class SampleConferenceCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_conference_call)
    }
}