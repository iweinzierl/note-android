package de.inselhome.noteapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.google.common.base.Strings;
import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.rest.NoteAppClient;
import de.inselhome.noteapp.security.Credentials;
import org.slf4j.Logger;


public class LoginActivity extends Activity {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("LoginActivity");

    private AutoCompleteTextView userView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;
    private CheckBox stayLoggedIn;

    private UserLoginTask userLoginTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (stayLoggedIn()) {
            startActivity(new Intent(this, NoteOverview.class));
        }

        stayLoggedIn = (CheckBox) findViewById(R.id.save_login);
        userView = (AutoCompleteTextView) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Button emailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private boolean isUserValid(String user) {
        return !Strings.isNullOrEmpty(user);
    }

    private boolean isPasswordValid(String password) {
        return !Strings.isNullOrEmpty(password);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void attemptLogin() {
        if (userLoginTask != null) {
            return;
        }

        userView.setError(null);
        passwordView.setError(null);

        final String username = userView.getText().toString();
        final String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            userView.setError(getString(R.string.error_field_required));
            focusView = userView;
            cancel = true;
        } else if (!isUserValid(username)) {
            userView.setError(getString(R.string.error_invalid_email));
            focusView = userView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            userLoginTask = new UserLoginTask(NoteApp.get(this).getNoteAppClient(), username, password);
            userLoginTask.execute((Void) null);
        }
    }

    private void reset() {
        userLoginTask = null;
        showProgress(false);
    }

    private boolean stayLoggedIn() {
        final NoteApp app = NoteApp.get(this);
        final Credentials credentials = app.loadCredentials();

        if (credentials != null) {
            app.getNoteAppClient().login(credentials.getUsername(), credentials.getPassword());
            return true;
        } else {
            return false;
        }
    }

    private void saveCredentialsIfSelected() {
        if (stayLoggedIn.isChecked()) {
            final String username = userView.getText().toString();
            final String password = passwordView.getText().toString();

            NoteApp.get(this).saveCredentials(username, password);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final NoteAppClient noteAppClient;
        private final String username;
        private final String password;

        protected UserLoginTask(final NoteAppClient noteAppClient, final String username, final String password) {
            this.noteAppClient = noteAppClient;
            this.username = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return noteAppClient.login(username, password);
            } catch (Exception e) {
                LOGGER.warn("Login failed: {}", e, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Boolean authenticated) {
            reset();

            if (authenticated) {
                saveCredentialsIfSelected();
                startActivity(new Intent(LoginActivity.this, NoteOverview.class));
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}
