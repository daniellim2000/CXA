package com.example.danie.schoolcashless;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;
import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONException;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText mUsernameView, mPasswordView;
    Button mRegisterView, mLoginView;
    TextView mForgotView;
    ProgressBar mRotateLoadingView;
    View mLoginFormView;

    UserLoginTask mAuthTask = null;

    String username, password;

    boolean showLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameView = (TextInputEditText) findViewById(R.id.login_username);
        mPasswordView = (TextInputEditText) findViewById(R.id.login_password);
        mLoginView = (Button) findViewById(R.id.login_btn_login);
        mRegisterView = (Button) findViewById(R.id.login_btn_register);
        mForgotView = (TextView) findViewById(R.id.login_btn_forgot);
        mRotateLoadingView = (ProgressBar) findViewById(R.id.rotateloading);
        mLoginFormView = findViewById(R.id.login_form);

        mLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store values at the time of the login attempt.
                username = mUsernameView.getText().toString();
                password = mPasswordView.getText().toString();
                login();
            }
        });

        mRegisterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        mForgotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgot();
            }
        });

        SharedPreferences prefs = getSharedPreferences("CREDENTIALS", MODE_PRIVATE);
        username = prefs.getString("email", null);
        password = prefs.getString("password", null);
        Log.d("LOGIN", "Username: " + username);
        Log.d("LOGIN", "Password: " + password);
        if (username != null && password != null) {
            login();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUsernameView.setError(null);
        mPasswordView.setError(null);
    }

    private void login() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        /*if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        if (username.length() == 0) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private void register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        LoginActivity.this.startActivity(intent);
    }

    private void forgot() {
        Intent intent = new Intent(LoginActivity.this, ForgotActivity.class);
        LoginActivity.this.startActivity(intent);
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                UserSession.createInstance(mEmail, mPassword);
            } catch (BadResponseException e) {
                e.printStackTrace();
                return R.string.error_misbehaving;
            } catch (IOException e) {
                e.printStackTrace();
                return R.string.error_connection;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return R.string.error_authenticate;
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mAuthTask = null;
            showProgress(false);

            if (success == 200) {
                SharedPreferences.Editor editor = getSharedPreferences("CREDENTIALS", MODE_PRIVATE).edit();
                editor.putString("email", username);
                editor.putString("password", password);
                editor.commit();
                finish();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
            } else {
                Snackbar.make(findViewById(R.id.parent), success, Snackbar.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void showProgress(boolean show) {
        showLoading = show;
        if (show) {
            mRotateLoadingView.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
        } else {
            mRotateLoadingView.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.VISIBLE);
        }
    }
}
