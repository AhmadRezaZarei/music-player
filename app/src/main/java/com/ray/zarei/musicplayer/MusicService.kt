package com.ray.zarei.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.*
import android.service.media.MediaBrowserService
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.ray.zarei.musicplayer.extensions.getTintedDrawable
import com.ray.zarei.musicplayer.utils.VersionUtils


class MusicService : MediaBrowserService(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

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

    inner class MusicBinder : Binder() {
        fun getService() = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer()
        initMusicPlayer()
    }

    private fun initMusicPlayer() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnErrorListener(this)
        player.setOnCompletionListener(this)
    }


    fun playSong() {

        player.reset()

        val song = songs[songPosition]

        val currentSongId = song.id

        val trackUri: Uri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currentSongId
        )

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

    fun pause() {

        if (player.isPlaying) {
            player.pause()
        }

    }
    fun pauseOrPauseSong() {

        if (player.isPlaying) {
            player.pause()
        } else {
            player.start()
        }
    }

    fun playNext() {


        if (songPosition == (songs.size - 1)) {
            songPosition = 0
        } else {
            songPosition++
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

    override fun onGetRoot(
        clientPackageName: String,
        clientId: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return null
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {

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

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()

        createNotificationChannel()

        startForeground(12, getNotification(songs[songPosition]))
        Log.e("MusicService", "started forground")

    }


    private fun buildPendingIntent(
        context: Context,
        action: String,
        serviceName: ComponentName?,
    ): PendingIntent {
        val intent = Intent(action)
        intent.component = serviceName
        return PendingIntent.getService(
            context, 0, intent, if (VersionUtils.hasMarshmallow())
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )
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

            val smallIcon =
                applicationContext.getTintedDrawable(R.drawable.ic_notification, tintColor)
                    .toBitmap()
            setImageViewBitmap(R.id.iv_small_icon, smallIcon)

            val prev = applicationContext.getTintedDrawable(R.drawable.ic_skip_previous, tintColor)
                .toBitmap()
            setImageViewBitmap(R.id.ib_action_prev, prev)

            val pause =
                applicationContext.getTintedDrawable(R.drawable.ic_pause_white_48dp, tintColor)
                    .toBitmap()
            setImageViewBitmap(R.id.ib_action_play_pause, pause)

            val next =
                applicationContext.getTintedDrawable(R.drawable.ic_skip_next, tintColor).toBitmap()
            setImageViewBitmap(R.id.ib_action_next, next)

            val close =
                applicationContext.getTintedDrawable(R.drawable.ic_close, tintColor).toBitmap()
            setImageViewBitmap(R.id.ib_action_quit, close)

            setImageViewUri(R.id.iv_song_cover, songs[songPosition].getCoverUri())


            val serviceName = ComponentName(applicationContext, MusicService::class.java)

            val pausedPendingIntent =
                buildPendingIntent(applicationContext, ACTION_PAUSE, serviceName)
            setOnClickPendingIntent(R.id.ib_action_play_pause, pausedPendingIntent)

            val nextPendingIndent = buildPendingIntent(applicationContext, ACTION_NEXT, serviceName)
            setOnClickPendingIntent(R.id.ib_action_next, nextPendingIndent)

            val previousPendingIndent =
                buildPendingIntent(applicationContext, ACTION_PREVIOUSE, serviceName)
            setOnClickPendingIntent(R.id.ib_action_prev, previousPendingIndent)

            val closePendingIndex =
                buildPendingIntent(applicationContext, ACTION_CLOSE, serviceName)
            setOnClickPendingIntent(R.id.ib_action_quit, closePendingIndex)

        }


// Apply the layouts to the notification
        val customNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setCustomBigContentView(notificationLayout)
            .setOngoing(true)
            .setCustomContentView(notificationLayout)
            .build()
        return customNotification
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {

            when (it.action) {

                ACTION_PAUSE -> {

                    pauseOrPauseSong()

                }

                ACTION_NEXT -> {

                    Log.e("MusicService", "onStartCommand: action next called")
                    playNext()

                }

                ACTION_PREVIOUSE -> {

                    playPrev()

                }

                ACTION_CLOSE -> {
                    player.release()
                    stopForeground(true)
                }

                else -> {

                }
            }

        }



        return super.onStartCommand(intent, flags, startId)
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

    companion object {
        val ACTION_PAUSE = "action_pause"
        val ACTION_PREVIOUSE = "action_previous"
        val ACTION_NEXT = "action_next"
        val ACTION_CLOSE = "action_close"
    }


}