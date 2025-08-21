package com.example.growcalth

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthDataManager(private val healthConnectClient: HealthConnectClient) {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class)
    )

    val permissionStrings: Set<String> = permissions

    suspend fun readStepsAndDistance(): Pair<Long, Double> {
        return try {
            val today = LocalDate.now()
            val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val stepsResponse = healthConnectClient.readRecords(
                ReadRecordsRequest<StepsRecord>(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            val totalSteps = stepsResponse.records.sumOf { it.count }

            val distanceResponse = healthConnectClient.readRecords(
                ReadRecordsRequest<DistanceRecord>(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            val totalDistance = distanceResponse.records.sumOf { it.distance.inMeters }

            Pair(totalSteps, totalDistance)
        } catch (e: Exception) {
            // Return default values if there's an error
            Pair(0L, 0.0)
        }
    }
}