package com.example.ae.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private boolean completed = false; // valor por defecto
    private long createdAt;     // fecha de creación (timestamp en milisegundos)
    private Long dueDate;       // fecha de cumplimiento (puede ser null)
    private String createdBy;   // usuario que creó la tarea
    private Long completedAt;   // fecha de cuando se completó (puede ser null)

    // 🔹 Clave única de Firestore
    private String firebaseId;

    // Constructor vacío (Room y Firebase lo requieren)
    public Task() { }

    // Constructor principal
    public Task(String title, String createdBy) {
        this.title = title;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.createdBy = createdBy;
        this.completedAt = null;
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

    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task)) return false;
        Task other = (Task) obj;
        return Objects.equals(firebaseId, other.firebaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firebaseId);
    }

}
