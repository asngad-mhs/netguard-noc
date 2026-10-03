package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TelegramApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun sendTelegramMessage(
        botToken: String,
        chatId: String,
        messageText: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = botToken.trim()
            val cleanChatId = chatId.trim()
            if (cleanToken.isEmpty() || cleanChatId.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Bot Token dan Chat ID Telegram harus diisi!"))
            }

            val url = "https://api.telegram.org/bot$cleanToken/sendMessage"
            val jsonBody = JSONObject().apply {
                put("chat_id", cleanChatId)
                put("text", messageText)
                put("parse_mode", "Markdown")
                put("disable_web_page_preview", true)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Result.success("Pesan Telegram berhasil terkirim ke $cleanChatId")
                } else {
                    val errorDesc = try {
                        val obj = JSONObject(bodyString)
                        obj.optString("description", "Error: ${response.code}")
                    } catch (e: Exception) {
                        "Error HTTP ${response.code}"
                    }
                    Result.failure(Exception(errorDesc))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun formatIncidentAlert(
        title: String,
        severity: String,
        device: String,
        ip: String,
        details: String
    ): String {
        val emoji = when (severity.uppercase()) {
            "CRITICAL" -> "🚨"
            "WARNING" -> "⚠️"
            else -> "ℹ️"
        }
        return """
            $emoji *[NETGUARD NOC ALERT - $severity]*
            ━━━━━━━━━━━━━━━━━━━━━
            📌 *Insiden:* `$title`
            🖥️ *Perangkat:* `$device`
            🌐 *Sumber IP:* `$ip`
            📝 *Detail:* $details
            ⏱️ *Waktu:* `Sekarang`
            ━━━━━━━━━━━━━━━━━━━━━
            🛡️ _NetGuard Network Operations Center_
        """.trimIndent()
    }

    fun formatDailyDigest(
        slaPercent: Double,
        downloadGb: Double,
        uploadGb: Double,
        activeClients: Int,
        openAlerts: Int
    ): String {
        return """
            📊 *[NETGUARD NOC - LAPORAN KINERJA HARIAN]*
            ━━━━━━━━━━━━━━━━━━━━━
            🟢 *WAN SLA Uptime:* `${"%.2f".format(slaPercent)}%`
            📥 *Total Download:* `${"%.1f".format(downloadGb)} GB`
            📤 *Total Upload:* `${"%.1f".format(uploadGb)} GB`
            👥 *Klien Aktif Puncak:* `$activeClients Perangkat`
            🚨 *Insiden Keamanan:* `$openAlerts Terdeteksi`
            ━━━━━━━━━━━━━━━━━━━━━
            ✅ _Semua gateway multi-vendor terpantau stabil._
        """.trimIndent()
    }
}
