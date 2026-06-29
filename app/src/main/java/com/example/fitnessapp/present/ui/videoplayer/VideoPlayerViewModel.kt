package com.example.fitnessapp.present.ui.videoplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.model.Resource
import com.example.fitnessapp.data.model.VideoWorkout
import com.example.fitnessapp.data.network.RetrofitClient
import com.example.fitnessapp.data.repository.WorkoutRepository
import com.example.fitnessapp.domain.usecase.GetVideoUseCase
import kotlinx.coroutines.launch

class VideoPlayerViewModel : ViewModel() {

    private val repository = WorkoutRepository(RetrofitClient.apiService)
    private val getVideoUseCase = GetVideoUseCase(repository)

    private val _video = MutableLiveData<Resource<VideoWorkout>>()
    val video: LiveData<Resource<VideoWorkout>> = _video

    fun loadVideo(id: Int) {
        viewModelScope.launch {
            _video.value = Resource.Loading
            _video.value = getVideoUseCase(id)
        }
    }
}
