package com.example.fitnessapp.data.network

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://ref.test.kolsa.ru/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS).build()

    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()).build()

    val apiService: FitnessApiService = retrofit.create(FitnessApiService::class.java)

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(UnstableApi::class)
    fun createDataSourceFactory(context: Context): DefaultDataSource.Factory {
        val httpDataSourceFactory =
            DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(30_000).setReadTimeoutMs(30_000).setUserAgent("FitnessApp/1.0")

        return DefaultDataSource.Factory(context, httpDataSourceFactory)
    }
}
