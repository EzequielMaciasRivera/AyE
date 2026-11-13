package com.example.ae.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.FirebaseRepository;
import com.example.ae.data.TaskDao;

import java.util.List;

public class TasksViewModel extends AndroidViewModel {

    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasks;
    private final FirebaseRepository firebaseRepo;

    public TasksViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = AppDatabase.getInstance(application);

        taskDao = db.taskDao();
        allTasks = taskDao.getAllTasks();

        firebaseRepo = new FirebaseRepository(application);
    }

    // ==========================
    // GETTERS
    // ==========================

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasks();
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return taskDao.getCompletedTasks();
    }

    public LiveData<List<Task>> getUpcomingTasks() {
        return taskDao.getUpcomingTasks();
    }

    // ==========================
    // INSERTAR
    // ==========================

    public void insert(Task task) {

        Log.d("INSERT_DEBUG",
                "ANTES saveTask firebaseId = " + task.getFirebaseId());

        firebaseRepo.saveTask(task);
    }

    // ==========================
    // ACTUALIZAR
    // ==========================

    public void update(Task task) {

        Log.d(
                "UPDATE_DEBUG",
                "Actualizando tarea -> title="
                        + task.getTitle()
                        + " firebaseId="
                        + task.getFirebaseId()
        );

        AppDatabase.databaseWriteExecutor.execute(() -> {

            taskDao.insertOrReplace(task);

            firebaseRepo.saveTask(task);
        });
    }

    // ==========================
    // ELIMINAR
    // ==========================

    public void delete(Task task) {

        AppDatabase.databaseWriteExecutor.execute(() -> {

            taskDao.delete(task);

            firebaseRepo.deleteTask(task);
        });
    }

    // ==========================
    // SINCRONIZAR DESDE FIREBASE
    // ==========================

    public void syncFromFirebase() {

        Log.d(
                "SYNC_DEBUG",
                "syncFromFirebase ejecutado"
        );

        firebaseRepo.fetchTasks();
    }
}