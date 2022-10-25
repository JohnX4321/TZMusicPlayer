package com.thingsenz.musicplayer.models.vm

import android.app.Application
import android.content.res.Resources
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.thingsenz.musicplayer.MusicApplication
import com.thingsenz.musicplayer.utils.Prefs
import com.thingsenz.musicplayer.utils.Util
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.random.Random

class SongViewModel(application: Application) : AndroidViewModel(application) {

    private var deviceSongs = MutableLiveData<MutableList<Song>?>()
    private val vmJob = SupervisorJob()

    private val handler = CoroutineExceptionHandler{_,e-> deviceSongs.value = null }
    private val uiDispatcher = Dispatchers.Main
    private val IODispatcher = Dispatchers.IO
    private val uiScope = CoroutineScope(uiDispatcher)

    private var songsList = ArrayList<Song>()

    fun getMusicObserver() = deviceSongs

    fun getSongFromIntent(name: String) =
        songsList.firstOrNull{it.displayName == name}

    var songsFiltered: MutableList<Song>? = null

    var songsByArtist : Map<String?,List<Song>>? = null

    var songsByAlbum : Map<String?,List<Song>>? = null

    var albumsByArtist : MutableMap<String,List<Any>>? = mutableMapOf()

    var musicByFolder : Map<String,List<Song>>? = null

    fun getShuffledSong(): Song? {
        songsFiltered?.shuffled()?.run {
            return get(Random.nextInt())
        }
        return songsFiltered?.random()
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
    }

    fun cancel() {
        onCleared()
    }

    fun getSongs() {
        uiScope.launch {
            withContext(IODispatcher) {
                val s = getSongs(getApplication())
                withContext(uiDispatcher) {
                    deviceSongs.value = s
                }
            }
        }
    }

    fun getSongs(application: Application) = try {
        val path = MediaStore.Audio.AudioColumns.DATA
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val proj = arrayOf(
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            path,
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
        )
        val selection = "${MediaStore.Audio.AudioColumns.IS_MUSIC}=1"
        val sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val c = application.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,proj,selection,null,sortOrder)
        c?.use { cursor->
            val artistI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val yearI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR)
            val trackI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
            val titleI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val displaynameI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val durationI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val albumI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
            val albumIDI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val idI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val relPathI = cursor.getColumnIndexOrThrow(path)
            val dateModifiedI = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
            while (cursor.moveToNext()) {
                val sId = cursor.getLong(idI)
                val sArtist = cursor.getString(artistI)
                val sYear = cursor.getInt(yearI)
                val sTrack = cursor.getInt(trackI)
                val sTitle = cursor.getString(titleI)
                val sDisplayname = cursor.getString(displaynameI)
                val sDuration = cursor.getLong(durationI)
                val sAlbum = cursor.getString(albumI)
                val sAlbumId = cursor.getLong(albumIDI)
                var sPath = cursor.getString(relPathI)
                val sDateMod = cursor.getInt(dateModifiedI)

                val sFolder = if (Util.isAtleastQ())
                    sPath?: "/"
                else {
                    val returnPath = File(sPath).parentFile?.name ?: "/"
                    if (returnPath!="0") returnPath
                    else "/"
                }
                val sArt = if (Util.isAtleastQ()) Util.getAlbumArt(MusicApplication.appContext!!, sAlbumId) else Util.getAlbumArt(path)
                songsList.add(Song(sArtist,sYear,sTrack,sTitle,sDisplayname,sDuration,sAlbum,sAlbumId,sPath,sFolder,sId,sDateMod,sArt))
            }
        }
        songsList
    } catch (e: Exception){
        e.printStackTrace()
        null
    }

    private fun getSong(application: Application): MutableList<Song> {
        synchronized(startQuery(application)) {
            buildLibrary(application.resources)
        }
        return songsList
    }

    private fun startQuery(application: Application) {
        getSongs(application)?.let { f->
            songsList = f
        }
    }

    private fun buildLibrary(res: Resources) {
        songsFiltered = songsList.distinctBy { it.artist to it.year to it.track to it.title to it.duration to it.album }
            .toMutableList()

        Prefs.getInstance().filters?.let { f->
            songsFiltered = songsFiltered?.filter { m->
                !f.contains(m.artist) and !f.contains(m.album)
            }?.toMutableList()
        }
        songsFiltered?.let { f->
            f.filterNot { Prefs.getInstance().filters?.contains(it.artist)!! }
            songsByArtist = f.groupBy { it.artist }
            songsByAlbum = f.groupBy { it.album }
            musicByFolder = f.groupBy { it.path!! }
        }

        songsByArtist?.keys?.iterator()?.let { i->
            while (i.hasNext()) {
                i.next()?.let { a->
                    val album = songsByArtist?.getValue(a)
                    //albumsByArtist?.set(a,Util.buildSortedArtistAlbums(res,album))
                }
            }
        }
    }


}