package com.my.applist.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import org.json.JSONObject
import java.util.Locale

val TAG = "CommonUtils"

fun dp2px(context: Context, dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

/**
 * 获取系统语言设置的国家代码
 */
fun getCountry(): String {
    try {
        return getLocal(Resources.getSystem().configuration)?.country?.uppercase() ?: "US"
    } catch (e: Exception) {
        Log.e(TAG, "Error getCountry: ${e.message}")
        return "US"
    }
}

private fun getLocal(configuration: Configuration): Locale? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return configuration.locales[0]
    } else {
        return configuration.locale
    }
}

/**
 * map转成json
 */
fun mapToJson(map: Map<String, Any?>): String {
    val jsonObject = JSONObject()
    map.forEach { (key, value) ->
        when (value) {
            null -> jsonObject.put(key, JSONObject.NULL)
            is String -> jsonObject.put(key, value)
            is Number -> jsonObject.put(key, value)
            is Boolean -> jsonObject.put(key, value)
            else -> jsonObject.put(key, value.toString())
        }
    }
    return jsonObject.toString()
}


fun openUrlWithBrowser(activity: Activity, url: String) {
    try {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = url.toUri()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    } catch (e: Exception) {
        Log.e(TAG, "Error openUrlWithBrowser: ${e.message}")
    }
}

fun getVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: ""
    } catch (e: Exception) {
        Log.e(TAG,"Error getVersionName: ${e.message}")
        ""
    }
}