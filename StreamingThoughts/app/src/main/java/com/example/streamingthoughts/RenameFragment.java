package com.example.streamingthoughts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import static com.example.streamingthoughts.RecordingContent.renameItem;

/**
 * A new fragment to rename the file.
 */
public class RenameFragment extends Fragment {

    private static final String POSITION = "POSITION";

    private Button mRename; // the button to press to rename
    private EditText mNewName; // the new name to rename the item into

    private int mPosition; // position of the item in the list

    /**
     * Required empty public constructor.
     */
    public RenameFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_rename, container, false);

        // get the position
        if (getArguments().containsKey(POSITION)) {
            mPosition = getArguments().getInt(POSITION);
        }

        return rootView;
    }

    /**
     * As the view is created, get the buttons from the fragment and edit text.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the edit text from the view
        mNewName = view.findViewById(R.id.new_name);

        // Get button
        mRename = view.findViewById(R.id.change_button);

        // Add click listener into the change button
        mRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rename();
            }
        });

        // key listener for the edit text
        mNewName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    rename();
                }
                return false;
            }
        });
    }

    /**
     * Changes the name of the entry that was selected.
     */
    private void rename() {

        // Get the text on the new name EditText
        String newTitle = mNewName.getText().toString();

        try {
            // Delegate to the RecordingContent function
            renameItem(mPosition, newTitle);
            ((HomeActivity) Objects.requireNonNull(getActivity())).closeRenameFragment();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(
                    getActivity(), "Please enter a file name", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Instantiating the fragment and returning the fragment to the activity.
     *
     * @return The new instance of the fragment
     */
    public static RenameFragment newInstance(int position) {
        // Create a new fragment
        RenameFragment renameFragment = new RenameFragment();

        // Bundle our information
        Bundle arguments = new Bundle();
        arguments.putInt(POSITION, position);
        renameFragment.setArguments(arguments);
        return renameFragment;
    }


}