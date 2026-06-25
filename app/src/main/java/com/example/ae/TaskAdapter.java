package com.example.ae;

import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskDao taskDao;

    public TaskAdapter(List<Task> taskList, TaskDao taskDao) {
        this.taskList = taskList;
        this.taskDao = taskDao;
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());

        // Mostrar información extra
        String infoExtra = "Creada el: " + formatDate(task.getCreatedAt());
        if (task.getDueDate() != null) {
            infoExtra += "\nCumplir antes de: " + formatDate(task.getDueDate());
        }
        if (task.getCreatedBy() != null) {
            infoExtra += "\nAutor: " + task.getCreatedBy();
        }
        holder.taskInfo.setText(infoExtra);

        // 🔹 Usar una sola instancia de SharedPreferences
        SharedPreferences prefs = holder.itemView.getContext()
                .getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
        String currentUser = prefs.getString("userName", "").trim();

        // Mostrar/ocultar botones según autor (normalizado)
        boolean esAutor = task.getCreatedBy() != null &&
                task.getCreatedBy().trim().equalsIgnoreCase(currentUser);
        holder.editButton.setVisibility(esAutor ? View.VISIBLE : View.GONE);
        holder.deleteButton.setVisibility(esAutor ? View.VISIBLE : View.GONE);
        holder.editAuthorButton.setVisibility(esAutor ? View.VISIBLE : View.GONE);

        // Checkbox
        holder.taskCheckBox.setOnCheckedChangeListener(null);
        holder.taskCheckBox.setChecked(task.isCompleted());

        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            if (isChecked) {
                task.setCompletedAt(System.currentTimeMillis());
            } else {
                task.setCompletedAt(null);
            }
            taskDao.update(task);

            showCustomToast(buttonView.getContext(),
                    isChecked ? "Tarea marcada como completada" : "Tarea marcada como pendiente",
                    isChecked ? R.drawable.enamorado : R.drawable.sorpresa);
        });

        // Botón eliminar
        holder.deleteButton.setOnClickListener(v -> {
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
                        taskDao.delete(task);
                        showCustomToast(v.getContext(),
                                "Tarea eliminada",
                                R.drawable.sorpresa);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Botón editar título
        holder.editButton.setOnClickListener(v -> {
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
                        String nuevoTitulo = input.getText().toString().trim();
                        if (!nuevoTitulo.isEmpty()) {
                            task.setTitle(nuevoTitulo);
                            taskDao.update(task);

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

        // Botón editar autor con actualización global
        holder.editAuthorButton.setOnClickListener(v -> {
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
                            // 🔹 Actualizar todas las tareas en la base de datos
                            taskDao.updateAllAuthors(nuevoAutor);

                            // 🔹 Actualizar SharedPreferences
                            prefs.edit().putString("userName", nuevoAutor).apply();


                            showCustomToast(v.getContext(),
                                    "Autor actualizado en todas las tareas",
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
    }

    @Override
    public int getItemCount() {
        return taskList.size();
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
