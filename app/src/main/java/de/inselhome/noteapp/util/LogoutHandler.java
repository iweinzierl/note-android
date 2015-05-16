package de.inselhome.noteapp.util;

import android.app.Activity;
import de.inselhome.noteapp.NoteApp;

/**
 * @author iweinzierl
 */
public class LogoutHandler {

    private final Activity activity;

    public LogoutHandler(Activity activity) {
        this.activity = activity;
    }

    public void logout() {
        NoteApp.get(activity).deleteCredentials();
        activity.finish();
    }
}
