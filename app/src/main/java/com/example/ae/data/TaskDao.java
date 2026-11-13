package com.example.ae.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ae.model.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(Task task);
    @Query("DELETE FROM tasks")
    void deleteAll();

    @Query("DELETE FROM tasks WHERE firebaseId NOT IN (:ids)")
    void deleteTasksNotIn(List<String> ids);

    // 🔹 Insertar nueva tarea (cuando no existe en Firebase aún)
    @Insert
    void insert(Task task);

    // 🔹 Actualizar tarea
    @Update
    void update(Task task);

    // 🔹 Eliminar tarea
    @Delete
    void delete(Task task);

    // 🔹 Borrar todas las tareas
    @Query("DELETE FROM tasks")
    void clearAll();

    // 🔹 Consultar todas las tareas
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    LiveData<List<Task>> getAllTasks();

    // 🔹 Consultar tareas pendientes
    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY dueDate ASC")
    LiveData<List<Task>> getPendingTasks();

    // 🔹 Consultar tareas completadas
    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY completedAt DESC")
    LiveData<List<Task>> getCompletedTasks();

    // 🔹 Consultar tareas próximas
    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND completed = 0 ORDER BY dueDate ASC")
    LiveData<List<Task>> getUpcomingTasks();

    // 🔹 Consultar tareas creadas por un usuario específico
    @Query("SELECT * FROM tasks WHERE createdBy = :userName")
    LiveData<List<Task>> getTasksByUser(String userName);

    // 🔹 Actualizar el autor de todas las tareas
    @Query("UPDATE tasks SET createdBy = :nuevoAutor")
    void updateAllAuthors(String nuevoAutor);
}
