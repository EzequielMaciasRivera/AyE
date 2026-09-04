package com.example.ae;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.model.ImportantDate;
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

import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.widget.Button;

public class TasksActivity extends AppCompatActivity {

    private TaskDao taskDao;
    private FloatingActionButton fabAddTask;
    private AppDatabase db;

    // ✅ Mantener instancia fija del fragmento de fechas
    private ImportantDatesFragment datesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        fabAddTask = findViewById(R.id.addTaskButton);

        db = AppDatabase.getInstance(getApplicationContext());
        taskDao = db.taskDao();

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
                return 2;
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("Por hacer");
                    else tab.setText("Hechas");
                }).attach();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // ✅ Crear instancia fija de fechas
        datesFragment = new ImportantDatesFragment();

        bottomNav.setOnItemSelectedListener(item -> {
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

                // ✅ Usar siempre la misma instancia
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, datesFragment)
                        .commit();

                animateFabChange(R.drawable.outline_add_ad_24);
                fabAddTask.setOnClickListener(v -> datesFragment.showAddDateDialog());
                return true;
            }

            return false;
        });

        fabAddTask.setImageResource(R.drawable.baseline_add_task_24);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    // ✅ Método de animación fluido
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

    private void showEditAuthorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar autor");

        EditText input = new EditText(this);
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentAuthor = prefs.getString("userName", "desconocido");
        input.setText(currentAuthor);

        builder.setView(input);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newAuthor = input.getText().toString().trim();
            if (!newAuthor.isEmpty()) {
                prefs.edit().putString("userName", newAuthor).apply();
                db.taskDao().updateAllAuthors(newAuthor);
                db.importantDateDao().updateAllAuthors(newAuthor);
                showCustomToast("Autor actualizado a " + newAuthor, R.drawable.enamorado);
            } else {
                showCustomToast("El autor no puede estar vacío", R.drawable.no);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
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

                        // ✅ Redirigir a la pestaña "Por hacer"
                        ViewPager2 viewPager = findViewById(R.id.viewPager);
                        viewPager.setCurrentItem(0, true);

                    } else {
                        showCustomToast("El título no puede estar vacío", R.drawable.no);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    public void showCustomToast(String message, int iconRes) {
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

    private void updateAuthor(String newAuthor) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("userName", newAuthor).apply();
        db.taskDao().updateAllAuthors(newAuthor);
        db.importantDateDao().updateAllAuthors(newAuthor);
        showCustomToast("Autor actualizado a " + newAuthor, R.drawable.enamorado);
    }
}