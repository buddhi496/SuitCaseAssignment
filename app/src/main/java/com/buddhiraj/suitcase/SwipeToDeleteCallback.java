package com.buddhiraj.suitcase;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    final ItemAdapter adapter;
    final Context context;

    public SwipeToDeleteCallback(Context context, ItemAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // Not needed for swipe-to-delete, returning false
        return false;
    }

    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Get the position of the swiped item
        final int position = viewHolder.getAdapterPosition();

        if (direction == ItemTouchHelper.RIGHT) {
            // Swiped right, show a custom edit dialog
            showMarkAsPurchasedConfirmationDialog(position);
        } else {
            // Swiped left, show a confirmation dialog for deletion
            showDeleteConfirmationDialog(position);
        }
    }

    private void showMarkAsPurchasedConfirmationDialog(final int position) {
        // Show a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Do you want to mark this item as purchased?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User confirmed marking the item as purchased, update the item status and database
                    updateItemStatusAndDatabase(position, true);
                    adapter.notifyItemChanged(position);

                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User canceled, notify the adapter to refresh the view
                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    // Dialog canceled, notify the adapter to refresh the view
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    private void updateItemStatusAndDatabase(int position, boolean isPurchased) {
        Items itemToUpdate = adapter.documentItemList.get(position);
        itemToUpdate.setStatus(isPurchased); // Update the local data

        // Call the method to update the item's status in the database for the appropriate category
        String categoryName = getCategoryNameForItem(itemToUpdate);
        if (categoryName != null) {
            updateItemStatusInCategory(itemToUpdate, "Clothing");
            updateItemStatusInCategory(itemToUpdate, "Books and Magazines");
            updateItemStatusInCategory(itemToUpdate, "Health");
            updateItemStatusInCategory(itemToUpdate, "Accessories");
            updateItemStatusInCategory(itemToUpdate, "Electronic");
            updateItemStatusInCategory(itemToUpdate, "Others");
        }
    }

    private String getCategoryNameForItem(Items item) {
        String itemName = item.getName();

        // Add your logic to determine the category based on item name or any other criteria
        if (itemName.toLowerCase().contains("books") || itemName.toLowerCase().contains("magazines")) {
            return "Books and Magazines";
        } else if (itemName.toLowerCase().contains("clothing")) {
            return "Clothing";
        } else if (itemName.toLowerCase().contains("health")) {
            return "Health";
        } else if (itemName.toLowerCase().contains("accessories")) {
            return "Accessories";
        } else if (itemName.toLowerCase().contains("electronics")) {
            return "Electronic";
        } else {
            return "Others"; // Default category if no specific match is found
        }
    }

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
                    Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to update status in " + categoryName, Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Method to show a confirmation dialog for deletion
    private void showDeleteConfirmationDialog(final int position) {
        // Get the item name from your adapter
        String itemName = adapter.getItemName(position);

        // Show a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // User confirmed deletion, remove the item from the dataset and the database
                    removeFromDatabase(itemName); // Call the method to remove the item from the database
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled, notify the adapter to refresh the view
                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    // Dialog canceled, notify the adapter to refresh the view
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    // Method to remove the item from the database
    private void removeFromDatabase(String itemName) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot itemSnapshot : categorySnapshot.getChildren()) {
                        // Iterate through items in all categories
                        String itemNameInDatabase = itemSnapshot.child("name").getValue(String.class);

                        if (itemNameInDatabase != null && itemNameInDatabase.equals(itemName)) {
                            // Item name matches, delete the item
                            itemSnapshot.getRef().removeValue();
                        }
                    }
                }

                // All items with the matching name have been removed
                Toast.makeText(context, "Items removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the operation
                Toast.makeText(context, "Failed to remove items from the database.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
