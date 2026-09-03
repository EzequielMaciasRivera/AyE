package com.example.ae;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.AppDatabase;
import com.example.ae.model.ImportantDate;

public class ImportantDatesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImportantDatesAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important_dates);

        recyclerView = findViewById(R.id.recyclerViewDates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        // ✅ Nuevo constructor del adapter con listener
        adapter = new ImportantDatesAdapter(new ImportantDatesAdapter.OnDateClickListener() {
            @Override
            public void onEdit(ImportantDate date) {
                Log.d("Activity", "Editar: " + date.getTitle());
            }

            @Override
            public void onEditAuthor(ImportantDate date) {
                Log.d("Activity", "Editar autor: " + date.getTitle());
            }

            @Override
            public void onDelete(ImportantDate date) {
                Log.d("Activity", "Eliminar: " + date.getTitle());
                db.importantDateDao().deleteDate(date);
            }
        });

        recyclerView.setAdapter(adapter);

        // Observa los datos en LiveData
        db.importantDateDao().getAllDates().observe(this, adapter::setDates);
    }
}
