package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RealtimeMetricPoint
import com.example.ui.theme.*

@Composable
fun RealtimeTrafficWaveChart(
    history: List<RealtimeMetricPoint>,
    modifier: Modifier = Modifier,
    heightDp: Int = 160
) {
    val downloadColor = CyanNeon
    val uploadColor = GreenSuccess

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NocDarkBg.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        // Chart Header Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(downloadColor, RoundedCornerShape(2.dp)))
                    val currentDl = history.lastOrNull()?.downloadMbps ?: 0.0
                    Text(
                        text = "RX (Download): ${"%.1f".format(currentDl)} Mbps",
                        color = downloadColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(uploadColor, RoundedCornerShape(2.dp)))
                    val currentUl = history.lastOrNull()?.uploadMbps ?: 0.0
                    Text(
                        text = "TX (Upload): ${"%.1f".format(currentUl)} Mbps",
                        color = uploadColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "Grafana Telemetry Live",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Drawing Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
        ) {
            val width = size.width
            val height = size.height

            if (history.size < 2) return@Canvas

            // Draw horizontal grid lines
            val gridCount = 4
            for (i in 0..gridCount) {
                val y = height * (i.toFloat() / gridCount)
                drawLine(
                    color = NocCardBorder.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            val maxSpeed = (history.maxOfOrNull { maxOf(it.downloadMbps, it.uploadMbps) } ?: 500.0)
                .coerceAtLeast(100.0) * 1.15

            val stepX = width / (history.size - 1)

            // Build Download (RX) Path
            val dlPath = Path()
            val dlFillPath = Path()

            val ulPath = Path()
            val ulFillPath = Path()

            history.forEachIndexed { index, point ->
                val x = index * stepX
                val dlNorm = (point.downloadMbps / maxSpeed).coerceIn(0.0, 1.0)
                val ulNorm = (point.uploadMbps / maxSpeed).coerceIn(0.0, 1.0)

                val dlY = (height - (dlNorm * height)).toFloat()
                val ulY = (height - (ulNorm * height)).toFloat()

                if (index == 0) {
                    dlPath.moveTo(x, dlY)
                    dlFillPath.moveTo(x, height)
                    dlFillPath.lineTo(x, dlY)

                    ulPath.moveTo(x, ulY)
                    ulFillPath.moveTo(x, height)
                    ulFillPath.lineTo(x, ulY)
                } else {
                    dlPath.lineTo(x, dlY)
                    dlFillPath.lineTo(x, dlY)

                    ulPath.lineTo(x, ulY)
                    ulFillPath.lineTo(x, ulY)
                }
            }

            dlFillPath.lineTo(width, height)
            dlFillPath.close()

            ulFillPath.lineTo(width, height)
            ulFillPath.close()

            // Draw Fills
            drawPath(
                path = dlFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(downloadColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            drawPath(
                path = ulFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(uploadColor.copy(alpha = 0.15f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Lines
            drawPath(
                path = dlPath,
                color = downloadColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            drawPath(
                path = ulPath,
                color = uploadColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Pulse point at latest position
            val lastX = (history.size - 1) * stepX
            val lastDlY = (height - ((history.last().downloadMbps / maxSpeed).coerceIn(0.0, 1.0) * height)).toFloat()
            val lastUlY = (height - ((history.last().uploadMbps / maxSpeed).coerceIn(0.0, 1.0) * height)).toFloat()

            drawCircle(
                color = downloadColor,
                radius = 5.dp.toPx(),
                center = Offset(lastX, lastDlY)
            )

            drawCircle(
                color = uploadColor,
                radius = 4.dp.toPx(),
                center = Offset(lastX, lastUlY)
            )
        }
    }
}
