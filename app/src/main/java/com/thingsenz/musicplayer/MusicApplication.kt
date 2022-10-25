package com.thingsenz.musicplayer

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.thingsenz.musicplayer.utils.Prefs

class MusicApplication: Application(), Thread.UncaughtExceptionHandler {

    companion object {
        var appContext: Context? = null
    }

    override fun uncaughtException(p0: Thread, p1: Throwable) {

    }

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        appContext = applicationContext
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

}