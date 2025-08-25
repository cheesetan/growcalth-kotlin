package com.example.growcalth

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthDataViewModel(private val manager: HealthDataManager) : ViewModel() {

    private val _steps = MutableStateFlow(0L)
    val steps: StateFlow<Long> = _steps.asStateFlow()

    private val _distance = MutableStateFlow(0.0)
    val distance: StateFlow<Double> = _distance.asStateFlow()

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _healthConnectAvailable = MutableStateFlow(true)
    val healthConnectAvailable: StateFlow<Boolean> = _healthConnectAvailable.asStateFlow()

    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class)
    )

    init {
        Log.d("HealthDataVM", "ViewModel init block started")
        viewModelScope.launch {
            Log.d("HealthDataVM", "Coroutine launched in init")

            try {
                Log.d("HealthDataVM", "About to call manager.isHealthConnectAvailable()...")
                _healthConnectAvailable.value = manager.isHealthConnectAvailable()
                Log.d("HealthDataVM", "Health Connect available: ${_healthConnectAvailable.value}")

                if (_healthConnectAvailable.value) {
                    Log.d("HealthDataVM", "Calling checkPermissionsAndLoad...")
                    checkPermissionsAndLoad()
                } else {
                    Log.w("HealthDataVM", "Health Connect not available, skipping data load")
                }
            } catch (e: Exception) {
                Log.e("HealthDataVM", "Error in init coroutine", e)
            }
        }
        Log.d("HealthDataVM", "ViewModel init block finished")
    }

    fun checkPermissionsAndLoad() {
        Log.d("HealthDataVM", "checkPermissionsAndLoad() called")
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("HealthDataVM", "Loading started...")
            try {
                Log.d("HealthDataVM", "About to call manager.hasPermissions()...")
                _hasPermissions.value = manager.hasPermissions()
                Log.d("HealthDataVM", "Has permissions: ${_hasPermissions.value}")

                if (_hasPermissions.value) {
                    Log.d("HealthDataVM", "About to call manager.getStepsAndDistance()...")
                    val (s, d) = manager.getStepsAndDistance()
                    Log.d("HealthDataVM", "Data fetched - Steps: $s, Distance: $d")
                    _steps.value = s
                    _distance.value = d
                } else {
                    Log.d("HealthDataVM", "No permissions, setting to 0")
                    _steps.value = 0L
                    _distance.value = 0.0
                }
            } catch (t: Throwable) {
                Log.e("HealthDataVM", "Data fetch failed", t)
            } finally {
                _isLoading.value = false
                Log.d("HealthDataVM", "Loading finished")
            }
        }
    }

    fun getPermissionStrings(): Array<String> {
        Log.d("HealthDataVM", "getPermissionStrings() called")
        return permissions.map { it.toString() }.toTypedArray()
    }

    suspend fun hasAllPermissions(context: Context): Boolean {
        val client = HealthConnectClient.getOrCreate(context)
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    fun getPermissions() = permissions

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HealthDataViewModel::class.java)) {
                return HealthDataViewModel(HealthDataManager(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
