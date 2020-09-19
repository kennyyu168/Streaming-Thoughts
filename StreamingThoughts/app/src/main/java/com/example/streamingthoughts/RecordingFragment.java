package com.example.streamingthoughts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static com.example.streamingthoughts.RecordingContent.loadSavedRecordings;

/**
 * Fragment that holds the audio recording side of the application.
 */
public class RecordingFragment extends Fragment {

    private Button mRecord, mDone, mClear; // stores all buttons in the fragment
    private MediaRecorder mediaRecorder; // our audio recorder
    private String outputFile; // file to be outputted to
    private FirebaseUser mUser; // user id
    private int recordingCount; // recordings left

    private FirebaseStorage mStorage; // reference to storage we are actually storing to

    // private final int READ_PERMISSION_CODE = 100; // permission code for reading storage
    // private final int WRITE_PERMISSION_CODE = 200; // permission code for writing to storage
    private static final String COUNT = "count"; // key for count

    private final int RECORD_PERMISSION_CODE = 1; // permission code for recording audio
    private int requestCode;
    private String[] permissions;
    private int[] grantResults;

    /**
     * Required empty public constructor.
     */
    public RecordingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        // get the uid
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get the count
        if (getArguments().containsKey(COUNT)) {
            recordingCount = getArguments().getInt(COUNT);
        }

        return rootView;
    }

    /**
     * As the view is created, get the buttons from the fragment and start up our recorder.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the interface buttons
        mRecord = view.findViewById(R.id.record_button);
        mDone = view.findViewById(R.id.finish_button);
        mClear = view.findViewById(R.id.clear_button);

        // Get the storage reference from firebase
        mStorage = FirebaseStorage.getInstance();

        // Disable stop and play button when activity is created
        mDone.setEnabled(false);
        mClear.setEnabled(false);

        // Set the file output
        outputFile = this.getActivity().getExternalFilesDir(null).getAbsolutePath() +
                "/" + getDateAndTime() + ".3gp";

        // Make new audio recorder
        mediaRecorder = new MediaRecorder();

        // set listener for record button
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Request permissions
                Log.d("PERMISSIONS", String.valueOf(ContextCompat.checkSelfPermission(
                        Objects.requireNonNull(getActivity()), Arrays.toString(new String[]
                        {Manifest.permission.RECORD_AUDIO}))));
                if (ContextCompat.checkSelfPermission(getActivity(), Arrays.toString(new String[]
                        {Manifest.permission.RECORD_AUDIO}))
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("RECORD_CLICK", "onClick: ");
                    requestPermissions(new String[]
                                    {Manifest.permission.RECORD_AUDIO}, RECORD_PERMISSION_CODE);
                } else {
                    recordAudio();
                }
            }

        });

        // set listener for stop button
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop and release the recorder
                mediaRecorder.stop();

                // Enable the clear, disable the done button
                mDone.setEnabled(false);
                mClear.setEnabled(true);

                // Upload audio
                uploadEntry();

                Toast.makeText(getActivity(), "Recording Ended Successfully",
                        Toast.LENGTH_SHORT).show();
                if (mUser.getEmail() == null || mUser.getEmail().equals("")) {
                    Toast.makeText(getActivity(), "You have " + (3 - recordingCount) +
                            " recordings left", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set listener for clear button
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Enable the recording button
                mClear.setEnabled(false);
                mRecord.setEnabled(true);
            }
        });

    }

    /**
     * Instantiating the Fragment and returning the fragment to the activity.
     *
     * @return The new instance of the fragment
     */
    public static RecordingFragment newInstance(int recordingsLeft) {
        // Create a new fragment
        RecordingFragment recordingFragment = new RecordingFragment();

        // Bundle our information
        Bundle arguments = new Bundle();
        arguments.putInt(COUNT, recordingsLeft);
        recordingFragment.setArguments(arguments);
        return recordingFragment;
    }

    /**
     * Get the current date and time.
     *
     * @return Date and time currently in String format
     */
    public String getDateAndTime() {
        // Create a formatter for the date and time
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

        // Get current time
        LocalDateTime timeNow = LocalDateTime.now();

        // return the formatted date
        return dateTimeFormatter.format(timeNow);
    }

    /**
     * Handle the callback for requesting recording permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                          int[] grantResults) {
        Log.d("REQUEST CODE", requestCode + "");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // depending on request code
        if (requestCode == RECORD_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TIME TO RECORD", "onRequestPermissionsResult: ");
                recordAudio();
            } else {
                // Permission denied, disable functionality
                Toast.makeText(getActivity(), "Permission Denied to record",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * Records audio.
     */
    private void recordAudio() {
        if ((recordingCount > 2) && (mUser.getEmail() == null || mUser.getEmail().equals(""))) {
            Toast.makeText(getActivity(),
                    "Please make an account to record more",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), AnonAuthActivity.class);
            getActivity().startActivity(intent);
        } else {
            // Set sources and types of output
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setOutputFile(outputFile);

            Log.d("SET PATH", "source set!");
            try {
                // Initialize and start recorder
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IllegalStateException ise) {
                Log.d("ISE", "IllegalStateException");
            } catch (IOException ioe) {
                Log.d("IOE", "IOException");
            }

            // Disable the record button and enable Finish button and show text
            mRecord.setEnabled(false);
            mDone.setEnabled(true);
            mClear.setEnabled(false);

            Toast.makeText(getActivity(), "Recording started",
                    Toast.LENGTH_SHORT).show();

            // Increment recording count
            recordingCount++;
        }
    }

    /**
     * Helper method to store the recording onto our database
     */
    private void uploadEntry() {

        // Create a storage reference from our app
        final StorageReference recordingRef = mStorage.getReference().child("audio/"
                + mUser.getUid() + "/" + getDateAndTime() + ".3gp");

        // Get a Uri for the file
        Uri recordingUri = Uri.fromFile(new File(outputFile));

        // Start upload task
        UploadTask uploadTask = recordingRef.putFile(recordingUri);

        // Add observers to listen for when upload is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FAIL", "Crap, I'm not online");
                Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Add metadata
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setCustomMetadata("title", "Untitled").build();
                recordingRef.updateMetadata(metadata).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        Log.d("UPLOAD_SUCCESS", "I'm online!");
                        Toast.makeText(getActivity(), "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FAIL", "FAILED TO ADD METADATA");
                    }
                });
            }
        });
    }

    /**
     * On resume, recreate the media recorder.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
    }

    /**
     * On stop, release the media recorder.
     */
    @Override
    public void onStop() {
        super.onStop();
        mediaRecorder.release();
        mediaRecorder = null;
    }
}