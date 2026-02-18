package com.megaproject.currencyconverter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CurrencyItem> items = new ArrayList<>();
    private String selectedCurrencyCode;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CurrencyItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<CurrencyItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    
    public void setSelectedCurrency(String code) {
        this.selectedCurrencyCode = code;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CurrencyItem.TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CurrencyItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvTitle.setText(item.getHeaderTitle());
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder vh = (ItemViewHolder) holder;
            vh.tvCode.setText(item.getCode());
            vh.tvName.setText(item.getName());
            
            Glide.with(vh.itemView.getContext())
                .load(item.getFlagUrl())
                .circleCrop()
                .into(vh.ivFlag);

            boolean isSelected = item.getCode().equalsIgnoreCase(selectedCurrencyCode);
            vh.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFlag, ivCheck;
        TextView tvCode, tvName;
        ItemViewHolder(View itemView) {
            super(itemView);
            ivFlag = itemView.findViewById(R.id.ivFlag);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
