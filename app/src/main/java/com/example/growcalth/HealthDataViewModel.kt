package com.example.growcalth

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthDataViewModel(private val context: Context) : ViewModel() {

    private val healthConnectClient = HealthConnectClient.getOrCreate(context)
    private val manager = HealthDataManager(healthConnectClient)

    // StateFlow for steps
    private val _steps = MutableStateFlow(0L)
    val steps: StateFlow<Long> = _steps.asStateFlow()

    // StateFlow for distance
    private val _distance = MutableStateFlow(0.0)
    val distance: StateFlow<Double> = _distance.asStateFlow()

    // StateFlow for permissions
    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    // StateFlow for loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkPermissionsAndLoadData()
    }

    fun loadHealthData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val (stepsData, distanceData) = manager.readStepsAndDistance()
                _steps.value = stepsData
                _distance.value = distanceData
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkPermissionsAndLoadData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                val requiredPermissions: Set<String> = manager.permissions
                _hasPermissions.value = granted.containsAll(requiredPermissions)

                if (_hasPermissions.value) {
                    loadHealthData()
                }
            } catch (e: Exception) {
                _hasPermissions.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun checkAndRequestPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            val requiredPermissions: Set<String> = manager.permissions
            val hasAllPermissions = granted.containsAll(requiredPermissions)

            if (!hasAllPermissions) {
                return false
            }

            hasAllPermissions
        } catch (e: Exception) {
            false
        }
    }

    fun getPermissions(): Set<String> = manager.permissions

    fun getPermissionStrings(): Array<String> = manager.permissions.toTypedArray()

    // Factory for creating ViewModel with Context
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HealthDataViewModel::class.java)) {
                return HealthDataViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}