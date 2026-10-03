package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DailyPlanEntity
import com.example.data.model.GoalEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.MilestoneItem
import com.example.data.model.TaskEntity
import com.example.data.model.TimeBlockItem
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LifeOsRepository(private val database: AppDatabase) {
    val activeProfile: Flow<UserProfileEntity?> = database.profileDao().getActiveProfile()

    fun getGoals(userId: String): Flow<List<GoalEntity>> = database.goalDao().getGoalsByUser(userId)
    fun getTasks(userId: String): Flow<List<TaskEntity>> = database.taskDao().getTasksByUser(userId)
    fun getMemories(userId: String): Flow<List<MemoryEntity>> = database.memoryDao().getMemoriesByUser(userId)
    fun getLatestDailyPlan(userId: String): Flow<DailyPlanEntity?> = database.dailyPlanDao().getLatestPlan(userId)
    fun getChatMessages(userId: String): Flow<List<ChatMessageEntity>> = database.chatDao().getChatMessages(userId)

    suspend fun checkSessionOnStart(firebaseUid: String?, firebaseEmail: String?, firebaseName: String?) = withContext(Dispatchers.IO) {
        if (firebaseUid != null) {
            activateFirebaseUser(firebaseUid, firebaseEmail ?: "", firebaseName)
        } else {
            // Strictly enforce: No user allowed until signed in
            database.profileDao().clearProfiles()
        }
    }

    suspend fun activateFirebaseUser(userId: String, email: String, name: String?) = withContext(Dispatchers.IO) {
        val existing = database.profileDao().getProfileById(userId)
        val displayName = if (!name.isNullOrBlank()) name else email.substringBefore("@").replace(".", " ").capitalize()
        if (existing == null) {
            seedUserData(userId, email, displayName, isOnboarded = false)
        } else {
            database.profileDao().insertProfile(existing.copy(email = email, fullName = displayName))
        }
    }

    suspend fun seedUserData(userId: String, email: String, name: String, isOnboarded: Boolean = false) = withContext(Dispatchers.IO) {
        val profile = UserProfileEntity(
            id = userId,
            email = email,
            fullName = name,
            productivityStyle = "Deep Work Specialist",
            focusAreas = "Autonomous AI, High Performance Engineering, Bio-Optimization",
            primeHours = "08:00 - 12:00 (Peak Morning Flow)",
            dailyTargetHours = 6,
            isOnboarded = isOnboarded,
            themePreference = "dark",
            isDemoMode = false
        )
        database.profileDao().insertProfile(profile)

        // Seed initial starter goals for the authenticated Firebase user
        val g1Milestones = listOf(
            MilestoneItem(UUID.randomUUID().toString(), "Define System Specs & Neural Flow", "Week 1", true),
            MilestoneItem(UUID.randomUUID().toString(), "Deploy Core Reasoning Engine & DB", "Week 2", true),
            MilestoneItem(UUID.randomUUID().toString(), "Real-time AI Chat & Goal Linkage", "Week 3", true),
            MilestoneItem(UUID.randomUUID().toString(), "User Testing & Multi-device Polish", "Week 4", false)
        )
        val g2Milestones = listOf(
            MilestoneItem(UUID.randomUUID().toString(), "Complete Consensus Algorithms Module", "Week 1", true),
            MilestoneItem(UUID.randomUUID().toString(), "Benchmarking Distributed Transaction Throughput", "Week 2", false),
            MilestoneItem(UUID.randomUUID().toString(), "Publish Comprehensive Technical Whitepaper", "Week 3", false)
        )

        val goal1Id = "goal_${userId}_lifeos"
        val goal2Id = "goal_${userId}_dist"

        val goal1 = GoalEntity(
            id = goal1Id,
            userId = userId,
            title = "Launch LIFEOS AI Operating System",
            description = "High-fidelity mobile productivity system with offline Room persistence and AI copilot.",
            category = "Engineering",
            deadline = "2026-10-25",
            progress = 75f,
            status = "active",
            milestonesJson = serializeMilestones(g1Milestones)
        )

        val goal2 = GoalEntity(
            id = goal2Id,
            userId = userId,
            title = "Master High-Throughput Distributed Engines",
            description = "Study Raft, Paxos, and distributed state machines for large-scale coordination.",
            category = "Learning",
            deadline = "2026-11-15",
            progress = 35f,
            status = "active",
            milestonesJson = serializeMilestones(g2Milestones)
        )

        database.goalDao().clearUserGoals(userId)
        database.goalDao().insertGoals(listOf(goal1, goal2))

        // Seed initial tasks for the authenticated user
        val tasks = listOf(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Calibrate AI Daily Schedule with prime energy hours",
                description = "Ensure high priority P1 tasks receive anchor slots.",
                priority = "P1",
                dueDate = "Today",
                isCompleted = false,
                goalId = goal1Id,
                goalTitle = goal1.title,
                estimatedMinutes = 60
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Setup Firebase Security rules and user profile sync",
                description = "Ensure touch targets are 48dp and theme switches seamlessly.",
                priority = "P1",
                dueDate = "Today",
                isCompleted = true,
                goalId = goal1Id,
                goalTitle = goal1.title,
                estimatedMinutes = 90
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Review consensus paper on multi-raft replication",
                description = "Read chapters 3 and 4 with annotations.",
                priority = "P2",
                dueDate = "Tomorrow",
                isCompleted = false,
                goalId = goal2Id,
                goalTitle = goal2.title,
                estimatedMinutes = 45
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Morning 30-minute zone-2 cardio session",
                description = "Optimal oxygenation for cognitive stamina.",
                priority = "P2",
                dueDate = "Today",
                isCompleted = true,
                goalId = null,
                goalTitle = null,
                estimatedMinutes = 30
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Test AI Goal Planner with new customized targets",
                description = "Deconstruct ambitious projects into weekly milestones.",
                priority = "P3",
                dueDate = "This Week",
                isCompleted = false,
                goalId = goal1Id,
                goalTitle = goal1.title,
                estimatedMinutes = 40
            )
        )
        database.taskDao().clearUserTasks(userId)
        database.taskDao().insertTasks(tasks)

        // Seed initial memories
        val memories = listOf(
            MemoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                category = "Energy Pattern",
                content = "Peak cognitive stamina occurs between 08:30 and 11:30. Best time for P1 complex tasks.",
                source = "AI Observation"
            ),
            MemoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                category = "Work Habit",
                content = "Prefers 90-minute uninterrupted deep focus blocks followed by a 15-minute screen break.",
                source = "User Note"
            )
        )
        database.memoryDao().clearUserMemories(userId)
        database.memoryDao().insertMemories(memories)

        // Seed initial Daily Plan
        val initialBlocks = listOf(
            TimeBlockItem(UUID.randomUUID().toString(), "08:30", "10:00", "Deep Focus: Core P1 Deliverable", "Deep Focus", true),
            TimeBlockItem(UUID.randomUUID().toString(), "10:15", "11:30", "Execution: Sprint & Architecture", "Execution", true),
            TimeBlockItem(UUID.randomUUID().toString(), "13:30", "14:30", "Review: Strategic Goals & Alignment", "Review", false),
            TimeBlockItem(UUID.randomUUID().toString(), "15:00", "16:00", "Secondary Priorities & Comms", "Execution", false),
            TimeBlockItem(UUID.randomUUID().toString(), "16:30", "17:00", "Daily Wind-down & Next Day Setup", "Wellness", false)
        )
        val dailyPlan = DailyPlanEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            date = "Today",
            summary = "Capitalize on morning peak neuro-clarity for strategic goals, followed by reviews.",
            focusScore = 94,
            timeBlocksJson = serializeTimeBlocks(initialBlocks)
        )
        database.dailyPlanDao().clearUserPlans(userId)
        database.dailyPlanDao().insertPlan(dailyPlan)

        // Seed Welcome Message
        val welcomeChat = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            role = "assistant",
            content = "Welcome to LIFEOS, $name. Firebase Authentication verified. System telemetry online. How may I direct your productivity today?"
        )
        database.chatDao().clearUserChat(userId)
        database.chatDao().insertMessage(welcomeChat)
    }

    // Task actions
    suspend fun toggleTaskCompletion(taskId: String, currentState: Boolean) = withContext(Dispatchers.IO) {
        val newState = !currentState
        val completedAt = if (newState) System.currentTimeMillis() else null
        database.taskDao().setTaskCompleted(taskId, newState, completedAt)
    }

    suspend fun insertTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        database.taskDao().insertTask(task)
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        database.taskDao().deleteTaskById(taskId)
    }

    // Goal actions
    suspend fun insertGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        database.goalDao().insertGoal(goal)
    }

    suspend fun updateGoalProgress(goalId: String, newProgress: Float) = withContext(Dispatchers.IO) {
        val existing = database.goalDao().getGoalById(goalId)
        if (existing != null) {
            val updated = existing.copy(
                progress = newProgress,
                status = if (newProgress >= 100f) "completed" else "active"
            )
            database.goalDao().updateGoal(updated)
        }
    }

    suspend fun deleteGoal(goalId: String) = withContext(Dispatchers.IO) {
        database.goalDao().deleteGoalById(goalId)
    }

    // Memory actions
    suspend fun insertMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
        database.memoryDao().insertMemory(memory)
    }

    suspend fun deleteMemory(memoryId: String) = withContext(Dispatchers.IO) {
        database.memoryDao().deleteMemoryById(memoryId)
    }

    // Daily Plan actions
    suspend fun saveDailyPlan(plan: DailyPlanEntity) = withContext(Dispatchers.IO) {
        database.dailyPlanDao().insertPlan(plan)
    }

    suspend fun toggleTimeBlock(plan: DailyPlanEntity, blockId: String) = withContext(Dispatchers.IO) {
        val blocks = deserializeTimeBlocks(plan.timeBlocksJson).map {
            if (it.id == blockId) it.copy(isCompleted = !it.isCompleted) else it
        }
        val updatedPlan = plan.copy(timeBlocksJson = serializeTimeBlocks(blocks))
        database.dailyPlanDao().insertPlan(updatedPlan)
    }

    // Chat actions
    suspend fun insertChatMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        database.chatDao().insertMessage(message)
    }

    // Profile & Auth
    suspend fun updateProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        database.profileDao().updateProfile(profile)
    }

    suspend fun authenticateUser(email: String, name: String, isDemo: Boolean) = withContext(Dispatchers.IO) {
        val userId = if (isDemo) "user_demo" else "user_${email.hashCode()}"
        val existing = database.profileDao().getProfileById(userId)
        if (existing == null) {
            seedUserData(userId, email, name, isOnboarded = true)
        } else {
            database.profileDao().insertProfile(existing.copy(isDemoMode = isDemo))
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        database.profileDao().clearProfiles()
    }

    // Serialization utilities
    fun serializeMilestones(milestones: List<MilestoneItem>): String {
        val array = JSONArray()
        milestones.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("targetWeek", m.targetWeek)
                put("isDone", m.isDone)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeMilestones(json: String): List<MilestoneItem> {
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

    fun serializeTimeBlocks(blocks: List<TimeBlockItem>): String {
        val array = JSONArray()
        blocks.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("startTime", b.startTime)
                put("endTime", b.endTime)
                put("title", b.title)
                put("category", b.category)
                put("isCompleted", b.isCompleted)
                put("linkedTaskId", b.linkedTaskId ?: "")
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeTimeBlocks(json: String): List<TimeBlockItem> {
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
}
