package com.ray.zarei.musicplayer


import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.MediaController
import android.widget.MediaController.MediaPlayerControl
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ray.zarei.musicplayer.adapters.SongsRecyclerViewAdapter
import com.ray.zarei.musicplayer.api.MainApiService
import com.ray.zarei.musicplayer.extensions.getInt
import com.ray.zarei.musicplayer.extensions.getLong
import com.ray.zarei.musicplayer.extensions.getString
import com.ray.zarei.musicplayer.extensions.getStringOrNull
import com.ray.zarei.musicplayer.fragments.PlayingFragment
import com.ray.zarei.musicplayer.utils.AudioUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var mainApiService: MainApiService

    var songs: ArrayList<Song> = ArrayList()

    lateinit var controller: MediaController

    var serviceToken: MusicPlayerRemote.ServiceToken? = null

    var songPosition = 0;

    var musicBound = false


    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as AppApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.songsLiveData.observe(this) {

            setupRecyclerView(it)

            this.serviceToken = MusicPlayerRemote.bindToService(this, ArrayList(it), object : ServiceConnection {
                override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {

                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                }
            })

        }

  //      val inputUri = songs[1].data;
    //    val outputUri = "/storage/emulated/0/Download/editdedddd.mp3"

//        AudioUtils.trim(inputUri, outputUri, 0, 10)






        viewModel.getAllSongs()

    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadMusic() {

        GlobalScope.launch {


            val file = File("/storage/emulated/0/Download/editdedddd.mp3")

            val requestFile = RequestBody.create(
                MediaType.parse("audio/mpeg"),
                file
            )

            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)


            val response = mainApiService.upload(body)


        }


    }


    fun setupRecyclerView(songs: List<Song>) {

        val rc = findViewById<RecyclerView>(R.id.rc)

        val adapter = SongsRecyclerViewAdapter(ArrayList(songs)) { clickedSongIndex ->

            openFragment(clickedSongIndex)

            MusicPlayerRemote.playSong()
        }
        rc.adapter = adapter

    }

    private fun openFragment(clickedSongIndex: Int) {
        val fragment = PlayingFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commit();

    }

    private fun getSongs() {

        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
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
        super.onDestroy()
        MusicPlayerRemote.unbindFromService(serviceToken)
    }

}