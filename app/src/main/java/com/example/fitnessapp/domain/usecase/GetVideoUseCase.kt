package com.example.fitnessapp.domain.usecase

import com.example.fitnessapp.data.model.Resource
import com.example.fitnessapp.data.model.VideoWorkout
import com.example.fitnessapp.data.repository.WorkoutRepository

class GetVideoUseCase(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(id: Int): Resource<VideoWorkout> {
        return repository.getVideo(id)
    }
}
