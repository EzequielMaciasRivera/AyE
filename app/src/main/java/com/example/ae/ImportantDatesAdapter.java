package com.example.ae;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

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
        setHasStableIds(true); // ✅ IDs estables activados
    }

    @Override
    public long getItemId(int position) {
        return dateList.get(position).getId(); // ✅ usar el ID único de la entidad
    }

    // 🔹 Usar DiffUtil en lugar de notifyDataSetChanged
    public void setDates(List<ImportantDate> newDates) {
        List<ImportantDate> safeNewDates = (newDates != null) ? newDates : new ArrayList<>();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return dateList.size(); }

            @Override
            public int getNewListSize() { return safeNewDates.size(); }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return dateList.get(oldItemPosition).getId() ==
                        safeNewDates.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ImportantDate oldItem = dateList.get(oldItemPosition);
                ImportantDate newItem = safeNewDates.get(newItemPosition);
                return oldItem.getTitle().equals(newItem.getTitle())
                        && oldItem.getDescription().equals(newItem.getDescription())
                        && oldItem.getDate() == newItem.getDate()
                        && oldItem.getAuthor().equals(newItem.getAuthor());
            }
        });

        dateList = safeNewDates;
        diffResult.dispatchUpdatesTo(this);
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

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.dateValue.setText("Fecha: " + sdf.format(new Date(date.getDate())));
        holder.author.setText("Autor: " + date.getAuthor());

        holder.editButton.setOnClickListener(v -> listener.onEdit(date));
        holder.editAuthorButton.setOnClickListener(v -> listener.onEditAuthor(date));

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar fecha")
                    .setMessage("¿Seguro que deseas eliminar esta fecha importante?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        listener.onDelete(date);
                        showCustomToast(v.getContext(),
                                "Fecha eliminada correctamente",
                                R.drawable.sorpresa);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return dateList != null ? dateList.size() : 0;
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, dateValue, author;
        ImageButton editButton, editAuthorButton, deleteButton;

        DateViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dateTitle);
            description = itemView.findViewById(R.id.dateDescription);
            dateValue = itemView.findViewById(R.id.dateValue);
            author = itemView.findViewById(R.id.dateAuthor);
            editButton = itemView.findViewById(R.id.editButton);
            editAuthorButton = itemView.findViewById(R.id.editAuthorButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private void showCustomToast(android.content.Context context, String message, int iconRes) {
        View layout = LayoutInflater.from(context)
                .inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toastText);
        text.setText(message);

        ImageView icon = layout.findViewById(R.id.toastIcon);
        icon.setImageResource(iconRes);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
