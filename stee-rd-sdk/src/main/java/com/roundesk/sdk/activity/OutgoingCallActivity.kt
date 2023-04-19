package com.roundesk.sdk.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.roundesk.sdk.R
import com.roundesk.sdk.base.AppBaseActivity

class OutgoingCallActivity : AppBaseActivity(), View.OnClickListener {

    private val TAG = OutgoingCallActivity::class.java.simpleName

    private var imgCallEnd: ImageView? = null
    private var imgBack: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_call)
        initView()
    }

    private fun initView() {
        imgCallEnd = findViewById(R.id.imgCallEnd)
        imgBack = findViewById(R.id.imgBack)

        imgCallEnd?.setOnClickListener(this)
        imgBack?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgCallEnd -> {
                finish()
            }
            R.id.imgBack -> {
                finish()
            }
        }
    }
}