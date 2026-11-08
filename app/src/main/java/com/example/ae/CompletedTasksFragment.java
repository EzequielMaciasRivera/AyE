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

public class CompletedTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private CompletedTasksAdapter adapter;
    private TasksViewModel tasksViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 🔹 Asegúrate que el layout se llame fragment_completed_tasks.xml
        View view = inflater.inflate(R.layout.fragment_completed_taks, container, false);

        recyclerView = view.findViewById(R.id.completedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 🔹 Obtener instancia de Room
        AppDatabase db = AppDatabase.getInstance(requireContext());
        TaskDao taskDao = db.taskDao();

        // 🔹 Inicializar el adapter con TaskDao y listener
        adapter = new CompletedTasksAdapter(taskDao, new CompletedTasksAdapter.OnTaskClickListener() {
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

        // 🔹 Usar el ViewModel compartido con la Activity
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // 🔹 Observar solo las tareas completadas
        tasksViewModel.getCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
        });

        // 🔹 Sincronizar con Firebase al iniciar
        tasksViewModel.syncFromFirebase();

        return view;
    }
}
