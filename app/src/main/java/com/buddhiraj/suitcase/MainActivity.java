package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.TextView;
import android.widget.Toast;

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

                if (itemId == R.id.menu_add_category) {
                    // Handle adding a category
                    showAddCategoryPopup(view); // Show the Add Category popup
                    return true;
                } else if (itemId == R.id.menu_add_item) {
                    // Handle adding an item
                    Toast.makeText(MainActivity.this, "Add Item clicked", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }
        });

        popupMenu.show();
    }

    private void showAddCategoryPopup(View view) { //for add category
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_category, null);

        EditText editCategoryName = popupView.findViewById(R.id.editCategoryName);
        // You can similarly find other views like ImageView and EditText for description

        Button addCategoryButton = popupView.findViewById(R.id.addCategoryButton);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = editCategoryName.getText().toString();
                // Get other input values from views like ImageView and EditText

                // Perform the logic to add the category to your database or wherever needed
                // You can also dismiss the popup after adding the category
                // popupWindow.dismiss();

                Toast.makeText(MainActivity.this, "Category added: " + categoryName, Toast.LENGTH_SHORT).show();
            }
        });

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }


    public void showPopupItems(View view) { //to add items in category
        View popupView = getLayoutInflater().inflate(R.layout.popup_items, null);

        // Create the pop-up window
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        // Find views in the pop-up layout
        LinearLayout itemListView = popupView.findViewById(R.id.itemListView);
        Button addItemsButton = popupView.findViewById(R.id.addItemsButton);

        LinearLayout itemDetailsLayout = popupView.findViewById(R.id.itemDetailsLayout);

        addItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the hidden layout
                itemDetailsLayout.setVisibility(View.VISIBLE);
                addItemsButton.setVisibility(View.INVISIBLE);
            }
        });
        // Show the pop-up
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

}


