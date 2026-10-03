package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.NetworkRepository
import com.example.data.repository.PingResultItem
import com.example.data.repository.PortScanResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NocNavTab(val title: String, val badgeCount: Int = 0) {
    DASHBOARD("Dasbor"),
    DEVICES("Perangkat"),
    CLIENTS_VLAN("Klien & VLAN"),
    SECURITY("Keamanan IDS"),
    GRAFANA_TELEGRAM("Grafana & Bot"),
    LOGS("Log Audit"),
    REPORTS_RBAC("Laporan & Akses"),
    TOOLS("Toolkit NOC")
}

class NetworkViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = NetworkRepository(database)

    // Navigation & UI state
    private val _currentTab = MutableStateFlow(NocNavTab.DASHBOARD)
    val currentTab = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedVendorFilter = MutableStateFlow<DeviceVendor?>(null)
    val selectedVendorFilter = _selectedVendorFilter.asStateFlow()

    private val _selectedVlanFilter = MutableStateFlow<Int?>(null)
    val selectedVlanFilter = _selectedVlanFilter.asStateFlow()

    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage = _snackBarMessage.asStateFlow()

    // Domain data flows from Repository
    val devices = repository.getAllDevices().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val alerts = repository.getAllAlerts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val logs = repository.getAllLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val clients = repository.getAllClients().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val reports = repository.getAllReports().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unresolvedAlertsCount = repository.getUnresolvedAlertsCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val wanInterfaces = repository.wanInterfaces
    val vlans = repository.vlans
    val realtimeHistory = repository.realtimeHistory
    val grafanaConfig = repository.grafanaConfig
    val telegramConfig = repository.telegramConfig
    val currentUser = repository.currentUser

    // Toolkit states
    private val _isPinging = MutableStateFlow(false)
    val isPinging = _isPinging.asStateFlow()
    private val _pingResults = MutableStateFlow<List<PingResultItem>>(emptyList())
    val pingResults = _pingResults.asStateFlow()

    private val _isScanningPorts = MutableStateFlow(false)
    val isScanningPorts = _isScanningPorts.asStateFlow()
    private val _portScanResults = MutableStateFlow<List<PortScanResult>>(emptyList())
    val portScanResults = _portScanResults.asStateFlow()

    private val _wolMessage = MutableStateFlow<String?>(null)
    val wolMessage = _wolMessage.asStateFlow()

    private val _isTestingTelegram = MutableStateFlow(false)
    val isTestingTelegram = _isTestingTelegram.asStateFlow()

    private val _latestGeneratedReport = MutableStateFlow<NetworkReportSummary?>(null)
    val latestGeneratedReport = _latestGeneratedReport.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
        startLiveTelemetryPolling()
    }

    private fun startLiveTelemetryPolling() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(2500)
                repository.appendTelemetrySample()
            }
        }
    }

    fun selectTab(tab: NocNavTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setVendorFilter(vendor: DeviceVendor?) {
        _selectedVendorFilter.value = vendor
    }

    fun setVlanFilter(vlanId: Int?) {
        _selectedVlanFilter.value = vlanId
    }

    fun showMessage(msg: String) {
        _snackBarMessage.value = msg
    }

    fun clearMessage() {
        _snackBarMessage.value = null
    }

    // Device Actions
    fun addNewDevice(device: NetworkDevice) {
        viewModelScope.launch {
            repository.addDevice(device)
            showMessage("Perangkat ${device.name} berhasil ditambahkan ke NOC monitoring.")
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            repository.deleteDevice(deviceId)
            showMessage("Perangkat berhasil dihapus.")
        }
    }

    fun rebootDevice(deviceId: String, deviceName: String) {
        viewModelScope.launch {
            val result = repository.rebootDevice(deviceId, deviceName)
            result.onSuccess { msg ->
                showMessage(msg)
            }.onFailure { err ->
                showMessage(err.message ?: "Gagal me-reboot perangkat")
            }
        }
    }

    // Security Actions
    fun resolveAlert(alertId: String, mitigationText: String) {
        viewModelScope.launch {
            repository.resolveAlert(alertId, mitigationText)
            showMessage("Insiden keamanan terselesaikan.")
        }
    }

    fun triggerThreatSimulation(type: AlertType) {
        viewModelScope.launch {
            repository.triggerSimulatedThreat(type)
            showMessage("Simulasi serangan ${type.name} berhasil diaktifkan.")
        }
    }

    // Client Actions
    fun toggleClientBlock(clientId: String, isBlocked: Boolean, hostname: String) {
        viewModelScope.launch {
            repository.setClientBlocked(clientId, isBlocked, hostname)
            val action = if (isBlocked) "diblokir" else "dibuka kembali"
            showMessage("Klien $hostname berhasil $action.")
        }
    }

    fun toggleClientStatic(clientId: String, isStatic: Boolean, hostname: String) {
        viewModelScope.launch {
            repository.setClientStatic(clientId, isStatic, hostname)
            val action = if (isStatic) "ditetapkan sebagai IP Statis" else "diubah ke Dynamic DHCP"
            showMessage("Klien $hostname $action.")
        }
    }

    // Telegram Actions
    fun updateTelegramConfig(config: TelegramBotConfig) {
        repository.updateTelegramConfig(config)
        showMessage("Konfigurasi Bot Telegram tersimpan.")
    }

    fun testTelegramBot(customMessage: String? = null) {
        viewModelScope.launch {
            _isTestingTelegram.value = true
            val result = repository.testTelegramNotification(customMessage)
            _isTestingTelegram.value = false
            result.onSuccess { msg ->
                showMessage("✅ $msg")
            }.onFailure { err ->
                showMessage("❌ Gagal kirim Telegram: ${err.message}")
            }
        }
    }

    // Grafana Actions
    fun updateGrafanaConfig(config: GrafanaConfig) {
        repository.updateGrafanaConfig(config)
        showMessage("Pengaturan Grafana Live Dashboard tersimpan.")
    }

    // User Role Actions
    fun switchUserRole(newRole: UserRole) {
        repository.switchUserRole(newRole)
        showMessage("Beralih peran pengguna: ${newRole.roleName}")
    }

    // Audit Log Actions
    fun clearAuditLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            showMessage("Seluruh log audit berhasil dibersihkan.")
        }
    }

    // Report Actions
    fun generateReport(period: String) {
        viewModelScope.launch {
            val rep = repository.generateAndSaveReport(period)
            _latestGeneratedReport.value = rep
            showMessage("Laporan ($period) berhasil di-generate dan disimpan.")
        }
    }

    // Toolkit Probes
    fun runPing(targetHost: String) {
        if (targetHost.isBlank()) return
        viewModelScope.launch {
            _isPinging.value = true
            _pingResults.value = emptyList()
            val results = repository.executePingProbe(targetHost, 4)
            _pingResults.value = results
            _isPinging.value = false
        }
    }

    fun runPortScan(targetHost: String, ports: List<Int> = listOf(22, 53, 80, 443, 161, 8291, 8728, 8080)) {
        if (targetHost.isBlank()) return
        viewModelScope.launch {
            _isScanningPorts.value = true
            _portScanResults.value = emptyList()
            val results = repository.scanPorts(targetHost, ports)
            _portScanResults.value = results
            _isScanningPorts.value = false
        }
    }

    fun sendWakeOnLan(macAddress: String) {
        if (macAddress.isBlank()) return
        viewModelScope.launch {
            val result = repository.sendWakeOnLan(macAddress)
            result.onSuccess { msg ->
                _wolMessage.value = msg
                showMessage(msg)
            }.onFailure { err ->
                _wolMessage.value = "Error: ${err.message}"
                showMessage("Gagal WOL: ${err.message}")
            }
        }
    }
}
