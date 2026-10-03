package com.example

import com.example.data.model.*
import com.example.data.remote.TelegramApiService
import com.example.ui.screens.calculateSubnet
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSubnetCalculator_c24() {
        val result = calculateSubnet("192.168.10.1", 24)
        assertEquals("192.168.10.0", result.networkIp)
        assertEquals("255.255.255.0", result.netmask)
        assertEquals("192.168.10.1", result.firstUsable)
        assertEquals("192.168.10.254", result.lastUsable)
        assertEquals("192.168.10.255", result.broadcastIp)
        assertEquals(254L, result.usableHosts)
    }

    @Test
    fun testUserRole_permissions() {
        val superAdmin = UserRole.SUPER_ADMIN
        val viewer = UserRole.VIEWER

        assertTrue(superAdmin.canRebootDevice())
        assertTrue(superAdmin.canModifyFirewall())
        assertTrue(superAdmin.canManageUsers())

        assertFalse(viewer.canRebootDevice())
        assertFalse(viewer.canModifyFirewall())
        assertFalse(viewer.canManageUsers())
        assertTrue(viewer.canExportReports())
    }

    @Test
    fun testTelegramMessageFormatting() {
        val service = TelegramApiService()
        val formatted = service.formatIncidentAlert(
            title = "Rogue DHCP Server",
            severity = "CRITICAL",
            device = "MikroTik-CCR2004",
            ip = "192.168.20.254",
            details = "Unauthorized offer packet"
        )
        assertTrue(formatted.contains("NETGUARD NOC ALERT"))
        assertTrue(formatted.contains("Rogue DHCP Server"))
    }
}
