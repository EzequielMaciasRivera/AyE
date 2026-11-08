package com.example.ae;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.AppDatabase;
import com.example.ae.data.TaskDao;
import com.example.ae.model.Task;
import com.example.ae.model.TasksViewModel;

public class PendingTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TasksViewModel tasksViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 🔹 Corrige el nombre del layout: debe ser fragment_pending_tasks.xml
        View view = inflater.inflate(R.layout.fragmet_pending_taks, container, false);

        recyclerView = view.findViewById(R.id.pendingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 🔹 Inicializar el ViewModel compartido con la Activity
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // 🔹 Inicializar el adapter con TaskDao y listener
        AppDatabase db = AppDatabase.getInstance(requireContext());
        TaskDao taskDao = db.taskDao();

        adapter = new TaskAdapter(taskDao, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onEdit(Task task) {
                // Aquí puedes abrir un diálogo para editar y luego llamar a tasksViewModel.update(task)
                tasksViewModel.update(task);
            }

            @Override
            public void onDelete(Task task) {
                tasksViewModel.delete(task); // 🔹 borra en Room + Firebase
                ((TasksActivity) requireActivity())
                        .showCustomToast("Tarea eliminada correctamente", R.drawable.sorpresa);
            }
        });

        recyclerView.setAdapter(adapter);

        // 🔹 Observar solo las tareas pendientes
        tasksViewModel.getPendingTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
        });

        // 🔹 Sincronizar con Firebase al iniciar
        tasksViewModel.syncFromFirebase();

        return view;
    }
}
