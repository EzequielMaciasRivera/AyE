package com.example.ae;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.AppDatabase;

import java.util.ArrayList;

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
        adapter = new ImportantDatesAdapter(new ArrayList<>(), db.importantDateDao());
        recyclerView.setAdapter(adapter);

        // Observa los datos en LiveData
        db.importantDateDao().getAllDates().observe(getViewLifecycleOwner(), dates -> {
            adapter.setDates(dates);
        });

        return view;
    }
}
