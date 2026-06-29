package com.example.fitnessapp.data.network

import com.example.fitnessapp.data.model.VideoWorkout
import com.example.fitnessapp.data.model.Workout
import retrofit2.http.GET
import retrofit2.http.Query

interface FitnessApiService {

    @GET("get_workouts")
    suspend fun getWorkouts(): List<Workout>

    @GET("get_video")
    suspend fun getVideo(@Query("id") id: Int): VideoWorkout
}
