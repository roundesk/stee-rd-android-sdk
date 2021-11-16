package com.roundesk_stee_sdk.util

import android.util.Log

open class LogUtil {

    companion object {
        private const val isLog: Boolean = true

        fun i(tag: String, message: String) {
            if (isLog) {
                Log.i(tag, message)
            }
        }

        fun e(tag: String, message: String) {
            if (isLog) {
                Log.e(tag, message)
            }
        }

        fun d(tag: String, message: String) {
            if (isLog) {
                Log.d(tag, message)
            }
        }

        fun v(tag: String, message: String) {
            if (isLog) {
                Log.v(tag, message)
            }
        }
    }

}