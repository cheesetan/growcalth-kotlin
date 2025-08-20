package com.example.growcalth

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HealthDataView(private val context: Context) {

    private val healthConnectClient = HealthConnectClient.getOrCreate(context)
    private val manager = HealthDataManager(healthConnectClient)

    // Public immutable states, private mutable backing states
    val steps: State<Long> get() = _steps
    private val _steps = mutableStateOf(0L)

    val distance: State<Double> get() = _distance
    private val _distance = mutableStateOf(0.0)

    val hasPermissions: State<Boolean> get() = _hasPermissions
    private val _hasPermissions = mutableStateOf(false)

    @Composable
    fun ComposableView(scope: CoroutineScope) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            scope.launch {
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                _hasPermissions.value = granted.containsAll(manager.permissions) // ✅ FIXED
                if (_hasPermissions.value) {
                    val (s, d) = manager.readStepsAndDistance()
                    _steps.value = s          // ✅ FIXED
                    _distance.value = d       // ✅ FIXED
                }
            }
        }

        // Load data on first launch if permissions already granted
        LaunchedEffect(Unit) {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            _hasPermissions.value = granted.containsAll(manager.permissions)
            if (_hasPermissions.value) {
                val (s, d) = manager.readStepsAndDistance()
                _steps.value = s
                _distance.value = d
            }
        }

        Column(Modifier.padding(16.dp)) {
            if (_hasPermissions.value) {
                Text("Steps today: ${_steps.value}")
                Text("Distance today: ${String.format("%.2f", _distance.value / 1000)} km")
            } else {
                Text("Grant Health Connect Permissions")
            }
        }
    }
}
