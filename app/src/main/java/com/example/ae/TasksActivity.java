package com.example.ae;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.widget.EditText;
import android.widget.Toast;

public class TasksActivity extends AppCompatActivity {

    private TaskDao taskDao;
    private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Referencias UI
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        fabAddTask = findViewById(R.id.addTaskButton);

        // Inicializar Room
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        taskDao = db.taskDao();

        // Configurar ViewPager con FragmentStateAdapter
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new PendingTasksFragment();
                } else {
                    return new CompletedTasksFragmet();
                }
            }

            @Override
            public int getItemCount() {
                return 2; // Dos pestañas
            }
        };

        viewPager.setAdapter(adapter);

        // Conectar TabLayout con ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Por hacer");
                    } else {
                        tab.setText("Hechas");
                    }
                }).attach();

        // Acción del botón flotante
        fabAddTask.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Escribe el título de la tarea");

            new AlertDialog.Builder(this)
                    .setTitle("Nueva tarea")
                    .setView(input)
                    .setPositiveButton("Agregar", (dialog, which) -> {
                        String taskTitle = input.getText().toString().trim();

                        if (!taskTitle.isEmpty()) {
                            // Crear tarea pendiente
                            Task newTask = new Task(taskTitle);
                            taskDao.insert(newTask);

                            Toast.makeText(this, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show();
                            // ❌ Ya no necesitas notifyDataSetChanged()
                            // ✅ LiveData refresca automáticamente los fragmentos
                        } else {
                            Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}