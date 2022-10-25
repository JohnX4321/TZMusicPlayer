package com.thingsenz.musicplayer.models.vm

import android.graphics.Bitmap

data class Song(
    val artist: String?,
    val year: Int,
    val track: Int,
    val title: String?,
    val displayName: String?,
    val duration: Long,
    val album: String?,
    val albumId: Long?,
    val path: String?,
    val folderName: String?,
    val id: Long?,
    val dateAdded: Int,
    val bitmap: Bitmap?
)
