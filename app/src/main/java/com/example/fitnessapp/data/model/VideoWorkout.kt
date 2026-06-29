package com.example.fitnessapp.data.model

import com.google.gson.annotations.SerializedName

data class VideoWorkout(
    @SerializedName("id") val id: Int,
    @SerializedName("duration") val duration: String,
    @SerializedName("link") val link: String
)
