package com.example.ae.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ae.model.Task;

import java.util.List;

import androidx.lifecycle.LiveData;

@Dao
public interface TaskDao {

    // Insertar nueva tarea
    @Insert
    void insert(Task task);

    // Actualizar tarea (ej. cambiar estado completado, título, fechas, etc.)
    @Update
    void update(Task task);

    // Eliminar tarea
    @Delete
    void delete(Task task);

    // Consultar tareas pendientes (LiveData)
    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY dueDate ASC")
    LiveData<List<Task>> getPendingTasks();

    // Consultar tareas completadas (LiveData)
    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY completedAt DESC") // 🔹 ahora ordena por fecha de completado
    LiveData<List<Task>> getCompletedTasks();

    // Consultar todas las tareas (LiveData)
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    LiveData<List<Task>> getAllTasks();

    // Consultar tareas creadas por un usuario específico
    @Query("SELECT * FROM tasks WHERE createdBy = :userName")
    LiveData<List<Task>> getTasksByUser(String userName);

    // Consultar tareas con fecha de cumplimiento próxima
    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND completed = 0 ORDER BY dueDate ASC")
    LiveData<List<Task>> getUpcomingTasks();
}