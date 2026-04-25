package com.example.ae.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;

    private boolean completed = false; // 🔹 valor por defecto

    // Constructor vacío (Room lo usa internamente)
    public Task() { }

    // Constructor principal
    public Task(String title) {
        this.title = title;
        this.completed = false; // 🔹 siempre inicia como pendiente
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}