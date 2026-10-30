package com.runtrack.data.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.runtrack.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()
                )
            )
            .build()
    }

    val userService: UserService by lazy { retrofit.create(UserService::class.java) }
    val shoeService: ShoeService by lazy { retrofit.create(ShoeService::class.java) }
    val runningService: RunningService by lazy { retrofit.create(RunningService::class.java) }
    val weatherService: WeatherService by lazy { retrofit.create(WeatherService::class.java) }
}
