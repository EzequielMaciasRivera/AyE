package com.example.ae;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.AppDatabase;

import java.util.ArrayList;

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

        adapter = new ImportantDatesAdapter(new ArrayList<>(), db.importantDateDao());
        recyclerView.setAdapter(adapter);

// Observa los datos en LiveData
        db.importantDateDao().getAllDates().observe(this, dates -> {
            adapter.setDates(dates);
        });


        // Observa los datos en LiveData
        db.importantDateDao().getAllDates().observe(this, dates -> {
            adapter.setDates(dates);
        });
    }
}
