package de.inselhome.noteapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Strings;

import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.R;


public class LoginActivity extends Activity {

    private AutoCompleteTextView userView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userView = (AutoCompleteTextView) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });

        final Button emailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private boolean isUserValid(String user) {
        return !Strings.isNullOrEmpty(user);
    }

    private boolean isPasswordValid(String password) {
        return !Strings.isNullOrEmpty(password);
    }

    public void login() {
        final String username = userView.getText().toString();
        final String password = passwordView.getText().toString();

        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
        }

        if (TextUtils.isEmpty(username)) {
            userView.setError(getString(R.string.error_field_required));
            focusView = userView;
        } else if (!isUserValid(username)) {
            userView.setError(getString(R.string.error_invalid_email));
            focusView = userView;
        }

        if (focusView != null) {
            focusView.requestFocus();
        } else {
            saveCredentials();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void saveCredentials() {
        final String username = userView.getText().toString();
        final String password = passwordView.getText().toString();

        NoteApp.get(this).saveCredentials(username, password);
    }
}
