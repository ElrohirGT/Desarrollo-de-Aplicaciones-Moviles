@file:OptIn(ExperimentalMaterial3Api::class)

package com.uvg.gt.laboratorio11

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.uvg.gt.laboratorio11.ui.theme.Laboratorio11Theme

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var locationCallback: LocationCallback

    private var lastLocationTaskSource: TaskCompletionSource<Location?> = TaskCompletionSource()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("GetLocation", "All location permissions granted!")
                    fusedLocationClient.lastLocation.addOnCompleteListener {
                        if (it.isSuccessful) {
                            lastLocationTaskSource.setResult(it.result)
                        }
                    }
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("GetLocation", "Permissions granted only to coarse location")
                    fusedLocationClient.lastLocation.addOnCompleteListener {
                        if (it.isSuccessful) {
                            lastLocationTaskSource.setResult(it.result)
                        }
                    }
                }

                else -> {
                    Log.e("GetLocation", "No permissions granted...")
                    lastLocationTaskSource.setResult(null)
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations.reversed()) {
                    lastLocationTaskSource.trySetResult(location)
                    break;
                }
            }
        }


        setContent {
            Laboratorio11Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainComponent({ checkForPermissions() }, { lastLocationTaskSource.task })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            createLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
    }

    private fun checkForPermissions() {
        lastLocationTaskSource = TaskCompletionSource()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            fusedLocationClient.lastLocation.addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.d("GetLocation", "No Location received!")
                }
            }
        }
    }
}

@Composable
fun MainComponent(
    checkPermissions: () -> Unit,
    getLocation: () -> Task<Location?>,
    modifier: Modifier = Modifier
) {
    val (latitude, setLatitude) = remember { mutableStateOf(0.0) }
    val (longitude, setLongitude) = remember { mutableStateOf(0.0) }
    val (isLoading, setIsLoading) = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Accediendo a la Ubicación")
        Text("LATITUD: $latitude")
        Text("LONGITUD: $longitude")
        Button(onClick = {
            setIsLoading(true)
            checkPermissions()

            getLocation().addOnCompleteListener {
                setIsLoading(false)
                if (!it.isSuccessful) {
                    Log.e("ButtonClick", "Error getting the location!")
                    return@addOnCompleteListener
                }

                val location = it.result!!
                setLatitude(location.latitude)
                setLongitude(location.longitude)
            }
        }, enabled = !isLoading) {
            Text("¡Get location!")
        }
    }
}