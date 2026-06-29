package com.example.fitnessapp.domain.usecase

import com.example.fitnessapp.data.model.Resource
import com.example.fitnessapp.data.model.Workout
import com.example.fitnessapp.data.repository.WorkoutRepository

class GetWorkoutsUseCase(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): Resource<List<Workout>> {
        return repository.getWorkouts()
    }
}
