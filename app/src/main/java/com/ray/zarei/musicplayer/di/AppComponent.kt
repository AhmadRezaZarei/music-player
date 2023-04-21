package com.ray.zarei.musicplayer.di

import com.ray.zarei.musicplayer.api.MainApiService
import com.ray.zarei.musicplayer.di.modules.NetworkModule
import dagger.Component

@Component(modules = [NetworkModule::class])
interface AppComponent {

    fun getMainApiService(): MainApiService

}