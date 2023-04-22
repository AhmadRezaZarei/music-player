package com.ray.zarei.musicplayer.repositories

import android.content.Context
import android.database.Cursor
import com.ray.zarei.musicplayer.Song


interface SongRepository {

    fun songs(): List<Song>

    fun songs(cursor: Cursor?): List<Song>

    fun songs(query: String): List<Song>

    fun sortedSongs(cursor: Cursor?): List<Song>

    fun songsByFilePath(filePath: String, ignoreBlackedList: Boolean = false)

    fun song(cursor: Cursor?): Song

    fun song(songId: Long): Song


}

class DefaultSongRepository(private val context: Context): SongRepository {


    override fun songs(): List<Song> {
        TODO("Not yet implemented")
    }

    override fun songs(cursor: Cursor?): List<Song> {
        TODO("Not yet implemented")
    }

    override fun songs(query: String): List<Song> {
        TODO("Not yet implemented")
    }

    override fun sortedSongs(cursor: Cursor?): List<Song> {
        TODO("Not yet implemented")
    }

    override fun songsByFilePath(filePath: String, ignoreBlackedList: Boolean) {
        TODO("Not yet implemented")
    }

    override fun song(cursor: Cursor?): Song {
        TODO("Not yet implemented")
    }

    override fun song(songId: Long): Song {
        TODO("Not yet implemented")
    }


}