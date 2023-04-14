package com.ray.zarei.musicplayer

import android.Manifest
import android.app.*
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private val NOTIFY_ID = 1

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

    fun playPrev() {
        songPosition--
        if (songPosition < 0) {
            songPosition = (songs.size - 1).toLong()
        }
        playSong()
    }

    fun playNext() {
        songPosition++

        if (songPosition == (songs.size - 1).toLong()) {
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

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()


        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        createNotificationChannel()

        val songTitle = songs[songPosition.toInt()].title

        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Content title")
            .setContentText("Content text blah blah" + songTitle)
            .setContentIntent(pendInt)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        val not = builder.build()


        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MusicService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
              //  Toast.makeText(this@MusicService, " Needs permission", Toast.LENGTH_LONG).show()

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(123, builder.build())
        }

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