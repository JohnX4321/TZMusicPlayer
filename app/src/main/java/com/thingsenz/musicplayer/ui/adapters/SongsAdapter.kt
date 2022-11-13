package com.thingsenz.musicplayer.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.thingsenz.musicplayer.R
import com.thingsenz.musicplayer.models.vm.Song
import com.thingsenz.musicplayer.utils.Util

class SongsAdapter(private val songsList: List<Song>,private val listener: ItemClickListener): RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

    companion object {

    }


    inner class SongViewHolder(view: View): RecyclerView.ViewHolder(view) {


        fun bindItems(song: Song?) {
            val songImage = itemView.findViewById<ImageView>(R.id.song_cover)
            val songTitle = itemView.findViewById<TextView>(R.id.song_title)
            val songArtist = itemView.findViewById<TextView>(R.id.song_artist)
            songTitle.text = song?.displayName
            songArtist.text = song?.artist
            Log.d("TZMP","${song?.path},${song?.folderName},${song?.title},${song?.displayName},${song?.id}")
            val bitmap = song?.bitmap
            if (bitmap!=null)
                songImage.setImageBitmap(bitmap)
            else
                songImage.setImageDrawable(ContextCompat.getDrawable(itemView.context,R.drawable.ic_action_name))
            songImage.setOnClickListener { listener.onItemClick(song?.path!!,absoluteAdapterPosition) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song,parent,false))
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bindItems(songsList[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    interface ItemClickListener {
        fun onItemClick(path: String,pos: Int)
    }

}