package com.thingsenz.musicplayer.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thingsenz.musicplayer.MainActivity
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.utils.AppConst
import com.thingsenz.musicplayer.utils.Util


class PlayerFragment: BottomSheetDialogFragment() {

    companion object {
        @Volatile
        private var sINSTANCE: PlayerFragment? = null

        fun getInstance(): PlayerFragment = synchronized(this) {
            if (sINSTANCE==null) {
                sINSTANCE = PlayerFragment()
            }
            return@synchronized sINSTANCE!!
        }

        var repeat = Util.RepeatMode.REPEAT_OFF
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.setOnShowListener { dil ->
            val d = dil as BottomSheetDialog
            val bottomSheetInternal =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(bottomSheetInternal).state =
                BottomSheetBehavior.STATE_EXPANDED
        }
        return inflater.inflate(R.layout.fragment_player,container,false)
    }

    private lateinit var songArt: ImageView
    private lateinit var songName: TextView
    private lateinit var songArtist: TextView
    private lateinit var playerSeekBar: SeekBar
    private lateinit var playPauseBtn: ImageView
    private lateinit var prevBtn: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var repeatBtn: ImageView
    private lateinit var likeBtn: ImageView
    private lateinit var act: MainActivity
    private lateinit var durationEndText: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songArt = view.findViewById(R.id.player_art)
        songName = view.findViewById(R.id.player_title)
        songArtist = view.findViewById(R.id.player_artist)
        playerSeekBar = view.findViewById(R.id.player_seekbar)
        playPauseBtn = view.findViewById(R.id.pauseBtn)
        prevBtn = view.findViewById(R.id.prevBtn)
        nextBtn = view.findViewById(R.id.nextBtn)
        repeatBtn = view.findViewById(R.id.repeatBtn)
        likeBtn = view.findViewById(R.id.player_like)
        durationEndText = view.findViewById(R.id.endDurationText)
        act = (requireActivity() as MainActivity)
        songArt.setImageBitmap(if (Util.isAtleastQ()) Util.getAlbumArt(view.context,Util.mediaFile!!.albumId ?: 0) else Util.getAlbumArt(Util.mediaFile!!.path ?: ""))
        songName.text = Util.mediaFile!!.displayName
        songArtist.text = Util.mediaFile!!.artist
        if (act.player?.mediaPlayer?.isPlaying == true) {
            playPauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause))
        }
        playPauseBtn.setOnClickListener {
            if (act.player?.mediaPlayer?.isPlaying==true)
                playPauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play))
            else
                playPauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause))
            it.context.sendBroadcast(Intent(AppConst.PLAY_PAUSE_ACTION))
        }
        durationEndText.text = Util.getFormattedTime(Util.mediaFile?.duration ?: 0)
        repeatBtn.setImageDrawable(if (repeat==Util.RepeatMode.REPEAT_OFF) ContextCompat.getDrawable(requireContext(),R.drawable.ic_repeat) else ContextCompat.getDrawable(requireContext(),R.drawable.ic_repeat_one))
        playerSeekBar.max = Util.mediaFile?.duration!!.toInt()

        if (act.player?.mediaPlayer!=null) {
            playerSeekBar.progress = act.player?.mediaPlayer!!.currentPosition
        }
        playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    act.player?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        repeatBtn.setOnClickListener {
            repeat = if (repeat==Util.RepeatMode.REPEAT_OFF) {
                repeatBtn.setImageDrawable(ContextCompat.getDrawable(it.context,R.drawable.ic_repeat_one))
                act.player?.mediaPlayer?.setNextMediaPlayer(act.player?.initPlayerInternal())
                Util.RepeatMode.REPEAT_ONCE
            } else {
                repeatBtn.setImageDrawable(ContextCompat.getDrawable(it.context,R.drawable.ic_repeat))
                act.player?.mediaPlayer?.setNextMediaPlayer(null)
                Util.RepeatMode.REPEAT_OFF
            }
        }
        val handler = Handler(Looper.getMainLooper())
        act.runOnUiThread(object : Runnable {
            override fun run() {
                if (act.player?.mediaPlayer!=null) {
                    playerSeekBar.progress = act.player?.mediaPlayer!!.currentPosition
                }
                handler.postDelayed(this,1000)
            }
        })
    }

    override fun onDestroy() {
        sINSTANCE = null
        super.onDestroy()
    }

}