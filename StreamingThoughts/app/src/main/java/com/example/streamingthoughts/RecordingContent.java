package com.example.streamingthoughts;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Date;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class RecordingContent {

    private static final String TITLE = "title";

    /**
     * An array of recordings.
     */
    public static final List<RecordingItem> listOfRecordings = new ArrayList<>();
    private static OnRetrieveListListener mListener;
    private static String userId;

    /**
     * Load the saved recordings.
     */
    public static void loadSavedRecordings(String uid, OnRetrieveListListener listener) {
        // set uid
        userId = uid;

        // Log the current amount of recordings
        Log.d("RECORDINGS BEFORE", listOfRecordings.size() + "");

        // Clear the list of recordings
        listOfRecordings.clear();

        // Gets the listener
        mListener = listener;

        // list the files out
        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("audio/" + uid);

        // List all items from list
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {

                // Lists out the items
                for (StorageReference item : listResult.getItems()) {

                    // Creates the uri, time, and name variable for getting data on recording
                    final Uri[] mUri = new Uri[1];
                    final long[] time = new long[1];
                    final String[] name = new String[1];

                    // Creates a final reference
                    final StorageReference mReference = item;

                    // all items under the list
                    mReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Get absolute path to file
                            String absolutePath = uri.toString();

                            // get uri of the file
                            mUri[0] = uri;
                            Log.d("PATH", absolutePath);

                            // Get the date metadata
                            mReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {

                                    // Get metadata for the recording to display to user
                                    time[0] = storageMetadata.getCreationTimeMillis();
                                    String title = storageMetadata.getCustomMetadata(TITLE);
                                    String fileName = storageMetadata.getName();

                                    // Tells listener that metadata is finished being retrieved
                                    mListener.onListComplete("Complete");

                                    // Load the recording onto the application
                                    loadRecording(mUri[0], time[0], title, fileName);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FAIL TO DL", "Oh no, failed to download");
                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("List error", "Oh no, can't list items");
            }
        });

    }

    /**
     * Loads a new recording from the folder.
     *
     * @param file The recording file to be added into the list
     */
    public static void loadRecording(/*File*/ Uri file, long time, String name, String fileName) {
        Log.d("LOAD", "I'm being created");
        // Creates a new recording item
        RecordingItem newItem = new RecordingItem();

        // Gets the file name
        newItem.fileName = fileName;

        // Gets the display name
        newItem.title = name;

        // Gets the Uri of the file
        newItem.uri = file;

        // Gets the date of the file
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d, yyyy hh:mm:ss aa");
        Date newDate = new Date(time);
        newItem.time = time;
        newItem.date = dateFormat.format(newDate);
        Log.d("Date", newItem.date);

        Log.d("LIST OF RECORDING", listOfRecordings.toString());

        // Adds item ot our list
        addItem(newItem);

        Log.d("COUNT" , listOfRecordings.size() + "");

        // Sort the list
        Collections.sort(listOfRecordings, new Comparator<RecordingItem>() {
            @Override
            public int compare(RecordingItem recordingItem, RecordingItem t1) {
                if (recordingItem.getTime() > t1.getTime()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    /**
     * Adds an item to the front of the list of items.
     *
     * @param item The item to be added to the list
     */
    private static void addItem(RecordingItem item) {
        listOfRecordings.add(0, item);
    }

    /**
     * Removes an item from the list and from Firebase.
     *
     * @param position The position of the item in the list
     */
    public static void removeItem(int position) {
        Log.d("POSITION", position + "");

        // Create a storage reference to get item
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the file to delete
        StorageReference itemRef = storageRef.child("audio/" +
                userId + "/" + listOfRecordings.get(position).getName());

        // Get Uid of current user
        Log.d("USER ID", userId);

        // delete the file
        itemRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // file deleted successfully
                Log.d("SUCCESS", "File deleted!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // And error occurred, log an error
                Log.e("ERROR", "Item could not be deleted!");
            }
        });

        // remove listing from its position in the list only after item itself is deleted
        listOfRecordings.remove(position);

    }

    /**
     * Renames the item by changing its "title" metadata field
     *
     * @param position The position of the item to be modified
     * @param newName The new title of the item to be modified
     */
    public static void renameItem(int position, String newName) {
        Log.d("POSITION", position + "");

        // Create a reference to the file to delete
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the file to delete
        StorageReference itemRef = storageRef.child("audio/" +
                userId + "/" + listOfRecordings.get(position).getName());

        // Build the metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata(TITLE, newName).build();

        // Update the metadata
        itemRef.updateMetadata(metadata).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FAILURE", "FAILED TO CHANGE NAME");
            }
        }).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // file name changed successfully
                Log.d("SUCCESS", "File title changed!");
            }
        });

        // Change the name on the list
        listOfRecordings.get(position).title = newName;
    }
}