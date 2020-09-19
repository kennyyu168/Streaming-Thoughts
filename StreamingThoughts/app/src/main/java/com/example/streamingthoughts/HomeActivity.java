package com.example.streamingthoughts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.streamingthoughts.RecordingContent.loadSavedRecordings;
import static com.example.streamingthoughts.RecordingContent.removeItem;
import static com.example.streamingthoughts.SoftKeyboard.hideSoftKeyboard;

public class HomeActivity extends AppCompatActivity
        implements ListOfRecordings.OnListFragmentInteractionListener {

    private FloatingActionButton mFloatingActionButton; // Floating button for recording
    private FloatingActionButton mBackButton; // Floating button for going back
    private boolean isRecordingFragmentDisplayed = false; // Checks for fragment displaying
    private boolean isRenamingFragmentDisplayed = false;

    private RecyclerView recyclerView; // recycler view to display the files
    private RecyclerView.Adapter mAdapter; // attach an adapter for data to be displayed

    private FirebaseAnalytics mAnalytics; // analytics variable for firebase
    private FirebaseDatabase mDatabase; // database for files to be stored into
    private DatabaseReference myRef; // a reference to the location to write to
    private String uid; // user id for specific user
    private FirebaseUser mUser; // user for firebase

    private View fadeBackground;

    public static final String EXTRA_RECORDING = "com.example.streamingthoughts.RECORDING";
    public static final String EXTRA_DATE = "com.example.streamingthoughts.DATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // adds toolbar to the top of the app
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // get UID
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the recycler view adapter is not there
        if (mAdapter == null) {
            // Get fragment and RecyclerView from the layout and connect them
            Fragment currentFragment =
                    getSupportFragmentManager().findFragmentById(R.id.recording_list);
            recyclerView = (RecyclerView) currentFragment.getView();
            mAdapter = ((RecyclerView) currentFragment.getView()).getAdapter();
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(recyclerView.getContext(),
                            DividerItemDecoration.VERTICAL));
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        }

        // Get the analytics instance
        mAnalytics = FirebaseAnalytics.getInstance(this);

        // Get the database and write a message to it
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();
        myRef.setValue("Hello!");

        // Get the background fade view
        fadeBackground = findViewById(R.id.fadeBackground);

        // Get the button from the view
        mFloatingActionButton = findViewById(R.id.recording_button);

        // Get back button from view
        mBackButton = findViewById(R.id.close_button);
        mBackButton.setVisibility(GONE);

        // Set the click listener for the button
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecordingFragmentDisplayed) {
                    if (checkNumRecordings()) {
                        Toast.makeText(HomeActivity.this,
                                "Please make an account to record more",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(HomeActivity.this, AnonAuthActivity.class);
                        HomeActivity.this.startActivity(intent);
                    } else {
                        displayFragment();
                        mAnalytics.logEvent("record", null);
                    }
                }
            }
        });

        // Set click listener for back button
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecordingFragmentDisplayed) {
                    closeFragment();
                } else if (isRenamingFragmentDisplayed) {
                    // close that fragment
                    closeRenameFragment();
                }
            }
        });
    }

    /**
     * If no email, print this.
     */
    public void noEmailPrint() {
        // Check if it's an anonymous user
        if (mUser.getEmail() == null || mUser.getEmail().equals("")) {
            Toast.makeText(this, "You have " + (3 - mAdapter.getItemCount())
                    + " recordings left", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks for number of recordings.
     */
    public boolean checkNumRecordings() {
        return (mAdapter.getItemCount() > 2) &&
                (mUser.getEmail() == null || mUser.getEmail().equals(""));
    }

    /**
     * Notify dataset has changed.
     */
    public void uploadNow() {
        loadSavedRecordings(uid, new OnRetrieveListListener() {
            @Override
            public void onListComplete(String response) {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Displays the fragment using a FragmentManager.
     */
    public void displayFragment() {
            // call the function to check for recordings
            noEmailPrint();

            // Create a new recording fragment
            RecordingFragment recordingFragment = RecordingFragment.newInstance(mAdapter.getItemCount());

            // Get FragmentManager and start a transaction to add the fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Add the RecordingFragment
            fragmentTransaction.add(R.id.fragment_container,
                    recordingFragment).addToBackStack(null).commit();

            // Set boolean flag to indicate fragment is open
            isRecordingFragmentDisplayed = true;

            // get rid of floating button
            mFloatingActionButton.setVisibility(GONE);

            // show back button
            mBackButton.setVisibility(VISIBLE);

            // show the background tiny to separate
            fadeBackground.setVisibility(VISIBLE);
            fadeBackground.setClickable(true);
            fadeBackground.setFocusable(true);
            fadeBackground.animate().alpha(0.5f);
    }

    /**
     * Closes the fragment.
     */
    public void closeFragment() {
        // Get the FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Check to see if the fragment is already showing
        RecordingFragment recordingFragment =
                (RecordingFragment) fragmentManager.findFragmentById(R.id.fragment_container);
        if (recordingFragment != null) {
            // Create and commit the transaction to remove the fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(recordingFragment).commit();
        }

        // Set boolean flag to indicate fragment is closed
        isRecordingFragmentDisplayed = false;

        // show the floating action button
        mFloatingActionButton.setVisibility(VISIBLE);

        // hide back button
        mBackButton.setVisibility(GONE);

        // get rid of separation
        fadeBackground.setVisibility(GONE);
        fadeBackground.setClickable(false);
        fadeBackground.setFocusable(false);

        loadSavedRecordings(uid, new OnRetrieveListListener() {
            @Override
            public void onListComplete(String response) {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Displays the renaming fragment.
     */
    public void displayRenameFragment(int position) {
        // Create a new fragment
        RenameFragment renameFragment = RenameFragment.newInstance(position);

        // Get fragment and start transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Add the RenameFragment
        fragmentTransaction.add(R.id.fragment_container, renameFragment)
                .addToBackStack(null).commit();

        // set the boolean to true to know it's showing
        isRenamingFragmentDisplayed = true;

        // get rid of floating button
        mFloatingActionButton.setVisibility(GONE);

        // show back button
        mBackButton.setVisibility(VISIBLE);

        // show the background tiny to separate
        fadeBackground.setVisibility(VISIBLE);
        fadeBackground.setClickable(true);
        fadeBackground.setFocusable(true);
        fadeBackground.animate().alpha(0.5f);
    }

    /**
     * Closes the renaming fragment.
     */
    public void closeRenameFragment() {
        // Get the FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Check to see if the fragment is already showing
        RenameFragment renameFragment =
                (RenameFragment) fragmentManager.findFragmentById(R.id.fragment_container);
        if (renameFragment != null) {
            // Create and commit the transaction to remove the fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(renameFragment).commit();
        }

        try {
            hideSoftKeyboard(HomeActivity.this);
        } catch (NullPointerException e) {
            // No keyboard to hide
        }

        // Set boolean flag to indicate fragment is closed
        isRenamingFragmentDisplayed = false;

        // show the floating action button
        mFloatingActionButton.setVisibility(VISIBLE);

        // hide back button
        mBackButton.setVisibility(GONE);

        // get rid of separation
        fadeBackground.setVisibility(GONE);
        fadeBackground.setClickable(false);
        fadeBackground.setFocusable(false);

        loadSavedRecordings(uid, new OnRetrieveListListener() {
            @Override
            public void onListComplete(String response) {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Handles clicking an item on the list.
     *
     * @param item The recording the user selected to play
     */
    @Override
    public void onListFragmentInteraction(RecordingItem item) {
        // Start the recording playback activity
        Intent intent = new Intent(this, RecordingPlaybackActivity.class);

        // Get info from the item and put in into the extra bundle
        Uri recordingId = item.uri;
        Log.d("ID", item.date);
        Log.d("URI TO PASS", item.uri.toString());
        intent.putExtra(EXTRA_RECORDING, recordingId.toString());
        intent.putExtra(EXTRA_DATE, item.date);

        // add flag to reorder to front
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        // recyclerView.setClickable(false);
        this.startActivity(intent);
    }

    /**
     * When user long clicks an item on the list
     */
    @Override
    public void onListFragmentLongInteraction(int position) {
        // display the fragment
        displayRenameFragment(position);
    }


    /**
     * Signs out the user from the current session.
     */
    public void signOut() {
        // Get an Auth UI instance and start the sign out process
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Bring back to main activity
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });
    }

    /**
     * On resuming to view, load the saved files.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ON RESUME", uid);
        // Run this on UI thread alone
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadSavedRecordings(uid, new OnRetrieveListListener() {
                    @Override
                    public void onListComplete(String response) {
                        mAdapter.notifyDataSetChanged();
                    }
                });
                // loadSavedRecordings();
                Log.d("ON RESUME", "RESUMED saved recordings");
                // Log.d("FILE LOCATION", getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
                Log.d("DATA", "Dataset changed ");
            }
        });

    }

    /**
     * On back pressed, close the fragment
     */
    @Override
    public void onBackPressed(){
        // Don't call super.onBackPressed() so that it doesn't go back to sign in page
        // super.onBackPressed();

        // Close the fragment just like when pressing back button
        closeFragment();
    }

    /**
     * Actually add the menu onto the options.
     *
     * @param menu The menu to be created
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, which adds items to the action bar if present
        getMenuInflater().inflate(R.menu.actionbar, menu);
        menu.removeItem(R.id.action_settings);

        if (mUser.getEmail() == null || mUser.getEmail().equals("")) {
            menu.removeItem(R.id.action_sign_out);
        } else {
            Log.d("EMAIL", mUser.getEmail());
            menu.removeItem(R.id.action_add_account);
        }
        return true;
    }

    /**
     * If the user selects and item from the app bar, this function handles any choices and chooses
     * actions to perform depending on what is chosen (in this case, just signing out).
     *
     * @param item the menu item that was selected
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_account:
                Intent intent = new Intent(this, AnonAuthActivity.class);
                intent.putExtra(MainActivity.UID, uid);
                startActivity(intent);
                return true;
            case R.id.action_sign_out:
                Log.d("SIGN OUT", "I want to sign out!");
                signOut();
                return true;
            case R.id.action_settings:
                Log.d("SETTINGS", "GET ME YOUR MANAGER");
                return true;
            default:
                // Action not recognized, calls the superclass's version to handle it
                return super.onOptionsItemSelected(item);
        }
    }

    // item touch helper for the list items
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback
            (0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // Remove the object and reference the thing to be removed
            removeItem(viewHolder.getAdapterPosition());

            // Notify the adapter that dataset has changed
            mAdapter.notifyDataSetChanged();
        }
    };
}