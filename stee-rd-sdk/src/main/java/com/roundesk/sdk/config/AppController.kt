package com.roundesk.sdk.config

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.roundesk.sdk.socket.SocketConnection
import com.roundesk.sdk.util.LogUtil


class AppController : Application(), Application.ActivityLifecycleCallbacks {

//    private var sharedPreferenceManager: SharedPreferenceManager? = null
//    private var resourceUtil: ResourceUtil? = null
    private var socketConnection: SocketConnection? = null
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    companion object {
        private var mInstance: AppController? = null

        @Synchronized
        fun getInstance(): AppController? {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
//        resourceUtil = ResourceUtil()
//        sharedPreferenceManager = SharedPreferenceManager(this)

        registerActivityLifecycleCallbacks(this)
    }

   /* fun getResourceUtil(): ResourceUtil? {
        return resourceUtil
    }

    fun getSharedPreferenceUtil(): SharedPreferenceManager? {
        return sharedPreferenceManager
    }*/

    fun getSocketInstance(): SocketConnection? {
        return socketConnection
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityCreated")
//        if (socketConnection == null) {
            socketConnection = SocketConnection()
            socketConnection!!.connectSocket()
//        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
        }
        LogUtil.e("isActivityChangingConfigurations", "onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations;
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
        }
        LogUtil.e("isActivityChangingConfigurations", "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
        LogUtil.e("isActivityChangingConfigurations", "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityDestroyed")
    }
}