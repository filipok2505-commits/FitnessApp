package com.example.fitnessapp.present.ui.workoutlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.data.model.Resource
import com.example.fitnessapp.data.model.Workout
import com.example.fitnessapp.databinding.ActivityWorkoutListBinding
import com.example.fitnessapp.present.ui.videoplayer.VideoPlayerActivity
import com.google.android.material.snackbar.Snackbar

class WorkoutListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutListBinding
    private lateinit var viewModel: WorkoutListViewModel
    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupSearchView()
        setupFilterSpinner()
        observeData()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[WorkoutListViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = WorkoutAdapter { workout ->
            openVideoPlayer(workout)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@WorkoutListActivity)
            adapter = this@WorkoutListActivity.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupFilterSpinner() {
        val types = arrayOf("Все типы", "Тренировка", "Эфир", "Комплекс")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val type = when (position) {
                    0 -> null
                    1 -> Workout.TYPE_WORKOUT
                    2 -> Workout.TYPE_LIVE
                    3 -> Workout.TYPE_COMPLEX
                    else -> null
                }
                viewModel.setSelectedType(type)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setSelectedType(null)
            }
        }
    }

    private fun observeData() {
        viewModel.workouts.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> showWorkouts(resource.data)
                is Resource.Error -> showError(resource.message)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
    }

    private fun showWorkouts(workouts: List<Workout>) {
        binding.progressBar.visibility = View.GONE

        if (workouts.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmpty.text = "Тренировки не найдены"
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
            adapter.submitList(workouts)
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvEmpty.text = "Ошибка: $message"

        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).setAction("Повторить") {
            viewModel.loadWorkouts()
        }.show()
    }

    private fun openVideoPlayer(workout: Workout) {
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra(VideoPlayerActivity.EXTRA_WORKOUT_ID, workout.id)
            putExtra(VideoPlayerActivity.EXTRA_WORKOUT_TITLE, workout.title)
            putExtra(VideoPlayerActivity.EXTRA_WORKOUT_DESCRIPTION, workout.description)
            putExtra(VideoPlayerActivity.EXTRA_WORKOUT_DURATION, workout.duration)
            putExtra(VideoPlayerActivity.EXTRA_WORKOUT_TYPE, workout.type)
        }
        startActivity(intent)
    }
}
