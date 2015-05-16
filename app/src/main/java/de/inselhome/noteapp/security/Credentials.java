package de.inselhome.noteapp.security;

import android.content.SharedPreferences;
import com.google.common.base.Preconditions;

/**
 * @author iweinzierl
 */
public class Credentials {

    private static final String LOCAL_STORGE_USERNAME = "noteapp.credentials.username";
    private static final String LOCAL_STORGE_PASSWORD = "noteapp.credentials.password";

    private final String username;
    private final String password;

    public Credentials(final String username, final String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void saveInSharedPreferences(final SharedPreferences preferences) {
        final SharedPreferences.Editor editPrefs = preferences.edit();
        editPrefs.putString(LOCAL_STORGE_USERNAME, username);
        editPrefs.putString(LOCAL_STORGE_PASSWORD, password);
        editPrefs.apply();
    }

    public static Credentials fromSharedPreferences(final SharedPreferences preferences) {
        final String username = preferences.getString(LOCAL_STORGE_USERNAME, null);
        final String password = preferences.getString(LOCAL_STORGE_PASSWORD, null);

        if (username != null && password != null) {
            return new Credentials(username, password);
        } else {
            return null;
        }
    }

    public static void deleteFromSharedPreferences(final SharedPreferences preferences) {
        final SharedPreferences.Editor editPrefs = preferences.edit();
        editPrefs.remove(LOCAL_STORGE_USERNAME);
        editPrefs.remove(LOCAL_STORGE_PASSWORD);
        editPrefs.apply();
    }
}
