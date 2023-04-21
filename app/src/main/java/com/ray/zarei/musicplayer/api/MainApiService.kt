package com.ray.zarei.musicplayer.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.google.gson.annotations.SerializedName
import retrofit2.Response

interface MainApiService {

    @Multipart
    @POST("upload")
    suspend fun upload(@Part() file: MultipartBody.Part): Response<UploadResponse>

}

data class UploadResponse(
    @SerializedName("error") val error: String?,
    @SerializedName("error_code") val errorCode: Int
)
