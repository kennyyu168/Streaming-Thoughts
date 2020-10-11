package com.example.streamingthoughts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private EditText mEmail, mPassword; // text fields for login info
    private Button mSignIn; // Button to sign up
    private FirebaseAuth mAuth; // used to check credentials

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialize the authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Get the button from the view
        mSignIn = findViewById(R.id.sign_in_button_auth);

        // Get listener for the sign in button
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sign the user in
                signInUser();
            }
        });
    }

    /**
     * Attempt to sign the user in from email and password entered.
     */
    private void signInUser() {
        // Get the edit texts entered by the user
        mEmail = findViewById(R.id.email_auth_act);
        mPassword = findViewById(R.id.password_auth_act);
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        // try to sign the user in
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Check if task is successful
                        if (task.isSuccessful()) {
                            // Sign in success, launch the main page
                            Log.d("SIGNED IN", "Successfully signed in");
                            startHomeActivity();
                        } else {
                            // Sign in failed, so user must know what's going on
                            Log.w("ERROR", "signInWithEmail:failure",
                                    task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Starts the main screen for the recordings with the signed in user.
     */
    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        this.startActivity(intent);
    }
}