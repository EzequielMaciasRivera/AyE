package com.example.ae.data;

import android.content.Context;
import android.util.Log;

import com.example.ae.model.ImportantDate;
import com.example.ae.model.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirebaseRepository {

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FirebaseRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    // ==============================
    // TAREAS
    // ==============================

    public void saveTask(Task task) {

        if (task.getFirebaseId() == null || task.getFirebaseId().isEmpty()) {

            String newId = db.collection("tasks")
                    .document()
                    .getId();

            task.setFirebaseId(newId);

            Log.d(
                    "FIREBASE_ID_DEBUG",
                    "Generado firebaseId = " + newId
            );
        }

        db.collection("tasks")
                .document(task.getFirebaseId())
                .set(task)
                .addOnSuccessListener(aVoid -> {

                    Log.d(
                            "FirebaseRepo",
                            "✅ Tarea guardada con ID: "
                                    + task.getFirebaseId()
                    );

                    AppDatabase.databaseWriteExecutor.execute(() -> {

                        Log.d(
                                "ROOM_SAVE_DEBUG",
                                "Guardando en Room -> "
                                        + task.getTitle()
                                        + " / "
                                        + task.getFirebaseId()
                        );

                        AppDatabase.getInstance(context)
                                .taskDao()
                                .insertOrReplace(task);

                    });

                })
                .addOnFailureListener(e ->
                        Log.e(
                                "FirebaseRepo",
                                "❌ Error al guardar tarea",
                                e
                        )
                );
    }

    public void update(Task task) {

        AppDatabase.databaseWriteExecutor.execute(() -> {

            AppDatabase.getInstance(context)
                    .taskDao()
                    .insertOrReplace(task);

            saveTask(task);
        });
    }

    public void deleteTask(Task task) {

        if (task.getFirebaseId() == null ||
                task.getFirebaseId().isEmpty()) {

            Log.w("FirebaseRepo",
                    "⚠️ No se puede eliminar: tarea sin firebaseId");
            return;
        }

        db.collection("tasks")
                .document(task.getFirebaseId())
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("FirebaseRepo",
                                "✅ Tarea eliminada en Firebase"))
                .addOnFailureListener(e ->
                        Log.e("FirebaseRepo",
                                "❌ Error al eliminar tarea",
                                e));
    }

    public void fetchTasks() {

        db.collection("tasks")
                .get()
                .addOnSuccessListener(query -> {

                    List<Task> tasks = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        Task task = doc.toObject(Task.class);

                        if (task != null) {

                            task.setFirebaseId(doc.getId());

                            tasks.add(task);
                        }
                    }

                    AppDatabase.databaseWriteExecutor.execute(() -> {

                        TaskDao taskDao =
                                AppDatabase.getInstance(context)
                                        .taskDao();

                        for (Task task : tasks) {

                            taskDao.insertOrReplace(task);
                        }

                        Log.d(
                                "FirebaseRepo",
                                "✅ Sincronizadas " +
                                        tasks.size() +
                                        " tareas desde Firebase"
                        );
                    });
                })
                .addOnFailureListener(e ->
                        Log.e(
                                "FirebaseRepo",
                                "❌ Error al obtener tareas",
                                e
                        ));
    }

    // ==============================
    // FECHAS IMPORTANTES
    // ==============================

    public void saveImportantDate(ImportantDate date) {

        if (date.getFirebaseId() == null ||
                date.getFirebaseId().isEmpty()) {

            String newId = db.collection("ImportantDates")
                    .document()
                    .getId();

            date.setFirebaseId(newId);

            db.collection("ImportantDates")
                    .document(newId)
                    .set(date)
                    .addOnSuccessListener(aVoid -> {

                        Log.d("FirebaseRepo",
                                "✅ Fecha guardada con ID: " + newId);

                        AppDatabase.databaseWriteExecutor.execute(() ->
                                AppDatabase.getInstance(context)
                                        .importantDateDao()
                                        .insertOrReplace(date));
                    })
                    .addOnFailureListener(e ->
                            Log.e("FirebaseRepo",
                                    "❌ Error al guardar fecha",
                                    e));

        } else {

            db.collection("ImportantDates")
                    .document(date.getFirebaseId())
                    .set(date)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FirebaseRepo",
                                    "✅ Fecha actualizada en Firebase"))
                    .addOnFailureListener(e ->
                            Log.e("FirebaseRepo",
                                    "❌ Error al actualizar fecha",
                                    e));
        }
    }

    public void deleteImportantDate(ImportantDate date) {

        if (date.getFirebaseId() == null ||
                date.getFirebaseId().isEmpty()) {

            Log.w("FirebaseRepo",
                    "⚠️ No se puede eliminar: fecha sin firebaseId");

            return;
        }

        db.collection("ImportantDates")
                .document(date.getFirebaseId())
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("FirebaseRepo",
                                "✅ Fecha eliminada en Firebase"))
                .addOnFailureListener(e ->
                        Log.e("FirebaseRepo",
                                "❌ Error al eliminar fecha",
                                e));
    }

    public void fetchImportantDates(
            Consumer<List<ImportantDate>> callback) {

        db.collection("ImportantDates")
                .get()
                .addOnSuccessListener(query -> {

                    List<ImportantDate> dates = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        ImportantDate date =
                                doc.toObject(ImportantDate.class);

                        if (date != null) {

                            date.setFirebaseId(doc.getId());

                            dates.add(date);
                        }
                    }

                    callback.accept(dates);
                })
                .addOnFailureListener(e ->
                        Log.e("FirebaseRepo",
                                "❌ Error al obtener fechas",
                                e));
    }

    // ==============================
    // COMPARTIDOS
    // ==============================

    public Query getSharedTasks(String userId) {
        return db.collection("sharedTasks")
                .whereArrayContains("participants", userId);
    }

    public Query getSharedDates(String userId) {
        return db.collection("sharedDates")
                .whereArrayContains("participants", userId);
    }
}