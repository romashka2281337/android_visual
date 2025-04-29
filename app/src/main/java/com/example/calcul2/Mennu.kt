package com.example.calcul2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Mennu : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<Button>(R.id.button_calculator).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.button_mp3_player).setOnClickListener {
            checkAudioPermission()
        }
    }

    private fun checkAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                startMp3Player()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                Toast.makeText(this, "Для работы MP3-плеера необходимо разрешение", Toast.LENGTH_LONG).show()
                requestAudioPermission(permission)
            }
            else -> {
                requestAudioPermission(permission)
            }
        }
    }

    private fun requestAudioPermission(permission: String) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun startMp3Player() {
        startActivity(Intent(this, Mp3PlayerActivity::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMp3Player()
            } else {
                Toast.makeText(this, "Доступ к аудиофайлам запрещен", Toast.LENGTH_SHORT).show()
            }
        }
    }
}