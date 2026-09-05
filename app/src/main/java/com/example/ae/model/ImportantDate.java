package com.example.ae.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "important_dates")
public class ImportantDate {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private long date;
    private String author; // 🔹 Nuevo campo

    // ✅ Constructor vacío requerido por Room
    public ImportantDate() {}

    // Constructor con parámetros
    public ImportantDate(String title, String description, long date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    // 🔹 Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
