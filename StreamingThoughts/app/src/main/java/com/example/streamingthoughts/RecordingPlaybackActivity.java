package com.example.streamingthoughts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;

import java.io.IOException;

public class RecordingPlaybackActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl {

    private TextView mNowPlaying; // text view to show now playing
    private MediaPlayer mMediaPlayer; // media player for our recording
    private MediaController controller; // controller for our recording
    private int mCurrentPosition = 0; // saves current position of the audio
    private String stringUri; // saves the Uri of the file to be played
    private String audioDate; // saves the date of the file to be played
    private static final String PLAYBACK_TIME = "play_time"; // key for instance bundle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_playback);

        // Get the intent that started the activity and extract the Uri of the audio
        Intent intent = getIntent();
        stringUri = intent.getStringExtra(HomeActivity.EXTRA_RECORDING);
        audioDate = intent.getStringExtra(HomeActivity.EXTRA_DATE);

        Log.d("PASSED IN URI", stringUri);

        // Get the text from xml and date from file
        mNowPlaying = findViewById(R.id.day_of_recording);
        mNowPlaying.setText(audioDate);

        // Get the time from saved instance
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }

        // set new mediaController
        controller = new MediaController(this);
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.audio_player));
        controller.setEnabled(true);
    }

    /**
     * When the window has focus again, show controller
     * @param hasFocus whether activity has focus or not
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Check if controller exists and is showing
        if(controller != null && !controller.isShowing()) {
            controller.requestFocus();
            controller.show(0);
        }
    }

    /**
     * When the application starts, initialize player.
     */
    @Override
    protected void onStart() {
        super.onStart();

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(stringUri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            seekTo(mCurrentPosition);

        } catch (IOException e) {
            Log.e("ERROR", "Ripperino, file no exist");
        }

    }

    /**
     * When the application resumes, resume playback.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * When the application pauses for any reason, pause the player.
     */
    @Override
    public void onPause() {
        super.onPause();

        Log.d("PAUSE", "I'm pausing");
        controller.hide();
        mMediaPlayer.pause();
        mCurrentPosition = getCurrentPosition();
    }

    /**
     * When the application stops for any reason, stop the player.
     */
    @Override
    public void onStop() {
        super.onStop();

        controller.hide();
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        Log.d("STOP", "Stopping");
    }

    /**
     * Saves the time in a previous instance.
     */
    protected void onSaveInstanceState(Bundle outState) {
        // Save current state
        outState.putInt(PLAYBACK_TIME, mCurrentPosition);
        super.onSaveInstanceState(outState);
    }

    /**
     * A media controller function, starts playing the audio.
     */
    @Override
    public void start() {
        mMediaPlayer.start();
    }

    /**
     * A media controller function, pauses the audio.
     */
    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    /**
     * A media controller function, gets the duration of the audio.
     * @return How long file is
     */
    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /**
     * A media controller function, gets current position of the audio.
     * @return Where the audio stopped or is at
     */
    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * A media controller function, moves the video to a certain point.
     * @param i When the video should be seeked to
     */
    @Override
    public void seekTo(int i) {
        mMediaPlayer.seekTo(i);
    }

    /**
     * A media controller function, gets whether media is playing or not.
     * @return True or false depending on if audio is playing
     */
    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /**
     * No idea what this means, left alone.
     * @return 0 (as default)
     */
    @Override
    public int getBufferPercentage() {
        return 0;
    }

    /**
     * A media controller function, allows the controller to pause the audio.
     * @return True
     */
    @Override
    public boolean canPause() {
        return true;
    }

    /**
     * A media controller function, allows the controller to seek.
     * @return True
     */
    @Override
    public boolean canSeekBackward() {
        return true;
    }

    /**
     * A media controller function, allows the controller to seek.
     * @return True
     */
    @Override
    public boolean canSeekForward() {
        return true;
    }

    /**
     * No idea what this means, left alone.
     * @return 0 (as default)
     */
    @Override
    public int getAudioSessionId() {
        return 0;
    }
}