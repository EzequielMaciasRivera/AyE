package com.example.ae.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.data.FirebaseRepository;

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
        firebaseRepo = new FirebaseRepository(application); // ✅ pasa el contexto aquí
    }

    // 🔹 Obtener todas las tareas
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

    // 🔹 Insertar tarea en Room + Firebase
    public void insert(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.insert(task);
            firebaseRepo.saveTask(task);
        });
    }

    // 🔹 Actualizar tarea en Room + Firebase
    public void update(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.update(task);
            firebaseRepo.saveTask(task);
        });
    }

    // 🔹 Eliminar tarea en Room + Firebase
    public void delete(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.delete(task);
            firebaseRepo.deleteTask(task);
        });
    }

    // 🔹 Descargar tareas desde Firebase y sincronizar con Room
    public void syncFromFirebase() {
        firebaseRepo.fetchTasks(tasks -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                for (Task task : tasks) {
                    Task existing = taskDao.findByFirebaseId(task.getFirebaseId());
                    if (existing == null) {
                        // Nueva tarea → insertar
                        taskDao.insertOrReplace(task);
                    } else {
                        // Ya existe → conservar id local y actualizar
                        task.setId(existing.getId());
                        taskDao.insertOrReplace(task);
                    }
                }
            });
        });
    }
}
