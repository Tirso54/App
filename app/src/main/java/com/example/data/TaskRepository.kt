package com.example.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val database: AppDatabase) {
    private val taskDao = database.taskDao()

    val allTasksFlow: Flow<List<Task>> = taskDao.getAllTasksFlow()

    suspend fun getTasksForDay(day: Int): List<Task> {
        return taskDao.getTasksForDay(day)
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun insertAll(tasks: List<Task>) {
        taskDao.insertAll(tasks)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        taskDao.deleteTaskById(id)
    }

    suspend fun clearAllTasks() {
        taskDao.clearAllTasks()
    }

    suspend fun replaceAllTasks(tasks: List<Task>) {
        // Run as a transaction so UI doesn't flicker with empty state
        database.withTransaction {
            taskDao.clearAllTasks()
            taskDao.insertAll(tasks)
        }
    }
}
