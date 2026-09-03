package com.example.ae.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.ImportantDateDAO;

import java.util.List;

public class ImportantDatesViewModel extends AndroidViewModel {
    private ImportantDateDAO dateDao;
    private LiveData<List<ImportantDate>> allDates;

    public ImportantDatesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        dateDao = db.importantDateDao();
        allDates = dateDao.getAllDates();
    }

    public LiveData<List<ImportantDate>> getAllDates() {
        return allDates;
    }

    public void insert(ImportantDate date) {
        AppDatabase.databaseWriteExecutor.execute(() -> dateDao.insertDate(date));
    }

    public void update(ImportantDate date) {
        AppDatabase.databaseWriteExecutor.execute(() -> dateDao.updateDate(date));
    }

    public void delete(ImportantDate date) {
        AppDatabase.databaseWriteExecutor.execute(() -> dateDao.deleteDate(date));
    }
}
