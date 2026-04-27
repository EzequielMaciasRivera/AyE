package com.example.ae;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskDao taskDao;

    public TaskAdapter(List<Task> taskList, TaskDao taskDao) {
        this.taskList = taskList;
        this.taskDao = taskDao;
    }

    // Método para actualizar la lista desde LiveData
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

        // 🔹 Desactivar listener antes de setChecked
        holder.taskCheckBox.setOnCheckedChangeListener(null);
        holder.taskCheckBox.setChecked(task.isCompleted());

        // 🔹 Reactivar listener solo para cambios del usuario
        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            taskDao.update(task);
        });

        // Botón eliminar → solo borrar en BD
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
        CheckBox taskCheckBox;
        ImageButton deleteButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}