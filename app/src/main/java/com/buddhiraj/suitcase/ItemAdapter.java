    package com.buddhiraj.suitcase;

    import android.annotation.SuppressLint;
    import android.app.AlertDialog;
    import android.content.Context;
    import android.content.Intent;
    import android.net.Uri;
    import android.provider.ContactsContract;
    import android.provider.MediaStore;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.CheckBox;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.RecyclerView;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;
    import com.squareup.picasso.Picasso;

    import java.util.ArrayList;
    import java.util.List;

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        private static final int PICK_IMAGE_REQUEST = 1;
        final List<Items> documentItemList;
        private OnItemClickListener itemClickListener;
        final Context context;

        public ItemAdapter(List<Items> documentItemList, Context context) {
            this.documentItemList = documentItemList;
            this.context = context;
        }

        public void filterByName(String query) {
            query = query.toLowerCase(); // Convert the query to lowercase for case-insensitive search
            ArrayList<Items> filteredItems = new ArrayList<>();

            for (Items item : documentItemList) {
                String itemName = item.getName().toLowerCase();
                if (itemName.contains(query)) {
                    filteredItems.add(item);
                }
            }

            documentItemList.clear();
            documentItemList.addAll(filteredItems);
            notifyDataSetChanged();
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

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Items documentItem = documentItemList.get(position);
            Items item = documentItemList.get(position);
            holder.nameTextView.setText(documentItem.getName());
            holder.priceTextView.setText("Price: " + documentItem.getPrice());
            holder.descriptionTextView.setText("Description: " + documentItem.getDescription());
            Picasso.get().load(documentItem.getImageUrl()).into(holder.itemImageView);


            holder.progressBar.setVisibility(View.VISIBLE);

            Picasso.get().load(documentItem.getImageUrl()).into(holder.itemImageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    // Image loaded successfully, hide the ProgressBar
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    // Handle any errors that occur during image loading (optional)
                    // You can choose to leave the ProgressBar visible or handle errors differently.
                    holder.progressBar.setVisibility(View.GONE);
                }
            });

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

            holder.sendSMS.setOnClickListener(view -> {
                String itemName = documentItem.getName();
                String itemDescription = documentItem.getDescription();
                String itemPrice = documentItem.getPrice();
                String storeName = documentItem.getStoreName();

                // Implement your sharing logic here
                messageItem(itemName, itemDescription, itemPrice, storeName);
            });

            // Inside onBindViewHolder method
            holder.findInMapImageView.setOnClickListener(view -> {
                String storeName = documentItem.getStoreName();
                // Implement the logic to open a map with the store location based on the storeName.
                openMapWithStoreLocation(storeName);
            });


            // Check the status and set the appropriate checkbox image
            if (documentItem.isStatus()) {
                holder.checkBox.setVisibility(View.GONE);
                holder.purchased.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.purchased.setVisibility(View.GONE);
            }

            // Add a click listener to the checkbox to update its state and the database
            holder.checkBox.setOnClickListener(view -> {
                // Invert the status of the item
                documentItem.setStatus(!documentItem.isStatus());

                // Update the item's status in the database
                updateItemStatusInCategory(item, "Books and Magazines");
                updateItemStatusInCategory(item, "Clothing");
                updateItemStatusInCategory(item, "Health");
                updateItemStatusInCategory(item, "Others");
                updateItemStatusInCategory(item, "Accessories");
                updateItemStatusInCategory(item, "Electronic");

                // Check the status and set the appropriate checkbox image
                if (documentItem.isStatus()) {
                    holder.checkBox.setVisibility(View.GONE);
                    holder.purchased.setVisibility(View.VISIBLE);
                } else {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    holder.purchased.setVisibility(View.GONE);
                }
            });


            holder.purchased.setOnClickListener(view -> {
                // Invert the status of the item

                // Update the item's status in the database
                updateItemStatus(item, "Books and Magazines");
                updateItemStatus(item, "Clothing");
                updateItemStatus(item, "Health");
                updateItemStatus(item, "Others");
                updateItemStatus(item, "Accessories");
                updateItemStatus(item, "Electronic");

            });

