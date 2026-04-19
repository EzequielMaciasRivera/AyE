package com.example.ae;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.widget.EditText;
import android.widget.Toast;


public class TasksActivity extends AppCompatActivity {

    private RecyclerView taskRecyclerView;
    private Button addTaskButton;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private TaskDao taskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        addTaskButton = findViewById(R.id.addTaskButton);


        taskList = new ArrayList<>(taskDao.getAllTasks());
        adapter = new TaskAdapter(taskList, taskDao);
        taskRecyclerView.setAdapter(adapter);

        // Inicializar Room
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "tasks-db")
                .allowMainThreadQueries() // ⚠️ Solo para pruebas, luego usar Async
                .build();

        taskDao = db.taskDao();

        // Cargar tareas desde la BD
        taskList = new ArrayList<Task>((Collection<? extends Task>) taskDao.getAllTasks());

        adapter = new TaskAdapter(taskList, taskDao);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(adapter);

        // Acción para agregar nueva tarea
        addTaskButton.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Escribe el título de la tarea");

            new AlertDialog.Builder(this)
                    .setTitle("Nueva tarea")
                    .setView(input)
                    .setPositiveButton("Agregar", (dialog, which) -> {
                        // 🔹 Aquí empieza la lógica al presionar "Agregar"
                        String taskTitle = input.getText().toString().trim();

                        if (!taskTitle.isEmpty()) {
                            // Crear nueva tarea
                            Task newTask = new Task(taskTitle, false);

                            // Insertar en la BD
                            taskDao.insert(newTask);

                            // Recargar lista
                            taskList.clear();
                            taskList.addAll(taskDao.getAllTasks());
                            adapter.notifyDataSetChanged();

                            // ✅ Aquí va el Toast
                            Toast.makeText(this, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            // ✅ Toast de error si el campo está vacío
                            Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }


}