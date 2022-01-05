package com.roundesk.sdk.config

import android.content.Context
import android.content.SharedPreferences

open class SharedPreferenceManager(val context: Context) {

    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    init {
        sharedPreferences = context.getSharedPreferences("", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit();
    }

    fun setStringData(key: String, value: String?) {
        editor?.putString(key, value)?.commit()
    }

    fun getStringData(key: String): String? {
        return sharedPreferences!!.getString(key, "")
    }

    fun setBooleanData(key: String, value: Boolean) {
        editor?.putBoolean(key, value)?.commit()
    }

    fun getBooleanData(key: String): Boolean {
        return sharedPreferences!!.getBoolean(key, false)
    }

    fun setIntData(key: String, value: Int) {
        editor?.putInt(key, value)?.commit()
    }

    fun getIntData(key: String): Int {
        return sharedPreferences!!.getInt(key, 0)
    }

    fun setStringSetData(key: String, value: Set<String>) {
        editor?.putStringSet(key, value)?.commit()
    }

    fun getStringSetData(key: String): MutableSet<String>? {
        return  sharedPreferences!!.getStringSet(key, null)
    }

    fun clear() {
        editor?.clear()
        editor?.commit()
    }

}