package com.ray.zarei.musicplayer.repositories

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.ray.zarei.musicplayer.Constants
import com.ray.zarei.musicplayer.Constants.baseProjection
import com.ray.zarei.musicplayer.Song
import com.ray.zarei.musicplayer.extensions.getInt
import com.ray.zarei.musicplayer.extensions.getLong
import com.ray.zarei.musicplayer.extensions.getString
import com.ray.zarei.musicplayer.extensions.getStringOrNull
import com.ray.zarei.musicplayer.helper.SortOrder
import com.ray.zarei.musicplayer.utils.VersionUtils
import java.text.Collator


interface SongsRepository {

    fun songs(): List<Song>

    fun songs(cursor: Cursor?): List<Song>

    fun songs(query: String): List<Song>

    fun sortedSongs(cursor: Cursor?): List<Song>

    fun songsByFilePath(filePath: String, ignoreBlackedList: Boolean = false): List<Song>

    fun song(cursor: Cursor?): Song

    fun song(songId: Long): Song


}

class DefaultSongsRepository(private val context: Context) : SongsRepository {


    override fun songs(): List<Song> {
        return sortedSongs(makeSongCursor(null, null))
    }

    override fun songs(cursor: Cursor?): List<Song> {
        val songs = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs

    }

    override fun songs(query: String): List<Song> {
        return songs(
            makeSongCursor(
                MediaStore.Audio.AudioColumns.TITLE + " LIKE ?",
                arrayOf("%$query%")
            )
        )
    }

    override fun sortedSongs(cursor: Cursor?): List<Song> {

        val collator = Collator.getInstance()
        val songs = songs(cursor)

        return songs.sortedWith { s1, s2 -> collator.compare(s1.title, s2.title) }

    }

    override fun songsByFilePath(filePath: String, ignoreBlackedList: Boolean): List<Song> {
        return songs(
            makeSongCursor(
                Constants.DATA + "=?",
                arrayOf(filePath),
                ignoreBlacklist = ignoreBlackedList
            )
        )
    }

    @JvmOverloads
    fun makeSongCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = SortOrder.SongSortOrder.SONG_A_Z,
        ignoreBlacklist: Boolean = false
    ): Cursor? {

        var selectionFinal = selection
        var selectionValuesFinal = selectionValues

        val uri = if (VersionUtils.hasQ()) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        return try {
            context.contentResolver.query(
                uri,
                baseProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder
            )
        } catch (ex: SecurityException) {
            return null
        }
    }

    override fun song(cursor: Cursor?): Song {
        val song: Song = if (cursor != null && cursor.moveToFirst()) {
            getSongFromCursorImpl(cursor)
        } else {
            Song.emptySong
        }
        cursor?.close()
        return song
    }

    override fun song(songId: Long): Song {
        return song(
            makeSongCursor(
                MediaStore.Audio.AudioColumns._ID + "=?",
                arrayOf(songId.toString())
            )
        )
    }

    private fun getSongFromCursorImpl(
        cursor: Cursor
    ): Song {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val year = cursor.getInt(MediaStore.Audio.AudioColumns.YEAR)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val data = cursor.getString(Constants.DATA)
        val dateModified = cursor.getLong(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ALBUM)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ARTIST)
        val composer = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")
        return Song(
            id,
            title,
            trackNumber,
            year,
            duration,
            data,
            dateModified,
            albumId,
            albumName ?: "",
            artistId,
            artistName ?: "",
            composer ?: "",
            albumArtist ?: ""
        )
    }


}