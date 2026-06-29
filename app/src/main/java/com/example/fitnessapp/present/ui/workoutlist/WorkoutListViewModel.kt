package com.example.fitnessapp.present.ui.workoutlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.model.Resource
import com.example.fitnessapp.data.model.Workout
import com.example.fitnessapp.data.network.RetrofitClient
import com.example.fitnessapp.data.repository.WorkoutRepository
import com.example.fitnessapp.domain.usecase.GetWorkoutsUseCase
import kotlinx.coroutines.launch

class WorkoutListViewModel : ViewModel() {

    private val repository = WorkoutRepository(RetrofitClient.apiService)
    private val getWorkoutsUseCase = GetWorkoutsUseCase(repository)

    private val _workouts = MutableLiveData<Resource<List<Workout>>>()
    val workouts: LiveData<Resource<List<Workout>>> = _workouts

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _selectedType = MutableLiveData<Int?>(null)
    val selectedType: LiveData<Int?> = _selectedType

    private var allWorkouts: List<Workout> = emptyList()

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _workouts.value = Resource.Loading
            val result = getWorkoutsUseCase()
            if (result is Resource.Success) {
                allWorkouts = result.data
                filterWorkouts()
            } else {
                _workouts.value = result
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterWorkouts()
    }

    fun setSelectedType(type: Int?) {
        _selectedType.value = type
        filterWorkouts()
    }

    private fun filterWorkouts() {
        val query = _searchQuery.value?.lowercase() ?: ""
        val type = _selectedType.value

        val filtered = allWorkouts.filter { workout ->
            val matchesQuery = query.isEmpty() || workout.title.lowercase().contains(query)
            val matchesType = type == null || workout.type == type
            matchesQuery && matchesType
        }

        _workouts.value = Resource.Success(filtered)
    }
}
