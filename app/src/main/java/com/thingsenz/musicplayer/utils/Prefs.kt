package com.thingsenz.musicplayer.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Prefs(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: Prefs? = null

        fun init(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE = Prefs(context)
            INSTANCE
        }

        fun getInstance(): Prefs = INSTANCE ?: error("null reference")

        private val FILTERS_KEY = "filtersKey"
        private val DARK_MODE_KEY = "darkModeKey"
        private val PLAY_SONGS_SEQ_KEY = "playSongsSeqKey"
    }

    private val mPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    var filters: Set<String>?
        get() = mPrefs.getStringSet(FILTERS_KEY, setOf())
        set(value) = mPrefs.edit { putStringSet(FILTERS_KEY,value) }

    var darkMode: Boolean
        get() = mPrefs.getBoolean(DARK_MODE_KEY,true)
        set(value) = mPrefs.edit { putBoolean(DARK_MODE_KEY,value) }

    var playSongsSeq: Boolean
        get() = mPrefs.getBoolean(PLAY_SONGS_SEQ_KEY,false)
        set(value) = mPrefs.edit { putBoolean(PLAY_SONGS_SEQ_KEY,value) }


}