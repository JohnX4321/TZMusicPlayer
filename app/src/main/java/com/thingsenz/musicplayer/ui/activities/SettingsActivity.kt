package com.thingsenz.musicplayer.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.fragments.SettingsFragment

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer,SettingsFragment.getInstance())
            .commit()
    }

}