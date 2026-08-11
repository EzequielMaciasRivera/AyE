package com.example.ae;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ae.R;


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
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tasks) {
                    tabLayout.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                    findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
                    return true;
                } else if (id == R.id.nav_dates) {
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                    findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new ImportantDatesFragment())
                            .commit();
                    return true;
                }


                return false;
            }
        });



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
                            // Recuperar nombre del usuario desde SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            String userName = prefs.getString("userName", "desconocido");

                            // Crear tarea con creador y fecha de creación
                            Task newTask = new Task(taskTitle, userName);
                            taskDao.insert(newTask);

                            // Toast personalizado
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast, null);

                            TextView text = layout.findViewById(R.id.toastText);
                            text.setText("Tarea agregada por " + userName);

                            ImageView icon = layout.findViewById(R.id.toastIcon);
                            icon.setImageResource(R.drawable.enamorado);

                            Toast toast = new Toast(getApplicationContext());
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();

                        } else {
                            // Toast de error
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast, null);

                            TextView text = layout.findViewById(R.id.toastText);
                            text.setText("El título no puede estar vacío");

                            ImageView icon = layout.findViewById(R.id.toastIcon);
                            icon.setImageResource(R.drawable.no);

                            Toast toast = new Toast(getApplicationContext());
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}