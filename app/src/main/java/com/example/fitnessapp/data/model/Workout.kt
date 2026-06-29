package com.example.fitnessapp.data.model

import com.google.gson.annotations.SerializedName

data class Workout(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: Int,
    @SerializedName("duration") val duration: String
) {
    companion object {
        const val TYPE_WORKOUT = 1
        const val TYPE_LIVE = 2
        const val TYPE_COMPLEX = 3

        fun getTypeName(type: Int): String = when (type) {
            TYPE_WORKOUT -> "Тренировка"
            TYPE_LIVE -> "Эфир"
            TYPE_COMPLEX -> "Комплекс"
            else -> "Неизвестно"
        }
    }
}
