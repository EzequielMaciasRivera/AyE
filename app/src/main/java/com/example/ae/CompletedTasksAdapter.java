package com.example.ae;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompletedTasksAdapter extends RecyclerView.Adapter<CompletedTasksAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskDao taskDao;

    public CompletedTasksAdapter(List<Task> taskList, TaskDao taskDao) {
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
                .inflate(R.layout.item_tasks_completed, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());

        // 🔹 Mostrar fecha de completado y autor
        String infoExtra = "";
        if (task.getCompletedAt() != null) {
            infoExtra = "Completada el: " + formatDate(task.getCompletedAt());
        }
        if (task.getCreatedBy() != null) {
            infoExtra += "\nAutor: " + task.getCreatedBy();
        }
        if (task.getDueDate() != null) {
            infoExtra += "\nCumplir antes de: " + formatDate(task.getDueDate());
        }
        holder.taskInfo.setText(infoExtra);

        // Botón eliminar
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que deseas eliminar esta tarea?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        taskDao.delete(task);

                        View layout = LayoutInflater.from(v.getContext())
                                .inflate(R.layout.custom_toast, (ViewGroup) v.getRootView(), false);

                        TextView text = layout.findViewById(R.id.toastText);
                        text.setText("Tarea eliminada");

                        ImageView icon = layout.findViewById(R.id.toastIcon);
                        icon.setImageResource(R.drawable.sorpresa);

                        Toast toast = new Toast(v.getContext());
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        // Botón editar
        holder.editButton.setOnClickListener(v -> {
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

                            // 🔹 Toast personalizado de éxito
                            View layout = LayoutInflater.from(v.getContext())
                                    .inflate(R.layout.custom_toast, (ViewGroup) v.getRootView(), false);

                            TextView text = layout.findViewById(R.id.toastText);
                            text.setText("Tarea actualizada correctamente");

                            ImageView icon = layout.findViewById(R.id.toastIcon);
                            icon.setImageResource(R.drawable.enamorado); // usa tu drawable

                            Toast toast = new Toast(v.getContext());
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();

                        } else {
                            // 🔹 Toast personalizado de error
                            View layout = LayoutInflater.from(v.getContext())
                                    .inflate(R.layout.custom_toast, (ViewGroup) v.getRootView(), false);

                            TextView text = layout.findViewById(R.id.toastText);
                            text.setText("El título no puede estar vacío");

                            ImageView icon = layout.findViewById(R.id.toastIcon);
                            icon.setImageResource(R.drawable.no); // usa tu drawable

                            Toast toast = new Toast(v.getContext());
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();
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
        TextView taskTitle, taskInfo;
        ImageButton deleteButton, editButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskInfo = itemView.findViewById(R.id.taskInfo);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}