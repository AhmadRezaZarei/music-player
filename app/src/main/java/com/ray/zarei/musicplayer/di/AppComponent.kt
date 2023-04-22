package com.ray.zarei.musicplayer.di

import com.ray.zarei.musicplayer.MainActivity
import com.ray.zarei.musicplayer.api.MainApiService
import com.ray.zarei.musicplayer.di.modules.NetworkModule
import com.ray.zarei.musicplayer.di.modules.SongsRepositoryModule
import com.ray.zarei.musicplayer.di.modules.ViewModelModule
import dagger.Component

@Component(modules = [NetworkModule::class, SongsRepositoryModule::class, ViewModelModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)

}