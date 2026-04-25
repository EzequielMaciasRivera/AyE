package com.example.ae.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.ae.model.Task;
import com.example.ae.data.TaskDao;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TaskDao taskDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "tasks-db")
                    .allowMainThreadQueries() // ⚠️ solo para pruebas
                    .build();
        }
        return INSTANCE;
    }
}