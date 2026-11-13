package com.example.ae.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.ImportantDateDAO;
import com.example.ae.data.FirebaseRepository;

import java.util.List;

public class ImportantDatesViewModel extends AndroidViewModel {

    private final ImportantDateDAO dateDao;
    private final LiveData<List<ImportantDate>> allDates;
    private final FirebaseRepository firebaseRepo;

    public ImportantDatesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        dateDao = db.importantDateDao();
        allDates = dateDao.getAllDates();
        firebaseRepo = new FirebaseRepository(application);
    }

    public LiveData<List<ImportantDate>> getAllDates() { return allDates; }

    // Insertar
    public void insert(ImportantDate date) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dateDao.insertOrReplace(date);
            if (date.getFirebaseId() == null || date.getFirebaseId().isEmpty()) {
                firebaseRepo.saveImportantDate(date);
            }
        });
    }

    // Actualizar
    // ImportantDatesViewModel
    public void update(ImportantDate date) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ImportantDate existing = dateDao.findByFirebaseId(date.getFirebaseId());
            if (existing != null) {
                date.setId(existing.getId()); // conserva id local
            }
            dateDao.insertOrReplace(date);
            firebaseRepo.saveImportantDate(date); // ✅ mantiene firebaseId
        });
    }


    // Eliminar
    public void delete(ImportantDate date) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dateDao.deleteDate(date);
            firebaseRepo.deleteImportantDate(date);
        });
    }

    // Sincronizar desde Firebase
    public void syncFromFirebase() {
        firebaseRepo.fetchImportantDates(dates -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                for (ImportantDate date : dates) {
                    ImportantDate existing = dateDao.findByFirebaseId(date.getFirebaseId());
                    if (existing == null) {
                        dateDao.insertOrReplace(date);
                    } else {
                        date.setId(existing.getId()); // ✅ conserva id local
                        dateDao.insertOrReplace(date);
                    }
                }
            });
        });
    }
}
