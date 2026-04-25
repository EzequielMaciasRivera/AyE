package com.example.ae.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ae.model.Task;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ae.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    // Insertar nueva tarea
    @Insert
    void insert(Task task);

    // Actualizar tarea (ej. cambiar estado completado)
    @Update
    void update(Task task);

    // Eliminar tarea
    @Delete
    void delete(Task task);

    // Consultar tareas pendientes (LiveData)
    @Query("SELECT * FROM tasks WHERE completed = 0")
    LiveData<List<Task>> getPendingTasks();

    // Consultar tareas completadas (LiveData)
    @Query("SELECT * FROM tasks WHERE completed = 1")
    LiveData<List<Task>> getCompletedTasks();

    // Consultar todas las tareas (opcional, LiveData)
    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> getAllTasks();
}