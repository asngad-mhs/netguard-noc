package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.viewmodel.NocNavTab

@Composable
fun DashboardScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val wanInterfaces by viewModel.wanInterfaces.collectAsState()
    val vlans by viewModel.vlans.collectAsState()
    val history by viewModel.realtimeHistory.collectAsState()
    val unresolvedCount by viewModel.unresolvedAlertsCount.collectAsState()

    val totalDownloadSpeed = wanInterfaces.sumOf { it.currentDownloadMbps }
    val totalUploadSpeed = wanInterfaces.sumOf { it.currentUploadMbps }
    val totalActiveClients = clients.size
    val onlineDevicesCount = devices.count { it.status == DeviceStatus.ONLINE }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // WAN Hero Header Card
        item {
            WanStatusHeroCard(
                wanList = wanInterfaces,
                onViewDetails = { viewModel.selectTab(NocNavTab.DEVICES) }
            )
        }

        // Quick KPI Metric Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickKpiTile(
                    title = "Total Trafik Masuk",
                    value = "${"%.1f".format(totalDownloadSpeed)} Mbps",
                    subtitle = "Bandwidth Download",
                    icon = Icons.Default.CloudDownload,
                    accentColor = CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                QuickKpiTile(
                    title = "Total Trafik Keluar",
                    value = "${"%.1f".format(totalUploadSpeed)} Mbps",
                    subtitle = "Bandwidth Upload",
                    icon = Icons.Default.CloudUpload,
                    accentColor = GreenSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickKpiTile(
                    title = "Klien Terhubung",
                    value = "$totalActiveClients Unit",
                    subtitle = "${vlans.size} VLAN Aktif",
                    icon = Icons.Default.Devices,
                    accentColor = PurpleAccent,
                    modifier = Modifier.weight(1f).clickable {
                        viewModel.selectTab(NocNavTab.CLIENTS_VLAN)
                    }
                )
                QuickKpiTile(
                    title = "Perangkat Multi-Vendor",
                    value = "$onlineDevicesCount / ${devices.size}",
                    subtitle = if (unresolvedCount > 0) "$unresolvedCount Alert Terbuka" else "Semua Normal",
                    icon = Icons.Default.Router,
                    accentColor = if (unresolvedCount > 0) RedCritical else TextHighlight,
                    modifier = Modifier.weight(1f).clickable {
                        if (unresolvedCount > 0) viewModel.selectTab(NocNavTab.SECURITY)
                        else viewModel.selectTab(NocNavTab.DEVICES)
                    }
                )
            }
        }

        // Realtime Grafana Telemetry Waveform Section
        item {
            NocCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CyanNeon.copy(alpha = 0.4f)
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
                        LivePulseIndicator(color = CyanNeon)
                        Text(
                            text = "Grafana Live Telemetry (Real-Time)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        modifier = Modifier.clickable { viewModel.selectTab(NocNavTab.GRAFANA_TELEGRAM) },
                        color = CyanNeon.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Grafana Config", tint = CyanNeon, modifier = Modifier.size(14.dp))
                            Text("Pengaturan", color = CyanNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                RealtimeTrafficWaveChart(history = history, heightDp = 150)
            }
        }

        // Multi-Vendor Infrastructure Snapshot
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Topologi Vendor Jaringan",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lihat Semua (${devices.size})",
                        color = CyanNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.selectTab(NocNavTab.DEVICES) }
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(devices) { device ->
                        DeviceMiniCard(
                            device = device,
                            onClick = { viewModel.selectTab(NocNavTab.DEVICES) }
                        )
                    }
                }
            }
        }

        // Active Security Incident Alert Banner (if any)
        val openAlerts = alerts.filter { !it.isResolved }
        if (openAlerts.isNotEmpty()) {
            item {
                NocCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTab(NocNavTab.SECURITY) },
                    borderColor = RedCritical,
                    backgroundColor = NocCardBgElevated
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
                            LivePulseIndicator(color = RedCritical)
                            Text(
                                text = "Peringatan Keamanan Aktif (${openAlerts.size})",
                                color = RedCritical,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Tangani",
                            color = RedCritical,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val topAlert = openAlerts.first()
                    Text(
                        text = topAlert.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = topAlert.description,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                }
            }
        }

        // Recent NOC Activity Feed Ticker
        item {
            NocCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aktivitas NOC Terkini",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seluruh Log",
                        color = TextHighlight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { viewModel.selectTab(NocNavTab.LOGS) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                logs.take(3).forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (log.isSuccess) GreenSuccess else RedCritical)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.description,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "${log.operatorName} • ${log.category}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WanStatusHeroCard(
    wanList: List<WanInterface>,
    onViewDetails: () -> Unit
) {
    val primary = wanList.firstOrNull { it.isPrimary } ?: wanList.firstOrNull()

    NocCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = CyanNeon.copy(alpha = 0.5f),
        backgroundColor = NocCardBgElevated
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
                LivePulseIndicator(color = GreenSuccess)
                Column {
                    Text(
                        text = primary?.name ?: "WAN Interface Gateway",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = primary?.ispName ?: "ISP Dedicated Connection",
                        color = CyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenSuccess.copy(alpha = 0.2f))
                    .border(1.dp, GreenSuccess, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SLA 99.98%",
                    color = GreenSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metrics Row: IP, Latency, Jitter, Packet Loss
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NocDarkBg.copy(alpha = 0.5f))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("IP Publik", color = TextSecondary, fontSize = 10.sp)
                Text(primary?.ipAddress ?: "103.28.14.50", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("Latency", color = TextSecondary, fontSize = 10.sp)
                Text("${primary?.latencyMs ?: 8} ms", color = GreenSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("Jitter", color = TextSecondary, fontSize = 10.sp)
                Text("${primary?.jitterMs ?: 2} ms", color = TextHighlight, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("Packet Loss", color = TextSecondary, fontSize = 10.sp)
                Text("${primary?.packetLossPercent ?: 0.0}%", color = GreenSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speeds Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Download", tint = CyanNeon, modifier = Modifier.size(16.dp))
                Column {
                    Text("Live Download", color = TextSecondary, fontSize = 10.sp)
                    Text("${"%.1f".format(primary?.currentDownloadMbps ?: 0.0)} Mbps", color = CyanNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Upload", tint = GreenSuccess, modifier = Modifier.size(16.dp))
                Column {
                    Text("Live Upload", color = TextSecondary, fontSize = 10.sp)
                    Text("${"%.1f".format(primary?.currentUploadMbps ?: 0.0)} Mbps", color = GreenSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Total Data Hari Ini", color = TextSecondary, fontSize = 10.sp)
                Text("${"%.1f".format((primary?.totalRxGb ?: 0.0) + (primary?.totalTxGb ?: 0.0))} GB", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun DeviceMiniCard(
    device: NetworkDevice,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(190.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, NocCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = NocCardBg
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VendorBadge(vendor = device.vendor)
                StatusBadge(status = device.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = device.name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = device.ipAddress,
                color = TextHighlight,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // CPU & RAM Mini bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CPU: ${device.cpuLoadPercent}%", color = if (device.cpuLoadPercent > 80) RedCritical else TextSecondary, fontSize = 10.sp)
                Text("RAM: ${device.memoryUsagePercent}%", color = TextSecondary, fontSize = 10.sp)
                Text("${device.pingLatencyMs}ms", color = GreenSuccess, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
