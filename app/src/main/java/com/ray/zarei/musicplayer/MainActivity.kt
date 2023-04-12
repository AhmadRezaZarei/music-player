package com.ray.zarei.musicplayer


import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.widget.MediaController.MediaPlayerControl
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ray.zarei.musicplayer.adapters.SongsRecyclerViewAdapter
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), MediaPlayerControl {


    lateinit var songs: ArrayList<Song>

    lateinit var controller: MusicController

    var musicService: MusicService? = null

    var playIntent: Intent? = null

    var musicBound = false

    fun setController() {
        controller = MusicController(this)
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.rc));
        controller.isEnabled = true;

    }

    override fun onStart() {
        super.onStart()

        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }

    }

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        getSongs()
        setupRecyclerView()

    }


    private val musicConnection = object: ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            musicService?.setList(songs)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicBound = false
        }

    }


    fun setupController() {

        setController()

        controller.setPrevNextListeners({ v -> // next
            playNext()
        }, { v -> // prev
            playPrev()
        })

    }

    fun playNext() {

    }

    fun playPrev() {

    }

    fun setupRecyclerView() {
        songs.sortWith { s1, s2 -> s1.title.compareTo(s2.title) }
        val rc = findViewById<RecyclerView>(R.id.rc)

        val adapter = SongsRecyclerViewAdapter(songs) { clickedSongIndex ->

            musicService?.setSong(clickedSongIndex)
            musicService?.playSong()

        }
        rc.adapter = adapter

    }

    private fun getSongs() {

        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)


        if (musicCursor != null && musicCursor.moveToFirst()) {

            val titleColumn =
                musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn =
                musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)

            songs = ArrayList()

            do {

                songs.add(
                    Song(
                        id = musicCursor.getLong(idColumn),
                        title = musicCursor.getString(titleColumn),
                        artist = musicCursor.getString(artistColumn)
                    )
                )

            } while (musicCursor.moveToNext())


        }


    }


    override fun onDestroy() {
        stopService(playIntent)
        musicService = null
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {


        }

        return super.onOptionsItemSelected(item)
    }

    override fun start() {

    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun getDuration(): Int {
        TODO("Not yet implemented")
    }

    override fun getCurrentPosition(): Int {
        TODO("Not yet implemented")
    }

    override fun seekTo(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getBufferPercentage(): Int {
        TODO("Not yet implemented")
    }

    override fun canPause(): Boolean {
        TODO("Not yet implemented")
    }

    override fun canSeekBackward(): Boolean {
        TODO("Not yet implemented")
    }

    override fun canSeekForward(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAudioSessionId(): Int {
        TODO("Not yet implemented")
    }
}