package com.buddhiraj.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    EditText userNameTextView, userEmailTextView, userPasswordTextView, conformPasswordTextView;
    Button signUpButton;
    ProgressBar progressbar;
    FirebaseAuth mAuth;

    CheckBox termsAndCondition;

    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // Find the "Register Now" TextView
        TextView signInNowTextView = findViewById(R.id.signInNow);

        // Initialize the DatabaseReference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set a click listener for the "Register Now" TextView
        signInNowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the RegistrationActivity when the TextView is clicked
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // taking FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();


        // initialising all views through id defined above
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        userPasswordTextView = findViewById(R.id.userPassword);
        conformPasswordTextView = findViewById(R.id.conformPassword);
        signUpButton = findViewById(R.id.signUpButton);
        termsAndCondition = findViewById(R.id.termsAndCondition);
        progressbar = findViewById(R.id.progressbar);



        // Set on Click Listener on Registration button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        termsAndCondition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                signUpButton.setEnabled(isChecked);
            }
        });


    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6; // Password should be at least 6 characters
    }
    private void registerNewUser() {


        progressbar.setVisibility(View.VISIBLE); // show the visibility of progress bar to show loading

        // Take the value of two edit texts in Strings
        String email, password;
        email = userEmailTextView.getText().toString();
        password = userPasswordTextView.getText().toString();

        String confirmPassword = conformPasswordTextView.getText().toString();
        String name = userNameTextView.getText().toString();

        if (TextUtils.isEmpty(name)) { //take users name
            Toast.makeText(getApplicationContext(),
                            "Please enter your name.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (!isEmailValid(email)) { //check for valid email
            Toast.makeText(getApplicationContext(),
                            "Invalid email format.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (!isPasswordValid(password)) { // check is password consider 6 character
            Toast.makeText(getApplicationContext(),
                            "Password must be at least 6 characters.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Validations for input email and password
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter email!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (!termsAndCondition.isChecked()) {
            Toast.makeText(getApplicationContext(),
                    "Please accept the terms and services.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter password!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(),
                            "Passwords do not match.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        progressbar.setVisibility(View.GONE);
        // create new user or register new user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Get the UID of the newly created user
                            String userId = mAuth.getCurrentUser().getUid();

                            // Save username and email to the database
                            String name = userNameTextView.getText().toString();
                            User user = new User(name, email); // Create User instance
                            databaseReference.child("users").child(userId).setValue(user);

                            Toast.makeText(getApplicationContext(),
                                            "Registration successful!",
                                            Toast.LENGTH_LONG)
                                    .show();

                            // if the user created intent to login activity
                            Intent intent
                                    = new Intent(RegistrationActivity.this,
                                    LoginActivity.class);
                            startActivity(intent);
                        } else {

                            // Registration failed
                            Toast.makeText(
                                            getApplicationContext(),
                                            "Registration failed!!"
                                                    + " Please try again later",
                                            Toast.LENGTH_LONG)
                                    .show();

                        }
                    }
                }
                );
    }
}