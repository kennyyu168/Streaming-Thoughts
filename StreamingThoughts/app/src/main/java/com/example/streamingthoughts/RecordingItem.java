package com.example.streamingthoughts;

import android.net.Uri;

import java.util.Date;

/**
 * Class used for each item to be inserted
 */
public class RecordingItem {
    public Uri uri; // Uri for the recording
    public String date; // Date for the recording
    public long time; // Actual date
    public String fileName; // File name for recording
    public String title; // Display name

    public long getTime() {
        return time;
    }
    public String getName() { return fileName; }
}
