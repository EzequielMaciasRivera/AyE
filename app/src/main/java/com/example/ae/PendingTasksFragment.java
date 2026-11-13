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
        // ✅ Nombre correcto del layout
        View view = inflater.inflate(R.layout.fragmet_pending_taks,container, false);

        recyclerView = view.findViewById(R.id.pendingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        // ✅ Inicializar el adapter con TaskDao y listener
        // ✅ Inicializa el ViewModel primero
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

// ✅ Pasa el ViewModel al adapter
        adapter = new TaskAdapter(tasksViewModel, new TaskAdapter.OnTaskClickListener() {
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

        // ✅ Observar solo las tareas pendientes
        tasksViewModel.getPendingTasks().observe(
                getViewLifecycleOwner(),
                tasks -> {

                    Log.d(
                            "ROOM_PENDING_DEBUG",
                            "Pendientes recibidas = "
                                    + tasks.size()
                    );

                    adapter.submitList(
                            tasks == null
                                    ? null
                                    : new java.util.ArrayList<>(tasks)
                    );
                }
        );

        return view;
    }
}
