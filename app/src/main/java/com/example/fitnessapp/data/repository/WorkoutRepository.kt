package com.example.fitnessapp.data.repository

import com.example.fitnessapp.data.model.Resource
import com.example.fitnessapp.data.model.VideoWorkout
import com.example.fitnessapp.data.model.Workout
import com.example.fitnessapp.data.network.FitnessApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepository(
    private val apiService: FitnessApiService
) {
    companion object {
        private const val BASE_MEDIA_URL = "https://ref.test.kolsa.ru/"
    }

    suspend fun getWorkouts(): Resource<List<Workout>> {
        return withContext(Dispatchers.IO) {
            try {
                val workouts = apiService.getWorkouts()
                Resource.Success(workouts)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Ошибка загрузки тренировок", e)
            }
        }
    }

    suspend fun getVideo(id: Int): Resource<VideoWorkout> {
        return withContext(Dispatchers.IO) {
            try {
                val video = apiService.getVideo(id)

                val fullLink = buildFullUrl(video.link)
                Resource.Success(video.copy(link = fullLink))
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Ошибка загрузки видео", e)
            }
        }
    }

    private fun buildFullUrl(link: String): String {
        return if (link.startsWith("https://") || link.startsWith("https://")) {
            link
        } else {
            BASE_MEDIA_URL + link.trimStart('/')
        }
    }
}
