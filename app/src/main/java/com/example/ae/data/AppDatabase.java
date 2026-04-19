package com.example.ae.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.ae.model.Task;


@Database(entities = {Task.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}