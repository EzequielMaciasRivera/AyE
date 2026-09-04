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

    @Insert
    void insertDate(ImportantDate date);

    @Update
    void updateDate(ImportantDate date);

    @Delete
    void deleteDate(ImportantDate date);

    // 🔹 Obtener todas las fechas ordenadas por fecha
    @Query("SELECT * FROM important_dates ORDER BY date ASC")
    LiveData<List<ImportantDate>> getAllDates();

    @Query("UPDATE important_dates SET author = :author WHERE id = :id")
    void updateAuthor(int id, String author);

    @Query("SELECT COUNT(*) FROM important_dates")
    int getCount();

    @Query("UPDATE important_dates SET author = :newAuthor")
    void updateAllAuthors(String newAuthor);
}
