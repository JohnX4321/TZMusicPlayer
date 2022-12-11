package com.thingsenz.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.thingsenz.musicplayer.fragments.PermissionDeniedFragment
import com.thingsenz.musicplayer.fragments.SongsFragment
import com.thingsenz.musicplayer.services.mediaplayer.MPMusicService
import com.thingsenz.musicplayer.ui.activities.SettingsActivity
import com.thingsenz.musicplayer.utils.Prefs
import com.thingsenz.musicplayer.utils.Util

class MainActivity : AppCompatActivity() {

    val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if ((Util.isAtleastT()&&res[android.Manifest.permission.READ_MEDIA_AUDIO]==true) || (res[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true)) {
            launchMusicFragment()
        } else {
            val permFailFragment = PermissionDeniedFragment.getInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,permFailFragment)
                .commit()
        }
    }

    public var player: MPMusicService? = null
    var serviceBound = false

    public val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
             val binder = p1 as (MPMusicService.MusicServiceBinder)
            player = binder.getService()
            serviceBound = true
            Log.d("TZMP","BINDER CONNECTED")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceBound = false
            Log.d("TZMP","BINDER DISCONNECTED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        volumeControlStream = AudioManager.STREAM_MUSIC
        if (Prefs.getInstance().darkMode) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
        else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
        if (Util.isPermissionGranted(this)) {
            launchMusicFragment()
        } else {
            permissionLauncher.launch(Util.getPermissionList())
        }
    }

    override fun onResume() {
        super.onResume()
        
    }

    private fun launchMusicFragment() {
        val fragment = SongsFragment.getInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this,SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



}