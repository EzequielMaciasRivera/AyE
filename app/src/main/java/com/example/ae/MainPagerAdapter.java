package com.example.ae;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new PendingTasksFragment();
            case 1: return new CompletedTasksFragmet();
            case 2: return new ImportantDatesFragment();
            default: return new PendingTasksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // número de pestañas
    }
}
