package com.example.ae.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.ae.model.ImportantDate;

import java.util.List;

@Dao
public interface ImportantDateDAO {


    @Query(
            "SELECT * FROM important_dates " +
                    "WHERE firebaseId = :firebaseId LIMIT 1"
    )
    ImportantDate findByFirebaseId(String firebaseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(ImportantDate date);

    @Delete
    void deleteDate(ImportantDate date);

    @Query(
            "SELECT * FROM important_dates " +
                    "ORDER BY date ASC"
    )
    LiveData<List<ImportantDate>> getAllDates();

    @Query("DELETE FROM important_dates")
    void deleteAll();

    @Query(
            "DELETE FROM important_dates " +
                    "WHERE firebaseId NOT IN (:ids)"
    )
    void deleteDatesNotIn(List<String> ids);

    @Query(
            "UPDATE important_dates " +
                    "SET createdBy = :nuevoAutor " +
                    "WHERE createdBy = :autorAnterior"
    )
    void updateAuthor(
            String autorAnterior,
            String nuevoAutor
    );
}