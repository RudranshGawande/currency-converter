package com.megaproject.currencyconverter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        
        String fromText = String.format("%.2f %s", item.fromAmount, item.fromCode);
        String toText = String.format("%.2f %s", item.toAmount, item.toCode);
        
        holder.tvConversionPair.setText(fromText + " → " + toText);
        holder.tvDate.setText(item.date);
        
        // Simple manual coloring logic for icons based on position to match UI screenshot approximately
        int colorRes;
        switch (position % 3) {
            case 0: colorRes = R.color.success_text; break; // Greenish
            case 1: colorRes = R.color.primary; break;      // Blueish
            default: colorRes = R.color.deep_violet; break; // Purplish
        }
        holder.ivIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), colorRes));
        
        holder.btnDeleteHistory.setOnClickListener(v -> {
            historyList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, historyList.size());
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConversionPair, tvDate;
        ImageView ivIcon, btnDeleteHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConversionPair = itemView.findViewById(R.id.tvConversionPair);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            btnDeleteHistory = itemView.findViewById(R.id.btnDeleteHistory);
        }
    }
}
