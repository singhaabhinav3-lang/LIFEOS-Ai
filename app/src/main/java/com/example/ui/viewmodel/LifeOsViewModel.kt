package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.auth.FirebaseAuthManager
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DailyPlanEntity
import com.example.data.model.GoalEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.LifeOsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen(val title: String) {
    object Auth : Screen("Auth")
    object Onboarding : Screen("Onboarding")
    object Home : Screen("Home")
    object Ai : Screen("AI Copilot")
    object Goals : Screen("Goals")
    object Tasks : Screen("Tasks")
    object DailyPlan : Screen("Daily Plan")
    object Profile : Screen("Profile")
}

@OptIn(ExperimentalCoroutinesApi::class)
class LifeOsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = LifeOsRepository(database)
    val firebaseAuthManager = FirebaseAuthManager(application)
    private val aiService = GeminiAiService()

    val profile: StateFlow<UserProfileEntity?> = repository.activeProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val goals: StateFlow<List<GoalEntity>> = profile.flatMapLatest { p ->
        if (p != null) repository.getGoals(p.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = profile.flatMapLatest { p ->
        if (p != null) repository.getTasks(p.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = profile.flatMapLatest { p ->
        if (p != null) repository.getMemories(p.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestDailyPlan: StateFlow<DailyPlanEntity?> = profile.flatMapLatest { p ->
        if (p != null) repository.getLatestDailyPlan(p.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<ChatMessageEntity>> = profile.flatMapLatest { p ->
        if (p != null) repository.getChatMessages(p.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _themePreference = MutableStateFlow("dark")
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    // Modals & Dialogs state
    private val _showAddTaskDialog = MutableStateFlow(false)
    val showAddTaskDialog: StateFlow<Boolean> = _showAddTaskDialog.asStateFlow()

    private val _showAddGoalDialog = MutableStateFlow(false)
    val showAddGoalDialog: StateFlow<Boolean> = _showAddGoalDialog.asStateFlow()

    private val _showGoalPlannerModal = MutableStateFlow(false)
    val showGoalPlannerModal: StateFlow<Boolean> = _showGoalPlannerModal.asStateFlow()

    private val _selectedGoalForPlanning = MutableStateFlow<GoalEntity?>(null)
    val selectedGoalForPlanning: StateFlow<GoalEntity?> = _selectedGoalForPlanning.asStateFlow()

    private val _showAddMemoryDialog = MutableStateFlow(false)
    val showAddMemoryDialog: StateFlow<Boolean> = _showAddMemoryDialog.asStateFlow()

    init {
        viewModelScope.launch {
            val user = firebaseAuthManager.currentUser
            repository.checkSessionOnStart(user?.uid, user?.email, user?.displayName)
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun setThemePreference(pref: String) {
        _themePreference.value = pref
        viewModelScope.launch {
            profile.value?.let { currentProfile ->
                repository.updateProfile(currentProfile.copy(themePreference = pref))
            }
        }
    }

    // Firebase Auth actions
    fun signInWithFirebase(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = firebaseAuthManager.signIn(email.trim(), pass)
            result.onSuccess { user ->
                repository.activateFirebaseUser(user.uid, user.email ?: email, user.displayName)
                _snackbarMessage.value = "Firebase Authentication Verified. Welcome to LIFEOS!"
                _currentScreen.value = Screen.Home
            }.onFailure { ex ->
                val errorMsg = ex.localizedMessage ?: "Firebase Authentication failed"
                _authError.value = errorMsg
            }
            _isAuthLoading.value = false
        }
    }

    fun signUpWithFirebase(email: String, pass: String, displayName: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = firebaseAuthManager.signUp(email.trim(), pass, displayName.trim())
            result.onSuccess { user ->
                repository.seedUserData(user.uid, user.email ?: email, displayName.ifBlank { "Commander" }, isOnboarded = false)
                _snackbarMessage.value = "Firebase Account Created! Please calibrate your profile."
                _currentScreen.value = Screen.Onboarding
            }.onFailure { ex ->
                val errorMsg = ex.localizedMessage ?: "Firebase Registration failed"
                _authError.value = errorMsg
            }
            _isAuthLoading.value = false
        }
    }

    fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = firebaseAuthManager.signInWithGoogle(context)
            result.onSuccess { user ->
                val name = user.displayName ?: user.email?.substringBefore("@")?.capitalize() ?: "Commander"
                repository.activateFirebaseUser(user.uid, user.email ?: "", name)
                _snackbarMessage.value = "Google Authentication Verified. Welcome, $name!"
                _currentScreen.value = Screen.Home
            }.onFailure { ex ->
                val errorMsg = ex.localizedMessage ?: "Google Authentication failed"
                _authError.value = errorMsg
            }
            _isAuthLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            firebaseAuthManager.signOut()
            repository.logout()
            _currentScreen.value = Screen.Auth
            _snackbarMessage.value = "Signed out from Firebase"
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            val user = profile.value ?: return@launch
            repository.seedUserData(user.id, user.email, user.fullName, isOnboarded = user.isOnboarded)
            _snackbarMessage.value = "LIFEOS system data refreshed"
        }
    }

    // Task actions
    fun toggleTask(taskId: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, currentState)
        }
    }

    fun createTask(
        title: String,
        description: String,
        priority: String,
        dueDate: String,
        goalId: String?,
        goalTitle: String?,
        estimatedMinutes: Int
    ) {
        viewModelScope.launch {
            val user = profile.value ?: return@launch
            val task = TaskEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                isCompleted = false,
                goalId = goalId,
                goalTitle = goalTitle,
                estimatedMinutes = estimatedMinutes
            )
            repository.insertTask(task)
            _showAddTaskDialog.value = false
            _snackbarMessage.value = "Task created and linked"
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            _snackbarMessage.value = "Task deleted"
        }
    }

    // Goal actions
    fun createGoal(title: String, description: String, category: String, deadline: String) {
        viewModelScope.launch {
            val user = profile.value ?: return@launch
            val goal = GoalEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = title,
                description = description,
                category = category,
                deadline = deadline,
                progress = 0f,
                status = "active",
                milestonesJson = "[]"
            )
            repository.insertGoal(goal)
            _showAddGoalDialog.value = false
            _snackbarMessage.value = "Goal indexed into LIFEOS"
        }
    }

    fun updateGoalProgress(goalId: String, newProgress: Float) {
        viewModelScope.launch {
            repository.updateGoalProgress(goalId, newProgress)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
            _snackbarMessage.value = "Goal removed"
        }
    }

    // AI Goal Planner
    fun openGoalPlanner(goal: GoalEntity) {
        _selectedGoalForPlanning.value = goal
        _showGoalPlannerModal.value = true
    }

    fun closeGoalPlanner() {
        _showGoalPlannerModal.value = false
        _selectedGoalForPlanning.value = null
    }

    fun generateGoalPlan(goal: GoalEntity) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val (milestones, tasks) = aiService.generateGoalPlan(
                    goalTitle = goal.title,
                    category = goal.category,
                    deadline = goal.deadline,
                    profile = profile.value
                )
                // Link tasks to this goal
                val linkedTasks = tasks.map { it.copy(goalId = goal.id, goalTitle = goal.title) }
                val updatedMilestonesJson = repository.serializeMilestones(milestones)
                val updatedGoal = goal.copy(
                    milestonesJson = updatedMilestonesJson,
                    progress = if (milestones.isNotEmpty()) (milestones.count { it.isDone }.toFloat() / milestones.size * 100f) else goal.progress
                )
                repository.insertGoal(updatedGoal)
                linkedTasks.forEach { repository.insertTask(it) }

                // Add memory note
                val memory = MemoryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = goal.userId,
                    category = "Goal Context",
                    content = "Deconstructed '${goal.title}' into ${milestones.size} milestones and ${linkedTasks.size} tasks.",
                    source = "Goal Planner"
                )
                repository.insertMemory(memory)

                _snackbarMessage.value = "AI generated ${milestones.size} milestones and ${linkedTasks.size} tasks!"
                closeGoalPlanner()
            } catch (e: Exception) {
                _snackbarMessage.value = "Goal planning error: ${e.message}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // AI "Plan My Day"
    fun planMyDay() {
        viewModelScope.launch {
            val user = profile.value ?: return@launch
            _isAiLoading.value = true
            try {
                val currentGoals = goals.value
                val currentTasks = tasks.value.filter { !it.isCompleted }
                val (summary, blocks) = aiService.generateDailyPlan(user, currentGoals, currentTasks)

                val plan = DailyPlanEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    date = "Today",
                    summary = summary,
                    focusScore = 95,
                    timeBlocksJson = repository.serializeTimeBlocks(blocks)
                )
                repository.saveDailyPlan(plan)
                _snackbarMessage.value = "AI Daily Plan synthesized for today!"
            } catch (e: Exception) {
                _snackbarMessage.value = "Plan generation error: ${e.message}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun toggleTimeBlock(plan: DailyPlanEntity, blockId: String) {
        viewModelScope.launch {
            repository.toggleTimeBlock(plan, blockId)
        }
    }

    // AI Chat Assistant
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            val user = profile.value ?: return@launch
            val userMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                role = "user",
                content = userText
            )
            repository.insertChatMessage(userMsg)

            _isAiLoading.value = true
            try {
                val aiReply = aiService.generateChatResponse(
                    userMessage = userText,
                    profile = user,
                    activeGoals = goals.value,
                    pendingTasks = tasks.value.filter { !it.isCompleted },
                    memories = memories.value
                )
                val assistantMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    role = "assistant",
                    content = aiReply
                )
                repository.insertChatMessage(assistantMsg)
            } catch (e: Exception) {
                val errMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    role = "assistant",
                    content = "LIFEOS Core Intelligence encountered an issue: ${e.message}"
                )
                repository.insertChatMessage(errMsg)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Memories
    fun addMemory(category: String, content: String) {
        viewModelScope.launch {
            val user = profile.value ?: return@launch
            val memory = MemoryEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                category = category,
                content = content,
                source = "User Defined"
            )
            repository.insertMemory(memory)
            _showAddMemoryDialog.value = false
            _snackbarMessage.value = "Memory indexed into LIFEOS Core"
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            repository.deleteMemory(memoryId)
            _snackbarMessage.value = "Memory pruned"
        }
    }

    // Dialog controls
    fun setShowAddTaskDialog(show: Boolean) { _showAddTaskDialog.value = show }
    fun setShowAddGoalDialog(show: Boolean) { _showAddGoalDialog.value = show }
    fun setShowAddMemoryDialog(show: Boolean) { _showAddMemoryDialog.value = show }

    // Onboarding completion
    fun completeOnboarding(
        fullName: String,
        style: String,
        focus: String,
        primeHours: String
    ) {
        viewModelScope.launch {
            val user = profile.value ?: return@launch
            val updated = user.copy(
                fullName = fullName,
                productivityStyle = style,
                focusAreas = focus,
                primeHours = primeHours,
                isOnboarded = true
            )
            repository.updateProfile(updated)
            _currentScreen.value = Screen.Home
            _snackbarMessage.value = "Personal operating system calibrated."
        }
    }
}
