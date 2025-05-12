package com.roundesk.sdk.socket

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SocketListenerHelper {

    val muteVideoList = LinkedHashMap<String, Boolean>()

    private val _muteVideoListState = MutableStateFlow(false)
    val muteVideoListState = _muteVideoListState.asStateFlow()

    fun muteVideoListState(name : String, isVideoMuted : Boolean){
        muteVideoList.put(name, isVideoMuted)
        _muteVideoListState.update{ oldValue ->
            !oldValue
        }
    }

    val orientationList = LinkedHashMap<String, OrientationState >()
    private val _orientationListState = MutableStateFlow(false)
    val orientationListState = _orientationListState.asStateFlow()

    fun updateOrientationList(streamId : String, orientation: String){
        Log.d("getOrientationDetails", "updateOrientationList $streamId $orientation")

        val state =  if(orientation == "landscape") OrientationState.Landscape else OrientationState.Portrait
        orientationList.put(streamId, state)
        _orientationListState.update { oldValue ->
            !oldValue
        }
    }
}

sealed class OrientationState {
    object Landscape : OrientationState()
    object Portrait : OrientationState()
}