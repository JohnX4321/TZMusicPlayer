package com.thingsenz.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.audiofx.AudioEffect
import android.provider.Settings
import androidx.multidex.MultiDex
import com.thingsenz.musicplayer.utils.AppConst
import com.thingsenz.musicplayer.utils.Prefs
import com.thingsenz.musicplayer.utils.Util

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
        val nm = getSystemService(NotificationManager::class.java)
        if (Util.isAtleastO()) {
            nm.createNotificationChannel(
                NotificationChannel(
                    AppConst.CHANNEL_ID,
                    AppConst.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)
            )
        }
        if (packageManager.resolveActivity(Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL),PackageManager.MATCH_SYSTEM_ONLY)!=null) {
            Prefs.getInstance().isSystemEqualizerPresent = true
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

}