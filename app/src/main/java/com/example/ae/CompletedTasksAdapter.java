package com.example.ae;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;

import java.util.List;

public class CompletedTasksAdapter extends RecyclerView.Adapter<CompletedTasksAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskDao taskDao;

    public CompletedTasksAdapter(List<Task> taskList, TaskDao taskDao) {
        this.taskList = taskList;
        this.taskDao = taskDao;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tasks_completed, parent, false); // 🔹 ahora sí correcto
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que deseas eliminar esta tarea?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        taskDao.delete(task);

                        // Inflar el layout del Toast personalizado
                        LayoutInflater inflater = LayoutInflater.from(v.getContext());
                        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) v.getRootView(), false);

                        // Cambiar texto dinámicamente
                        TextView text = layout.findViewById(R.id.toastText);
                        text.setText("Tarea eliminada");

                        // Cambiar ícono dinámicamente
                        ImageView icon = layout.findViewById(R.id.toastIcon);
                        icon.setImageResource(R.drawable.sorpresa); // tu drawable

                        // Crear y mostrar el Toast
                        Toast toast = new Toast(v.getContext());
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
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
        ImageButton deleteButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}