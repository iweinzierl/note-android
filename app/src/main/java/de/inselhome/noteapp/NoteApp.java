package de.inselhome.noteapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import de.inselhome.noteapp.data.NoteAppClient;
import de.inselhome.noteapp.data.impl.LocalNoteAppClient;
import de.inselhome.noteapp.data.impl.remote.BufferedRemoteClient;
import de.inselhome.noteapp.data.impl.remote.RemoteNoteAppClient;
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
    private LocalNoteAppClient localNoteAppClient;
    private RemoteNoteAppClient remoteNoteAppClient;

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

        throw new RuntimeException("Application is no instance of NoteApp");
    }

    public NoteAppClient getNoteAppClient() {
        if (noteAppClient == null) {
            // TODO evaluate settings (shall remote server be used or not)
            noteAppClient = getRemoteNoteAppClient();
            //noteAppClient = getLocalNoteAppClient();
        }

        return noteAppClient;
    }

    public LocalNoteAppClient getLocalNoteAppClient() {
        if (localNoteAppClient == null) {
            localNoteAppClient = new LocalNoteAppClient(this);

            final Credentials credentials = loadCredentials();
            if (credentials != null) {
                localNoteAppClient.setUsername(loadCredentials().getUsername());
                localNoteAppClient.setPassword(loadCredentials().getPassword());
            }
        }

        return localNoteAppClient;
    }

    public RemoteNoteAppClient getRemoteNoteAppClient() {
        if (remoteNoteAppClient == null) {
            remoteNoteAppClient = new BufferedRemoteClient(getApplicationContext(), getBackendUrl(), getLocalNoteAppClient());

            final Credentials credentials = loadCredentials();
            if (credentials != null) {
                remoteNoteAppClient.setUsername(loadCredentials().getUsername());
                remoteNoteAppClient.setPassword(loadCredentials().getPassword());
            }
        }

        return remoteNoteAppClient;
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
