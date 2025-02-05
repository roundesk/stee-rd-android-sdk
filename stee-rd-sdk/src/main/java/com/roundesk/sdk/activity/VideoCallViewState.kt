package com.roundesk.sdk.activity


sealed class MuteVideoViewState{
    object Initial : MuteVideoViewState()
    object Loading : MuteVideoViewState()
    object Success : MuteVideoViewState()
    data class Error(val msg : String) : MuteVideoViewState()
}

sealed class MuteAudioViewState{
    object Initial : MuteAudioViewState()
    object Loading : MuteAudioViewState()
    object Success : MuteAudioViewState()
    data class Error(val msg : String) : MuteAudioViewState()
}