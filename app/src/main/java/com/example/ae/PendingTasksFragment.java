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
import androidx.room.Room;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;

import java.util.ArrayList;
import java.util.List;


public class PendingTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskDao taskDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmet_pending_taks, container, false);

        recyclerView = view.findViewById(R.id.pendingRecyclerView);

        // Usar siempre la instancia singleton
        AppDatabase db = AppDatabase.getInstance(requireContext());
        taskDao = db.taskDao();

        adapter = new TaskAdapter(new ArrayList<>(), taskDao);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Observar cambios en la base de datos
        taskDao.getPendingTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks); // método en el adapter para actualizar la lista
        });

        return view;
    }
}