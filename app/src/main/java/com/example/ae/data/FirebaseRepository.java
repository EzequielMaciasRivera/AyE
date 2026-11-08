package com.example.ae.data;

import android.content.Context;
import android.util.Log;

import com.example.ae.model.Task;
import com.example.ae.model.ImportantDate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirebaseRepository {

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔹 Constructor que recibe el contexto
    public FirebaseRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    // 🔹 Guardar tarea en Firestore
    public void saveTask(Task task) {
        if (task.getFirebaseId() == null || task.getFirebaseId().isEmpty()) {
            db.collection("tasks")
                    .add(task)
                    .addOnSuccessListener(docRef -> {
                        task.setFirebaseId(docRef.getId());
                        // Actualiza también en Room
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            AppDatabase.getInstance(context).taskDao().insertOrReplace(task);
                        });
                        Log.d("FirebaseRepo", "✅ Tarea guardada con ID: " + docRef.getId());
                    })
                    .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al guardar tarea", e));
        } else {
            db.collection("tasks")
                    .document(task.getFirebaseId())
                    .set(task)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseRepo", "✅ Tarea actualizada en Firebase"))
                    .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al actualizar tarea", e));
        }
    }

    // 🔹 Guardar fecha importante en Firestore
    public void saveImportantDate(ImportantDate date) {
        if (date.getFirebaseId() == null || date.getFirebaseId().isEmpty()) {
            db.collection("ImportantDates")
                    .add(date)
                    .addOnSuccessListener(docRef -> {
                        date.setFirebaseId(docRef.getId());
                        Log.d("FirebaseRepo", "✅ Fecha guardada con ID: " + docRef.getId());
                    })
                    .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al guardar fecha", e));
        } else {
            db.collection("ImportantDates")
                    .document(date.getFirebaseId())
                    .set(date)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseRepo", "✅ Fecha actualizada en Firebase"))
                    .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al actualizar fecha", e));
        }
    }

    // 🔹 Obtener tareas compartidas
    public Query getSharedTasks(String userId) {
        return db.collection("sharedTasks")
                .whereArrayContains("participants", userId);
    }

    // 🔹 Obtener fechas importantes compartidas
    public Query getSharedDates(String userId) {
        return db.collection("sharedDates")
                .whereArrayContains("participants", userId);
    }

    // 🔹 Eliminar tarea
    public void deleteTask(Task task) {
        if (task.getFirebaseId() != null && !task.getFirebaseId().isEmpty()) {
            db.collection("tasks")
                    .document(task.getFirebaseId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseRepo", "✅ Tarea eliminada en Firebase"))
                    .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al eliminar tarea", e));
        } else {
            Log.w("FirebaseRepo", "⚠️ No se puede eliminar: tarea sin ID de Firebase");
        }
    }

    // 🔹 Obtener todas las tareas
    public void fetchTasks(Consumer<List<Task>> callback) {
        db.collection("tasks")
                .get()
                .addOnSuccessListener(query -> {
                    List<Task> tasks = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Task task = doc.toObject(Task.class);
                        if (task != null) {
                            task.setFirebaseId(doc.getId()); // 🔹 clave única
                            tasks.add(task);
                        }
                    }
                    callback.accept(tasks);
                })
                .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al obtener tareas", e));
    }



    // 🔹 Eliminar fecha importante
    public void deleteImportantDate(ImportantDate date) {
        if (date.getFirebaseId() != null && !date.getFirebaseId().isEmpty()) {
            db.collection("ImportantDates")
                    .document(date.getFirebaseId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseRepo", "✅ Fecha eliminada en Firebase"))
                    .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al eliminar fecha", e));
        } else {
            Log.w("FirebaseRepo", "⚠️ No se puede eliminar: fecha sin ID de Firebase");
        }
    }

    // 🔹 Obtener todas las fechas importantes
    public void fetchImportantDates(Consumer<List<ImportantDate>> callback) {
        db.collection("ImportantDates")
                .get()
                .addOnSuccessListener(query -> {
                    List<ImportantDate> dates = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        ImportantDate date = doc.toObject(ImportantDate.class);
                        if (date != null) {
                            date.setFirebaseId(doc.getId()); // 🔹 asegura que tenga firebaseId
                            dates.add(date);
                        }
                    }
                    callback.accept(dates);
                })
                .addOnFailureListener(e -> Log.e("FirebaseRepo", "❌ Error al obtener fechas", e));
    }
}
