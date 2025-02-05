package com.roundesk.sdk.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.PixelCopy
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.hardware.display.DisplayManagerCompat
import org.webrtc.SurfaceViewRenderer
import java.io.OutputStream


class Screenshot(context: Context) {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var density: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private lateinit var display: Display
    private var metrics: DisplayMetrics
    var isProcessing = false
    init {
        mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            val defaultDisplay = DisplayManagerCompat.getInstance(context).getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = context.createDisplayContext(defaultDisplay!!)
            metrics = displayContext.resources.displayMetrics
        }else{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            display = windowManager.defaultDisplay
            metrics = DisplayMetrics()
            display.getRealMetrics(metrics)
        }
        density = metrics.densityDpi
        width = metrics.widthPixels
        height = metrics.heightPixels
    }


    fun captureScreen(activity: Activity, view: SurfaceViewRenderer) {
        val window = activity.window

        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )

//        PixelCopy.request(view, bitmap, { copyResult ->
//            if (copyResult === PixelCopy.SUCCESS) {
//                // Screenshot successfully captured
//                saveImageToFolder(activity,bitmap) // Save the screenshot or do further processing
//            } else {
//                Log.e("Screenshot", "PixelCopy failed: $copyResult")
//            }
//        }, Handler(Looper.getMainLooper()))
    }

    fun screenShotBitmap(view: View, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    @SuppressLint("WrongConstant")
     fun captureScreenShot(
        resultCode: Int,
        resultIntent: Intent?,
        context: Context,
        screenShotResultCallback: (message: String) -> Unit
    ) {
        if (resultCode != Activity.RESULT_OK || resultIntent == null || !::mediaProjectionManager.isInitialized) {
            Log.d("ScreenshotResult", "result intent null or result code mismatch")
            screenShotResultCallback("Screenshot capture failed, result is null")
            return
        }
        Log.d("ScreenshotResult", "fun")
        val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultIntent)
        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
        val virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader.surface,
            null,
            Handler(Looper.getMainLooper())
        )
        imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {

            override fun onImageAvailable(p0: ImageReader?) {
                if (isProcessing) return
                isProcessing = true
                Log.d("ScreenshotResult", "inside imageReader")
                try {
                    val image = p0?.acquireLatestImage()
                    if (image != null) {
                        val plane = image.planes[0]
                        val buffer = plane.buffer
                        val pixelStride = plane.pixelStride
                        val rowStride = plane.rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bitmap = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)
                        image.close()
                        Log.d("ScreenshotResult", "save to fol")

                        saveImageToFolder(context, bitmap)
                        screenShotResultCallback("success")
                    }
                } catch (e: Exception) {
                    Log.d("ScreenshotResult", "failed : ${e}")
                    Log.d("ScreenshotResult", "failed : ${e.message}")

                    screenShotResultCallback("failed")
                }finally {
                    Log.d("ScreenshotResult", "closing")

                    imageReader.close()
                    virtualDisplay.release()
                    mediaProjection.stop()
                    isProcessing = false
                    Log.d("ScreenshotResult", "closed")

                }
            }


        }, Handler(Looper.getMainLooper()))
    }


    fun saveImageToFolder(context: Context, bitmap: Bitmap): String {
        val fileName = "Stee_ScreenShot${System.currentTimeMillis()}.jpg"
        val outputStream: OutputStream?
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
            }
            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (imageUri != null) {
                outputStream = context.contentResolver.openOutputStream(imageUri)
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                outputStream?.close()
                return "Success ${imageUri}"
            }
        } catch (e: Exception) {
            return "Failed : ${e.message}"
        }
        return "Failed saving ScreenShot"
    }
}