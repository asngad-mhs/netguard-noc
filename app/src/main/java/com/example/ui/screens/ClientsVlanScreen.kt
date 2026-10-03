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

@Composable
fun ClientsVlanScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val clients by viewModel.clients.collectAsState()
    val vlans by viewModel.vlans.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedVlanId by viewModel.selectedVlanFilter.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedClientForModal by remember { mutableStateOf<DhcpClient?>(null) }

    val filteredClients = clients.filter { client ->
        (selectedVlanId == null || client.vlanId == selectedVlanId) &&
        (searchQuery.isBlank() || client.hostname.contains(searchQuery, ignoreCase = true) ||
         client.ipAddress.contains(searchQuery, ignoreCase = true) ||
         client.macAddress.contains(searchQuery, ignoreCase = true) ||
         client.vendorOui.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Segmented Switcher
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = NocCardBg,
            contentColor = CyanNeon,
            divider = {},
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NocCardBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Klien DHCP (${clients.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("VLAN & Subnet (${vlans.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTabIndex == 0) {
            // DHCP Clients View
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("client_search_input"),
                placeholder = { Text("Cari Hostname, IP, MAC, OUI...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanNeon) },
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

            Spacer(modifier = Modifier.height(8.dp))

            // VLAN Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedVlanId == null,
                        onClick = { viewModel.setVlanFilter(null) },
                        label = { Text("Semua VLAN") }
                    )
                }
                items(vlans) { vlan ->
                    FilterChip(
                        selected = selectedVlanId == vlan.vlanId,
                        onClick = { viewModel.setVlanFilter(if (selectedVlanId == vlan.vlanId) null else vlan.vlanId) },
                        label = { Text("VLAN ${vlan.vlanId} (${vlan.name})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clients List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(filteredClients, key = { it.id }) { client ->
                    ClientRowCard(
                        client = client,
                        onClick = { selectedClientForModal = client },
                        onToggleBlock = {
                            viewModel.toggleClientBlock(client.id, !client.isBlocked, client.hostname)
                        }
                    )
                }
            }
        } else {
            // VLAN Management View
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    NocCard(
                        borderColor = CyanNeon.copy(alpha = 0.4f),
                        backgroundColor = NocCardBgElevated
                    ) {
                        Text(
                            text = "Konfigurasi Segmentasi Jaringan (802.1Q)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "VLAN memisahkan trafik manajemen NOC, staf kantor, tamu publik, server, dan perangkat IoT/CCTV secara terisolasi.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                items(vlans) { vlan ->
                    VlanCardItem(vlan = vlan)
                }
            }
        }
    }

    // Client Action Modal Dialog
    selectedClientForModal?.let { client ->
        AlertDialog(
            onDismissRequest = { selectedClientForModal = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LaptopMac, contentDescription = null, tint = CyanNeon)
                    Text(client.hostname, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Detail Klien Jaringan:", color = TextSecondary, fontSize = 12.sp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NocDarkBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("IP Address", color = TextSecondary, fontSize = 10.sp)
                            Text(client.ipAddress, color = CyanNeon, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("MAC Address", color = TextSecondary, fontSize = 10.sp)
                            Text(client.macAddress, color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Vendor OUI:", color = TextSecondary, fontSize = 12.sp)
                        Text(client.vendorOui, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("VLAN ID:", color = TextSecondary, fontSize = 12.sp)
                        Text("VLAN ${client.vlanId}", color = TextHighlight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Terhubung Pada:", color = TextSecondary, fontSize = 12.sp)
                        Text(client.connectedDevice, color = TextPrimary, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total Trafik Digunakan:", color = TextSecondary, fontSize = 12.sp)
                        Text("${"%.1f".format(client.totalUsageMb / 1024.0)} GB", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (client.isSuspicious) {
                        Surface(
                            color = RedCritical.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedCritical)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RedCritical, modifier = Modifier.size(16.dp))
                                Text("Terdeteksi Port Scanning / Trafik Anomali", color = RedCritical, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleClientBlock(client.id, !client.isBlocked, client.hostname)
                        selectedClientForModal = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (client.isBlocked) GreenSuccess else RedCritical,
                        contentColor = NocDarkBg
                    )
                ) {
                    Text(
                        if (client.isBlocked) "Buka Blokir Firewall" else "Blokir Akses Jaringan",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleClientStatic(client.id, !client.isStaticReservation, client.hostname)
                        selectedClientForModal = null
                    }
                ) {
                    Text(if (client.isStaticReservation) "Lepas IP Statis" else "Jadikan IP Statis", color = CyanNeon)
                }
            },
            containerColor = NocCardBgElevated
        )
    }
}

@Composable
fun ClientRowCard(
    client: DhcpClient,
    onClick: () -> Unit,
    onToggleBlock: () -> Unit
) {
    NocCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        borderColor = if (client.isBlocked) RedCritical else if (client.isSuspicious) AmberWarning else NocCardBorder
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (client.isBlocked) RedCritical.copy(alpha = 0.2f)
                            else if (client.isSuspicious) AmberWarning.copy(alpha = 0.2f)
                            else CyanNeon.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (client.isBlocked) Icons.Default.Block else Icons.Default.Laptop,
                        contentDescription = null,
                        tint = if (client.isBlocked) RedCritical else if (client.isSuspicious) AmberWarning else CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = client.hostname,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (client.isStaticReservation) {
                            Surface(
                                color = PurpleAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "STATIS",
                                    color = PurpleAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${client.ipAddress} • ${client.macAddress}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = NocDarkBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "VLAN ${client.vlanId}",
                        color = TextHighlight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "↓ ${"%.1f".format(client.downloadSpeedKbps / 1000.0)} Mbps",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun VlanCardItem(vlan: VlanInfo) {
    NocCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = NocCardBorder
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
                Surface(
                    color = CyanNeon.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon)
                ) {
                    Text(
                        text = "VLAN ${vlan.vlanId}",
                        color = CyanNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Column {
                    Text(
                        text = vlan.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${vlan.subnet} • Gateway ${vlan.gateway}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (vlan.isIsolated) {
                Surface(
                    color = RedCritical.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "ISOLATED",
                        color = RedCritical,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Capacity Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Klien Terkoneksi: ${vlan.activeClients} / ${vlan.maxCapacity}", color = TextSecondary, fontSize = 11.sp)
            Text("DHCP Pool: ${vlan.dhcpPoolRange.split(" - ").first().substringAfterLast(".")}-${vlan.dhcpPoolRange.substringAfterLast(".")}", color = TextHighlight, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(4.dp))
        val progress = (vlan.activeClients.toFloat() / vlan.maxCapacity).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (progress > 0.85f) RedCritical else GreenSuccess,
            trackColor = NocCardBorder
        )
    }
}
