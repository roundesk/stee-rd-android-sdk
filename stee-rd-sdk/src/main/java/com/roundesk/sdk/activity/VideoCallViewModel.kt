package com.roundesk.sdk.activity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roundesk.sdk.dataclass.MuteAudioRequestData
import com.roundesk.sdk.dataclass.MuteVideoRequestData
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoCallViewModel : ViewModel() {

    private val _videoViewWidth = MutableStateFlow<Int>(0)
    val videoViewWidth : StateFlow<Int> = _videoViewWidth.asStateFlow()

    private val _muteVideoState = MutableStateFlow<MuteVideoViewState>(MuteVideoViewState.Initial)
    val muteVideoState : StateFlow<MuteVideoViewState> = _muteVideoState.asStateFlow()

    private val _muteAudioState = MutableStateFlow<MuteAudioViewState>(MuteAudioViewState.Initial)
    val muteAudioState : StateFlow<MuteAudioViewState> = _muteAudioState.asStateFlow()

    private val api = ServiceBuilder.buildService(ApiInterface::class.java)

    fun muteVideo(data : MuteVideoRequestData){
        viewModelScope.launch(Dispatchers.IO){
            _muteVideoState.value = MuteVideoViewState.Loading
            val call = api.muteVideo(data)
            Log.d("muteVideo success", data.toString())

            call.enqueue(object : Callback<Any>{
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful){
                        _muteVideoState.value = MuteVideoViewState.Success
                    }else{
                        _muteVideoState.value = MuteVideoViewState.Error(response.message())
                    }
                    Log.d("muteVideo success", response.toString())

                    Log.d("muteVideo success", response.body().toString())
                }
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    _muteVideoState.value = MuteVideoViewState.Error(t.message ?: "Something went wrong")
                    Log.d("muteVideo error", t.message.toString())
                    Log.d("muteVideo error", t.cause.toString())
                }
            })
        }
    }




    fun muteAudio(data : MuteAudioRequestData){
        viewModelScope.launch(Dispatchers.IO){
            _muteAudioState.value = MuteAudioViewState.Loading
            val call = api.muteAudio(data)
            call.enqueue(object : Callback<Any>{
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    Log.d("muteAudio success", response.toString())
                    Log.d("muteAudio success", response.body().toString())
                    if (response.isSuccessful){
                        _muteAudioState.value = MuteAudioViewState.Success
                    }else{
                        _muteAudioState.value = MuteAudioViewState.Error(response.message())
                    }
                }
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.d("muteAudio error", t.message.toString())
                    Log.d("muteAudio error", t.cause.toString())
                    _muteAudioState.value = MuteAudioViewState.Error(t.message?: "Something went wrong")

                }
            })
        }
    }


    fun updateOnOrientationChange( meetingId : Int, meetingUUID : String,  dimension : String ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.updateOnOrientationChange(
                    meetingId = meetingId,
                    callerId = meetingUUID,
                    dimension = dimension
                )
                if (response.isSuccessful) {
                    Log.d("Orientation api suc", response.toString())
                    Log.d("Orientation api suc", response.body().toString())
                } else {
                    Log.d("Orientation api error", response.body().toString())
                }
            } catch (e: Exception) {
                Log.d("Orientation api errr", "${e.message}")
            }
            Log.d("Orientation api", "end")

        }
    }
}