//            // Set the initial checkbox state based on the item's status
//            holder.checkbox.setChecked(documentItem.isStatus());
//            // Add a click listener to the checkbox
//            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                // Update the item's status in the local data
//                documentItem.setStatus(isChecked);
//
//                updateItemStatusInCategory(item, "Books and Magazines");
//                updateItemStatusInCategory(item, "Clothing");
//                updateItemStatusInCategory(item, "Health");
//                updateItemStatusInCategory(item, "Others");
//                updateItemStatusInCategory(item, "Accessories");
//                updateItemStatusInCategory(item, "Electronic");
//
//            });

            // Inside onBindViewHolder method
            holder.editImageView.setOnClickListener(view -> {
                // Create an AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                // Inflate the custom layout (activity_edit.xml) for the dialog
                View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_edit, null);

                // Find and initialize the UI elements within the dialogView if needed
                EditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);
                EditText descriptionEditText = dialogView.findViewById(R.id.editTextDescription);
                EditText priceEditText = dialogView.findViewById(R.id.editTextPrice);
                EditText storeNameEditText = dialogView.findViewById(R.id.editTextStoreName);
                ImageView itemImageView = dialogView.findViewById(R.id.imageViewItem);
                TextView pickImage = dialogView.findViewById(R.id.btnChooseImage);

                // Populate the UI elements with data from the selected item
                itemNameEditText.setText(documentItem.getName());
                descriptionEditText.setText(documentItem.getDescription());
                priceEditText.setText(documentItem.getPrice());
                storeNameEditText.setText(documentItem.getStoreName());
                Picasso.get().load(documentItem.getImageUrl()).into(itemImageView);

                pickImage.setOnClickListener(v -> {
                    // Open an image picker or camera intent here
                    // For example, you can use startActivityForResult to open the image picker
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    if (context instanceof AppCompatActivity) {
                        ((AppCompatActivity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }
                });
                // Set the dialog's custom view
                builder.setView(dialogView);

                // Add positive and negative buttons (e.g., Save and Cancel)
                builder.setPositiveButton("Save", (dialog, which) -> {
                    // Retrieve updated data from the dialog's UI elements
                    String updatedItemName = itemNameEditText.getText().toString();
                    String updatedItemDescription = descriptionEditText.getText().toString();
                    String updatedItemPrice = priceEditText.getText().toString();
                    String updatedStoreName = storeNameEditText.getText().toString();

                    // Update the item data in the database
                    updateClothingInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName);
                    updateAccessoriesInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName);
                    updateBAMInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName);
                    updateHealthInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName);
                    updateElectronicInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName);
                    updateOthersInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName);
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle the cancel action if needed
                });

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            });

        }




        private void updateAccessoriesInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = databaseRef.child("Accessories"); // Replace with the appropriate category in your database

            Query query = categoryRef.orderByChild("name").equalTo(item.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            // Update the item's data in the database
                            itemSnapshot.getRef().child("name").setValue(updatedName);
                            itemSnapshot.getRef().child("description").setValue(updatedDescription);
                            itemSnapshot.getRef().child("price").setValue(updatedPrice);
                            itemSnapshot.getRef().child("storeName").setValue(updatedStoreName);
                        }
                        // Notify the user that the item has been updated
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updateBAMInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = databaseRef.child("Books and Magazines"); // Replace with the appropriate category in your database

            Query query = categoryRef.orderByChild("name").equalTo(item.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            // Update the item's data in the database
                            itemSnapshot.getRef().child("name").setValue(updatedName);
                            itemSnapshot.getRef().child("description").setValue(updatedDescription);
                            itemSnapshot.getRef().child("price").setValue(updatedPrice);
                            itemSnapshot.getRef().child("storeName").setValue(updatedStoreName);
                        }
                        // Notify the user that the item has been updated
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updateHealthInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = databaseRef.child("Health"); // Replace with the appropriate category in your database

            Query query = categoryRef.orderByChild("name").equalTo(item.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            // Update the item's data in the database
                            itemSnapshot.getRef().child("name").setValue(updatedName);
                            itemSnapshot.getRef().child("description").setValue(updatedDescription);
                            itemSnapshot.getRef().child("price").setValue(updatedPrice);
                            itemSnapshot.getRef().child("storeName").setValue(updatedStoreName);
                        }
                        // Notify the user that the item has been updated
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            });
        }
        private void updateElectronicInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = databaseRef.child("Electronic"); // Replace with the appropriate category in your database

            Query query = categoryRef.orderByChild("name").equalTo(item.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            // Update the item's data in the database
                            itemSnapshot.getRef().child("name").setValue(updatedName);
                            itemSnapshot.getRef().child("description").setValue(updatedDescription);
                            itemSnapshot.getRef().child("price").setValue(updatedPrice);
                            itemSnapshot.getRef().child("storeName").setValue(updatedStoreName);
                        }
                        // Notify the user that the item has been updated
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            });
        }
        private void updateOthersInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = databaseRef.child("Others"); // Replace with the appropriate category in your database

            Query query = categoryRef.orderByChild("name").equalTo(item.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            // Update the item's data in the database
                            itemSnapshot.getRef().child("name").setValue(updatedName);
                            itemSnapshot.getRef().child("description").setValue(updatedDescription);
                            itemSnapshot.getRef().child("price").setValue(updatedPrice);
                            itemSnapshot.getRef().child("storeName").setValue(updatedStoreName);
                        }
                        // Notify the user that the item has been updated
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            });
        }
        private void updateClothingInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = databaseRef.child("Clothing"); // Replace with the appropriate category in your database

            Query query = categoryRef.orderByChild("name").equalTo(item.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            // Update the item's data in the database
                            itemSnapshot.getRef().child("name").setValue(updatedName);
                            itemSnapshot.getRef().child("description").setValue(updatedDescription);
                            itemSnapshot.getRef().child("price").setValue(updatedPrice);
                            itemSnapshot.getRef().child("storeName").setValue(updatedStoreName);
                        }
                        // Notify the user that the item has been updated
                        Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            });
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
                            // Update the "status" field in the database to true
                            itemSnapshot.getRef().child("status").setValue(true);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update status in " + categoryName, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updateItemStatus(Items item, String categoryName) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoryRef = databaseRef.child(categoryName);

            Query query = categoryRef.orderByChild("name").equalTo(item.getName());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            // Update the "status" field in the database to true
                            itemSnapshot.getRef().child("status").setValue(false);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to update status in " + categoryName, Toast.LENGTH_SHORT).show();
                }
            });
        }


        @SuppressLint("QueryPermissionsNeeded")
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
                        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();}
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

        private void messageItem(String itemName, String itemDescription, String itemPrice, String storeName) {
            try {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", "Check out this item for me:\nName: " + itemName +
                        "\nDescription: " + itemDescription + "\nPrice: " + itemPrice +
                        "\nStore Name: " + storeName);
                context.startActivity(sendIntent);
            } catch (Exception e) {
                // Handle exceptions, e.g., if there's no SMS app available
                Toast.makeText(context, "Failed to open SMS app", Toast.LENGTH_SHORT).show();
            }
        }




        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView nameTextView;
            public TextView descriptionTextView;
            public TextView priceTextView;
            public TextView purchased;
            public ImageView itemImageView;
            public ImageView deleteImageView;
            public ImageView shareImageView;
            public ImageView sendSMS;
            public ImageView checkBox;
            public View editImageView;
            public View findInMapImageView;
            public ProgressBar progressBar;


            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                descriptionTextView = itemView.findViewById(R.id.productDescription);
                priceTextView = itemView.findViewById(R.id.productPrice);
                itemImageView = itemView.findViewById(R.id.itemImage);
                deleteImageView = itemView.findViewById(R.id.deleteItem);
                shareImageView = itemView.findViewById(R.id.shareItem);
                editImageView = itemView.findViewById(R.id.editItem);
                sendSMS = itemView.findViewById(R.id.messageItem);
                findInMapImageView = itemView.findViewById(R.id.findInMap);
                progressBar = itemView.findViewById(R.id.progressBar);
                purchased = itemView.findViewById(R.id.purchasedItem);
                checkBox = itemView.findViewById(R.id.checkbox);
            }
        }
    }
