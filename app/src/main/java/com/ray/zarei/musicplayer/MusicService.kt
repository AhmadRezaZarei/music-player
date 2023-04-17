package com.ray.zarei.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.ray.zarei.musicplayer.extensions.getTintedDrawable


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private val NOTIFY_ID = 1

    private val musicBind: IBinder = MusicBinder()

    lateinit var player: MediaPlayer

    lateinit var songs: ArrayList<Song>

    var songPosition: Int = 0

    fun setList(songs: ArrayList<Song>) {
        this.songs = songs
    }


    fun setSong(songPosition: Int) {
        this.songPosition = songPosition
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

    fun playPrev() {
        songPosition--
        if (songPosition < 0) {
            songPosition = (songs.size - 1)
        }
        playSong()
    }

    fun playNext() {
        songPosition++

        if (songPosition == (songs.size - 1)) {
            songPosition = 0
        }

        playSong()

    }

    override fun onBind(intent: Intent): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.release()
        return false
    }

    val CHANNEL_ID = "channel_id"

    fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "channel_name"
            val descriptionText = "description text"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()

        createNotificationChannel()

        startForeground(12, getNotification(songs[songPosition]))
        Log.e("MusicService", "started forground" )

    }

    @SuppressLint("RemoteViewLayout")
    private fun getNotification(song: Song): Notification {

// Get the layouts to use in the custom notification
        val notificationLayout = RemoteViews(packageName, R.layout.layout_notification)
//        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.layout_notification)

        notificationLayout.apply {
            setTextViewText(R.id.tv_title, song.title)
            setTextViewText(R.id.tv_subtitle, song.artistName)
            setImageViewResource(R.id.iv_song_cover, R.drawable.ic_launcher_background)
            setTextViewText(R.id.tv_app_name, "Music Player")


            val tintColor = applicationContext.resources.getColor(R.color.black)

             val smallIcon = applicationContext.getTintedDrawable(R.drawable.ic_notification, tintColor).toBitmap()
             setImageViewBitmap(R.id.iv_small_icon, smallIcon)

            val prev = applicationContext.getTintedDrawable(R.drawable.ic_skip_previous, tintColor).toBitmap()
            setImageViewBitmap(R.id.iv_action_prev, prev)

            val pause = applicationContext.getTintedDrawable(R.drawable.ic_pause_white_48dp, tintColor).toBitmap()
            setImageViewBitmap(R.id.iv_action_play_pause, pause)

            val next = applicationContext.getTintedDrawable(R.drawable.ic_skip_next, tintColor).toBitmap()
            setImageViewBitmap(R.id.iv_action_next,next)

            val close = applicationContext.getTintedDrawable(R.drawable.ic_close, tintColor).toBitmap()
            setImageViewBitmap(R.id.iv_action_quit, close)

            setImageViewUri(R.id.iv_song_cover, songs[songPosition].getCoverUri())

        }


// Apply the layouts to the notification
        val customNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            . setCustomBigContentView(notificationLayout)
            .setOngoing(true)
            .setCustomContentView(notificationLayout)
            .build()
        return customNotification
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }

    override fun onCompletion(p0: MediaPlayer?) {

    }

    fun getPosn(): Int {
        return player.getCurrentPosition()
    }

    fun getDur(): Int {
        return player.getDuration()
    }

    fun isPng(): Boolean {
        return player.isPlaying()
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }


}