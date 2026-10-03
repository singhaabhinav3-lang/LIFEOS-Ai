package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPlanEntity
import com.example.data.model.TimeBlockItem
import com.example.ui.components.FocusScoreRing
import com.example.ui.components.FuturisticCard
import com.example.ui.components.LinearProgressStyled
import com.example.ui.theme.AmberNeon
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.VioletNeon
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Composable
fun DailyPlanScreen(
    dailyPlan: DailyPlanEntity?,
    isAiLoading: Boolean,
    onPlanMyDay: () -> Unit,
    onToggleTimeBlock: (plan: DailyPlanEntity, blockId: String) -> Unit
) {
    val blocks = if (dailyPlan != null) parseTimeBlocks(dailyPlan.timeBlocksJson) else emptyList()
    val completedBlocks = blocks.count { it.isCompleted }
    val progressPercent = if (blocks.isNotEmpty()) (completedBlocks.toFloat() / blocks.size * 100f) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Daily Schedule",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Time-blocked execution anchored to prime hours",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onPlanMyDay,
                    enabled = !isAiLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("regenerate_plan_button")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.background)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plan My Day", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Executive Summary Card
            FuturisticCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CyanNeon.copy(alpha = 0.35f)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Focus Thesis for Today",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldNeon.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "FOCUS ${dailyPlan?.focusScore ?: 94}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldNeon,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dailyPlan?.summary
                            ?: "No schedule calibrated for today yet. Tap 'Plan My Day' to synthesize your optimal time blocks based on your goals and pending tasks.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    if (blocks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$completedBlocks of ${blocks.size} blocks completed",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${progressPercent.toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressStyled(progress = progressPercent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Chronological Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (blocks.isEmpty()) {
            item {
                FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Schedule Synthesized",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onPlanMyDay, colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                            Text("Generate AI Daily Plan")
                        }
                    }
                }
            }
        } else {
            items(blocks) { block ->
                TimeBlockCard(
                    block = block,
                    onToggle = {
                        if (dailyPlan != null) {
                            onToggleTimeBlock(dailyPlan, block.id)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TimeBlockCard(
    block: TimeBlockItem,
    onToggle: () -> Unit
) {
    val (categoryColor, categoryBg) = when (block.category) {
        "Deep Focus" -> Pair(CyanNeon, CyanPrimary.copy(alpha = 0.2f))
        "Execution" -> Pair(VioletNeon, VioletNeon.copy(alpha = 0.2f))
        "Review" -> Pair(AmberNeon, AmberNeon.copy(alpha = 0.2f))
        else -> Pair(EmeraldNeon, EmeraldNeon.copy(alpha = 0.2f))
    }

    FuturisticCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (block.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        borderColor = if (block.isCompleted)
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        else categoryColor.copy(alpha = 0.4f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (block.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle time block",
                    tint = if (block.isCompleted) EmeraldNeon else categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = categoryColor, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${block.startTime} — ${block.endTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryBg
                    ) {
                        Text(
                            text = block.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = block.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (block.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (block.isCompleted) TextDecoration.LineThrough else null,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun parseTimeBlocks(json: String): List<TimeBlockItem> {
    val list = mutableListOf<TimeBlockItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                TimeBlockItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    startTime = obj.optString("startTime", "09:00"),
                    endTime = obj.optString("endTime", "10:00"),
                    title = obj.optString("title", ""),
                    category = obj.optString("category", "Deep Focus"),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    linkedTaskId = obj.optString("linkedTaskId").takeIf { it.isNotBlank() }
                )
            )
        }
    } catch (_: Exception) {}
    return list
}
