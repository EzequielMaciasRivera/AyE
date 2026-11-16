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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.model.ImportantDate;
import com.example.ae.model.ImportantDatesViewModel;
import com.example.ae.model.TasksViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ImportantDatesFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImportantDatesAdapter adapter;
    private ImportantDatesViewModel datesViewModel;
    private TasksViewModel tasksViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_important_dates, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Inicializar ViewModel
        datesViewModel = new ViewModelProvider(requireActivity()).get(ImportantDatesViewModel.class);
        tasksViewModel =
                new ViewModelProvider(requireActivity())
                        .get(TasksViewModel.class);

        // ✅ Adapter con listener
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
                datesViewModel.delete(date); // 🔹 borra en Room + Firebase
                ((TasksActivity) requireActivity())
                        .showCustomToast("Fecha eliminada correctamente", R.drawable.sorpresa);
            }
        });

        recyclerView.setAdapter(adapter);

        // ✅ Observar LiveData desde el ViewModel
        datesViewModel.getAllDates().observe(getViewLifecycleOwner(), dates -> {

            Log.d(
                    "DATES_DEBUG",
                    "Observer recibió "
                            + dates.size()
                            + " registros"
            );

            if (!dates.isEmpty()) {

                Log.d(
                        "DATES_DEBUG",
                        "Primer elemento: "
                                + dates.get(0).getTitle()
                                + " | "
                                + dates.get(0).getFirebaseId()
                );
            }

            adapter.setDates(
                    dates == null
                            ? new java.util.ArrayList<>()
                            : new java.util.ArrayList<>(dates)
            );

        });


        // ✅ Sincronizar con Firebase al iniciar
        datesViewModel.syncFromFirebase();

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
            newDate.setDescription(
                    descriptionInput.getText().toString().trim()
            );
            newDate.setDate(selectedDate[0]);

            SharedPreferences prefs =
                    requireContext().getSharedPreferences(
                            "AppPrefs",
                            Context.MODE_PRIVATE
                    );

            String userName =
                    prefs.getString(
                            "userName",
                            "desconocido"
                    );

            String currentOwnerId =
                    prefs.getString(
                            "ownerId",
                            ""
                    );

            newDate.setCreatedBy(
                    userName
            );

            Log.d(
                    "PREFS_OWNER",
                    "ownerId prefs = " +
                            prefs.getString(
                                    "ownerId",
                                    "NO_EXISTE"
                            )
            );

            newDate.setOwnerId(
                    currentOwnerId
            );
            Log.d(
                    "DATE_BEFORE_INSERT",
                    "ownerId = " + newDate.getOwnerId()
            );
            datesViewModel.insert(newDate); // 🔹 guarda en Room + Firebase

            ((TasksActivity) requireActivity())
                    .showCustomToast("Fecha agregada por " + userName, R.drawable.enamorado);

            dialog.dismiss();
        });
    }

    // 🔹 Diálogo para editar fecha
    private void showEditDateDialog(ImportantDate date) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(requireContext());

        builder.setTitle("Editar fecha importante");

        View dialogView =
                LayoutInflater.from(getContext())
                        .inflate(
                                R.layout.dialog_add_date,
                                null
                        );

        EditText titleInput =
                dialogView.findViewById(R.id.editTitle);

        EditText descriptionInput =
                dialogView.findViewById(R.id.editDescription);

        Button dateButton =
                dialogView.findViewById(R.id.btnPickDate);

        titleInput.setText(date.getTitle());
        descriptionInput.setText(date.getDescription());

        final long[] selectedDate = {date.getDate()};

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        dateButton.setText(
                sdf.format(
                        new Date(selectedDate[0])
                )
        );

        dateButton.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker =
                    new DatePickerDialog(
                            requireContext(),
                            (view, year, month, dayOfMonth) -> {

                                Calendar chosen =
                                        Calendar.getInstance();

                                chosen.set(
                                        year,
                                        month,
                                        dayOfMonth
                                );

                                selectedDate[0] =
                                        chosen.getTimeInMillis();

                                dateButton.setText(
                                        dayOfMonth + "/"
                                                + (month + 1)
                                                + "/"
                                                + year
                                );
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            datePicker.show();
        });

        builder.setView(dialogView);

        builder.setPositiveButton(
                "Guardar",
                (dialog, which) -> {

                    String title =
                            titleInput.getText()
                                    .toString()
                                    .trim();

                    String description =
                            descriptionInput.getText()
                                    .toString()
                                    .trim();

                    if (title.isEmpty()) {

                        ((TasksActivity) requireActivity())
                                .showCustomToast(
                                        "El título no puede estar vacío",
                                        R.drawable.no
                                );

                        return;
                    }

                    ImportantDate updatedDate =
                            new ImportantDate(date);

                    updatedDate.setTitle(title);
                    updatedDate.setDescription(description);
                    updatedDate.setDate(selectedDate[0]);

                    datesViewModel.update(updatedDate);

                    ((TasksActivity) requireActivity())
                            .showCustomToast(
                                    "Fecha actualizada correctamente",
                                    R.drawable.enamorado
                            );
                });

        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }

    // 🔹 Diálogo para editar autor
    // 🔹 Diálogo para editar autor
    private void showEditAuthorDialog(ImportantDate date) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(requireContext());

        builder.setTitle("Editar autor");

        EditText authorInput =
                new EditText(requireContext());

        authorInput.setText(date.getCreatedBy());

        builder.setView(authorInput);

        builder.setPositiveButton(
                "Guardar",
                (dialog, which) -> {

                    String newAuthor =
                            authorInput.getText()
                                    .toString()
                                    .trim();

                    if (!newAuthor.isEmpty()) {

                        SharedPreferences prefs =
                                requireContext()
                                        .getSharedPreferences(
                                                "AppPrefs",
                                                Context.MODE_PRIVATE
                                        );

                        String currentOwnerId =
                                prefs.getString(
                                        "ownerId",
                                        ""
                                );

                        datesViewModel.updateAuthorForAllDates(
                                currentOwnerId,
                                newAuthor
                        );

                        tasksViewModel.updateAuthorForAllTasks(
                                currentOwnerId,
                                newAuthor
                        );

                        prefs.edit()
                                .putString(
                                        "userName",
                                        newAuthor
                                )
                                .apply();

                        ((TasksActivity) requireActivity())
                                .showCustomToast(
                                        "Autor actualizado correctamente",
                                        R.drawable.enamorado
                                );

                    } else {

                        ((TasksActivity) requireActivity())
                                .showCustomToast(
                                        "El autor no puede estar vacío",
                                        R.drawable.no
                                );
                    }
                });

        builder.setNegativeButton(
                "Cancelar",
                null
        );

        builder.show();
    }
}
