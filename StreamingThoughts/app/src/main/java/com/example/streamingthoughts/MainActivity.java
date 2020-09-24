package com.example.streamingthoughts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int PAUSE_TIME = 1500; // ms to stop before clicking again
    public static final int RC_SIGN_IN = 123; // code for Firebase Sign in
    public static final String UID = "uid"; // identifier for user in

    private Button mSignin; // Button for Sign in
    private Button mAnon; // Button for Anon Sign in
    private FirebaseAuth mAuth; // Auth for Firebase

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get Sign in buttons
        mSignin = findViewById(R.id.button_sign_in);
        mAnon = findViewById(R.id.button_anon);

        // Check current user
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // gets current user
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                // If there is a session, launch main activity
                if (firebaseUser != null) {
                    mSignin.setVisibility(View.GONE);
                    mAnon.setVisibility(View.GONE);
                    startSession(firebaseUser.getUid());
                }
            }
        });

        mSignin.setEnabled(true);
        mAnon.setEnabled(true);

        // show button, and add listener
        // mSignin.setVisibility(View.VISIBLE);
        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < PAUSE_TIME){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                // createSignInIntent();
                signUserIn();
            }
        });
        // mAnon.setVisibility(View.VISIBLE);
        mAnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < PAUSE_TIME){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                signInAnonymously();
            }
        });
    }

    /**
     * When no user, sign them in anonymously
     */
    private void signInAnonymously() {
        // Disable sign in button
        mSignin.setEnabled(false);
        mAnon.setEnabled(false);

        // Sign in anonymously
        mAuth.signInAnonymously().addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in successful, update UI with signed in user's info
                    Log.d("ANON USER", "signed in anonymously");
                    Log.d("USER ID ANON", FirebaseAuth.getInstance().getCurrentUser().getUid());
                } else {
                    Log.e("BAD SIGN IN", "BAD SIGN IN");
                    Toast.makeText(MainActivity.this,
                            "Anonymous Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Instead of using firebase Auth UI, sign in using own interface.
     */
    private void signUserIn() {
        Intent intent = new Intent(this, AuthActivity.class);
        this.startActivity(intent);
    }

    /**
     * Launches new intent to start the session.
     *
     * @param uid Uid to pass into the MainActivity
     */
    private void startSession(String uid) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(UID, uid);
        this.startActivity(intent);
    }

    /**
     * Sign in intent for signing into Firebase for authentication.
     */
    public void createSignInIntent() {
        // Disable button for sign in
        mSignin.setEnabled(false);
        mAnon.setEnabled(false);

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        // Create and launch sign-in intent
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), RC_SIGN_IN);
    }

    /**
     * When sign in flow is complete, do this
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check request code
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                startSession(user.getUid());
            } else {
                // failed somewhere
                Log.d("BAD SIGN_IN", "OH NO, BAD SIGN IN");
            }
        }
    }

    /**
     * On resume, set the buttons back to enabled.
     */
    @Override
    public void onResume() {
        super.onResume();
        mSignin.setEnabled(true);
        mAnon.setEnabled(true);
    }
}