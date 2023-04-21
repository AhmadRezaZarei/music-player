package com.ray.zarei.musicplayer

import android.app.Application
import com.ray.zarei.musicplayer.di.AppComponent
import com.ray.zarei.musicplayer.di.DaggerAppComponent

class AppApplication: Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().build()

    }

}