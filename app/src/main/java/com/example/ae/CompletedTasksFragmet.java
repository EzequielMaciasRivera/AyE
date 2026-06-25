package com.example.ae;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;

import java.util.ArrayList;

public class CompletedTasksFragmet extends Fragment {

    private RecyclerView recyclerView;
    private CompletedTasksAdapter adapter;
    private TaskDao taskDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ⚠️ Revisa que el nombre del layout sea correcto: fragment_completed_tasks.xml
        View view = inflater.inflate(R.layout.fragment_completed_taks, container, false);

        recyclerView = view.findViewById(R.id.completedRecyclerView);

        // Usar siempre la instancia singleton de Room
        AppDatabase db = AppDatabase.getInstance(requireContext());
        taskDao = db.taskDao();

        // Inicializar adapter con lista vacía
        adapter = new CompletedTasksAdapter(new ArrayList<>(), taskDao);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Observar cambios en la base de datos (LiveData)
        taskDao.getCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks); // método en el adapter para actualizar la lista
        });

        return view;
    }
}
