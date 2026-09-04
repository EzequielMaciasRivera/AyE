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
import androidx.lifecycle.Transformations;
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
        recyclerView.setItemAnimator(null); // ✅ eliminar animaciones

        db = AppDatabase.getInstance(getContext());
        Log.d("Fragment", "Registros actuales en la tabla: " + db.importantDateDao().getCount());

        adapter = new ImportantDatesAdapter(new ImportantDatesAdapter.OnDateClickListener() {
            @Override
            public void onEdit(ImportantDate date) { showEditDateDialog(date); }
            @Override
            public void onEditAuthor(ImportantDate date) { showEditAuthorDialog(date); }
            @Override
            public void onDelete(ImportantDate date) {
                db.importantDateDao().deleteDate(date);
                ((TasksActivity) requireActivity())
                        .showCustomToast("Fecha eliminada correctamente", R.drawable.sorpresa);
            }
        });

        recyclerView.setAdapter(adapter);

        // ✅ Observa los datos con distinctUntilChanged
        Transformations.distinctUntilChanged(
                db.importantDateDao().getAllDates()
        ).observe(getViewLifecycleOwner(), dates -> {
            adapter.setDates(dates);
            Log.d("Fragment", "Observer recibió " + dates.size() + " registros");
        });

        return view;
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
                ((TasksActivity) requireActivity())
                        .showCustomToast("El título no puede estar vacío", R.drawable.no);
                return;
            }

            date.setTitle(title);
            date.setDescription(description);
            date.setDate(selectedDate[0]);

            db.importantDateDao().updateDate(date);
            Log.d("Fragment", "Fecha actualizada: " + date.getTitle());

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

                db.taskDao().updateAllAuthors(newAuthor);
                db.importantDateDao().updateAllAuthors(newAuthor);

                Log.d("Fragment", "Autor actualizado a: " + newAuthor);

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

    public void showAddDateDialog() {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_date, null);
        EditText inputTitle = dialogView.findViewById(R.id.editTitle);
        EditText inputDesc = dialogView.findViewById(R.id.editDescription);
        Button dateButton = dialogView.findViewById(R.id.btnPickDate);

        // Fecha inicial
        final long[] selectedDate = {System.currentTimeMillis()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateButton.setText(sdf.format(new Date(selectedDate[0])));

        // Abrir calendario al pulsar el botón
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

        new AlertDialog.Builder(requireContext())
                .setTitle("Nueva fecha importante")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();

                    if (!title.isEmpty()) {
                        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                        String userName = prefs.getString("userName", "desconocido");

                        ImportantDate newDate = new ImportantDate();
                        newDate.setTitle(title);
                        newDate.setDescription(desc);
                        newDate.setDate(selectedDate[0]);
                        newDate.setAuthor(userName);

                        db.importantDateDao().insertDate(newDate);

                        // ✅ Llamar al método de la Activity para mostrar el Toast
                        ((TasksActivity) requireActivity()).showCustomToast("Fecha agregada por " + userName, R.drawable.enamorado);
                    } else {
                        ((TasksActivity) requireActivity()).showCustomToast("El título no puede estar vacío", R.drawable.no);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

}
