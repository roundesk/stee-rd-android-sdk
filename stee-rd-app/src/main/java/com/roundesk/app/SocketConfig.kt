package com.roundesk.app

//import com.roundesk.sdk.socket.SocketConnection
import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.util.URLConfigurationUtil
import java.io.File
import java.io.IOException
import java.net.URISyntaxException


class SocketConfig : Application() {

    private var mSocket: Socket? = null

    companion object {
        private var mInstance: SocketConfig? = null
        private var mContext: Context? = null

        @Synchronized
        fun getInstance(): SocketConfig? {
            return mInstance
        }
    }

    fun getAppContext(): Context? {
        return mContext
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        mContext = applicationContext;
        storeDataLogsFile()
        try {
            //creating socket instance
            mSocket = IO.socket(URLConfigurationUtil.getSocketURL())
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    //return socket instance
    fun getMSocket(): Socket? {
        return mSocket
    }

    private fun storeDataLogsFile() {
        if (isExternalStorageWritable()) {
//            val appDirectory = File(Environment.getExternalStorageDirectory().toString() + "/STEE_APP_DATA_LOGS")
            val cDir: File? = getAppContext()?.getExternalFilesDir(null);
            val appDirectory = File(cDir?.path + "/" + "STEE_APP_DATA_LOGS")
            val logDirectory = File("$appDirectory/logs")
            val logFile = File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt")
            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir()
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir()
            }

            // clear the previous logcat and then write the new one to the file
            try {
//                Process process = Runtime.getRuntime().exec("logcat -c");
                val process = Runtime.getRuntime().exec("logcat -f $logFile")

                Log.e("SocketConfig", "File Path $process");

            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (isExternalStorageReadable()) {
            // only readable
        } else {
            // not accessible
        }
    }

    /* Checks if external storage is available for read and write */
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    /* Checks if external storage is available to at least read */
    private fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    /*override fun onActivityCreated(activity: Activity, p1: Bundle?) {
//        LogUtil.e("isActivityChangingConfigurations", "onActivityCreated")
        socketConnection = SocketConnection()
        socketConnection!!.connectSocket()
    }

    override fun onActivityStarted(activity: Activity) {
//        LogUtil.e("isActivityChangingConfigurations", "onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
//        LogUtil.e("isActivityChangingConfigurations", "onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
//        LogUtil.e("isActivityChangingConfigurations", "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
//        LogUtil.e("isActivityChangingConfigurations", "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
//        LogUtil.e("isActivityChangingConfigurations", "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
//        LogUtil.e("isActivityChangingConfigurations", "onActivityDestroyed")
    }*/
}