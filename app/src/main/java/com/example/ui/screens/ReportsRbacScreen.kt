package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.NocCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.NetworkViewModel

@Composable
fun ReportsRbacScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val latestReport by viewModel.latestGeneratedReport.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Reports, 1 = RBAC
    var selectedPeriod by remember { mutableStateOf("Harian (Daily SLA)") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Switcher
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = NocCardBg,
            contentColor = CyanNeon,
            divider = {},
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NocCardBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Ekspor Laporan NOC", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Akses Pengguna & RBAC", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            if (activeTab == 0) {
                // Reports Tab
                item {
                    NocCard(
                        borderColor = CyanNeon.copy(alpha = 0.4f),
                        backgroundColor = NocCardBgElevated
                    ) {
                        Text(
                            text = "Generator Laporan Kinerja & SLA Otomatis",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Menghasilkan ringkasan komprehensif performa throughput bandwidth, SLA ketersediaan WAN, log insiden keamanan, dan pemakaian trafik per klien.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Period Selector & Generator Button
                item {
                    NocCard {
                        Text("Pilih Periode Laporan:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Harian (Daily SLA)", "Mingguan (Weekly)", "Bulanan (Monthly)").forEach { period ->
                                FilterChip(
                                    selected = selectedPeriod == period,
                                    onClick = { selectedPeriod = period },
                                    label = { Text(period.split(" ").first(), fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.generateReport(selectedPeriod) },
                            modifier = Modifier.fillMaxWidth().testTag("generate_report_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = NocDarkBg)
                        ) {
                            Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Laporan $selectedPeriod", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Generated Report View (if present)
                latestReport?.let { rep ->
                    item {
                        ReportPreviewCard(
                            report = rep,
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "NetGuard NOC Performance Report - ${rep.period}")
                                    putExtra(Intent.EXTRA_TEXT, formatReportText(rep))
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Jaringan"))
                            }
                        )
                    }
                }
            } else {
                // RBAC Tab
                item {
                    NocCard(
                        borderColor = PurpleAccent.copy(alpha = 0.4f),
                        backgroundColor = NocCardBgElevated
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = PurpleAccent.copy(alpha = 0.25f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(currentUser.avatarInitials, color = PurpleAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }

                                Column {
                                    Text(currentUser.fullName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(currentUser.role.roleName, color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Surface(
                                color = GreenSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("AKTIF", color = GreenSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }
                }

                // Role Switcher Demo
                item {
                    NocCard {
                        Text("Ganti Role Operator (Simulasi RBAC):", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        UserRole.values().forEach { role ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (currentUser.role == role) CyanNeon.copy(alpha = 0.15f) else NocDarkBg)
                                    .clickable { viewModel.switchUserRole(role) }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(role.roleName, color = if (currentUser.role == role) CyanNeon else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Tingkat Otoritas Level ${role.level}", color = TextSecondary, fontSize = 10.sp)
                                }
                                RadioButton(
                                    selected = currentUser.role == role,
                                    onClick = { viewModel.switchUserRole(role) },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyanNeon)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // Permission Matrix Table
                item {
                    NocCard {
                        Text("Matriks Izin Akses Pengguna (Permissions)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        PermissionRow("Reboot Perangkat Multi-Vendor", currentUser.role.canRebootDevice())
                        PermissionRow("Modifikasi Aturan Firewall / IDS", currentUser.role.canModifyFirewall())
                        PermissionRow("Blokir / Isolasi Klien DHCP", currentUser.role.canBlockClient())
                        PermissionRow("Edit Konfigurasi Jaringan & VLAN", currentUser.role.canEditConfigs())
                        PermissionRow("Ekspor & Bagikan Laporan SLA", currentUser.role.canExportReports())
                        PermissionRow("Manajemen Akun & Reset Log Audit", currentUser.role.canManageUsers())
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRow(label: String, isAllowed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 12.sp)
        if (isAllowed) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Allowed", tint = GreenSuccess, modifier = Modifier.size(18.dp))
        } else {
            Icon(Icons.Default.Cancel, contentDescription = "Denied", tint = RedCritical, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun ReportPreviewCard(
    report: NetworkReportSummary,
    onShare: () -> Unit
) {
    NocCard(
        borderColor = CyanNeon,
        backgroundColor = NocCardBgElevated
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Laporan: ${report.period}", color = CyanNeon, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("ID: ${report.reportId} • ${report.generatedDate}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = CyanNeon)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Key stats grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(NocDarkBg)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("WAN SLA Uptime", color = TextSecondary, fontSize = 10.sp)
                Text("${report.slaUptimePercent}%", color = GreenSuccess, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("Total Trafik", color = TextSecondary, fontSize = 10.sp)
                Text("${"%.1f".format(report.totalDataTransferredGb)} GB", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("Peak Download", color = TextSecondary, fontSize = 10.sp)
                Text("${report.peakDownloadSpeedMbps} Mbps", color = CyanNeon, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("Insiden Keamanan", color = TextSecondary, fontSize = 10.sp)
                Text("${report.securityIncidentsCount}", color = RedCritical, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Penggunaan Bandwidth Tertinggi (Top Consumers):", color = TextSecondary, fontSize = 11.sp)
        report.topClientUsage.forEach { (host, usageGb) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• $host", color = TextPrimary, fontSize = 11.sp)
                Text("$usageGb GB", color = TextHighlight, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = NocDarkBg)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bagikan / Ekspor Laporan (PDF/Text)", fontWeight = FontWeight.Bold)
        }
    }
}

fun formatReportText(rep: NetworkReportSummary): String {
    return """
        ========================================
        NETGUARD NOC - LAPORAN KINERJA JARINGAN
        ========================================
        ID Laporan: ${rep.reportId}
        Periode: ${rep.period}
        Tanggal Generate: ${rep.generatedDate}

        1. METRIK SLA & KINERJA GATEWAY:
        - WAN SLA Uptime: ${rep.slaUptimePercent}%
        - Rata-rata Latensi: ${rep.avgLatencyMs} ms
        - Total Data Transferred: ${"%.1f".format(rep.totalDataTransferredGb)} GB
          * Total Download: ${"%.1f".format(rep.totalDownloadGb)} GB
          * Total Upload: ${"%.1f".format(rep.totalUploadGb)} GB
        - Puncak Kecepatan Download: ${rep.peakDownloadSpeedMbps} Mbps
        - Puncak Kecepatan Upload: ${rep.peakUploadSpeedMbps} Mbps
        - Klien Terkoneksi Maksimal: ${rep.activeClientsPeak} Perangkat

        2. AUDIT KEAMANAN & IDS:
        - Total Insiden Terdeteksi: ${rep.securityIncidentsCount}
        - Upaya Rogue DHCP Diblokir: ${rep.rogueDhcpAttemptsBlocked}

        3. KONSUMSI BANDWIDTH TERATAS:
        ${rep.topClientUsage.joinToString("\n") { "  * ${it.first}: ${it.second} GB" }}

        ========================================
        Generated by NetGuard Network Operations Center
    """.trimIndent()
}
