package com.buddhiraj.suitcase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        String itemNumber = String.valueOf(position + 1);
        holder.nameTextView.setText(itemNumber + ". " + documentItem.getName());

        holder.descriptionTextView.setText(documentItem.getDescription());
        holder.priceTextView.setText("Price: " + documentItem.getPrice());
        Picasso.get().load(documentItem.getImageUrl()).into(holder.itemImageView);

        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });

        holder.deleteImageView.setOnClickListener(view -> {
            String itemName = documentItem.getName();
            String category1 = "Books and Magazines";
            String category2 = "Clothing";
            String category3 = "Health";
            String category4 = "Accessories";
            String category5 = "Electronic";
            String category6 = "Others";
            showDeleteConfirmationDialog(itemName, position, category1, category2, category3, category4, category5, category6);
        });

        // Add click listener for the "shareItem" ImageView
        holder.shareImageView.setOnClickListener(view -> {
            String itemName = documentItem.getName();
            String itemDescription = documentItem.getDescription();
            String itemPrice = documentItem.getPrice();

            // Implement your sharing logic here
            shareItem(itemName, itemDescription, itemPrice);
        });


        // Inside onBindViewHolder method
        holder.editImageView.setOnClickListener(view -> {
            String itemId = documentItem.getName();
            Intent editIntent = new Intent(context, EditActivity.class);
            editIntent.putExtra("itemId", itemId);
            context.startActivity(editIntent);
        });

        // Inside onBindViewHolder method
        holder.findInMapImageView.setOnClickListener(view -> {
            String storeName = documentItem.getStoreName();
            // Implement the logic to open a map with the store location based on the storeName.
            openMapWithStoreLocation(storeName);
        });

        // Set the initial state of the checkbox based on the 'status' field in your database
        boolean isChecked = documentItem.isStatus();
        holder.checkbox.setChecked(isChecked);

        // Add an OnCheckedChangeListener to the checkbox
        holder.checkbox.setOnCheckedChangeListener((compoundButton, checked) -> {
            // Update the 'status' field in the database for the corresponding item
            updateStatusInDatabase(documentItem.getName(), checked);

            // Update the 'status' field in the item object to reflect the current state
            documentItem.setStatus(checked);

            // Notify the adapter that the dataset has changed
            notifyDataSetChanged();
        });
        }

    private void openMapWithStoreLocation(String storeName) {
        // Create an intent to open a mapping application
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(storeName));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            // Fallback: Open a web map in a browser
            Uri webMapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(storeName));
            Intent webMapIntent = new Intent(Intent.ACTION_VIEW, webMapUri);
            context.startActivity(webMapIntent);
        }

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
                    Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void shareItem(String itemName, String itemDescription, String itemPrice) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = "Check out this item:\nName: " + itemName + "\nDescription: " + itemDescription + "\nPrice: " + itemPrice;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void updateStatusInDatabase(String itemId, boolean isChecked) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference itemRef = databaseRef.child("Health").child(itemId);

        // Update the 'status' field in the database
        itemRef.child("status").setValue(isChecked)
                .addOnSuccessListener(aVoid -> {
                    // The status was successfully updated in the database
                    Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // There was an error updating the status
                    Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView descriptionTextView;
        public TextView priceTextView;
        public ImageView itemImageView;
        public ImageView deleteImageView;
        public ImageView shareImageView;
        public View editImageView;
        public CheckBox checkbox;
        public View findInMapImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.productDescription);
            priceTextView = itemView.findViewById(R.id.productPrice);
            itemImageView = itemView.findViewById(R.id.itemImage);
            deleteImageView = itemView.findViewById(R.id.deleteItem);
            shareImageView = itemView.findViewById(R.id.shareItem);
            editImageView = itemView.findViewById(R.id.editItem);
            findInMapImageView = itemView.findViewById(R.id.findInMap);
            checkbox = itemView.findViewById(R.id.checkbox);

        }
    }
}
