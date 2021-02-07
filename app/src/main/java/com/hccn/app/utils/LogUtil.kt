package com.hccn.app.utils

import android.content.Context
import android.util.Log
import com.hccn.sisyphus.Sisyphus

object LogUtil {
    fun i(context: Context, tag: String, msg: String) {
        if (Sisyphus.getFlavorEnvironmentBean(context) == Sisyphus.PRINTLOG_OPEN_ENVIRONMENT) {
            Log.i(tag, msg)
        }
    }

    fun e(context: Context, tag: String, msg: String) {
        if (Sisyphus.getFlavorEnvironmentBean(context) == Sisyphus.PRINTLOG_OPEN_ENVIRONMENT) {
            Log.e(tag, msg)
        }
    }

    fun e(context: Context, tag: String, msg: String, th: Throwable) {
        if (Sisyphus.getFlavorEnvironmentBean(context) == Sisyphus.PRINTLOG_OPEN_ENVIRONMENT) {
            Log.e(tag, msg, th)
        }
    }
}