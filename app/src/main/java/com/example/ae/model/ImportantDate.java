package com.example.ae.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "important_dates")
public class ImportantDate {
    @ColumnInfo(name = "ownerId")
    private String ownerId;
    @PrimaryKey
    @NonNull
    private String firebaseId = "";

    private String title;
    private String description;
    private long date;

    @ColumnInfo(name = "createdBy")
    private String createdBy;

    public ImportantDate() {
    }

    public ImportantDate(
            String title,
            String description,
            long date,
            String createdBy
    ) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.createdBy = createdBy;
    }

    public ImportantDate(ImportantDate other) {
        this.firebaseId = other.firebaseId;
        this.title = other.title;
        this.description = other.description;
        this.date = other.date;
        this.createdBy = other.createdBy;
        this.ownerId = other.ownerId;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof ImportantDate))
            return false;

        ImportantDate other = (ImportantDate) obj;

        return Objects.equals(firebaseId, other.firebaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firebaseId);
    }
}

