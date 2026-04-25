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

public class CompletedTasksFragmet extends Fragment {

    private RecyclerView recyclerView;
    private CompletedTasksAdapter adapter;
    private TaskDao taskDao;
    private List<Task> completedTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_taks, container, false);

        recyclerView = view.findViewById(R.id.completedRecyclerView);

        // 🔹 Usar siempre la instancia singleton
        AppDatabase db = AppDatabase.getInstance(requireContext());
        taskDao = db.taskDao();

        adapter = new CompletedTasksAdapter(completedTasks, taskDao);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Observar cambios en la base de datos
        taskDao.getCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            completedTasks.clear();
            completedTasks.addAll(tasks);
            adapter.notifyDataSetChanged();
        });

        return view;
    }
}