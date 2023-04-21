package com.ray.zarei.musicplayer.di.modules

import com.ray.zarei.musicplayer.api.MainApiService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {


    @Provides
    fun provideAuthApiService(): MainApiService {
        return Retrofit.Builder()
            .baseUrl("http://172.20.176.47:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MainApiService::class.java)
    }

}