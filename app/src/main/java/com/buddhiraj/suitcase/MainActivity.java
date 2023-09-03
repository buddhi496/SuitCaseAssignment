package com.buddhiraj.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView welcomeText;
    private DatabaseReference databaseReference;
    private ImageView imageView;
    private EditText editItemName, editPrice, editItemDetails, editStore;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        profileImage = findViewById(R.id.profileImage);
        welcomeText = findViewById(R.id.welcomeText);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            retrieveUserNameFromDatabase(currentUser.getUid());
        }
    }
    private void retrieveUserNameFromDatabase(String userId) { //fetchUserName
        DatabaseReference userRef = databaseReference.child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("username").getValue(String.class);
                    if (userName != null) {
                        String welcomeMessage = "Hi,\n " + userName;
                        // Apply different color to the username part
                        SpannableString spannableString = new SpannableString(welcomeMessage);
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.buttonBackground));
                        int startIndex = welcomeMessage.indexOf(userName);
                        int endIndex = startIndex + userName.length();
                        spannableString.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        welcomeText.setText(spannableString);
                    } else {
                        welcomeText.setText("Welcome, User");
                    }
                } else {
                    welcomeText.setText("Welcome, User");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error if data retrieval is cancelled
                welcomeText.setText("Welcome, User");
                Log.e("Database", "Data retrieval cancelled: " + databaseError.getMessage());
            }
        });

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu(view);
            }
        });

    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_your_profile) {
                    openProfileActivity(); // Open "Your Profile" activity
                    return true;
                } else if (itemId == R.id.menu_log_out) {
                    logoutUser(); // Log out
                    return true;
                }

                return false;
            }

            private void openProfileActivity() {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }

        });

        popupMenu.show();
    }

    private void PopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_add_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menu_add_item) {
                    // Handle adding an item
                    Intent addItemIntent = new Intent(MainActivity.this, AddItemActivity.class);

                    // Start the AddItemCategory activity
                    startActivity(addItemIntent);

                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }


    public void showDocuments(View view) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Documents");

        // Listen for changes in the "Documents" node
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<DocumentItem> documentItems = new ArrayList<>();

                    for (DataSnapshot documentSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve data from each document in the "Documents" node
                        int image = documentSnapshot.child("image").getValue(Integer.class);
                        String name = documentSnapshot.child("name").getValue(String.class);
                        String price = documentSnapshot.child("price").getValue(String.class);
                        String description = documentSnapshot.child("description").getValue(String.class);
                        String storeName = documentSnapshot.child("storeName").getValue(String.class);

                        // Create a DocumentItem object and add it to the list
                        DocumentItem documentItem = new DocumentItem(image, name, price, description, storeName);
                        documentItems.add(documentItem);
                    }

                    // Create a custom adapter
                    DocumentAdapter adapter = new DocumentAdapter(MainActivity.this, documentItems);

                    // Create a dialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Documents")
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Handle item click here (e.g., open document details, etc.)
                                    DocumentItem selectedDocument = documentItems.get(which);
                                    // Access selectedDocument.getImage(), selectedDocument.getName(), etc.
                                }
                            });

                    // Create and show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // Handle the case when there are no documents in the database
                    Toast.makeText(MainActivity.this, "No documents found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if data retrieval is canceled
                Toast.makeText(MainActivity.this, "Data retrieval canceled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


