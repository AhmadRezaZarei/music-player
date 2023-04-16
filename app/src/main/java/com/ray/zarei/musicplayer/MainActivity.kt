package com.ray.zarei.musicplayer


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.MediaController.MediaPlayerControl
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.ray.zarei.musicplayer.adapters.SongsRecyclerViewAdapter
import com.ray.zarei.musicplayer.extensions.getLong
import com.ray.zarei.musicplayer.extensions.getString
import com.ray.zarei.musicplayer.extensions.getStringOrNull
import com.ray.zarei.musicplayer.extensions.getInt
import java.util.*

import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), MediaPlayerControl {

    var songs: ArrayList<Song> = ArrayList()

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

    val CHANNEL_ID = "channel_id"

    @SuppressLint("RemoteViewLayout")
    private fun getNotification(): Notification {
// Get the layouts to use in the custom notification
        val notificationLayout = RemoteViews(packageName, R.layout.layout_notification)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.layout_notification)
// Apply the layouts to the notification
        var customNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .build()

        customNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("My notification")
            .setContentText("Hello World!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true).build()

        customNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .build()

        return customNotification
    }

    fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "channel_name"
            val descriptionText = "description text"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }

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
        val cursor = musicResolver.query(musicUri, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {

            songs = ArrayList()

            do {


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

                val song = Song(
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

                songs.add(song)

            } while (cursor.moveToNext())


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