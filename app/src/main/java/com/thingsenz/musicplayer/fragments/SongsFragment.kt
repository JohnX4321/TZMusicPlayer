package com.thingsenz.musicplayer.fragments

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eudycontreras.boneslibrary.extensions.applySkeletonDrawable
import com.eudycontreras.boneslibrary.extensions.dp
import com.eudycontreras.boneslibrary.framework.skeletons.SkeletonDrawable
import com.thingsenz.musicplayer.MainActivity
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.models.vm.Song
import com.thingsenz.musicplayer.models.vm.SongViewModel
import com.thingsenz.musicplayer.services.mediaplayer.MPMusicService
import com.thingsenz.musicplayer.ui.adapters.SongsAdapter
import com.thingsenz.musicplayer.utils.AppConst
import com.thingsenz.musicplayer.utils.Util

class SongsFragment: Fragment(), SongsAdapter.ItemClickListener {

    companion object {
        @Volatile
        private var sINSTANCE: SongsFragment? = null

        fun getInstance(): SongsFragment = synchronized(this) {
             if (sINSTANCE==null) {
                sINSTANCE = SongsFragment()
            }
            return@synchronized sINSTANCE!!
        }
        var position=-1
    }
    private lateinit var act: MainActivity
    private var songsList: MutableList<Song>? = null
    private lateinit var miniPlayer: LinearLayout
    private lateinit var miniPlayerSongName: TextView
    private lateinit var miniPlayerSongArtist: TextView
    private lateinit var miniPlayerExpandBtn: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context).inflate(R.layout.fragment_songs,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vm = ViewModelProvider(requireActivity()).get(SongViewModel::class.java)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)
        progressBar.visibility = View.VISIBLE
        vm.getMusicObserver().observe(viewLifecycleOwner) {
            songsList = it
            val adapter = SongsAdapter(songsList?: emptyList(),this)
            adapter.setHasStableIds(true)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
            progressBar.visibility=View.GONE
        }
        act = (requireActivity() as MainActivity)
        vm.getSongs()
        miniPlayer = view.findViewById(R.id.miniPlayerView)
        miniPlayerSongName = view.findViewById(R.id.miniPlayerName)
        miniPlayerSongArtist = view.findViewById(R.id.miniPlayerArtist)
        miniPlayerExpandBtn = view.findViewById(R.id.miniPlayerExpandBtn)
        miniPlayerExpandBtn.setOnClickListener {
            if (Util.mediaFile!=null) {
                childFragmentManager.beginTransaction()
                    .add(PlayerFragment.getInstance(), "PLAYER")
                    .commit()
            }
        }
    }

    private fun playSong(file: String="",pos: Int=0, start: Boolean) {
        if (start) {
            Util.mediaFile = songsList!![pos]
            val playerIntent = Intent(requireContext(), MPMusicService::class.java)
            ContextCompat.startForegroundService(act, playerIntent)
            if (!act.serviceBound) {
                act.bindService(playerIntent, act.serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } else {
            act.player?.stopPlayback()
            act.player?.mediaPlayer?.reset()
        }
    }

    override fun onItemClick(path: String,pos: Int) {
        if (act.player?.mediaPlayer!=null && act.player?.mediaPlayer?.isPlaying!!) {
            playSong(start = false)
        }
        miniPlayerSongName.text = songsList!![pos].displayName?.apply {
            if (length>15)
                substring(0,15) + "..."
        }
        miniPlayerSongArtist.text = songsList!![pos].artist
        playSong(path,pos,true)
        position = pos
    }

}