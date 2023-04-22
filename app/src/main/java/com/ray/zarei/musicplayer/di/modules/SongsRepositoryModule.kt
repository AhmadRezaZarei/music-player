package com.ray.zarei.musicplayer.di.modules

import android.content.Context
import com.ray.zarei.musicplayer.repositories.DefaultSongsRepository
import com.ray.zarei.musicplayer.repositories.SongsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class SongsRepositoryModule(private val context: Context) {

    @Provides
    fun provideSongRepository(): SongsRepository {
        return DefaultSongsRepository(context)
    }

}
