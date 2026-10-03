package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DailyPlanScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.LifeOsTheme
import com.example.ui.viewmodel.LifeOsViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: LifeOsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val profile by viewModel.profile.collectAsState()
            val themePref by viewModel.themePreference.collectAsState()

            val isDark = when (themePref) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            LifeOsTheme(darkTheme = isDark) {
                LifeOsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LifeOsApp(viewModel: LifeOsViewModel) {
    val profile by viewModel.profile.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val dailyPlan by viewModel.latestDailyPlan.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val themePref by viewModel.themePreference.collectAsState()

    val showAddTaskDialog by viewModel.showAddTaskDialog.collectAsState()
    val showAddGoalDialog by viewModel.showAddGoalDialog.collectAsState()
    val showGoalPlannerModal by viewModel.showGoalPlannerModal.collectAsState()
    val selectedGoalForPlanning by viewModel.selectedGoalForPlanning.collectAsState()
    val showAddMemoryDialog by viewModel.showAddMemoryDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Strict Auth Gate: Do not allow user to access ANY functionality until signed in
    if (profile == null) {
        val context = LocalContext.current
        AuthScreen(
            isAuthLoading = isAuthLoading,
            authError = authError,
            onSignIn = { email, pass ->
                viewModel.signInWithFirebase(email, pass)
            },
            onSignUp = { email, pass, name ->
                viewModel.signUpWithFirebase(email, pass, name)
            },
            onSignInWithGoogle = {
                viewModel.signInWithGoogle(context)
            },
            onClearError = { viewModel.clearAuthError() }
        )
        return
    }

    // Onboarding gate
    if (!profile!!.isOnboarded) {
        OnboardingScreen(
            initialName = profile!!.fullName,
            onComplete = { name, style, focus, primeHours ->
                viewModel.completeOnboarding(name, style, focus, primeHours)
            }
        )
        return
    }

    // BackHandler for secondary screens
    if (currentScreen != Screen.Home) {
        BackHandler {
            viewModel.navigateTo(Screen.Home)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            LifeOsBottomNav(
                currentScreen = currentScreen,
                onSelectScreen = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Home -> DashboardScreen(
                    profile = profile,
                    goals = goals,
                    tasks = tasks,
                    dailyPlan = dailyPlan,
                    onPlanMyDayClick = { viewModel.planMyDay() },
                    onNavigateToGoals = { viewModel.navigateTo(Screen.Goals) },
                    onNavigateToTasks = { viewModel.navigateTo(Screen.Tasks) },
                    onNavigateToDailyPlan = { viewModel.navigateTo(Screen.DailyPlan) },
                    onNavigateToAi = { viewModel.navigateTo(Screen.Ai) },
                    onToggleTask = { id, state -> viewModel.toggleTask(id, state) },
                    onToggleTimeBlock = { plan, blockId -> viewModel.toggleTimeBlock(plan, blockId) },
                    onOpenGoalPlanner = { goal -> viewModel.openGoalPlanner(goal) },
                    onAddTaskClick = { viewModel.setShowAddTaskDialog(true) },
                    onAddGoalClick = { viewModel.setShowAddGoalDialog(true) }
                )

                is Screen.Ai -> AiChatScreen(
                    messages = chatMessages,
                    isAiLoading = isAiLoading,
                    memoryCount = memories.size,
                    activeGoalsCount = goals.count { it.status == "active" },
                    pendingTasksCount = tasks.count { !it.isCompleted },
                    onSendMessage = { text -> viewModel.sendChatMessage(text) }
                )

                is Screen.Goals -> GoalsScreen(
                    goals = goals,
                    isAiLoading = isAiLoading,
                    showAddGoalDialog = showAddGoalDialog,
                    showGoalPlannerModal = showGoalPlannerModal,
                    selectedGoalForPlanning = selectedGoalForPlanning,
                    onOpenAddGoalDialog = { viewModel.setShowAddGoalDialog(true) },
                    onCloseAddGoalDialog = { viewModel.setShowAddGoalDialog(false) },
                    onCreateGoal = { title, desc, cat, deadline -> viewModel.createGoal(title, desc, cat, deadline) },
                    onOpenGoalPlanner = { goal -> viewModel.openGoalPlanner(goal) },
                    onCloseGoalPlanner = { viewModel.closeGoalPlanner() },
                    onGenerateGoalPlan = { goal -> viewModel.generateGoalPlan(goal) },
                    onUpdateProgress = { id, progress -> viewModel.updateGoalProgress(id, progress) },
                    onDeleteGoal = { id -> viewModel.deleteGoal(id) }
                )

                is Screen.Tasks -> TasksScreen(
                    tasks = tasks,
                    goals = goals,
                    showAddTaskDialog = showAddTaskDialog,
                    onOpenAddTaskDialog = { viewModel.setShowAddTaskDialog(true) },
                    onCloseAddTaskDialog = { viewModel.setShowAddTaskDialog(false) },
                    onToggleTask = { id, state -> viewModel.toggleTask(id, state) },
                    onDeleteTask = { id -> viewModel.deleteTask(id) },
                    onCreateTask = { title, desc, priority, due, gId, gTitle, mins ->
                        viewModel.createTask(title, desc, priority, due, gId, gTitle, mins)
                    }
                )

                is Screen.DailyPlan -> DailyPlanScreen(
                    dailyPlan = dailyPlan,
                    isAiLoading = isAiLoading,
                    onPlanMyDay = { viewModel.planMyDay() },
                    onToggleTimeBlock = { plan, blockId -> viewModel.toggleTimeBlock(plan, blockId) }
                )

                is Screen.Profile -> ProfileScreen(
                    profile = profile,
                    memories = memories,
                    themePreference = themePref,
                    showAddMemoryDialog = showAddMemoryDialog,
                    onOpenAddMemoryDialog = { viewModel.setShowAddMemoryDialog(true) },
                    onCloseAddMemoryDialog = { viewModel.setShowAddMemoryDialog(false) },
                    onAddMemory = { cat, content -> viewModel.addMemory(cat, content) },
                    onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                    onSetThemePreference = { pref -> viewModel.setThemePreference(pref) },
                    onResetDemoData = { viewModel.resetDemoData() },
                    onLogout = { viewModel.logout() }
                )

                else -> {}
            }
        }
    }
}

@Composable
fun LifeOsBottomNav(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit
) {
    val navItems = listOf(
        Triple(Screen.Home, "Home", Icons.Default.Home),
        Triple(Screen.Ai, "AI", Icons.Default.AutoAwesome),
        Triple(Screen.Goals, "Goals", Icons.Default.Flag),
        Triple(Screen.Tasks, "Tasks", Icons.Default.CheckCircle),
        Triple(Screen.DailyPlan, "Daily", Icons.Default.CalendarToday),
        Triple(Screen.Profile, "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyanNeon,
                    selectedTextColor = CyanNeon,
                    indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}
