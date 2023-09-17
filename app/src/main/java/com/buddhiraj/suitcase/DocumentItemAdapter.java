package com.buddhiraj.suitcase;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DocumentItemAdapter extends RecyclerView.Adapter<DocumentItemAdapter.ViewHolder> {
    private List<DocumentItem> documentItemList;

    public DocumentItemAdapter(List<DocumentItem> documentItemList) {
        this.documentItemList = documentItemList;
    }

    public String getItemName(int position) {
        if (position >= 0 && position < documentItemList.size()) {
            return documentItemList.get(position).getName();
        }
        return null;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener itemClickListener;

    public DocumentItemAdapter(List<DocumentItem> documentItemList, OnItemClickListener itemClickListener) {
        this.documentItemList = documentItemList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DocumentItem documentItem = documentItemList.get(position);

        // Set the numbering (position + 1) and the name
        String itemNumber = String.valueOf(position + 1);
        holder.nameTextView.setText(itemNumber + ". " + documentItem.getName());

        // Set click listener for each item
        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentItemList.size();
    }

    public void deleteItem(int position) {
        documentItemList.remove(position);
        notifyItemRemoved(position);
        // Update numbering after an item is removed
        notifyItemRangeChanged(position, documentItemList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameTextView = itemView.findViewById(R.id.nameTextView);
        }
    }
}
