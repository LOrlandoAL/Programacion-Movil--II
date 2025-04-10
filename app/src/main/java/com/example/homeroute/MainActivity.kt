package com.example.homeroute

import kotlinx.coroutines.tasks.await
import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.homeroute.viewmodel.MapViewModel
import com.example.homeroute.viewmodel.MapViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicita permisos
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )

        setContent {
            MaterialTheme {
                MapScreen()
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory())
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val route = viewModel.route.value
    val cameraPositionState = rememberCameraPositionState()

    var destination by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    // Obtener la ubicación actual
    LaunchedEffect(Unit) {
        val location = fusedLocationClient.lastLocation.await()
        location?.let {
            currentLocation = LatLng(it.latitude, it.longitude)
            cameraPositionState.position = CameraPosition.Builder()
                .target(currentLocation)
                .zoom(14f)
                .build()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFe0f7fa), Color(0xFF80deea))
                )
            )
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = currentLocation),
                title = "Tu ubicación"
            )

            if (route.isNotEmpty()) {
                Polyline(points = route)
                Marker(
                    state = MarkerState(position = route.last()),
                    title = "Destino"
                )
            }
        }

        // UI Superior encima del mapa
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Planea tu Ruta",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006064)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                elevation = elevatedCardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destino") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocationName(destination, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses.first()
                        val destinationLatLng = LatLng(address.latitude, address.longitude)
                        viewModel.getRoute(
                            "5b3ce3597851110001cf624869e60ed2dd544af98cb7a2c6c05aa6b8",
                            currentLocation,
                            destinationLatLng
                        )
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0097A7),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Trazar Ruta")
            }
        }
    }
}
