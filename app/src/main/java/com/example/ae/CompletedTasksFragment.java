package com.example.ae;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        // ✅ Nombre correcto del layout
        View view = inflater.inflate(R.layout.fragment_completed_taks, container, false);

        recyclerView = view.findViewById(R.id.completedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ✅ Inicializar el ViewModel compartido con la Activity
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // ✅ Inicializar el adapter con el ViewModel y listener
        adapter = new CompletedTasksAdapter(tasksViewModel, new CompletedTasksAdapter.OnTaskClickListener() {
            @Override
            public void onEdit(Task task) {
                tasksViewModel.update(task);
            }

            @Override
            public void onDelete(Task task) {
                tasksViewModel.delete(task);
                ((TasksActivity) requireActivity())
                        .showCustomToast("Tarea eliminada correctamente", R.drawable.sorpresa);
            }
        });

        recyclerView.setAdapter(adapter);

        // ✅ Observar solo las tareas completadas
        tasksViewModel.getCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {


            adapter.submitList(
                    tasks == null
                            ? null
                            : new java.util.ArrayList<>(tasks)
            );

            // 🔹 Sincronizar solo si la DB local está vacía

        });

        return view;
    }
}
