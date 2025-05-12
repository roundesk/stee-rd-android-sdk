package com.roundesk.sdk.util

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback


fun ComponentActivity.onBackButtonPressed(work :  () -> Unit){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                work()
            }
        })
    }
}