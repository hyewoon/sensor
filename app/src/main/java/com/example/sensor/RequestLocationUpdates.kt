package com.example.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sensor.databinding.ActivityRequestLocationUpdatesBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestLocationUpdates : AppCompatActivity() {

    private lateinit var binding: ActivityRequestLocationUpdatesBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    //이전 위치
    private var lastLocation: Location? = null
    //누적 거리
    var total_distance : Float = 0.0F
    //누적 시간
    var total_time : Long = 0

    override fun onStart() {
        super.onStart()
        RequestPermissionsUtil(this).requestLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestLocationUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermission()

    }

    override fun onResume() {
        super.onResume()
         requestLocationUpdates()
    }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestLocationUpdates()
        }
    }
    private fun requestLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update interval in milliseconds
            fastestInterval = 5000 // Fastest update interval in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // Looper can be provided for custom threading
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            //lcoation = 현재 위치
            val location = locationResult.lastLocation
            //현재시간
            val timestamp = location.time

            if (lastLocation != null) {
                //두 지점 간의 거리 측정 단위 : meter
                val distance = lastLocation?.distanceTo(location)
                val lastTime =  lastLocation!!.time

                val time = (timestamp - lastTime!!)/1000

                val beforeTime = SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.getDefault()).format(Date(
                    lastTime!!
                ))
                val nowTime = SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.getDefault()).format(Date(timestamp))


                binding.distance.text= "이동거리"+ String.format("%.2f", distance)+ "m"
                binding.lastTime.text = "이전 시간 : ${beforeTime}"
                binding.currentTime.text = "현재 시간 : ${nowTime}"
                binding.duration.text = "이동 시간 : ${time} 초 "

                distance?.let {
                    total_distance += it
                }
                total_time += time

                // 변환
                var sec = total_time % 60
                var min = total_time / 60 % 60
                var hour = total_time/3600

                //미터를  킬로미터로 변환
                var distanceKm = total_distance /1000

                binding.totalDistance.text = "전체이동거리" + String.format("%.3f", distanceKm)+ "km"
                binding.totalTime.text = "전체이동시간 : ${hour}시 + ${min}분 + ${sec}초"
            }
            lastLocation = location
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                // Handle permission denied
            }
        }
    }


}