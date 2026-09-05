package com.example.ae;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.AppDatabase;
import com.example.ae.model.ImportantDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ImportantDatesFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImportantDatesAdapter adapter;
    private AppDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_important_dates, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = AppDatabase.getInstance(getContext());
        int count = db.importantDateDao().getCount();
        Log.d("Fragment", "Registros actuales en la tabla: " + count);

        // ✅ Constructor del adapter con listener
        adapter = new ImportantDatesAdapter(new ImportantDatesAdapter.OnDateClickListener() {
            @Override
            public void onEdit(ImportantDate date) {
                showEditDateDialog(date);
            }

            @Override
            public void onEditAuthor(ImportantDate date) {
                showEditAuthorDialog(date);
            }

            @Override
            public void onDelete(ImportantDate date) {
                db.importantDateDao().deleteDate(date);
                Log.d("Fragment", "Fecha eliminada: " + date.getTitle());

                // 🔹 Toast después de eliminar
                ((TasksActivity) requireActivity())
                        .showCustomToast("Fecha eliminada correctamente", R.drawable.sorpresa);
            }
        });

        recyclerView.setAdapter(adapter);

        // Observa los datos en LiveData
        db.importantDateDao().getAllDates().observe(getViewLifecycleOwner(), dates -> {
            adapter.setDates(dates);
            Log.d("Fragment", "Observer recibió " + dates.size() + " registros");
        });

        return view;
    }

    // 🔹 Diálogo para agregar nueva fecha
    public void showAddDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nueva fecha importante");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_date, null);
        EditText titleInput = dialogView.findViewById(R.id.editTitle);
        EditText descriptionInput = dialogView.findViewById(R.id.editDescription);
        Button dateButton = dialogView.findViewById(R.id.btnPickDate);

        final long[] selectedDate = {System.currentTimeMillis()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateButton.setText(sdf.format(new Date(selectedDate[0])));

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
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

        builder.setView(dialogView);
        builder.setPositiveButton("Guardar", null);
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                titleInput.setError("El título no puede estar vacío");
                return;
            }

            ImportantDate newDate = new ImportantDate();
            newDate.setTitle(title);
            newDate.setDescription(descriptionInput.getText().toString().trim());
            newDate.setDate(selectedDate[0]);

            SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String userName = prefs.getString("userName", "desconocido");
            newDate.setAuthor(userName);

            db.importantDateDao().insertDate(newDate);
            Log.d("Fragment", "Fecha insertada: " + newDate.getTitle());

            // 🔹 Toast después de guardar
            ((TasksActivity) requireActivity())
                    .showCustomToast("Fecha agregada por " + userName, R.drawable.enamorado);

            dialog.dismiss();
        });
    }

    // 🔹 Diálogo para editar fecha
    private void showEditDateDialog(ImportantDate date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar fecha importante");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_date, null);
        EditText titleInput = dialogView.findViewById(R.id.editTitle);
        EditText descriptionInput = dialogView.findViewById(R.id.editDescription);
        Button dateButton = dialogView.findViewById(R.id.btnPickDate);

        titleInput.setText(date.getTitle());
        descriptionInput.setText(date.getDescription());

        final long[] selectedDate = {date.getDate()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateButton.setText(sdf.format(new Date(selectedDate[0])));

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
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

        builder.setView(dialogView);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                // 🔹 Mostrar toast si el título está vacío
                ((TasksActivity) requireActivity())
                        .showCustomToast("El título no puede estar vacío", R.drawable.no);
                return;
            }

            date.setTitle(title);
            date.setDescription(description);
            date.setDate(selectedDate[0]);

            db.importantDateDao().updateDate(date);
            Log.d("Fragment", "Fecha actualizada: " + date.getTitle());

            // 🔹 Toast después de guardar correctamente
            ((TasksActivity) requireActivity())
                    .showCustomToast("Fecha actualizada correctamente", R.drawable.enamorado);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // 🔹 Diálogo para editar autor
    private void showEditAuthorDialog(ImportantDate date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar autor");

        EditText authorInput = new EditText(requireContext());
        authorInput.setText(date.getAuthor());

        builder.setView(authorInput);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newAuthor = authorInput.getText().toString().trim();
            if (!newAuthor.isEmpty()) {
                date.setAuthor(newAuthor);
                db.importantDateDao().updateDate(date);

                SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString("userName", newAuthor).apply();

                // 🔹 Actualizar todos los registros
                db.taskDao().updateAllAuthors(newAuthor);
                db.importantDateDao().updateAllAuthors(newAuthor);

                Log.d("Fragment", "Autor actualizado a: " + newAuthor);

                // 🔹 Toast después de guardar
                ((TasksActivity) requireActivity())
                        .showCustomToast("Autor actualizado correctamente", R.drawable.enamorado);
            } else {
                ((TasksActivity) requireActivity())
                        .showCustomToast("El autor no puede estar vacío", R.drawable.no);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
