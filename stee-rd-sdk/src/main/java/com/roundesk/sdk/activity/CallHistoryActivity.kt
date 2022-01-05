package com.roundesk.sdk.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import com.roundesk.sdk.adapter.CallHistoryAdapter
import com.roundesk.sdk.dataclass.CallHistoryResponseDataClass
import com.roundesk.sdk.util.LogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallHistoryActivity : AppCompatActivity() {

    private val TAG = CallHistoryActivity::class.java.simpleName

    private var recyclerview: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_history)
        recyclerview = findViewById(R.id.recyclerview)
        progressBar = findViewById(R.id.progressBar)

        callAPI()

    }

    private fun callAPI() {
        val request = ServiceBuilder.buildService(ApiInterface::class.java)

        val call = request.getCallHistoryData(
            "eyJ0eXAiOiJLV1PiLOJhbK1iOiJSUzI1NiJ9",
            "drpbzfjiouhqkaegcvtl",
            "all"
        )

        call.enqueue(object : Callback<List<CallHistoryResponseDataClass?>> {
            override fun onResponse(
                call: Call<List<CallHistoryResponseDataClass?>>,
                response: Response<List<CallHistoryResponseDataClass?>>
            ) {
                if (response.isSuccessful) {
                    progressBar?.visibility = View.GONE
                    LogUtil.e(TAG, "onSuccess: ${Gson().toJson(response.body())}")
                    val callHistoryResponseDataClass: List<CallHistoryResponseDataClass?> =
                        response.body()!!

                    recyclerview?.layoutManager = LinearLayoutManager(this@CallHistoryActivity)
                    val adapter = CallHistoryAdapter(this@CallHistoryActivity,callHistoryResponseDataClass)

                    // Setting the Adapter with the recyclerview
                    recyclerview?.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<CallHistoryResponseDataClass?>>, t: Throwable) {
                Log.e(TAG, "onFailure : ${t.message}")
            }
        })
    }
}