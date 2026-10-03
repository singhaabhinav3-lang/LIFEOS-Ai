package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticCard
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.VioletNeon

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initialName: String,
    onComplete: (fullName: String, style: String, focus: String, primeHours: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName.ifBlank { "Alex Mercer" }) }
    var selectedStyle by remember { mutableStateOf("Deep Work Specialist") }
    var selectedHours by remember { mutableStateOf("08:00 - 12:00 (Peak Morning Flow)") }
    val focusOptions = listOf("AI Architecture", "Engineering", "Health & Energy", "Career Growth", "Deep Learning", "Writing")
    val selectedFocus = remember { mutableStateOf(setOf("AI Architecture", "Engineering", "Health & Energy")) }

    val styles = listOf(
        "Deep Work Specialist" to "90-minute hyper-focused blocks with zero interruption",
        "Pomodoro Sprint" to "25-minute sprints with 5-minute active recovery",
        "Time Blocker" to "Rigid calendar blocks aligned to high leverage goals",
        "Agile Flow" to "Dynamic task backlog with high contextual adaptability"
    )

    val hourOptions = listOf(
        "08:00 - 12:00 (Peak Morning Flow)",
        "13:00 - 17:00 (Afternoon Execution)",
        "19:00 - 23:00 (Night Owl Solitude)"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.2f))
                    .border(1.dp, CyanNeon, CircleShape)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "System Calibration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Personalizing LIFEOS to your cognitive rhythm",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step 1: Identity
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "1. Identity & Call Sign",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What should LIFEOS call you?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Productivity Style
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "2. Cognitive Operating Mode",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.height(12.dp))
                styles.forEach { (style, desc) ->
                    val isSelected = selectedStyle == style
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) CyanPrimary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) CyanNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedStyle = style }
                            .padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) CyanNeon else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = style, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 3: Prime Hours
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "3. Peak Neuro-Energy Hours",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.height(8.dp))
                hourOptions.forEach { hourStr ->
                    val isSelected = selectedHours == hourStr
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) VioletNeon.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) VioletNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedHours = hourStr }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = if (isSelected) VioletNeon else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = hourStr,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 4: Focus Areas
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "4. Core Life Focus Areas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    focusOptions.forEach { tag ->
                        val isSelected = selectedFocus.value.contains(tag)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) EmeraldNeon.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    val current = selectedFocus.value.toMutableSet()
                                    if (isSelected) current.remove(tag) else current.add(tag)
                                    selectedFocus.value = current
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) EmeraldNeon else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val focusStr = selectedFocus.value.joinToString(", ")
                onComplete(name, selectedStyle, focusStr, selectedHours)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("calibrate_system_button")
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.background)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Calibrate & Launch LIFEOS",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
