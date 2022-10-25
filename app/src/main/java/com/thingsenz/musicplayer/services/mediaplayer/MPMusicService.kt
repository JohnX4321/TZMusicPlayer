package com.thingsenz.musicplayer.services.mediaplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.utils.AppConst
import com.thingsenz.musicplayer.utils.Util
import kotlin.math.ln

class MPMusicService: Service(),MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,MediaPlayer.OnBufferingUpdateListener,AudioManager.OnAudioFocusChangeListener,MediaPlayer.OnSeekCompleteListener {

    private val localbinder = MusicServiceBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    var mediaFile = ""
    private var resumePosition = 0
    private var ongoingCall = false

    private val NOISY_RECEIVER = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            pausePlayback()
        }
    }


    private val NEXT_ACTION_RECEIVER = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            playNextSong()
        }
    }

    private val PLAY_PAUSE_ACTION_RECEIVER = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (mediaPlayer?.isPlaying!!) {
                pausePlayback()
            } else if (mediaFile!="") {
                resumePlayback()
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.notify(199, getNotification())
        }
    }

    private fun playNextSong() {

    }

    private val STOP_ACTION_RECEIVER = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            stopPlayback()
            stopSelf()
        }
    }

    private val PREV_ACTION_RECEIVER = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            playPrevSong()
        }
    }

    private fun playPrevSong() {

    }

    private fun registerNoisyReceiver() {
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(NOISY_RECEIVER,intentFilter)
    }

    private fun registerBtnReceivers() {
        registerReceiver(PLAY_PAUSE_ACTION_RECEIVER, IntentFilter(AppConst.PLAY_PAUSE_ACTION))
        registerReceiver(NEXT_ACTION_RECEIVER, IntentFilter(AppConst.NEXT_ACTION))
        registerReceiver(PREV_ACTION_RECEIVER, IntentFilter(AppConst.PREV_ACTION))
        registerReceiver(STOP_ACTION_RECEIVER, IntentFilter(AppConst.STOP_ACTION))
    }

    private fun unregisterReceivers() {
        unregisterReceiver(NOISY_RECEIVER)
        unregisterReceiver(PLAY_PAUSE_ACTION_RECEIVER)
        unregisterReceiver(NEXT_ACTION_RECEIVER)
        unregisterReceiver(PREV_ACTION_RECEIVER)
    }




    override fun onBind(p0: Intent?): IBinder {
        return localbinder
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(199, getNotification())
        registerNoisyReceiver()

    }

    override fun onDestroy() {
        unregisterReceivers()
        if (mediaPlayer!=null) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer=null
        }
        super.onDestroy()
    }

    private fun getNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (Util.isAtleastO()) {
            nm.createNotificationChannel(NotificationChannel(AppConst.CHANNEL_ID,AppConst.CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH))
        }
        val notifView = RemoteViews(packageName,R.layout.layout_notification_music_small)
        notifView.setTextViewText(R.id.notifSongName,Util.mediaFile?.displayName)
        notifView.setImageViewBitmap(R.id.notifSongCover,Util.mediaFile?.bitmap)
        notifView.setOnClickPendingIntent(R.id.notifStopBtn,PendingIntent.getBroadcast(this,0,Intent(AppConst.STOP_ACTION),PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE))
        notifView.setOnClickPendingIntent(R.id.notifPreviousBtn, PendingIntent.getBroadcast(this,0,
            Intent(AppConst.PREV_ACTION),PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        ))
        notifView.setOnClickPendingIntent(R.id.notifNextBtn, PendingIntent.getBroadcast(this,0,
            Intent(AppConst.NEXT_ACTION),PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        ))
        if (mediaPlayer==null || mediaPlayer?.isPlaying!!) {
            notifView.setImageViewResource(R.id.notifPauseBtn,R.drawable.ic_pause)
        } else {
            notifView.setImageViewResource(R.id.notifPauseBtn,R.drawable.ic_play)
        }
        notifView.setOnClickPendingIntent(R.id.notifPauseBtn, PendingIntent.getBroadcast(this,0,Intent(),PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE))
        val notif = NotificationCompat.Builder(this,AppConst.CHANNEL_ID)
            .setContentTitle("Music Player")
            .setCustomBigContentView(notifView)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH).build()
        return notif
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!requestAudioFocus()) stopSelf()
        if (mediaFile=="") {
            mediaFile=Util.mediaFile?.path?: ""
        }
        if (mediaFile!="") {
            initPlayer()
            startPlayback()
        }
        Log.d("TZMP","SERVICE STARTED")
        return START_STICKY
    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
        //TODO buffering over network
    }

    override fun onCompletion(p0: MediaPlayer?) {
        //playback completed
        stopPlayback()
        stopSelf()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        //error during playback
        when (p1) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d("TZMusicPlayer","Media Error not valid")
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d("TZMusicPlayer","Media error server died")
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d("TZMusicPlayer","Media error unknown")
        }
        Toast.makeText(this,"An unknown Error occurred",Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        //communication information
        return false
    }

    override fun onPrepared(p0: MediaPlayer?) {
        //ready for playback
        startPlayback()
    }

    override fun onAudioFocusChange(p0: Int) {
        //invoked when audio focus updated
    }

    override fun onSeekComplete(p0: MediaPlayer?) {
        //invoked on seek complete
    }

    fun initPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener(this@MPMusicService)
            setOnErrorListener(this@MPMusicService)
            setOnPreparedListener(this@MPMusicService)
            setOnBufferingUpdateListener(this@MPMusicService)
            setOnSeekCompleteListener(this@MPMusicService)
            setOnInfoListener(this@MPMusicService)
            reset()
            setVolume(getVolume(),getVolume())
            setAudioAttributes(AudioAttributes.Builder().setFlags(AudioManager.STREAM_MUSIC).build())
        }
        if (mediaFile=="") {
            mediaFile=Util.mediaFile?.path?: ""
        }
        try {
            mediaPlayer?.setDataSource(mediaFile)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception){
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(AudioManager::class.java)
        val res = if (Util.isAtleastO())
            audioManager.requestAudioFocus(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(AudioAttributes.Builder().setFlags(AudioManager.STREAM_MUSIC).build()).setOnAudioFocusChangeListener(this).build())
        else
            audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND)
        return res==AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)
    }

    fun startPlayback() {
        if (!mediaPlayer!!.isPlaying) mediaPlayer?.start()
    }

    fun stopPlayback() {
        if (mediaPlayer==null) return
        if (mediaPlayer!!.isPlaying)
            mediaPlayer!!.stop()
    }

    fun pausePlayback() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    fun resumePlayback() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
        }
    }

    inner class MusicServiceBinder: Binder() {
        fun getService() = this@MPMusicService
    }

    fun getVolume(): Float {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)/ 15f

        //just added the following line
        return (ln(15 - currentVolume) / ln(15.0)).toFloat()
    }

}