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
    private DatabaseReference databaseReference;

    public ItemAdapter(List<Items> documentItemList, Context context) {
        this.documentItemList = documentItemList;
        this.context = context;
        this.databaseReference = databaseReference;

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
        Items item = documentItemList.get(position);


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

        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }

            // Create an Intent to open the ItemDetailActivity
            Intent intent = new Intent(context, ItemDetailActivity.class);

            // Pass the necessary data as extras in the Intent
            intent.putExtra("itemName", documentItem.getName());
            intent.putExtra("description", documentItem.getDescription());
            intent.putExtra("itemPrice", documentItem.getPrice());
            intent.putExtra("itemStoreName", documentItem.getStoreName());
            intent.putExtra("imageUrl", documentItem.getImageUrl());

            // Start the ItemDetailActivity
            context.startActivity(intent);
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
            String storeName = documentItem.getStoreName();

            // Implement your sharing logic here
            shareItem(itemName, itemDescription, itemPrice, storeName);
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

        // Set the initial checkbox state based on the item's status
        holder.checkbox.setChecked(documentItem.isStatus());
        // Add a click listener to the checkbox
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the item's status in the local data
            documentItem.setStatus(isChecked);

            updateItemStatusInCategory(item, "Books and Magazines");
            updateItemStatusInCategory(item, "Clothing");
            updateItemStatusInCategory(item, "Health");
            updateItemStatusInCategory(item, "Others");
            updateItemStatusInCategory(item, "Accessories");
            updateItemStatusInCategory(item, "Electronic");

        });
        }

    // Add a method to update the item's status in the Firebase Realtime Database
    private void updateItemStatusInCategory(Items item, String categoryName) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference categoryRef = databaseRef.child(categoryName);

        Query query = categoryRef.orderByChild("name").equalTo(item.getName());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        // Update the "status" field in the database
                        itemSnapshot.getRef().child("status").setValue(item.isStatus());
                    }
                    Toast.makeText(context, "Marked as Purchased ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to update status in " + categoryName, Toast.LENGTH_SHORT).show();
            }
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

    private void shareItem(String itemName, String itemDescription, String itemPrice, String storeName) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = "Check out this item for me:\nName: " + itemName + "\nDescription: " + itemDescription + "\nPrice: " + itemPrice + "\nStore Name: " + storeName;
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(shareIntent, "Share via"));
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
