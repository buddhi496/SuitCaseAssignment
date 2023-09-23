package com.buddhiraj.suitcase;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Items> documentItemList;
    private OnItemClickListener itemClickListener;
    private Context context;

    public ItemAdapter(List<Items> documentItemList, Context context) {
        this.documentItemList = documentItemList;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    public String getItemName(int position) {
        if (position >= 0 && position < documentItemList.size()) {
            return documentItemList.get(position).getName();
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Items documentItem = documentItemList.get(position);

        // Set the numbering (position + 1) and the name
        String itemNumber = String.valueOf(position + 1);
        holder.nameTextView.setText(itemNumber + ". " + documentItem.getName());

        // Set description and price
        holder.descriptionTextView.setText(documentItem.getDescription());
        holder.priceTextView.setText("Price: " + documentItem.getPrice());
        Picasso.get().load(documentItem.getImageUrl()).into(holder.itemImageView);

        // Set click listener for each item
        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });

        // Set click listener for the delete ImageView
        holder.deleteImageView.setOnClickListener(view -> {
            String itemName = documentItem.getName();
            String category1 = "Books and Magazines"; // Set the first category name here
            String category2 = "Clothing"; // Set the second category name here
            showDeleteConfirmationDialog(itemName, position, category1, category2);
        });
    }

    @Override
    public int getItemCount() {
        return documentItemList.size();
    }

    public void deleteItem(int position) {
        documentItemList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, documentItemList.size());
    }

    private void showDeleteConfirmationDialog(String itemName, int position, String... categories) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    for (String category : categories) {
                        removeFromCategory(itemName, category);
                    }
                    deleteItem(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled, do nothing
                })
                .show();
    }

    private void removeFromCategory(String itemName, String category) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference categoryRef = databaseRef.child(category);

        Query query = categoryRef.orderByChild("name").equalTo(itemName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        itemSnapshot.getRef().removeValue();
                    }
                    // Item(s) removed successfully from the category
                    // You can add any necessary logic here
                } else {
                    // No matching item found in the category
                    // You can add error handling logic here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the query
                // You can add error handling logic here
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView descriptionTextView;
        public TextView priceTextView;
        public ImageView itemImageView;
        public ImageView deleteImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.productDescription);
            priceTextView = itemView.findViewById(R.id.productPrice);
            itemImageView = itemView.findViewById(R.id.itemImage);
            deleteImageView = itemView.findViewById(R.id.deleteItem);
        }
    }
}
