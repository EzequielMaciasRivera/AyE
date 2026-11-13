package com.example.ae;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.model.Task;
import com.example.ae.model.TasksViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CompletedTasksAdapter extends ListAdapter<Task, CompletedTasksAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onEdit(Task task);
        void onDelete(Task task);
    }

    private final TasksViewModel tasksViewModel;
    private final OnTaskClickListener listener;

    public CompletedTasksAdapter(TasksViewModel tasksViewModel, OnTaskClickListener listener) {
        super(DIFF_CALLBACK);
        this.tasksViewModel = tasksViewModel;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Task>() {
                @Override
                public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
                    // ✅ Compara por firebaseId
                    return oldItem.getFirebaseId() != null &&
                            oldItem.getFirebaseId().equals(newItem.getFirebaseId());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Task oldItem,
                        @NonNull Task newItem) {

                    return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                            && Objects.equals(oldItem.getCreatedBy(), newItem.getCreatedBy())
                            && Objects.equals(oldItem.getDueDate(), newItem.getDueDate())
                            && Objects.equals(oldItem.getCompletedAt(), newItem.getCompletedAt())
                            && oldItem.isCompleted() == newItem.isCompleted()
                            && oldItem.getCreatedAt() == newItem.getCreatedAt();
                }
            };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tasks_completed, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        SharedPreferences prefs = holder.itemView.getContext()
                .getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        holder.bind(task, tasksViewModel, listener, prefs);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskInfo;
        ImageButton deleteButton;
        ImageButton editButton;
        ImageButton editAuthorButton;
        ImageButton editDateButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskInfo = itemView.findViewById(R.id.taskInfo);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
            editAuthorButton = itemView.findViewById(R.id.editAuthorButton);
            editDateButton = itemView.findViewById(R.id.editDateButton);
        }

        void bind(Task task, TasksViewModel tasksViewModel, OnTaskClickListener listener, SharedPreferences prefs) {
            taskTitle.setText(task.getTitle());
            taskTitle.setPaintFlags(
                    taskTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            );

            String infoExtra = "Completada el: " + formatDate(task.getCompletedAt());
            if (task.getDueDate() != null) {
                infoExtra += "\nCumplir antes de: " + formatDate(task.getDueDate());
            }
            if (task.getCreatedBy() != null) {
                infoExtra += "\nAutor: " + task.getCreatedBy();
            }
            taskInfo.setText(infoExtra);

            String currentUser = prefs.getString("userName", "").trim();
            boolean esAutor = task.getCreatedBy() != null &&
                    task.getCreatedBy().trim().equalsIgnoreCase(currentUser);

            editButton.setVisibility(esAutor ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(esAutor ? View.VISIBLE : View.GONE);
            editAuthorButton.setVisibility(esAutor ? View.VISIBLE : View.GONE);
            editDateButton.setVisibility(esAutor ? View.VISIBLE : View.GONE);

            // Botón eliminar
            deleteButton.setOnClickListener(v -> {
                if (!esAutor) {
                    showCustomToast(v.getContext(),
                            "Solo el autor puede eliminar esta tarea",
                            R.drawable.no);
                    return;
                }
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Eliminar tarea")
                        .setMessage("¿Seguro que deseas eliminar esta tarea completada?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            tasksViewModel.delete(task); // ✅ usa ViewModel
                            listener.onDelete(task);
                            showCustomToast(v.getContext(),
                                    "Tarea eliminada",
                                    R.drawable.sorpresa);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });

            // Botón editar título
            editButton.setOnClickListener(v -> {
                if (!esAutor) {
                    showCustomToast(v.getContext(),
                            "Solo el autor puede editar esta tarea",
                            R.drawable.no);
                    return;
                }
                EditText input = new EditText(v.getContext());
                input.setText(task.getTitle());
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Editar tarea completada")
                        .setView(input)
                        .setPositiveButton("Guardar", (dialog, which) -> {
                            String nuevoTitulo = input.getText().toString();
                            if (!nuevoTitulo.trim().isEmpty()) {
                                Task taskActualizada = new Task(task);
                                taskActualizada.setTitle(nuevoTitulo);
                                tasksViewModel.update(taskActualizada);



                                Log.d(
                                        "SPACE_DEBUG",
                                        "[" + task.getTitle() + "]"
                                );
                                showCustomToast(v.getContext(),
                                        "Tarea actualizada correctamente",
                                        R.drawable.enamorado);
                            } else {
                                showCustomToast(v.getContext(),
                                        "El título no puede estar vacío",
                                        R.drawable.no);
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });

            // Botón editar autor
            editAuthorButton.setOnClickListener(v -> {
                if (!esAutor) {
                    showCustomToast(v.getContext(),
                            "Solo el autor puede editar su nombre",
                            R.drawable.no);
                    return;
                }
                EditText input = new EditText(v.getContext());
                input.setText(task.getCreatedBy());
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Editar autor")
                        .setView(input)
                        .setPositiveButton("Guardar", (dialog, which) -> {
                            String nuevoAutor = input.getText().toString().trim();
                            if (!nuevoAutor.isEmpty()) {
                                Task taskActualizada = new Task(task);
                                taskActualizada.setCreatedBy(nuevoAutor);
                                tasksViewModel.update(taskActualizada);

                                prefs.edit().putString("userName", nuevoAutor).apply();
                                showCustomToast(v.getContext(),
                                        "Autor actualizado correctamente",
                                        R.drawable.enamorado);
                            } else {
                                showCustomToast(v.getContext(),
                                        "El nombre no puede estar vacío",
                                        R.drawable.no);
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });

            // Botón editar fecha completada
            editDateButton.setOnClickListener(v -> {
                if (!esAutor) {
                    showCustomToast(v.getContext(),
                            "Solo el autor puede editar la fecha",
                            R.drawable.no);
                    return;
                }
                final Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(v.getContext(),
                        (view, year, month, dayOfMonth) -> {
                            TimePickerDialog timePicker = new TimePickerDialog(v.getContext(),
                                    (timeView, hourOfDay, minute) -> {
                                        Calendar newDate = Calendar.getInstance();
                                        newDate.set(year, month, dayOfMonth, hourOfDay, minute);

                                        Task taskActualizada = new Task(task);
                                        taskActualizada.setCompletedAt(newDate.getTimeInMillis());
                                        tasksViewModel.update(taskActualizada);

                                        showCustomToast(v.getContext(),
                                                "Fecha de completado actualizada",
                                                R.drawable.enamorado);
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true);
                            timePicker.show();
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            });
        }

        private String formatDate(Long timestamp) {
            if (timestamp == null || timestamp == 0) return "-";
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        }

        private void showCustomToast(Context context, String message, int iconRes) {
            View layout = LayoutInflater.from(context)
                    .inflate(R.layout.custom_toast, null);

            TextView text = layout.findViewById(R.id.toastText);
            text.setText(message);

            ImageView icon = layout.findViewById(R.id.toastIcon);
            icon.setImageResource(iconRes);

            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show(); // ✅ ahora sí completo
        }
    }
}