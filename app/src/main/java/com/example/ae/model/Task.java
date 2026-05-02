package com.example.ae.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private boolean completed = false; // 🔹 valor por defecto

    // Nuevos campos
    private long createdAt;     // fecha de creación (timestamp en milisegundos)
    private Long dueDate;       // fecha de cumplimiento (puede ser null)
    private String createdBy;   // nombre de quien creó la tarea

    // Constructor vacío (Room lo usa internamente)
    public Task() { }

    // Constructor principal
    public Task(String title, String createdBy) {
        this.title = title;
        this.completed = false; // 🔹 siempre inicia como pendiente
        this.createdAt = System.currentTimeMillis(); // 🔹 asigna fecha actual
        this.createdBy = createdBy;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}