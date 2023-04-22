package com.ray.zarei.musicplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set


object MusicPlayerRemote  {
    val TAG: String = MusicPlayerRemote::class.java.simpleName
    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()

    var musicService: MusicService? = null


    lateinit var songs: ArrayList<Song>


    val isServiceConnected: Boolean
        get() = musicService != null



    fun bindToService(context: Context, songs: ArrayList<Song>, callback: ServiceConnection): ServiceToken? {

        val realActivity = (context as Activity).parent ?: context
        val contextWrapper = ContextWrapper(realActivity)
        val intent = Intent(contextWrapper, MusicService::class.java)

        this.songs = songs

        // https://issuetracker.google.com/issues/76112072#comment184
        // Workaround for ForegroundServiceDidNotStartInTimeException
        try {
            context.startService(intent)
        } catch (e: Exception) {
            ContextCompat.startForegroundService(context, intent)
        }

        val binder = ServiceBinder(callback)

        if (contextWrapper.bindService(
                Intent().setClass(contextWrapper, MusicService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            mConnectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            musicService = null
        }
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }


    fun playSong() {
        musicService?.playSong()
    }

    fun pauseSong() {
        musicService?.pause()
    }

    /**
     * Async
     */
    fun playNextSong() {
        musicService?.playNext()
    }

    /**
     * Async
     */
    fun playPreviousSong() {
        musicService?.playPrev()
    }


    class ServiceBinder internal constructor(private val mCallback: ServiceConnection?) :
        ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            musicService?.setList(songs)
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            musicService = null
        }
    }

    class ServiceToken internal constructor(internal var mWrappedContext: ContextWrapper)
}
