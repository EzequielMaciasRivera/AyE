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
import com.example.ae.data.NotificationHelper;
import com.example.ae.data.TaskDao;
import com.example.ae.model.ImportantDate;
import com.example.ae.model.Task;
import com.example.ae.model.TasksViewModel;
import com.example.ae.model.ImportantDatesViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import android.widget.Button;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Collections;

public class TasksActivity extends AppCompatActivity {

    private TaskDao taskDao;
    private FloatingActionButton fabAddTask;
    private AppDatabase db;
    private TasksViewModel tasksViewModel;
    private boolean modoMover = false;
    private float dX;
    private float dY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        NotificationHelper.createNotificationChannel(this);


        // Referencias UI
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);



        fabAddTask = findViewById(R.id.addTaskButton);
        SharedPreferences prefs =
                getSharedPreferences(
                        "AppPrefs",
                        MODE_PRIVATE
                );

        float x = prefs.getFloat(
                "fabX",
                -1
        );

        float y = prefs.getFloat(
                "fabY",
                -1
        );

        if (x != -1 && y != -1) {

            fabAddTask.post(() -> {
                fabAddTask.setX(x);
                fabAddTask.setY(y);
            });
        }
        fabAddTask.setOnLongClickListener(v -> {

            modoMover = !modoMover;

            fabAddTask.setAlpha(
                    modoMover ? 0.7f : 1f
            );

            Toast.makeText(
                    this,
                    modoMover
                            ? "Modo mover activado"
                            : "Modo mover desactivado",
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        });
        fabAddTask.setOnTouchListener((view, event) -> {

            if (!modoMover) {
                return false; // deja funcionar el click normal
            }

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();

                    return true;

                case MotionEvent.ACTION_MOVE:

                    float nuevaX = event.getRawX() + dX;
                    float nuevaY = event.getRawY() + dY;

                    nuevaX = Math.max(
                            0,
                            Math.min(
                                    nuevaX,
                                    ((View)view.getParent()).getWidth()
                                            - view.getWidth()
                            )
                    );

                    nuevaY = Math.max(
                            0,
                            Math.min(
                                    nuevaY,
                                    ((View)view.getParent()).getHeight()
                                            - view.getHeight()
                            )
                    );

                    view.setX(nuevaX);
                    view.setY(nuevaY);

                    return true;

                case MotionEvent.ACTION_UP:

                    getSharedPreferences(
                            "AppPrefs",
                            MODE_PRIVATE
                    )
                            .edit()
                            .putFloat("fabX", view.getX())
                            .putFloat("fabY", view.getY())
                            .apply();

                    modoMover = false;

                    fabAddTask.setAlpha(1f);

                    Toast.makeText(
                            TasksActivity.this,
                            "Posición guardada",
                            Toast.LENGTH_SHORT
                    ).show();

                    return true;
                default:
                    return false;
            }
        });
        // Inicializar Room
        // ✅ Inicializa el ViewModel primero
        tasksViewModel = new ViewModelProvider(this).get(TasksViewModel.class);

        tasksViewModel.syncFromFirebase();
        String ownerId =
                prefs.getString(
                        "ownerId",
                        null
                );

        if (ownerId == null) {

            ownerId = UUID.randomUUID().toString();

            prefs.edit()
                    .putString(
                            "ownerId",
                            ownerId
                    )
                    .apply();
        }

        final String finalOwnerId = ownerId;

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {

                    Log.d(
                            "FCM_TOKEN",
                            token
                    );

                    Log.d(
                            "OWNER_ID",
                            finalOwnerId
                    );

                    HashMap<String, Object> tokenData =
                            new HashMap<>();

                    tokenData.put(
                            "token",
                            token
                    );

                    tokenData.put(
                            "ownerId",
                            finalOwnerId
                    );

                    FirebaseFirestore.getInstance()
                            .collection("deviceTokens")
                            .document(token)
                            .set(tokenData);
                });

// ✅ Pasa el ViewModel al adapter




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

                        String ownerId = prefs.getString(
                                "ownerId",
                                null
                        );

                        if (ownerId == null) {

                            ownerId = java.util.UUID
                                    .randomUUID()
                                    .toString();

                            prefs.edit()
                                    .putString("ownerId", ownerId)
                                    .apply();
                        }

                        newTask.setCreatedBy(userName);
                        newTask.setOwnerId(ownerId);

                        tasksViewModel.insert(newTask);

                        ViewPager2 viewPager = findViewById(R.id.viewPager);

// Si estoy en "Hechas", volver a "Por hacer"
                        if (viewPager.getCurrentItem() == 1) {
                            viewPager.setCurrentItem(0, true);
                        }

                        Log.d(
                                "TasksActivity",
                                "🟢 Insertando tarea: "
                                        + newTask.getTitle()
                        );

                        showCustomToast(
                                "Tarea agregada por " + userName,
                                R.drawable.enamorado
                        );
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

                        ImportantDatesViewModel datesViewModel =
                                new ViewModelProvider(this).get(ImportantDatesViewModel.class);
                        String ownerId =
                                prefs.getString(
                                        "ownerId",
                                        null
                                );

                        if (ownerId == null) {

                            ownerId = UUID.randomUUID().toString();

                            prefs.edit()
                                    .putString(
                                            "ownerId",
                                            ownerId
                                    )
                                    .apply();
                        }

                        newDate.setOwnerId(ownerId);
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
        TextView text = layout.findViewById(R.id.toastText);
        text.setText(message);
        ImageView icon = layout.findViewById(R.id.toastIcon);
        icon.setImageResource(iconRes);
        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}
