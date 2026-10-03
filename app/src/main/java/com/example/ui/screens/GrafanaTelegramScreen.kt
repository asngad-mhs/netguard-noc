package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun GrafanaTelegramScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val telegramConfig by viewModel.telegramConfig.collectAsState()
    val grafanaConfig by viewModel.grafanaConfig.collectAsState()
    val isTestingTelegram by viewModel.isTestingTelegram.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Telegram, 1 = Grafana

    // Telegram Local edit states
    var botToken by remember(telegramConfig) { mutableStateOf(telegramConfig.botToken) }
    var chatId by remember(telegramConfig) { mutableStateOf(telegramConfig.chatId) }
    var channelName by remember(telegramConfig) { mutableStateOf(telegramConfig.channelName) }
    var notifyWanDown by remember(telegramConfig) { mutableStateOf(telegramConfig.notifyWanDown) }
    var notifySecurityAlerts by remember(telegramConfig) { mutableStateOf(telegramConfig.notifySecurityAlerts) }
    var notifyHighCpu by remember(telegramConfig) { mutableStateOf(telegramConfig.notifyHighCpu) }
    var notifyClientSpike by remember(telegramConfig) { mutableStateOf(telegramConfig.notifyClientSpike) }
    var dailyDigestEnabled by remember(telegramConfig) { mutableStateOf(telegramConfig.dailyDigestEnabled) }

    // Grafana Local edit states
    var grafanaUrl by remember(grafanaConfig) { mutableStateOf(grafanaConfig.endpointUrl) }
    var grafanaApiKey by remember(grafanaConfig) { mutableStateOf(grafanaConfig.apiKey) }
    var dashboardUid by remember(grafanaConfig) { mutableStateOf(grafanaConfig.dashboardUid) }
    var refreshInterval by remember(grafanaConfig) { mutableIntStateOf(grafanaConfig.refreshIntervalSec) }

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
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Bot Notifikasi Telegram", fontWeight = FontWeight.Bold)
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
                        Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Grafana Live Telemetry", fontWeight = FontWeight.Bold)
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
                // Telegram Tab
                item {
                    NocCard(
                        borderColor = CyanNeon.copy(alpha = 0.4f),
                        backgroundColor = NocCardBgElevated
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CyanNeon)
                            Text(
                                text = "Integrasi Notifikasi Bot Telegram",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "NetGuard NOC secara real-time mengirimkan peringatan insiden keamanan, WAN down, lonjakan bandwidth, dan ringkasan SLA harian ke grup atau channel Telegram Anda.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Telegram Credentials Input Card
                item {
                    NocCard {
                        Text(
                            text = "Kredensial & Target Telegram",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = botToken,
                            onValueChange = { botToken = it },
                            label = { Text("Bot Token Telegram (dari @BotFather)") },
                            placeholder = { Text("123456789:ABCDefgh-123456...") },
                            modifier = Modifier.fillMaxWidth().testTag("telegram_token_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = chatId,
                            onValueChange = { chatId = it },
                            label = { Text("Chat ID / ID Channel Target (cth: -100123456789 atau @channel)") },
                            placeholder = { Text("-100192837465 atau @noc_alerts") },
                            modifier = Modifier.fillMaxWidth().testTag("telegram_chat_id_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = channelName,
                            onValueChange = { channelName = it },
                            label = { Text("Nama Alias / Label Saluran") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val updated = telegramConfig.copy(
                                        botToken = botToken,
                                        chatId = chatId,
                                        channelName = channelName,
                                        notifyWanDown = notifyWanDown,
                                        notifySecurityAlerts = notifySecurityAlerts,
                                        notifyHighCpu = notifyHighCpu,
                                        notifyClientSpike = notifyClientSpike,
                                        dailyDigestEnabled = dailyDigestEnabled
                                    )
                                    viewModel.updateTelegramConfig(updated)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = NocDarkBg)
                            ) {
                                Text("Simpan Konfigurasi", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val updated = telegramConfig.copy(botToken = botToken, chatId = chatId)
                                    viewModel.updateTelegramConfig(updated)
                                    viewModel.testTelegramBot()
                                },
                                enabled = !isTestingTelegram && botToken.isNotBlank() && chatId.isNotBlank(),
                                modifier = Modifier.weight(1f).testTag("test_telegram_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = NocDarkBg)
                            ) {
                                if (isTestingTelegram) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NocDarkBg, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kirim Tes Bot", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        telegramConfig.lastTestResult?.let { res ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (res.startsWith("Sukses")) GreenSuccess.copy(alpha = 0.15f) else RedCritical.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = res,
                                    color = if (res.startsWith("Sukses")) GreenSuccess else RedCritical,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // Alert Trigger Filters
                item {
                    NocCard {
                        Text(
                            text = "Filter Pemicu Notifikasi Otomatis",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchOptionRow("Peringatan WAN Gateway Down / Failover", notifyWanDown) { notifyWanDown = it }
                        SwitchOptionRow("Alert Keamanan (Port Scan, Rogue DHCP, DDoS)", notifySecurityAlerts) { notifySecurityAlerts = it }
                        SwitchOptionRow("CPU & Beban Perangkat Tinggi (>80%)", notifyHighCpu) { notifyHighCpu = it }
                        SwitchOptionRow("Lonjakan Klien Baru / Trafik Mencurigakan", notifyClientSpike) { notifyClientSpike = it }
                        SwitchOptionRow("Ringkasan SLA & Trafik Harian (Daily Digest)", dailyDigestEnabled) { dailyDigestEnabled = it }
                    }
                }
            } else {
                // Grafana Tab
                item {
                    NocCard(
                        borderColor = PurpleAccent.copy(alpha = 0.4f),
                        backgroundColor = NocCardBgElevated
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = PurpleAccent)
                            Text(
                                text = "Integrasi Grafana Live Dashboard",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Menghubungkan telemetry stream NetGuard ke Grafana Dashboard melalui REST API, Prometheus exporter, dan WebSocket live feeds.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                item {
                    NocCard {
                        Text(
                            text = "Koneksi Endpoint Grafana",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = grafanaUrl,
                            onValueChange = { grafanaUrl = it },
                            label = { Text("Grafana Endpoint Server URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = dashboardUid,
                            onValueChange = { dashboardUid = it },
                            label = { Text("Dashboard UID / Panel Identifier") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = grafanaApiKey,
                            onValueChange = { grafanaApiKey = it },
                            label = { Text("Service Account API Token (Bearer)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Interval Refresh Telemetry:", color = TextSecondary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 5, 10, 30).forEach { sec ->
                                FilterChip(
                                    selected = refreshInterval == sec,
                                    onClick = { refreshInterval = sec },
                                    label = { Text("${sec}s") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val updated = grafanaConfig.copy(
                                    endpointUrl = grafanaUrl,
                                    dashboardUid = dashboardUid,
                                    apiKey = grafanaApiKey,
                                    refreshIntervalSec = refreshInterval
                                )
                                viewModel.updateGrafanaConfig(updated)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = NocDarkBg)
                        ) {
                            Text("Simpan Pengaturan Grafana", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Prometheus & Webhook Target Card
                item {
                    NocCard {
                        Text("Prometheus & Metrics Webhook Exporter", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = NocDarkBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Prometheus Target Scrape URL:", color = TextSecondary, fontSize = 10.sp)
                                Text("http://192.168.10.1:9100/metrics", color = CyanNeon, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Grafana Live Push Webhook:", color = TextSecondary, fontSize = 10.sp)
                                Text("https://grafana.noc.internal/api/live/push/custom_stream", color = GreenSuccess, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwitchOptionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanNeon,
                checkedTrackColor = CyanNeon.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = NocDarkBg
            )
        )
    }
}
