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
            showEditDialog(position);
        } else {
            // Swiped left, show a confirmation dialog for deletion
            showDeleteConfirmationDialog(position);
        }
    }

    // Method to show a custom edit dialog
    @SuppressLint("NotifyDataSetChanged")
    private void showEditDialog(int position) {
        // Create a custom dialog view using your activity_edit.xml layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_edit, null);

        // Create an AlertDialog with your custom view
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Handle the save action here
                    // You can access views within your custom dialog view using dialogView.findViewById
                    // For example, EditText editText = dialogView.findViewById(R.id.editText);

                    // After editing, you can update the item in your adapter or database
                    // adapter.updateItem(position, editedItem);

                    // Notify the adapter to refresh the view
                    adapter.notifyDataSetChanged();

                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled, notify the adapter to refresh the view
                    adapter.notifyItemChanged(position);

                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .show();
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
                    adapter.deleteItem(position);
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
