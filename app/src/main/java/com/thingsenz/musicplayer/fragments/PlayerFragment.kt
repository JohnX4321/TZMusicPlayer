package com.thingsenz.musicplayer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        act = (requireActivity() as MainActivity)
        songArt.setImageBitmap(if (Util.isAtleastQ()) Util.getAlbumArt(view.context,Util.mediaFile!!.albumId ?: 0) else Util.getAlbumArt(Util.mediaFile!!.path ?: ""))
        songName.text = Util.mediaFile!!.displayName
        songArtist.text = Util.mediaFile!!.artist
        playPauseBtn.setOnClickListener {
            if (act.player?.mediaPlayer?.isPlaying==true)
                playPauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play))
            else
                playPauseBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause))
            it.context.sendBroadcast(Intent(AppConst.PLAY_PAUSE_ACTION))
        }

    }

}