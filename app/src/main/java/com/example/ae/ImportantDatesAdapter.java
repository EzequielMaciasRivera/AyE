package com.example.ae;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ae.data.ImportantDateDAO;
import com.example.ae.model.ImportantDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImportantDatesAdapter extends RecyclerView.Adapter<ImportantDatesAdapter.DateViewHolder> {
    private List<ImportantDate> dateList = new ArrayList<>();
    private final OnDateClickListener listener;

    // Interfaz para manejar acciones desde el Fragment
    public interface OnDateClickListener {
        void onEdit(ImportantDate date);
        void onEditAuthor(ImportantDate date);
        void onDelete(ImportantDate date);
    }

    public ImportantDatesAdapter(OnDateClickListener listener) {
        this.listener = listener;
    }

    public void setDates(List<ImportantDate> dates) {
        this.dateList = dates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_important_dates, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        ImportantDate date = dateList.get(position);

        holder.title.setText(date.getTitle());
        holder.description.setText(date.getDescription());

        // Formatear fecha desde timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.dateValue.setText("Fecha: " + sdf.format(new Date(date.getDate())));

        // Acciones de botones
        holder.editButton.setOnClickListener(v -> listener.onEdit(date));
        holder.editAuthorButton.setOnClickListener(v -> listener.onEditAuthor(date));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(date));
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, dateValue;
        ImageButton editButton, editAuthorButton, deleteButton;

        DateViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dateTitle);
            description = itemView.findViewById(R.id.dateDescription);
            dateValue = itemView.findViewById(R.id.dateValue);
            editButton = itemView.findViewById(R.id.editButton);
            editAuthorButton = itemView.findViewById(R.id.editAuthorButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
