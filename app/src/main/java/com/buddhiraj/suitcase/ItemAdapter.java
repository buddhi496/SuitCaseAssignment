package com.buddhiraj.suitcase;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private static final int PICK_IMAGE_REQUEST = 1;
    final List<Items> documentItemList;
    private OnItemClickListener itemClickListener;
    final Context context;
    private Uri selectedImageUri;

    public ImageView itemImageView;

    // Update the selected image URI
    public void setSelectedImageUri(Uri selectedImageUri) {
        this.selectedImageUri = selectedImageUri;
        Log.d("Edit_image", " Adapter Selected Image URI: " + selectedImageUri);
        Picasso.get().load(selectedImageUri).into(itemImageView);
        notifyDataSetChanged();
    }


    public ItemAdapter(List<Items> documentItemList, Context context) {
        this.documentItemList = documentItemList;
        this.context = context;
    }

    public void filterByName(String query) {
        query = query.toLowerCase();
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


        holder.progressBar.setVisibility(View.VISIBLE);

        Picasso.get().load(documentItem.getImageUrl()).into(holder.itemImageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
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

            Intent intent = new Intent(context, ItemDetailActivity.class);

            // Pass the necessary data as extras in the Intent
            intent.putExtra("itemName", documentItem.getName());
            intent.putExtra("description", documentItem.getDescription());
            intent.putExtra("itemPrice", documentItem.getPrice());
            intent.putExtra("itemStoreName", documentItem.getStoreName());
            intent.putExtra("imageUrl", documentItem.getImageUrl());

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

        holder.shareImageView.setOnClickListener(view -> {
            String itemName = documentItem.getName();
            String itemDescription = documentItem.getDescription();
            String itemPrice = documentItem.getPrice();
            String storeName = documentItem.getStoreName();

            shareItem(itemName, itemDescription, itemPrice, storeName);
        });

        holder.sendSMS.setOnClickListener(view -> {
            String itemName = documentItem.getName();
            String itemDescription = documentItem.getDescription();
            String itemPrice = documentItem.getPrice();
            String storeName = documentItem.getStoreName();

            messageItem(itemName, itemDescription, itemPrice, storeName);
        });

        holder.findInMapImageView.setOnClickListener(view -> {
            String storeName = documentItem.getStoreName();
            openMapWithStoreLocation(storeName);
        });

        if (documentItem.isStatus()) {
            holder.checkBox.setVisibility(View.GONE);
            holder.purchased.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.purchased.setVisibility(View.GONE);
        }

        holder.checkBox.setOnClickListener(view -> {
            documentItem.setStatus(!documentItem.isStatus());

            updateItemStatusInCategory(item, "Books and Magazines");
            updateItemStatusInCategory(item, "Clothing");
            updateItemStatusInCategory(item, "Health");
            updateItemStatusInCategory(item, "Others");
            updateItemStatusInCategory(item, "Accessories");
            updateItemStatusInCategory(item, "Electronic");

            if (documentItem.isStatus()) {
                holder.checkBox.setVisibility(View.GONE);
                holder.purchased.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.purchased.setVisibility(View.GONE);
            }
        });


        holder.purchased.setOnClickListener(view -> {

            updateItemStatus(item, "Books and Magazines");
            updateItemStatus(item, "Clothing");
            updateItemStatus(item, "Health");
            updateItemStatus(item, "Others");
            updateItemStatus(item, "Accessories");
            updateItemStatus(item, "Electronic");

        });

        holder.editImageView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_edit, null);

            EditText itemNameEditText = dialogView.findViewById(R.id.editTextItemName);
            EditText descriptionEditText = dialogView.findViewById(R.id.editTextDescription);
            EditText priceEditText = dialogView.findViewById(R.id.editTextPrice);
            EditText storeNameEditText = dialogView.findViewById(R.id.editTextStoreName);
            itemImageView = dialogView.findViewById(R.id.imageViewItem);
            TextView pickImage = dialogView.findViewById(R.id.btnChooseImage);

            itemNameEditText.setText(documentItem.getName());
            descriptionEditText.setText(documentItem.getDescription());
            priceEditText.setText(documentItem.getPrice());
            storeNameEditText.setText(documentItem.getStoreName());

            pickImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");

                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });

            // Update the itemImageView with the selected image
            if (selectedImageUri != null) {
                Picasso.get().load(selectedImageUri).into(itemImageView);
                Log.d("Edit_image","selected image uri not null");
            } else {
                // Load the existing image
                Picasso.get().load(documentItem.getImageUrl()).into(itemImageView);
                Log.d("Edit_image", "Selecte image uri null");
            }

            builder.setView(dialogView);

            builder.setPositiveButton("Save", (dialog, which) -> {
                String updatedItemName = itemNameEditText.getText().toString();
                String updatedItemDescription = descriptionEditText.getText().toString();
                String updatedItemPrice = priceEditText.getText().toString();
                String updatedStoreName = storeNameEditText.getText().toString();

                if (selectedImageUri != null) {
                    // A new image is selected, update the image URL with the new image
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("item_images/" + documentItem.getName() + ".jpg");

                    storageRef.putFile(selectedImageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                storageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            String updatedImageUrl = uri.toString();
                                            updateClothingInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, updatedImageUrl);
                                            updateAccessoriesInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, updatedImageUrl);
                                            updateOthersInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, updatedImageUrl);
                                            updateHealthInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, updatedImageUrl);
                                            updateBAMInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, updatedImageUrl);
                                            updateElectronicInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, updatedImageUrl);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle the failure to get the download URL
                                            Toast.makeText(context, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                // Handle the failure to upload the image
                                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // No new image selected, proceed with updating other item details with the existing image URL
                    updateClothingInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, documentItem.getImageUrl());
                    updateAccessoriesInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, documentItem.getImageUrl());
                    updateElectronicInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, documentItem.getImageUrl());
                    updateOthersInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, documentItem.getImageUrl());
                    updateHealthInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, documentItem.getImageUrl());
                    updateBAMInDatabase(documentItem, updatedItemName, updatedItemDescription, updatedItemPrice, updatedStoreName, documentItem.getImageUrl());
                }
            });


            builder.setNegativeButton("Cancel", (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }

    private void updateAccessoriesInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName, String updatedImageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference categoryRef = databaseRef.child("Accessories");

        Query query = categoryRef.orderByChild("name").equalTo(item.getName());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        itemSnapshot.getRef().child("name").setValue(updatedName);
                        itemSnapshot.getRef().child("description").setValue(updatedDescription);
                        itemSnapshot.getRef().child("price").setValue(updatedPrice);
                        itemSnapshot.getRef().child("storeName").setValue(updatedStoreName);
                        itemSnapshot.getRef().child("imageUrl").setValue(updatedImageUrl);
                    }
                    Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateBAMInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName, String updatedImageUrl) {
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
                        itemSnapshot.getRef().child("imageUrl").setValue(updatedImageUrl); // Update the image URL

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

    private void updateHealthInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName, String updatedImageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference categoryRef = databaseRef.child("Health");

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
                        itemSnapshot.getRef().child("imageUrl").setValue(updatedImageUrl); // Update the image URL

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
    private void updateElectronicInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName, String updatedImageUrl) {
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
                        itemSnapshot.getRef().child("imageUrl").setValue(updatedImageUrl); // Update the image URL

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
    private void updateOthersInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName, String updatedImageUrl) {
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
                        itemSnapshot.getRef().child("imageUrl").setValue(updatedImageUrl); // Update the image URL

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
    private void updateClothingInDatabase(Items item, String updatedName, String updatedDescription, String updatedPrice, String updatedStoreName, String updatedImageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference categoryRef = databaseRef.child("Clothing");

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

                        if (updatedImageUrl != null) {
                            itemSnapshot.getRef().child("imageUrl").setValue(updatedImageUrl);
                        }
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
