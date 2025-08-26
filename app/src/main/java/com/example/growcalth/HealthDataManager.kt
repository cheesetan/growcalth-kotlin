package com.example.growcalth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

class HealthDataManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    // Permissions you need
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class)
    )


    /**
     * Check whether Health Connect is actually available on this device.
     * If this returns false, do NOT call other APIs (they can crash).
     */
    fun isHealthConnectAvailable(): Boolean {
        Log.d("HealthDataManager", "isHealthConnectAvailable() called")
        Log.d("HealthDataManager", "SDK_AVAILABLE constant: ${HealthConnectClient.SDK_AVAILABLE}")
        Log.d("HealthDataManager", "SDK_UNAVAILABLE: ${HealthConnectClient.SDK_UNAVAILABLE}")
        val status = HealthConnectClient.getSdkStatus(context)
        Log.d("HealthDataManager", "SDK Status: $status")
        val available = status == HealthConnectClient.SDK_AVAILABLE
        Log.d("HealthDataManager", "Health Connect available: $available")
        return available
    }

    /**
     * Optionally open Play Store to install/update Health Connect (if not available).
     */
    fun buildInstallHealthConnectIntent(): Intent {
        val uri = Uri.parse("market://details?id=com.google.android.apps.healthdata")
        return Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Have we already been granted all Health Connect permissions?
     */
    suspend fun hasPermissions(): Boolean = withContext(Dispatchers.IO) {
        Log.d("HealthDataManager", "hasPermissions() called")
        if (!isHealthConnectAvailable()) {
            Log.w("HealthDataManager", "Health Connect not available for permission check")
            return@withContext false
        }

        try {
            Log.d("HealthDataManager", "About to call getGrantedPermissions()...")
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d("HealthDataManager", "getGrantedPermissions() completed")
            Log.d("HealthDataManager", "Granted permissions: $granted")
            Log.d("HealthDataManager", "Required permissions: $permissions")

            // Check each permission individually
            permissions.forEach { permission ->
                val isGranted = granted.contains(permission)
                Log.d("HealthDataManager", "Permission $permission: granted=$isGranted")
            }

            val hasAll = granted.containsAll(permissions)
            Log.d("HealthDataManager", "Has all permissions: $hasAll")
            return@withContext hasAll
        } catch (t: Throwable) {
            Log.e("HealthDataManager", "getGrantedPermissions failed: ${t.message}", t)
            return@withContext false
        }
    }

    /**
     * Fetch today's total steps and distance (meters).
     * Returns Pair(steps, distanceMeters).
     */
    suspend fun getStepsAndDistance(): Pair<Long, Double> = withContext(Dispatchers.IO) {
        Log.d("HealthDataManager", "Starting data fetch...")

        if (!isHealthConnectAvailable()) {
            Log.w("HealthDataManager", "Health Connect not available")
            return@withContext 0L to 0.0
        }

        val end = Instant.now()
        val start = end.atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()

        Log.d("HealthDataManager", "Time range: $start to $end")

        return@withContext try {
            val result = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            Log.d("HealthDataManager", "Aggregate result: $result")

            val steps = result[StepsRecord.COUNT_TOTAL] ?: 0L
            val distance = result[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
            steps to distance
        } catch (t: Throwable) {
            Log.e("HealthDataManager", "Aggregate failed: ${t.message}", t)
            0L to 0.0
        }
    }
}
