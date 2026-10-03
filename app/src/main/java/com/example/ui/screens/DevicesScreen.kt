package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NetworkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedVendor by viewModel.selectedVendorFilter.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDeviceForDetails by remember { mutableStateOf<NetworkDevice?>(null) }
    var showRebootConfirmDialog by remember { mutableStateOf<NetworkDevice?>(null) }

    val filteredDevices = devices.filter { dev ->
        (selectedVendor == null || dev.vendor == selectedVendor) &&
        (searchQuery.isBlank() || dev.name.contains(searchQuery, ignoreCase = true) ||
         dev.ipAddress.contains(searchQuery, ignoreCase = true) ||
         dev.model.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NocDarkBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanNeon,
                contentColor = NocDarkBg,
                icon = { Icon(Icons.Default.Add, contentDescription = "Tambah Perangkat") },
                text = { Text("Tambah Perangkat", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_device_fab").offset(y = (-70).dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_search_input"),
                placeholder = { Text("Cari nama router, IP, model...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanNeon) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = NocCardBorder,
                    focusedContainerColor = NocCardBg,
                    unfocusedContainerColor = NocCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Vendor Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedVendor == null,
                        onClick = { viewModel.setVendorFilter(null) },
                        label = { Text("Semua (${devices.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                            selectedLabelColor = CyanNeon,
                            containerColor = NocCardBg,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedVendor == null,
                            borderColor = if (selectedVendor == null) CyanNeon else NocCardBorder
                        )
                    )
                }

                items(DeviceVendor.values()) { vendor ->
                    val count = devices.count { it.vendor == vendor }
                    if (count > 0 || vendor in listOf(DeviceVendor.MIKROTIK, DeviceVendor.CISCO, DeviceVendor.RUIJIE, DeviceVendor.LINKSYS)) {
                        FilterChip(
                            selected = selectedVendor == vendor,
                            onClick = { viewModel.setVendorFilter(if (selectedVendor == vendor) null else vendor) },
                            label = { Text("${vendor.displayName} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                selectedLabelColor = CyanNeon,
                                containerColor = NocCardBg,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedVendor == vendor,
                                borderColor = if (selectedVendor == vendor) CyanNeon else NocCardBorder
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Devices List
            if (filteredDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Router, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Text("Tidak ada perangkat ditemukan", color = TextSecondary, fontSize = 15.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(filteredDevices, key = { it.id }) { device ->
                        DeviceCardItem(
                            device = device,
                            onCardClick = { selectedDeviceForDetails = device },
                            onRebootClick = { showRebootConfirmDialog = device }
                        )
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    selectedDeviceForDetails?.let { device ->
        ModalBottomSheet(
            onDismissRequest = { selectedDeviceForDetails = null },
            containerColor = NocSurfaceDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = CyanNeon) }
        ) {
            DeviceDetailSheetContent(
                device = device,
                currentUserRole = currentUser.role,
                onReboot = {
                    showRebootConfirmDialog = device
                    selectedDeviceForDetails = null
                },
                onDelete = {
                    viewModel.deleteDevice(device.id)
                    selectedDeviceForDetails = null
                },
                onClose = { selectedDeviceForDetails = null }
            )
        }
    }

    // Reboot Confirmation Dialog
    showRebootConfirmDialog?.let { device ->
        AlertDialog(
            onDismissRequest = { showRebootConfirmDialog = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = AmberWarning)
                    Text("Konfirmasi Reboot Router", color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Apakah Anda yakin ingin me-reboot perangkat ini secara remote via API?",
                        color = TextSecondary
                    )
                    Text(
                        "${device.name} (${device.ipAddress})",
                        color = CyanNeon,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (!currentUser.role.canRebootDevice()) {
                        Text(
                            "⚠️ Peringatan: Role Anda (${currentUser.role.roleName}) tidak memiliki izin reboot perangkat!",
                            color = RedCritical,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rebootDevice(device.id, device.name)
                        showRebootConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = NocDarkBg)
                ) {
                    Text("Ya, Reboot Sekarang", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirmDialog = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = NocCardBgElevated
        )
    }

    // Add Device Dialog
    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onAddDevice = { newDev ->
                viewModel.addNewDevice(newDev)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DeviceCardItem(
    device: NetworkDevice,
    onCardClick: () -> Unit,
    onRebootClick: () -> Unit
) {
    NocCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        borderColor = when (device.status) {
            DeviceStatus.ONLINE -> NocCardBorder
            DeviceStatus.DEGRADED -> AmberWarning.copy(alpha = 0.5f)
            DeviceStatus.OFFLINE -> RedCritical.copy(alpha = 0.5f)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VendorBadge(vendor = device.vendor)
                StatusBadge(status = device.status)
            }

            Surface(
                color = CyanNeon.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "${device.pingLatencyMs} ms",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = device.name,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${device.model} • ${device.role.title}",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // IP & Protocol
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(NocDarkBg.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${device.ipAddress}:${device.port}",
                color = TextHighlight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = device.protocol.label.split("(").first().trim(),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Resource bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CPU Load", color = TextSecondary, fontSize = 10.sp)
                    Text("${device.cpuLoadPercent}%", color = if (device.cpuLoadPercent > 80) RedCritical else GreenSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { device.cpuLoadPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = if (device.cpuLoadPercent > 80) RedCritical else GreenSuccess,
                    trackColor = NocCardBorder
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("RAM Usage", color = TextSecondary, fontSize = 10.sp)
                    Text("${device.memoryUsagePercent}%", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { device.memoryUsagePercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = CyanNeon,
                    trackColor = NocCardBorder
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Temp", color = TextSecondary, fontSize = 10.sp)
                Text("${device.temperatureCelsius}°C", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeviceDetailSheetContent(
    device: NetworkDevice,
    currentUserRole: UserRole,
    onReboot: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VendorBadge(vendor = device.vendor)
                    StatusBadge(status = device.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(device.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${device.model} • ${device.firmwareVersion}", color = TextSecondary, fontSize = 12.sp)
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
            }
        }

        // Specs Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NocCardBg)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("IP Address", color = TextSecondary, fontSize = 10.sp)
                Text("${device.ipAddress}:${device.port}", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("Protokol", color = TextSecondary, fontSize = 10.sp)
                Text(device.protocol.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("Lokasi Rak", color = TextSecondary, fontSize = 10.sp)
                Text(device.location, color = TextHighlight, fontSize = 12.sp)
            }
        }

        // Interface Port Table
        Text("Daftar Interface & Port Aktif", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NocDarkBg)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            device.interfaces.take(4).forEach { iface ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (iface.isUp) GreenSuccess else RedCritical)
                        )
                        Text(iface.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }
                    Text("RX: ${iface.rxSpeedMbps}M / TX: ${iface.txSpeedMbps}M", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onReboot,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = NocDarkBg)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reboot Router", fontWeight = FontWeight.Bold)
            }

            if (currentUserRole.canManageUsers()) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCritical),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedCritical)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hapus Node")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAddDevice: (NetworkDevice) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("192.168.10.") }
    var port by remember { mutableStateOf("8728") }
    var vendor by remember { mutableStateOf(DeviceVendor.MIKROTIK) }
    var role by remember { mutableStateOf(DeviceRole.CORE_ROUTER) }
    var protocol by remember { mutableStateOf(ConnectionProtocol.ROUTEROS_API) }
    var model by remember { mutableStateOf("RouterOS CCR Cloud Core") }
    var location by remember { mutableStateOf("Main Server Rack 01") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tambah Perangkat Jaringan", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Perangkat (cth: MikroTik-Core-01)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP Address Host") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port API/SNMP") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model Hardware") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Text("Vendor Jaringan:", color = TextSecondary, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DeviceVendor.values()) { v ->
                        FilterChip(
                            selected = vendor == v,
                            onClick = {
                                vendor = v
                                port = v.defaultPort.toString()
                                protocol = when (v) {
                                    DeviceVendor.MIKROTIK -> ConnectionProtocol.ROUTEROS_API
                                    DeviceVendor.CISCO -> ConnectionProtocol.RESTCONF_API
                                    DeviceVendor.RUIJIE -> ConnectionProtocol.RUIJIE_EKIT
                                    DeviceVendor.LINKSYS -> ConnectionProtocol.LINKSYS_WEB
                                    else -> ConnectionProtocol.SNMP_V2C
                                }
                            },
                            label = { Text(v.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && ipAddress.isNotBlank()) {
                        val newDev = NetworkDevice(
                            id = "dev-${System.currentTimeMillis()}",
                            name = name,
                            vendor = vendor,
                            model = model,
                            ipAddress = ipAddress,
                            port = port.toIntOrNull() ?: 8728,
                            protocol = protocol,
                            role = role,
                            status = DeviceStatus.ONLINE,
                            cpuLoadPercent = 25,
                            memoryUsagePercent = 45,
                            temperatureCelsius = 40,
                            uptimeSeconds = 3600L,
                            firmwareVersion = "v1.0.0",
                            pingLatencyMs = 2,
                            location = location
                        )
                        onAddDevice(newDev)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = NocDarkBg)
            ) {
                Text("Simpan Perangkat", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
        containerColor = NocCardBgElevated
    )
}
