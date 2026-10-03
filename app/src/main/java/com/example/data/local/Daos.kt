package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DailyPlanEntity
import com.example.data.model.GoalEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles LIMIT 1")
    fun getActiveProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :userId LIMIT 1")
    suspend fun getProfileById(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun clearProfiles()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsByUser(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun clearUserGoals(userId: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY isCompleted ASC, priority ASC, createdAt DESC")
    fun getTasksByUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND goalId = :goalId")
    fun getTasksForGoal(userId: String, goalId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: String, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun clearUserTasks(userId: String)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMemoriesByUser(userId: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryEntity>)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: String)

    @Query("DELETE FROM memories WHERE userId = :userId")
    suspend fun clearUserMemories(userId: String)
}

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestPlan(userId: String): Flow<DailyPlanEntity?>

    @Query("SELECT * FROM daily_plans WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getPlanByDate(userId: String, date: String): DailyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DailyPlanEntity)

    @Query("DELETE FROM daily_plans WHERE userId = :userId")
    suspend fun clearUserPlans(userId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getChatMessages(userId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearUserChat(userId: String)
}
