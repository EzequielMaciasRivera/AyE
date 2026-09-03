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

                    animateFabChange(R.drawable.baseline_add_task_24);
                    fabAddTask.setOnClickListener(v -> showAddTaskDialog());

                    return true;
                } else if (id == R.id.nav_dates) {
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                    findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new ImportantDatesFragment())
                            .commit();

                    animateFabChange(R.drawable.outline_add_ad_24);
                    fabAddTask.setOnClickListener(v -> showAddDateDialog());

                    return true;
                }

                return false;
            }
        });

        // Acción inicial del FAB
        fabAddTask.setImageResource(R.drawable.baseline_add_task_24);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    // Método de animación fluido
    private void animateFabChange(int newIconRes) {
        fabAddTask.animate()
                .rotationBy(180f)
                .alpha(0f)
                .setDuration(700)
                .withEndAction(() -> {
                    fabAddTask.setImageResource(newIconRes);
                    fabAddTask.setRotation(0f);

                    fabAddTask.animate()
                            .rotationBy(360f)
                            .alpha(1f)
                            .setDuration(700)
                            .start();
                })
                .start();
    }

    private void showAddTaskDialog() {
        EditText input = new EditText(this);
        input.setHint("Escribe el título de la tarea");

        new AlertDialog.Builder(this)
                .setTitle("Nueva tarea")
                .setView(input)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String taskTitle = input.getText().toString().trim();

                    if (!taskTitle.isEmpty()) {
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        String userName = prefs.getString("userName", "desconocido");

                        Task newTask = new Task(taskTitle, userName);
                        taskDao.insert(newTask);

                        showCustomToast("Tarea agregada por " + userName, R.drawable.enamorado);
                    } else {
                        showCustomToast("El título no puede estar vacío", R.drawable.no);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showAddDateDialog() {
        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Título del evento");

        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Descripción");

        new AlertDialog.Builder(this)
                .setTitle("Nueva fecha importante")
                .setView(inputTitle)
                .setPositiveButton("Siguiente", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();

                    if (!title.isEmpty()) {
                        Toast.makeText(this, "Fecha agregada: " + title, Toast.LENGTH_SHORT).show();
                    } else {
                        showCustomToast("El título no puede estar vacío", R.drawable.no);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showCustomToast(String message, int iconRes) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toastText);
        text.setText(message);

        ImageView icon = layout.findViewById(R.id.toastIcon);
        icon.setImageResource(iconRes);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
