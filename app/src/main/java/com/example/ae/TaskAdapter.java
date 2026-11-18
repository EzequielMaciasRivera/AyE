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

import com.example.ae.model.ImportantDatesViewModel;
import com.example.ae.model.Task;
import com.example.ae.model.TasksViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import android.app.DatePickerDialog;
import java.util.Calendar;
import android.widget.Button;
import android.util.TypedValue;


public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {


    @Override
    public void submitList(java.util.List<Task> list) {

        super.submitList(list);
    }

    public interface OnTaskClickListener {
        void onEdit(Task task);
        void onDelete(Task task);
    }


    private final OnTaskClickListener listener;

    private final TasksViewModel tasksViewModel;
    private final ImportantDatesViewModel importantDatesViewModel;

    public TaskAdapter(
            TasksViewModel tasksViewModel,
            ImportantDatesViewModel importantDatesViewModel,
            OnTaskClickListener listener
    ) {

        super(DIFF_CALLBACK);

        this.tasksViewModel = tasksViewModel;
        this.importantDatesViewModel = importantDatesViewModel;
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
        holder.bind(
                task,
                tasksViewModel,
                importantDatesViewModel,
                listener,
                prefs
        );
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        androidx.cardview.widget.CardView cardView;
        TextView taskTitle;
        TextView taskInfo;
        CheckBox taskCheckBox;
        ImageButton deleteButton;
        ImageButton editButton;
        ImageButton editAuthorButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            cardView =
                    (androidx.cardview.widget.CardView)
                            itemView;
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskInfo = itemView.findViewById(R.id.taskInfo);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
            editAuthorButton = itemView.findViewById(R.id.editAuthorButton);
        }

        void bind(
                Task task,
                TasksViewModel tasksViewModel,
                ImportantDatesViewModel importantDatesViewModel,
                OnTaskClickListener listener,
                SharedPreferences prefs
        ) {


            taskTitle.setText(task.getTitle());
            taskTitle.setText(task.getTitle());

            taskTitle.setTextColor(
                    android.graphics.Color.BLACK
            );
            // Mostrar información extra
            String infoExtra = "Creada el: " + formatDate(task.getCreatedAt());
            int backgroundColor =
                    android.graphics.Color.parseColor(
                            "#FAFAFA"
                    );

            if (task.getDueDate() != null) {

                infoExtra +=
                        "\n📅 Cumplir antes de: "
                                + formatDate(
                                task.getDueDate()
                        );

                long diferenciaMillis =
                        task.getDueDate()
                                - System.currentTimeMillis();

                long horas =
                        diferenciaMillis
                                / (1000 * 60 * 60);

                long dias =
                        diferenciaMillis
                                / (1000 * 60 * 60 * 24);

                if (diferenciaMillis < 0) {

                    long diasVencida =
                            Math.abs(horas / 24);

                    if (diasVencida == 0) {

                        infoExtra +=
                                "\n❗Venció hace "
                                        + Math.abs(horas)
                                        + " horas";

                    } else {

                        infoExtra +=
                                "\n❗Vencida hace "
                                        + diasVencida
                                        + " días";
                    }

                    backgroundColor =
                            android.graphics.Color.parseColor(
                                    "#F8D7DA"
                            );

                } else if (horas < 24) {

                    infoExtra +=
                            "\n🚨 Vence en "
                                    + horas
                                    + " horas";

                    backgroundColor =
                            android.graphics.Color.parseColor(
                                    "#F8D7DA"
                            );

                } else if (dias <= 3) {

                    infoExtra +=
                            "\n🟡 Faltan "
                                    + dias
                                    + " días";

                    backgroundColor =
                            android.graphics.Color.parseColor(
                                    "#FFF3CD"
                            );

                } else {

                    infoExtra +=
                            "\n🟢 Faltan "
                                    + dias
                                    + " días";

                    backgroundColor =
                            android.graphics.Color.parseColor(
                                    "#D4EDDA"
                            );
                }
            }

            if (task.getCreatedBy() != null) {
                infoExtra += "\nAutor: " + task.getCreatedBy();
            }

            taskInfo.setText(infoExtra);

            cardView.setCardBackgroundColor(
                    backgroundColor
            );

            String currentOwnerId =
                    prefs.getString(
                            "ownerId",
                            ""
                    );

            boolean esAutor =
                    task.getOwnerId() != null
                            && currentOwnerId != null
                            && task.getOwnerId().trim()
                            .equals(currentOwnerId.trim());
            taskCheckBox.setVisibility(
                    esAutor ? View.VISIBLE : View.GONE
            );

            editButton.setVisibility(
                    esAutor ? View.VISIBLE : View.GONE
            );

            deleteButton.setVisibility(
                    esAutor ? View.VISIBLE : View.GONE
            );

            editAuthorButton.setVisibility(
                    esAutor ? View.VISIBLE : View.GONE
            );

            // Checkbox
            taskCheckBox.setOnCheckedChangeListener(null);
            taskCheckBox.setChecked(task.isCompleted());
            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                task.setCompletedAt(isChecked ? System.currentTimeMillis() : null);
                tasksViewModel.update(task); // ✅ usa ViewModel
               // firebaseRepo.sendTaskCompletedNotification(
              //          task
               // );
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

                    showCustomToast(
                            v.getContext(),
                            "Solo el autor puede editar esta tarea",
                            R.drawable.no
                    );

                    return;
                }

                View dialogView =
                        LayoutInflater.from(v.getContext())
                                .inflate(
                                        R.layout.dialog_edit_task,
                                        null
                                );

                EditText input =
                        dialogView.findViewById(
                                R.id.editTaskTitle
                        );

                Button btnDueDate =
                        dialogView.findViewById(
                                R.id.btnEditDueDate
                        );

                input.setText(
                        task.getTitle()
                );

                final Long[] selectedDueDate =
                        { task.getDueDate() };

                if (task.getDueDate() != null) {

                    btnDueDate.setText(
                            "📅 "
                                    + formatDate(
                                    task.getDueDate()
                            )
                    );
                }

                btnDueDate.setOnClickListener(btn -> {

                    Calendar calendar =
                            Calendar.getInstance();

                    DatePickerDialog picker =
                            new DatePickerDialog(
                                    v.getContext(),
                                    (view, year, month, day) -> {

                                        Calendar due =
                                                Calendar.getInstance();

                                        due.set(
                                                year,
                                                month,
                                                day,
                                                23,
                                                59
                                        );

                                        selectedDueDate[0] =
                                                due.getTimeInMillis();

                                        btnDueDate.setText(
                                                "📅 "
                                                        + day
                                                        + "/"
                                                        + (month + 1)
                                                        + "/"
                                                        + year
                                        );

                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                            );

                    picker.show();
                });

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Editar tarea")
                        .setView(dialogView)
                        .setPositiveButton("Guardar",
                                (dialog, which) -> {

                                    String nuevoTitulo =
                                            input.getText()
                                                    .toString()
                                                    .trim();

                                    if (!nuevoTitulo.isEmpty()) {

                                        Task tareaActualizada =
                                                new Task(task);

                                        tareaActualizada.setTitle(
                                                nuevoTitulo
                                        );

                                        tareaActualizada.setDueDate(
                                                selectedDueDate[0]
                                        );

                                        tasksViewModel.update(
                                                tareaActualizada
                                        );

                                        showCustomToast(
                                                v.getContext(),
                                                "Tarea actualizada",
                                                R.drawable.enamorado
                                        );
                                    }
                                })
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .show();
            });

            // Botón editar autor
            editAuthorButton.setOnClickListener(v -> {

                EditText input = new EditText(v.getContext());
                input.setText(task.getCreatedBy());

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Editar autor")
                        .setView(input)
                        .setPositiveButton("Guardar", (dialog, which) -> {

                            String nuevoAutor =
                                    input.getText().toString().trim();

                            if (!nuevoAutor.isEmpty()) {

                                tasksViewModel.updateAuthorForAllTasks(
                                        currentOwnerId,
                                        nuevoAutor
                                );

                                importantDatesViewModel.updateAuthorForAllDates(
                                        currentOwnerId,
                                        nuevoAutor
                                );

                                prefs.edit()
                                        .putString(
                                                "userName",
                                                nuevoAutor
                                        )
                                        .apply();

                                showCustomToast(
                                        v.getContext(),
                                        "Autor actualizado correctamente",
                                        R.drawable.enamorado
                                );

                            } else {

                                showCustomToast(
                                        v.getContext(),
                                        "El nombre no puede estar vacío",
                                        R.drawable.no
                                );
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
