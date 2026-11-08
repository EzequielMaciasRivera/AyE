package com.example.ae;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.model.ImportantDate;
import com.example.ae.model.Task;
import com.example.ae.model.TasksViewModel;
import com.example.ae.model.ImportantDatesViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.util.Log;
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
    private TasksViewModel tasksViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);


        // Referencias UI
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        fabAddTask = findViewById(R.id.addTaskButton);

        // Inicializar Room
        db = AppDatabase.getInstance(getApplicationContext());
        taskDao = db.taskDao();

        // Inicializar ViewModel
        tasksViewModel = new ViewModelProvider(this).get(TasksViewModel.class);

        // 🔹 Observa cambios en las tareas
        tasksViewModel.getAllTasks().observe(this, tasks -> {
            // Aquí actualizas tu RecyclerView Adapter
            // Ejemplo: tasksAdapter.submitList(tasks);
        });

        // Configurar ViewPager con FragmentStateAdapter
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new PendingTasksFragment();
                } else {
                    return new CompletedTasksFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        };

        viewPager.setAdapter(adapter);

        // Conectar TabLayout con ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Por hacer" : "Hechas")
        ).attach();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

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

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ImportantDatesFragment())
                        .commit();

                animateFabChange(R.drawable.outline_add_ad_24);
                fabAddTask.setOnClickListener(v -> showAddDateDialog());
                return true;
            }

            return false;
        });

        // Acción inicial del FAB
        fabAddTask.setImageResource(R.drawable.baseline_add_task_24);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
        tasksViewModel.syncFromFirebase();
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

                        // 🔹 Guardar en Room + Firebase
                        tasksViewModel.insert(newTask);
                        Log.d("TasksActivity", "🟢 Insertando tarea: " + newTask.getTitle());

                        showCustomToast("Tarea agregada por " + userName, R.drawable.enamorado);
                    } else {
                        showCustomToast("El título no puede estar vacío", R.drawable.no);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showAddDateDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_date, null);
        EditText inputTitle = dialogView.findViewById(R.id.editTitle);
        EditText inputDesc = dialogView.findViewById(R.id.editDescription);
        Button dateButton = dialogView.findViewById(R.id.btnPickDate);

        final long[] selectedDate = {System.currentTimeMillis()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateButton.setText(sdf.format(new Date(selectedDate[0])));

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth);
                        selectedDate[0] = chosen.getTimeInMillis();
                        dateButton.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Nueva fecha importante")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();

                    if (!title.isEmpty()) {
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        String userName = prefs.getString("userName", "desconocido");

                        ImportantDate newDate = new ImportantDate();
                        newDate.setTitle(title);
                        newDate.setDescription(desc);
                        newDate.setDate(selectedDate[0]);
                        newDate.setCreatedBy(userName);

                        // 🔹 Guardar en Room + Firebase
                        ImportantDatesViewModel datesViewModel =
                                new ViewModelProvider(this).get(ImportantDatesViewModel.class);
                        datesViewModel.insert(newDate);

                        showCustomToast("Fecha agregada por " + userName, R.drawable.enamorado);
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

    }
    }