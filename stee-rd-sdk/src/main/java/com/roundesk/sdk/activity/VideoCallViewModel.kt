package com.roundesk.sdk.activity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.roundesk.sdk.dataclass.MuteAudioRequestData
import com.roundesk.sdk.dataclass.MuteVideoRequestData
import com.roundesk.sdk.network.ApiInterface
import com.roundesk.sdk.network.ServiceBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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


    val muteVideoList = LinkedHashMap<String, Boolean>()
    private val _muteVideoListState = MutableStateFlow(false)
    val muteVideoListState = _muteVideoListState.asStateFlow()

     suspend  fun setVideoWidth(width : Int){
        _videoViewWidth.value = width/2
    }

    fun muteVideo(data : MuteVideoRequestData){
        viewModelScope.launch(Dispatchers.IO){
            _muteVideoState.value = MuteVideoViewState.Loading
            val api = ServiceBuilder.buildService(ApiInterface::class.java)
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


    fun muteVideoListState(name : String, isVideoMuted : Boolean){
        muteVideoList.put(name, isVideoMuted)
        _muteVideoListState.update{ oldValue ->
            !oldValue
        }
    }

    fun muteAudio(data : MuteAudioRequestData){
        viewModelScope.launch(Dispatchers.IO){
            _muteAudioState.value = MuteAudioViewState.Loading
            val api = ServiceBuilder.buildService(ApiInterface::class.java)
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

}