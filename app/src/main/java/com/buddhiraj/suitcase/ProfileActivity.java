package com.buddhiraj.suitcase;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 123;
    private static final int CAMERA_REQUEST_CODE = 124;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 126;

    private ImageView profile;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.buttonBackground));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Your Profile");
        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        profile = findViewById(R.id.profileImage);
        progressBar = findViewById(R.id.progressBar); // Initialize the progress bar

        // Initialize Firebase references
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Get the currently logged-in user's ID
        String authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user's profileImageURL
        DatabaseReference profileImageRef = databaseRef.child("users").child(authUid).child("profileImageURL");

        // Listen for changes to the profileImageURL
        profileImageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.getValue(String.class);

                    // Show the progress bar while the image is being loaded
                    progressBar.setVisibility(View.VISIBLE);

                    // Load and display the image in the ImageView using Picasso
                    Picasso.get().load(imageUrl).into(profile, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Image loading is successful; hide the progress bar
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            // Handle any errors during image loading
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "Failed to load profile image.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });

        TextView changeProfileImageText = findViewById(R.id.changeProfileImage);
        TextView changeUsernameText = findViewById(R.id.changeUsername);

        // Initialize Firebase references
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Request camera and storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        changeProfileImageText.setOnClickListener(v -> {
            openGallery();
        });

        changeUsernameText.setOnClickListener(v -> {
            // Inflate the set_username.xml layout for the dialog
            View dialogView = getLayoutInflater().inflate(R.layout.set_user_name, null);

            // Create an AlertDialog for the username change dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView)
                    .setTitle("Change Username")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TextInputLayout usernameTextInputLayout = dialogView.findViewById(R.id.userName);
                            String newUsername = usernameTextInputLayout.getEditText().getText().toString().trim();

                            if (!TextUtils.isEmpty(newUsername)) {
                                // Update the username in the Firebase Realtime Database
                                String authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference userRef = databaseRef.child("users").child(authUid);

                                if (newUsername.equals("")) {
                                    userRef.child("username").removeValue(); // Remove username if new one is empty
                                } else {
                                    userRef.child("username").setValue(newUsername); // Set new username
                                }

                                Toast.makeText(ProfileActivity.this, "Username updated successfully! Restart app to see changes", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Username cannot be empty. No changes were made.", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.create().show();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted; you can now open the camera
                openCamera();
            } else {
                // Permission denied; handle it accordingly (e.g., show a message to the user)
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Handle gallery image selection
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                // Display the selected image in a dialog box
                showImageDialog(bitmap, imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Handle camera capture result
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Display the captured image in a dialog box
            showImageDialog(imageBitmap, null);
        }
    }

    private void showImageDialog(Bitmap imageBitmap, Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selected Image");

        ImageView imageView = new ImageView(this);
        if (imageBitmap != null) {
            // If the image is from the camera capture
            imageView.setImageBitmap(imageBitmap);
        } else if (imageUri != null) {
            // If the image is from the gallery selection
            imageView.setImageURI(imageUri);
        }
        builder.setView(imageView);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Save"
                if (imageUri != null) {
                    // Upload the selected image to Firebase Storage and update the image URL in the database
                    uploadImageToFirebaseStorage(imageUri);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Cancel"
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        if (imageUri != null) {
            String authUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            StorageReference imageRef = storageRef.child("profile_images").child(authUid + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageURL = uri.toString();
                            updateUserProfileImageURL(authUid, imageURL);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(ProfileActivity.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateUserProfileImageURL(String userId, String imageURL) {
        databaseRef.child("users").child(userId).child("profileImageURL").setValue(imageURL);
        Toast.makeText(ProfileActivity.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
