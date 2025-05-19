package com.example.calcul2

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Mp3PlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private val songIds = listOf(R.raw.song1, R.raw.song2)
    private var currentSongIndex = 0
    private var isPlaying = false
    private var isCycleEnabled = false
    private var volumeLevel = 0
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var timeCurrent: TextView
    private lateinit var timeTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mp3)

        initializeViews()
        initializeMediaPlayer()
    }

    private fun initializeViews() {
        val trackTitle = findViewById<TextView>(R.id.track_title)
        val btnPlay = findViewById<Button>(R.id.btn_play)
        val btnPrev = findViewById<Button>(R.id.btn_prev)
        val btnNext = findViewById<Button>(R.id.btn_next)
        val cbCycle = findViewById<CheckBox>(R.id.cb_cycle)
        val sbProgress = findViewById<SeekBar>(R.id.sb_progress)
        val sbVolume = findViewById<SeekBar>(R.id.sb_volume)

        timeCurrent = findViewById(R.id.time_current)
        timeTotal = findViewById(R.id.time_total)

        btnPlay.setOnClickListener {
            if (isPlaying) {
                pauseMedia()
            } else {
                playMedia()
            }
        }

        btnPrev.setOnClickListener {
            previousTrack()
        }

        btnNext.setOnClickListener {
            nextTrack()
        }

        cbCycle.setOnCheckedChangeListener { _, isChecked ->
            isCycleEnabled = isChecked
            mediaPlayer.isLooping = isCycleEnabled
        }

        sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    mediaPlayer.seekTo(progress)

                    // Текущее время в секундах
                    val currentSec = progress / 1000
                    timeCurrent.text = "$currentSec"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    adjustVolume(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, songIds[currentSongIndex])
        updateTrackInfo()

        mediaPlayer.setOnCompletionListener {
            nextTrack()
        }
    }

    private fun playMedia() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.start()
            isPlaying = true
            updateProgressBar()
        }
    }

    private fun pauseMedia() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
    }

    private fun previousTrack() {
        if (currentSongIndex > 0) {
            currentSongIndex--
        } else {
            currentSongIndex = songIds.size - 1
        }
        resetMediaPlayer()
    }

    private fun nextTrack() {
        if (currentSongIndex < songIds.size - 1) {
            currentSongIndex++
        } else {
            currentSongIndex = 0
        }
        resetMediaPlayer()
    }

    private fun resetMediaPlayer() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.reset()
            mediaPlayer = MediaPlayer.create(this, songIds[currentSongIndex])
            playMedia()
            updateTrackInfo()
        }
    }

    private fun updateTrackInfo() {
        val trackTitleView = findViewById<TextView>(R.id.track_title)
        trackTitleView.text = resources.getResourceEntryName(songIds[currentSongIndex])


        val durationSec = mediaPlayer.duration / 1000
        timeTotal.text = "/ $durationSec"

        val sbProgress = findViewById<SeekBar>(R.id.sb_progress)
        sbProgress.max = mediaPlayer.duration
    }

    private fun updateProgressBar() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            val sbProgress = findViewById<SeekBar>(R.id.sb_progress)
            sbProgress.progress = mediaPlayer.currentPosition

            // Текущее время в секундах
            val currentSec = mediaPlayer.currentPosition / 1000
            timeCurrent.text = "$currentSec"

            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (mediaPlayer.isPlaying) {
                    updateProgressBar()
                }
            }, 500)
        }
    }

    private fun adjustVolume(volumeLevel: Int) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volumeLevel.coerceIn(0, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),
            0
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}


