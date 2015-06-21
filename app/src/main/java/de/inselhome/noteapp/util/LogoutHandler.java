package de.inselhome.noteapp.util;

import android.app.Activity;
import android.content.Intent;

import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.activity.LoginActivity;

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
        activity.startActivity(new Intent(activity, LoginActivity.class));
    }
}
