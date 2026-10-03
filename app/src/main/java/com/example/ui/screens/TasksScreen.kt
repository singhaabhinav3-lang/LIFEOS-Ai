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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.CategoryChip
import com.example.ui.components.FuturisticCard
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.RoseNeon

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    goals: List<GoalEntity>,
    showAddTaskDialog: Boolean,
    onOpenAddTaskDialog: () -> Unit,
    onCloseAddTaskDialog: () -> Unit,
    onToggleTask: (taskId: String, currentState: Boolean) -> Unit,
    onDeleteTask: (taskId: String) -> Unit,
    onCreateTask: (
        title: String,
        description: String,
        priority: String,
        dueDate: String,
        goalId: String?,
        goalTitle: String?,
        estimatedMinutes: Int
    ) -> Unit
) {
    var selectedStatusTab by remember { mutableIntStateOf(0) } // 0: Pending, 1: Completed, 2: All
    var selectedPriorityFilter by remember { mutableStateOf("ALL") }

    val filteredTasks = tasks.filter { task ->
        val statusMatches = when (selectedStatusTab) {
            0 -> !task.isCompleted
            1 -> task.isCompleted
            else -> true
        }
        val priorityMatches = selectedPriorityFilter == "ALL" || task.priority.equals(selectedPriorityFilter, ignoreCase = true)
        statusMatches && priorityMatches
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
                            text = "Task Operations",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${tasks.count { !it.isCompleted }} pending • ${tasks.count { it.isCompleted }} executed",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onOpenAddTaskDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("create_task_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Task", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Tabs
                TabRow(
                    selectedTabIndex = selectedStatusTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = CyanNeon,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedStatusTab]),
                            color = CyanNeon
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedStatusTab == 0,
                        onClick = { selectedStatusTab = 0 },
                        text = { Text("Pending (${tasks.count { !it.isCompleted }})", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedStatusTab == 1,
                        onClick = { selectedStatusTab = 1 },
                        text = { Text("Completed (${tasks.count { it.isCompleted }})", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedStatusTab == 2,
                        onClick = { selectedStatusTab = 2 },
                        text = { Text("All (${tasks.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Filter Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listOf("ALL", "P1", "P2", "P3")) { p ->
                        CategoryChip(
                            category = if (p == "ALL") "All Priorities" else "$p Priority",
                            isSelected = selectedPriorityFilter == p,
                            onClick = { selectedPriorityFilter = p }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredTasks.isEmpty()) {
                item {
                    FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Text(
                                text = if (selectedStatusTab == 0) "No pending tasks in queue" else "No matching tasks found",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Keep execution lean and focused on high-leverage goals",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    TaskCardItem(
                        task = task,
                        onToggle = { onToggleTask(task.id, task.isCompleted) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            goals = goals,
            onDismiss = onCloseAddTaskDialog,
            onConfirm = onCreateTask
        )
    }
}

@Composable
fun TaskCardItem(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    FuturisticCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (task.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        borderColor = if (task.priority == "P1" && !task.isCompleted)
            RoseNeon.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle completion",
                    tint = if (task.isCompleted) EmeraldNeon else CyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    lineHeight = 18.sp
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(priority = task.priority)

                    if (task.dueDate.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${task.dueDate} • ${task.estimatedMinutes}m",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (task.goalTitle != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = task.goalTitle,
                                fontSize = 11.sp,
                                color = CyanPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 100.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    goals: List<GoalEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        priority: String,
        dueDate: String,
        goalId: String?,
        goalTitle: String?,
        estimatedMinutes: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("P1") }
    var dueDate by remember { mutableStateOf("Today") }
    var estimatedMinutes by remember { mutableIntStateOf(45) }

    var selectedGoal by remember { mutableStateOf<GoalEntity?>(goals.firstOrNull()) }
    var isGoalDropdownExpanded by remember { mutableStateOf(false) }

    val priorities = listOf("P1", "P2", "P3")
    val dueDates = listOf("Today", "Tomorrow", "This Week", "Upcoming")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Actionable Task", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Action Item") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Context & Deliverable") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Priority Level:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (priority == p) CyanPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (priority == p) CyanNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { priority = p }
                        ) {
                            Text(
                                text = when (p) {
                                    "P1" -> "P1 • Urgent"
                                    "P2" -> "P2 • Core"
                                    else -> "P3 • Standard"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (priority == p) CyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Due Cadence:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dueDates) { d ->
                        CategoryChip(
                            category = d,
                            isSelected = dueDate == d,
                            onClick = { dueDate = d }
                        )
                    }
                }

                if (goals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Link to Strategic Goal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = isGoalDropdownExpanded,
                        onExpandedChange = { isGoalDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedGoal?.title ?: "None (Unlinked)",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGoalDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isGoalDropdownExpanded,
                            onDismissRequest = { isGoalDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Unlinked)") },
                                onClick = {
                                    selectedGoal = null
                                    isGoalDropdownExpanded = false
                                }
                            )
                            goals.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        selectedGoal = g
                                        isGoalDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            description,
                            priority,
                            dueDate,
                            selectedGoal?.id,
                            selectedGoal?.title,
                            estimatedMinutes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("confirm_create_task_btn")
            ) {
                Text("Add to Queue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
