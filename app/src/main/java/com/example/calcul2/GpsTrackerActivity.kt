package com.example.calcul2

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GpsTrackerActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvTime: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val UPDATE_INTERVAL = 10000L
    private var isTracking = false

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startTracking()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_tracker)

        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvTime = findViewById(R.id.tvTime)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (hasLocationPermission()) {
            startTracking()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (isTracking) return
        isTracking = true

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = UPDATE_INTERVAL
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    locationResult.lastLocation?.let {
                        updateLocationUI(it)
                        saveLocationData(it)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun updateLocationUI(location: Location) {
        runOnUiThread {
            tvLatitude.text = "Широта: ${"%.6f".format(location.latitude)}"
            tvLongitude.text = "Долгота: ${"%.6f".format(location.longitude)}"
            tvTime.text = "Время GPS: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(location.time))}"

        }
    }

    private fun saveLocationData(location: Location) {
        try {
            val locationData = JSONObject().apply {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(location.time)))
            }

            val file = File(getExternalFilesDir(null), "locations.json")
            val jsonArray = if (file.exists()) JSONArray(file.readText()) else JSONArray()
            jsonArray.put(locationData)
            file.writeText(jsonArray.toString())

        } catch (e: Exception) {
            android.util.Log.e("GPS", "Ошибка сохранения данных", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isTracking = false
        handler.removeCallbacksAndMessages(null)
        fusedLocationClient.removeLocationUpdates(object : com.google.android.gms.location.LocationCallback() {})
    }
}