package com.example.uploadimagetoserver.network

import com.example.uploadimagetoserver.models.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


//Here we create our retrofit interface
interface ApiCall {
    @Multipart
    @POST("upload")
    //This function here will upload user selected image to the backend server
    fun uploadImage(
        @Part image: MultipartBody.Part, //File to send to the Api
        @Part("multipart/form-data") desc: RequestBody): Call<UploadResponse>

    companion object {
        operator fun invoke(): ApiCall {
            return Retrofit.Builder()
                .baseUrl("https://darot-image-upload-service.herokuapp.com/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiCall::class.java)
        }
    }
}
