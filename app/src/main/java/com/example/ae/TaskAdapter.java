package com.example.ae;

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

        // 🔹 Mostrar información extra (fecha y creador)
        String infoExtra = "Creado: " + formatDate(task.getCreatedAt());
        if (task.getDueDate() != null) {
            infoExtra += "\nCumplir antes de: " + formatDate(task.getDueDate());
        }
        if (task.getCreatedBy() != null) {
            infoExtra += "\nPor: " + task.getCreatedBy();
        }
        holder.taskInfo.setText(infoExtra);

        // 🔹 Desactivar listener antes de setChecked
        holder.taskCheckBox.setOnCheckedChangeListener(null);
        holder.taskCheckBox.setChecked(task.isCompleted());

        // 🔹 Reactivar listener solo para cambios del usuario
        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AlertDialog dialog = new AlertDialog.Builder(buttonView.getContext())
                        .setTitle("Confirmar")
                        .setMessage("¿Seguro que quieres marcar esta tarea como completada?")
                        .setPositiveButton("Sí", (d, which) -> {
                            task.setCompleted(true);
                            taskDao.update(task);

                            Toast.makeText(buttonView.getContext(),
                                    "Tarea marcada como completada",
                                    Toast.LENGTH_SHORT).show();

                            d.dismiss();
                        })
                        .setNegativeButton("No", (d, which) -> {
                            holder.taskCheckBox.setChecked(false);
                            d.dismiss();
                        })
                        .create();

                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
            } else {
                task.setCompleted(false);
                taskDao.update(task);
            }
        });

        // 🔹 Botón eliminar
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que deseas eliminar esta tarea?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        taskDao.delete(task);

                        LayoutInflater inflater = LayoutInflater.from(v.getContext());
                        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) v.getRootView(), false);

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

        // 🔹 Botón editar
        holder.editButton.setOnClickListener(v -> {
            EditText input = new EditText(v.getContext());
            input.setText(task.getTitle()); // mostrar título actual

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Editar tarea")
                    .setView(input)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String nuevoTitulo = input.getText().toString().trim();
                        if (!nuevoTitulo.isEmpty()) {
                            task.setTitle(nuevoTitulo);
                            taskDao.update(task);

                            Toast.makeText(v.getContext(),
                                    "Tarea actualizada correctamente",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(v.getContext(),
                                    "El título no puede estar vacío",
                                    Toast.LENGTH_SHORT).show();
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
        ImageButton editButton; // 🔹 nuevo botón

        TaskViewHolder(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskInfo = itemView.findViewById(R.id.taskInfo);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton); // referencia al botón editar
        }
    }

    // 🔹 Método auxiliar para formatear fechas
    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}