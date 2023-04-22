package com.ray.zarei.musicplayer.di.modules

import com.ray.zarei.musicplayer.repositories.DefaultSongsRepository
import com.ray.zarei.musicplayer.repositories.SongsRepository
import dagger.Binds
import dagger.Module


@Module
abstract class SongsRepositoryModule {

    @Binds
    abstract fun bindSongsRepository(repo: DefaultSongsRepository): SongsRepository

}
