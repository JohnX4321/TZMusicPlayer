package com.thingsenz.musicplayer.utils

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import com.thingsenz.musicplayer.MainActivity
import com.thingsenz.musicplayer.models.vm.Song
import java.io.File
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Util {

    @JvmStatic
    fun isAtleastQ() = Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q

    @JvmStatic
    fun isAtleastR() = Build.VERSION.SDK_INT>=Build.VERSION_CODES.R

    @JvmStatic
    fun isAtleastT() = Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU

    @JvmStatic
    fun isAtleastO() = Build.VERSION.SDK_INT>=Build.VERSION_CODES.O

    @JvmStatic
    var mediaFile : Song? = null

    /*fun buildSortedArtistAlbums(res: Resources, artistSongs: List<Song>?): List<Album> {
        val sortedAlbums = mutableListOf<Album>()
        artistSongs?.let {
            try {
                val groupedSongs = it.groupBy { s->s.album }
                val iter = groupedSongs.keys.iterator()
                while (iter.hasNext()) {
                    val album = iter.next()
                    val albumSongs = groupedSongs.getValue(album).toMutableList()
                    albumSongs.sortBy { s->s.track }
                    sortedAlbums.add(
                        Album(album,albumSongs.first().year.toYear(res),albumSongs,albumSongs.sumOf { s->s.duration })
                    )
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
            sortedAlbums.sortBy {a->a.year}
        }
        return sortedAlbums
    }*/

    fun getAlbumArt(fd: FileDescriptor): Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(fd)
        val ba = mmr.embeddedPicture
        return if (ba!=null)
            BitmapFactory.decodeByteArray(ba,0,ba?.size?: 0)
        else
            null
    }

    fun getAlbumArt(path: String): Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(path)
        val ba = mmr.embeddedPicture
        return if (ba!=null)
            BitmapFactory.decodeByteArray(ba,0,ba?.size?: 0)
        else
            null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAlbumArt(context: Context, albumId: Long): Bitmap? {
        return try {
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,albumId)
            context.contentResolver.loadThumbnail(uri,Size(64,64),null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getPath(id: Long): String {
        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)
        return File(contentUri.path).absolutePath
    }

    private var PERMISSION_LIST = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun getPermissionList() = when {
        isAtleastT() -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        isAtleastR() -> PERMISSION_LIST
        else -> PERMISSION_LIST
    }

    fun shouldShowRationale(activity: Activity) = when {
        isAtleastT() -> activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO)
        else -> activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun getContentUri(id: Long) = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)

    enum class RepeatMode {
        REPEAT_ONCE,
        REPEAT_OFF
    }

    fun getFormattedTime(duration: Long) : String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration).toInt() % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration).toInt() % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            seconds > 0 -> String.format("00:%02d", seconds)
            else -> {
                "00:00"
            }
        }
    }

}