package de.inselhome.noteapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import de.inselhome.noteapp.rest.NoteAppClient;
import de.inselhome.noteapp.rest.impl.CacheableNoteAppClient;
import de.inselhome.noteapp.rest.impl.NoteAppClientImpl;
import de.inselhome.noteapp.security.Credentials;
import de.inselhome.noteapp.service.UpdateOverviewWidgetService;

/**
 * @author iweinzierl
 */
public class NoteApp extends Application {

    private static final String SHARED_PREFERENCES = "noteapp.credentials";

    //public static final String BASE_URL = "http://192.168.192.53:8088";
    public static final String BASE_URL = "http://inselhome.org:8088";

    private NoteAppClient noteAppClient;

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, UpdateOverviewWidgetService.class));
    }

    public static NoteApp get(final Activity activity) {
        final Application application = activity.getApplication();
        if (application instanceof NoteApp) {
            return (NoteApp) application;
        }

        return null;
    }

    public NoteAppClient getNoteAppClient() {
        if (noteAppClient == null) {
            noteAppClient = new CacheableNoteAppClient(this, new NoteAppClientImpl(getBackendUrl()), getCacheDir());
        }

        return noteAppClient;
    }

    public void saveCredentials(final String username, final String password) {
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        new Credentials(username, password).saveInSharedPreferences(sharedPreferences);
    }

    public Credentials loadCredentials() {
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return Credentials.fromSharedPreferences(sharedPreferences);
    }

    public void deleteCredentials() {
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        Credentials.deleteFromSharedPreferences(sharedPreferences);
    }

    private String getBackendUrl() {
        // TODO return backend url from configuration
        return BASE_URL;
    }
}
