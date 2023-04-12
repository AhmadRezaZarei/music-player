package com.ray.zarei.musicplayer

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private val musicBind: IBinder = MusicBinder()

    lateinit var player: MediaPlayer

    lateinit var songs: ArrayList<Song>

    var songPosition: Long = 0

    fun setList(songs: ArrayList<Song>) {
        this.songs = songs
    }


    fun setSong(songPosition: Int) {
        this.songPosition = songPosition.toLong()
    }

    inner class MusicBinder: Binder() {
        fun getService() = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer()
        initMusicPlayer()
    }

    fun initMusicPlayer() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnErrorListener(this)
        player.setOnCompletionListener(this)
    }


    fun playSong() {
        player.reset()

        val song = songs[songPosition.toInt()]

        val currentSongId = song.id

        val trackUri: Uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongId)


        try {
            player.setDataSource(applicationContext, trackUri)
            player.prepareAsync()
        } catch (e: java.lang.Exception) {
            Log.e("MusicService", "Error setting data source", e)
        }




    }

    override fun onBind(intent: Intent): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.release()
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }

    override fun onCompletion(p0: MediaPlayer?) {

    }

}