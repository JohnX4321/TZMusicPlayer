package com.thingsenz.musicplayer.services.mediaplayer

import android.app.Notification
import android.app.Notification.Builder
import android.app.Notification.Style
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.VolumeShaper
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.thingsenz.musicplayer.MusicApplication
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.fragments.SongsFragment
import com.thingsenz.musicplayer.models.vm.Song
import com.thingsenz.musicplayer.models.vm.SongViewModel
import com.thingsenz.musicplayer.utils.AppConst
import com.thingsenz.musicplayer.utils.Prefs
import com.thingsenz.musicplayer.utils.Util
import kotlin.math.ln

class MPMusicService: Service(),MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,MediaPlayer.OnBufferingUpdateListener,AudioManager.OnAudioFocusChangeListener,MediaPlayer.OnSeekCompleteListener {

    private val localbinder = MusicServiceBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    var currSong: Song? = null
    private var resumePosition = 0L
    private var ongoingCall = false
    private var notifBuilder: NotificationCompat.Builder? = null
    private var notifView: RemoteViews? = null

    init {

            }

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
            } else {
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
            stopForeground(STOP_FOREGROUND_REMOVE)
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
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(199)
        removeAudioFocus()
        super.onDestroy()
    }

    private fun getNotification(): Notification {
        //if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R)
            return getNotificationR()
        /*if (notifView==null) {
            notifView = RemoteViews(
                MusicApplication.appContext!!.packageName,
                R.layout.layout_notification_music_small
            )
            notifView!!.setTextViewText(R.id.notifSongName, Util.mediaFile?.displayName)
            notifView!!.setImageViewBitmap(R.id.notifSongCover, Util.mediaFile?.bitmap)
            notifView!!.setOnClickPendingIntent(
                R.id.notifStopBtn,
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(AppConst.STOP_ACTION),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            notifView!!.setOnClickPendingIntent(
                R.id.notifPreviousBtn, PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(AppConst.PREV_ACTION),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            notifView!!.setOnClickPendingIntent(
                R.id.notifNextBtn, PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(AppConst.NEXT_ACTION),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            notifView!!.setOnClickPendingIntent(
                R.id.notifPauseBtn,
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(AppConst.PLAY_PAUSE_ACTION),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        if (mediaPlayer==null || mediaPlayer?.isPlaying!!) {
            notifView!!.setImageViewResource(R.id.notifPauseBtn,R.drawable.ic_pause)
        } else {
            notifView!!.setImageViewResource(R.id.notifPauseBtn,R.drawable.ic_play)
        }

        notifBuilder = NotificationCompat.Builder(this,AppConst.CHANNEL_ID)
            .setContentTitle("Music Player")
            .setCustomBigContentView(notifView)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        return notifBuilder!!.build()*/
    }

    fun getNotificationR(): Notification {
        val mediaSession = MediaSessionCompat(this,"PlayerService")
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SEEK_TO)
            .setState(if (mediaPlayer==null||mediaPlayer?.isPlaying!!) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,resumePosition,1f,SystemClock.elapsedRealtime()).build())
        mediaSession.setMetadata(MediaMetadataCompat.fromMediaMetadata(MediaMetadata.Builder().putLong(MediaMetadata.METADATA_KEY_DURATION,currSong?.duration ?: -1L).build()))
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                if (mediaPlayer!=null)
                    mediaPlayer!!.seekTo(pos.toInt())
            }
        })
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken)
        notifBuilder = NotificationCompat.Builder(this,AppConst.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(currSong?.bitmap?: BitmapFactory.decodeResource(resources,R.drawable.ic_action_name))
            .setOnlyAlertOnce(true)
            .setStyle(mediaStyle)
            .setDeleteIntent(PendingIntent.getBroadcast(this,0,Intent(AppConst.STOP_ACTION),PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE))
            .setContentText(Util.mediaFile?.displayName)
            .addAction(NotificationCompat.Action(if (mediaPlayer==null||mediaPlayer?.isPlaying!!) R.drawable.ic_pause else R.drawable.ic_play,"Pause",PendingIntent.getBroadcast(
                this,
                0,
                Intent(AppConst.PLAY_PAUSE_ACTION),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )))
            .addAction(NotificationCompat.Action(R.drawable.ic_previous,"Previous",PendingIntent.getBroadcast(
                this,
                0,
                Intent(AppConst.PREV_ACTION),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )))
            .addAction(NotificationCompat.Action(R.drawable.ic_next,"Next",PendingIntent.getBroadcast(
                this,
                0,
                Intent(AppConst.NEXT_ACTION),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )))
            .addAction(NotificationCompat.Action(R.drawable.ic_stop,"Stop",PendingIntent.getBroadcast(
                this,
                0,
                Intent(AppConst.STOP_ACTION),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        return notifBuilder!!.build()

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!requestAudioFocus()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        if (currSong==null)
            currSong=Util.mediaFile
        startForeground(199, getNotification())
        initPlayer()
        startPlayback()
        registerBtnReceivers()
        Log.d("TZMP","SERVICE STARTED")
        return START_STICKY
    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
        //TODO buffering over network
    }

    override fun onCompletion(p0: MediaPlayer?) {
        //playback completed
        if (!Prefs.getInstance().playSongsSeq) {
            stopPlayback()
            stopSelf()
        } else {
            Util.mediaFile = SongViewModel.songsList[if (SongsFragment.position>SongViewModel.songsList.size) 0 else SongsFragment.apply { position+=1 }.position]//next
            val newMP = initPlayerInternal()
            mediaPlayer?.setNextMediaPlayer(newMP)
            mediaPlayer?.release()
            mediaPlayer = newMP
        }
    }

    fun setNextSong(path: String) {

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
        mediaPlayer=initPlayerInternal()
    }

    private fun initPlayerInternal(): MediaPlayer {
         return MediaPlayer().apply {
             reset()
            setOnCompletionListener(this@MPMusicService)
            setOnErrorListener(this@MPMusicService)
            setOnPreparedListener(this@MPMusicService)
            setOnBufferingUpdateListener(this@MPMusicService)
            setOnSeekCompleteListener(this@MPMusicService)
            setOnInfoListener(this@MPMusicService)
            //setVolume(getVolume(),getVolume())
            //setAudioAttributes(AudioAttributes.Builder().setFlags(AudioManager.STREAM_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())
             if (currSong!=Util.mediaFile) {
                currSong=Util.mediaFile
             }
             try {
                 setDataSource(currSong!!.path)
                 prepare()
             } catch (e: Exception){
                 e.printStackTrace()
                 stopSelf()
             }
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(AudioManager::class.java)
        val res = if (Util.isAtleastO())
            audioManager.requestAudioFocus(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(AudioAttributes.Builder().setFlags(AudioManager.STREAM_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()).setOnAudioFocusChangeListener(this).build())
        else
            audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,  AudioManager.FLAG_PLAY_SOUND)
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
            resumePosition = mediaPlayer!!.currentPosition.toLong()
        }
    }

    fun resumePlayback() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition.toInt())
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