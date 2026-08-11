package com.example.ae;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.ImportantDateDAO;
import com.example.ae.model.ImportantDate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImportantDatesAdapter extends RecyclerView.Adapter<ImportantDatesAdapter.DateViewHolder> {
    private List<ImportantDate> dateList;
    private ImportantDateDAO dateDao;

    public ImportantDatesAdapter(List<ImportantDate> dateList, ImportantDateDAO dateDao) {
        this.dateList = dateList;
        this.dateDao = dateDao;
    }

    public void setDates(List<ImportantDate> dates) {
        this.dateList = dates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_important_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        ImportantDate date = dateList.get(position);
        holder.title.setText(date.getTitle());
        holder.description.setText(date.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.date.setText(sdf.format(new Date(date.getDate())));
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;

        DateViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dateTitle);
            description = itemView.findViewById(R.id.dateDescription);
            date = itemView.findViewById(R.id.dateValue);
        }
    }
}
