package com.example.ae.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ae.model.ImportantDate;

import java.util.List;

@Dao
public interface ImportantDateDAO {

    // 🔹 Buscar fecha por firebaseId
    @Query("SELECT * FROM important_dates WHERE firebaseId = :firebaseId LIMIT 1")
    ImportantDate findByFirebaseId(String firebaseId);

    // 🔹 Insertar o reemplazar (evita duplicados)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(ImportantDate date);

    // 🔹 Insertar nueva fecha
    @Insert
    void insertDate(ImportantDate date);

    // 🔹 Actualizar fecha
    @Update
    void updateDate(ImportantDate date);

    // 🔹 Eliminar fecha
    @Delete
    void deleteDate(ImportantDate date);

    // 🔹 Borrar todas las fechas
    @Query("DELETE FROM important_dates")
    void clearAll();

    // 🔹 Consultar todas las fechas
    @Query("SELECT * FROM important_dates ORDER BY date ASC")
    LiveData<List<ImportantDate>> getAllDates();

    // 🔹 Actualizar autor de todas las fechas
    @Query("UPDATE important_dates SET createdBy = :nuevoAutor")
    void updateAllAuthors(String nuevoAutor);
}
