package com.buddhiraj.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {


    EditText emailTextView, passwordTextView, forgotPasswordTextView;
    Button loginButton;
    ProgressBar divider;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Find the "Register Now" TextView
        TextView registerNowTextView = findViewById(R.id.registerNow);

        // Set a click listener for the "Register Now" TextView
        registerNowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the RegistrationActivity when the TextView is clicked
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
        // taking FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // initialising all views through id defined above
        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.password);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.loginButton);
        divider = findViewById(R.id.divider);

        // Set on Click Listener on Registration button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });

    }
        private void loginUserAccount()
        {

            // Take the value of two edit texts in Strings
            String email, password;
            email = emailTextView.getText().toString();
            password = passwordTextView.getText().toString();

            // validations for input email and password
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(),
                                "Please enter email!!",
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(),
                                "Please enter password!!",
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // signin existing user
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(
                                        @NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),
                                                        "Login successful!!",
                                                        Toast.LENGTH_LONG)
                                                .show();


                                        // if sign-in is successful
                                        // intent to home activity
                                        Intent intent
                                                = new Intent(LoginActivity.this,
                                                MainActivity.class);
                                        startActivity(intent);
                                    } else {

                                        // sign-in failed
                                        Toast.makeText(getApplicationContext(),
                                                        "Email or Password doesn't match!!",
                                                        Toast.LENGTH_LONG)
                                                .show();

                                    }
                                }
                            });
        }
    }