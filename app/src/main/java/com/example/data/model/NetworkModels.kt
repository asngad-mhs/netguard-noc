package com.example.data.model

enum class DeviceVendor(val displayName: String, val defaultPort: Int) {
    MIKROTIK("MikroTik", 8728),
    CISCO("Cisco Systems", 443),
    RUIJIE("Ruijie / Reyee", 80),
    LINKSYS("Linksys", 80),
    FORTINET("Fortinet", 443),
    UBIQUITI("Ubiquiti UniFi", 8443),
    GENERIC("Generic Router", 80)
}

enum class DeviceRole(val title: String) {
    CORE_ROUTER("Core Router"),
    EDGE_GATEWAY("Edge WAN Gateway"),
    DISTRIBUTION_SWITCH("Distribution Switch"),
    ACCESS_SWITCH("Access Switch"),
    ACCESS_POINT("Wireless AP / Mesh"),
    FIREWALL("Security Gateway / UTM")
}

enum class ConnectionProtocol(val label: String) {
    ROUTEROS_API("RouterOS API (8728/8729)"),
    RESTCONF_API("RESTCONF / HTTPS"),
    SNMP_V2C("SNMP v2c (161)"),
    SNMP_V3("SNMP v3 (Encrypted)"),
    SSH("SSH CLI (22)"),
    RUIJIE_EKIT("Ruijie Cloud / eKit API"),
    LINKSYS_WEB("Linksys Smart Wi-Fi API")
}

enum class DeviceStatus {
    ONLINE,
    DEGRADED,
    OFFLINE
}

enum class Severity {
    CRITICAL,
    WARNING,
    INFO
}

enum class AlertType {
    PORT_SCAN,
    SYN_FLOOD,
    ROGUE_DHCP,
    ARP_SPOOF,
    BRUTE_FORCE,
    BANDWIDTH_SPIKE,
    INTERFACE_DOWN,
    HIGH_TEMPERATURE,
    HIGH_CPU_LOAD
}

enum class UserRole(val roleName: String, val level: Int) {
    SUPER_ADMIN("Super Administrator (NOC Lead)", 4),
    NETWORK_ADMIN("Network Administrator", 3),
    SEC_OPS("Security Operations Operator", 2),
    VIEWER("Guest / NOC Viewer (Read-Only)", 1);

    fun canRebootDevice(): Boolean = this.level >= 3
    fun canModifyFirewall(): Boolean = this.level >= 2
    fun canBlockClient(): Boolean = this.level >= 2
    fun canEditConfigs(): Boolean = this.level >= 3
    fun canExportReports(): Boolean = this.level >= 1
    fun canManageUsers(): Boolean = this.level >= 4
}

data class DeviceInterface(
    val name: String,
    val type: String, // WAN, ether, sfp-plus, wlan, bridge
    val isUp: Boolean,
    val ipAddress: String? = null,
    val macAddress: String,
    val rxSpeedMbps: Double = 0.0,
    val txSpeedMbps: Double = 0.0,
    val mtu: Int = 1500
)

data class NetworkDevice(
    val id: String,
    val name: String,
    val vendor: DeviceVendor,
    val model: String,
    val ipAddress: String,
    val port: Int,
    val protocol: ConnectionProtocol,
    val role: DeviceRole,
    val status: DeviceStatus,
    val cpuLoadPercent: Int,
    val memoryUsagePercent: Int,
    val temperatureCelsius: Int,
    val uptimeSeconds: Long,
    val firmwareVersion: String,
    val pingLatencyMs: Int,
    val interfaces: List<DeviceInterface> = emptyList(),
    val location: String = "Server Rack A",
    val isMonitored: Boolean = true
)

data class WanInterface(
    val id: String,
    val name: String,
    val ispName: String,
    val ipAddress: String,
    val gateway: String,
    val dns: String,
    val isPrimary: Boolean,
    val isConnected: Boolean,
    val latencyMs: Int,
    val jitterMs: Int,
    val packetLossPercent: Double,
    val currentDownloadMbps: Double,
    val currentUploadMbps: Double,
    val peakDownloadMbps: Double,
    val peakUploadMbps: Double,
    val totalRxGb: Double,
    val totalTxGb: Double
)

data class VlanInfo(
    val vlanId: Int,
    val name: String,
    val subnet: String,
    val gateway: String,
    val dhcpPoolRange: String,
    val activeClients: Int,
    val maxCapacity: Int,
    val isIsolated: Boolean = false,
    val colorHex: String = "#00E5FF"
)

data class DhcpClient(
    val id: String,
    val ipAddress: String,
    val macAddress: String,
    val hostname: String,
    val vendorOui: String,
    val vlanId: Int,
    val leaseTimeRemaining: String,
    val downloadSpeedKbps: Double,
    val uploadSpeedKbps: Double,
    val totalUsageMb: Double,
    val isStaticReservation: Boolean = false,
    val isBlocked: Boolean = false,
    val isSuspicious: Boolean = false,
    val connectedDevice: String = "MikroTik-CCR2004",
    val connectedSince: String = "Today, 08:30"
)

data class SecurityAlert(
    val id: String,
    val timestamp: Long,
    val severity: Severity,
    val type: AlertType,
    val title: String,
    val description: String,
    val sourceIp: String,
    val targetDevice: String,
    val isResolved: Boolean = false,
    val actionTaken: String? = null
)

data class ActivityAuditLog(
    val id: String,
    val timestamp: Long,
    val operatorName: String,
    val operatorRole: UserRole,
    val category: String,
    val description: String,
    val isSuccess: Boolean = true
)

data class RealtimeMetricPoint(
    val timestamp: Long,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val cpuPercent: Double,
    val ramPercent: Double,
    val activeClients: Int,
    val pingMs: Int
)

data class GrafanaConfig(
    val endpointUrl: String = "https://grafana.internal.net/api/datasources",
    val apiKey: String = "",
    val dashboardUid: String = "netguard-noc-live",
    val refreshIntervalSec: Int = 5,
    val isStreamingEnabled: Boolean = true,
    val prometheusTarget: String = "192.168.1.1:9100/metrics"
)

data class TelegramBotConfig(
    val botToken: String = "",
    val chatId: String = "",
    val channelName: String = "@netguard_noc_alerts",
    val isEnabled: Boolean = true,
    val notifyWanDown: Boolean = true,
    val notifySecurityAlerts: Boolean = true,
    val notifyHighCpu: Boolean = true,
    val notifyClientSpike: Boolean = true,
    val dailyDigestEnabled: Boolean = true,
    val lastTestResult: String? = null
)

data class UserAccount(
    val username: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val pinCode: String = "1234",
    val avatarInitials: String = "SA"
)

data class NetworkReportSummary(
    val reportId: String,
    val generatedDate: String,
    val period: String, // "Harian (Daily)", "Mingguan (Weekly)", "Bulanan (Monthly)"
    val slaUptimePercent: Double,
    val totalDataTransferredGb: Double,
    val totalDownloadGb: Double,
    val totalUploadGb: Double,
    val peakDownloadSpeedMbps: Double,
    val peakUploadSpeedMbps: Double,
    val avgLatencyMs: Double,
    val activeClientsPeak: Int,
    val securityIncidentsCount: Int,
    val rogueDhcpAttemptsBlocked: Int,
    val topClientUsage: List<Pair<String, Double>> // Hostname to GB
)
