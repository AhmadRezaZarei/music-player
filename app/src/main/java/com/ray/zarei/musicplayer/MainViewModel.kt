package com.ray.zarei.musicplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.internal.LinkedTreeMap
import com.ray.zarei.musicplayer.repositories.SongsRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(private val songsRepository: SongsRepository): ViewModel() {

    private val _songsLiveData = MutableLiveData<List<Song>>()

    val songsLiveData: LiveData<List<Song>> = _songsLiveData

    fun getAllSongs() {

        _songsLiveData.value = songsRepository.songs()

    }

}