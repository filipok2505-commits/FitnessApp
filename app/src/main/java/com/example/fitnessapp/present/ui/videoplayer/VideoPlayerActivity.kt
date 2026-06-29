package com.example.fitnessapp.present.ui.videoplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.fitnessapp.R
import com.example.fitnessapp.data.model.Resource
import com.example.fitnessapp.data.model.Workout
import com.example.fitnessapp.data.network.RetrofitClient
import com.example.fitnessapp.databinding.ActivityVideoPlayerBinding
import com.google.android.material.snackbar.Snackbar

class VideoPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WORKOUT_ID = "workout_id"
        const val EXTRA_WORKOUT_TITLE = "workout_title"
        const val EXTRA_WORKOUT_DESCRIPTION = "workout_description"
        const val EXTRA_WORKOUT_DURATION = "workout_duration"
        const val EXTRA_WORKOUT_TYPE = "workout_type"
        private const val TAG = "VideoPlayer"
        private const val PLAYER_HEIGHT_DP = 300
    }

    private lateinit var binding: ActivityVideoPlayerBinding
    private lateinit var viewModel: VideoPlayerViewModel
    private var player: ExoPlayer? = null
    private var currentSpeed: Float = 1.0f
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        isFullscreen = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isFullscreen) {
            applyFullscreen(true)
        }

        setupViewModel()
        setupUI()
        setupControls()
        loadVideo()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[VideoPlayerViewModel::class.java]
    }

    private fun setupUI() {
        val title = intent.getStringExtra(EXTRA_WORKOUT_TITLE) ?: ""
        val description = intent.getStringExtra(EXTRA_WORKOUT_DESCRIPTION) ?: "Описание отсутствует"
        val duration = intent.getStringExtra(EXTRA_WORKOUT_DURATION) ?: ""
        val type = intent.getIntExtra(EXTRA_WORKOUT_TYPE, 0)

        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.tvDuration.text = "Длительность: $duration"
        binding.tvType.text = "Тип: ${Workout.getTypeName(type)}"

        setupSpeedSpinner()
        setupQualitySpinner()
    }

    private fun setupSpeedSpinner() {
        val speeds = arrayOf("0.5x", "0.75x", "1x", "1.25x", "1.5x", "2x")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, speeds)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSpeed.adapter = adapter
        binding.spinnerSpeed.setSelection(2)

        binding.spinnerSpeed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val speed = when (position) {
                    0 -> 0.5f
                    1 -> 0.75f
                    2 -> 1.0f
                    3 -> 1.25f
                    4 -> 1.5f
                    5 -> 2.0f
                    else -> 1.0f
                }
                currentSpeed = speed
                player?.setPlaybackParameters(PlaybackParameters(speed))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupQualitySpinner() {
        val qualities = arrayOf("Авто", "720p", "480p", "360p")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, qualities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuality.adapter = adapter
    }

    private fun setupControls() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnRewind.setOnClickListener {
            player?.let { it.seekTo((it.currentPosition - 10_000).coerceAtLeast(0)) }
        }

        binding.btnForward.setOnClickListener {
            player?.let { it.seekTo(it.currentPosition + 10_000) }
        }

        binding.btnPlayPause.setOnClickListener { togglePlayPause() }
        binding.btnFullscreen.setOnClickListener { toggleFullscreen() }
    }

    private fun togglePlayPause() {
        player?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            } else {
                exoPlayer.play()
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        if (isFullscreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }


    private fun applyFullscreen(fullscreen: Boolean) {
        if (fullscreen) {

            val playerParams = binding.playerContainer.layoutParams as ViewGroup.MarginLayoutParams
            playerParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            playerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            playerParams.setMargins(0, 0, 0, 0)
            binding.playerContainer.layoutParams = playerParams


            binding.contentScrollView.visibility = View.GONE


            hideSystemBars()


            binding.btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit)

        } else {

            val density = resources.displayMetrics.density
            val heightPx = (PLAYER_HEIGHT_DP * density).toInt()
            val playerParams = binding.playerContainer.layoutParams as ViewGroup.MarginLayoutParams
            playerParams.height = heightPx
            playerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            playerParams.setMargins(0, 0, 0, 0)
            binding.playerContainer.layoutParams = playerParams


            binding.contentScrollView.visibility = View.VISIBLE

            val scrollParams =
                binding.contentScrollView.layoutParams as ViewGroup.MarginLayoutParams
            scrollParams.topMargin = heightPx
            binding.contentScrollView.layoutParams = scrollParams


            showSystemBars()


            binding.btnFullscreen.setImageResource(R.drawable.ic_fullscreen)
        }
    }

    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }

    @Suppress("DEPRECATION")
    private fun showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isFullscreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        applyFullscreen(isFullscreen)
    }


    private fun loadVideo() {
        val workoutId = intent.getIntExtra(EXTRA_WORKOUT_ID, -1)
        if (workoutId == -1) {
            showError("Неверный ID тренировки")
            return
        }

        viewModel.video.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    Log.d(TAG, "🎬 Video URL: ${resource.data.link}")
                    initializePlayer(resource.data.link)
                }

                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message)
                }
            }
        }

        viewModel.loadVideo(workoutId)
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(videoUrl: String) {


        val dataSourceFactory = RetrofitClient.createDataSourceFactory(this)

        player = ExoPlayer.Builder(this).setMediaSourceFactory(
            DefaultMediaSourceFactory(this).setDataSourceFactory(dataSourceFactory)
        ).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            setPlaybackParameters(PlaybackParameters(currentSpeed))
            prepare()
            play()
        }

        binding.playerView.player = player
        binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.playerView.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.playerView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Повторить") { loadVideo() }.show()
    }

    override fun onStart() {
        super.onStart()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
