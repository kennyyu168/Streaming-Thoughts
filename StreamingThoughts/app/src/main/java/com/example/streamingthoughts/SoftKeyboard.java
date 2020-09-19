package com.example.streamingthoughts;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility class for hiding keyboard because there's no simple .hide() method
 */
public class SoftKeyboard {

    /**
     * Class to hide the keyboard during current activity.
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.
                hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
