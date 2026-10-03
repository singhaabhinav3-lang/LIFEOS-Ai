package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.GoalEntity
import com.example.data.model.MilestoneItem
import com.example.ui.components.CategoryChip
import com.example.ui.components.FuturisticCard
import com.example.ui.components.LinearProgressStyled
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.RoseNeon
import com.example.ui.theme.VioletNeon
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    isAiLoading: Boolean,
    showAddGoalDialog: Boolean,
    showGoalPlannerModal: Boolean,
    selectedGoalForPlanning: GoalEntity?,
    onOpenAddGoalDialog: () -> Unit,
    onCloseAddGoalDialog: () -> Unit,
    onCreateGoal: (title: String, desc: String, category: String, deadline: String) -> Unit,
    onOpenGoalPlanner: (GoalEntity) -> Unit,
    onCloseGoalPlanner: () -> Unit,
    onGenerateGoalPlan: (GoalEntity) -> Unit,
    onUpdateProgress: (goalId: String, progress: Float) -> Unit,
    onDeleteGoal: (goalId: String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "Engineering", "Career", "Learning", "Health", "Creative")

    val filteredGoals = goals.filter {
        selectedCategory == "ALL" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                            text = "Strategic Goals",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "High-leverage targets linked to AI milestones",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onOpenAddGoalDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_goal_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Goal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Filter Bar
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        CategoryChip(
                            category = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredGoals.isEmpty()) {
                item {
                    FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No goals in this category",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Set ambitious milestones and let LIFEOS decompose them",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredGoals) { goal ->
                    GoalCardItem(
                        goal = goal,
                        onOpenGoalPlanner = { onOpenGoalPlanner(goal) },
                        onUpdateProgress = { newProgress -> onUpdateProgress(goal.id, newProgress) },
                        onDeleteGoal = { onDeleteGoal(goal.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            categories = categories.filter { it != "ALL" },
            onDismiss = onCloseAddGoalDialog,
            onConfirm = { title, desc, cat, deadline ->
                onCreateGoal(title, desc, cat, deadline)
            }
        )
    }

    // AI Goal Planner Modal
    if (showGoalPlannerModal && selectedGoalForPlanning != null) {
        AiGoalPlannerDialog(
            goal = selectedGoalForPlanning,
            isAiLoading = isAiLoading,
            onDismiss = onCloseGoalPlanner,
            onDeconstruct = { onGenerateGoalPlan(selectedGoalForPlanning) }
        )
    }
}

@Composable
fun GoalCardItem(
    goal: GoalEntity,
    onOpenGoalPlanner: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onDeleteGoal: () -> Unit
) {
    val milestones = parseMilestones(goal.milestonesJson)
    var sliderValue by remember(goal.progress) { mutableFloatStateOf(goal.progress) }

    FuturisticCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (goal.progress >= 100f) EmeraldNeon.copy(alpha = 0.5f) else CyanPrimary.copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VioletNeon.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = goal.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletNeon,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = goal.deadline,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDeleteGoal, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = goal.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar & Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Progress",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${sliderValue.toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (sliderValue >= 100f) EmeraldNeon else CyanNeon
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressStyled(progress = sliderValue)

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onUpdateProgress(sliderValue) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.height(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Milestones Preview
            if (milestones.isNotEmpty()) {
                Text(
                    text = "Milestone Roadmap (${milestones.count { it.isDone }}/${milestones.size} complete):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                milestones.forEach { m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (m.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (m.isDone) EmeraldNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = m.title,
                            fontSize = 12.sp,
                            color = if (m.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = m.targetWeek,
                            fontSize = 10.sp,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // AI Goal Planner CTA Button
            OutlinedButton(
                onClick = onOpenGoalPlanner,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_goal_planner_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (milestones.isEmpty()) "AI Goal Planner • Auto-Deconstruct" else "AI Planner • Regenerate Roadmap",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, category: String, deadline: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Engineering") }
    var deadline by remember { mutableStateOf("2026-11-30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Strategic Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_goal_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Scope & Purpose") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Target Deadline (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        CategoryChip(
                            category = cat,
                            isSelected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, category, deadline)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Index Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AiGoalPlannerDialog(
    goal: GoalEntity,
    isAiLoading: Boolean,
    onDismiss: () -> Unit,
    onDeconstruct: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isAiLoading) onDismiss() },
        icon = {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(32.dp))
        },
        title = {
            Text("AI Strategic Goal Planner", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Goal: ${goal.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The LIFEOS AI engine will decompose this target into:\n" +
                            "• 3-4 structured execution milestones\n" +
                            "• Concrete actionable tasks with P1/P2 priorities\n" +
                            "• Automatic linking directly into your Task Queue",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                if (isAiLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyanNeon)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Deconstructing architecture...", fontSize = 12.sp, color = CyanPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDeconstruct,
                enabled = !isAiLoading,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("confirm_ai_deconstruct_btn")
            ) {
                Text(if (isAiLoading) "Processing..." else "Generate Plan & Tasks")
            }
        },
        dismissButton = {
            if (!isAiLoading) {
                TextButton(onClick = onDismiss) { Text("Dismiss") }
            }
        }
    )
}

private fun parseMilestones(json: String): List<MilestoneItem> {
    val list = mutableListOf<MilestoneItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                MilestoneItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    title = obj.optString("title", ""),
                    targetWeek = obj.optString("targetWeek", ""),
                    isDone = obj.optBoolean("isDone", false)
                )
            )
        }
    } catch (_: Exception) {}
    return list
}
