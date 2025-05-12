package com.roundesk.sdk.util

import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.RelativeLayout



 fun fullSizeLayoutRelativeLayoutParams () : RelativeLayout.LayoutParams{
    return RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        marginEnd = 0
        topMargin = 0

    }
}

fun fullSizeMuteViewLayoutParams() : RelativeLayout.LayoutParams{
    return RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
    ).apply {
        marginEnd = 0
        topMargin = 0

    }
}

 fun smallAndFullViewFrameLayoutLayoutParams(fullScreen: Boolean) : FrameLayout.LayoutParams {

    return  if (fullScreen){
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = 0
            topMargin = 0
            gravity = Gravity.CENTER
        }
    }else{
        FrameLayout.LayoutParams(
            432,510
        ).apply {
            marginEnd = 40
            topMargin = 40
            gravity = Gravity.END
        }
    }
}

fun remoteVideoLayoutParamsOnOrientation(fullScreen: Boolean, orientation : Int) : FrameLayout.LayoutParams{
    when {
        fullScreen && orientation == Configuration.ORIENTATION_PORTRAIT -> {
            return FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = 0
                topMargin = 0
                gravity = Gravity.CENTER
            }
        }

        fullScreen && orientation == Configuration.ORIENTATION_LANDSCAPE -> {
            val displayMetrics = Resources.getSystem().displayMetrics
            val width = displayMetrics.widthPixels
            val h = width
            val w = (width * 9) / 16
            Log.d("getDisplayMetrics" , " $w -------  $h")
            Log.d("getDisplayMetrics" , " ${displayMetrics.widthPixels} -------  ${displayMetrics.heightPixels}")
            return FrameLayout.LayoutParams(
                w, h
            ).apply {
                marginEnd = 0
                topMargin = 0
                gravity = Gravity.CENTER
            }
        }

        else -> {
            return FrameLayout.LayoutParams(
                432, 510
            ).apply {
                marginEnd = 40
                topMargin = 40
                gravity = Gravity.END
            }

        }
    }
}


fun onPiPModeSmallViewLayoutParams()  : FrameLayout.LayoutParams{
   return FrameLayout.LayoutParams(
        165,210
    ).apply {
        marginEnd = 10
        topMargin = 10
        gravity = Gravity.END
    }
}


