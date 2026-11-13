package com.example.ae;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.model.Task;
import com.example.ae.model.TasksViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {


    @Override
    public void submitList(java.util.List<Task> list) {

        Log.d(
                "ADAPTER_DEBUG",
                "submitList tamaño = "
                        + (list == null ? 0 : list.size())
        );

        if (list != null) {

            for (Task task : list) {

                Log.d(
                        "ADAPTER_DEBUG",
                        "Tarea -> "
                                + task.getTitle()
                                + " | "
                                + task.getFirebaseId()
                );
            }
        }

        super.submitList(list);
    }

    public interface OnTaskClickListener {
        void onEdit(Task task);
        void onDelete(Task task);
    }

    private final TasksViewModel tasksViewModel;
    private final OnTaskClickListener listener;

    public TaskAdapter(TasksViewModel tasksViewModel, OnTaskClickListener listener) {
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
                    Log.d("DIFF_DEBUG",
                            "oldTitle=" + oldItem.getTitle()
                                    + " | newTitle=" + newItem.getTitle());

                    return java.util.Objects.equals(
                            oldItem.getTitle(),
                            newItem.getTitle())

                            && java.util.Objects.equals(
                            oldItem.getCreatedBy(),
                            newItem.getCreatedBy())

                            && java.util.Objects.equals(
                            oldItem.getDueDate(),
                            newItem.getDueDate())

                            && java.util.Objects.equals(
                            oldItem.getCompletedAt(),
                            newItem.getCompletedAt())

                            && oldItem.isCompleted()
                            == newItem.isCompleted()

                            && oldItem.getCreatedAt()
                            == newItem.getCreatedAt();
                }

            };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        SharedPreferences prefs = holder.itemView.getContext()
                .getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
        holder.bind(task, tasksViewModel, listener, prefs);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskInfo;
        CheckBox taskCheckBox;
        ImageButton deleteButton;
        ImageButton editButton;
        ImageButton editAuthorButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskInfo = itemView.findViewById(R.id.taskInfo);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
            editAuthorButton = itemView.findViewById(R.id.editAuthorButton);
        }

        void bind(Task task, TasksViewModel tasksViewModel, OnTaskClickListener listener, SharedPreferences prefs) {
            Log.d(
                    "BIND_DEBUG",
                    "hash=" + System.identityHashCode(task)
                            + " title=" + task.getTitle()
            );
            taskTitle.setText(task.getTitle());

            // Mostrar información extra
            String infoExtra = "Creada el: " + formatDate(task.getCreatedAt());
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

            // Checkbox
            taskCheckBox.setOnCheckedChangeListener(null);
            taskCheckBox.setChecked(task.isCompleted());
            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                task.setCompletedAt(isChecked ? System.currentTimeMillis() : null);
                tasksViewModel.update(task); // ✅ usa ViewModel
                showCustomToast(buttonView.getContext(),
                        isChecked ? "Tarea marcada como completada" : "Tarea marcada como pendiente",
                        isChecked ? R.drawable.enamorado : R.drawable.sorpresa);
            });

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
                        .setMessage("¿Seguro que deseas eliminar esta tarea?")
                        .setPositiveButton("Sí", (dialog, which) -> {

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
                        .setTitle("Editar tarea")
                        .setView(input)
                        .setPositiveButton("Guardar", (dialog, which) -> {
                            String nuevoTitulo = input.getText().toString();
                            if (!nuevoTitulo.trim().isEmpty()) {

                                Task taskActualizada = new Task(task);
                                taskActualizada.setTitle(nuevoTitulo);
                                tasksViewModel.update(taskActualizada);

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
                            }else {
                                showCustomToast(v.getContext(),
                                        "El nombre no puede estar vacío",
                                        R.drawable.no);
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        }

        private String formatDate(long timestamp) {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        }

        private void showCustomToast(android.content.Context context, String message, int iconRes) {
            View layout = LayoutInflater.from(context)
                    .inflate(R.layout.custom_toast, null);
            TextView text = layout.findViewById(R.id.toastText);
            text.setText(message);
            ImageView icon = layout.findViewById(R.id.toastIcon);
            icon.setImageResource(iconRes);
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        }
    }
}
