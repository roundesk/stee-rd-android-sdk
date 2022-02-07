package com.roundesk.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.roundesk.sdk.socket.SocketConnection
import com.roundesk.sdk.util.LogUtil


class SocketConfig : Application(), Application.ActivityLifecycleCallbacks {

    private var socketConnection: SocketConnection? = null

    companion object {
        private var mInstance: SocketConfig? = null

        @Synchronized
        fun getInstance(): SocketConfig? {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this

        registerActivityLifecycleCallbacks(this)
    }

    fun getSocketInstance(): SocketConnection? {
        return socketConnection
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityCreated")
        socketConnection = SocketConnection()
        socketConnection!!.connectSocket()
    }

    override fun onActivityStarted(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
        LogUtil.e("isActivityChangingConfigurations", "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        LogUtil.e("isActivityChangingConfigurations", "onActivityDestroyed")
    }
}