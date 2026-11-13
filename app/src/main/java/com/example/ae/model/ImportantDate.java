package com.example.ae.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "important_dates")
public class ImportantDate {

    // 🔹 ID local autogenerado por Room
    @PrimaryKey(autoGenerate = true)
    private int id;

    // 🔹 ID único de Firestore (String)
    private String firebaseId;

    private String title;
    private String description;
    private long date;

    @ColumnInfo(name = "createdBy")
    private String createdBy;

    // ✅ Constructor vacío requerido por Room y Firebase
    public ImportantDate() {}

    // 🔹 Constructor con parámetros
    public ImportantDate(String title, String description, long date, String createdBy) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.createdBy = createdBy;
    }

    // 🔹 Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ImportantDate)) return false;
        ImportantDate other = (ImportantDate) obj;
        return Objects.equals(firebaseId, other.firebaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firebaseId);
    }
}
