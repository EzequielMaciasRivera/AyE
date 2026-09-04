package com.example.ae;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    // 🔹 Mantener instancias fijas de los fragments
    private final Fragment pendingFragment = new PendingTasksFragment();
    private final Fragment completedFragment = new CompletedTasksFragmet();
    private final Fragment datesFragment = new ImportantDatesFragment();

    public MainPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return pendingFragment;
            case 1: return completedFragment;
            case 2: return datesFragment;
            default: return pendingFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3; // número de pestañas
    }
}
