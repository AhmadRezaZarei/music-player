package com.ray.zarei.musicplayer


import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.widget.MediaController.MediaPlayerControl
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ray.zarei.musicplayer.adapters.SongsRecyclerViewAdapter
import java.util.*


class MainActivity : AppCompatActivity(), MediaPlayerControl {


    lateinit var songs: ArrayList<Song>

    lateinit var controller: MusicController

    var songPosition = 0;

    var musicService: MusicService? = null

    var playIntent: Intent? = null

    var musicBound = false

    fun setController() {
        controller = MusicController(this)
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.rc));
        controller.isEnabled = true;

        controller.setPrevNextListeners({ v -> // next
            playNext()
        }, { v -> // prev
            playPrev()
        })

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

        setController()
    }


    private val musicConnection = object : ServiceConnection {

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


    fun playNext() {
        musicService?.playNext()
        controller.show(0)
    }

    fun playPrev() {
        musicService?.playPrev()
        controller.show(0)
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

        when (item.itemId) {


        }

        return super.onOptionsItemSelected(item)
    }


    override fun start() {
        musicService?.go()
    }

    override fun pause() {
        musicService?.pausePlayer()
    }

    override fun getDuration(): Int {


        musicService?.let {

            if (musicBound && it.isPng()) {
                return it.getDur()
            }
        }

        return 0
    }

    override fun getCurrentPosition(): Int {

        musicService?.let {

            if (musicBound && it.isPng()) {
                return it.getPosn()
            }

        }

        return 0

    }

    override fun seekTo(position: Int) {
        musicService?.seek(position)
    }

    override fun isPlaying(): Boolean {

        musicService?.let {

            if (musicBound) {
                return it.isPng()
            }

        }

        return false
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        return 0
    }
}