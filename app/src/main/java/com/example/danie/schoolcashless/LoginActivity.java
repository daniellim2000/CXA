package com.example.danie.schoolcashless;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.schoolcashless.model.UserSession;
import com.example.danie.schoolcashless.model.exception.BadAuthenticationException;
import com.example.danie.schoolcashless.model.exception.BadResponseException;
import com.victor.loading.rotate.RotateLoading;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText mUsernameView, mPasswordView;
    Button mRegisterView, mLoginView;
    TextView mForgotView;
    RotateLoading mRotateLoadingView;
    View mLoginFormView;

    UserLoginTask mAuthTask = null;

    boolean showLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameView = (TextInputEditText) findViewById(R.id.login_username);
        mPasswordView = (TextInputEditText) findViewById(R.id.login_password);
        mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mLoginView = (Button) findViewById(R.id.login_btn_login);
        mRegisterView = (Button) findViewById(R.id.login_btn_register);
        mForgotView = (TextView) findViewById(R.id.login_btn_forgot);
        mRotateLoadingView = (RotateLoading) findViewById(R.id.rotateloading);
        mLoginFormView = findViewById(R.id.login_form);

        mLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mRegisterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    private void login() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        /*if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        if (mUsernameView.length() == 0) {
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
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            } catch (BadAuthenticationException e) {
                e.printStackTrace();
                return 2;
            }

            return 200;
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mAuthTask = null;
            showProgress(false);

            if (success == 200) {
                finish();
                Intent intent = new Intent(LoginActivity.this, SavingsActivity.class);
                LoginActivity.this.startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "" + success, Toast.LENGTH_SHORT).show();
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
            mRotateLoadingView.start();
        } else {
            mRotateLoadingView.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.VISIBLE);
            mRotateLoadingView.stop();
        }
    }
}
