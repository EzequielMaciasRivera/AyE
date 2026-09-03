package com.example.ae.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ae.model.Task;
import com.example.ae.model.ImportantDate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Task.class, ImportantDate.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract ImportantDateDAO importantDateDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "tasks-db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .allowMainThreadQueries() // ⚠️ solo para pruebas
                    .build();
        }
        return INSTANCE;
    }

    // Executor para operaciones en segundo plano
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Migración de versión 1 a 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tasks ADD COLUMN dueDate INTEGER");
            database.execSQL("ALTER TABLE tasks ADD COLUMN createdBy TEXT");
            database.execSQL("ALTER TABLE tasks ADD COLUMN completedAt INTEGER");
        }
    };

    // Migración de versión 2 a 3: crear tabla de fechas importantes
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS important_dates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "date INTEGER NOT NULL)");
        }
    };
}
