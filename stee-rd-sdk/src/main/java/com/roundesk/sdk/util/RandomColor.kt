package com.roundesk.sdk.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.roundesk.sdk.R
import java.util.Random


fun getRandomColor(context: Context) : Drawable{
    val random = Random()
    val hue = random.nextInt(360).toFloat()
    val saturation = 0.6f + random.nextFloat() * 0.4f
    val brightness = 0.3f + random.nextFloat() * 0.4f
    val color =  Color.HSVToColor(floatArrayOf(hue, saturation, brightness))

    val drawable = ContextCompat.getDrawable(context, R.drawable.mute_text_circle_bg) as GradientDrawable
    drawable.setColor(color)
    return drawable
}