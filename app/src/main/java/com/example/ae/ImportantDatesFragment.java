package com.example.ae;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

        // ✅ Constructor del adapter con listener
        adapter = new ImportantDatesAdapter(new ImportantDatesAdapter.OnDateClickListener() {
            @Override
            public void onEdit(ImportantDate date) {
                Log.d("Fragment", "Editar: " + date.getTitle());
                showEditDateDialog(date);
            }

            @Override
            public void onEditAuthor(ImportantDate date) {
                Log.d("Fragment", "Editar autor: " + date.getTitle());
                showEditAuthorDialog(date);
            }

            @Override
            public void onDelete(ImportantDate date) {
                Log.d("Fragment", "Eliminar: " + date.getTitle());
                db.importantDateDao().deleteDate(date);
            }
        });

        recyclerView.setAdapter(adapter);

        // Observa los datos en LiveData
        db.importantDateDao().getAllDates().observe(getViewLifecycleOwner(), adapter::setDates);

        // 🔹 Ya no hay FAB aquí, el FAB global en TasksActivity se encarga de llamar a showAddDateDialog()

        return view;
    }

    // 🔹 Diálogo para agregar nueva fecha
    public void showAddDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nueva fecha importante");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_date, null);
        EditText titleInput = dialogView.findViewById(R.id.editTitle);
        EditText descriptionInput = dialogView.findViewById(R.id.editDescription);
        View dateButton = dialogView.findViewById(R.id.btnPickDate);

        final long[] selectedDate = {System.currentTimeMillis()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        ((android.widget.Button) dateButton).setText(sdf.format(new Date(selectedDate[0])));

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth);
                        selectedDate[0] = chosen.getTimeInMillis();
                        ((android.widget.Button) dateButton).setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            ImportantDate newDate = new ImportantDate();
            newDate.setTitle(titleInput.getText().toString());
            newDate.setDescription(descriptionInput.getText().toString());
            newDate.setDate(selectedDate[0]);

            AppDatabase.databaseWriteExecutor.execute(() -> db.importantDateDao().insertDate(newDate));
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // 🔹 Diálogo para editar fecha
    private void showEditDateDialog(ImportantDate date) {
        // similar a showAddDateDialog pero cargando datos existentes
    }

    // 🔹 Diálogo para editar autor
    private void showEditAuthorDialog(ImportantDate date) {
        // similar pero solo con un EditText para el autor
    }
}
