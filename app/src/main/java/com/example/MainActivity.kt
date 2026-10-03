package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LivePulseIndicator
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NetworkViewModel
import com.example.ui.viewmodel.NocNavTab
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val viewModel: NetworkViewModel = viewModel()
                NetGuardNocApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetGuardNocApp(viewModel: NetworkViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val snackBarMessage by viewModel.snackBarMessage.collectAsState()
    val unresolvedAlertsCount by viewModel.unresolvedAlertsCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackBarMessage) {
        snackBarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Back handling: if not in Dashboard, go back to Dashboard
    BackHandler(enabled = currentTab != NocNavTab.DASHBOARD) {
        viewModel.selectTab(NocNavTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NocDarkBg,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NocCardBgElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            NocAppBar(
                currentTab = currentTab,
                unresolvedAlertsCount = unresolvedAlertsCount,
                userName = currentUser.fullName,
                userInitials = currentUser.avatarInitials,
                onUserClick = { viewModel.selectTab(NocNavTab.REPORTS_RBAC) },
                onAlertsClick = { viewModel.selectTab(NocNavTab.SECURITY) }
            )
        },
        bottomBar = {
            NocBottomNavigationBar(
                currentTab = currentTab,
                unresolvedAlertsCount = unresolvedAlertsCount,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "tab_transition",
            modifier = Modifier.padding(innerPadding)
        ) { tab ->
            when (tab) {
                NocNavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                NocNavTab.DEVICES -> DevicesScreen(viewModel = viewModel)
                NocNavTab.CLIENTS_VLAN -> ClientsVlanScreen(viewModel = viewModel)
                NocNavTab.SECURITY -> SecurityScreen(viewModel = viewModel)
                NocNavTab.GRAFANA_TELEGRAM -> GrafanaTelegramScreen(viewModel = viewModel)
                NocNavTab.LOGS -> LogsAuditScreen(viewModel = viewModel)
                NocNavTab.REPORTS_RBAC -> ReportsRbacScreen(viewModel = viewModel)
                NocNavTab.TOOLS -> ToolsScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NocAppBar(
    currentTab: NocNavTab,
    unresolvedAlertsCount: Int,
    userName: String,
    userInitials: String,
    onUserClick: () -> Unit,
    onAlertsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = CyanNeon.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NetGuard NOC",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LivePulseIndicator(color = GreenSuccess)
                    }
                    Text(
                        text = "Multi-Vendor Telemetry • ${currentTab.title}",
                        color = CyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        actions = {
            // Security alert quick icon
            IconButton(onClick = onAlertsClick, modifier = Modifier.testTag("appbar_alert_btn")) {
                BadgedBox(
                    badge = {
                        if (unresolvedAlertsCount > 0) {
                            Badge(containerColor = RedCritical, contentColor = TextPrimary) {
                                Text("$unresolvedAlertsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = if (unresolvedAlertsCount > 0) RedCritical else TextSecondary
                    )
                }
            }

            // User Profile Avatar Chip
            Surface(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clip(CircleShape)
                    .clickable { onUserClick() },
                color = CyanNeon.copy(alpha = 0.2f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon)
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        color = CyanNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NocSurfaceDark,
            titleContentColor = TextPrimary
        ),
        windowInsets = WindowInsets.statusBars
    )
}

@Composable
fun NocBottomNavigationBar(
    currentTab: NocNavTab,
    unresolvedAlertsCount: Int,
    onTabSelected: (NocNavTab) -> Unit
) {
    val navItems = listOf(
        NocNavTab.DASHBOARD to Icons.Default.Dashboard,
        NocNavTab.DEVICES to Icons.Default.Router,
        NocNavTab.CLIENTS_VLAN to Icons.Default.Devices,
        NocNavTab.SECURITY to Icons.Default.Shield,
        NocNavTab.GRAFANA_TELEGRAM to Icons.Default.QueryStats,
        NocNavTab.TOOLS to Icons.Default.Build,
        NocNavTab.REPORTS_RBAC to Icons.Default.Summarize
    )

    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("noc_bottom_nav"),
        containerColor = NocSurfaceDark,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { (tab, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == NocNavTab.SECURITY && unresolvedAlertsCount > 0) {
                                Badge(containerColor = RedCritical) {
                                    Text("$unresolvedAlertsCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NocDarkBg,
                    selectedTextColor = CyanNeon,
                    indicatorColor = CyanNeon,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
