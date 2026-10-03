package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val vendor: String,
    val model: String,
    val ipAddress: String,
    val port: Int,
    val protocol: String,
    val role: String,
    val status: String,
    val cpuLoadPercent: Int,
    val memoryUsagePercent: Int,
    val temperatureCelsius: Int,
    val uptimeSeconds: Long,
    val firmwareVersion: String,
    val pingLatencyMs: Int,
    val location: String,
    val isMonitored: Boolean
)

@Entity(tableName = "security_alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val severity: String,
    val type: String,
    val title: String,
    val description: String,
    val sourceIp: String,
    val targetDevice: String,
    val isResolved: Boolean,
    val actionTaken: String?
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val operatorName: String,
    val operatorRole: String,
    val category: String,
    val description: String,
    val isSuccess: Boolean
)

@Entity(tableName = "dhcp_clients")
data class ClientEntity(
    @PrimaryKey val id: String,
    val ipAddress: String,
    val macAddress: String,
    val hostname: String,
    val vendorOui: String,
    val vlanId: Int,
    val leaseTimeRemaining: String,
    val downloadSpeedKbps: Double,
    val uploadSpeedKbps: Double,
    val totalUsageMb: Double,
    val isStaticReservation: Boolean,
    val isBlocked: Boolean,
    val isSuspicious: Boolean,
    val connectedDevice: String,
    val connectedSince: String
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val reportId: String,
    val generatedDate: String,
    val period: String,
    val slaUptimePercent: Double,
    val totalDownloadGb: Double,
    val totalUploadGb: Double,
    val peakDownloadSpeedMbps: Double,
    val peakUploadSpeedMbps: Double,
    val avgLatencyMs: Double,
    val activeClientsPeak: Int,
    val securityIncidentsCount: Int,
    val summaryJson: String
)
