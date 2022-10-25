package com.thingsenz.musicplayer.ui.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.thingsenz.musicplayer.BuildConfig
import com.thingsenz.musicplayer.R

class AboutActivity : AppCompatActivity() {

    private lateinit var aboutImage: ImageView
    private lateinit var aboutName: TextView
    private lateinit var aboutVersion: TextView
    private lateinit var aboutPermissions: TextView
    private lateinit var aboutDev: TextView
    private lateinit var aboutRepo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        aboutImage = findViewById(R.id.aboutIcon)
        aboutName = findViewById(R.id.aboutName)
        aboutVersion = findViewById(R.id.aboutVersion)
        aboutPermissions = findViewById(R.id.aboutPermissions)
        aboutDev = findViewById(R.id.aboutDev)
        aboutRepo = findViewById(R.id.aboutRepo)

        aboutRepo.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("https://github.com/JohnX4321/TZMusicPlayer")
            }
            startActivity(intent)
        }

        aboutVersion.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        aboutImage.setImageDrawable(ContextCompat.getDrawable(this,R.mipmap.ic_launcher))
    }
}