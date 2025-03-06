package com.roundesk.sdk.socket

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object VideoMuteListenerHelper {

    val muteVideoList = LinkedHashMap<String, Boolean>()

    private val _muteVideoListState = MutableStateFlow(false)
    val muteVideoListState = _muteVideoListState.asStateFlow()

    fun muteVideoListState(name : String, isVideoMuted : Boolean){
        muteVideoList.put(name, isVideoMuted)
        _muteVideoListState.update{ oldValue ->
            !oldValue
        }
    }
}