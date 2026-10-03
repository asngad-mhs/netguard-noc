package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SecurityScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.alerts.collectAsState()
    val unresolvedCount by viewModel.unresolvedAlertsCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showMitigationDialog by remember { mutableStateOf<SecurityAlert?>(null) }
    var selectedThreatToSimulate by remember { mutableStateOf<AlertType?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
    ) {
        // Security Status Banner
        item {
            NocCard(
                borderColor = if (unresolvedCount > 0) RedCritical else GreenSuccess,
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
                        LivePulseIndicator(color = if (unresolvedCount > 0) RedCritical else GreenSuccess)
                        Column {
                            Text(
                                text = if (unresolvedCount > 0) "LEVEL ANCAMAN: TINGGI ($unresolvedCount Insiden)" else "POSTUR KEAMANAN: AMAN",
                                color = if (unresolvedCount > 0) RedCritical else GreenSuccess,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Intrusion Detection System (IDS) & Firewall Aktif",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = if (unresolvedCount > 0) RedCritical else GreenSuccess,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Active Firewall Defense Features Grid
        item {
            NocCard {
                Text(
                    text = "Proteksi Multi-Vendor Terpasang",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DefenseFeatureBadge("DHCP Snooping", true, modifier = Modifier.weight(1f))
                    DefenseFeatureBadge("ARP DAI Inspection", true, modifier = Modifier.weight(1f))
                    DefenseFeatureBadge("IP Source Guard", true, modifier = Modifier.weight(1f))
                }
            }
        }

        // Attack Simulator & Test Dispatcher
        item {
            NocCard(
                borderColor = AmberWarning.copy(alpha = 0.5f),
                backgroundColor = NocCardBg
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
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = AmberWarning)
                        Text(
                            text = "Simulasi Serangan & Uji Alert",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Trigger Telegram",
                        color = AmberWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Klik tombol simulasi di bawah untuk menguji deteksi otomatis IDS & pengiriman notifikasi instan ke Bot Telegram:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerThreatSimulation(AlertType.ROGUE_DHCP) },
                        colors = ButtonDefaults.buttonColors(containerColor = RedCritical.copy(alpha = 0.25f), contentColor = RedCritical),
                        modifier = Modifier.weight(1f).testTag("sim_rogue_dhcp"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Rogue DHCP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.triggerThreatSimulation(AlertType.PORT_SCAN) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning.copy(alpha = 0.25f), contentColor = AmberWarning),
                        modifier = Modifier.weight(1f).testTag("sim_port_scan"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Port Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.triggerThreatSimulation(AlertType.SYN_FLOOD) },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent.copy(alpha = 0.25f), contentColor = PurpleAccent),
                        modifier = Modifier.weight(1f).testTag("sim_syn_flood"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SYN Flood", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Incident Feed Header
        item {
            Text(
                text = "Daftar Insiden & Log Keamanan Jaringan (${alerts.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Alerts List
        items(alerts, key = { it.id }) { alert ->
            SecurityAlertCard(
                alert = alert,
                onResolveClick = { showMitigationDialog = alert }
            )
        }
    }

    // Mitigation Dialog
    showMitigationDialog?.let { alert ->
        var actionText by remember { mutableStateOf("Isolasi Port Switch & Blokir IP Sumber di Firewall Raw Table") }

        AlertDialog(
            onDismissRequest = { showMitigationDialog = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GreenSuccess)
                    Text("Tindakan Mitigasi Insiden", color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(alert.title, color = CyanNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Sumber IP: ${alert.sourceIp}", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Text("Target: ${alert.targetDevice}", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = actionText,
                        onValueChange = { actionText = it },
                        label = { Text("Langkah Mitigasi / Catatan NOC") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolveAlert(alert.id, actionText)
                        showMitigationDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = NocDarkBg)
                ) {
                    Text("Terapkan & Selesaikan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMitigationDialog = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = NocCardBgElevated
        )
    }
}

@Composable
fun DefenseFeatureBadge(
    title: String,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = NocDarkBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isEnabled) GreenSuccess.copy(alpha = 0.4f) else NocCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isEnabled) GreenSuccess else RedCritical))
            Text(title, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
fun SecurityAlertCard(
    alert: SecurityAlert,
    onResolveClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp))

    NocCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (alert.isResolved) NocCardBorder else when (alert.severity) {
            Severity.CRITICAL -> RedCritical
            Severity.WARNING -> AmberWarning
            Severity.INFO -> CyanNeon
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
                SeverityBadge(severity = alert.severity)
                Text(
                    text = alert.type.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = dateStr,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = alert.title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = alert.description,
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Target & Source Details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(NocDarkBg)
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Src: ${alert.sourceIp}", color = CyanNeon, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("Dst: ${alert.targetDevice}", color = TextHighlight, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }

        if (alert.isResolved) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "✅ Terselesaikan: ${alert.actionTaken ?: "Mitigasi Berhasil"}",
                color = GreenSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onResolveClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon.copy(alpha = 0.2f), contentColor = CyanNeon),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mitigasi & Selesaikan Insiden", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
