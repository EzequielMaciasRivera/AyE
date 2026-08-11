package com.example.ae.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ae.model.ImportantDate;

import java.util.List;

@Dao
public interface ImportantDateDAO {
    @Query("SELECT * FROM important_dates ORDER BY date ASC")
    LiveData<List<ImportantDate>> getAllDates();

    @Insert
    void insertDate(ImportantDate date);

    @Update
    void updateDate(ImportantDate date);

    @Delete
    void deleteDate(ImportantDate date);
}
