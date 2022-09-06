package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

interface PostApi {
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("posts/{id}")
    fun delete(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/likes")
    fun like(@Path("id") id: Long): Call<Post>

    @DELETE("posts/{id}/likes")
    fun unlike(@Path("id") id: Long): Call<Post>
}

object PostApiHolder {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .let {
            if (BuildConfig.DEBUG) {
                it.addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            } else {
                it
            }
        }
        .addInterceptor { chain ->
            chain.proceed(
                chain.request()
            ).also { response ->
                if (!response.isSuccessful) {
                    when (response.code) {
                        in 400..499 -> throw IOException("Client error")
                        in 500..599 -> throw IOException("Server error")
                    }
                }
                if (response.body == null) throw RuntimeException("body is null")
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .build()

    val api: PostApi by lazy {
        retrofit.create()
    }
}