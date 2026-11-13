package com.example.ae.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey
    @NonNull
    private String firebaseId = "";

    private String title;
    private boolean completed = false;
    private long createdAt;
    private Long dueDate;
    private String createdBy;
    private Long completedAt;

    public Task() { }

    public Task(String title, String createdBy) {
        this.title = title;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.createdBy = createdBy;
        this.completedAt = null;
    }
    public Task(Task other) {
        this.firebaseId = other.firebaseId;
        this.title = other.title;
        this.completed = other.completed;
        this.createdAt = other.createdAt;
        this.dueDate = other.dueDate;
        this.createdBy = other.createdBy;
        this.completedAt = other.completedAt;
    }

    @NonNull
    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(@NonNull String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

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