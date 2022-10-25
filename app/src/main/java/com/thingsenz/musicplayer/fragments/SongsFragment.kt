package com.thingsenz.musicplayer.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thingsenz.musicplayer.MainActivity
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.models.vm.Song
import com.thingsenz.musicplayer.models.vm.SongViewModel
import com.thingsenz.musicplayer.services.mediaplayer.MPMusicService
import com.thingsenz.musicplayer.ui.adapters.SongsAdapter
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
    }
    private lateinit var act: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TZMP","FRag created")
        return LayoutInflater.from(context).inflate(R.layout.fragment_songs,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vm = ViewModelProvider(requireActivity()).get(SongViewModel::class.java)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        var songsList: MutableList<Song>?
        vm.getMusicObserver().observe(viewLifecycleOwner) {
            songsList = it
            Log.d("TZMP",songsList?.size.toString())
            val adapter = SongsAdapter(songsList?: emptyList(),this)
            adapter.setHasStableIds(true)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            recyclerView.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
        }
        act = (requireActivity() as MainActivity)
        vm.getSongs()

    }

    private fun playSong(file: String,start: Boolean) {
        if (!act.serviceBound) {
            val playerIntent = Intent(requireContext(), MPMusicService::class.java)
            ContextCompat.startForegroundService(act, playerIntent)
            act.bindService(playerIntent, act.serviceConnection, Context.BIND_AUTO_CREATE)
        }
        /*if (start) {
            Util.mediaFile = file
            act.player?.mediaFile=file
            act.player?.initPlayer()
            act.player?.startPlayback()
        } else {
            act.player?.stopPlayback()
            act.player?.mediaPlayer?.reset()
        }*/
    }

    override fun onItemClick(path: String) {
        /*if (act.player?.mediaPlayer?.isPlaying!!) {
            playSong("",false)
        }*/
        playSong(path,true)
    }

}