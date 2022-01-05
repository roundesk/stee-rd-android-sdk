package com.roundesk.sdk.util

import android.content.Context
import android.widget.Toast


object ToastUtil {

    fun displayShortDurationToast(context: Context, strToast: String): Void? {
        Toast.makeText(context, strToast, Toast.LENGTH_SHORT).show()
        return null
    }

    fun displayLongDurationToast(context: Context, strToast: String): Void? {
        Toast.makeText(context, strToast, Toast.LENGTH_LONG).show()
        return null
    }
}
