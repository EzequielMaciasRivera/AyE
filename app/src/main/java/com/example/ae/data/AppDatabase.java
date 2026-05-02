package com.example.ae.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ae.model.Task;

@Database(entities = {Task.class}, version = 2) // 🔹 versión 2
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TaskDao taskDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "tasks-db")
                    .addMigrations(MIGRATION_1_2) // 🔹 añadimos migración
                    .allowMainThreadQueries() // ⚠️ solo para pruebas
                    .build();
        }
        return INSTANCE;
    }

    // 🔹 Migración de versión 1 a 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Agregar nuevas columnas
            database.execSQL("ALTER TABLE tasks ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN dueDate INTEGER");
            database.execSQL("ALTER TABLE tasks ADD COLUMN createdBy TEXT");
            database.execSQL("ALTER TABLE tasks ADD COLUMN completedAt INTEGER"); // 🔹 faltaba esta
        }
    };
}