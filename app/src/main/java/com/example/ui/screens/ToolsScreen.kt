package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.repository.PingResultItem
import com.example.data.repository.PortScanResult
import com.example.ui.components.NocCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.NetworkViewModel

@Composable
fun ToolsScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val isPinging by viewModel.isPinging.collectAsState()
    val pingResults by viewModel.pingResults.collectAsState()
    val isScanningPorts by viewModel.isScanningPorts.collectAsState()
    val portResults by viewModel.portScanResults.collectAsState()
    val wolMessage by viewModel.wolMessage.collectAsState()

    var activeToolTab by remember { mutableIntStateOf(0) } // 0=Ping, 1=Port Scan, 2=Subnet Calc, 3=WOL

    // Local tool states
    var pingTarget by remember { mutableStateOf("192.168.10.1") }
    var scanTarget by remember { mutableStateOf("192.168.10.1") }
    var calcIp by remember { mutableStateOf("192.168.20.1") }
    var calcCidr by remember { mutableIntStateOf(24) }
    var wolMac by remember { mutableStateOf("BC:D0:74:11:22:33") }
    var wolBroadcast by remember { mutableStateOf("255.255.255.255") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Tool Tabs
        ScrollableTabRow(
            selectedTabIndex = activeToolTab,
            containerColor = NocCardBg,
            contentColor = CyanNeon,
            edgePadding = 8.dp,
            divider = {},
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NocCardBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(selected = activeToolTab == 0, onClick = { activeToolTab = 0 }, text = { Text("Ping Probe", fontWeight = FontWeight.Bold) })
            Tab(selected = activeToolTab == 1, onClick = { activeToolTab = 1 }, text = { Text("Port Scanner", fontWeight = FontWeight.Bold) })
            Tab(selected = activeToolTab == 2, onClick = { activeToolTab = 2 }, text = { Text("Subnet CIDR", fontWeight = FontWeight.Bold) })
            Tab(selected = activeToolTab == 3, onClick = { activeToolTab = 3 }, text = { Text("Wake-on-LAN", fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            when (activeToolTab) {
                0 -> {
                    // Ping Probe Tool
                    item {
                        NocCard {
                            Text("Diagnostik ICMP / Ping Probe", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = pingTarget,
                                onValueChange = { pingTarget = it },
                                label = { Text("Target Host / IP Gateway") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("ping_target_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.runPing(pingTarget) },
                                enabled = !isPinging && pingTarget.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("run_ping_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = NocDarkBg)
                            ) {
                                if (isPinging) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NocDarkBg, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kirim Ping (4 Paket)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (pingResults.isNotEmpty()) {
                        item {
                            val successes = pingResults.filter { it.isSuccess }
                            val minLat = successes.minOfOrNull { it.latencyMs } ?: 0
                            val maxLat = successes.maxOfOrNull { it.latencyMs } ?: 0
                            val avgLat = if (successes.isNotEmpty()) successes.map { it.latencyMs }.average() else 0.0
                            val lossPercent = ((4 - successes.size) / 4.0) * 100

                            NocCard {
                                Text("Statistik Hasil Ping:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NocDarkBg)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Min: ${minLat}ms", color = GreenSuccess, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text("Avg: ${"%.1f".format(avgLat)}ms", color = CyanNeon, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text("Max: ${maxLat}ms", color = AmberWarning, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text("Loss: ${lossPercent}%", color = if (lossPercent > 0) RedCritical else GreenSuccess, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                pingResults.forEach { res ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Seq ${res.sequence}: ${res.host}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        Text(
                                            if (res.isSuccess) "${res.latencyMs} ms (TTL=${res.ttl})" else "Request Timed Out",
                                            color = if (res.isSuccess) GreenSuccess else RedCritical,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Port Scanner
                    item {
                        NocCard {
                            Text("Port Scanner Multi-Vendor", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = scanTarget,
                                onValueChange = { scanTarget = it },
                                label = { Text("IP Target (Router / Switch / Server)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("scan_target_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.runPortScan(scanTarget) },
                                enabled = !isScanningPorts && scanTarget.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("run_scan_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = NocDarkBg)
                            ) {
                                if (isScanningPorts) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NocDarkBg, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pindai Port Layanan", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (portResults.isNotEmpty()) {
                        item {
                            NocCard {
                                Text("Hasil Pemindaian Port:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                portResults.forEach { p ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Port ${p.port} (${p.service})", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Surface(
                                            color = if (p.isOpen) GreenSuccess.copy(alpha = 0.2f) else RedCritical.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                if (p.isOpen) "OPEN / LISTENING" else "CLOSED / FILTERED",
                                                color = if (p.isOpen) GreenSuccess else RedCritical,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Subnet CIDR Calculator
                    item {
                        val calcResult = calculateSubnet(calcIp, calcCidr)

                        NocCard {
                            Text("Kalkulator Subnet & CIDR IP", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = calcIp,
                                onValueChange = { calcIp = it },
                                label = { Text("IP Address Host") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Prefix CIDR: /$calcCidr (${calcResult.netmask})", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = calcCidr.toFloat(),
                                onValueChange = { calcCidr = it.toInt() },
                                valueRange = 16f..30f,
                                steps = 13,
                                colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Subnet Breakdown Table
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NocDarkBg)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CalcRow("Network IP:", calcResult.networkIp)
                                CalcRow("Netmask Subnet:", calcResult.netmask)
                                CalcRow("Usable Range:", "${calcResult.firstUsable} - ${calcResult.lastUsable}")
                                CalcRow("Broadcast IP:", calcResult.broadcastIp)
                                CalcRow("Total Host Valid:", "${calcResult.usableHosts} Host")
                                CalcRow("Wildcard Mask:", calcResult.wildcardMask)
                            }
                        }
                    }
                }

                3 -> {
                    // Wake-on-LAN (WOL)
                    item {
                        NocCard {
                            Text("Wake-on-LAN (WOL) Packet Sender", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Kirimkan Magic Packet UDP Port 9 untuk menghidupkan PC/Server di jaringan lokal secara remote.", color = TextSecondary, fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = wolMac,
                                onValueChange = { wolMac = it },
                                label = { Text("Target MAC Address (cth: AA:BB:CC:DD:EE:FF)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("wol_mac_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = wolBroadcast,
                                onValueChange = { wolBroadcast = it },
                                label = { Text("Broadcast IP Subnet") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.sendWakeOnLan(wolMac) },
                                modifier = Modifier.fillMaxWidth().testTag("send_wol_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = NocDarkBg)
                            ) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Siarkan Paket WOL Magic", fontWeight = FontWeight.Bold)
                            }

                            wolMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(msg, color = GreenSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalcRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

data class SubnetCalculationResult(
    val networkIp: String,
    val netmask: String,
    val firstUsable: String,
    val lastUsable: String,
    val broadcastIp: String,
    val usableHosts: Long,
    val wildcardMask: String
)

fun calculateSubnet(ipStr: String, prefix: Int): SubnetCalculationResult {
    return try {
        val parts = ipStr.split(".").map { it.toIntOrNull() ?: 0 }
        val ipLong = ((parts.getOrElse(0) { 192 } shl 24) or
                      (parts.getOrElse(1) { 168 } shl 16) or
                      (parts.getOrElse(2) { 10 } shl 8) or
                      (parts.getOrElse(3) { 1 })).toLong() and 0xFFFFFFFFL

        val maskLong = (-1L shl (32 - prefix)) and 0xFFFFFFFFL
        val wildcardLong = maskLong.inv() and 0xFFFFFFFFL
        val networkLong = ipLong and maskLong
        val broadcastLong = networkLong or wildcardLong

        fun longToIp(l: Long): String {
            return "${(l shr 24) and 0xFF}.${(l shr 16) and 0xFF}.${(l shr 8) and 0xFF}.${l and 0xFF}"
        }

        val totalHosts = if (prefix >= 31) 0L else (1L shl (32 - prefix)) - 2

        SubnetCalculationResult(
            networkIp = longToIp(networkLong),
            netmask = longToIp(maskLong),
            firstUsable = longToIp(networkLong + 1),
            lastUsable = longToIp(broadcastLong - 1),
            broadcastIp = longToIp(broadcastLong),
            usableHosts = totalHosts.coerceAtLeast(0),
            wildcardMask = longToIp(wildcardLong)
        )
    } catch (e: Exception) {
        SubnetCalculationResult("192.168.10.0", "255.255.255.0", "192.168.10.1", "192.168.10.254", "192.168.10.255", 254, "0.0.0.255")
    }
}
